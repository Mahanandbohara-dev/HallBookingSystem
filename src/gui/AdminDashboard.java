package gui;

import io.FileManager;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;

public class AdminDashboard extends JFrame {

    private final FileManager fileManager;
    private final Administrator admin;

    // Scheduler staff tab
    private DefaultTableModel schedulerModel;
    private JTable schedulerTable;

    // User management tab
    private DefaultTableModel userModel;
    private JTable userTable;
    private JComboBox<String> userRoleFilterCombo;

    // Booking management tab
    private DefaultTableModel bookingModel;
    private JTable bookingTable;
    private JComboBox<String> bookingFilterCombo;

    public AdminDashboard(FileManager fileManager, Administrator admin) {
        this.fileManager = fileManager;
        this.admin = admin;
        buildUI();
    }

    private void buildUI() {
        setTitle("Administrator Dashboard - " + admin.getFullName());
        setSize(800, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Scheduler Staff Management", buildSchedulerStaffPanel());
        tabs.addTab("User Management", buildUserManagementPanel());
        tabs.addTab("Booking Management", buildBookingManagementPanel());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(logoutButton);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    // -----------------------------------------------------------------
    // SCHEDULER STAFF MANAGEMENT (add / view+filter / edit / delete)
    // -----------------------------------------------------------------

    private JPanel buildSchedulerStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Scheduler");
        JButton editButton = new JButton("Edit Selected");
        JButton deleteButton = new JButton("Delete Selected");
        top.add(addButton);
        top.add(editButton);
        top.add(deleteButton);
        panel.add(top, BorderLayout.NORTH);

        schedulerModel = new DefaultTableModel(new Object[]{"User ID", "Username", "Full Name", "Email", "Blocked"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        schedulerTable = new JTable(schedulerModel);
        panel.add(new JScrollPane(schedulerTable), BorderLayout.CENTER);

        addButton.addActionListener(e -> showAddSchedulerDialog());
        editButton.addActionListener(e -> showEditSchedulerDialog());
        deleteButton.addActionListener(e -> deleteSelectedScheduler());

        refreshSchedulerTable();
        return panel;
    }

    private void refreshSchedulerTable() {
        schedulerModel.setRowCount(0);
        for (User u : fileManager.getUsersByRole("SCHEDULER")) {
            schedulerModel.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getFullName(), u.getEmail(), u.isBlocked()});
        }
    }

    private void showAddSchedulerDialog() {
        JTextField nameField = new JTextField();
        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(new JLabel("Full Name:")); form.add(nameField);
        form.add(new JLabel("Username:")); form.add(usernameField);
        form.add(new JLabel("Email:")); form.add(emailField);
        form.add(new JLabel("Password:")); form.add(passwordField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add Scheduler Staff", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (fileManager.findUserByUsername(username) != null) {
            JOptionPane.showMessageDialog(this, "Username already taken.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Scheduler scheduler = new Scheduler(fileManager.nextUserId("U"), username, password, name, email);
        fileManager.addUser(scheduler);
        refreshSchedulerTable();
        refreshUserTable();
        JOptionPane.showMessageDialog(this, "Scheduler staff added.");
    }

    private void showEditSchedulerDialog() {
        int row = schedulerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a scheduler to edit.");
            return;
        }
        String userId = (String) schedulerModel.getValueAt(row, 0);
        User user = fileManager.findUserById(userId);
        if (user == null) return;

        JTextField nameField = new JTextField(user.getFullName());
        JTextField emailField = new JTextField(user.getEmail());
        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(new JLabel("Full Name:")); form.add(nameField);
        form.add(new JLabel("Email:")); form.add(emailField);

        int result = JOptionPane.showConfirmDialog(this, form, "Edit Scheduler Staff", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        user.setFullName(nameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        fileManager.updateUser(user);
        refreshSchedulerTable();
        refreshUserTable();
    }

    private void deleteSelectedScheduler() {
        int row = schedulerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a scheduler to delete.");
            return;
        }
        String userId = (String) schedulerModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this scheduler account?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        fileManager.deleteUser(userId);
        refreshSchedulerTable();
        refreshUserTable();
    }

    // -----------------------------------------------------------------
    // USER MANAGEMENT (view+filter / block / delete)
    // -----------------------------------------------------------------

    private JPanel buildUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Filter by role:"));
        userRoleFilterCombo = new JComboBox<>(new String[]{"ALL", "CUSTOMER", "SCHEDULER", "ADMINISTRATOR", "MANAGER"});
        top.add(userRoleFilterCombo);
        JButton blockButton = new JButton("Block/Unblock Selected");
        JButton deleteButton = new JButton("Delete Selected");
        top.add(blockButton);
        top.add(deleteButton);
        panel.add(top, BorderLayout.NORTH);

        userModel = new DefaultTableModel(new Object[]{"User ID", "Role", "Username", "Full Name", "Email", "Blocked"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = new JTable(userModel);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        userRoleFilterCombo.addActionListener(e -> refreshUserTable());
        blockButton.addActionListener(e -> toggleBlockSelectedUser());
        deleteButton.addActionListener(e -> deleteSelectedUser());

        refreshUserTable();
        return panel;
    }

    private void refreshUserTable() {
        userModel.setRowCount(0);
        String filter = (String) userRoleFilterCombo.getSelectedItem();
        for (User u : fileManager.getAllUsers()) {
            if (filter != null && !filter.equals("ALL") && !u.getRole().equals(filter)) continue;
            userModel.addRow(new Object[]{u.getUserId(), u.getRole(), u.getUsername(), u.getFullName(), u.getEmail(), u.isBlocked()});
        }
    }

    private void toggleBlockSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user.");
            return;
        }
        String userId = (String) userModel.getValueAt(row, 0);
        User user = fileManager.findUserById(userId);
        if (user == null) return;
        if (user.getRole().equals("ADMINISTRATOR")) {
            JOptionPane.showMessageDialog(this, "Administrator accounts cannot be blocked.", "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        user.setBlocked(!user.isBlocked());
        fileManager.updateUser(user);
        refreshUserTable();
        refreshSchedulerTable();
    }

    private void deleteSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user.");
            return;
        }
        String userId = (String) userModel.getValueAt(row, 0);
        User user = fileManager.findUserById(userId);
        if (user != null && user.getRole().equals("ADMINISTRATOR")) {
            JOptionPane.showMessageDialog(this, "Administrator accounts cannot be deleted.", "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this user account?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        fileManager.deleteUser(userId);
        refreshUserTable();
        refreshSchedulerTable();
    }

    // -----------------------------------------------------------------
    // BOOKING MANAGEMENT (view+filter for all customers)
    // -----------------------------------------------------------------

    private JPanel buildBookingManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Show:"));
        bookingFilterCombo = new JComboBox<>(new String[]{"All", "Upcoming", "Past", "Cancelled"});
        top.add(bookingFilterCombo);
        panel.add(top, BorderLayout.NORTH);

        bookingModel = new DefaultTableModel(
                new Object[]{"Booking ID", "Customer", "Hall", "Start", "End", "Cost (RM)", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bookingTable = new JTable(bookingModel);
        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);

        bookingFilterCombo.addActionListener(e -> refreshBookingTable());
        refreshBookingTable();
        return panel;
    }

    private void refreshBookingTable() {
        bookingModel.setRowCount(0);
        String filter = (String) bookingFilterCombo.getSelectedItem();
        LocalDateTime now = LocalDateTime.now();
        for (Booking b : fileManager.getAllBookings()) {
            boolean include;
            switch (filter == null ? "All" : filter) {
                case "Upcoming": include = b.getStatus() == Booking.Status.CONFIRMED && b.getStartDateTime().isAfter(now); break;
                case "Past": include = b.getEndDateTime().isBefore(now); break;
                case "Cancelled": include = b.getStatus() == Booking.Status.CANCELLED; break;
                default: include = true;
            }
            if (!include) continue;
            User customer = fileManager.findUserById(b.getCustomerId());
            Hall hall = fileManager.findHallById(b.getHallId());
            bookingModel.addRow(new Object[]{
                    b.getBookingId(),
                    customer != null ? customer.getFullName() : b.getCustomerId(),
                    hall != null ? hall.getHallName() : b.getHallId(),
                    b.getStartDateTime(), b.getEndDateTime(),
                    String.format("%.2f", b.getTotalCost()),
                    b.getStatus()
            });
        }
    }

    private void logout() {
        dispose();
        new LoginFrame(fileManager).setVisible(true);
    }
}
