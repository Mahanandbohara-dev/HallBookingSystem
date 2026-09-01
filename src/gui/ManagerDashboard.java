package gui;

import io.FileManager;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.LinkedHashMap;
import java.util.Map;

public class ManagerDashboard extends JFrame {

    private final FileManager fileManager;
    private final Manager manager;

    // Sales dashboard tab
    private JComboBox<String> salesPeriodCombo;
    private DefaultTableModel salesModel;
    private JLabel totalSalesLabel;

    // Issues tab
    private DefaultTableModel issueModel;
    private JTable issueTable;

    public ManagerDashboard(FileManager fileManager, Manager manager) {
        this.fileManager = fileManager;
        this.manager = manager;
        fileManager.addDataChangeListener(dataType -> {
            if ("issues".equals(dataType)) {
                SwingUtilities.invokeLater(this::refreshIssueTable);
            }
        });
        buildUI();
    }

    private void buildUI() {
        setTitle("Manager Dashboard - " + manager.getFullName());
        setSize(820, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Sales Dashboard", buildSalesPanel());
        tabs.addTab("Maintenance / Issue Management", buildIssuePanel());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(logoutButton);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    // -----------------------------------------------------------------
    // SALES DASHBOARD (weekly / monthly / yearly, view + filter)
    // -----------------------------------------------------------------

    private JPanel buildSalesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Group sales by:"));
        salesPeriodCombo = new JComboBox<>(new String[]{"Weekly", "Monthly", "Yearly"});
        top.add(salesPeriodCombo);
        totalSalesLabel = new JLabel();
        panel.add(top, BorderLayout.NORTH);

        salesModel = new DefaultTableModel(new Object[]{"Period", "Number of Bookings", "Total Sales (RM)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable salesTable = new JTable(salesModel);
        panel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        panel.add(totalSalesLabel, BorderLayout.SOUTH);

        salesPeriodCombo.addActionListener(e -> refreshSales());
        refreshSales();
        return panel;
    }

    private void refreshSales() {
        salesModel.setRowCount(0);
        String period = (String) salesPeriodCombo.getSelectedItem();
        // Only confirmed bookings count as realised sales; cancelled bookings are excluded.
        Map<String, double[]> grouped = new LinkedHashMap<>(); // key -> [count, totalCost]
        double grandTotal = 0;

        for (Booking b : fileManager.getAllBookings()) {
            if (b.getStatus() != Booking.Status.CONFIRMED) continue;
            LocalDate date = b.getStartDateTime().toLocalDate();
            String key;
            switch (period == null ? "Monthly" : period) {
                case "Weekly":
                    key = date.get(IsoFields.WEEK_BASED_YEAR) + "-W" + String.format("%02d", date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
                    break;
                case "Yearly":
                    key = String.valueOf(date.getYear());
                    break;
                default:
                    key = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            }
            double[] agg = grouped.computeIfAbsent(key, k -> new double[2]);
            agg[0] += 1;
            agg[1] += b.getTotalCost();
            grandTotal += b.getTotalCost();
        }

        for (Map.Entry<String, double[]> entry : grouped.entrySet()) {
            salesModel.addRow(new Object[]{entry.getKey(), (int) entry.getValue()[0], String.format("%.2f", entry.getValue()[1])});
        }
        totalSalesLabel.setText("  Grand Total (all confirmed bookings): RM " + String.format("%.2f", grandTotal));
    }

    // -----------------------------------------------------------------
    // ISSUE MANAGEMENT (view/respond, assign scheduler, change status)
    // -----------------------------------------------------------------

    private JPanel buildIssuePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        issueModel = new DefaultTableModel(
                new Object[]{"Issue ID", "Customer", "Booking", "Description", "Status", "Assigned Scheduler", "Manager Reply"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        issueTable = new JTable(issueModel);
        panel.add(new JScrollPane(issueTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton assignButton = new JButton("Assign Scheduler");
        JButton statusButton = new JButton("Change Status");
        JButton replyButton = new JButton("Add Manager Reply");
        bottom.add(assignButton);
        bottom.add(statusButton);
        bottom.add(replyButton);
        panel.add(bottom, BorderLayout.SOUTH);

        assignButton.addActionListener(e -> assignScheduler());
        statusButton.addActionListener(e -> changeIssueStatus());
        replyButton.addActionListener(e -> addManagerReply());

        refreshIssueTable();
        return panel;
    }

    private void refreshIssueTable() {
        issueModel.setRowCount(0);
        for (Issue issue : fileManager.getAllIssues()) {
            User customer = fileManager.findUserById(issue.getCustomerId());
            User assignedScheduler = issue.getAssignedSchedulerId() != null ? fileManager.findUserById(issue.getAssignedSchedulerId()) : null;
            issueModel.addRow(new Object[]{
                    issue.getIssueId(),
                    customer != null ? customer.getFullName() : issue.getCustomerId(),
                    issue.getBookingId(),
                    issue.getDescription(),
                    issue.getStatus(),
                    assignedScheduler != null ? assignedScheduler.getFullName() : "(unassigned)",
                    issue.getManagerReply() != null && !issue.getManagerReply().isBlank() ? issue.getManagerReply() : "(No reply yet)"
            });
        }
    }

    private void assignScheduler() {
        int row = issueTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an issue.");
            return;
        }
        String issueId = (String) issueModel.getValueAt(row, 0);
        Issue issue = findIssueById(issueId);
        if (issue == null) return;

        java.util.List<User> schedulers = fileManager.getUsersByRole("SCHEDULER");
        if (schedulers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No scheduler staff available to assign.");
            return;
        }
        JComboBox<User> combo = new JComboBox<>(schedulers.toArray(new User[0]));
        int result = JOptionPane.showConfirmDialog(this, combo, "Assign Scheduler to Fix Issue", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        User selected = (User) combo.getSelectedItem();
        issue.setAssignedSchedulerId(selected.getUserId());
        if (issue.getStatus() == Issue.Status.OPEN) {
            issue.setStatus(Issue.Status.IN_PROGRESS);
        }
        fileManager.updateIssue(issue);
        refreshIssueTable();
    }

    private void changeIssueStatus() {
        int row = issueTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an issue.");
            return;
        }
        String issueId = (String) issueModel.getValueAt(row, 0);
        Issue issue = findIssueById(issueId);
        if (issue == null) return;

        JComboBox<Issue.Status> combo = new JComboBox<>(Issue.Status.values());
        combo.setSelectedItem(issue.getStatus());
        int result = JOptionPane.showConfirmDialog(this, combo, "Change Issue Status", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        issue.setStatus((Issue.Status) combo.getSelectedItem());
        fileManager.updateIssue(issue);
        refreshIssueTable();
    }

    private void addManagerReply() {
        int row = issueTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an issue.");
            return;
        }
        String issueId = (String) issueModel.getValueAt(row, 0);
        Issue issue = findIssueById(issueId);
        if (issue == null) return;

        String currentReply = issue.getManagerReply() == null ? "" : issue.getManagerReply();
        String reply = (String) JOptionPane.showInputDialog(this, "Manager reply:", "Add Reply",
                JOptionPane.PLAIN_MESSAGE, null, null, currentReply);
        if (reply == null) return;

        issue.setManagerReply(reply.trim());
        if (issue.getStatus() == Issue.Status.OPEN) {
            issue.setStatus(Issue.Status.IN_PROGRESS);
        }
        fileManager.updateIssue(issue);
        refreshIssueTable();
    }

    private Issue findIssueById(String issueId) {
        for (Issue i : fileManager.getAllIssues()) {
            if (i.getIssueId().equals(issueId)) return i;
        }
        return null;
    }

    private void logout() {
        dispose();
        new LoginFrame(fileManager).setVisible(true);
    }
}
