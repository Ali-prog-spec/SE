package controllers.ProjectManager;

import model.ProjectManager.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;


public class RequestMaterialController {

    @FXML private ComboBox<Project> cmbProjects;
    @FXML private ComboBox<InventoryManager> cmbManagers;
    @FXML private VBox vboxMaterials;
    @FXML private ScrollPane scrollPane;
    @FXML private Button btnBack;

    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private final ObservableList<InventoryManager> managers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
    loadProjects();

    cmbProjects.setOnAction(e -> {
        Project selected = cmbProjects.getValue();
        if (selected != null) {
            loadManagers(selected.getId());
            loadMaterials(selected.getId());  // now loads assigned_resources
        }
    });
}


    // --------------------- LOAD PROJECTS ---------------------
    private void loadProjects() {
        projects.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            ResultSet rs = conn.prepareStatement("SELECT id, name FROM projects").executeQuery();
            while (rs.next()) {
                Project p = new Project();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                projects.add(p);
            }
            cmbProjects.setItems(projects);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading projects: " + e.getMessage());
        }
    }

    // --------------------- LOAD PROCUREMENT OFFICER ---------------------
    private void loadManagers(int projectId) {
        managers.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get procurement officer for the project
            PreparedStatement pst = conn.prepareStatement("SELECT procurement_officer FROM projects WHERE id = ?");
            pst.setInt(1, projectId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("procurement_officer");

                PreparedStatement pstUser = conn.prepareStatement("SELECT id, name FROM users WHERE id = ?");
                pstUser.setInt(1, userId);
                ResultSet rsUser = pstUser.executeQuery();

                if (rsUser.next()) {
                    InventoryManager manager = new InventoryManager();
                    manager.setId(rsUser.getInt("id"));
                    manager.setName(rsUser.getString("name"));
                    managers.add(manager);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Procurement officer not found in users table.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "No procurement officer assigned to this project.");
            }

            cmbManagers.setItems(managers);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading procurement officer: " + e.getMessage());
        }
    }

    // --------------------- LOAD MATERIAL CARDS ---------------------
  private void loadMaterials(int projectId) {
    vboxMaterials.getChildren().clear();

    String sql = """
        SELECT r.id, r.name, r.type
        FROM assigned_resources ar
        JOIN resources r ON ar.resource_id = r.id
        WHERE ar.project_id = ?
    """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setInt(1, projectId);

        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            int materialId = rs.getInt("id");
            String name = rs.getString("name");
            String unit = rs.getString("type");

            VBox card = createMaterialCard(materialId, name, unit);
            card.prefWidthProperty().bind(scrollPane.widthProperty().subtract(20));
            vboxMaterials.getChildren().add(card);
        }

    } catch (Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error loading materials: " + e.getMessage());
    }
}

    private VBox createMaterialCard(int materialId, String name, String unit) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color:white; -fx-padding:12; -fx-border-radius:10; -fx-background-radius:10; -fx-border-color:#dcdcdc;");

        Label lblName = new Label(name);
        lblName.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");

        TextField txtQuantity = new TextField();
        txtQuantity.setPromptText("Enter quantity (" + unit + ")");
        HBox.setHgrow(txtQuantity, Priority.ALWAYS);

        Button btnRequest = new Button("Request");
        btnRequest.setStyle("-fx-background-color:#4CAF50; -fx-text-fill:white;");
        btnRequest.setOnAction(e -> requestMaterial(materialId, txtQuantity.getText(), unit));

        HBox hbox = new HBox(10, txtQuantity, btnRequest);
        hbox.setPrefWidth(Double.MAX_VALUE);

        card.getChildren().addAll(lblName, hbox);
        return card;
    }

    // --------------------- INSERT MATERIAL REQUEST ---------------------
    private void requestMaterial(int materialId, String quantityStr, String unit) {
    Project selectedProject = cmbProjects.getValue();
    InventoryManager selectedManager = cmbManagers.getValue();

    if (selectedProject == null || selectedManager == null) {
        showAlert(Alert.AlertType.ERROR, "Select project and procurement officer.");
        return;
    }

    double quantity;
    try {
        quantity = Double.parseDouble(quantityStr);
        if (quantity <= 0) throw new NumberFormatException();
    } catch (NumberFormatException ex) {
        showAlert(Alert.AlertType.ERROR, "Enter a valid positive number for quantity.");
        return;
    }

    User currentUser = App.getCurrentUser();
    if (currentUser == null) {
        showAlert(Alert.AlertType.ERROR, "No logged-in user found. Please login again.");
        return;
    }

    try (Connection conn = DatabaseConnection.getConnection()) {
        // Check existence of project, user, and material
        if (!exists(conn, "projects", selectedProject.getId()) ||
            !exists(conn, "users", selectedManager.getId()) ||
            !exists(conn, "resources", materialId)) {
            showAlert(Alert.AlertType.ERROR, "Invalid selection. Cannot send request.");
            return;
        }

        String sqlInsert = "INSERT INTO material_requests (project_id, procurement_officer, user_id, material_id, quantity, unit, status, processed, requestDate) " +
                   "VALUES (?,?,?,?,?,?, 'Pending',0,?)";

        PreparedStatement pstInsert = conn.prepareStatement(sqlInsert);
        pstInsert.setInt(1, selectedProject.getId());        // project_id
        pstInsert.setInt(2, selectedManager.getId());        // procurement_officer
        pstInsert.setInt(3, App.getCurrentUser().getUserId());            // user_id (SENDER)
        pstInsert.setInt(4, materialId);                     // material_id
        pstInsert.setDouble(5, quantity);                    // quantity
        pstInsert.setString(6, unit);                        // unit
        pstInsert.setTimestamp(7, new Timestamp(System.currentTimeMillis())); // requestDate
        pstInsert.executeUpdate(); 

        
            String msg = "You have a Request to Accept or Reject for user : " + App.getCurrentUser().getUsername() + 
            "and Project ID : " + selectedProject.getId();
           
        showAlert(Alert.AlertType.INFORMATION, "Material request sent successfully!");

    } catch (Exception ex) {
        ex.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error sending request: " + ex.getMessage());
    }
}


    // --------------------- CHECK IF RECORD EXISTS ---------------------
    private boolean exists(Connection conn, String table, int id) throws Exception {
        PreparedStatement pst = conn.prepareStatement("SELECT id FROM " + table + " WHERE id = ?");
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        return rs.next();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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

            Scene scene = new Scene(root, 1200, 750);
            scene.getStylesheets().add(getClass().getResource("/screens/ProjectManager/dashboard.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Dashboard");
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
