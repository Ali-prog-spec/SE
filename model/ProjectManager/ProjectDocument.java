package model.ProjectManager;
public class ProjectDocument {

    private int id;
    private int projectId;
    private String category;
    private String fileName;
    private String filePath;
    private java.sql.Timestamp uploadedAt;

    // ---- GETTERS ----

    public int getId() {
        return id;
    }

    public int getProjectId() {
        return projectId;
    }

    public String getCategory() {
        return category;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public java.sql.Timestamp getUploadedAt() {
        return uploadedAt;
    }

    // ---- SETTERS ----

    public void setId(int id) {
        this.id = id;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setUploadedAt(java.sql.Timestamp uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
