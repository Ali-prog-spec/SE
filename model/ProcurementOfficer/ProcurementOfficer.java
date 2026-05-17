package model.ProcurementOfficer;

import model.ProjectManager.User;

public class ProcurementOfficer extends User {

    public ProcurementOfficer() {
        super();
        setRole("ProcurementOfficer");
    }

    public ProcurementOfficer(int userId, String username, String email, String fullName) {
        super(userId, username, email, "ProcurementOfficer", fullName);
    }
}
