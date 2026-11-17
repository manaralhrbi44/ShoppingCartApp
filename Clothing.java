/** Clothing product: has a size. */
public class Clothing extends Product {
    private final String size;

    public Clothing(String name, double price, String size) {
        super(name, price);
        this.size = size;
    }

    @Override public String getCategory() { return "Clothing"; }
    @Override public String describe()    { return getName() + " (size " + size + ")"; }
}
