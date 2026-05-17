package model.ProcurementOfficer;

import model.ProjectManager.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaterialRequestDAO {

    // ===============================
    //  GET ALL REQUESTS
    // ===============================
    public List<MaterialRequest> getAllRequests() throws SQLException {
        String sql = """
                SELECT mr.id, mr.material_id, mr.project_id, mr.procurement_officer,
                       mr.user_id,
                       mr.quantity, mr.unit, mr.status, mr.requestDate, mr.processed,
                       r.name AS material_name
                FROM material_requests mr
                LEFT JOIN Resources r ON r.id = mr.material_id
                ORDER BY mr.requestDate DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            List<MaterialRequest> list = new ArrayList<>();

            while (rs.next()) {
                MaterialRequest req = new MaterialRequest(
                        rs.getInt("id"),
                        rs.getInt("material_id"),
                        rs.getInt("project_id"),
                        rs.getInt("procurement_officer"),
                        rs.getInt("user_id"),             // FIXED
                        rs.getInt("quantity"),
                        rs.getString("unit"),
                        rs.getString("status"),
                        rs.getTimestamp("requestDate"),
                        rs.getBoolean("processed"),
                        rs.getString("material_name")
                );

                list.add(req);
            }
            return list;
        }
    }



    // ===============================
    //  GET PENDING REQUESTS
    // ===============================
    public List<MaterialRequest> getPendingRequests() throws SQLException {
        String sql = """
                SELECT mr.id, mr.material_id, mr.project_id, mr.procurement_officer,
                       mr.user_id,
                       mr.quantity, mr.unit, mr.status, mr.requestDate, mr.processed,
                       r.name AS material_name
                FROM material_requests mr
                LEFT JOIN Resources r ON r.id = mr.material_id
                WHERE mr.status = 'Pending'
                ORDER BY mr.requestDate DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            List<MaterialRequest> list = new ArrayList<>();

            while (rs.next()) {
                list.add(new MaterialRequest(
                        rs.getInt("id"),
                        rs.getInt("material_id"),
                        rs.getInt("project_id"),
                        rs.getInt("procurement_officer"),
                        rs.getInt("user_id"),              // FIXED
                        rs.getInt("quantity"),
                        rs.getString("unit"),
                        rs.getString("status"),
                        rs.getTimestamp("requestDate"),
                        rs.getBoolean("processed"),
                        rs.getString("material_name")
                ));
            }

            return list;
        }
    }




    // ===============================
    //  GET BY ID (WITH CONNECTION)
    // ===============================
    public MaterialRequest getById(Connection conn, int id) throws SQLException {
        String sql = """
                SELECT mr.id, mr.material_id, mr.project_id, mr.procurement_officer,
                       mr.user_id,
                       mr.quantity, mr.unit, mr.status, mr.requestDate, mr.processed,
                       r.name AS material_name
                FROM material_requests mr
                LEFT JOIN Resources r ON r.id = mr.material_id
                WHERE mr.id = ?
                """;

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new MaterialRequest(
                            rs.getInt("id"),
                            rs.getInt("material_id"),
                            rs.getInt("project_id"),
                            rs.getInt("procurement_officer"),
                            rs.getInt("user_id"),           // FIXED
                            rs.getInt("quantity"),
                            rs.getString("unit"),
                            rs.getString("status"),
                            rs.getTimestamp("requestDate"),
                            rs.getBoolean("processed"),
                            rs.getString("material_name")
                    );
                }
            }
        }
        return null;
    }



    // ===============================
    //  UPDATE STATUS
    // ===============================
    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE material_requests SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, status);
            pst.setInt(2, id);
            pst.executeUpdate();
        }
    }



    // ===============================
    //  MARK PROCESSED
    // ===============================
    public void markProcessed(int id) throws SQLException {
        String sql = "UPDATE material_requests SET processed = 1 WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }



    // ===============================
    //  COUNT BY STATUS
    // ===============================
    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM material_requests WHERE status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, status);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt");
            }
        }
        return 0;
    }



    // ===============================
    //  COUNT LAST N DAYS
    // ===============================
    public int countByStatusLastDays(String status, int days) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS cnt
                FROM material_requests
                WHERE status = ?
                AND requestDate >= DATE_SUB(NOW(), INTERVAL ? DAY)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, status);
            pst.setInt(2, days);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt");
            }
        }

        return 0;
    }
}
