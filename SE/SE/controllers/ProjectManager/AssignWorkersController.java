package controllers.ProjectManager;
import model.ProjectManager.*;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;



public class AssignWorkersController {

    @FXML private ComboBox<Project> cmbProjects;
    @FXML private VBox vboxTeamMembers;
    @FXML private Button btnBack;
    @FXML private ScrollPane scrollPane;

    private ObservableList<Project> projects = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadProjects();

        // ComboBox display
        cmbProjects.setConverter(new StringConverter<Project>() {
            @Override
            public String toString(Project project) {
                return project != null ? project.getName() : "";
            }
            @Override
            public Project fromString(String string) { return null; }
        });

        cmbProjects.setOnAction(e -> {
            Project selected = cmbProjects.getValue();
            if (selected != null) {
                loadTeamMembers(selected.getId());
            }
        });

        // ScrollPane settings
        scrollPane.setFitToWidth(true);   // horizontal
        scrollPane.setFitToHeight(true);  // vertical
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Responsive width for cards
        scrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            vboxTeamMembers.setPrefWidth(newBounds.getWidth() - 20);
            for (javafx.scene.Node node : vboxTeamMembers.getChildren()) {
                if (node instanceof VBox) {
                    ((VBox) node).setPrefWidth(newBounds.getWidth() - 20);
                }
            }
        });
    }

    private void loadProjects() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, name FROM projects";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            projects.clear();
            while (rs.next()) {
                Project p = new Project();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                projects.add(p);
            }
            cmbProjects.setItems(projects);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

   private void loadTeamMembers(int projectId) {
    vboxTeamMembers.getChildren().clear();

    try (Connection conn = DatabaseConnection.getConnection()) {
        String sql = "SELECT id, member_name, role, task FROM project_team WHERE project_id = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, projectId);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            int memberId = rs.getInt("id");
            String name = rs.getString("member_name");
            String role = rs.getString("role");
            String task = rs.getString("task");

            VBox card = createMemberCard(memberId, name, role, task);
            card.setMaxWidth(Double.MAX_VALUE); // expand horizontally
            VBox.setVgrow(card, Priority.NEVER);
            vboxTeamMembers.getChildren().add(card);
        }

        // Force layout update
        vboxTeamMembers.applyCss();
        vboxTeamMembers.layout();

        // Adjust ScrollPane to show all content or scroll to bottom
        Platform.runLater(() -> {
            scrollPane.setVvalue(1.0); // scroll to bottom
        });

       
        

    } catch (Exception ex) {
        ex.printStackTrace();
    }
}


    private VBox createMemberCard(int memberId, String name, String role, String task) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color:white; -fx-padding:10; -fx-background-radius:10; -fx-border-radius:10; -fx-border-color:#dcdcdc;");

        Label lblName = new Label("Name: " + name);
        lblName.setStyle("-fx-font-weight:bold;");
        Label lblRole = new Label("Role: " + role);

        TextField txtTask = new TextField();
        txtTask.setPromptText("Assign task");
        if (task != null) txtTask.setText(task);

        Button btnAssign = new Button("Assign");
        btnAssign.setStyle("-fx-background-color:#4CAF50; -fx-text-fill:white; -fx-font-weight:bold;");
        btnAssign.setOnAction(e -> assignTask(memberId, txtTask.getText()));

        HBox hbox = new HBox(10, txtTask, btnAssign);
        hbox.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(lblName, lblRole, hbox);
        return card;
    }

    private void assignTask(int memberId, String task) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE project_team SET task = ? WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, task);
            pst.setInt(2, memberId);
            pst.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Task assigned successfully!");

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error assigning task: " + ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Assign Tasks");
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
            Scene scene = new Scene(root, 1200, 750);
            DashboardController dashboardController = loader.getController();
            User currentUser = App.getCurrentUser();
            if (currentUser != null) dashboardController.setUsername(currentUser.getUsername());

            
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
