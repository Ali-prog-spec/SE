package controllers.InventoryManager;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.InventoryManager.InventoryDAO;
import model.InventoryManager.InventoryItem;

import java.sql.SQLException;

public class ManageInventoryController {

    // =========================
    // FXML injected controls
    // =========================
    @FXML private Button btnAddItem;
    @FXML private TextField searchField;
    @FXML private BorderPane rootPane;

    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, String> colName;
    @FXML private TableColumn<InventoryItem, String> colCategory;
    @FXML private TableColumn<InventoryItem, String> colQuantity;
    @FXML private TableColumn<InventoryItem, String> colMinStock;
    @FXML private TableColumn<InventoryItem, String> colStatus;
    @FXML private TableColumn<InventoryItem, InventoryItem> colActions;

    private ObservableList<InventoryItem> inventoryList = FXCollections.observableArrayList();
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    // =========================
    // Hover methods (for FXML)
    // =========================
    @FXML
    public void hoverIn(MouseEvent event) {
        Object source = event.getSource();
        if (source instanceof Button btn) {
            btn.setStyle("-fx-background-color: #1d4ed8; -fx-text-fill: white;");
        }
    }

    @FXML
    public void hoverOut(MouseEvent event) {
        Object source = event.getSource();
        if (source instanceof Button btn) {
            btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
        }
    }

    @FXML
    public void initialize() {
        loadInventoryFromDB();

        // Bind columns
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colCategory.setCellValueFactory(c -> c.getValue().categoryProperty());
        colQuantity.setCellValueFactory(c -> c.getValue().quantityProperty());
        colMinStock.setCellValueFactory(c -> c.getValue().minStockProperty());
        colStatus.setCellValueFactory(c -> c.getValue().statusProperty());

        // Status column coloring
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText("");
                    setStyle("");
                    return;
                }

                InventoryItem item = getTableRow().getItem();
                if (item.getQuantity() < item.getMinStock()) {
                    setText("Low Stock");
                    setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-alignment: CENTER;");
                    item.statusProperty().set("Low Stock");
                } else if (item.getQuantity() < item.getMinStock() * 1.2) {
                    setText("Near Min");
                    setStyle("-fx-background-color: #fff7ed; -fx-text-fill: #b45309; -fx-alignment: CENTER;");
                    item.statusProperty().set("Near Min");
                } else {
                    setText("In Stock");
                    setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-alignment: CENTER;");
                    item.statusProperty().set("In Stock");
                }
            }
        });

        // Actions column (Edit/Delete)
        colActions.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button delBtn = new Button("Delete");

            {
                editBtn.setStyle("-fx-background-color: #fbbf24; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");
                delBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");
                addHoverEffect(editBtn);
                addHoverEffect(delBtn);
            }

            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                editBtn.setOnAction(e -> openEditDialog(item));
                delBtn.setOnAction(e -> deleteItem(item));

                HBox box = new HBox(8, editBtn, delBtn);
                setGraphic(box);
            }
        });

        // Search filtering
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.toLowerCase();
            inventoryTable.setItems(inventoryList.filtered(item ->
                    item.getName().toLowerCase().contains(q) ||
                    item.getCategory().toLowerCase().contains(q)
            ));
        });

        addHoverEffect(btnAddItem);
        btnAddItem.setOnAction(e -> openAddDialog());
    }

    private void loadInventoryFromDB() {
        try {
            inventoryList.setAll(inventoryDAO.getAllInventory());
            inventoryTable.setItems(inventoryList);
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load inventory from database.");
            alert.showAndWait();
        }
    }

    // =========================
    // Add/Edit/Delete Dialogs
    // =========================
    private void openAddDialog() {
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Inventory Item");

        TextField name = new TextField();
        TextField category = new TextField();
        TextField quantity = new TextField();
        TextField minStock = new TextField();
        TextField unit = new TextField();

        VBox content = new VBox(10,
                new Label("Item Name:"), name,
                new Label("Category:"), category,
                new Label("Quantity:"), quantity,
                new Label("Min Stock:"), minStock,
                new Label("Unit:"), unit
        );
        content.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                int q = parseIntOrZero(quantity.getText());
                int m = parseIntOrZero(minStock.getText());
                String status = q < m ? "Low Stock" : "In Stock";
                InventoryItem newItem = new InventoryItem(name.getText(), category.getText(), q, unit.getText(), m, status);
                try {
                    inventoryDAO.addInventoryItem(newItem);
                    return newItem;
                } catch (SQLException e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to add item to database.");
                    alert.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(item -> inventoryList.add(item));
    }

    private void openEditDialog(InventoryItem item) {
        String oldName = item.getName();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Inventory Item");

        TextField name = new TextField(item.getName());
        TextField category = new TextField(item.getCategory());
        TextField quantity = new TextField(String.valueOf(item.getQuantity()));
        TextField minStock = new TextField(String.valueOf(item.getMinStock()));
        TextField unit = new TextField(item.getUnit());

        VBox content = new VBox(10,
                new Label("Item Name:"), name,
                new Label("Category:"), category,
                new Label("Quantity:"), quantity,
                new Label("Min Stock:"), minStock,
                new Label("Unit:"), unit
        );
        content.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                item.nameProperty().set(name.getText());
                item.categoryProperty().set(category.getText());
                item.quantityPropertyRaw().set(parseIntOrZero(quantity.getText()));
                item.minStockPropertyRaw().set(parseIntOrZero(minStock.getText()));
                item.unitProperty().set(unit.getText());
                item.statusProperty().set(parseIntOrZero(quantity.getText()) < parseIntOrZero(minStock.getText()) ? "Low Stock" : "In Stock");

                try {
                    inventoryDAO.updateInventoryItem(item, oldName);
                } catch (SQLException e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update item in database.");
                    alert.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();
        inventoryTable.refresh();
    }

    private void deleteItem(InventoryItem item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete " + item.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                try {
                    inventoryDAO.deleteInventoryItem(item);
                    inventoryList.remove(item);
                } catch (SQLException e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to delete item from database.");
                    alert.showAndWait();
                }
            }
        });
    }

    private int parseIntOrZero(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return 0; }
    }

    private void addHoverEffect(Button btn) {
        btn.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> btn.setOpacity(0.8));
        btn.addEventHandler(MouseEvent.MOUSE_EXITED, e -> btn.setOpacity(1));
    }
}
