package model.InventoryManager;

import model.ProjectManager.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
// 
public class InventoryDAO {

    private final Connection conn;

    public InventoryDAO() {
        this.conn = DatabaseConnection.getConnection();
    }

    public List<InventoryItem> getAllInventory() throws SQLException {
        List<InventoryItem> inventoryList = new ArrayList<>();
        String query = "SELECT * FROM inventory";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int quantity = rs.getInt("quantity");
                int minStock = rs.getInt("min_stock");
                String status;

                if (quantity <= minStock / 2) status = "critical";
                else if (quantity <= minStock) status = "low";
                else status = "ok";

                InventoryItem item = new InventoryItem(
                        rs.getString("name"),
                        rs.getString("category"),
                        quantity,
                        rs.getString("unit"),
                        minStock,
                        status
                );
                inventoryList.add(item);
            }
        }

        return inventoryList;
    }

    public void addInventoryItem(InventoryItem item) throws SQLException {
        String query = "INSERT INTO inventory (name, category, quantity, unit, min_stock) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setInt(3, item.getQuantity());
            ps.setString(4, item.getUnit());
            ps.setInt(5, item.getMinStock());
            ps.executeUpdate();
        }
    }

    public void updateInventoryItem(InventoryItem item, String oldName) throws SQLException {
        String query = "UPDATE inventory SET name=?, category=?, quantity=?, unit=?, min_stock=? WHERE name=?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setInt(3, item.getQuantity());
            ps.setString(4, item.getUnit());
            ps.setInt(5, item.getMinStock());
            ps.setString(6, oldName);
            ps.executeUpdate();
        }
    }

    public void deleteInventoryItem(InventoryItem item) throws SQLException {
        String query = "DELETE FROM inventory WHERE name=?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, item.getName());
            ps.executeUpdate();
        }
    }
}
