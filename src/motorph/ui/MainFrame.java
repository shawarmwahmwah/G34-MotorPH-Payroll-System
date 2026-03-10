package motorph.ui;

import motorph.ui.component.SidebarButton;
import motorph.ui.component.UserProfilePanel;
import motorph.ui.panel.AttendancePanel;
import motorph.ui.panel.DashboardPanel;
import motorph.ui.panel.EmployeePanel;
import motorph.ui.panel.LeaveApprovalPanel;
import motorph.ui.panel.LeavePanel;
import motorph.ui.panel.PayrollPanel;
import motorph.ui.session.UserSession;
import motorph.ui.panel.AttendanceAdjustmentPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MainFrame
 *
 * Main application window after login.
 * Uses one frame only, with CardLayout-based content switching.
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final UserSession session;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final Map<String, SidebarButton> navButtons;

    public static final String PANEL_DASHBOARD = "DASHBOARD";
    public static final String PANEL_EMPLOYEES = "EMPLOYEES";
    public static final String PANEL_PAYROLL = "PAYROLL";
    public static final String PANEL_ATTENDANCE = "ATTENDANCE";
    public static final String PANEL_LEAVE = "LEAVE";
    public static final String PANEL_LEAVE_APPROVALS = "LEAVE_APPROVALS";
    public static final String PANEL_ATTENDANCE_ADJUSTMENTS = "ATTENDANCE_ADJUSTMENTS";

    public MainFrame(UserSession session) {
        this.session = session;
        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);
        this.navButtons = new LinkedHashMap<>();

        setTitle("MotorPH Employee Payroll System");
        setSize(1360, 820);
        setMinimumSize(new Dimension(1180, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildUI();
    }

    /**
     * Builds the full application shell.
     */
    private void buildUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.APP_BACKGROUND);

        JPanel sidebar = buildSidebar();
        buildContentPanels();

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        showPanel(PANEL_DASHBOARD);
    }

    /**
     * Builds the sidebar.
     */
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR_BACKGROUND);
        sidebar.setPreferredSize(new Dimension(280, 820));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_LIGHT));

        // Top profile
        UserProfilePanel profilePanel = new UserProfilePanel(session);
        sidebar.add(profilePanel, BorderLayout.NORTH);

        // Center navigation
        JPanel navPanel = new JPanel();
        navPanel.setBackground(Theme.SIDEBAR_BACKGROUND);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));

        SidebarButton dashboardButton = createNavButton("Dashboard", PANEL_DASHBOARD);
        SidebarButton employeesButton = createNavButton("Employees", PANEL_EMPLOYEES);
        SidebarButton payrollButton = createNavButton("Payroll", PANEL_PAYROLL);
        SidebarButton attendanceButton = createNavButton("Attendance", PANEL_ATTENDANCE);
        SidebarButton leaveButton = createNavButton("Leave", PANEL_LEAVE);
        SidebarButton approvalsButton = createNavButton("Leave Approvals", PANEL_LEAVE_APPROVALS);
        SidebarButton attendanceAdjustmentsButton = createNavButton("Attendance Adjustments", PANEL_ATTENDANCE_ADJUSTMENTS);
        
        navPanel.add(dashboardButton);
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        if (canSeeEmployeesModule()) {
            navPanel.add(employeesButton);
            navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        navPanel.add(payrollButton);
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        navPanel.add(attendanceButton);
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        navPanel.add(leaveButton);
        navPanel.add(attendanceButton); //eto yong attendance button natin
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        if (canSeeAttendanceAdjustmentModule()) {
            navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            navPanel.add(attendanceAdjustmentsButton);
        }
        
        navPanel.add(leaveButton);

        navPanel.add(Box.createVerticalGlue());

        if (canSeeLeaveApprovalModule()) {
            navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            navPanel.add(approvalsButton);
        }

        sidebar.add(navPanel, BorderLayout.CENTER);

        // Bottom logout
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Theme.SIDEBAR_BACKGROUND);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 20, 14));

        SidebarButton logoutButton = new SidebarButton("Logout");
        logoutButton.setBackground(Theme.LOGOUT_BACKGROUND);
        logoutButton.setForeground(Theme.LOGOUT_TEXT);
        logoutButton.addActionListener(e -> handleLogout());

        bottomPanel.add(logoutButton, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    /**
     * Registers all content panels.
     */
    private void buildContentPanels() {
        contentPanel.setBackground(Theme.CONTENT_BACKGROUND);

        contentPanel.add(new DashboardPanel(session), PANEL_DASHBOARD);
        contentPanel.add(new EmployeePanel(), PANEL_EMPLOYEES);
        contentPanel.add(new PayrollPanel(), PANEL_PAYROLL);
        contentPanel.add(new AttendancePanel(session), PANEL_ATTENDANCE);
        contentPanel.add(new LeavePanel(), PANEL_LEAVE);
        contentPanel.add(new LeaveApprovalPanel(), PANEL_LEAVE_APPROVALS);
        contentPanel.add(new AttendanceAdjustmentPanel(session), PANEL_ATTENDANCE_ADJUSTMENTS);
    }

    /**
     * Creates a sidebar button linked to a panel.
     */
    private SidebarButton createNavButton(String label, String panelName) {
        SidebarButton button = new SidebarButton(label);
        navButtons.put(panelName, button);
        button.addActionListener(e -> showPanel(panelName));
        return button;
    }

    /**
     * Shows a panel without opening another frame.
     */
    private void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
        updateSelectedButton(panelName);
    }

    /**
     * Updates selected button style.
     */
    private void updateSelectedButton(String selectedPanel) {
        for (Map.Entry<String, SidebarButton> entry : navButtons.entrySet()) {
            entry.getValue().setSelectedStyle(entry.getKey().equals(selectedPanel));
        }
    }

    /**
     * Handles logout.
     */
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    /**
     * Role visibility for Employees module.
     */
    private boolean canSeeEmployeesModule() {
        String role = safeLower(session.getRole());
        return role.contains("admin")
                || role.contains("hr")
                || role.contains("it")
                || role.contains("supervisor");
    }

    /**
     * Role visibility for Leave Approvals module.
     */
    private boolean canSeeLeaveApprovalModule() {
        String role = safeLower(session.getRole());
        return role.contains("admin")
                || role.contains("hr")
                || role.contains("supervisor");
    }
    private boolean canSeeAttendanceAdjustmentModule() {
        String role = safeLower(session.getRole());
        return role.contains("hr")
                || role.contains("admin")
                || role.contains("supervisor");
    }

    /**
     * Null-safe lowercase helper.
     */
    private String safeLower(String text) {
        return text == null ? "" : text.toLowerCase();
    }
}