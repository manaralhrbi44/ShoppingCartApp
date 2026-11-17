/** Book product: has an author. */
public class Book extends Product {
    private final String author;

    public Book(String name, double price, String author) {
        super(name, price);
        this.author = author;
    }

    @Override public String getCategory() { return "Book"; }
    @Override public String describe()    { return "\"" + getName() + "\" by " + author; }
}
