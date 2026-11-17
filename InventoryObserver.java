import java.util.HashMap;
import java.util.Map;

/** Concrete Observer #2: reserves / releases stock when items enter or leave the cart. */
public class InventoryObserver implements CartObserver {
    private final Map<String, Integer> stock = new HashMap<>();
    private final int defaultStock;

    public InventoryObserver(int defaultStock) { this.defaultStock = defaultStock; }

    public int getStock(String productName) {
        return stock.getOrDefault(productName, defaultStock);
    }

    @Override
    public void update(CartEvent event, Product product) {
        if (product == null) return;
        int current = getStock(product.getName());
        if (event == CartEvent.ADDED) {
            stock.put(product.getName(), current - 1);
            int left = current - 1;
            System.out.printf("  [Inventory]    %s stock reserved, %d left%s%n", product.getName(), left,
                    left <= 2 ? "  (LOW STOCK WARNING)" : "");
        } else if (event == CartEvent.REMOVED) {
            stock.put(product.getName(), current + 1);
            System.out.printf("  [Inventory]    %s stock released, %d left%n", product.getName(), current + 1);
        }
    }
}
