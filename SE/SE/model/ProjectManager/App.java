package model.ProjectManager;
import controllers.ProjectManager.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Stage primaryStage;
    private static User currentUser; // <--- store logged-in user

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        AppController controller = new AppController();
        controller.showLogin();

        primaryStage.setResizable(true);
        primaryStage.show();
    }

    // Change Scene with optional CSS
    public static void changeScene(String title, Scene scene, String cssFile) { 
        if (primaryStage != null && scene != null) {
            if (cssFile != null && !cssFile.isEmpty()) {
                String css = App.class.getResource(cssFile).toExternalForm();
                scene.getStylesheets().clear();
                scene.getStylesheets().add(css);
            }
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
        }
    }

    // Set the current logged-in user
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Get the current logged-in user
    public static User getCurrentUser() {
        return currentUser;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
