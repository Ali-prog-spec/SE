package controllers.ProjectManager;

import model.ProjectManager.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.awt.Desktop;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;


import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class DocumentController {

    @FXML private ComboBox<Project> cmbProjects;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private Button btnChooseFile;
    @FXML private Button btnUpload;
    @FXML private Label lblSelectedFile;
    @FXML private VBox vboxDocuments;
    @FXML private Button btnBack;

    private File selectedFile;

    @FXML
    public void initialize() {
        loadProjects();

        cmbCategory.setItems(FXCollections.observableArrayList(
                "Report", "Blueprint", "Contract", "Invoice", "Other"
        ));

        cmbProjects.setOnAction(e -> loadDocuments());
    }

    private void loadProjects() {
        try (var conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, name FROM projects";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            ObservableList<Project> list = FXCollections.observableArrayList();

            while (rs.next()) {
                Project p = new Project();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                list.add(p);
            }

            cmbProjects.setItems(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void chooseFile() {
        FileChooser chooser = new FileChooser();
        selectedFile = chooser.showOpenDialog(null);

        if (selectedFile != null) {
            lblSelectedFile.setText("Selected: " + selectedFile.getName());
        }
    }

    @FXML
    private void uploadDocument() {
        if (cmbProjects.getValue() == null || cmbCategory.getValue() == null || selectedFile == null) {
            show("Please choose project, category and file.");
            return;
        }

        try {
            int projectId = cmbProjects.getValue().getId();
            String category = cmbCategory.getValue();

            File folder = new File("uploaded_documents");
            if (!folder.exists()) folder.mkdir();

            File dest = new File(folder, selectedFile.getName());
            Files.copy(selectedFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

            ProjectDocumentDAO.saveDocument(projectId, category, selectedFile.getName(), dest.getAbsolutePath());

            show("Document uploaded successfully!");

            loadDocuments();
            lblSelectedFile.setText("No file selected");
            selectedFile = null;

        } catch (Exception e) {
            e.printStackTrace();
            show("Upload failed: " + e.getMessage());
        }
    }


private void loadDocuments() {
    vboxDocuments.getChildren().clear();

    if (cmbProjects.getValue() == null) return;

    int projectId = cmbProjects.getValue().getId();
    List<ProjectDocument> docs = ProjectDocumentDAO.getDocumentsByProject(projectId);

    for (ProjectDocument d : docs) {
        Hyperlink link = new Hyperlink(d.getCategory() + " - " + d.getFileName() + 
                " (Uploaded: " + d.getUploadedAt() + ")");
        link.setStyle("-fx-background-color:white; -fx-padding:10; -fx-border-color:#dcdcdc; -fx-border-radius:6;");

        // Open file on click
        link.setOnAction(e -> {
            try {
                File file = new File(d.getFilePath());
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                } else {
                    show("File not found: " + d.getFileName());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                show("Failed to open file: " + ex.getMessage());
            }
        });

        vboxDocuments.getChildren().add(link);
    }
}

    private void show(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
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
