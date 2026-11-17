/** Concrete Strategy #1: credit card payment with basic validation (length + Luhn check). */
public class CreditCardPayment implements PaymentStrategy {
    private final String cardNumber;
    private final String holder;
    private final String cvv;

    public CreditCardPayment(String cardNumber, String holder, String cvv) {
        this.cardNumber = cardNumber == null ? "" : cardNumber.replaceAll("[\\s-]", "");
        this.holder = holder;
        this.cvv = cvv == null ? "" : cvv;
    }

    @Override public String getName() { return "Credit Card"; }

    @Override
    public PaymentResult pay(double amount) {
        if (amount <= 0) return new PaymentResult(false, "Invalid amount", null);
        if (!cardNumber.matches("\\d{16}") || !luhnValid(cardNumber))
            return new PaymentResult(false, "Card declined: invalid card number", null);
        if (!cvv.matches("\\d{3}"))
            return new PaymentResult(false, "Card declined: invalid CVV", null);
        String masked = "**** **** **** " + cardNumber.substring(12);
        return new PaymentResult(true,
                String.format("Paid SAR %.2f by credit card %s (%s)", amount, masked, holder),
                "CC-" + cardNumber.substring(12) + "-" + (int) (amount * 100));
    }

    /** Standard Luhn checksum used by real card numbers. */
    static boolean luhnValid(String digits) {
        int sum = 0;
        boolean alt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (alt) { d *= 2; if (d > 9) d -= 9; }
            sum += d;
            alt = !alt;
        }
        return sum % 10 == 0;
    }
}
