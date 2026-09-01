import gui.LoginFrame;
import io.FileManager;

import javax.swing.*;

/**
 * Hall Booking Management System - Hall Symphony Inc.
 * Entry point: initializes the text-file backed data store and shows the login screen.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }

            FileManager fileManager = new FileManager();
            new LoginFrame(fileManager).setVisible(true);
        });
    }
}
