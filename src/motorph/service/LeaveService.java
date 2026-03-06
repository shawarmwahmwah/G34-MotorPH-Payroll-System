package motorph.service;

import java.util.List;
import java.util.UUID;

import motorph.model.Employee;
import motorph.model.LeaveBalance;
import motorph.model.LeaveRequest;
import motorph.model.UserAccount;
import motorph.repository.ActivityLogRepository;
import motorph.repository.CsvUserRepository;
import motorph.repository.LeaveBalanceRepository;
import motorph.repository.LeaveRepository;

/*
 * LeaveService
 *
 * Handles:
 * - leave submission
 * - leave approval
 * - leave rejection
 * - leave balance checking
 * - auto-reject when balance is insufficient
 * - balance deduction after approval
 * - approver role validation
 */
public class LeaveService {

    // Repository for leave request records
    private final LeaveRepository leaveRepo = new LeaveRepository();

    // Repository for leave balances
    private final LeaveBalanceRepository balanceRepo = new LeaveBalanceRepository();

    // Repository for activity logs
    private final ActivityLogRepository logRepo = new ActivityLogRepository();

    // Repository for user accounts so we can check approver role
    private final CsvUserRepository userRepo = new CsvUserRepository();

    /*
     * Employee submits a leave request.
     * This stores the request as PENDING first.
     */
    public String submitLeaveRequest(
            Employee emp,
            String leaveType,
            String startDate,
            String endDate,
            double daysRequested,
            String reason
    ) {
        // Rule: probationary employees cannot submit leave requests
        if ("Probationary".equalsIgnoreCase(emp.getStatus())) {
            return "Error: Probationary employees cannot submit leave requests.";
        }

        // Basic beginner-friendly validation
        if (leaveType == null || leaveType.trim().isEmpty()) {
            return "Error: Leave type is required.";
        }

        if (startDate == null || startDate.trim().isEmpty()) {
            return "Error: Start date is required.";
        }

        if (endDate == null || endDate.trim().isEmpty()) {
            return "Error: End date is required.";
        }

        if (daysRequested <= 0) {
            return "Error: Days requested must be greater than 0.";
        }

        if (reason == null || reason.trim().isEmpty()) {
            return "Error: Reason is required.";
        }

        // Generate short request id for console testing
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        // Create leave request object
        LeaveRequest request = new LeaveRequest(
                requestId,
                emp.getEmployeeId(),
                emp.getFirstName() + " " + emp.getLastName(),
                leaveType,
                startDate,
                endDate,
                daysRequested,
                "PENDING",
                reason,
                "",
                ""
        );

        // Save request to CSV
        leaveRepo.addRequest(request);

        // Save activity log
        logRepo.log(emp.getEmployeeId(), "LEAVE_SUBMITTED", "Leave request submitted: " + leaveType);

        return "Leave request submitted successfully. Status: PENDING. Request ID: " + requestId;
    }

    /*
     * Approve request:
     * - approver must have allowed role
     * - request must still be PENDING
     * - emergency leave is approved without balance deduction
     * - if insufficient balance, request becomes AUTO_REJECTED
     * - if enough balance, deduct then approve
     */
    public String approveLeave(String requestId, String approverId) {

        // Check if approver is allowed to approve
        if (!isAuthorizedApprover(approverId)) {
            return "Error: Only HR, Supervisor, or current admin test accounts can approve leave requests.";
        }

        List<LeaveRequest> requests = leaveRepo.loadAll();
        List<LeaveBalance> balances = balanceRepo.loadAll();

        for (int i = 0; i < requests.size(); i++) {
            LeaveRequest req = requests.get(i);

            // Skip rows until we find the matching request
            if (!req.getRequestId().equals(requestId)) {
                continue;
            }

            // Only pending requests can be approved
            if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
                return "Error: Only PENDING requests can be approved.";
            }

            // Emergency leave is unpaid and unlimited based on your rule
            if ("Emergency Leave".equalsIgnoreCase(req.getLeaveType())) {

                LeaveRequest updated = new LeaveRequest(
                        req.getRequestId(),
                        req.getEmployeeId(),
                        req.getEmployeeName(),
                        req.getLeaveType(),
                        req.getStartDate(),
                        req.getEndDate(),
                        req.getDaysRequested(),
                        "APPROVED",
                        req.getReason(),
                        approverId,
                        "Approved (unpaid emergency leave)"
                );

                // Replace old request row in memory
                requests.set(i, updated);

                // Save updated request list
                leaveRepo.saveAll(requests);

                // Log activity
                logRepo.log(approverId, "LEAVE_APPROVED", "Emergency leave approved for " + req.getEmployeeId());

                return "Leave approved successfully. Emergency leave is unpaid and no balance was deducted.";
            }

            // Find employee balance record
            LeaveBalance targetBalance = findBalanceByEmployeeId(balances, req.getEmployeeId());

            if (targetBalance == null) {
                return "Error: Leave balance record not found.";
            }

            // Get current balance for the correct leave type
            double currentBalance = getBalanceByType(targetBalance, req.getLeaveType());

            // Auto reject if balance is insufficient
            if (currentBalance < req.getDaysRequested()) {

                LeaveRequest updated = new LeaveRequest(
                        req.getRequestId(),
                        req.getEmployeeId(),
                        req.getEmployeeName(),
                        req.getLeaveType(),
                        req.getStartDate(),
                        req.getEndDate(),
                        req.getDaysRequested(),
                        "AUTO_REJECTED",
                        req.getReason(),
                        approverId,
                        "Insufficient leave balance"
                );

                // Replace request in memory and save
                requests.set(i, updated);
                leaveRepo.saveAll(requests);

                // Log activity
                logRepo.log(
                        approverId,
                        "LEAVE_AUTO_REJECTED",
                        "Insufficient balance for " + req.getEmployeeId()
                );

                return "Error: Insufficient leave balance. Request was auto-rejected.";
            }

            // Deduct leave balance
            deductBalanceByType(targetBalance, req.getLeaveType(), req.getDaysRequested());

            // Save updated balance CSV
            balanceRepo.saveAll(balances);

            // Mark request as approved
            LeaveRequest updated = new LeaveRequest(
                    req.getRequestId(),
                    req.getEmployeeId(),
                    req.getEmployeeName(),
                    req.getLeaveType(),
                    req.getStartDate(),
                    req.getEndDate(),
                    req.getDaysRequested(),
                    "APPROVED",
                    req.getReason(),
                    approverId,
                    "Approved successfully"
            );

            // Replace request and save CSV
            requests.set(i, updated);
            leaveRepo.saveAll(requests);

            // Log activity
            logRepo.log(approverId, "LEAVE_APPROVED", "Leave approved for " + req.getEmployeeId());

            return "Leave approved successfully. Balance deducted.";
        }

        return "Error: Request ID not found.";
    }

    /*
     * Reject leave request:
     * - approver must have allowed role
     * - request must still be PENDING
     * - rejection does not deduct balance
     */
    public String rejectLeave(String requestId, String approverId, String remarks) {

        // Check if approver is allowed to reject
        if (!isAuthorizedApprover(approverId)) {
            return "Error: Only HR, Supervisor, or current admin test accounts can reject leave requests.";
        }

        List<LeaveRequest> requests = leaveRepo.loadAll();

        for (int i = 0; i < requests.size(); i++) {
            LeaveRequest req = requests.get(i);

            // Skip until matching request is found
            if (!req.getRequestId().equals(requestId)) {
                continue;
            }

            // Only pending requests can be rejected
            if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
                return "Error: Only PENDING requests can be rejected.";
            }

            // Prevent blank remarks so rejection has a reason
            if (remarks == null || remarks.trim().isEmpty()) {
                remarks = "Rejected by approver";
            }

            LeaveRequest updated = new LeaveRequest(
                    req.getRequestId(),
                    req.getEmployeeId(),
                    req.getEmployeeName(),
                    req.getLeaveType(),
                    req.getStartDate(),
                    req.getEndDate(),
                    req.getDaysRequested(),
                    "REJECTED",
                    req.getReason(),
                    approverId,
                    remarks
            );

            // Replace and save
            requests.set(i, updated);
            leaveRepo.saveAll(requests);

            // Log activity
            logRepo.log(approverId, "LEAVE_REJECTED", "Leave rejected for " + req.getEmployeeId());

            return "Leave rejected successfully.";
        }

        return "Error: Request ID not found.";
    }

    public LeaveBalance viewLeaveBalance(String employeeId) {
        return balanceRepo.findByEmployeeId(employeeId);
    }
    public List<LeaveRequest> viewRequestHistory() {
        return leaveRepo.loadAll();
    }

    private LeaveBalance findBalanceByEmployeeId(List<LeaveBalance> balances, String employeeId) {
        for (LeaveBalance balance : balances) {
            if (balance.getEmployeeId().equals(employeeId)) {
                return balance;
            }
        }
        return null;
    }

    private double getBalanceByType(LeaveBalance balance, String leaveType) {
        switch (leaveType) {
            case "Sick Leave":
                return balance.getSickLeave();
            case "Vacation Leave":
                return balance.getVacationLeave();
            case "Maternity Leave":
                return balance.getMaternityLeave();
            case "Paternity Leave":
                return balance.getPaternityLeave();
            case "Bereavement Leave":
                return balance.getBereavementLeave();
            case "Emergency Leave":
                // Unlimited and unpaid, so approval logic handles it earlier
                return Double.MAX_VALUE;
            default:
                return 0.0;
        }
    }

    private void deductBalanceByType(LeaveBalance balance, String leaveType, double daysRequested) {
        switch (leaveType) {
            case "Sick Leave":
                balance.setSickLeave(balance.getSickLeave() - daysRequested);
                break;
            case "Vacation Leave":
                balance.setVacationLeave(balance.getVacationLeave() - daysRequested);
                break;
            case "Maternity Leave":
                balance.setMaternityLeave(balance.getMaternityLeave() - daysRequested);
                break;
            case "Paternity Leave":
                balance.setPaternityLeave(balance.getPaternityLeave() - daysRequested);
                break;
            case "Bereavement Leave":
                balance.setBereavementLeave(balance.getBereavementLeave() - daysRequested);
                break;
            case "Emergency Leave":
                // No deduction for emergency leave
                break;
            default:
                // Unknown leave type, do nothing
                break;
        }
    }
    private boolean isAuthorizedApprover(String approverId) {
        List<UserAccount> users = userRepo.loadUsers();

        for (UserAccount user : users) {
            if (!user.getEmployeeId().equals(approverId)) {
                continue;
            }

            String role = user.getRole();

            if (role == null) {
                return false;
            }

            return "hr".equalsIgnoreCase(role)
                    || "supervisor".equalsIgnoreCase(role)
                    || "admin".equalsIgnoreCase(role);
        }

        return false;
    }
}