/** Thrown by a RecommendationProvider when it cannot produce suggestions. */
public class RecommendationException extends Exception {
    public RecommendationException(String message)                { super(message); }
    public RecommendationException(String message, Throwable why) { super(message, why); }
}
