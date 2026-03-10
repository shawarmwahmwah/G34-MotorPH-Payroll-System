package motorph.model;

/**
 * AttendanceAdjustmentRequest
 *
 * Stores one attendance adjustment request.
 * HR submits the request.
 * Admin can approve or reject it.
 * Supervisor can view it.
 */
public class AttendanceAdjustmentRequest {

    private final String requestId;
    private final String employeeId;
    private final String employeeName;
    private final String date;

    private final String currentTimeIn;
    private final String currentTimeOut;

    private final String proposedTimeIn;
    private final String proposedTimeOut;

    private final String reason;

    private final String requestedById;
    private final String requestedByName;

    private String status;
    private String approverId;
    private String approverRemarks;
    private final String requestedAt;
    private String decidedAt;

    public AttendanceAdjustmentRequest(
            String requestId,
            String employeeId,
            String employeeName,
            String date,
            String currentTimeIn,
            String currentTimeOut,
            String proposedTimeIn,
            String proposedTimeOut,
            String reason,
            String requestedById,
            String requestedByName,
            String status,
            String approverId,
            String approverRemarks,
            String requestedAt,
            String decidedAt
    ) {
        this.requestId = requestId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.date = date;
        this.currentTimeIn = currentTimeIn;
        this.currentTimeOut = currentTimeOut;
        this.proposedTimeIn = proposedTimeIn;
        this.proposedTimeOut = proposedTimeOut;
        this.reason = reason;
        this.requestedById = requestedById;
        this.requestedByName = requestedByName;
        this.status = status;
        this.approverId = approverId;
        this.approverRemarks = approverRemarks;
        this.requestedAt = requestedAt;
        this.decidedAt = decidedAt;
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

    public String getCurrentTimeIn() {
        return currentTimeIn;
    }

    public String getCurrentTimeOut() {
        return currentTimeOut;
    }

    public String getProposedTimeIn() {
        return proposedTimeIn;
    }

    public String getProposedTimeOut() {
        return proposedTimeOut;
    }

    public String getReason() {
        return reason;
    }

    public String getRequestedById() {
        return requestedById;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public String getStatus() {
        return status;
    }

    public String getApproverId() {
        return approverId;
    }

    public String getApproverRemarks() {
        return approverRemarks;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public String getDecidedAt() {
        return decidedAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public void setApproverRemarks(String approverRemarks) {
        this.approverRemarks = approverRemarks;
    }

    public void setDecidedAt(String decidedAt) {
        this.decidedAt = decidedAt;
    }
}