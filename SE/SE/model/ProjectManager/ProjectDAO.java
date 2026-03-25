package model.ProjectManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    public static List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        String query = "SELECT * FROM projects";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Project p = new Project();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setStatus(rs.getString("status"));
                p.setStartDate(rs.getDate("start_date"));
                p.setEndDate(rs.getDate("end_date"));
                p.setBudget(rs.getDouble("budget"));
                projects.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return projects;
    }

    public static int getTotalProjects() {
        String query = "SELECT COUNT(*) AS total FROM projects";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static int getActiveProjects() {
        String query = "SELECT COUNT(*) AS active FROM projects WHERE status = 'On Track'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt("active");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static int getAtRiskProjects() {
        String query = "SELECT COUNT(*) AS atrisk FROM projects WHERE status = 'At Risk'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt("atrisk");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

     public static void deleteProject(int projectId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // transactional

            // 1️⃣ Delete milestones for this project
            try (PreparedStatement pst = conn.prepareStatement(
                    "DELETE FROM milestones WHERE project_id = ?")) {
                pst.setInt(1, projectId);
                pst.executeUpdate();
            }

            // 2️⃣ Delete project itself
            try (PreparedStatement pst = conn.prepareStatement(
                    "DELETE FROM projects WHERE id = ?")) {
                pst.setInt(1, projectId);
                int affected = pst.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Project not found with id: " + projectId);
                }
            }

            conn.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
