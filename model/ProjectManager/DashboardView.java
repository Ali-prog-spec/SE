package model.ProjectManager;
import controllers.ProjectManager.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class DashboardView extends Application {

    @Override
    public void start(Stage primaryStage) {

        // --------------------
        // Sidebar
        // --------------------
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dfe3e6;");

        Label cmisTitle = new Label("CMIS");
        cmisTitle.setFont(Font.font(24));

        ListView<String> menu = new ListView<>();
        menu.getItems().addAll(
                "Dashboard", "Create Project", "Assign Resources",
                "Assign Workers", "Project Phases", "Manage Inventory",
                "Request Material", "Log Activity", "Add Expense",
                "Budget Monitor", "Documents", "Project Status"
        );
        menu.getSelectionModel().select(0);
        menu.setStyle("-fx-background-insets: 0;");

        sidebar.getChildren().addAll(cmisTitle, menu);

        // --------------------
        // Header
        // --------------------
        HBox header = new HBox();
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e8eaed;");
        Label dashboardTitle = new Label("Project Manager Dashboard");
        dashboardTitle.setFont(Font.font(22));
        header.getChildren().add(dashboardTitle);

        // --------------------
        // Top Cards
        // --------------------
        HBox cards = new HBox(20);
        cards.setPadding(new Insets(20));

        cards.getChildren().addAll(
                createCard("Total Projects", "12"),
                createCard("Active Projects", "8"),
                createCard("Pending Requests", "5"),
                createCard("Budget Utilization", "$850K / $1M")
        );

        // --------------------
        // Charts Section
        // --------------------
        HBox charts = new HBox(20);
        charts.setPadding(new Insets(20));

        // Project Progress Bar Chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> progressChart = new BarChart<>(xAxis, yAxis);
        progressChart.setTitle("Project Progress");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Project A", 75));
        series.getData().add(new XYChart.Data<>("Project B", 45));
        series.getData().add(new XYChart.Data<>("Project C", 90));
        series.getData().add(new XYChart.Data<>("Project D", 30));

        progressChart.getData().add(series);

        // Budget Pie Chart
        PieChart budgetPie = new PieChart();
        budgetPie.getData().add(new PieChart.Data("Labor ($45K)", 45));
        budgetPie.getData().add(new PieChart.Data("Materials ($35K)", 35));
        budgetPie.getData().add(new PieChart.Data("Equipment ($15K)", 15));
        budgetPie.getData().add(new PieChart.Data("Misc ($5K)", 5));
        budgetPie.setTitle("Budget Distribution");

        charts.getChildren().addAll(progressChart, budgetPie);

        // --------------------
        // Combine everything
        // --------------------
        VBox content = new VBox(header, cards, charts);
        content.setStyle("-fx-background-color: #f5f6fa;");

        HBox root = new HBox(sidebar, content);

        Scene scene = new Scene(root, 1350, 800);

        primaryStage.setTitle("Project Manager Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --------------------
    // Card UI Builder
    // --------------------
    private VBox createCard(String title, String value) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setPrefSize(200, 100);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-border-radius: 12;" +
                "-fx-border-color: #dcdcdc;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );

        Label t = new Label(title);
        t.setFont(Font.font(14));
        t.setTextFill(Color.web("#555"));

        Label v = new Label(value);
        v.setFont(Font.font(22));
        v.setTextFill(Color.web("#111"));

        card.getChildren().addAll(t, v);

        return card;
    }
}
