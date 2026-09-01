package gui;

import exception.AuthenticationException;
import io.FileManager;
import model.*;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final FileManager fileManager;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame(FileManager fileManager) {
        this.fileManager = fileManager;
        buildUI();
    }

    private void buildUI() {
        setTitle("Hall Symphony Inc. - Login");
        setSize(380, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Hall Booking Management System", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register as Customer");
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 1;
        panel.add(loginButton, gbc);
        gbc.gridx = 1;
        panel.add(registerButton, gbc);

        add(panel);

        loginButton.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> {
            new RegisterFrame(fileManager, this).setVisible(true);
            setVisible(false);
        });
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        try {
            User user = authenticate(username, password);
            openDashboardFor(user);
            dispose();
        } catch (AuthenticationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Demonstrates EXCEPTION HANDLING: invalid credentials / blocked accounts are
     *  reported through a custom checked exception rather than a boolean flag. */
    private User authenticate(String username, String password) throws AuthenticationException {
        if (username.isEmpty() || password.isEmpty()) {
            throw new AuthenticationException("Please enter both username and password.");
        }
        User user = fileManager.findUserByUsername(username);
        if (user == null || !user.checkPassword(password)) {
            throw new AuthenticationException("Invalid username or password.");
        }
        if (user.isBlocked()) {
            throw new AuthenticationException("This account has been blocked. Please contact the administrator.");
        }
        return user;
    }

    /** POLYMORPHISM: the same User reference opens a different dashboard
     *  depending on its actual runtime role. */
    private void openDashboardFor(User user) {
        switch (user.getRole()) {
            case "CUSTOMER":
                new CustomerDashboard(fileManager, (Customer) user).setVisible(true);
                break;
            case "SCHEDULER":
                new SchedulerDashboard(fileManager, (Scheduler) user).setVisible(true);
                break;
            case "ADMINISTRATOR":
                new AdminDashboard(fileManager, (Administrator) user).setVisible(true);
                break;
            case "MANAGER":
                new ManagerDashboard(fileManager, (Manager) user).setVisible(true);
                break;
        }
    }
}
