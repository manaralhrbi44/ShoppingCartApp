import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * FALLBACK provider: works offline with static, category-based suggestions.
 * Used automatically when the AI API is unavailable (no key, no network, timeout, bad response).
 */
public class RuleBasedRecommendationProvider implements RecommendationProvider {

    private static final Map<String, List<String>> BY_CATEGORY = Map.of(
        "Electronics", List.of("Laptop Sleeve - protects your device",
                               "Wireless Mouse - more comfortable work",
                               "USB-C Fast Charger - extra charging at home"),
        "Book",        List.of("Bookmark Set - keeps your place",
                               "Reading Lamp - better light for reading",
                               "Notebook - for notes while you read"),
        "Clothing",    List.of("Leather Belt - matches most outfits",
                               "Sports Socks (3 pack) - everyday comfort",
                               "Cap - completes the look"));

    private static final List<String> BESTSELLERS = List.of(
        "Wireless Earbuds - our best seller",
        "Water Bottle - popular everyday item",
        "Gift Card - always a safe choice");

    @Override public String getName() { return "Fallback (rule-based)"; }

    @Override
    public List<String> recommend(List<Product> cartItems) {
        Set<String> inCart = new LinkedHashSet<>();
        Set<String> categories = new LinkedHashSet<>();
        for (Product p : cartItems) {
            inCart.add(p.getName().toLowerCase());
            categories.add(p.getCategory());
        }
        List<String> pool = new ArrayList<>();
        for (String c : categories) pool.addAll(BY_CATEGORY.getOrDefault(c, List.of()));
        if (pool.isEmpty()) pool.addAll(BESTSELLERS);

        List<String> result = new ArrayList<>();
        for (String s : pool) {
            String name = s.split(" - ")[0].toLowerCase();
            if (!inCart.contains(name) && !result.contains(s)) result.add(s);
            if (result.size() == 3) break;
        }
        return result;
    }
}
