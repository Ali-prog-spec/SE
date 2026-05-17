package controllers.ProjectManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.ProjectManager.DatabaseConnection;
import model.ProjectManager.App;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class SignupController {

    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Button signupButton;
    @FXML private Label errorLabel;
    @FXML private Button btnBack; // bind the FXML button
    @FXML
    public void initialize() {
        // Populate roles
        roleCombo.setItems(FXCollections.observableArrayList(
                "Project Manager", "Finance Officer", "Procurement Officer",
                "Inventory Manager"
        ));

        signupButton.setOnAction(e -> handleSignup());
    }

   @FXML
private void handleSignup() {
    String name = txtFullName.getText().trim();
    String email = txtEmail.getText().trim();
    String password = txtPassword.getText().trim();
    String confirmPassword = txtConfirmPassword.getText().trim();
    String role = roleCombo.getValue();

    // Validation
    if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role == null) {
        showError("Please fill all fields and select a role.");
        return;
    }

    if (password.length() < 6) {
        showError("Password must be at least 6 characters.");
        return;
    }

    if (!password.equals(confirmPassword)) {
        showError("Passwords do not match.");
        return;
    }

    // Insert into 'users' table
    try (Connection conn = DatabaseConnection.getConnection()) {
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, name);
        pst.setString(2, email);
        pst.setString(3, password); // Optional: hash before storing in production
        pst.setString(4, role);

        int rowsInserted = pst.executeUpdate();
        if (rowsInserted > 0) {
            showAlert("Signup successful! You can now login.");
            clearForm();
        } else {
            showError("Failed to create account. Please try again.");
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        showError("Error: " + ex.getMessage());
    }
}


    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Signup");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        errorLabel.setVisible(false);
    }

    private void clearForm() {
        txtFullName.clear();
        txtEmail.clear();
        txtPassword.clear();
        txtConfirmPassword.clear();
        roleCombo.setValue(null);
    }

  @FXML
private void goBack() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1200, 750); // set preferred size
        scene.getStylesheets().add(getClass().getResource("/screens/ProjectManager/login.css").toExternalForm());

        Stage stage = (Stage) btnBack.getScene().getWindow(); // get current stage
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
