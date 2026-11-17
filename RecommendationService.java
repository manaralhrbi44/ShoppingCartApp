import java.util.List;

/**
 * Orchestrates the "You Might Also Like..." feature.
 * Tries the primary provider (AI); if it fails for ANY reason it silently falls back
 * to the secondary provider, so the checkout page never breaks.
 */
public class RecommendationService {

    /** What the UI receives: suggestions + where they came from + how long it took. */
    public record Result(List<String> suggestions, String source, long millis, String note) { }

    private final RecommendationProvider primary;
    private final RecommendationProvider fallback;

    public RecommendationService(RecommendationProvider primary, RecommendationProvider fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    public Result getRecommendations(List<Product> cartItems) {
        long start = System.currentTimeMillis();
        try {
            List<String> list = primary.recommend(cartItems);
            return new Result(list, primary.getName(), System.currentTimeMillis() - start, "OK");
        } catch (Exception e) {                       // any failure -> fallback
            List<String> list;
            try { list = fallback.recommend(cartItems); } catch (Exception e2) { list = List.of(); }
            return new Result(list, fallback.getName(), System.currentTimeMillis() - start,
                    "AI unavailable (" + e.getMessage() + ")");
        }
    }

    /** Console version of the "You Might Also Like..." widget. */
    public void display(List<Product> cartItems) {
        Result r = getRecommendations(cartItems);
        System.out.println("  ---- You Might Also Like... ----");
        for (String s : r.suggestions()) System.out.println("   * " + s);
        System.out.printf("  [source: %s | %d ms | %s]%n", r.source(), r.millis(), r.note());
    }
}
