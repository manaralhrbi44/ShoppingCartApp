/** Outcome of a payment attempt. */
public record PaymentResult(boolean success, String message, String reference) { }
