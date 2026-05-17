package model.ProjectManager;
import java.sql.Timestamp;

public class User {
    private int userId;
    private String username;
    private String password;
    private String email;
    private String role;
    private String fullName;
    private Timestamp createdAt;
    
    // Constructors
    public User() {}
    
    public User(int userId, String username, String email, String role, String fullName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
    }
    
    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getName() {
    return fullName;}

    // Generic getter for integer fields
public int getInt(String field) {
    switch (field) {
        case "userId": return userId;
        default:
            throw new IllegalArgumentException("Invalid int field: " + field);
    }
}

// Generic getter for string fields
public String getString(String field) {
    switch (field) {
        case "username": return username;
        case "password": return password;
        case "email": return email;
        case "role": return role;
        case "fullName": return fullName;
        default:
            throw new IllegalArgumentException("Invalid string field: " + field);
    }
}


}