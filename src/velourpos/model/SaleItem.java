package velourpos.model;

/** A single line item within a Sale */
public class SaleItem {
    private Product product;
    private int     quantity;
    private double  unitPrice;

    public SaleItem(Product product, int quantity, double unitPrice) {
        this.product   = product;
        this.quantity  = quantity;
        this.unitPrice = unitPrice;
    }

    public double getLineTotal() { return unitPrice * quantity; }

    public Product getProduct()   { return product; }
    public int     getQuantity()  { return quantity; }
    public double  getUnitPrice() { return unitPrice; }
}