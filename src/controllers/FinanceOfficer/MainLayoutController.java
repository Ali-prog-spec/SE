package controllers.FinanceOfficer;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import model.ProjectManager.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class MainLayoutController {

    @FXML
    private StackPane mainContent;

    @FXML
    public void initialize() {
        // Automatically load Dashboard when main layout is initialized
        handleDashboard(null);
    }

    @FXML
    public void handleDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/FinanceOfficer/finance_dashboard.fxml"));
            Parent dashboard = loader.load();

            // Pass mainContent reference to dashboard controller
            FinanceDashboardController controller = loader.getController();
            controller.setMainContent(mainContent);

            mainContent.getChildren().clear();
            mainContent.getChildren().add(dashboard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCreateInvoice(ActionEvent event) {
        loadPage("CreateInvoiceForm.fxml");
    }
    @FXML
    private void handleAddExpense() {
        loadPage("AddExpenseForm.fxml");
    }

    @FXML
    private void handleBudgetMonitor() {
        loadPage("BudgetMonitor.fxml");
    }

    @FXML
    private void handleNoti() {
        loadPage("notifications.fxml");
    }
    

    private void loadPage(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/screens/FinanceOfficer/" + fxmlFile));
            mainContent.getChildren().clear();
            mainContent.getChildren().add(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlelogOut() {
          try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            App.changeScene("Login", scene, "/screens/ProjectManager/login.css");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}
