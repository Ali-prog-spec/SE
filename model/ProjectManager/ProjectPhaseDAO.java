package model.ProjectManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectPhaseDAO {

    // Get all phases for a project, including status
    public static List<ProjectPhase> getPhasesByProject(int projectId) {
        List<ProjectPhase> phases = new ArrayList<>();
        String sql = "SELECT * FROM project_phases WHERE project_id = ? ORDER BY phase_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, projectId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ProjectPhase phase = new ProjectPhase();
                phase.setId(rs.getInt("id"));
                phase.setProjectId(rs.getInt("project_id"));
                phase.setPhaseNumber(rs.getInt("phase_number"));
                phase.setName(rs.getString("name"));
                phase.setStartDate(rs.getDate("start_date"));
                phase.setEndDate(rs.getDate("end_date"));
                phase.setDescription(rs.getString("description"));
                
                // Get status column
                String status = rs.getString("status");
                if (status == null || status.isEmpty()) status = "In Progress"; // default fallback
                phase.setStatus(status);
                
                phases.add(phase);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phases;
    }

    // Get next phase number
    public static int getNextPhaseNumber(int projectId) {
        String sql = "SELECT MAX(phase_number) AS max_phase FROM project_phases WHERE project_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, projectId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("max_phase") + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1; // first phase
    }

    // Add a new phase, default status = "In Progress"
    public static void addPhase(ProjectPhase phase) {
        String sql = "INSERT INTO project_phases (project_id, phase_number, name, start_date, end_date, description, status) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, phase.getProjectId());
            pst.setInt(2, phase.getPhaseNumber());
            pst.setString(3, phase.getName());
            pst.setDate(4, new java.sql.Date(phase.getStartDate().getTime()));
            pst.setDate(5, new java.sql.Date(phase.getEndDate().getTime()));
            pst.setString(6, phase.getDescription());
            pst.setString(7, "In Progress"); // default status
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void updatePhaseStatus(int phaseId, String status) {
    String sql = "UPDATE project_phases SET status = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {
        pst.setString(1, status);
        pst.setInt(2, phaseId);
        pst.executeUpdate();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

}
