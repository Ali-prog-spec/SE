package controllers.ProjectManager;

import model.ProjectManager.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);

        // Populate roles
        roleCombo.getItems().addAll(
                "Project Manager",
                "Inventory Manager",
                "Procurement Officer",
                "Finance Officer"
        );

        // Handle 'Enter' key
        usernameField.setOnKeyPressed(this::handleEnter);
        passwordField.setOnKeyPressed(this::handleEnter);
        roleCombo.setOnKeyPressed(this::handleEnter);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleCombo.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showError("Please enter username, password, and select a role");
            return;
        }

        // Authenticate user
        User user = UserDAO.authenticateUser(username, password);
        if (user == null) {
            showError("Invalid username or password");
            return;
        }

        if (!role.equalsIgnoreCase(user.getRole())) {
            showError("Selected role does not match account role");
            return;
        }

        // Store user globally
        App.setCurrentUser(user);

        // Show corresponding dashboard
        showDashboardForRole(user);
    }

    private void showDashboardForRole(User user) {
        try {
            String role = user.getRole();
            FXMLLoader loader;
            Parent root;
            Scene scene;
            Stage stage = (Stage) usernameField.getScene().getWindow();

            switch (role.toLowerCase()) {
                case "project manager":
                    loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/dashboard.fxml"));
                    root = loader.load();
                    scene = new Scene(root, 1350, 800);
                    scene.getStylesheets().add(getClass().getResource("/screens/ProjectManager/dashboard.css").toExternalForm());
                    stage.setScene(scene);
                    stage.setTitle("Project Manager Dashboard");
                    stage.show();
                    break;

                case "inventory manager":
                    loader = new FXMLLoader(getClass().getResource("/screens/InventoryManager/Dashboard.fxml"));
                    root = loader.load();
                    scene = new Scene(root, 1350, 800);
                    scene.getStylesheets().add(getClass().getResource("/screens/InventoryManager/styles.css").toExternalForm());
                    stage.setScene(scene);
                    stage.setTitle("Inventory Manager Dashboard");
                    stage.show();
                    break;

                case "procurement officer":
                    loader = new FXMLLoader(getClass().getResource("/screens/ProcurementOfficer/Dashboard.fxml"));
                    root = loader.load();
                    scene = new Scene(root, 1350, 800);
                    scene.getStylesheets().add(getClass().getResource("/screens/ProcurementOfficer/styles.css").toExternalForm());
                    stage.setScene(scene);
                    stage.setTitle("Procurement Officer Dashboard");
                    stage.show();
                    break;

                    case "finance officer":
                    loader = new FXMLLoader(getClass().getResource("/screens/FinanceOfficer/MainLayout.fxml"));
                    root = loader.load();
                    scene = new Scene(root, 1350, 800);
                    scene.getStylesheets().add(getClass().getResource("/screens/FinanceOfficer/styles.css").toExternalForm());
                    stage.setScene(scene);
                    stage.setTitle("Finance Officer Dashboard");
                    stage.show();
                    break;

                default:
                    showError("Dashboard not implemented for role: " + role);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error loading dashboard: " + ex.getMessage());
        }
    }

    @FXML
private void handleSignup() {
    try {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/signup.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 500, 600); // adjust size as needed
        scene.getStylesheets().add(getClass().getResource("/screens/ProjectManager/login.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Sign Up");
        stage.show();
    } catch (Exception e) {
        e.printStackTrace();
        showError("Error opening Sign Up page: " + e.getMessage());
    }
}


    private void handleEnter(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) handleLogin();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
