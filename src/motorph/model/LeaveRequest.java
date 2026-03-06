package motorph.model;

public class LeaveRequest {

    private final String requestId;
    private final String employeeId;
    private final String employeeName;
    private final String leaveType;
    private final String startDate;
    private final String endDate;
    private final double daysRequested;
    private final String status;       // PENDING / APPROVED / REJECTED / AUTO_REJECTED
    private final String reason;
    private final String approverId;
    private final String remarks;

    public LeaveRequest(
            String requestId,
            String employeeId,
            String employeeName,
            String leaveType,
            String startDate,
            String endDate,
            double daysRequested,
            String status,
            String reason,
            String approverId,
            String remarks
    ) {
        this.requestId = requestId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.daysRequested = daysRequested;
        this.status = status;
        this.reason = reason;
        this.approverId = approverId;
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

    public String getLeaveType() {
        return leaveType;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public double getDaysRequested() {
        return daysRequested;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getApproverId() {
        return approverId;
    }

    public String getRemarks() {
        return remarks;
    }
}