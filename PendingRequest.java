package model.ProcurementOfficer;

import javafx.beans.property.*;

public class PendingRequest {

    private final StringProperty type;
    private final StringProperty item;
    private final StringProperty project;
    private final StringProperty requester;
    private final IntegerProperty quantity;
    private final StringProperty date;
    private final StringProperty actions;

    public PendingRequest(String type, String item, String project, String requester, int quantity, String date, String actions) {
        this.type = new SimpleStringProperty(type);
        this.item = new SimpleStringProperty(item);
        this.project = new SimpleStringProperty(project);
        this.requester = new SimpleStringProperty(requester);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.date = new SimpleStringProperty(date);
        this.actions = new SimpleStringProperty(actions);
    }

    public StringProperty typeProperty() { return type; }
    public StringProperty itemProperty() { return item; }
    public StringProperty projectProperty() { return project; }
    public StringProperty requesterProperty() { return requester; }
    public IntegerProperty quantityProperty() { return quantity; }
    public StringProperty dateProperty() { return date; }
    public StringProperty actionsProperty() { return actions; }

    public String getItem(){return item.get();} 
}
