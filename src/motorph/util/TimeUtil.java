package motorph.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {

    private TimeUtil() {}

    // Pattern supports "H:mm" and "HH:mm"
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");

    public static LocalTime parseTime(String raw) {
        if (raw == null) return null;

        String cleaned = raw.trim();
        if (cleaned.isEmpty()) return null;

        try {
            return LocalTime.parse(cleaned, TIME_FMT);
        } catch (Exception e) {
            return null;
        }
    }
}