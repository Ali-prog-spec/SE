package model.ProjectManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {

    // -------------------- FETCH ALL RESOURCES --------------------
    public static List<Resource> getAllResources() {
        List<Resource> resources = new ArrayList<>();

        String sql = "SELECT id, name, type, unit FROM resources ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Resource r = new Resource();
                r.setId(rs.getInt("id"));
                r.setName(rs.getString("name"));
                r.setType(rs.getString("type"));
                r.setUnit(rs.getString("unit"));
                resources.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resources;
    }

    // -------------------- ADD NEW RESOURCE --------------------
    public static boolean addResource(String name, String type, String unit) {
        String sql = "INSERT INTO resources (name, type, unit) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, name);
            pst.setString(2, type);
            pst.setString(3, unit);

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------- UPDATE RESOURCE --------------------
    public static boolean updateResource(int id, String name, String type, String unit) {
        String sql = "UPDATE resources SET name = ?, type = ?, unit = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, name);
            pst.setString(2, type);
            pst.setString(3, unit);
            pst.setInt(4, id);

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------- DELETE RESOURCE --------------------
    public static boolean deleteResource(int id) {
        String sql = "DELETE FROM resources WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, id);
            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------- FETCH SINGLE RESOURCE --------------------
    public static Resource getResourceById(int id) {
        String sql = "SELECT id, name, type, unit FROM resources WHERE id = ?";
        Resource resource = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    resource = new Resource();
                    resource.setId(rs.getInt("id"));
                    resource.setName(rs.getString("name"));
                    resource.setType(rs.getString("type"));
                    resource.setUnit(rs.getString("unit"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resource;
    }
}
