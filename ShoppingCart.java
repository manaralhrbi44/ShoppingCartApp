import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The cart itself. It is the concrete Subject of the Observer pattern. */
public class ShoppingCart extends CartSubject {
    private final List<Product> items = new ArrayList<>();

    public void addProduct(Product p) {
        items.add(p);
        notifyObservers(CartEvent.ADDED, p);
    }

    /** Removes the first product with the given name. Returns true if something was removed. */
    public boolean removeProduct(String name) {
        for (Product p : items) {
            if (p.getName().equalsIgnoreCase(name)) {
                items.remove(p);
                notifyObservers(CartEvent.REMOVED, p);
                return true;
            }
        }
        return false;
    }

    public void clear() {
        items.clear();
        notifyObservers(CartEvent.CLEARED, null);
    }

    public List<Product> getItems() { return Collections.unmodifiableList(items); }
    public boolean isEmpty()        { return items.isEmpty(); }

    public double getSubtotal() {
        double sum = 0;
        for (Product p : items) sum += p.getPrice();
        return sum;
    }
}
