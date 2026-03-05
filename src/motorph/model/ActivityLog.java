package motorph.model;

import java.time.LocalDateTime;

public class ActivityLog {

    private LocalDateTime timestamp;
    private String actorId;
    private String action;
    private String details;

    public ActivityLog(LocalDateTime timestamp, String actorId, String action, String details) {
        this.timestamp = timestamp;
        this.actorId = actorId;
        this.action = action;
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getActorId() {
        return actorId;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }
}