package controllers.ProjectManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.util.converter.DoubleStringConverter;

import model.ProjectManager.User;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.ProjectManager.App;

import java.util.Map;
import java.util.HashMap;




public class BudgetDashboardController {

    @FXML private Button btnSaveChanges;
    @FXML private Button btnEditBudget;

    @FXML private ComboBox<String> projectDropdown;

    @FXML private Label totalBudgetLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label totalRemainingLabel;

    @FXML private BarChart<String, Number> budgetBarChart;
    @FXML private PieChart expensePieChart;
    @FXML private Button btnBack;



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
