package model.ProjectManager;
import java.util.Date;

public class ProjectPhase {
    private int id;
    private int projectId;
    private int phaseNumber;
    private String name;
    private Date startDate;
    private Date endDate;
    private String description;
    private String status;

    // getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }
    public int getPhaseNumber() { return phaseNumber; }
    public void setPhaseNumber(int phaseNumber) { this.phaseNumber = phaseNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
}
