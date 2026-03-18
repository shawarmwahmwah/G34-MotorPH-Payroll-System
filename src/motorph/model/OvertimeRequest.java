package motorph.model;

public class OvertimeRequest {

    private final String requestId;
    private final String employeeId;
    private final String employeeName;
    private final String date;
    private final String timeRange;
    private final double hoursRequested;
    private final String reason;
    private final String status;
    private final String requestedAt;
    private final String approverId;
    private final String approvedAt;
    private final String remarks;

    public OvertimeRequest(
            String requestId,
            String employeeId,
            String employeeName,
            String date,
            String timeRange,
            double hoursRequested,
            String reason,
            String status,
            String requestedAt,
            String approverId,
            String approvedAt,
            String remarks
    ) {
        this.requestId = requestId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.date = date;
        this.timeRange = timeRange;
        this.hoursRequested = hoursRequested;
        this.reason = reason;
        this.status = status;
        this.requestedAt = requestedAt;
        this.approverId = approverId;
        this.approvedAt = approvedAt;
        this.remarks = remarks;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getDate() {
        return date;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public double getHoursRequested() {
        return hoursRequested;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public String getApproverId() {
        return approverId;
    }

    public String getApprovedAt() {
        return approvedAt;
    }

    public String getRemarks() {
        return remarks;
    }
}
