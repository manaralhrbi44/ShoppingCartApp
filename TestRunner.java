import java.util.List;

/** Lightweight test runner (no JUnit needed). Run: java TestRunner */
public class TestRunner {
    static int passed = 0, failed = 0;

    static void check(String name, boolean ok) {
        System.out.println((ok ? "  [PASS] " : "  [FAIL] ") + name);
        if (ok) passed++; else failed++;
    }

    /** A provider that always fails - simulates an AI outage. */
    static class BrokenProvider implements RecommendationProvider {
        public String getName() { return "Broken AI"; }
        public List<String> recommend(List<Product> c) throws RecommendationException {
            throw new RecommendationException("simulated timeout");
        }
    }

    static boolean throwsIllegalArg(Runnable r) {
        try { r.run(); return false; } catch (IllegalArgumentException e) { return true; }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== FACTORY tests ===");
        Product e = ProductFactory.create("electronics", "Laptop", 3500, "24");
        Product b = ProductFactory.create("BOOK", "Clean Code", 120, "Robert C. Martin");
        Product c = ProductFactory.create("clothing", "T-Shirt", 60, "L");
        check("Factory returns Electronics for 'electronics'", e instanceof Electronics);
        check("Factory returns Book for 'BOOK' (case-insensitive)", b instanceof Book);
        check("Factory returns Clothing for 'clothing'", c instanceof Clothing);
        check("Product keeps name and price", e.getName().equals("Laptop") && e.getPrice() == 3500);
        check("Factory rejects unknown type", throwsIllegalArg(() -> ProductFactory.create("car", "X", 1, "")));
        check("Factory rejects negative price", throwsIllegalArg(() -> ProductFactory.create("book", "X", -5, "A")));

        System.out.println("=== OBSERVER tests ===");
        ShoppingCart cart = new ShoppingCart();
        final int[] events = {0};
        final CartEvent[] last = {null};
        CartObserver spy = (ev, p) -> { events[0]++; last[0] = ev; };
        InventoryObserver inv = new InventoryObserver(5);
        cart.attach(spy);
        cart.attach(inv);
        cart.addProduct(e);
        check("Observer notified when item added", events[0] == 1 && last[0] == CartEvent.ADDED);
        check("InventoryObserver reduced stock (5 -> 4)", inv.getStock("Laptop") == 4);
        cart.removeProduct("Laptop");
        check("Observer notified when item removed", events[0] == 2 && last[0] == CartEvent.REMOVED);
        check("InventoryObserver restored stock (4 -> 5)", inv.getStock("Laptop") == 5);
        cart.detach(spy);
        cart.addProduct(b);
        check("Detached observer is no longer notified", events[0] == 2);
        check("Removing a missing item returns false", !cart.removeProduct("Nothing"));
        check("Cart subtotal is correct", cart.getSubtotal() == 120.0);

        System.out.println("=== STRATEGY tests ===");
        ShoppingCart cart2 = new ShoppingCart();
        cart2.addProduct(e);   // 3500
        cart2.addProduct(b);   // 120
        Checkout co = new Checkout(cart2);
        check("Grand total = subtotal + 15% VAT (4163.00)", Math.abs(co.getGrandTotal() - 4163.0) < 0.001);
        check("No strategy selected -> payment refused", !co.processPayment().success());
        co.setPaymentStrategy(new CreditCardPayment("4111 1111 1111 1111", "Student", "123"));
        PaymentResult ok = co.processPayment();
        check("CreditCardPayment accepts valid card", ok.success());
        check("Card number is masked in the message", ok.message().contains("**** **** **** 1111")
                && !ok.message().contains("4111 1111"));
        check("Cart is cleared after successful payment", cart2.isEmpty());
        cart2.addProduct(b);
        co.setPaymentStrategy(new CreditCardPayment("1234 5678 9012 3456", "Student", "123"));
        check("CreditCardPayment rejects invalid (Luhn) card", !co.processPayment().success());
        check("Failed payment keeps the cart", !cart2.isEmpty());
        co.setPaymentStrategy(new PayPalPayment("not-an-email"));
        check("PayPalPayment rejects invalid e-mail", !co.processPayment().success());
        co.setPaymentStrategy(new PayPalPayment("student@example.com"));  // strategy swapped at run time
        check("PayPalPayment accepts valid e-mail (strategy swapped at run time)", co.processPayment().success());
        check("Empty cart cannot be paid", !co.processPayment().success());

        System.out.println("=== AI RECOMMENDATION tests ===");
        List<Product> items = List.of(e, b);
        String prompt = AiRecommendationProvider.buildPrompt(items);
        check("Prompt starts with the required sentence",
                prompt.startsWith("Based on the following cart items, suggest additional products"));
        check("Prompt lists the cart items", prompt.contains("Laptop") && prompt.contains("Clean Code"));
        String sample = "{\"id\":\"msg_1\",\"content\":[{\"type\":\"text\",\"text\":"
                + "\"1. Laptop Sleeve - protects it\\n- Wireless Mouse - handy\\nUSB-C Hub - more ports\\nExtra line\"}]}";
        List<String> parsed = AiRecommendationProvider.parseSuggestions(sample);
        check("Parser extracts exactly 3 suggestions from API JSON", parsed.size() == 3);
        check("Parser strips numbering/bullets", parsed.get(0).equals("Laptop Sleeve - protects it")
                && parsed.get(1).equals("Wireless Mouse - handy"));
        boolean bad = false;
        try { AiRecommendationProvider.parseSuggestions("{\"error\":\"x\"}"); } catch (RecommendationException ex) { bad = true; }
        check("Parser rejects an unexpected response", bad);
        boolean noKey = false;
        try { new AiRecommendationProvider("", "m").recommend(items); } catch (RecommendationException ex) { noKey = true; }
        check("Missing API key raises an error (no crash)", noKey);

        String gReq = GeminiRecommendationProvider.buildRequestJson("Say \"hi\"\nplease");
        check("Gemini request JSON contains the escaped prompt",
                gReq.contains("\"text\":\"Say \\\"hi\\\"\\nplease\"") && gReq.startsWith("{\"contents\""));
        String gSample = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"1. Laptop Sleeve - protects it\\n2. Mouse - handy\\n3. USB Hub - more ports\"}],"
                + "\"role\":\"model\"},\"finishReason\":\"STOP\"}]}";
        List<String> gParsed = AiRecommendationProvider.parseSuggestions(gSample);
        check("Gemini response parsed into 3 clean suggestions",
                gParsed.size() == 3 && gParsed.get(1).equals("Mouse - handy"));
        boolean gNoKey = false;
        try { new GeminiRecommendationProvider("", "m").recommend(items); } catch (RecommendationException ex) { gNoKey = true; }
        check("Missing Gemini key raises an error (no crash)", gNoKey);

        RecommendationService svc = new RecommendationService(new BrokenProvider(), new RuleBasedRecommendationProvider());
        RecommendationService.Result res = svc.getRecommendations(items);
        check("FALLBACK used when AI fails", res.source().startsWith("Fallback"));
        check("Fallback returns 3 suggestions", res.suggestions().size() == 3);
        check("Fallback does not suggest items already in cart",
                res.suggestions().stream().noneMatch(s -> s.startsWith("Laptop - ") || s.startsWith("Clean Code")));
        check("Fallback responds fast (< 200 ms)", res.millis() < 200);
        check("Empty cart still gets suggestions",
                !svc.getRecommendations(List.of()).suggestions().isEmpty());

        System.out.println("\nRESULT: " + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
