package motorph.ui.component;

import motorph.ui.Theme;
import motorph.ui.session.UserSession;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

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
        setBackground(Theme.SIDEBAR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(22, 18, 18, 18));
        setPreferredSize(new Dimension(280, 190));

        // Avatar placeholder
        JLabel avatarLabel = new JLabel("👤", SwingConstants.CENTER);
        avatarLabel.setFont(new Font("SansSerif", Font.PLAIN, 40));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appNameLabel = new JLabel("MotorPH");
        appNameLabel.setFont(Theme.FONT_HEADING);
        appNameLabel.setForeground(Theme.TEXT_PRIMARY);
        appNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Use HTML so text can wrap instead of getting cut off
        JLabel userNameLabel = new JLabel(toCenteredHtml(session.getEmployeeName(), true));
        userNameLabel.setFont(Theme.FONT_BODY);
        userNameLabel.setForeground(Theme.TEXT_PRIMARY);
        userNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel positionLabel = new JLabel(toCenteredHtml(session.getPosition(), false));
        positionLabel.setFont(Theme.FONT_SMALL);
        positionLabel.setForeground(Theme.TEXT_SECONDARY);
        positionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("Role: " + formatRole(session.getRole()));
        roleLabel.setFont(Theme.FONT_SMALL);
        roleLabel.setForeground(new Color(90, 90, 90));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(avatarLabel);
        add(Box.createRigidArea(new Dimension(0, 8)));
        add(appNameLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(userNameLabel);
        add(Box.createRigidArea(new Dimension(0, 6)));
        add(positionLabel);
        add(Box.createRigidArea(new Dimension(0, 6)));
        add(roleLabel);
    }

    /**
     * Converts plain text to centered HTML so it wraps properly.
     */
    private String toCenteredHtml(String text, boolean bold) {
        if (text == null) {
            text = "";
        }

        String safeText = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        if (bold) {
            return "<html><div style='text-align:center; width:180px;'><b>" + safeText + "</b></div></html>";
        }

        return "<html><div style='text-align:center; width:180px;'>" + safeText + "</div></html>";
    }

    private String formatRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return "";
        }

        String value = role.trim().toLowerCase();

        if (value.equals("hr")) {
            return "HR";
        }

        if (value.equals("it")) {
            return "IT";
        }

        if (value.equals("admin")) {
            return "Admin";
        }

        if (value.equals("supervisor")) {
            return "Supervisor";
        }
        
        if (value.equals("finance")) {
            return "Finance";
        }

        if (value.equals("employee")) {
            return "Employee";
        }

        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}