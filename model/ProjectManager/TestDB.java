package model.ProjectManager;
public class TestDB {
    public static void main(String[] args) {
        if (DatabaseConnection.getConnection() != null) {
            System.out.println("Test connection successful!");
            DatabaseConnection.closeConnection();
        } else {
            System.out.println("Test connection failed!");
        }
    }
}
