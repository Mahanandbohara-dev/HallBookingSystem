package gui;

import exception.CancellationNotAllowedException;
import exception.InvalidBookingException;
import io.FileManager;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CustomerDashboard extends JFrame {

    private final FileManager fileManager;
    private final Customer customer;

    private static final LocalTime OPEN_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(18, 0);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // Book Hall tab
    private JTable hallTable;
    private DefaultTableModel hallTableModel;
    private List<Hall> hallList;
    private JTextField dateField, startTimeField, endTimeField;
    private JLabel costPreviewLabel;

    // My Bookings tab
    private DefaultTableModel bookingsModel;
    private JTable bookingsTable;
    private JComboBox<String> bookingFilterCombo;

    // Profile tab
    private JTextField profileNameField, profileEmailField, profilePhoneField;
    private JPasswordField profilePasswordField;

    // Raise issue tab
    private JComboBox<Booking> issueBookingCombo;
    private JTextArea issueDescriptionArea;

    // My issues tab
    private DefaultTableModel issuesModel;
    private JTable issuesTable;

    public CustomerDashboard(FileManager fileManager, Customer customer) {
        this.fileManager = fileManager;
        this.customer = customer;
        fileManager.addDataChangeListener(dataType -> {
            if ("issues".equals(dataType)) {
                SwingUtilities.invokeLater(this::refreshIssuesTable);
            }
        });
        buildUI();
    }

    private void buildUI() {
        setTitle("Customer Dashboard - " + customer.getFullName());
        setSize(720, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Book a Hall", buildBookHallPanel());
        tabs.addTab("My Bookings", buildMyBookingsPanel());
        tabs.addTab("My Profile", buildProfilePanel());
        tabs.addTab("Raise an Issue", buildIssuePanel());
        tabs.addTab("My Issues", buildMyIssuesPanel());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(logoutButton);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    // -----------------------------------------------------------------
    // BOOK A HALL
    // -----------------------------------------------------------------

    private JPanel buildBookHallPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Available Halls (select a row):"), BorderLayout.NORTH);

        hallTableModel = new DefaultTableModel(
                new Object[]{"Hall Name", "Type", "Capacity", "Rate (RM/hr)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        hallTable = new JTable(hallTableModel);
        hallTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        hallTable.setRowSelectionAllowed(true);
        refreshHallTable();
        JScrollPane hallScroll = new JScrollPane(hallTable);
        hallScroll.setPreferredSize(new Dimension(600, 160));
        panel.add(hallScroll, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        dateField = new JTextField(LocalDate.now().plusDays(1).toString(), 12);
        gbc.gridx = 1;
        form.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Start Time (HH:mm, 08:00-18:00):"), gbc);
        startTimeField = new JTextField("09:00", 12);
        gbc.gridx = 1;
        form.add(startTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("End Time (HH:mm, 08:00-18:00):"), gbc);
        endTimeField = new JTextField("11:00", 12);
        gbc.gridx = 1;
        form.add(endTimeField, gbc);

        JButton previewButton = new JButton("Preview Cost");
        gbc.gridx = 0; gbc.gridy = 3;
        form.add(previewButton, gbc);
        costPreviewLabel = new JLabel("Total Cost: RM 0.00");
        gbc.gridx = 1;
        form.add(costPreviewLabel, gbc);

        JButton bookButton = new JButton("Proceed to Payment");
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        form.add(bookButton, gbc);

        panel.add(form, BorderLayout.SOUTH);

        previewButton.addActionListener(e -> {
            try {
                Hall hall = getSelectedHall();
                if (hall == null) {
                    costPreviewLabel.setText("Total Cost: (select a hall)");
                    return;
                }
                double hours = hoursBetween(startTimeField.getText(), endTimeField.getText());
                costPreviewLabel.setText("Total Cost: RM " + String.format("%.2f", hall.calculateCost(hours)));
            } catch (Exception ex) {
                costPreviewLabel.setText("Total Cost: (invalid time)");
            }
        });

        bookButton.addActionListener(e -> performBooking());

        return panel;
    }

    private Hall getSelectedHall() {
        int row = hallTable.getSelectedRow();
        if (row < 0 || hallList == null || row >= hallList.size()) return null;
        return hallList.get(row);
    }

    private double hoursBetween(String startText, String endText) {
        LocalTime start = LocalTime.parse(startText.trim(), TIME_FMT);
        LocalTime end = LocalTime.parse(endText.trim(), TIME_FMT);
        return java.time.Duration.between(start, end).toMinutes() / 60.0;
    }

    private void refreshHallTable() {
        hallList = fileManager.getActiveHalls();
        hallTableModel.setRowCount(0);
        for (Hall h : hallList) {
            hallTableModel.addRow(new Object[]{
                    h.getHallName(), h.getHallType(), h.getCapacity(),
                    String.format("%.2f", h.getRatePerHour())
            });
        }
    }

    private void performBooking() {
        try {
            Hall hall = getSelectedHall();
            if (hall == null) {
                throw new InvalidBookingException("No hall selected.");
            }

            LocalDate date;
            LocalTime startTime, endTime;
            try {
                date = LocalDate.parse(dateField.getText().trim(), DATE_FMT);
                startTime = LocalTime.parse(startTimeField.getText().trim(), TIME_FMT);
                endTime = LocalTime.parse(endTimeField.getText().trim(), TIME_FMT);
            } catch (Exception parseEx) {
                throw new InvalidBookingException("Please enter a valid date (YYYY-MM-DD) and times (HH:mm).");
            }

            if (date.isBefore(LocalDate.now())) {
                throw new InvalidBookingException("Booking date cannot be in the past.");
            }
            if (startTime.isBefore(OPEN_TIME) || endTime.isAfter(CLOSE_TIME) || !endTime.isAfter(startTime)) {
                throw new InvalidBookingException("Bookings must fall within business hours (08:00-18:00) and end time must be after start time.");
            }

            LocalDateTime start = LocalDateTime.of(date, startTime);
            LocalDateTime end = LocalDateTime.of(date, endTime);

            // conflict check against existing confirmed bookings for this hall
            for (Booking b : fileManager.getBookingsForHall(hall.getHallId())) {
                if (b.getStatus() == Booking.Status.CONFIRMED && overlaps(start, end, b.getStartDateTime(), b.getEndDateTime())) {
                    throw new InvalidBookingException("This hall is already booked for part of the selected time slot.");
                }
            }
            // conflict check against scheduler-set maintenance windows
            for (HallSchedule s : fileManager.getSchedulesForHall(hall.getHallId())) {
                if (s.getType() == HallSchedule.Type.MAINTENANCE && s.overlaps(start, end)) {
                    throw new InvalidBookingException("This hall is under maintenance during the selected time slot.");
                }
            }

            double hours = java.time.Duration.between(startTime, endTime).toMinutes() / 60.0;
            double cost = hall.calculateCost(hours);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Total for " + hours + " hour(s) at " + hall.getHallName() + ": RM " + String.format("%.2f", cost) +
                            "\nProceed with payment?",
                    "Confirm Payment", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            Booking booking = new Booking(fileManager.nextBookingId(), customer.getUserId(), hall.getHallId(),
                    start, end, cost, Booking.Status.CONFIRMED, LocalDateTime.now());
            fileManager.addBooking(booking);

            new ReceiptFrame(booking, hall, customer.getFullName()).setVisible(true);
            refreshBookingsTable();

        } catch (InvalidBookingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean overlaps(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        return s1.isBefore(e2) && s2.isBefore(e1);
    }

    // -----------------------------------------------------------------
    // MY BOOKINGS
    // -----------------------------------------------------------------

    private JPanel buildMyBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Show:"));
        bookingFilterCombo = new JComboBox<>(new String[]{"All", "Upcoming", "Past", "Cancelled"});
        filterPanel.add(bookingFilterCombo);
        JButton cancelButton = new JButton("Cancel Selected Booking");
        filterPanel.add(cancelButton);
        panel.add(filterPanel, BorderLayout.NORTH);

        bookingsModel = new DefaultTableModel(
                new Object[]{"Booking ID", "Hall", "Start", "End", "Cost (RM)", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bookingsTable = new JTable(bookingsModel);
        panel.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);

        bookingFilterCombo.addActionListener(e -> refreshBookingsTable());
        cancelButton.addActionListener(e -> cancelSelectedBooking());

        refreshBookingsTable();
        return panel;
    }

    private void refreshBookingsTable() {
        bookingsModel.setRowCount(0);
        String filter = (String) bookingFilterCombo.getSelectedItem();
        LocalDateTime now = LocalDateTime.now();
        for (Booking b : fileManager.getBookingsForCustomer(customer.getUserId())) {
            boolean include;
            switch (filter == null ? "All" : filter) {
                case "Upcoming": include = b.getStatus() == Booking.Status.CONFIRMED && b.getStartDateTime().isAfter(now); break;
                case "Past": include = b.getEndDateTime().isBefore(now); break;
                case "Cancelled": include = b.getStatus() == Booking.Status.CANCELLED; break;
                default: include = true;
            }
            if (!include) continue;
            Hall hall = fileManager.findHallById(b.getHallId());
            bookingsModel.addRow(new Object[]{
                    b.getBookingId(),
                    hall != null ? hall.getHallName() : b.getHallId(),
                    b.getStartDateTime(), b.getEndDateTime(),
                    String.format("%.2f", b.getTotalCost()),
                    b.getStatus()
            });
        }
    }

    private void cancelSelectedBooking() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.");
            return;
        }
        String bookingId = (String) bookingsModel.getValueAt(row, 0);
        try {
            Booking booking = fileManager.findBookingById(bookingId);
            if (booking == null) return;
            if (booking.getStatus() == Booking.Status.CANCELLED) {
                throw new CancellationNotAllowedException("This booking is already cancelled.");
            }
            long daysUntilBooking = java.time.Duration.between(LocalDateTime.now(), booking.getStartDateTime()).toDays();
            if (daysUntilBooking < 3) {
                throw new CancellationNotAllowedException(
                        "Cancellation not allowed. Bookings must be cancelled at least 3 days before the booking date.");
            }
            booking.setStatus(Booking.Status.CANCELLED);
            fileManager.updateBooking(booking);
            JOptionPane.showMessageDialog(this, "Booking cancelled successfully.");
            refreshBookingsTable();
        } catch (CancellationNotAllowedException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cancellation Not Allowed", JOptionPane.WARNING_MESSAGE);
        }
    }

    // -----------------------------------------------------------------
    // PROFILE
    // -----------------------------------------------------------------

    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Full Name:"), gbc);
        profileNameField = new JTextField(customer.getFullName(), 18);
        gbc.gridx = 1; panel.add(profileNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);
        profileEmailField = new JTextField(customer.getEmail(), 18);
        gbc.gridx = 1; panel.add(profileEmailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Phone:"), gbc);
        profilePhoneField = new JTextField(customer.getPhoneNumber(), 18);
        gbc.gridx = 1; panel.add(profilePhoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("New Password (optional):"), gbc);
        profilePasswordField = new JPasswordField(18);
        gbc.gridx = 1; panel.add(profilePasswordField, gbc);

        JButton saveButton = new JButton("Save Changes");
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            String name = profileNameField.getText().trim();
            String email = profileEmailField.getText().trim();
            String phone = profilePhoneField.getText().trim();
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name, email and phone cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            customer.setFullName(name);
            customer.setEmail(email);
            customer.setPhoneNumber(phone);
            String newPassword = new String(profilePasswordField.getPassword());
            if (!newPassword.isEmpty()) {
                if (newPassword.length() < 6) {
                    JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                customer.setPassword(newPassword);
            }
            fileManager.updateUser(customer);
            setTitle("Customer Dashboard - " + customer.getFullName());
            JOptionPane.showMessageDialog(this, "Profile updated successfully.");
        });

        return panel;
    }

    // -----------------------------------------------------------------
    // RAISE ISSUE
    // -----------------------------------------------------------------

    private JPanel buildIssuePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        top.add(new JLabel("Regarding Booking:"), gbc);
        issueBookingCombo = new JComboBox<>();
        refreshIssueBookingCombo();
        gbc.gridx = 1;
        top.add(issueBookingCombo, gbc);

        panel.add(top, BorderLayout.NORTH);

        issueDescriptionArea = new JTextArea(6, 30);
        issueDescriptionArea.setLineWrap(true);
        issueDescriptionArea.setBorder(BorderFactory.createTitledBorder("Describe the issue"));
        panel.add(new JScrollPane(issueDescriptionArea), BorderLayout.CENTER);

        JButton submitButton = new JButton("Submit Issue to Manager");
        JPanel bottom = new JPanel();
        bottom.add(submitButton);
        panel.add(bottom, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> {
            Booking selected = (Booking) issueBookingCombo.getSelectedItem();
            String description = issueDescriptionArea.getText().trim();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "You have no bookings to raise an issue for.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please describe the issue.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Issue issue = new Issue(fileManager.nextIssueId(), customer.getUserId(), selected.getBookingId(),
                    description, Issue.Status.OPEN, null, LocalDateTime.now());
            fileManager.addIssue(issue);
            issueDescriptionArea.setText("");
            refreshIssuesTable();
            JOptionPane.showMessageDialog(this, "Your issue has been submitted to the manager.");
        });

        return panel;
    }

    private JPanel buildMyIssuesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        issuesModel = new DefaultTableModel(
                new Object[]{"Issue ID", "Booking", "Description", "Status", "Manager Reply"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        issuesTable = new JTable(issuesModel);
        panel.add(new JScrollPane(issuesTable), BorderLayout.CENTER);
        refreshIssuesTable();
        return panel;
    }

    private void refreshIssueBookingCombo() {
        issueBookingCombo.removeAllItems();
        for (Booking b : fileManager.getBookingsForCustomer(customer.getUserId())) {
            issueBookingCombo.addItem(b);
        }
    }

    private void refreshIssuesTable() {
        if (issuesModel == null) return;
        issuesModel.setRowCount(0);
        for (Issue issue : fileManager.getIssuesForCustomer(customer.getUserId())) {
            issuesModel.addRow(new Object[]{
                    issue.getIssueId(),
                    issue.getBookingId(),
                    issue.getDescription(),
                    issue.getStatus(),
                    issue.getManagerReply() != null && !issue.getManagerReply().isBlank() ? issue.getManagerReply() : "(No reply yet)"
            });
        }
    }

    private void logout() {
        dispose();
        new LoginFrame(fileManager).setVisible(true);
    }
}
