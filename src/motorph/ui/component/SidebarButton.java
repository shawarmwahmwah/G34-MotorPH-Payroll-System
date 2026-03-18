package motorph.ui.component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import motorph.ui.Theme;

/**
 * SidebarButton
 *
 * Reusable styled button for the left navigation sidebar.
 */
public class SidebarButton extends JButton {

    private boolean selected;

    public SidebarButton(String text) {
        super(text);

        setFont(Theme.FONT_BUTTON);
        setForeground(Theme.BUTTON_TEXT);
        setBackground(Theme.BUTTON_BACKGROUND);
        setFocusPainted(false);
        setBorderPainted(true);
        setOpaque(true);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(true);

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)
        ));

        setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        setMinimumSize(new Dimension(80, 38));
        setAlignmentX(LEFT_ALIGNMENT);
        setHorizontalAlignment(LEFT);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!selected) {
                    setBackground(Theme.SIDEBAR_BUTTON_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!selected) {
                    setBackground(Theme.BUTTON_BACKGROUND);
                }
            }
        });
    }

    /**
     * Applies selected/unselected style.
     */
    public void setSelectedStyle(boolean selected) {
        this.selected = selected;

        if (selected) {
            setBackground(Theme.SIDEBAR_BUTTON_ACTIVE);
            setForeground(Theme.PRIMARY_DARK);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(168, 187, 255), 1, true),
                    BorderFactory.createEmptyBorder(9, 12, 9, 12)
            ));
        } else {
            setBackground(Theme.BUTTON_BACKGROUND);
            setForeground(Theme.BUTTON_TEXT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
                    BorderFactory.createEmptyBorder(9, 12, 9, 12)
            ));
        }
    }
}