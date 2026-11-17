/** OBSERVER PATTERN - the Observer interface. */
public interface CartObserver {
    /** Called by the subject every time the cart changes. */
    void update(CartEvent event, Product product);
}
