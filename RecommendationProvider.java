import java.util.List;

/** Strategy interface for "where do the suggestions come from?" (AI or rule based). */
public interface RecommendationProvider {
    String getName();
    List<String> recommend(List<Product> cartItems) throws RecommendationException;
}
