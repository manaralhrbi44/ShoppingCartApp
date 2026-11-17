/**
 * FACTORY PATTERN
 * Single place that knows how to build each concrete Product.
 * Client code only says WHAT it wants ("book") and not HOW to build it,
 * so new product types can be added here without touching the rest of the app.
 */
public class ProductFactory {

    /**
     * @param type   "electronics", "book" or "clothing" (case-insensitive)
     * @param name   product name
     * @param price  price in SAR
     * @param extra  type-specific attribute: warranty months (electronics),
     *               author (book) or size (clothing)
     */
    public static Product create(String type, String name, double price, String extra) {
        if (type == null) throw new IllegalArgumentException("Product type is required");
        switch (type.toLowerCase()) {
            case "electronics": return new Electronics(name, price, Integer.parseInt(extra));
            case "book":        return new Book(name, price, extra);
            case "clothing":    return new Clothing(name, price, extra);
            default: throw new IllegalArgumentException("Unknown product type: " + type);
        }
    }
}
