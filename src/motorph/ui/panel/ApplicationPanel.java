package motorph.ui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import motorph.ui.Theme;
import motorph.ui.security.RoleAccess;
import motorph.ui.session.UserSession;

public class ApplicationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public enum ViewMode {
        SELF_SERVICE("Applications", "File and review your own leave and overtime requests."),
        OVERTIME_ONLY("Overtime", "File overtime requests and review your own overtime status."),
        TEAM_REQUESTS("Team Requests", "Review requests filed by employees under your supervision."),
        APPROVALS("Approvals", "Approve or reject subordinate requests within your scope."),
        LEAVE_MONITORING("Leave Monitoring", "Monitor request activity across the organization.");

        private final String title;
        private final String subtitle;

        ViewMode(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    public ApplicationPanel(UserSession session) {
        this(session, ViewMode.SELF_SERVICE);
    }

    public ApplicationPanel(UserSession session, ViewMode viewMode) {
        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Theme.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel(viewMode.title);
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(viewMode.subtitle);
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JTextArea details = new JTextArea(buildModeMessage(session, viewMode));
        details.setEditable(false);
        details.setFont(Theme.FONT_BODY);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setBackground(Theme.CARD_BACKGROUND);
        details.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JScrollPane scrollPane = new JScrollPane(details);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(980, 520));

        wrapper.add(titleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 6)));
        wrapper.add(subtitleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 14)));
        wrapper.add(scrollPane);

        add(wrapper, BorderLayout.CENTER);
    }

    private String buildModeMessage(UserSession session, ViewMode viewMode) {
        StringBuilder text = new StringBuilder();
        text.append("Role-based applications module\n\n");
        text.append("Signed-in role: ").append(RoleAccess.getRoleLabel(session)).append("\n");
        text.append("Employee ID: ").append(session.getEmployeeId()).append("\n\n");

        switch (viewMode) {
            case SELF_SERVICE:
                text.append("This screen is reserved for employee self-service actions.\n");
                text.append("Use it for your own leave and overtime requests only.\n");
                break;
            case OVERTIME_ONLY:
                text.append("This screen is reserved for your overtime-related requests only.\n");
                break;
            case TEAM_REQUESTS:
                text.append("This screen is limited to subordinate requests resolved through supervisor matching.\n");
                text.append("Subordinate count: ").append(RoleAccess.resolveSubordinateEmployeeIds(session).size()).append("\n");
                break;
            case APPROVALS:
                text.append("Approval actions are limited to authorized approvers and subordinate scope.\n");
                text.append("Can approve requests: ").append(RoleAccess.canApproveRequests(session) ? "Yes" : "No").append("\n");
                break;
            case LEAVE_MONITORING:
                text.append("This screen is reserved for organization-wide monitoring roles.\n");
                break;
            default:
                break;
        }

        text.append("\nNavigation and data visibility are now role-based even when this module shares a common panel implementation.");
        return text.toString();
    }
}
