package model.ProjectManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProjectTeamDAO {

    public List<TeamMember> getTeamMembersByProject(int projectId) {
        List<TeamMember> members = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, member_name, role, task FROM project_team WHERE project_id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, projectId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                TeamMember member = new TeamMember();
                member.setId(rs.getInt("id"));
                member.setName(rs.getString("member_name"));
                member.setRole(rs.getString("role"));
                member.setTask(rs.getString("task"));
                members.add(member);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return members;
    }

    public boolean assignTask(int memberId, String task) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE project_team SET task = ? WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, task);
            pst.setInt(2, memberId);
            int updated = pst.executeUpdate();
            return updated > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
