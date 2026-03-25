package controllers.FinanceOfficer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AddExpenseController implements Initializable {

    @FXML private ComboBox<ProjectItem> projectCombo;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField amountField;
    @FXML private TextArea descriptionArea;
    @FXML private Button btnSubmit;
    @FXML private Button btnCancel;
    @FXML private Button btnBack;
    @FXML private StackPane successPane;
    @FXML private Label successMessage;

    // ---------- DATABASE SETTINGS ----------
    private final String URL = "jdbc:mysql://localhost:3306/construction_management?useSSL=false&serverTimezone=UTC";
    private final String USER = "root";
    private final String PASS = "";

    // ---------- MODEL FOR PROJECT COMBO ----------
    public static class ProjectItem {
        public int id;
        public String name;

        public ProjectItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() { return name; } // ComboBox displays name
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadProjects();
        loadCategories();

        btnSubmit.setOnAction(e -> addExpense());
        btnCancel.setOnAction(e -> clearForm());
        btnBack.setOnAction(e -> returnToDashboard());
    }

    // ---------- LOAD PROJECT NAMES ----------
    private void loadProjects() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            String sql = "SELECT id, name FROM projects";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                projectCombo.getItems().add(
                        new ProjectItem(rs.getInt("id"), rs.getString("name"))
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------- STATIC CATEGORIES (YOU CAN CHANGE) ----------
    private void loadCategories() {
        categoryCombo.getItems().addAll(
                "Labor", "Material", "Equipment", "Software", "Maintenance",
                "Utility", "Sub-Contract", "Miscellaneous"
        );
    }

    // ---------- INSERT EXPENSE ----------
    private void addExpense() {

        // Validation
        if (projectCombo.getValue() == null ||
            datePicker.getValue() == null ||
            categoryCombo.getValue() == null ||
            amountField.getText().isEmpty() ||
            descriptionArea.getText().isEmpty()) {

            showAlert("Missing Fields", "Please fill all required fields.");
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            showAlert("Invalid Amount", "Amount must be a number.");
            return;
        }

        ProjectItem project = projectCombo.getValue();
        LocalDate date = datePicker.getValue();
        String category = categoryCombo.getValue();
        String desc = descriptionArea.getText();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            String sql = "INSERT INTO expense (project_id, date, category, amount, description) " +
                         "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, project.id);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setString(3, category);
            stmt.setDouble(4, amount);
            stmt.setString(5, desc);

            stmt.executeUpdate();

            showSuccess("Expense added successfully for project: " + project.name);
            clearForm();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", e.getMessage());
        }
    }

    // ---------- CLEAR FORM ----------
    private void clearForm() {
        projectCombo.setValue(null);
        datePicker.setValue(null);
        categoryCombo.setValue(null);
        amountField.clear();
        descriptionArea.clear();
    }

    // ---------- RETURN NAVIGATION ----------
    private void returnToDashboard() {
        // You will integrate this later with stackpane loader
        System.out.println("Back clicked");
    }

    // ---------- ALERT ----------
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.show();
    }

    // ---------- SUCCESS POPUP ----------
    private void showSuccess(String msg) {
        successMessage.setText(msg);
        successPane.setVisible(true);

        // hide after 2 seconds
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (Exception ignored) {}
            successPane.setVisible(false);
        }).start();
    }
}
