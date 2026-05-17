package model.InventoryManager;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryItemFactory {

    public static InventoryItem createFromResultSet(ResultSet rs) throws SQLException {
        return new InventoryItem(
                rs.getString("name"),
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getString("unit"),
                rs.getInt("min_stock"),
                rs.getInt("quantity") < rs.getInt("min_stock") ? "Low Stock" : "In Stock"
        );
    }
}
