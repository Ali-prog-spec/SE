package model.FinanceOfficer;
public class InvoiceItem {
    private String item;
    private double quantity;
    private double rate;
    private double tax;      // percentage
    private double total;    // auto-calculated

    public InvoiceItem(String item, double quantity, double rate, double tax) {
        this.item = item;
        this.quantity = quantity;
        this.rate = rate;
        this.tax = tax;
        calculateTotal();
    }

    public void calculateTotal() {
        double subtotal = quantity * rate;
        double taxAmount = subtotal * (tax / 100);
        this.total = subtotal + taxAmount;
    }

    // --- Getters and setters ---
    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public double getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
        calculateTotal();
    }

    public double getRate() { return rate; }
    public void setRate(double rate) { 
        this.rate = rate; 
        calculateTotal();
    }

    public double getTax() { return tax; }
    public void setTax(double tax) { 
        this.tax = tax; 
        calculateTotal();
    }

    public double getTotal() { return total; }
}
