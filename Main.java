/**
 * Shopping cart demo - combines FACTORY + OBSERVER + STRATEGY
 * and an AI-powered recommendation component (with fallback).
 *
 * Run:  javac *.java && java Main
 * AI:   add GEMINI_API_KEY (or ANTHROPIC_API_KEY) in Replit "Secrets"; without a key the fallback is used.
 */
public class Main {
    public static void main(String[] args) {
        banner("1. FACTORY - creating products");
        Product laptop = ProductFactory.create("electronics", "Laptop", 3500.00, "24");
        Product book   = ProductFactory.create("book", "Clean Code", 120.00, "Robert C. Martin");
        Product shirt  = ProductFactory.create("clothing", "T-Shirt", 60.00, "L");
        Product mouse  = ProductFactory.create("electronics", "Wireless Mouse", 85.00, "12");
        for (Product p : new Product[]{laptop, book, shirt, mouse})
            System.out.println("  Created: " + p.describe() + "  -> " + p);

        banner("2. OBSERVER - cart notifies its observers");
        ShoppingCart cart = new ShoppingCart();
        cart.attach(new CartDisplayObserver(cart));
        cart.attach(new InventoryObserver(3));
        System.out.println("  Observers attached: " + cart.observerCount());
        cart.addProduct(laptop);
        cart.addProduct(book);
        cart.addProduct(shirt);
        System.out.println("  -- customer changes their mind --");
        cart.removeProduct("T-Shirt");
        cart.addProduct(mouse);

        banner("3. AI COMPONENT - smart recommendations on the checkout page");
        RecommendationService recommender = new RecommendationService(
                chooseAiProvider(), new RuleBasedRecommendationProvider());
        recommender.display(cart.getItems());

        banner("4. STRATEGY - choosing a payment method at run time");
        Checkout checkout = new Checkout(cart);
        System.out.printf("  Subtotal: SAR %.2f | VAT 15%%: SAR %.2f | Total: SAR %.2f%n",
                checkout.getSubtotal(), checkout.getVat(), checkout.getGrandTotal());

        System.out.println("  Attempt 1: credit card with a WRONG number");
        checkout.setPaymentStrategy(new CreditCardPayment("1234 5678 9012 3456", "Student", "123"));
        System.out.println("   -> " + checkout.processPayment().message());

        System.out.println("  Attempt 2: PayPal");
        checkout.setPaymentStrategy(new PayPalPayment("student@example.com"));
        PaymentResult r = checkout.processPayment();
        System.out.println("   -> " + r.message() + " [ref " + r.reference() + "]");
        System.out.println("  Cart empty after payment: " + cart.isEmpty());
    }

    /** Gemini if GEMINI_API_KEY is set, otherwise the Claude provider (which needs ANTHROPIC_API_KEY). */
    private static RecommendationProvider chooseAiProvider() {
        String gemini = System.getenv("GEMINI_API_KEY");
        if (gemini != null && !gemini.isBlank()) return new GeminiRecommendationProvider();
        return new AiRecommendationProvider();
    }

    private static void banner(String title) { System.out.println("\n=== " + title + " ==="); }
}
