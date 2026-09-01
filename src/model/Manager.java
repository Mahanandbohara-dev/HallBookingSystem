package model;

/**
 * Manager (staff) role - oversees sales performance and maintenance
 * operations (customer-raised issues).
 */
public class Manager extends User {

    public Manager(String userId, String username, String password, String fullName, String email) {
        super(userId, username, password, fullName, email);
    }

    @Override
    public String getRole() {
        return "MANAGER";
    }
}
