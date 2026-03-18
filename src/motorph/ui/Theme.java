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
    public static final Color APP_BACKGROUND = new Color(239, 241, 245);
    public static final Color SIDEBAR_BACKGROUND = new Color(244, 245, 248);
    public static final Color CONTENT_BACKGROUND = new Color(249, 250, 252);
    public static final Color CARD_BACKGROUND = new Color(255, 255, 255);

    // Accent / selected colors
    public static final Color PRIMARY = new Color(80, 114, 245);
    public static final Color PRIMARY_DARK = new Color(61, 91, 210);
    public static final Color SIDEBAR_BUTTON_HOVER = new Color(232, 236, 248);
    public static final Color SIDEBAR_BUTTON_ACTIVE = new Color(220, 229, 255);
    public static final Color HEADER_BACKGROUND = new Color(255, 255, 255);

    // Text colors
    public static final Color TEXT_PRIMARY = new Color(34, 38, 48);
    public static final Color TEXT_SECONDARY = new Color(108, 115, 128);
    public static final Color BORDER_LIGHT = new Color(224, 228, 236);

    // Button colors
    public static final Color BUTTON_BACKGROUND = new Color(245, 247, 250);
    public static final Color BUTTON_TEXT = new Color(56, 61, 70);
    public static final Color LOGOUT_BACKGROUND = new Color(255, 240, 242);
    public static final Color LOGOUT_TEXT = new Color(184, 64, 74);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private Theme() {
        // Prevent instantiation of utility class
    }
}