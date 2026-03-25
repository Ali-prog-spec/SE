package controllers.ProjectManager;

import model.ProjectManager.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.text.SimpleDateFormat;
import java.util.List;

public class ProjectPhaseController {

    @FXML private ComboBox<Project> cmbProjects;
    @FXML private VBox vboxPhases;
    @FXML private ScrollPane scrollPane;
    @FXML private Button btnBack, btnAddPhase;

    private ObservableList<Project> projects = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadProjects();

        // ComboBox display
        cmbProjects.setConverter(new StringConverter<>() {
            @Override
            public String toString(Project project) {
                return project != null ? project.getName() : "";
            }
            @Override
            public Project fromString(String string) { return null; }
        });

        cmbProjects.setOnAction(e -> {
            Project selected = cmbProjects.getValue();
            if (selected != null) loadPhases(selected.getId());
        });

        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        vboxPhases.setSpacing(10);
    }

    private void loadProjects() {
        try {
            projects.clear();
            projects.addAll(ProjectDAO.getAllProjects());
            cmbProjects.setItems(projects);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to load projects: " + e.getMessage());
        }
    }

    public void loadPhases(int projectId) {
        vboxPhases.getChildren().clear();

        List<ProjectPhase> phases = ProjectPhaseDAO.getPhasesByProject(projectId);
        for (ProjectPhase phase : phases) {
            VBox vboxRow = new VBox(5);
            vboxRow.setPadding(new Insets(5));
            vboxRow.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding:5; -fx-background-radius:5;");

            HBox topRow = new HBox(10);
            Label lblPhase = new Label(phase.getPhaseNumber() + ". " + phase.getName());
            lblPhase.setPrefWidth(200);

            ComboBox<String> cmbStatus = new ComboBox<>();
            cmbStatus.getItems().addAll("In Progress", "Completed");
            cmbStatus.setValue(phase.getStatus() != null ? phase.getStatus() : "In Progress");

            // Update status in DB when changed
            cmbStatus.setOnAction(e -> {
                String newStatus = cmbStatus.getValue();
                ProjectPhaseDAO.updatePhaseStatus(phase.getId(), newStatus);
                phase.setStatus(newStatus);
            });

            topRow.getChildren().addAll(lblPhase, cmbStatus);

            // Description row
            Label lblDesc = new Label("Description: " + (phase.getDescription() != null ? phase.getDescription() : "-"));
            lblDesc.setWrapText(true);

            vboxRow.getChildren().addAll(topRow, lblDesc);
            vboxPhases.getChildren().add(vboxRow);
        }
    }

    @FXML
    private void addPhase() {
        Project selected = cmbProjects.getValue();
        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "Please select a project first.");
            return;
        }

        Dialog<ProjectPhase> dialog = new Dialog<>();
        dialog.setTitle("Add Project Phase");

        Label lblName = new Label("Phase Name:");
        TextField txtName = new TextField();
        Label lblStart = new Label("Start Date (yyyy-MM-dd):");
        TextField txtStart = new TextField();
        Label lblEnd = new Label("End Date (yyyy-MM-dd):");
        TextField txtEnd = new TextField();
        Label lblDesc = new Label("Description:");
        TextField txtDesc = new TextField();

        GridPane grid = new GridPane();
        grid.setVgap(10); grid.setHgap(10);
        grid.add(lblName, 0, 0); grid.add(txtName, 1, 0);
        grid.add(lblStart, 0, 1); grid.add(txtStart, 1, 1);
        grid.add(lblEnd, 0, 2); grid.add(txtEnd, 1, 2);
        grid.add(lblDesc, 0, 3); grid.add(txtDesc, 1, 3);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnTypeOk = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnTypeOk, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnTypeOk) {
                try {
                    ProjectPhase phase = new ProjectPhase();
                    phase.setProjectId(selected.getId());
                    phase.setPhaseNumber(ProjectPhaseDAO.getNextPhaseNumber(selected.getId()));
                    phase.setName(txtName.getText());

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    phase.setStartDate(sdf.parse(txtStart.getText()));
                    phase.setEndDate(sdf.parse(txtEnd.getText()));
                    phase.setDescription(txtDesc.getText());
                    phase.setStatus("In Progress"); // default
                    return phase;
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid data: " + e.getMessage());
                }
            }
            return null;
        });

        ProjectPhase phase = dialog.showAndWait().orElse(null);
        if (phase != null) {
            ProjectPhaseDAO.addPhase(phase);
            loadPhases(selected.getId());
            showAlert(Alert.AlertType.INFORMATION, "Phase added successfully!");
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle("Project Phases");
        alert.setHeaderText(null);
        alert.setContentText(msg);
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
            if (currentUser != null) dashboardController.setUsername(currentUser.getUsername());

            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/screens/ProjectManager/dashboard.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Dashboard");
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to load dashboard: " + e.getMessage());
        }
    }
}
