package model.ProcurementOfficer;

import javafx.beans.property.*;

public class MaterialRequest {

    private final IntegerProperty id;
    private final IntegerProperty materialId;
    private final IntegerProperty projectId;
    private final IntegerProperty procurementOfficerId;
    private final IntegerProperty userId;  // requester (you missed this earlier)
    private final IntegerProperty quantity;
    private final StringProperty unit;
    private final StringProperty status;
    private final ObjectProperty<java.sql.Timestamp> requestedDate;
    private final BooleanProperty processed;
    private final StringProperty materialName; // joined from material table

    public MaterialRequest(int id, int materialId, int projectId,
                           int procurementOfficerId, int userId,
                           int quantity, String unit, String status,
                           java.sql.Timestamp requestedDate,
                           boolean processed, String materialName) {

        this.id = new SimpleIntegerProperty(id);
        this.materialId = new SimpleIntegerProperty(materialId);
        this.projectId = new SimpleIntegerProperty(projectId);
        this.procurementOfficerId = new SimpleIntegerProperty(procurementOfficerId);
        this.userId = new SimpleIntegerProperty(userId);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unit = new SimpleStringProperty(unit);
        this.status = new SimpleStringProperty(status);
        this.requestedDate = new SimpleObjectProperty<>(requestedDate);
        this.processed = new SimpleBooleanProperty(processed);
        this.materialName = new SimpleStringProperty(materialName);
    }

    // ───────────────────────────────────────────────
    // JavaFX Properties
    // ───────────────────────────────────────────────
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty materialIdProperty() { return materialId; }
    public IntegerProperty projectIdProperty() { return projectId; }
    public IntegerProperty procurementOfficerIdProperty() { return procurementOfficerId; }
    public IntegerProperty userIdProperty() { return userId; }
    public IntegerProperty quantityProperty() { return quantity; }
    public StringProperty unitProperty() { return unit; }
    public StringProperty statusProperty() { return status; }
    public ObjectProperty<java.sql.Timestamp> requestedDateProperty() { return requestedDate; }
    public BooleanProperty processedProperty() { return processed; }
    public StringProperty materialNameProperty() { return materialName; }

    // ───────────────────────────────────────────────
    // Getters
    // ───────────────────────────────────────────────
    public int getId() { return id.get(); }
    public int getMaterialId() { return materialId.get(); }
    public int getProjectId() { return projectId.get(); }
    public int getProcurementOfficerId() { return procurementOfficerId.get(); }
    public int getUserId() { return userId.get(); }
    public int getQuantity() { return quantity.get(); }
    public String getUnit() { return unit.get(); }
    public String getStatus() { return status.get(); }
    public java.sql.Timestamp getRequestedDate() { return requestedDate.get(); }
    public boolean isProcessed() { return processed.get(); }
    public String getMaterialName() { return materialName.get(); }

    // ───────────────────────────────────────────────
    // Setters (only fields that should change)
    // ───────────────────────────────────────────────
    public void setStatus(String newStatus) { status.set(newStatus); }
    public void setProcessed(boolean p) { processed.set(p); }
}
