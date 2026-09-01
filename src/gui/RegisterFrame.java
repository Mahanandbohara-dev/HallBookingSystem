package gui;

import io.FileManager;
import model.Customer;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {

    private final FileManager fileManager;
    private final LoginFrame loginFrame;

    private JTextField usernameField, fullNameField, emailField, phoneField;
    private JPasswordField passwordField, confirmPasswordField;

    public RegisterFrame(FileManager fileManager, LoginFrame loginFrame) {
        this.fileManager = fileManager;
        this.loginFrame = loginFrame;
        buildUI();
    }

    private void buildUI() {
        setTitle("Customer Registration");
        setSize(400, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        fullNameField = addField(panel, gbc, "Full Name:");
        usernameField = addField(panel, gbc, "Username:");
        emailField = addField(panel, gbc, "Email:");
        phoneField = addField(panel, gbc, "Phone Number:");

        gbc.gridy++; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        panel.add(new JLabel("Confirm Password:"), gbc);
        confirmPasswordField = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(confirmPasswordField, gbc);

        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back to Login");
        gbc.gridy++; gbc.gridx = 0;
        panel.add(backButton, gbc);
        gbc.gridx = 1;
        panel.add(registerButton, gbc);

        add(panel);

        registerButton.addActionListener(e -> register());
        backButton.addActionListener(e -> {
            loginFrame.setVisible(true);
            dispose();
        });
    }

    private JTextField addField(JPanel panel, GridBagConstraints gbc, String label) {
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        JTextField field = new JTextField(15);
        gbc.gridx = 1;
        panel.add(field, gbc);
        gbc.gridy++;
        return field;
    }

    private void register() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        // ---- input validation (avoids logical errors, per general requirements) ----
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!phone.matches("^[0-9\\-+ ]{7,15}$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid phone number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (fileManager.findUserByUsername(username) != null) {
            JOptionPane.showMessageDialog(this, "Username already taken. Please choose another.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Customer customer = new Customer(fileManager.nextUserId("U"), username, password, fullName, email, phone);
        fileManager.addUser(customer);
        JOptionPane.showMessageDialog(this, "Registration successful! You may now log in.");
        loginFrame.setVisible(true);
        dispose();
    }
}
