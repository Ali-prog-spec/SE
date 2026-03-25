
package controllers.ProjectManager;

import model.ProjectManager.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ManageInventoryController {

    @FXML private VBox vboxResources;
    @FXML private Button btnBack;

    private static final double WINDOW_WIDTH = 1200;
    private static final double WINDOW_HEIGHT = 700;

    @FXML
    public void initialize() {
        // Load resources once scene is ready
        Platform.runLater(this::loadResources);
    }

    // -------------------- LOAD ALL RESOURCES --------------------
    private void loadResources() {
        vboxResources.getChildren().clear();

        for (Resource r : ResourceDAO.getAllResources()) {
            vboxResources.getChildren().add(createResourceCard(r));
        }
    }

    // -------------------- CREATE RESOURCE CARD --------------------
    private VBox createResourceCard(Resource r) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color:white; -fx-padding:15; -fx-background-radius:10; -fx-border-color:#dcdcdc;");
        card.setMaxWidth(Double.MAX_VALUE);

        Label lblName = new Label("Name: " + r.getName());
        lblName.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

        Label lblType = new Label("Type: " + r.getType());
        Label lblUnit = new Label("Unit: " + r.getUnit());

        Button btnDelete = new Button("Delete");
        btnDelete.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white;");
        btnDelete.setOnAction(e -> {
            if (ResourceDAO.deleteResource(r.getId())) {
                showAlert("Resource deleted successfully!");
                loadResources();
            }
        });

        card.getChildren().addAll(lblName, lblType, lblUnit, btnDelete);
        return card;
    }

    // -------------------- OPEN ADD RESOURCE DIALOG --------------------
    @FXML
    private void openAddResourceDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Resource");

        TextField txtName = new TextField();
        txtName.setPromptText("Resource Name");

        TextField txtType = new TextField();
        txtType.setPromptText("Type (Material / Tool / etc.)");

        TextField txtUnit = new TextField();
        txtUnit.setPromptText("Unit (kg, ton, bags, etc.)");

        VBox box = new VBox(10, txtName, txtType, txtUnit);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    String name = txtName.getText().trim();
                    String type = txtType.getText().trim();
                    String unit = txtUnit.getText().trim();

                    if (name.isEmpty() || type.isEmpty() || unit.isEmpty()) {
                        showAlert("All fields are required!");
                        return null;
                    }

                    if (ResourceDAO.addResource(name, type, unit)) {
                        showAlert("Resource added successfully!");
                        loadResources();
                    } else {
                        showAlert("Failed to add resource!");
                    }

                } catch (Exception ex) {
                    showAlert("Invalid input!");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    // -------------------- GO BACK TO DASHBOARD --------------------
    @FXML
    private void goBack() {
        try {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController dashboardController = loader.getController();
            User currentUser = App.getCurrentUser();
            if (currentUser != null) {
                dashboardController.setUsername(currentUser.getUsername());
            }

            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            scene.getStylesheets().add(getClass().getResource("/screens/ProjectManager/dashboard.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Dashboard");
            stage.setResizable(false); // fixed size
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
