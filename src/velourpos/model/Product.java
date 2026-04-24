package velourpos.model;

public class Product {

    public enum Category { TOPS, BOTTOMS, DRESSES, OUTERWEAR, ACCESSORIES, FOOTWEAR }
    public enum Status   { IN_STOCK, LOW_STOCK, OUT_OF_STOCK }

    private int    id;
    private String sku;
    private String name;
    private String brand;
    private Category category;
    private String size;
    private String color;
    private double costPrice;
    private double sellingPrice;
    private int    quantity;
    private int    reorderLevel;

    public Product(int id, String sku, String name, String brand,
                   Category category, String size, String color,
                   double costPrice, double sellingPrice,
                   int quantity, int reorderLevel) {
        this.id           = id;
        this.sku          = sku;
        this.name         = name;
        this.brand        = brand;
        this.category     = category;
        this.size         = size;
        this.color        = color;
        this.costPrice    = costPrice;
        this.sellingPrice = sellingPrice;
        this.quantity     = quantity;
        this.reorderLevel = reorderLevel;
    }

    // ── Derived ───────────────────────────────────────────────────────────────
    public Status getStatus() {
        if (quantity == 0)                  return Status.OUT_OF_STOCK;
        if (quantity <= reorderLevel)       return Status.LOW_STOCK;
        return Status.IN_STOCK;
    }

    public double getMargin() {
        if (sellingPrice == 0) return 0;
        return ((sellingPrice - costPrice) / sellingPrice) * 100;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int      getId()            { return id; }
    public String   getSku()           { return sku; }
    public String   getName()          { return name; }
    public void     setName(String n)  { name = n; }
    public String   getBrand()         { return brand; }
    public void     setBrand(String b) { brand = b; }
    public Category getCategory()      { return category; }
    public void     setCategory(Category c) { category = c; }
    public String   getSize()          { return size; }
    public void     setSize(String s)  { size = s; }
    public String   getColor()         { return color; }
    public void     setColor(String c) { color = c; }
    public double   getCostPrice()     { return costPrice; }
    public void     setCostPrice(double p) { costPrice = p; }
    public double   getSellingPrice()  { return sellingPrice; }
    public void     setSellingPrice(double p) { sellingPrice = p; }
    public int      getQuantity()      { return quantity; }
    public void     setQuantity(int q) { quantity = q; }
    public int      getReorderLevel()  { return reorderLevel; }
    public void     setReorderLevel(int r) { reorderLevel = r; }
}