package controllers.ProjectManager;
import model.ProjectManager.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class AppController {

    private User loggedInUser;

    public AppController() { }

   public void showLogin() {
    try {
        Parent root = FXMLLoader.load(getClass().getResource("/screens/ProjectManager/login.fxml"));
        Scene scene = new Scene(root, 1200, 700);

        // Load CSS
        scene.getStylesheets().add(getClass().getResource("/screens/ProjectManager/login.css").toExternalForm());

        App.changeScene("Login - Construction Management", scene,"/screens/ProjectManager/login.css");
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    public void onLogin(User user) {
        this.loggedInUser = user;
    }
public void showProjectManagerDashboard(User user) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/dashboard.fxml"));
        Parent root = loader.load();

        DashboardController dashboardController = loader.getController();

        // Pass the username
        if (user != null) {
            dashboardController.setUsername(user.getUsername());
        }

        Scene scene = new Scene(root, 1350, 800);
        App.changeScene("Dashboard", scene, "/screens/ProjectManager/dashboard.css");

    } catch (Exception e) {
        e.printStackTrace();
    }
}



    public void logout() {
        loggedInUser = null;
        showLogin();
    }
}
