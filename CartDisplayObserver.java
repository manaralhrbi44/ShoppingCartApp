/** Concrete Observer #1: prints a live summary every time the cart changes. */
public class CartDisplayObserver implements CartObserver {
    private final ShoppingCart cart;
    public CartDisplayObserver(ShoppingCart cart) { this.cart = cart; }

    @Override
    public void update(CartEvent event, Product product) {
        String what = (event == CartEvent.CLEARED) ? "Cart cleared"
                    : (event == CartEvent.ADDED ? "Added " : "Removed ") + product.getName();
        System.out.printf("  [CartDisplay]  %s -> %d item(s), subtotal SAR %.2f%n",
                what, cart.getItems().size(), cart.getSubtotal());
    }
}
