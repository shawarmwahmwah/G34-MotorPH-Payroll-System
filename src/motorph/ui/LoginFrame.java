package motorph.ui;

import motorph.model.Employee;
import motorph.model.UserAccount;
import motorph.repository.CsvEmployeeRepository;
import motorph.repository.CsvUserRepository;
import motorph.service.AuthService;
import motorph.ui.session.UserSession;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

/**
 * LoginFrame
 *
 * Handles authentication before opening the main application window.
 */
public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField usernameField;
    private JPasswordField passwordField;

    // AuthService depends on CsvUserRepository
    private final AuthService authService = new AuthService(new CsvUserRepository());

    // Repository for loading employee details after successful login
    private final CsvEmployeeRepository employeeRepository = new CsvEmployeeRepository();

    public LoginFrame() {
        setTitle("MotorPH Payroll System - Login");
        setSize(600,470);
        setMinimumSize(new Dimension(600,470));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildUI();

        // Focus the username field after the frame is ready
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }

    /**
     * Builds the login user interface.
     */
    private void buildUI() {

        // Overall frame layout
        getContentPane().setBackground(Theme.APP_BACKGROUND);
        setLayout(new BorderLayout());

        // Outer panel gives proper spacing around the login card
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBackground(Theme.APP_BACKGROUND);
        outerPanel.setBorder(BorderFactory.createEmptyBorder(40, 55, 40, 55));

        // Card panel
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(Theme.CARD_BACKGROUND);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(28, 28, 28, 28)
        ));

        // Fix the card width/height so it looks balanced
        cardPanel.setPreferredSize(new Dimension(430, 300));

        // Title
        JLabel titleLabel = new JLabel("MotorPH");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Employee Payroll System Login");
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Form wrapper
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBackground(Theme.CARD_BACKGROUND);
        formWrapper.setAlignmentX(LEFT_ALIGNMENT);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        formPanel.setBackground(Theme.CARD_BACKGROUND);

        // Username label
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(Theme.FONT_BODY);
        usernameLabel.setForeground(Theme.TEXT_PRIMARY);

        // Username field
        usernameField = new JTextField();
        usernameField.setFont(Theme.FONT_BODY);
        usernameField.setPreferredSize(new Dimension(360, 40));

        // Password label
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(Theme.FONT_BODY);
        passwordLabel.setForeground(Theme.TEXT_PRIMARY);

        // Password field
        passwordField = new JPasswordField();
        passwordField.setFont(Theme.FONT_BODY);
        passwordField.setPreferredSize(new Dimension(360, 40));

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(Theme.FONT_BUTTON);
        loginButton.setBackground(Theme.PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        loginButton.setPreferredSize(new Dimension(360, 42));

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(loginButton);

        formWrapper.add(formPanel, BorderLayout.CENTER);

        // Build card layout
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        cardPanel.add(subtitleLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 26)));
        cardPanel.add(formWrapper);

        outerPanel.add(cardPanel, BorderLayout.CENTER);
        add(outerPanel, BorderLayout.CENTER);

        // Mouse click triggers login
        loginButton.addActionListener(e -> handleLogin());

        // Enter key also triggers login
        getRootPane().setDefaultButton(loginButton);
        passwordField.addActionListener(e -> handleLogin());
    }

    /**
     * Attempts login using AuthService and opens MainFrame if successful.
     */
    private void handleLogin() {

        // Read entered values
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Authenticate through service layer
        UserAccount account = authService.login(username, password);

        // If login failed, show proper message
        if (account == null) {

            String serviceMessage = authService.getLastLoginMessage();

            if (serviceMessage == null || serviceMessage.trim().isEmpty()) {
                serviceMessage = "Invalid username or password, please try again.";
            }

            String popupMessage;
            if (serviceMessage.toLowerCase().contains("locked")) {
                popupMessage = "Account is locked. Please contact your IT/Admin.";
            } else {
                popupMessage = "Invalid username or password, please try again.";
            }

            JOptionPane.showMessageDialog(
                    this,
                    popupMessage,
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Load employee linked to account
        Employee employee = employeeRepository.findById(account.getEmployeeId());

        if (employee == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Employee record not found for ID: " + account.getEmployeeId(),
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Build session
        UserSession session = new UserSession(account, employee);

        // Open the main frame
        MainFrame mainFrame = new MainFrame(session);
        mainFrame.setVisible(true);

        // Close login frame
        dispose();
    }
}