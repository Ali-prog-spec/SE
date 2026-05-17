package controllers.InventoryManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import java.io.IOException;

public class MainController {

    @FXML private BorderPane rootPane;
    @FXML private Button btnDashboard;
    @FXML private Button btnManageInventory;

    // Keep reference to loaded content to avoid reloading unnecessarily
    private Node dashboardContent;
    private Node manageInventoryContent;

    @FXML
    public void initialize() {
        try {
            // Load Dashboard and ManageInventory FXML once
            dashboardContent = FXMLLoader.load(getClass().getResource("/screens/InventoryManager/Dashboard.fxml"));
            manageInventoryContent = FXMLLoader.load(getClass().getResource("/screens/InventoryManager/ManageInventory.fxml"));

            // Initially show Dashboard
            rootPane.setCenter(dashboardContent);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Button actions
        btnDashboard.setOnAction(e -> rootPane.setCenter(dashboardContent));
        btnManageInventory.setOnAction(e -> rootPane.setCenter(manageInventoryContent));
    }
}
