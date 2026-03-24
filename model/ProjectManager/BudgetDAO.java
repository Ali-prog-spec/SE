package model.ProjectManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class BudgetDAO {
    private final Connection conn;

    public BudgetDAO(Connection conn) {
        this.conn = conn;
    }

    public ObservableList<BudgetItem> getBudgetItems() throws SQLException {
        ObservableList<BudgetItem> list = FXCollections.observableArrayList();

        String sql = """
            SELECT p.id, p.name, p.budget, 
                   COALESCE(SUM(e.amount), 0) as spent
            FROM projects p
            LEFT JOIN expense e ON p.id = e.project_id
            GROUP BY p.id, p.name, p.budget
            """;

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                //int id = rs.getInt("project_id");
                String name = rs.getString("name");
                double budget = rs.getDouble("budget");
                double spent = rs.getDouble("spent");
                list.add(new BudgetItem(name, budget, spent));
            }
        }

        return list;
    }

    public ObservableList<String> getProjectNames() throws SQLException {
        ObservableList<String> projects = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT name FROM projects ORDER BY name";
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                projects.add(rs.getString("name"));
            }
        }
        return projects;
    }

    public ObservableList<BudgetItem> getBudgetItemsByProject(String projectName) throws SQLException {
        ObservableList<BudgetItem> list = FXCollections.observableArrayList();
        String query = "SELECT p.name, p.budget, IFNULL(SUM(e.amount), 0) AS spent " +
                    "FROM projects p LEFT JOIN expense e ON p.id = e.project_id " +
                    "WHERE p.name = ? " +
                    "GROUP BY p.id";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, projectName);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {
            list.add(new BudgetItem(rs.getString("name"),
                                    rs.getDouble("budget"),
                                    rs.getDouble("spent")));
        }
        return list;
    }

    public void updateBudget(String projectName, double newBudget) throws SQLException {
        String sql = "UPDATE projects SET budget = ? WHERE name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setDouble(1, newBudget);
        stmt.setString(2, projectName);
        stmt.executeUpdate();
        stmt.close();
    }

    public Map<String, Double> getExpenseByCategory(String projectName) throws SQLException {
        Map<String, Double> categoryTotals = new HashMap<>();
        String sql;

        if (projectName == null || projectName.equalsIgnoreCase("All Projects")) {
            // No filter, get totals for all projects
            sql = "SELECT category, SUM(amount) as total FROM expense GROUP BY category";
            try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String category = rs.getString("category");
                    double total = rs.getDouble("total");
                    categoryTotals.put(category, total);
                }
            }
        } else {
            // Filter by specific project
            sql = "SELECT e.category, SUM(e.amount) AS total " +
              "FROM expense e " +
              "JOIN projects p ON e.project_id = p.id " +
              "WHERE p.name = ? " +
              "GROUP BY e.category";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, projectName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String category = rs.getString("category");
                        double total = rs.getDouble("total");
                        categoryTotals.put(category, total);
                    }
                }
            }
        }

        return categoryTotals;
    }



    public double getTotalExpenseForProject(String projectName) throws SQLException {
        double total = 0;

        // Get project_id from project name
        int projectId = 0;
        String projectSql = "SELECT id FROM projects WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(projectSql)) {
            stmt.setString(1, projectName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    projectId = rs.getInt("id");
                } else {
                    return 0; // project not found
                }
            }
        }

        // Get sum of expenses for this project
        String expenseSql = "SELECT SUM(amount) as total FROM expense WHERE project_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(expenseSql)) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("total");
                }
            }
        }

        return total;
    }

    // BudgetDAO.java

    public int getInvoicesCreatedThisMonth() throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM invoices WHERE MONTH(date) = MONTH(CURDATE())";
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("count");
        }
        return 0;
    }

    public double getTotalExpenses() throws SQLException {
        String sql = "SELECT SUM(amount) AS total FROM expense";
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("total");
        }
        return 0.0;
    }

    public double getTotalBudget() throws SQLException {
        String sql = "SELECT SUM(budget) AS total FROM projects";
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("total");
        }
        return 0.0;
    }

   


}
