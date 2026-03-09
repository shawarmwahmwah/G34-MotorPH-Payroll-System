package motorph.ui;

import javax.swing.SwingUtilities;

/**
 * AppLauncher
 *
 * Starts the GUI version of the MotorPH Payroll System.
 */
public class AppLauncher {

    public static void main(String[] args) {

        // Launch Swing UI safely on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}