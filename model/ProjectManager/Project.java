package model.ProjectManager;
import java.util.Date;

public class Project {
    private int id;
    private String name;
    private String client;
    private String location;
    private Date startDate;
    private Date endDate;
    private double budget;
    private String team; // comma-separated team members or JSON
    private String description;
    private String status;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

     @Override
    public String toString() {
        return name; // so ComboBox shows the project name
    }
}
