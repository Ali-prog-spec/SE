package controllers.ProjectManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import model.ProjectManager.App;
import model.ProjectManager.Notification;
import model.ProjectManager.NotificationDAO;
import model.ProjectManager.User;

import java.util.List;

public class NotificationsController {

    @FXML private VBox vboxNotifications;
    @FXML private ScrollPane scrollPane;
    @FXML private Button btnBack;

    @FXML
    public void initialize() {
        loadNotifications();
    }

    @FXML
    public void loadNotifications() {
        vboxNotifications.getChildren().clear();

        int currentUserId = App.getCurrentUser().getUserId();
        List<Notification> notifications = NotificationDAO.getNotificationsForUser(currentUserId);

        if (notifications.isEmpty()) {
            Label lbl = new Label("No new notifications");
            lbl.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            lbl.setTextFill(Color.GRAY);
            vboxNotifications.getChildren().add(lbl);
            return;
        }

        for (Notification n : notifications) {
            VBox card = createNotificationCard(n);
            card.prefWidthProperty().bind(scrollPane.widthProperty().subtract(20));
            vboxNotifications.getChildren().add(card);
        }
    }
    @FXML
private void showNotifications(javafx.event.ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/notifications.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        Scene scene = new Scene(root, 600, 500); // adjust size
        scene.getStylesheets().add(getClass().getResource("/screens/ProjectManager/notifications.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Notifications");
        stage.show();
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    private VBox createNotificationCard(Notification n) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color:white; -fx-padding:12; -fx-background-radius:10; " +
                      "-fx-border-radius:10; -fx-border-color:#dcdcdc;");

        Label lblMessage = new Label("Message: " + n.getMessage());
        lblMessage.setWrapText(true);
        lblMessage.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        Label lblUser = new Label(" (ID: "  + n.getUserId() + ")");
        lblUser.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        lblUser.setTextFill(Color.DARKGRAY);

        Label lblDate = new Label("Received: " + n.getCreatedAt());
        lblDate.setFont(Font.font("Arial", FontWeight.LIGHT, 12));
        lblDate.setTextFill(Color.GRAY);

        card.getChildren().addAll(lblMessage, lblUser, lblDate);
        return card;
    }

     @FXML
    private void goBack() {
        try {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 750);
            DashboardController dashboardController = loader.getController();
            User currentUser = App.getCurrentUser();
            if (currentUser != null) dashboardController.setUsername(currentUser.getUsername());

            
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
