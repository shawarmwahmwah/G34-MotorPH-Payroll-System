package motorph.repository;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import motorph.model.ActivityLog;
import motorph.util.PathHelper;

public class ActivityLogRepository {

    private static final String HEADER = "timestamp,actorId,action,details";
    private static final Path FILE = PathHelper.getDataFile("activity_log.csv");

    public void log(String actorId, String action, String details) {

        try {
            boolean writeHeader = !Files.exists(FILE) || Files.size(FILE) == 0;

            ActivityLog log = new ActivityLog(
                    LocalDateTime.now(),
                    actorId,
                    action,
                    details
            );

            try (FileWriter fw = new FileWriter(FILE.toFile(), true)) {
                if (writeHeader) {
                    fw.write(HEADER + "\n");
                }

                fw.write(
                        escape(log.getTimestamp().toString()) + ","
                                + escape(log.getActorId()) + ","
                                + escape(log.getAction()) + ","
                                + escape(log.getDetails())
                                + "\n"
                );
            }

        } catch (Exception e) {
            System.out.println("[ActivityLogRepository] log write failed: " + e.getMessage());
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }
}