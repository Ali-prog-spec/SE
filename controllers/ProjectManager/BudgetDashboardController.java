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
import model.FinanceOfficer.BudgetDAO;
import model.FinanceOfficer.BudgetItem;
import model.ProjectManager.User;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.ProjectManager.App;

import java.util.Map;
import java.util.HashMap;




public class BudgetDashboardController {

    @FXML private TableView<BudgetItem> budgetTable;

    @FXML private TableColumn<BudgetItem, String> colProject;
    @FXML private TableColumn<BudgetItem, Double> colBudget;
    @FXML private TableColumn<BudgetItem, Double> colSpent;
    @FXML private TableColumn<BudgetItem, Double> colRemaining;
    @FXML private TableColumn<BudgetItem, Double> colUtilization;
    @FXML private TableColumn<BudgetItem, Void> colEdit;

    @FXML private Button btnSaveChanges;
    @FXML private Button btnEditBudget;

    @FXML private ComboBox<String> projectDropdown;

    @FXML private Label totalBudgetLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label totalRemainingLabel;

    @FXML private BarChart<String, Number> budgetBarChart;
    @FXML private PieChart expensePieChart;
    @FXML private Button btnBack;


    private final ObservableList<BudgetItem> budgets = FXCollections.observableArrayList();
    private boolean editMode = false;
    
    private Connection conn;
    private BudgetDAO budgetDAO;

    // Map to store original budget values while editing
    private Map<BudgetItem, Double> originalValues = new HashMap<>();



    @FXML
    public void initialize() {
        // Setup database connection
      
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/construction_management?useSSL=false&serverTimezone=UTC", "root", "");
            budgetDAO = new BudgetDAO(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
       
        setupTableColumns();
        loadBudgetItems();
        setupProjectDropdown();

        

    

        
    }

    private void setupTableColumns() {

        budgetTable.setColumnResizePolicy(param -> true);
        colProject.setCellValueFactory(data -> data.getValue().projectProperty());
        colBudget.setCellValueFactory(data -> data.getValue().budgetProperty().asObject());
        colSpent.setCellValueFactory(data -> data.getValue().spentProperty().asObject());
        colRemaining.setCellValueFactory(data -> data.getValue().remainingProperty().asObject());
        colUtilization.setCellValueFactory(data -> data.getValue().utilizationProperty().asObject());

        colBudget.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colBudget.setOnEditCommit(event -> {
            BudgetItem item = event.getRowValue();
            item.setBudget(event.getNewValue());
            item.recalculate();
        });

        
        budgetTable.setEditable(false);
    }

    private void loadBudgetItems() {
        try {
            budgetTable.setItems(budgetDAO.getBudgetItems());
            updateSummaryCards();
            updateBarChart();       // bar chart
            updateExpensePieChart(null)  ;     // pie chart
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /** Create Edit/Delete/Cancel buttons per row */
    
   

    

    
  

    
    private void setupProjectDropdown() {
    try {
        ObservableList<String> projects = FXCollections.observableArrayList();
        projects.add("All Projects"); // default option
        projects.addAll(budgetDAO.getProjectNames()); // fetch project names from DB
        projectDropdown.setItems(projects);
        projectDropdown.getSelectionModel().selectFirst(); // select "All Projects"

        // Listener to filter table
        projectDropdown.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterTableByProject(newVal);
        });

        } catch (SQLException e) {
             e.printStackTrace();
        }
    }

    private void filterTableByProject(String projectName) {
    try {
        ObservableList<BudgetItem> filtered;
        if (projectName.equals("All Projects")) {
            filtered = budgetDAO.getBudgetItems(); // fetch all items
            updateExpensePieChart(null);
        } else {
            filtered = budgetDAO.getBudgetItemsByProject(projectName); // fetch items for selected project
            updateExpensePieChart(projectName);
        }
            budgetTable.setItems(filtered);
            updateSummaryCards();
            updateBarChart();       // bar chart
                  
        }   
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateSummaryCards() {
        double totalBudget = 0;
        double totalSpent = 0;

        for (BudgetItem item : budgetTable.getItems()) {
            totalBudget += item.getBudget();
            totalSpent += item.getSpent();
        }

        double totalRemaining = totalBudget - totalSpent;

        totalBudgetLabel.setText(String.format("%.2f", totalBudget));
        totalSpentLabel.setText(String.format("%.2f", totalSpent));
        totalRemainingLabel.setText(String.format("%.2f", totalRemaining));
    }

    private void updateBarChart() {
        budgetBarChart.getData().clear(); // clear previous data

        XYChart.Series<String, Number> budgetSeries = new XYChart.Series<>();
        budgetSeries.setName("Budget");

        XYChart.Series<String, Number> spentSeries = new XYChart.Series<>();
        spentSeries.setName("Spent");

        for (BudgetItem item : budgetTable.getItems()) {
            budgetSeries.getData().add(new XYChart.Data<>(item.getProject(), item.getBudget()));
            spentSeries.getData().add(new XYChart.Data<>(item.getProject(), item.getSpent()));
        }

        budgetBarChart.getData().addAll(budgetSeries, spentSeries);
    }

    private void updateExpensePieChart(String projectName) {
    try {
        expensePieChart.getData().clear();
        Map<String, Double> data = budgetDAO.getExpenseByCategory(projectName);
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        expensePieChart.setData(pieChartData);

    } catch (SQLException e) {
        e.printStackTrace();
    }
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
