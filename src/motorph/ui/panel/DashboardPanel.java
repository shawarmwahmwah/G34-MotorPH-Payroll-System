package motorph.ui.panel;

import motorph.ui.Theme;
import motorph.ui.session.UserSession;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * DashboardPanel
 *
 * Default landing panel after login.
 */
public class DashboardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public DashboardPanel(UserSession session) {

        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JPanel contentWrapper = new JPanel();
        contentWrapper.setLayout(new BoxLayout(contentWrapper, BoxLayout.Y_AXIS));
        contentWrapper.setBackground(Theme.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Welcome back, " + session.getEmployeeName());
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);

        JPanel welcomeCard = new JPanel();
        welcomeCard.setLayout(new BoxLayout(welcomeCard, BoxLayout.Y_AXIS));
        welcomeCard.setBackground(Theme.CARD_BACKGROUND);
        welcomeCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(22, 22, 22, 22)
        ));
        welcomeCard.setMaximumSize(new Dimension(700, 220));
        welcomeCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel cardTitle = new JLabel("<html><div style='width:620px;'><b>MotorPH Employee Payroll System</b></div></html>");
        cardTitle.setFont(Theme.FONT_HEADING);
        cardTitle.setForeground(Theme.TEXT_PRIMARY);

        JLabel cardText1 = new JLabel(
                "<html><div style='width:620px;'>You are now inside the single-frame application shell.</div></html>"
        );
        cardText1.setFont(Theme.FONT_BODY);
        cardText1.setForeground(Theme.TEXT_SECONDARY);

        JLabel cardText2 = new JLabel(
                "<html><div style='width:620px;'>Use the sidebar to move between modules without opening new windows.</div></html>"
        );
        cardText2.setFont(Theme.FONT_BODY);
        cardText2.setForeground(Theme.TEXT_SECONDARY);

        welcomeCard.add(cardTitle);
        welcomeCard.add(Box.createRigidArea(new Dimension(0, 12)));
        welcomeCard.add(cardText1);
        welcomeCard.add(Box.createRigidArea(new Dimension(0, 8)));
        welcomeCard.add(cardText2);

        contentWrapper.add(titleLabel);
        contentWrapper.add(Box.createRigidArea(new Dimension(0, 6)));
        contentWrapper.add(subtitleLabel);
        contentWrapper.add(Box.createRigidArea(new Dimension(0, 22)));
        contentWrapper.add(welcomeCard);

        add(contentWrapper, BorderLayout.NORTH);
    }
}