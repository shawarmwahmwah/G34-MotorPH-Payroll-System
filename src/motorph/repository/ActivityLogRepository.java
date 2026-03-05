package motorph.repository;

import java.io.FileWriter;
import java.time.LocalDateTime;

import motorph.model.ActivityLog;

public class ActivityLogRepository {

    private static final String FILE = "data/activity_log.csv";

    public void log(String actorId, String action, String details) {

        try {

            FileWriter fw = new FileWriter(FILE, true);

            ActivityLog log = new ActivityLog(
                    LocalDateTime.now(),
                    actorId,
                    action,
                    details
            );

            fw.write(
                    log.getTimestamp() + "," +
                    log.getActorId() + "," +
                    log.getAction() + "," +
                    log.getDetails() + "\n"
            );

            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}