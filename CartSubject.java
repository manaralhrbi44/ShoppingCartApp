import java.util.ArrayList;
import java.util.List;

/**
 * OBSERVER PATTERN - the Subject.
 * Keeps the list of observers and notifies all of them when something happens.
 * ShoppingCart extends this class, so the cart does not need to know who is listening.
 */
public abstract class CartSubject {
    private final List<CartObserver> observers = new ArrayList<>();

    public void attach(CartObserver observer) { observers.add(observer); }
    public void detach(CartObserver observer) { observers.remove(observer); }
    public int observerCount()                { return observers.size(); }

    protected void notifyObservers(CartEvent event, Product product) {
        for (CartObserver o : new ArrayList<>(observers)) {
            o.update(event, product);
        }
    }
}
