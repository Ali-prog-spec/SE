
import model.ProjectManager.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import controllers.ProjectManager.AppController;
import model.InventoryManager.InventoryManagerNew;
import model.FinanceOfficer.FinanceOfficer;
import model.ProcurementOfficer.ProcurementOfficer;

public class Login_SignUp
{
    private VBox view;
    private AppController controller;

    public Login_SignUp(AppController controller) 
    {
        this.controller = controller;
        createView();
    }

    private void createView() 
    {
        view = new VBox(20);
        view.setAlignment(Pos.CENTER);
        view.setStyle("-fx-background-color: black; -fx-padding: 50;");

        Label title = new Label("Project Management Login");
        title.setTextFill(Color.GOLD);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: red; -fx-text-fill: gold; -fx-font-weight: bold;");

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        loginBtn.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();

            User user = authenticate(email, password);
            if (user != null) {
                controller.onLogin(user);
            } else {
                errorLabel.setText("Invalid credentials!");
            }
        });

        view.getChildren().addAll(title, emailField, passwordField, loginBtn, errorLabel);
    }

    public VBox getView() 
    {
        return view;
    }
    private User authenticate(String email, String password) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE email=? AND password=?"
            );
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                int userId = rs.getInt("id");
                String username = rs.getString("name");
                String userEmail = rs.getString("email");
                String role = rs.getString("role");
                String fullName = rs.getString("full_name");

                User u = null;

                if (role.equals("InventoryManager")) 
                {
                    u = new InventoryManagerNew(userId, username, userEmail, fullName);
                }
                else if (role.equals("FinanceOfficer")) 
                {
                    u = new FinanceOfficer(userId, username, userEmail, fullName);
                }
                else if (role.equals("ProcurementOfficer")) 
                {
                    u = new ProcurementOfficer(userId, username, userEmail, fullName);
                }

                return u;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
