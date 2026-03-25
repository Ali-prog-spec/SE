package model.ProjectManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    // Get all tasks for a project
    public static List<Task> getTasksByProjectId(int projectId) {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT * FROM tasks WHERE project_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, projectId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setProjectId(rs.getInt("project_id"));
                task.setName(rs.getString("name"));
                task.setAssignedTo(rs.getInt("assigned_to"));
                task.setStatus(rs.getString("status"));
                task.setStartDate(rs.getDate("start_date"));
                task.setEndDate(rs.getDate("end_date"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    // Calculate project completion percentage
    public static int getCompletionPercentage(int projectId) {
        List<Task> tasks = getTasksByProjectId(projectId);
        if (tasks.isEmpty()) return 0;

        int completedTasks = 0;
        for (Task t : tasks) {
            if ("Completed".equalsIgnoreCase(t.getStatus())) {
                completedTasks++;
            }
        }

        return (int) ((double) completedTasks / tasks.size() * 100);
    }
}
