package model.FinanceOfficer;

import model.ProjectManager.User;

public class FinanceOfficer extends User {

    public FinanceOfficer() {
        super();
        setRole("FinanceOfficer");
    }

    public FinanceOfficer(int userId, String username, String email, String fullName) {
        super(userId, username, email, "FinanceOfficer", fullName);
    }
}
