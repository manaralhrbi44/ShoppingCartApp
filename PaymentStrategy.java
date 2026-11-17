/** STRATEGY PATTERN - common interface for every payment method. */
public interface PaymentStrategy {
    String getName();
    PaymentResult pay(double amount);
}
