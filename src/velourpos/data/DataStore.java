package velourpos.data;

import velourpos.model.*;
import velourpos.model.Product.Category;
import velourpos.model.Sale.PaymentMethod;

import java.time.LocalDateTime;
import java.util.*;

/**
 * In-memory data store seeded with realistic VELOUR demo data.
 * In a production app this would connect to a database.
 */
public class DataStore {

    private static DataStore instance;

    private List<Product> products = new ArrayList<>();
    private List<Sale>    sales    = new ArrayList<>();
    private int nextProductId = 100;
    private int nextSaleId    = 1000;

    private DataStore() {
        seedProducts();
        seedSales();
    }

    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    // ── Products ──────────────────────────────────────────────────────────────
    private void seedProducts() {
        products.add(new Product(nextProductId++, "VLR-TOP-001", "Cashmere Ribbed Turtleneck", "Velour",  Category.TOPS,        "M",   "Ivory",       45.00,  129.99, 24, 5));
        products.add(new Product(nextProductId++, "VLR-TOP-002", "Silk Wrap Blouse",           "Velour",  Category.TOPS,        "S",   "Dusty Rose",  38.00,  109.99, 18, 5));
        products.add(new Product(nextProductId++, "VLR-TOP-003", "Linen Oversized Shirt",      "Velour",  Category.TOPS,        "L",   "Sand",        22.00,   72.99, 35, 8));
        products.add(new Product(nextProductId++, "VLR-TOP-004", "Merino Knit Vest",           "Velour",  Category.TOPS,        "XS",  "Forest",      34.00,   89.99,  3, 5));
        products.add(new Product(nextProductId++, "VLR-BOT-001", "Wide-Leg Tailored Trousers", "Velour",  Category.BOTTOMS,     "M",   "Charcoal",    55.00,  149.99, 20, 5));
        products.add(new Product(nextProductId++, "VLR-BOT-002", "Pleated Midi Skirt",         "Velour",  Category.BOTTOMS,     "S",   "Caramel",     40.00,  119.99, 14, 5));
        products.add(new Product(nextProductId++, "VLR-BOT-003", "Slim Cropped Denim",         "AGOLDE",  Category.BOTTOMS,     "28",  "Indigo",      65.00,  168.00, 22, 6));
        products.add(new Product(nextProductId++, "VLR-DRS-001", "Bias-Cut Satin Midi",        "Velour",  Category.DRESSES,     "M",   "Champagne",   70.00,  219.99,  8, 4));
        products.add(new Product(nextProductId++, "VLR-DRS-002", "Linen Shirt Dress",          "Velour",  Category.DRESSES,     "L",   "Ecru",        48.00,  139.99, 11, 4));
        products.add(new Product(nextProductId++, "VLR-DRS-003", "Velvet Wrap Dress",          "Velour",  Category.DRESSES,     "XS",  "Bordeaux",    82.00,  249.99,  2, 4));
        products.add(new Product(nextProductId++, "VLR-OUT-001", "Longline Wool Coat",         "Velour",  Category.OUTERWEAR,   "M",   "Camel",      145.00,  389.99,  9, 3));
        products.add(new Product(nextProductId++, "VLR-OUT-002", "Quilted Puffer Vest",        "Velour",  Category.OUTERWEAR,   "L",   "Slate",       65.00,  179.99, 16, 4));
        products.add(new Product(nextProductId++, "VLR-OUT-003", "Leather Moto Jacket",        "IRO",     Category.OUTERWEAR,   "S",   "Black",      210.00,  549.99,  4, 3));
        products.add(new Product(nextProductId++, "VLR-ACC-001", "Silk Scarf",                 "Velour",  Category.ACCESSORIES, "OS",  "Multi",       18.00,   59.99, 40, 10));
        products.add(new Product(nextProductId++, "VLR-ACC-002", "Leather Belt",               "Velour",  Category.ACCESSORIES, "OS",  "Tan",         25.00,   79.99, 28, 8));
        products.add(new Product(nextProductId++, "VLR-ACC-003", "Gold Hoop Earrings",         "Mejuri",  Category.ACCESSORIES, "OS",  "Gold",        30.00,   89.99, 17, 6));
        products.add(new Product(nextProductId++, "VLR-FOO-001", "Leather Loafers",            "ATP Atelier", Category.FOOTWEAR, "38", "Black",      90.00,  249.99,  6, 3));
        products.add(new Product(nextProductId++, "VLR-FOO-002", "Suede Ankle Boots",          "ATP Atelier", Category.FOOTWEAR, "39", "Cognac",    105.00,  289.99,  0, 3));
    }

    private void seedSales() {
        Random rng = new Random(42);
        String[] cashiers = {"Amara K.", "Sofia R.", "James T."};
        PaymentMethod[] methods = PaymentMethod.values();

        // Generate 60 days of sales history
        for (int day = 59; day >= 0; day--) {
            int dailySales = 3 + rng.nextInt(6);
            for (int s = 0; s < dailySales; s++) {
                List<SaleItem> items = new ArrayList<>();
                int itemCount = 1 + rng.nextInt(3);
                for (int i = 0; i < itemCount; i++) {
                    Product p = products.get(rng.nextInt(products.size()));
                    int qty = 1 + rng.nextInt(2);
                    items.add(new SaleItem(p, qty, p.getSellingPrice()));
                }
                double discount = rng.nextInt(10) < 2 ? (10 + rng.nextInt(4)) * 5.0 : 0;
                LocalDateTime ts = LocalDateTime.now()
                        .minusDays(day)
                        .withHour(9 + rng.nextInt(10))
                        .withMinute(rng.nextInt(60));
                sales.add(new Sale(
                        nextSaleId++, ts, items, discount,
                        methods[rng.nextInt(methods.length)],
                        cashiers[rng.nextInt(cashiers.length)]
                ));
            }
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────
    public List<Product> getProducts()                   { return Collections.unmodifiableList(products); }
    public List<Sale>    getSales()                      { return Collections.unmodifiableList(sales); }

    public void addProduct(Product p)                    { products.add(p); }
    public int  nextProductId()                          { return nextProductId++; }

    public void updateProduct(Product updated) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == updated.getId()) {
                products.set(i, updated);
                return;
            }
        }
    }

    public void deleteProduct(int id) {
        products.removeIf(p -> p.getId() == id);
    }

    public double getTodayRevenue() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return sales.stream()
                .filter(s -> s.getTimestamp().isAfter(startOfDay))
                .mapToDouble(Sale::getTotal).sum();
    }

    public double getMonthRevenue() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        return sales.stream()
                .filter(s -> s.getTimestamp().isAfter(startOfMonth))
                .mapToDouble(Sale::getTotal).sum();
    }

    public long getTodayTransactions() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return sales.stream().filter(s -> s.getTimestamp().isAfter(startOfDay)).count();
    }

    public int getLowStockCount() {
        return (int) products.stream()
                .filter(p -> p.getStatus() == Product.Status.LOW_STOCK
                          || p.getStatus() == Product.Status.OUT_OF_STOCK)
                .count();
    }

    /** Daily revenue for the last N days (index 0 = oldest) */
    public double[] getDailyRevenue(int days) {
        double[] rev = new double[days];
        for (Sale s : sales) {
            long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(
                    s.getTimestamp().toLocalDate(), LocalDateTime.now().toLocalDate());
            if (daysAgo >= 0 && daysAgo < days) {
                rev[(int)(days - 1 - daysAgo)] += s.getTotal();
            }
        }
        return rev;
    }

    /** Units sold per category */
    public Map<String, Integer> getSalesByCategory() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Product.Category c : Product.Category.values()) map.put(c.name(), 0);
        for (Sale s : sales)
            for (SaleItem i : s.getItems()) {
                String key = i.getProduct().getCategory().name();
                map.merge(key, i.getQuantity(), Integer::sum);
            }
        return map;
    }
}