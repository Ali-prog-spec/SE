package model.ProcurementOfficer;

import model.ProjectManager.*;
import model.InventoryManager.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Facade offering high-level operations:
 * - approveRequest(requestId)
 * - rejectRequest(requestId)
 *
 * Fully transactional: all updates occur in a single connection.
 * Notifications are sent after commit to avoid connection issues.
 */
public class MaterialRequestFacade {

    private final MaterialRequestDAO dao = new MaterialRequestDAO();

    /**
     * Approve a material request and deduct from inventory (transactional).
     *
     * @param requestId id of material_requests
     * @throws SQLException on DB error
     */
    public void approveRequest(int requestId) throws SQLException {
        MaterialRequest r;

        int newQty = -1;
        String resourceName = null;
        int projectId = -1;

        // 1️⃣ Transactional DB operations
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Load request
            r = dao.getById(conn, requestId);
            if (r == null) throw new SQLException("Request not found: " + requestId);

            projectId = r.getProjectId();

            // Update request status to Approved
            try (PreparedStatement pst = conn.prepareStatement(
                    "UPDATE material_requests SET status = ? WHERE id = ?")) {
                pst.setString(1, "Approved");
                pst.setInt(2, requestId);
                pst.executeUpdate();
            }

            // Deduct from inventory if not processed
            if (!r.isProcessed()) {
                resourceName = r.getMaterialName();

                if (resourceName == null) {
                    try (PreparedStatement pst = conn.prepareStatement(
                            "SELECT name FROM Resources WHERE id = ?")) {
                        pst.setInt(1, r.getMaterialId());
                        try (ResultSet rs = pst.executeQuery()) {
                            if (rs.next()) resourceName = rs.getString("name");
                        }
                    }
                }

                if (resourceName != null) {
                    try (PreparedStatement pst = conn.prepareStatement(
                            "SELECT id, quantity FROM inventory WHERE name = ? FOR UPDATE")) {
                        pst.setString(1, resourceName);
                        try (ResultSet rs = pst.executeQuery()) {
                            if (rs.next()) {
                                int invId = rs.getInt("id");
                                int currentQty = rs.getInt("quantity");
                                int deduct = r.getQuantity();
                                newQty = Math.max(currentQty - deduct, 0);

                                // Update inventory
                                try (PreparedStatement upd = conn.prepareStatement(
                                        "UPDATE inventory SET quantity = ? WHERE id = ?")) {
                                    upd.setInt(1, newQty);
                                    upd.setInt(2, invId);
                                    upd.executeUpdate();
                                }
                            } else {
                                throw new SQLException("Inventory not found for resource: " + resourceName);
                            }
                        }
                    }
                }

                // Mark request as processed
                try (PreparedStatement pst = conn.prepareStatement(
                        "UPDATE material_requests SET processed = 1 WHERE id = ?")) {
                    pst.setInt(1, requestId);
                    pst.executeUpdate();
                }
            }

            // Commit transaction
            conn.commit();

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw ex; // let controller handle it
        }

        try {
            int inventoryManagerId = -1;

            // Query Inventory Manager for this project
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pst = conn.prepareStatement(
                         "SELECT inventory_manager FROM projects WHERE id = ?")) {
                pst.setInt(1, projectId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        inventoryManagerId = rs.getInt("inventory_manager");
                    }
                }
            }

            if (inventoryManagerId > 0) {
                String msg = "Inventory updated for resource '" + resourceName +
                             "' (new quantity: " + newQty + ") due to approved request in Project ID: " + projectId;
                InvNotificationDAO.sendNotificationToUser(inventoryManagerId, msg);
            }

        } catch (Exception ex) {
            // Log, but don’t fail the main transaction
            ex.printStackTrace();
        }
    }

    /**
     * Reject a request (simple, single query).
     */
    public void rejectRequest(int requestId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "UPDATE material_requests SET status = ? WHERE id = ?")) {
            pst.setString(1, "Rejected");
            pst.setInt(2, requestId);
            pst.executeUpdate();
        }
    }
}
