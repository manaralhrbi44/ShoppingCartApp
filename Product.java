/**
 * Product - abstract base class for everything that can be put in the cart.
 * Concrete products are NEVER created with "new" by client code;
 * they are created through ProductFactory (Factory pattern).
 */
public abstract class Product {
    private final String name;
    private final double price;     // price in SAR

    protected Product(String name, double price) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Product name is required");
        if (price <= 0) throw new IllegalArgumentException("Price must be positive");
        this.name = name;
        this.price = price;
    }

    public String getName()  { return name; }
    public double getPrice() { return price; }

    /** Category label, used by the cart and by the recommendation component. */
    public abstract String getCategory();

    /** Human readable description (differs per product type). */
    public abstract String describe();

    @Override
    public String toString() {
        return String.format("%s [%s] - SAR %.2f", name, getCategory(), price);
    }
}
