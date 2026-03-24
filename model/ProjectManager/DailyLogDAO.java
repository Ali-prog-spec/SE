package model.ProjectManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DailyLogDAO {

    public static void saveLog(int projectId, String description) throws Exception {
        String sql = "INSERT INTO daily_logs(project_id, log_date, description) VALUES (?, CURDATE(), ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            stmt.setString(2, description);
            stmt.executeUpdate();
        }
    }

    public static List<String> getLogsForToday(int projectId) throws Exception {
        List<String> logs = new ArrayList<>();

        String sql = "SELECT description FROM daily_logs WHERE project_id = ? AND log_date = CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(rs.getString("description"));
            }
        }
        return logs;
    }
}
