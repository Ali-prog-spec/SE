package model.InventoryManager;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class InventoryItem {

    private final SimpleStringProperty name;
    private final SimpleStringProperty category;
    private final SimpleIntegerProperty quantity;
    private final SimpleStringProperty unit;
    private final SimpleIntegerProperty minStock;
    private final SimpleStringProperty status;

    public InventoryItem(String name, String category, int quantity, String unit, int minStock, String status) {
        this.name = new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unit = new SimpleStringProperty(unit);
        this.minStock = new SimpleIntegerProperty(minStock);
        this.status = new SimpleStringProperty(status);
    }

    public SimpleStringProperty nameProperty() { return name; }
    public SimpleStringProperty categoryProperty() { return category; }
    public SimpleStringProperty quantityProperty() { return new SimpleStringProperty(quantity.get() + " " + unit.get()); }
    public SimpleStringProperty minStockProperty() { return new SimpleStringProperty(minStock.get() + " " + unit.get()); }
    public SimpleStringProperty statusProperty() { return status; }
    public SimpleIntegerProperty quantityPropertyRaw() { return quantity; }
    public SimpleIntegerProperty minStockPropertyRaw() { return minStock; }
    public SimpleStringProperty unitProperty() { return unit; }


    public String getName() { return name.get(); }
    public String getCategory() { return category.get(); }
    public int getQuantity() { return quantity.get(); }
    public String getUnit() { return unit.get(); }
    public int getMinStock() { return minStock.get(); }
    public String getStatus() { return status.get(); }
}
