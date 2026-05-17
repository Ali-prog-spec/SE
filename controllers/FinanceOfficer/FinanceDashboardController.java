package controllers.FinanceOfficer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import model.FinanceOfficer.BudgetDAO;
import model.FinanceOfficer.BudgetItem;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.sql.*;
import javafx.scene.control.*;

public class FinanceDashboardController {

    @FXML
    private BarChart<String, Number> expenseBarChart; // Reference to chart in FXML
    @FXML
private BarChart<String, Number> budgetVsExpensesChart;
    // Reference to the StackPane in MainLayout
    private StackPane mainContent;

    private Connection conn;
private BudgetDAO budgetDAO;


    @FXML private Label lblTotalInvoices;
    @FXML private Label lblPendingApprovals;
    @FXML private Label lblTotalExpenses;
    @FXML private Label lblBudgetStatus;


    public void setMainContent(StackPane mainContent) {
        this.mainContent = mainContent;
    }

    @FXML
    private void initialize() {
        try {
        // Connect to the database
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/construction_management?useSSL=false&serverTimezone=UTC", "root", "");
        budgetDAO = new BudgetDAO(conn); // create DAO object
        } catch (SQLException e) {
            e.printStackTrace();
        }
        loadExpenseChart(); // Load chart automatically when dashboard opens
        updateBudgetVsExpensesChart();
        updateSummaryCards();
    }

    // ===========================
    // Load Bar Chart Data
    // ===========================
    private void loadExpenseChart() {

        String url = "jdbc:mysql://localhost:3306/construction_management?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";

        String sql = "SELECT category, SUM(amount) AS total FROM expense GROUP BY category";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Total Expenses by Category");

            while (rs.next()) {
                String category = rs.getString("category");
                double total = rs.getDouble("total");
                series.getData().add(new XYChart.Data<>(category, total));
            }

            expenseBarChart.getData().clear();
            expenseBarChart.getData().add(series);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // ===========================
    // Page Navigation Handlers
    // ===========================
    @FXML
    private void handleCreateInvoice(ActionEvent event) {
        loadPage("/screens/FinanceOfficer/CreateInvoiceForm.fxml");
    }

    @FXML
    private void handleAddExpense(ActionEvent event) {
        loadPage("/screens/FinanceOfficer/AddExpenseForm.fxml");
    }

    @FXML
    private void handleViewAllExpenses(ActionEvent event) {
        loadPage("/screens/FinanceOfficer/expenses_list.fxml");
    }

    @FXML
    private void handleMonitorBudget(ActionEvent event) {
        loadPage("/screens/FinanceOfficer/BudgetMonitor.fxml");
    }

    // ===========================
    // Page Loader Helper
    // ===========================
    private void loadPage(String fxmlPath) {
        if (mainContent == null) {
            System.err.println("Error: mainContent StackPane is not set!");
            return;
        }

        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxmlPath));

            mainContent.getChildren().clear();
            mainContent.getChildren().add(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void updateBudgetVsExpensesChart() {
        budgetVsExpensesChart.getData().clear();

        try {
            // Get all budget items
            ObservableList<BudgetItem> allBudgets = budgetDAO.getBudgetItems();

            XYChart.Series<String, Number> budgetSeries = new XYChart.Series<>();
            budgetSeries.setName("Budget");

            XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
            expenseSeries.setName("Expenses");

            for (BudgetItem item : allBudgets) {
                String project = item.getProject();
                double budgetAmount = item.getBudget();

                // Get total expense for this project from expense table
                double totalExpense = budgetDAO.getTotalExpenseForProject(project);

                budgetSeries.getData().add(new XYChart.Data<>(project, budgetAmount));
                expenseSeries.getData().add(new XYChart.Data<>(project, totalExpense));
            }

            budgetVsExpensesChart.getData().addAll(budgetSeries, expenseSeries);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateSummaryCards() {
    try {
        // Card 1: Total Invoices this month
        int totalInvoices = budgetDAO.getInvoicesCreatedThisMonth();
        lblTotalInvoices.setText(String.valueOf(totalInvoices));

        

        // Card 3: Total Expenses
        double totalExpenses = budgetDAO.getTotalExpenses();
        lblTotalExpenses.setText(String.format("%.2f", totalExpenses));

        // Card 4: Budget Status (percentage utilized)
        double totalBudget = budgetDAO.getTotalBudget();
        double utilization = totalBudget > 0 ? (totalExpenses / totalBudget) * 100 : 0;
        lblBudgetStatus.setText(String.format("%.1f%%", utilization));

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
}
