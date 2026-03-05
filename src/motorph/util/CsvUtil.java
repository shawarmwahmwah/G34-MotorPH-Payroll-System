package motorph.util;

import java.util.ArrayList;
import java.util.List;

public final class CsvUtil {
    private CsvUtil() {}

    /**
     * Simple CSV split that supports quoted values.
     * Good enough for your current files (employees.csv has quotes).
     */
    public static List<String> splitCsvLine(String line) {
        List<String> out = new ArrayList<>();
        if (line == null) return out;

        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                out.add(cur.toString().trim().replaceAll("^\"|\"$", ""));
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }

        out.add(cur.toString().trim().replaceAll("^\"|\"$", ""));
        return out;
    }
}