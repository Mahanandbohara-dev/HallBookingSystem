package gui;

import io.FileManager;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SchedulerDashboard extends JFrame {

    private final FileManager fileManager;
    private final Scheduler scheduler;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // Hall management tab
    private DefaultTableModel hallModel;
    private JTable hallTable;
    private JComboBox<String> hallTypeFilterCombo;

    // Schedule tab
    private JComboBox<Hall> scheduleHallCombo;
    private JComboBox<HallSchedule.Type> scheduleTypeCombo;
    private JTextField schedStartDateField, schedStartTimeField, schedEndDateField, schedEndTimeField, schedRemarksField;
    private DefaultTableModel scheduleModel;
    private JTable scheduleTable;

    public SchedulerDashboard(FileManager fileManager, Scheduler scheduler) {
        this.fileManager = fileManager;
        this.scheduler = scheduler;
        buildUI();
    }

    private void buildUI() {
        setTitle("Scheduler Dashboard - " + scheduler.getFullName());
        setSize(780, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Hall Management", buildHallManagementPanel());
        tabs.addTab("Hall Availability / Maintenance Schedule", buildSchedulePanel());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(logoutButton);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    // -----------------------------------------------------------------
    // HALL MANAGEMENT (add / view+filter / edit / delete)
    // -----------------------------------------------------------------

    private JPanel buildHallManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Filter by type:"));
        hallTypeFilterCombo = new JComboBox<>(new String[]{"ALL", "AUDITORIUM", "BANQUET_HALL", "MEETING_ROOM"});
        top.add(hallTypeFilterCombo);
        JButton addButton = new JButton("Add New Hall");
        JButton editButton = new JButton("Edit Selected");
        JButton deleteButton = new JButton("Delete Selected");
        top.add(addButton);
        top.add(editButton);
        top.add(deleteButton);
        panel.add(top, BorderLayout.NORTH);

        hallModel = new DefaultTableModel(new Object[]{"Hall ID", "Type", "Name", "Capacity", "Rate/Hour (RM)", "Active"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        hallTable = new JTable(hallModel);
        panel.add(new JScrollPane(hallTable), BorderLayout.CENTER);

        hallTypeFilterCombo.addActionListener(e -> refreshHallTable());
        addButton.addActionListener(e -> showAddHallDialog());
        editButton.addActionListener(e -> showEditHallDialog());
        deleteButton.addActionListener(e -> deleteSelectedHall());

        refreshHallTable();
        return panel;
    }

    private void refreshHallTable() {
        hallModel.setRowCount(0);
        String filter = (String) hallTypeFilterCombo.getSelectedItem();
        for (Hall h : fileManager.getAllHalls()) {
            if (filter != null && !filter.equals("ALL") && !h.getHallType().equals(filter)) continue;
            hallModel.addRow(new Object[]{h.getHallId(), h.getHallType(), h.getHallName(), h.getCapacity(),
                    h.getRatePerHour(), h.isActive()});
        }
        refreshScheduleHallCombo();
    }

    private void showAddHallDialog() {
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"AUDITORIUM", "BANQUET_HALL", "MEETING_ROOM"});
        JTextField nameField = new JTextField();
        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(new JLabel("Hall Type (fixed capacity/rate per Table 1):"));
        form.add(typeCombo);
        form.add(new JLabel("Hall Name:"));
        form.add(nameField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Hall", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hall name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String type = (String) typeCombo.getSelectedItem();
        Hall hall;
        switch (type) {
            case "AUDITORIUM": hall = new Auditorium(fileManager.nextHallId(), name); break;
            case "BANQUET_HALL": hall = new BanquetHall(fileManager.nextHallId(), name); break;
            default: hall = new MeetingRoom(fileManager.nextHallId(), name); break;
        }
        fileManager.addHall(hall);
        refreshHallTable();
        JOptionPane.showMessageDialog(this, "Hall added successfully.");
    }

    private void showEditHallDialog() {
        int row = hallTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a hall to edit.");
            return;
        }
        String hallId = (String) hallModel.getValueAt(row, 0);
        Hall hall = fileManager.findHallById(hallId);
        if (hall == null) return;

        JTextField nameField = new JTextField(hall.getHallName());
        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(new JLabel("Hall Name (capacity and rate are fixed by hall type):"));
        form.add(nameField);

        int result = JOptionPane.showConfirmDialog(this, form, "Edit Hall", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hall name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        hall.setHallName(name);
        fileManager.updateHall(hall);
        refreshHallTable();
        JOptionPane.showMessageDialog(this, "Hall updated successfully.");
    }

    private void deleteSelectedHall() {
        int row = hallTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a hall to delete.");
            return;
        }
        String hallId = (String) hallModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this hall? Existing bookings referencing it are kept for records.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        fileManager.deleteHall(hallId);
        refreshHallTable();
    }

    // -----------------------------------------------------------------
    // AVAILABILITY / MAINTENANCE SCHEDULE
    // -----------------------------------------------------------------

    private JPanel buildSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Hall:"), gbc);
        scheduleHallCombo = new JComboBox<>();
        gbc.gridx = 1;
        form.add(scheduleHallCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Schedule Type:"), gbc);
        scheduleTypeCombo = new JComboBox<>(HallSchedule.Type.values());
        gbc.gridx = 1;
        form.add(scheduleTypeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Start Date (YYYY-MM-DD) / Time (HH:mm):"), gbc);
        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        schedStartDateField = new JTextField(LocalDate.now().toString(), 10);
        schedStartTimeField = new JTextField("08:00", 6);
        startPanel.add(schedStartDateField);
        startPanel.add(schedStartTimeField);
        gbc.gridx = 1;
        form.add(startPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("End Date (YYYY-MM-DD) / Time (HH:mm):"), gbc);
        JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        schedEndDateField = new JTextField(LocalDate.now().toString(), 10);
        schedEndTimeField = new JTextField("18:00", 6);
        endPanel.add(schedEndDateField);
        endPanel.add(schedEndTimeField);
        gbc.gridx = 1;
        form.add(endPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        form.add(new JLabel("Remarks (optional):"), gbc);
        schedRemarksField = new JTextField(20);
        gbc.gridx = 1;
        form.add(schedRemarksField, gbc);

        JButton addScheduleButton = new JButton("Add Schedule Entry");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        form.add(addScheduleButton, gbc);

        panel.add(form, BorderLayout.NORTH);

        scheduleModel = new DefaultTableModel(new Object[]{"Hall", "Type", "Start", "End", "Remarks"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        scheduleTable = new JTable(scheduleModel);
        panel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        scheduleHallCombo.addActionListener(e -> refreshScheduleTable());
        addScheduleButton.addActionListener(e -> addScheduleEntry());

        refreshScheduleHallCombo();
        return panel;
    }

    private void refreshScheduleHallCombo() {
        if (scheduleHallCombo == null) return;
        Hall previouslySelected = (Hall) scheduleHallCombo.getSelectedItem();
        scheduleHallCombo.removeAllItems();
        for (Hall h : fileManager.getActiveHalls()) {
            scheduleHallCombo.addItem(h);
        }
        if (previouslySelected != null) scheduleHallCombo.setSelectedItem(previouslySelected);
        refreshScheduleTable();
    }

    private void refreshScheduleTable() {
        scheduleModel.setRowCount(0);
        Hall hall = (Hall) scheduleHallCombo.getSelectedItem();
        if (hall == null) return;
        for (HallSchedule s : fileManager.getSchedulesForHall(hall.getHallId())) {
            scheduleModel.addRow(new Object[]{hall.getHallName(), s.getType(), s.getStart(), s.getEnd(), s.getRemarks()});
        }
    }

    private void addScheduleEntry() {
        Hall hall = (Hall) scheduleHallCombo.getSelectedItem();
        if (hall == null) {
            JOptionPane.showMessageDialog(this, "Please add a hall first.");
            return;
        }
        try {
            LocalDate startDate = LocalDate.parse(schedStartDateField.getText().trim(), DATE_FMT);
            LocalTime startTime = LocalTime.parse(schedStartTimeField.getText().trim(), TIME_FMT);
            LocalDate endDate = LocalDate.parse(schedEndDateField.getText().trim(), DATE_FMT);
            LocalTime endTime = LocalTime.parse(schedEndTimeField.getText().trim(), TIME_FMT);
            LocalDateTime start = LocalDateTime.of(startDate, startTime);
            LocalDateTime end = LocalDateTime.of(endDate, endTime);

            if (!end.isAfter(start)) {
                JOptionPane.showMessageDialog(this, "End date/time must be after start date/time.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            HallSchedule.Type type = (HallSchedule.Type) scheduleTypeCombo.getSelectedItem();
            String remarks = schedRemarksField.getText().trim();
            HallSchedule schedule = new HallSchedule(fileManager.nextScheduleId(), hall.getHallId(), type, start, end, remarks);
            fileManager.addSchedule(schedule);
            refreshScheduleTable();
            JOptionPane.showMessageDialog(this, type + " schedule added for " + hall.getHallName() + ".");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid dates (YYYY-MM-DD) and times (HH:mm).", "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void logout() {
        dispose();
        new LoginFrame(fileManager).setVisible(true);
    }
}
