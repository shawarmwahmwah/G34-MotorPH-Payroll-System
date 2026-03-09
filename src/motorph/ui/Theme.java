package motorph.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * Theme
 *
 * Central place for UI colors, fonts, and common styling values.
 * This helps keep the GUI consistent and easier to update later.
 */
public class Theme {

    // Main background colors
    public static final Color APP_BACKGROUND = new Color(245, 246, 248);
    public static final Color SIDEBAR_BACKGROUND = new Color(255, 255, 255);
    public static final Color CONTENT_BACKGROUND = new Color(245, 246, 248);
    public static final Color CARD_BACKGROUND = new Color(255, 255, 255);

    // Accent / selected colors
    public static final Color PRIMARY = new Color(79, 110, 247);
    public static final Color PRIMARY_DARK = new Color(58, 86, 210);

    // Text colors
    public static final Color TEXT_PRIMARY = new Color(30, 30, 30);
    public static final Color TEXT_SECONDARY = new Color(110, 110, 110);
    public static final Color BORDER_LIGHT = new Color(230, 232, 236);

    // Button colors
    public static final Color BUTTON_BACKGROUND = new Color(248, 249, 251);
    public static final Color BUTTON_TEXT = new Color(45, 45, 45);
    public static final Color LOGOUT_BACKGROUND = new Color(255, 241, 241);
    public static final Color LOGOUT_TEXT = new Color(180, 50, 50);

    // Fonts
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);

    private Theme() {
        // Prevent instantiation of utility class
    }
}