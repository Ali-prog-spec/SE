package model.InventoryManager;

import model.ProjectManager.User;

public class InventoryManagerNew extends User {

    public InventoryManagerNew() {
        super();
        setRole("InventoryManager");
    }

    public InventoryManagerNew(int userId, String username, String email, String fullName) {
        super(userId, username, email, "InventoryManager", fullName);
    }
}
