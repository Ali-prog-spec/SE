package controllers.InventoryManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.InventoryManager.InventoryDAO;
import model.InventoryManager.InventoryItem;
import model.ProjectManager.App;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DashboardController {

    @FXML private BorderPane rootPane;
    @FXML private Label totalItemsLabel, okStockLabel, lowStockLabel, criticalStockLabel;
    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, String> colName, colCategory, colQuantity, colMinStock, colStatus;
    @FXML private VBox alertsContainer;
    @FXML private Button btnManageInventory, btnSidebarDashboard, btnSidebarManage, btnSidebarAlerts;
    @FXML private MenuItem logoutMenuItem;

    private final ObservableList<InventoryItem> inventoryItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Load inventory from database
        InventoryDAO dao = new InventoryDAO();
        try {
            List<InventoryItem> list = dao.getAllInventory();
            inventoryItems.setAll(list);
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load inventory from database.", ButtonType.OK);
            alert.showAndWait();
        }

        // Table columns
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colCategory.setCellValueFactory(data -> data.getValue().categoryProperty());
        colQuantity.setCellValueFactory(data -> data.getValue().quantityProperty());
        colMinStock.setCellValueFactory(data -> data.getValue().minStockProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        btnSidebarAlerts.setOnMouseClicked(this::onAlertsClick);

        inventoryTable.setItems(inventoryItems);

        // Update summary cards and alerts
        updateSummary();
        populateAlerts();

        // Sidebar and buttons
        btnManageInventory.setOnAction(e -> loadCenterContent("/screens/InventoryManager/ManageInventory.fxml"));
        btnSidebarDashboard.setOnAction(e -> loadCenterContent("/screens/InventoryManager/Dashboard.fxml"));
        btnSidebarManage.setOnAction(e -> loadCenterContent("/screens/InventoryManager/ManageInventory.fxml"));
        logoutMenuItem.setOnAction(e -> logout());
    }

    private void updateSummary() {
        int totalItems = inventoryItems.size();
        int okStock = (int) inventoryItems.stream().filter(i -> i.getStatus().equals("ok")).count();
        int lowStock = (int) inventoryItems.stream().filter(i -> i.getStatus().equals("low")).count();
        int criticalStock = (int) inventoryItems.stream().filter(i -> i.getStatus().equals("critical")).count();

        totalItemsLabel.setText(String.valueOf(totalItems));
        okStockLabel.setText(String.valueOf(okStock));
        lowStockLabel.setText(String.valueOf(lowStock));
        criticalStockLabel.setText(String.valueOf(criticalStock));
    }

    private void populateAlerts() {
        alertsContainer.getChildren().clear();
        for (InventoryItem item : inventoryItems) {
            if (!item.getStatus().equals("ok")) {
                HBox alertBox = new HBox(10);
                alertBox.setStyle("-fx-padding: 8; -fx-border-color: #ccc; -fx-background-color: " +
                        (item.getStatus().equals("critical") ? "#FEE2E2" : "#FFFBEB") +
                        "; -fx-border-radius: 5; -fx-background-radius: 5;");

                Label itemLabel = new Label(item.getName() + " - Current: " + item.getQuantity() + " " + item.getUnit()
                        + " | Min: " + item.getMinStock() + " " + item.getUnit());
                itemLabel.setTextFill(item.getStatus().equals("critical") ? Color.RED : Color.ORANGE);

                Button reorderBtn = new Button("Reorder Now");
                reorderBtn.setStyle(item.getStatus().equals("critical") ?
                        "-fx-background-color: #DC2626; -fx-text-fill: white;" :
                        "-fx-background-color: #FBBF24; -fx-text-fill: black;");

                alertBox.getChildren().addAll(itemLabel, reorderBtn);
                alertsContainer.getChildren().add(alertBox);
            }
        }
    }

    private void loadCenterContent(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent loadedRoot = loader.load();

            if (loadedRoot instanceof BorderPane) {
                BorderPane loadedPane = (BorderPane) loadedRoot;
                Node newTop = loadedPane.getTop();
                Node newCenter = loadedPane.getCenter();

                if (newCenter != null) rootPane.setCenter(newCenter);
                if (newTop != null) rootPane.setTop(newTop);
            } else {
                rootPane.setCenter(loadedRoot);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void onAlertsClick(MouseEvent e) {
      
         loadCenterContent("/screens/InventoryManager/notifications.fxml");
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            App.changeScene("Login", scene, "/screens/ProjectManager/login.css");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
