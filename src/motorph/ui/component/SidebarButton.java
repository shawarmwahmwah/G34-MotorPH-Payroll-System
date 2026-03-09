package motorph.ui.component;

import motorph.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;

/**
 * SidebarButton
 *
 * Reusable styled button for the left navigation sidebar.
 */
public class SidebarButton extends JButton {

    public SidebarButton(String text) {
        super(text);

        // Basic button styling
        setFont(Theme.FONT_BUTTON);
        setForeground(Theme.BUTTON_TEXT);
        setBackground(Theme.BUTTON_BACKGROUND);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(true);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Inner padding
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Consistent size
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // Left-align the text like a dashboard sidebar
        setHorizontalAlignment(LEFT);
    }

    /**
     * Applies selected/unselected style.
     */
    public void setSelectedStyle(boolean selected) {
        if (selected) {
            setBackground(Theme.PRIMARY);
            setForeground(Color.WHITE);
        } else {
            setBackground(Theme.BUTTON_BACKGROUND);
            setForeground(Theme.BUTTON_TEXT);
        }
    }
}