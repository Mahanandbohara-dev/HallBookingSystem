package model;

/**
 * Scheduler (staff) role - manages hall information and hall
 * availability / maintenance schedules.
 */
public class Scheduler extends User {

    public Scheduler(String userId, String username, String password, String fullName, String email) {
        super(userId, username, password, fullName, email);
    }

    @Override
    public String getRole() {
        return "SCHEDULER";
    }
}
