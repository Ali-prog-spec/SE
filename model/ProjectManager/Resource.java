package model.ProjectManager;
public class Resource {

    private int id;
    private String name;
    private String type;
    private String unit;
    private double totalQuantity;
    private double usedQuantity;

    public Resource() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(double totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public double getUsedQuantity() {
        return usedQuantity;
    }

    public void setUsedQuantity(double usedQuantity) {
        this.usedQuantity = usedQuantity;
    }

    // Optional — helps when displaying available quantity
    public double getRemainingQuantity() {
        return totalQuantity - usedQuantity;
    }
}
