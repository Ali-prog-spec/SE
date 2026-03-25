package model.ProjectManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MilestoneDAO {

    public static List<Milestone> getUpcomingMilestones() {
        List<Milestone> milestones = new ArrayList<>();
        String query = "SELECT m.id, m.name, m.status, m.due_date, p.name AS project_name " +
                       "FROM milestones m JOIN projects p ON m.project_id = p.id " +
                       "WHERE m.due_date >= CURDATE() ORDER BY m.due_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Milestone m = new Milestone();
                m.setId(rs.getInt("id"));
                m.setName(rs.getString("name"));
                m.setStatus(rs.getString("status"));
                m.setDueDate(rs.getDate("due_date"));
                m.setProjectName(rs.getString("project_name"));
                milestones.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return milestones;
    }

    public static List<Milestone> getMilestonesByProject(int projectId) {
    List<Milestone> milestones = new ArrayList<>();
    String query = "SELECT * FROM milestones WHERE project_id = ? ORDER BY due_date ASC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(query)) {

        ps.setInt(1, projectId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Milestone m = new Milestone();
            m.setId(rs.getInt("id"));
            m.setName(rs.getString("name"));
            m.setDueDate(rs.getDate("due_date"));
            milestones.add(m);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return milestones;
}

 public static void deleteMilestone(int milestoneId) throws SQLException {
        String sql = "DELETE FROM milestones WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, milestoneId);
            int affected = pst.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Milestone not found with id: " + milestoneId);
            }
        }
    }

}
