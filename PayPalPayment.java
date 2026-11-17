/** Concrete Strategy #2: PayPal payment, identified by the account e-mail. */
public class PayPalPayment implements PaymentStrategy {
    private final String email;

    public PayPalPayment(String email) { this.email = email == null ? "" : email.trim(); }

    @Override public String getName() { return "PayPal"; }

    @Override
    public PaymentResult pay(double amount) {
        if (amount <= 0) return new PaymentResult(false, "Invalid amount", null);
        if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$"))
            return new PaymentResult(false, "PayPal login failed: invalid e-mail", null);
        return new PaymentResult(true,
                String.format("Paid SAR %.2f via PayPal account %s", amount, email),
                "PP-" + Math.abs(email.hashCode() % 100000) + "-" + (int) (amount * 100));
    }
}
