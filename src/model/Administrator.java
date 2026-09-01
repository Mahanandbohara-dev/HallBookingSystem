package model;

/**
 * Administrator (staff) role - manages scheduler staff accounts and
 * customer/user accounts.
 */
public class Administrator extends User {

    public Administrator(String userId, String username, String password, String fullName, String email) {
        super(userId, username, password, fullName, email);
    }

    @Override
    public String getRole() {
        return "ADMINISTRATOR";
    }
}
