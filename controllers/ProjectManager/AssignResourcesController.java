package controllers.ProjectManager;

import model.ProjectManager.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.HashMap;


public class AssignResourcesController {

    @FXML private ComboBox<String> cmbProject;
    @FXML private ComboBox<String> cmbResource;
    @FXML private ListView<String> lstAssigned;
    @FXML private Button btnBack;

    private HashMap<String, Integer> projectIdMap = new HashMap<>();
    private HashMap<String, Integer> resourceIdMap = new HashMap<>();

    @FXML
    public void initialize() {
        loadProjects();
        loadResources();
        cmbProject.setOnAction(e -> loadAssignedResources());
    }

    private void loadProjects() {
        try (Connection conn = DatabaseConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT id, name FROM projects")) {

            while (rs.next()) {
                String name = rs.getString("name");
                cmbProject.getItems().add(name);
                projectIdMap.put(name, rs.getInt("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadResources() {
        try (Connection conn = DatabaseConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                     "SELECT id, name FROM resources")) {

            while (rs.next()) {
                String name = rs.getString("name");
                cmbResource.getItems().add(name);
                resourceIdMap.put(name, rs.getInt("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void assignResource() {
        String project = cmbProject.getValue();
        String resource = cmbResource.getValue();

        if (project == null || resource == null) {
            alert("Please select a project and a resource.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            int projectId = projectIdMap.get(project);
            int resourceId = resourceIdMap.get(resource);

            // Check if already assigned
            PreparedStatement check = conn.prepareStatement(
                    "SELECT * FROM assigned_resources WHERE project_id = ? AND resource_id = ?");
            check.setInt(1, projectId);
            check.setInt(2, resourceId);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                alert("Resource is already assigned to this project!");
                return;
            }

            // Insert assignment
            try (PreparedStatement pst = conn.prepareStatement(
                    "INSERT INTO assigned_resources (project_id, resource_id) VALUES (?, ?)")) {
                pst.setInt(1, projectId);
                pst.setInt(2, resourceId);
                pst.executeUpdate();
            }

            alert("Resource assigned successfully!");
            loadAssignedResources();

        } catch (Exception e) {
            e.printStackTrace();
            alert("Error: " + e.getMessage());
        }
    }

    private void loadAssignedResources() {
        lstAssigned.getItems().clear();

        if (cmbProject.getValue() == null) return;

        try {
            int projectId = projectIdMap.get(cmbProject.getValue());
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pst = conn.prepareStatement(
                     "SELECT r.name FROM assigned_resources a " +
                     "JOIN resources r ON a.resource_id = r.id " +
                     "WHERE a.project_id = ?")) {
                pst.setInt(1, projectId);
                ResultSet rs = pst.executeQuery();

                while (rs.next()) {
                    lstAssigned.getItems().add(rs.getString("name"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }

      @FXML
    private void goBack() {
        try {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController dashboardController = loader.getController();
            User currentUser = App.getCurrentUser();
            if (currentUser != null) dashboardController.setUsername(currentUser.getUsername());

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
