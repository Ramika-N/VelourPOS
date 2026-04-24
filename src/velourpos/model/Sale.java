package velourpos.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/** A completed sale transaction */
public class Sale {

    public enum PaymentMethod { CASH, CARD, MOBILE }

    private int           id;
    private LocalDateTime timestamp;
    private List<SaleItem> items;
    private double        discount;
    private PaymentMethod paymentMethod;
    private String        cashierName;

    public Sale(int id, LocalDateTime timestamp, List<SaleItem> items,
                double discount, PaymentMethod paymentMethod, String cashierName) {
        this.id            = id;
        this.timestamp     = timestamp;
        this.items         = items == null ? new ArrayList<>() : items;
        this.discount      = discount;
        this.paymentMethod = paymentMethod;
        this.cashierName   = cashierName;
    }

    public double getSubtotal() {
        return items.stream().mapToDouble(SaleItem::getLineTotal).sum();
    }

    public double getTotal() {
        return getSubtotal() - discount;
    }

    // Getters
    public int           getId()            { return id; }
    public LocalDateTime getTimestamp()     { return timestamp; }
    public List<SaleItem> getItems()        { return items; }
    public double        getDiscount()      { return discount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String        getCashierName()   { return cashierName; }
}