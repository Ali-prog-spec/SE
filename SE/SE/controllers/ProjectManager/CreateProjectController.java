package controllers.ProjectManager;

import model.ProjectManager.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import model.ProjectManager.App;



public class CreateProjectController {

    @FXML private TextField txtProjectName;
    @FXML private TextField txtClient;
    @FXML private TextField txtLocation;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private TextField txtBudget;
    @FXML private TextArea txtDescription;
    @FXML private Button btnBack;

    @FXML private TextField txtTeamMember;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Button btnAddMember;
    @FXML private ListView<String> lstTeamMembers;

    @FXML private ComboBox<User> cmbFinanceOfficer;
    @FXML private ComboBox<User> cmbProcurementOfficer;
    @FXML private ComboBox<User> cmbInventoryManager;

    @FXML private Button btnCreateProject;

    private ObservableList<String> teamMembers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Roles for other team members
        cmbRole.getItems().addAll(
                "Site Engineer", "Plumber", "Electrician", "Carpenter",
                "Painter", "Mason", "Welder", "Supervisor",
                "Architect", "Safety Officer", "Labour"
        );

        lstTeamMembers.setItems(teamMembers);

        // Load users by role
        loadUsersByRole();

        btnAddMember.setOnAction(e -> addTeamMember());
        btnCreateProject.setOnAction(e -> saveProject());
    }

    private void loadUsersByRole() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            loadRoleUsers(conn, "Finance Officer", cmbFinanceOfficer);
            loadRoleUsers(conn, "Procurement Officer", cmbProcurementOfficer);
            loadRoleUsers(conn, "Inventory Manager", cmbInventoryManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRoleUsers(Connection conn, String role, ComboBox<User> comboBox) throws Exception {
        String sql = "SELECT id, name FROM users WHERE role = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, role);
        ResultSet rs = pst.executeQuery();

        ObservableList<User> list = FXCollections.observableArrayList();
        while (rs.next()) {
            User u = new User();
            u.setUserId(rs.getInt("id"));
            u.setUsername(rs.getString("name"));
            list.add(u);
        }

        comboBox.setItems(list);
        comboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(User u) { return u != null ? u.getUsername() : ""; }
            @Override public User fromString(String s) { return null; }
        });
    }

    @FXML
    private void addTeamMember() {
        String name = txtTeamMember.getText().trim();
        String role = cmbRole.getValue();

        if (name.isEmpty() || role == null) {
            showAlert(Alert.AlertType.WARNING, "Please enter member name and select a role.");
            return;
        }

        teamMembers.add(name + " (" + role + ")");
        txtTeamMember.clear();
        cmbRole.setValue(null);
    }

    @FXML
    private void createProject() {
        saveProject();
    }

  private void saveProject() {
    String name = txtProjectName.getText().trim();
    String client = txtClient.getText().trim();
    String location = txtLocation.getText().trim();
    LocalDate start = dpStartDate.getValue();
    LocalDate end = dpEndDate.getValue();
    String budgetStr = txtBudget.getText().trim();
    String description = txtDescription.getText().trim();

    if (name.isEmpty() || start == null || end == null || budgetStr.isEmpty()) {
        showAlert(Alert.AlertType.ERROR, "Please fill all required fields.");
        return;
    }

    double budget;
    try {
        budget = Double.parseDouble(budgetStr);
    } catch (NumberFormatException ex) {
        showAlert(Alert.AlertType.ERROR, "Budget must be a number.");
        return;
    }

    try (Connection conn = DatabaseConnection.getConnection()) {

        // Insert project
        String sql = """
                INSERT INTO projects 
                (name, client, location, start_date, end_date, budget, description, status,
                 finance_officer, procurement_officer, inventory_manager)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        PreparedStatement pst = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        pst.setString(1, name);
        pst.setString(2, client);
        pst.setString(3, location);
        pst.setDate(4, java.sql.Date.valueOf(start));
        pst.setDate(5, java.sql.Date.valueOf(end));
        pst.setDouble(6, budget);
        pst.setString(7, description);
        pst.setString(8, "On Track");

        pst.setObject(9, cmbFinanceOfficer.getValue() != null ? cmbFinanceOfficer.getValue().getUserId() : null);
        pst.setObject(10, cmbProcurementOfficer.getValue() != null ? cmbProcurementOfficer.getValue().getUserId() : null);
        pst.setObject(11, cmbInventoryManager.getValue() != null ? cmbInventoryManager.getValue().getUserId() : null);

        pst.executeUpdate();

        // Get generated project ID immediately
        int projectId = 0;
        try (ResultSet rs = pst.getGeneratedKeys()) {
            if (rs.next()) {
                projectId = rs.getInt(1);
            }
        }

        // Insert team members
        if (!teamMembers.isEmpty()) {
            String sqlTeam = "INSERT INTO project_team (project_id, member_name, role) VALUES (?, ?, ?)";
            try (PreparedStatement pstTeam = conn.prepareStatement(sqlTeam)) {
                for (String member : teamMembers) {
                    int idx = member.lastIndexOf(" (");
                    String memberName = member.substring(0, idx);
                    String memberRole = member.substring(idx + 2, member.length() - 1);

                    pstTeam.setInt(1, projectId);
                    pstTeam.setString(2, memberName);
                    pstTeam.setString(3, memberRole);
                    pstTeam.addBatch();
                }
                pstTeam.executeBatch();
            }
        }

        

  
        showAlert(Alert.AlertType.INFORMATION, "Project created successfully!");
        clearForm();

    } catch (Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error saving project: " + e.getMessage());
    }
}


    private void clearForm() {
        txtProjectName.clear();
        txtClient.clear();
        txtLocation.clear();
        dpStartDate.setValue(null);
        dpEndDate.setValue(null);
        txtBudget.clear();
        txtDescription.clear();
        teamMembers.clear();
        cmbFinanceOfficer.setValue(null);
        cmbProcurementOfficer.setValue(null);
        cmbInventoryManager.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Create Project");
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
