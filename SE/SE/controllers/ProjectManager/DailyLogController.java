package controllers.ProjectManager;

import model.ProjectManager.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;


public class DailyLogController {

    @FXML private ComboBox<Project> cmbProjects;
    @FXML private TextArea txtDescription;
    @FXML private Button btnSave, btnExportCSV;
    @FXML private ListView<String> listLogs;
    @FXML private Button btnBack;

    private ObservableList<Project> projects = FXCollections.observableArrayList();
    private ObservableList<String> logEntries = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadProjects();
        listLogs.setItems(logEntries);

        // Load logs when user selects project
        cmbProjects.setOnAction(event -> loadTodayLogs());
    }

    // Load all projects from database
    private void loadProjects() {
        try {
            projects.clear();
            List<Project> projectList = ProjectDAO.getAllProjects();
            projects.addAll(projectList);
            cmbProjects.setItems(projects);
        } catch (Exception e) {
            e.printStackTrace();
        }

        cmbProjects.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Project project) {
                return project != null ? project.getName() : "";
            }
            @Override
            public Project fromString(String string) { return null; }
        });
    }

    // Load today's logs for selected project
    private void loadTodayLogs() {
        logEntries.clear();

        Project selected = cmbProjects.getValue();
        if (selected == null) return;

        try {
            List<String> logs = DailyLogDAO.getLogsForToday(selected.getId());
            for (String desc : logs) {
                String line = LocalDate.now() + " | " + selected.getName() + " | " + desc;
                logEntries.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Save log to SQL + CSV
    @FXML
    private void saveLog() {
        Project selectedProject = cmbProjects.getValue();
        String description = txtDescription.getText().trim();

        if (selectedProject == null || description.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please select a project and enter description!");
            return;
        }

        String date = LocalDate.now().toString();
        String logLine = date + "," + selectedProject.getName() + "," + description;

        try {
            // SAVE TO SQL
            DailyLogDAO.saveLog(selectedProject.getId(), description);

            // SAVE TO DAILY CSV
            String fileName = "logs_" + date + ".csv";
            File file = new File(fileName);
            boolean isNew = !file.exists();

            try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
                if (isNew)
                    pw.println("Date,Project,Description");
                pw.println(logLine);
            }

            // Update UI
            logEntries.add(date + " | " + selectedProject.getName() + " | " + description);
            txtDescription.clear();

            showAlert(Alert.AlertType.INFORMATION, "Log saved successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error saving log: " + e.getMessage());
        }
    }

    // Export all logs to CSV
    @FXML
    private void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Daily Logs");
        fileChooser.setInitialFileName("daily_logs.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        Stage stage = (Stage) btnExportCSV.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("Date,Project,Description");
                for (String entry : logEntries) {
                    pw.println(entry.replace(" | ", ","));
                }
                showAlert(Alert.AlertType.INFORMATION, "Logs exported successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error exporting CSV: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type, msg, ButtonType.OK);
        alert.setHeaderText(null);
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
            if (currentUser != null)
                dashboardController.setUsername(currentUser.getUsername());

            Scene scene = new Scene(root, 1200, 750);
            scene.getStylesheets().add(getClass().getResource("/screens/ProjectManager/dashboard.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Dashboard");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
