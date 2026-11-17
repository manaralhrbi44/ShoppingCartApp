/**
 * Checkout = the CONTEXT of the Strategy pattern.
 * It does not know HOW a payment is done; it delegates to whichever
 * PaymentStrategy was selected at run time.
 */
public class Checkout {
    public static final double VAT_RATE = 0.15;   // Saudi VAT 15%

    private final ShoppingCart cart;
    private PaymentStrategy strategy;

    public Checkout(ShoppingCart cart) { this.cart = cart; }

    public void setPaymentStrategy(PaymentStrategy strategy) { this.strategy = strategy; }

    public double getSubtotal()   { return cart.getSubtotal(); }
    public double getVat()        { return getSubtotal() * VAT_RATE; }
    public double getGrandTotal() { return getSubtotal() + getVat(); }

    public PaymentResult processPayment() {
        if (cart.isEmpty())    return new PaymentResult(false, "Cart is empty", null);
        if (strategy == null)  return new PaymentResult(false, "No payment method selected", null);
        PaymentResult result = strategy.pay(getGrandTotal());
        if (result.success()) cart.clear();      // Observers are notified (CLEARED)
        return result;
    }
}
