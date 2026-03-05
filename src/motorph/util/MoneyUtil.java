package motorph.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtil {

    private MoneyUtil() {
    }

    public static double parseMoney2dp(String raw) {
        if (raw == null) return 0.0;

        // Remove commas, spaces, and peso sign if ever present
        String cleaned = raw.replace(",", "").replace("Php", "").replace("₱", "").trim();

        if (cleaned.isEmpty()) return 0.00;

        try {
            // Use BigDecimal for accurate rounding
            BigDecimal bd = new BigDecimal(cleaned);

            // Round to 2 decimal places
            bd = bd.setScale(2, RoundingMode.HALF_UP);

            // Convert back to double for computations
            return bd.doubleValue();
        } catch (Exception e) {
            // If parsing fails, return 0 so program does not crash
            return 0.00;
        }
    }
}