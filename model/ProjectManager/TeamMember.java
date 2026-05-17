package model.ProjectManager;
public class TeamMember {
    private int id;         // database primary key for project_team table
    private String name;    // worker name
    private String role;    // worker role
    private String task;    // assigned task

    public TeamMember() {}

    public TeamMember(int id, String name, String role, String task) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.task = task;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}
