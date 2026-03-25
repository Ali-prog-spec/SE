package controllers.FinanceOfficer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import model.FinanceOfficer.Invoice;
import model.FinanceOfficer.InvoiceItem;
import model.ProjectManager.DatabaseConnection;
import javafx.scene.control.Alert.AlertType;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateInvoiceFormController {

    @FXML private ComboBox<String> projectCombo;
    @FXML private TextField clientField;
    @FXML private DatePicker datePicker;
    @FXML private Button addItemBtn;
    @FXML private VBox itemRows;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML
    private TextField subtotalField;

    @FXML
    private TextField totalTaxField;

    @FXML
    private TextField grandTotalField;


    private Invoice invoice; // Invoice object will be created on first save/add
    private void loadProjectsFromDatabase() {
    try (var conn = DatabaseConnection.getConnection();
         var stmt = conn.createStatement();
         var rs = stmt.executeQuery("SELECT name FROM projects")) {

        while (rs.next()) {
            projectCombo.getItems().add(rs.getString("name"));
        }

    } catch (Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load projects: " + e.getMessage());
    }
}

    @FXML
    public void initialize() {
        loadProjectsFromDatabase();


        addItemBtn.setOnAction(e -> addItemRow());

        if (saveBtn != null) saveBtn.setOnAction(e -> saveInvoice());
        if (cancelBtn != null) cancelBtn.setOnAction(e -> clearForm());

        // Add initial row
        addItemRow();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
    }

    private void addItemRow() {
    GridPane row = new GridPane();
    row.setHgap(10);
    row.setVgap(10);
    row.getColumnConstraints().addAll(
            new ColumnConstraints(200),
            new ColumnConstraints(80),
            new ColumnConstraints(80),
            new ColumnConstraints(80),
            new ColumnConstraints(80), // for total
            new ColumnConstraints(40)  // for delete icon
    );

    TextField itemField = new TextField();
    itemField.setPromptText("Item name");

    Spinner<Double> qtySpinner = createNumericSpinner(0, 1000, 1, 1);
    Spinner<Double> rateSpinner = createNumericSpinner(0, 10000, 0, 0.5);
    Spinner<Double> taxSpinner = createNumericSpinner(0, 100, 0, 0.5);

    TextField totalField = new TextField("0.00");
    totalField.setEditable(false);
    totalField.setStyle("-fx-background-color: #eee;");

    // Delete button with trash icon
    Button deleteBtn = new Button("\uD83D\uDDD1"); // 🗑
    deleteBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 14; -fx-text-fill: red;");
    deleteBtn.setOnAction(e -> {
        itemRows.getChildren().remove(row);
        recalcInvoiceTotals();
    });

    // Add listeners to recalc totals
    qtySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
        recalcTotal(qtySpinner, rateSpinner, taxSpinner, totalField);
        recalcInvoiceTotals();
    });
    rateSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
        recalcTotal(qtySpinner, rateSpinner, taxSpinner, totalField);
        recalcInvoiceTotals();
    });
    taxSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
        recalcTotal(qtySpinner, rateSpinner, taxSpinner, totalField);
        recalcInvoiceTotals();
    });

    row.add(itemField, 0, 0);
    row.add(qtySpinner, 1, 0);
    row.add(rateSpinner, 2, 0);
    row.add(taxSpinner, 3, 0);
    row.add(totalField, 4, 0);
    row.add(deleteBtn, 5, 0);

    itemRows.getChildren().add(row);
}


    private Spinner<Double> createNumericSpinner(double min, double max, double initial, double step) {
        Spinner<Double> spinner = new Spinner<>(min, max, initial, step);
        spinner.setEditable(true);
        spinner.getValueFactory().setWrapAround(true);
        return spinner;
    }

    private void recalcTotal(Spinner<Double> qtySpinner, Spinner<Double> rateSpinner,
                             Spinner<Double> taxSpinner, TextField totalField) {
        double qty = qtySpinner.getValue();
        double rate = rateSpinner.getValue();
        double tax = taxSpinner.getValue();
        double subtotal = qty * rate;
        double total = subtotal + (subtotal * tax / 100);
        totalField.setText(String.format("%.2f", total));
    }

    private void recalcInvoiceTotals() {
        double subtotal = 0;
        double totalTax = 0;

        for (javafx.scene.Node node : itemRows.getChildren()) {
            if (node instanceof GridPane row) {
                Spinner<Double> qtySpinner = (Spinner<Double>) row.getChildren().get(1);
                Spinner<Double> rateSpinner = (Spinner<Double>) row.getChildren().get(2);
                Spinner<Double> taxSpinner = (Spinner<Double>) row.getChildren().get(3);

                double qty = qtySpinner.getValue();
                double rate = rateSpinner.getValue();
                double tax = taxSpinner.getValue();

                subtotal += qty * rate;
                totalTax += (qty * rate) * tax / 100;
            }
        }

    subtotalField.setText(String.format("%.2f", subtotal));
    totalTaxField.setText(String.format("%.2f", totalTax));
    grandTotalField.setText(String.format("%.2f", subtotal + totalTax));
    }


    private void saveInvoice() {
        // Lazy creation of invoice
        if (invoice == null) {
            invoice = new Invoice(projectCombo.getValue(), clientField.getText(), datePicker.getValue());
        } else {
            // Update details in case user edited them
            invoice.setProject(projectCombo.getValue());
            invoice.setClient(clientField.getText());
            invoice.setDate(datePicker.getValue());
        }

        // Clear previous items and add all current rows
        invoice.clearItems();

        for (javafx.scene.Node node : itemRows.getChildren()) {
            if (node instanceof GridPane) {
                GridPane row = (GridPane) node;

                TextField itemField = (TextField) row.getChildren().get(0);
                Spinner<Double> qtySpinner = (Spinner<Double>) row.getChildren().get(1);
                Spinner<Double> rateSpinner = (Spinner<Double>) row.getChildren().get(2);
                Spinner<Double> taxSpinner = (Spinner<Double>) row.getChildren().get(3);
                TextField totalField = (TextField) row.getChildren().get(4);

                InvoiceItem item = new InvoiceItem(
                        itemField.getText(),
                        qtySpinner.getValue(),
                        rateSpinner.getValue(),
                        taxSpinner.getValue()
                        
                );

                invoice.addItem(item);
            }
        }
        invoice.calculateTotals();
        // Optional: print invoice summary
        invoice.saveToDatabase();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Invoice generated successfully!");
        clearForm();
        System.out.println(invoice);
    }

    private void clearForm() {
        projectCombo.setValue(null);
        clientField.clear();
        datePicker.setValue(null);
        itemRows.getChildren().clear();
        invoice = null; // reset invoice
        addItemRow();
    }

    
}
