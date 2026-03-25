package model.ProjectManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDocumentDAO {

    public static void saveDocument(int projectId, String category, String fileName, String path) throws Exception {
        String sql = "INSERT INTO project_documents (project_id, category, file_name, file_path) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, projectId);
            pst.setString(2, category);
            pst.setString(3, fileName);
            pst.setString(4, path);
            pst.executeUpdate();
        }
    }

    public static List<ProjectDocument> getDocumentsByProject(int projectId) {
        List<ProjectDocument> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM project_documents WHERE project_id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, projectId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                ProjectDocument d = new ProjectDocument();
                d.setId(rs.getInt("id"));
                d.setProjectId(rs.getInt("project_id"));
                d.setCategory(rs.getString("category"));
                d.setFileName(rs.getString("file_name"));
                d.setFilePath(rs.getString("file_path"));
                d.setUploadedAt(rs.getTimestamp("uploaded_at"));
                list.add(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
