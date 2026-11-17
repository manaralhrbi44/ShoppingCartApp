/** Electronics product: has a warranty period. */
public class Electronics extends Product {
    private final int warrantyMonths;

    public Electronics(String name, double price, int warrantyMonths) {
        super(name, price);
        this.warrantyMonths = warrantyMonths;
    }

    @Override public String getCategory() { return "Electronics"; }
    @Override public String describe()    { return getName() + " (" + warrantyMonths + "-month warranty)"; }
}
