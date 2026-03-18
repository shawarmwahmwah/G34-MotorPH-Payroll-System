package motorph.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import motorph.ui.component.SidebarButton;
import motorph.ui.component.UserProfilePanel;
import motorph.ui.panel.ApplicationPanel;
import motorph.ui.panel.AttendancePanel;
import motorph.ui.panel.DashboardPanel;
import motorph.ui.panel.EmployeePanel;
import motorph.ui.panel.ModulePlaceholderPanel;
import motorph.ui.panel.PayrollConfigurationPanel;
import motorph.ui.panel.PayrollPanel;
import motorph.ui.panel.SystemLogsPanel;
import motorph.ui.security.RoleAccess;
import motorph.ui.security.RoleAccess.AppRole;
import motorph.ui.session.UserSession;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String PANEL_PROFILE = "PROFILE";
    private static final String PANEL_ATTENDANCE = "ATTENDANCE";
    private static final String PANEL_TEAM_ATTENDANCE = "TEAM_ATTENDANCE";
    private static final String PANEL_ATTENDANCE_MONITORING = "ATTENDANCE_MONITORING";
    private static final String PANEL_PAYSLIP = "PAYSLIP";
    private static final String PANEL_APPLICATIONS = "APPLICATIONS";
    private static final String PANEL_OVERTIME = "OVERTIME";
    private static final String PANEL_TEAM_REQUESTS = "TEAM_REQUESTS";
    private static final String PANEL_APPROVALS = "APPROVALS";
    private static final String PANEL_MANAGE_EMPLOYEES = "MANAGE_EMPLOYEES";
    private static final String PANEL_EMPLOYEE_RECORDS = "EMPLOYEE_RECORDS";
    private static final String PANEL_LEAVE_MONITORING = "LEAVE_MONITORING";
    private static final String PANEL_MANAGE_PAYROLL = "MANAGE_PAYROLL";
    private static final String PANEL_PAYROLL_HISTORY = "PAYROLL_HISTORY";
    private static final String PANEL_EMPLOYEE_PAYROLL_VIEW = "EMPLOYEE_PAYROLL_VIEW";
    private static final String PANEL_PAYROLL_CONFIG = "PAYROLL_CONFIG";
    private static final String PANEL_SYSTEM_LOGS = "SYSTEM_LOGS";
    private static final String PANEL_USER_ACCESS = "USER_ACCESS";

    private final UserSession session;
    private final AppRole appRole;
    private final java.awt.CardLayout cardLayout;
    private final JPanel contentPanel;
    private final Map<String, SidebarButton> navButtons;
    private final Map<String, String> panelTitles;
    private final JLabel pageTitleLabel;
    private String defaultPanel;

    public MainFrame(UserSession session) {
        this.session = session;
        this.appRole = RoleAccess.resolveRole(session);
        this.cardLayout = new java.awt.CardLayout();
        this.contentPanel = new JPanel(cardLayout);
        this.navButtons = new LinkedHashMap<>();
        this.panelTitles = new LinkedHashMap<>();
        this.pageTitleLabel = new JLabel("Dashboard");

        setTitle("MotorPH Payroll System");
        setSize(1360, 820);
        setMinimumSize(new Dimension(1180, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.APP_BACKGROUND);

        initializePanelTitles();
        buildContentPanels();

        add(buildSidebar(), BorderLayout.WEST);
        add(buildRightContent(), BorderLayout.CENTER);

        showPanel(defaultPanel);
    }

    private void initializePanelTitles() {
        panelTitles.put(PANEL_PROFILE, "Profile");
        panelTitles.put(PANEL_ATTENDANCE, "Attendance");
        panelTitles.put(PANEL_TEAM_ATTENDANCE, "Team Attendance");
        panelTitles.put(PANEL_ATTENDANCE_MONITORING, "Attendance Monitoring");
        panelTitles.put(PANEL_PAYSLIP, "Payslip");
        panelTitles.put(PANEL_APPLICATIONS, "Applications");
        panelTitles.put(PANEL_OVERTIME, "Overtime");
        panelTitles.put(PANEL_TEAM_REQUESTS, "Team Requests");
        panelTitles.put(PANEL_APPROVALS, "Approvals");
        panelTitles.put(PANEL_MANAGE_EMPLOYEES, "Manage Employees");
        panelTitles.put(PANEL_EMPLOYEE_RECORDS, "Employee Records");
        panelTitles.put(PANEL_LEAVE_MONITORING, "Leave Monitoring");
        panelTitles.put(PANEL_MANAGE_PAYROLL, "Manage Payroll");
        panelTitles.put(PANEL_PAYROLL_HISTORY, "Payroll History");
        panelTitles.put(PANEL_EMPLOYEE_PAYROLL_VIEW, "Employee Payroll View");
        panelTitles.put(PANEL_PAYROLL_CONFIG, "Payroll Configuration");
        panelTitles.put(PANEL_SYSTEM_LOGS, "System Logs");
        panelTitles.put(PANEL_USER_ACCESS, "User Access Control");
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR_BACKGROUND);
        sidebar.setPreferredSize(new Dimension(292, 820));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_LIGHT));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(Theme.SIDEBAR_BACKGROUND);
        topPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

        JPanel appBadgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        appBadgePanel.setOpaque(false);
        appBadgePanel.add(createDot(new Color(255, 95, 87)));
        appBadgePanel.add(createDot(new Color(255, 189, 46)));
        appBadgePanel.add(createDot(new Color(40, 201, 64)));
        appBadgePanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel systemName = new JLabel("MotorPH Payroll");
        systemName.setFont(Theme.FONT_HEADING);
        systemName.setForeground(Theme.TEXT_PRIMARY);
        systemName.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel systemTagline = new JLabel(RoleAccess.getRoleLabel(session));
        systemTagline.setFont(Theme.FONT_SMALL);
        systemTagline.setForeground(Theme.TEXT_SECONDARY);
        systemTagline.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        UserProfilePanel profilePanel = new UserProfilePanel(session);
        profilePanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        topPanel.add(appBadgePanel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        topPanel.add(systemName);
        topPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        topPanel.add(systemTagline);
        topPanel.add(Box.createRigidArea(new Dimension(0, 9)));
        topPanel.add(profilePanel);
        sidebar.add(topPanel, BorderLayout.NORTH);

        JPanel navPanel = new JPanel();
        navPanel.setBackground(Theme.SIDEBAR_BACKGROUND);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(4, 12, 0, 12));

        JLabel navTitle = new JLabel("NAVIGATION");
        navTitle.setFont(Theme.FONT_SMALL);
        navTitle.setForeground(Theme.TEXT_SECONDARY);
        navTitle.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        navPanel.add(navTitle);
        navPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        registerButtonsForRole(navPanel);
        navPanel.add(Box.createVerticalGlue());
        sidebar.add(navPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Theme.SIDEBAR_BACKGROUND);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 16, 12));

        SidebarButton logoutButton = new SidebarButton("Logout");
        logoutButton.setBackground(Theme.LOGOUT_BACKGROUND);
        logoutButton.setForeground(Theme.LOGOUT_TEXT);
        logoutButton.addActionListener(e -> handleLogout());
        bottomPanel.add(logoutButton, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel buildRightContent() {
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Theme.CONTENT_BACKGROUND);
        right.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER_BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        pageTitleLabel.setFont(Theme.FONT_HEADING);
        pageTitleLabel.setForeground(Theme.TEXT_PRIMARY);

        JButton refreshButton = new JButton("Refresh View");
        refreshButton.setFont(Theme.FONT_BUTTON);
        refreshButton.setBackground(Theme.BUTTON_BACKGROUND);
        refreshButton.setForeground(Theme.BUTTON_TEXT);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> refreshCurrentPanel());

        header.add(pageTitleLabel, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        body.add(contentPanel, BorderLayout.CENTER);

        right.add(header, BorderLayout.NORTH);
        right.add(body, BorderLayout.CENTER);
        return right;
    }

    private void registerButtonsForRole(JPanel navPanel) {
        addNavButton(navPanel, "Profile", PANEL_PROFILE);
        addNavButton(navPanel, "Attendance", PANEL_ATTENDANCE);
        addNavButton(navPanel, "Payslip", PANEL_PAYSLIP);

        switch (appRole) {
            case EMPLOYEE:
                addNavButton(navPanel, "Applications", PANEL_APPLICATIONS);
                addNavButton(navPanel, "Overtime", PANEL_OVERTIME);
                break;
            case SUPERVISOR:
                addNavButton(navPanel, "Applications", PANEL_APPLICATIONS);
                addNavButton(navPanel, "Team Attendance", PANEL_TEAM_ATTENDANCE);
                addNavButton(navPanel, "Team Requests", PANEL_TEAM_REQUESTS);
                addNavButton(navPanel, "Approvals", PANEL_APPROVALS);
                break;
            case HR_STAFF:
                addNavButton(navPanel, "Applications", PANEL_APPLICATIONS);
                addNavButton(navPanel, "Overtime", PANEL_OVERTIME);
                addNavButton(navPanel, "Manage Employees", PANEL_MANAGE_EMPLOYEES);
                addNavButton(navPanel, "Employee Records", PANEL_EMPLOYEE_RECORDS);
                addNavButton(navPanel, "Leave Monitoring", PANEL_LEAVE_MONITORING);
                addNavButton(navPanel, "Attendance Monitoring", PANEL_ATTENDANCE_MONITORING);
                break;
            case PAYROLL_ADMIN:
                addNavButton(navPanel, "Applications", PANEL_APPLICATIONS);
                addNavButton(navPanel, "Manage Payroll", PANEL_MANAGE_PAYROLL);
                addNavButton(navPanel, "Payroll History", PANEL_PAYROLL_HISTORY);
                addNavButton(navPanel, "Employee Payroll View", PANEL_EMPLOYEE_PAYROLL_VIEW);
                addNavButton(navPanel, "Payroll Configuration", PANEL_PAYROLL_CONFIG);
                break;
            case SYSTEM_ADMIN:
                addNavButton(navPanel, "Applications", PANEL_APPLICATIONS);
                addNavButton(navPanel, "Overtime", PANEL_OVERTIME);
                addNavButton(navPanel, "Manage Employees", PANEL_MANAGE_EMPLOYEES);
                addNavButton(navPanel, "Manage Payroll", PANEL_MANAGE_PAYROLL);
                addNavButton(navPanel, "Payroll Configuration", PANEL_PAYROLL_CONFIG);
                addNavButton(navPanel, "System Logs", PANEL_SYSTEM_LOGS);
                addNavButton(navPanel, "User Access Control", PANEL_USER_ACCESS);
                break;
            default:
                break;
        }
    }

    private void buildContentPanels() {
        addModulePanel(PANEL_PROFILE, new DashboardPanel(session));
        addModulePanel(PANEL_ATTENDANCE, new AttendancePanel(session, AttendancePanel.ViewScope.SELF));
        addModulePanel(PANEL_TEAM_ATTENDANCE, new AttendancePanel(session, AttendancePanel.ViewScope.TEAM));
        addModulePanel(PANEL_ATTENDANCE_MONITORING, new AttendancePanel(session, AttendancePanel.ViewScope.ALL));
        addModulePanel(PANEL_PAYSLIP, new PayrollPanel(session, PayrollPanel.ViewMode.SELF_SERVICE));
        addModulePanel(PANEL_APPLICATIONS, new ApplicationPanel(session, ApplicationPanel.ViewMode.SELF_SERVICE));
        addModulePanel(PANEL_OVERTIME, new ApplicationPanel(session, ApplicationPanel.ViewMode.OVERTIME_ONLY));
        addModulePanel(PANEL_TEAM_REQUESTS, new ApplicationPanel(session, ApplicationPanel.ViewMode.TEAM_REQUESTS));
        addModulePanel(PANEL_APPROVALS, new ApplicationPanel(session, ApplicationPanel.ViewMode.APPROVALS));
        addModulePanel(PANEL_MANAGE_EMPLOYEES, new EmployeePanel(session));
        addModulePanel(PANEL_EMPLOYEE_RECORDS, new EmployeePanel(session));
        addModulePanel(PANEL_LEAVE_MONITORING, new ApplicationPanel(session, ApplicationPanel.ViewMode.LEAVE_MONITORING));
        addModulePanel(PANEL_MANAGE_PAYROLL, new PayrollPanel(session, PayrollPanel.ViewMode.MANAGE));
        addModulePanel(PANEL_PAYROLL_HISTORY, new ModulePlaceholderPanel("Payroll History", "Review generated payroll exports and historical payroll runs."));
        addModulePanel(PANEL_EMPLOYEE_PAYROLL_VIEW, new PayrollPanel(session, PayrollPanel.ViewMode.ALL_RECORDS));
        addModulePanel(PANEL_PAYROLL_CONFIG, new PayrollConfigurationPanel(session));
        addModulePanel(PANEL_SYSTEM_LOGS, new SystemLogsPanel(session));
        addModulePanel(PANEL_USER_ACCESS, new ModulePlaceholderPanel("User Access Control", "Manage role assignments and permission policies from a central admin view."));
    }

    private void addModulePanel(String panelName, JPanel panel) {
        contentPanel.add(panel, panelName);
        if (defaultPanel == null) {
            defaultPanel = panelName;
        }
    }

    private void addNavButton(JPanel navPanel, String label, String panelName) {
        SidebarButton button = new SidebarButton(label);
        navButtons.put(panelName, button);
        button.addActionListener(e -> showPanel(panelName));
        navPanel.add(button);
        navPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private JPanel createDot(Color color) {
        JPanel dot = new JPanel();
        dot.setBackground(color);
        dot.setPreferredSize(new Dimension(10, 10));
        dot.setMaximumSize(new Dimension(10, 10));
        dot.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 35), 1, true));
        return dot;
    }

    private void showPanel(String panelName) {
        if (panelName == null || !navButtons.containsKey(panelName)) {
            return;
        }
        cardLayout.show(contentPanel, panelName);
        updateSelectedButton(panelName);
        pageTitleLabel.setText(panelTitles.getOrDefault(panelName, "Dashboard"));
    }

    private void refreshCurrentPanel() {
        java.awt.Component current = null;
        for (java.awt.Component c : contentPanel.getComponents()) {
            if (c.isVisible()) {
                current = c;
                break;
            }
        }
        if (current != null) {
            current.revalidate();
            current.repaint();
        }
    }

    private void updateSelectedButton(String selectedPanel) {
        for (Map.Entry<String, SidebarButton> entry : navButtons.entrySet()) {
            entry.getValue().setSelectedStyle(entry.getKey().equals(selectedPanel));
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}
