package io;

import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Central class responsible for ALL reading/writing of the plain .txt
 * data files (users.txt, halls.txt, hall_schedules.txt, bookings.txt,
 * issues.txt), as required by the brief ("no database allowed").
 *
 * In-memory lists act as a simple cache; every mutating operation
 * immediately rewrites the corresponding file so data survives restarts.
 */
public class FileManager {

    public interface DataChangeListener {
        void onDataChanged(String dataType);
    }

    private static final String DATA_DIR = resolveDataDir();

    /**
     * Locates the project's "data" folder no matter which working directory
     * the app is launched from (e.g. project root vs. running Main.java
     * directly from inside "src/" in an IDE). Without this, a relative
     * "data" path would resolve to a different, empty folder depending on
     * the run configuration, causing existing users/halls/bookings to
     * appear "missing" (e.g. login failing for accounts that really do
     * exist in the real data/users.txt).
     */
    private static String resolveDataDir() {
        String[] candidates = {"data", "../data", "../../data"};
        for (String candidate : candidates) {
            if (new File(candidate).isDirectory()) {
                return candidate;
            }
        }
        return "data"; // none found: fall back to creating it here, as before
    }
    private static final String USERS_FILE = DATA_DIR + "/users.txt";
    private static final String HALLS_FILE = DATA_DIR + "/halls.txt";
    private static final String SCHEDULES_FILE = DATA_DIR + "/hall_schedules.txt";
    private static final String BOOKINGS_FILE = DATA_DIR + "/bookings.txt";
    private static final String ISSUES_FILE = DATA_DIR + "/issues.txt";

    private final List<User> users = new ArrayList<>();
    private final List<Hall> halls = new ArrayList<>();
    private final List<HallSchedule> schedules = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private final List<Issue> issues = new ArrayList<>();
    private final List<DataChangeListener> listeners = new ArrayList<>();

    public FileManager() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Could not create data directory: " + e.getMessage());
        }
        loadUsers();
        loadHalls();
        loadSchedules();
        loadBookings();
        loadIssues();
        seedIfEmpty();
    }

    // ---------------------------------------------------------------
    // USERS
    // ---------------------------------------------------------------

    private void loadUsers() {
        for (String line : readLines(USERS_FILE)) {
            String[] p = line.split("\\|", -1);
            if (p.length < 7) continue;
            String userId = p[0], role = p[1], username = p[2], password = p[3],
                    fullName = p[4], email = p[5];
            boolean blocked = Boolean.parseBoolean(p[6]);
            User user;
            switch (role) {
                case "CUSTOMER":
                    String phone = p.length > 7 ? p[7] : "";
                    user = new Customer(userId, username, password, fullName, email, phone);
                    break;
                case "SCHEDULER":
                    user = new Scheduler(userId, username, password, fullName, email);
                    break;
                case "ADMINISTRATOR":
                    user = new Administrator(userId, username, password, fullName, email);
                    break;
                case "MANAGER":
                    user = new Manager(userId, username, password, fullName, email);
                    break;
                default:
                    continue;
            }
            user.setBlocked(blocked);
            users.add(user);
        }
    }

    private void saveUsers() {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            String line = u.getUserId() + "|" + u.getRole() + "|" + u.getUsername() + "|" +
                    u.getPassword() + "|" + u.getFullName() + "|" + u.getEmail() + "|" + u.isBlocked();
            if (u instanceof Customer) {
                line += "|" + ((Customer) u).getPhoneNumber();
            }
            lines.add(line);
        }
        writeLines(USERS_FILE, lines);
    }

    public List<User> getAllUsers() { return new ArrayList<>(users); }

    public List<User> getUsersByRole(String role) {
        List<User> result = new ArrayList<>();
        for (User u : users) if (u.getRole().equals(role)) result.add(u);
        return result;
    }

    public User findUserByUsername(String username) {
        for (User u : users) if (u.getUsername().equalsIgnoreCase(username)) return u;
        return null;
    }

    public User findUserById(String userId) {
        for (User u : users) if (u.getUserId().equals(userId)) return u;
        return null;
    }

    public void addUser(User user) {
        users.add(user);
        saveUsers();
    }

    public void updateUser(User user) {
        saveUsers();
    }

    public void deleteUser(String userId) {
        users.removeIf(u -> u.getUserId().equals(userId));
        saveUsers();
    }

    public String nextUserId(String prefix) {
        List<String> ids = new ArrayList<>();
        for (User u : users) ids.add(u.getUserId());
        return nextId(prefix, ids);
    }

    // ---------------------------------------------------------------
    // HALLS
    // ---------------------------------------------------------------

    private void loadHalls() {
        for (String line : readLines(HALLS_FILE)) {
            String[] p = line.split("\\|", -1);
            if (p.length < 6) continue;
            String hallId = p[0], type = p[1], name = p[2];
            boolean active = Boolean.parseBoolean(p[5]);
            Hall hall;
            switch (type) {
                case "AUDITORIUM": hall = new Auditorium(hallId, name); break;
                case "BANQUET_HALL": hall = new BanquetHall(hallId, name); break;
                case "MEETING_ROOM": hall = new MeetingRoom(hallId, name); break;
                default: continue;
            }
            hall.setActive(active);
            halls.add(hall);
        }
    }

    private void saveHalls() {
        List<String> lines = new ArrayList<>();
        for (Hall h : halls) lines.add(h.toFileLine());
        writeLines(HALLS_FILE, lines);
    }

    public List<Hall> getAllHalls() { return new ArrayList<>(halls); }

    public List<Hall> getActiveHalls() {
        List<Hall> result = new ArrayList<>();
        for (Hall h : halls) if (h.isActive()) result.add(h);
        return result;
    }

    public Hall findHallById(String hallId) {
        for (Hall h : halls) if (h.getHallId().equals(hallId)) return h;
        return null;
    }

    public void addHall(Hall hall) {
        halls.add(hall);
        saveHalls();
    }

    public void updateHall(Hall hall) {
        saveHalls();
    }

    public void deleteHall(String hallId) {
        // soft delete: keep the record (bookings reference it) but mark inactive
        Hall h = findHallById(hallId);
        if (h != null) h.setActive(false);
        saveHalls();
    }

    public String nextHallId() {
        List<String> ids = new ArrayList<>();
        for (Hall h : halls) ids.add(h.getHallId());
        return nextId("H", ids);
    }

    // ---------------------------------------------------------------
    // HALL SCHEDULES (availability / maintenance)
    // ---------------------------------------------------------------

    private void loadSchedules() {
        for (String line : readLines(SCHEDULES_FILE)) {
            String[] p = line.split("\\|", -1);
            if (p.length < 5) continue;
            String scheduleId = p[0], hallId = p[1];
            HallSchedule.Type type = HallSchedule.Type.valueOf(p[2]);
            LocalDateTime start = LocalDateTime.parse(p[3]);
            LocalDateTime end = LocalDateTime.parse(p[4]);
            String remarks = p.length > 5 ? p[5] : "";
            schedules.add(new HallSchedule(scheduleId, hallId, type, start, end, remarks));
        }
    }

    private void saveSchedules() {
        List<String> lines = new ArrayList<>();
        for (HallSchedule s : schedules) lines.add(s.toFileLine());
        writeLines(SCHEDULES_FILE, lines);
    }

    public List<HallSchedule> getAllSchedules() { return new ArrayList<>(schedules); }

    public List<HallSchedule> getSchedulesForHall(String hallId) {
        List<HallSchedule> result = new ArrayList<>();
        for (HallSchedule s : schedules) if (s.getHallId().equals(hallId)) result.add(s);
        return result;
    }

    public void addSchedule(HallSchedule schedule) {
        schedules.add(schedule);
        saveSchedules();
    }

    public String nextScheduleId() {
        List<String> ids = new ArrayList<>();
        for (HallSchedule s : schedules) ids.add(s.getScheduleId());
        return nextId("SCH", ids);
    }

    // ---------------------------------------------------------------
    // BOOKINGS
    // ---------------------------------------------------------------

    private void loadBookings() {
        for (String line : readLines(BOOKINGS_FILE)) {
            String[] p = line.split("\\|", -1);
            if (p.length < 8) continue;
            bookings.add(new Booking(
                    p[0], p[1], p[2],
                    LocalDateTime.parse(p[3]), LocalDateTime.parse(p[4]),
                    Double.parseDouble(p[5]), Booking.Status.valueOf(p[6]),
                    LocalDateTime.parse(p[7])
            ));
        }
    }

    private void saveBookings() {
        List<String> lines = new ArrayList<>();
        for (Booking b : bookings) lines.add(b.toFileLine());
        writeLines(BOOKINGS_FILE, lines);
    }

    public List<Booking> getAllBookings() { return new ArrayList<>(bookings); }

    public List<Booking> getBookingsForCustomer(String customerId) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : bookings) if (b.getCustomerId().equals(customerId)) result.add(b);
        return result;
    }

    public List<Booking> getBookingsForHall(String hallId) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : bookings) if (b.getHallId().equals(hallId)) result.add(b);
        return result;
    }

    public Booking findBookingById(String bookingId) {
        for (Booking b : bookings) if (b.getBookingId().equals(bookingId)) return b;
        return null;
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
        saveBookings();
    }

    public void updateBooking(Booking booking) {
        saveBookings();
    }

    public String nextBookingId() {
        List<String> ids = new ArrayList<>();
        for (Booking b : bookings) ids.add(b.getBookingId());
        return nextId("BKG", ids);
    }

    // ---------------------------------------------------------------
    // ISSUES
    // ---------------------------------------------------------------

    private void loadIssues() {
        for (String line : readLines(ISSUES_FILE)) {
            String[] p = line.split("\\|", -1);
            if (p.length < 7) continue;
            String assigned = p[5].isEmpty() ? null : p[5];
            String managerReply = p.length > 7 ? p[7] : "";
            issues.add(new Issue(p[0], p[1], p[2], p[3], Issue.Status.valueOf(p[4]), assigned,
                    LocalDateTime.parse(p[6]), managerReply));
        }
    }

    private void saveIssues() {
        List<String> lines = new ArrayList<>();
        for (Issue i : issues) lines.add(i.toFileLine());
        writeLines(ISSUES_FILE, lines);
    }

    public List<Issue> getAllIssues() { return new ArrayList<>(issues); }

    public void addDataChangeListener(DataChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    private void notifyDataChanged(String dataType) {
        for (DataChangeListener listener : new ArrayList<>(listeners)) {
            listener.onDataChanged(dataType);
        }
    }

    public List<Issue> getIssuesForCustomer(String customerId) {
        List<Issue> result = new ArrayList<>();
        for (Issue i : issues) if (i.getCustomerId().equals(customerId)) result.add(i);
        return result;
    }

    public void addIssue(Issue issue) {
        issues.add(issue);
        saveIssues();
        notifyDataChanged("issues");
    }

    public void updateIssue(Issue issue) {
        saveIssues();
        notifyDataChanged("issues");
    }

    public String nextIssueId() {
        List<String> ids = new ArrayList<>();
        for (Issue i : issues) ids.add(i.getIssueId());
        return nextId("ISS", ids);
    }

    // ---------------------------------------------------------------
    // Generic file helpers
    // ---------------------------------------------------------------

    private List<String> readLines(String path) {
        List<String> lines = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return lines;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading " + path + ": " + e.getMessage());
        }
        return lines;
    }

    private void writeLines(String path, List<String> lines) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path, false), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing " + path + ": " + e.getMessage());
        }
    }

    /**
     * Scans all ids that start with the given prefix across the relevant
     * in-memory list and returns prefix + (max numeric suffix + 1),
     * zero-padded to 3 digits. Robust to deletions (unlike a simple
     * list-size counter).
     */
    private String nextId(String prefix, List<String> existingIds) {
        int max = 0;
        for (String id : existingIds) {
            if (id != null && id.startsWith(prefix)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(prefix.length())));
                } catch (NumberFormatException ignored) { /* not a numeric suffix, skip */ }
            }
        }
        return prefix + String.format("%03d", max + 1);
    }

    /** Seeds one default account per role + the 3 hall types on first run. */
    private void seedIfEmpty() {
        boolean changed = false;
        if (users.isEmpty()) {
            users.add(new Administrator(nextUserId("U"), "admin", "admin123", "System Administrator", "admin@hallsymphony.com"));
            users.add(new Manager(nextUserId("U"), "manager", "manager123", "System Manager", "manager@hallsymphony.com"));
            users.add(new Scheduler(nextUserId("U"), "scheduler", "scheduler123", "System Scheduler", "scheduler@hallsymphony.com"));
            users.add(new Customer(nextUserId("U"), "customer", "customer123", "Demo Customer", "customer@example.com", "0123456789"));
            changed = true;
        }
        if (halls.isEmpty()) {
            halls.add(new Auditorium(nextHallId(), "Main Auditorium"));
            halls.add(new BanquetHall(nextHallId(), "Grand Banquet Hall"));
            halls.add(new MeetingRoom(nextHallId(), "Meeting Room A"));
            changed = true;
        }
        if (changed) {
            saveUsers();
            saveHalls();
        }
    }
}
