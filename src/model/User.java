package model;

import java.io.Serializable;

/**
 * Abstract base class for every person who can log into the system.
 * Demonstrates ABSTRACTION (common user behaviour is defined here,
 * role-specific behaviour is left to subclasses) and is the root of
 * the INHERITANCE hierarchy: Scheduler, Customer, Administrator, Manager.
 */
public abstract class User implements Serializable {

    private String userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private boolean blocked;

    public User(String userId, String username, String password, String fullName, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.blocked = false;
    }

    // Every role must be able to describe itself -> forces POLYMORPHISM
    // when the GUI needs to display "Role: X" without checking instanceof.
    public abstract String getRole();

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public boolean isBlocked() { return blocked; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public boolean checkPassword(String candidate) {
        return this.password != null && this.password.equals(candidate);
    }

    /**
     * Serializes this user to a single pipe-delimited line for text-file storage.
     * Subclasses append their own extra fields (extends the format, an example
     * of a template-method style use of polymorphism).
     */
    public String toFileLine() {
        return userId + "|" + username + "|" + password + "|" + fullName + "|" + email + "|" + blocked;
    }

    @Override
    public String toString() {
        return fullName + " (" + getRole() + ")";
    }
}
