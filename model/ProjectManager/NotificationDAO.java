package model.ProjectManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    // --------------------- SEND NOTIFICATION ---------------------
  public static void sendNotificationToUser(int userId, String message) {
    String sql = "INSERT INTO notifications (user_id, message, is_read, created_at) " +
                 "VALUES (?, ?, FALSE, CURRENT_TIMESTAMP)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setInt(1, userId);
        pst.setString(2, message);

        pst.executeUpdate();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    // --------------------- GET NOTIFICATIONS FOR A USER ---------------------
    public static List<Notification> getNotificationsForUser(int userId) {
    List<Notification> notifications = new ArrayList<>();
    String query = "SELECT id, user_id, message, is_read, created_at FROM notifications WHERE user_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setInt(1, userId);

        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            Notification n = new Notification();
            n.setId(rs.getInt("id"));
            n.setUserId(rs.getInt("user_id"));
            n.setMessage(rs.getString("message"));
            n.setRead(rs.getBoolean("is_read"));
            n.setCreatedAt(rs.getTimestamp("created_at"));
            notifications.add(n);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return notifications;
}


   

    // --------------------- GET USERNAME BY ID ---------------------
    public static String getUsernameById(int userId) {
        String name = "";
        String sql = "SELECT username FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                name = rs.getString("username");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return name;
    }

}
