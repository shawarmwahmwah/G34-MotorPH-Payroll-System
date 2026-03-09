package motorph.ui.panel;

import motorph.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * ModulePlaceholderPanel
 *
 * Reusable temporary panel for modules that are not yet fully connected
 * to backend logic.
 */
public class ModulePlaceholderPanel extends JPanel {

    public ModulePlaceholderPanel(String title, String subtitle) {

        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Theme.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel line1 = new JLabel("This screen is part of the single-frame GUI structure.");
        line1.setFont(Theme.FONT_BODY);
        line1.setForeground(Theme.TEXT_PRIMARY);

        JLabel line2 = new JLabel("Backend integration for this module will be connected in the next steps.");
        line2.setFont(Theme.FONT_BODY);
        line2.setForeground(Theme.TEXT_SECONDARY);

        card.add(line1);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(line2);

        wrapper.add(titleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 4)));
        wrapper.add(subtitleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 20)));
        wrapper.add(card);

        add(wrapper, BorderLayout.NORTH);
    }
}