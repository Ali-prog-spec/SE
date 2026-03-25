package model.FinanceOfficer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/FinanceOfficer/MainLayout.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 600);
        stage.setTitle("Construction Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
