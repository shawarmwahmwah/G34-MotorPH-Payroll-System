package motorph.ui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import motorph.ui.Theme;
import motorph.ui.security.RoleAccess;
import motorph.ui.session.UserSession;

/**
 * UserProfilePanel
 *
 * Displays the logged-in user's avatar, name, position, and role
 * in the top-left section of the sidebar.
 */
public class UserProfilePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public UserProfilePanel(UserSession session) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBackground(new Color(251, 252, 255));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        String initials = buildInitials(session.getEmployeeName());
        JLabel avatarLabel = new JLabel(initials, SwingConstants.CENTER);
        avatarLabel.setFont(Theme.FONT_BUTTON.deriveFont(14f));
        avatarLabel.setForeground(Theme.PRIMARY_DARK);
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(new Color(232, 238, 255));
        avatarLabel.setBorder(BorderFactory.createLineBorder(new Color(186, 204, 252), 1, true));
        avatarLabel.setPreferredSize(new Dimension(46, 46));
        avatarLabel.setMaximumSize(new Dimension(46, 46));
        avatarLabel.setMinimumSize(new Dimension(46, 46));
        avatarLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userNameLabel = new JLabel(escape(session.getEmployeeName()));
        userNameLabel.setFont(Theme.FONT_BUTTON.deriveFont(14f));
        userNameLabel.setForeground(Theme.TEXT_PRIMARY);
        userNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel("Role: " + RoleAccess.getRoleLabel(session));
        roleLabel.setFont(Theme.FONT_SMALL);
        roleLabel.setForeground(Theme.TEXT_SECONDARY);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel positionLabel = new JLabel("Position: " + escape(session.getPosition()));
        positionLabel.setFont(Theme.FONT_SMALL);
        positionLabel.setForeground(Theme.TEXT_SECONDARY);
        positionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel employeeIdLabel = new JLabel("Employee ID: " + escape(session.getEmployeeId()));
        employeeIdLabel.setFont(Theme.FONT_SMALL);
        employeeIdLabel.setForeground(Theme.TEXT_SECONDARY);
        employeeIdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(avatarLabel);
        add(Box.createRigidArea(new Dimension(0, 9)));
        add(userNameLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(roleLabel);
        add(Box.createRigidArea(new Dimension(0, 2)));
        add(positionLabel);
        add(Box.createRigidArea(new Dimension(0, 2)));
        add(employeeIdLabel);
    }

    private String buildInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "EM";
        }

        String[] parts = fullName.trim().split("\\s+");
        String first = parts[0].substring(0, 1).toUpperCase(Locale.ENGLISH);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1).toUpperCase(Locale.ENGLISH) : "M";
        return first + last;
    }

    private String escape(String text) {
        return text == null ? "" : text;
    }
}
