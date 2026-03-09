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
 * - leave request submission
 * - leave approval
 * - leave rejection
 * - leave balance checking
 * - auto-rejection if balance is insufficient
 * - balance deduction after approval
 * - approver role validation
 */
public class LeaveService {

    // Repository for leave requests
    private final LeaveRepository leaveRepo = new LeaveRepository();

    // Repository for leave balances
    private final LeaveBalanceRepository balanceRepo = new LeaveBalanceRepository();

    // Repository for activity logs
    private final ActivityLogRepository logRepo = new ActivityLogRepository();

    // Repository for users so we can validate approver roles
    private final CsvUserRepository userRepo = new CsvUserRepository();

    /*
     * Employee submits a leave request.
     * This stores the request first as PENDING.
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

        // Basic validation so bad input is caught early
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

        // Generate a short request ID for easier console testing
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        // Create request object
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

        // Log the action
        logRepo.log(emp.getEmployeeId(), "LEAVE_SUBMITTED", "Leave request submitted: " + leaveType);

        return "Leave request submitted successfully. Status: PENDING. Request ID: " + requestId;
    }

    /*
     * Approve a leave request.
     *
     * Rules:
     * - only authorized approvers can approve
     * - only PENDING requests can be approved
     * - emergency leave is unpaid and unlimited
     * - insufficient balance causes AUTO_REJECTED
     * - valid approval deducts the correct balance
     */
    public String approveLeave(String requestId, String approverId) {

        // Validate if the approver has elevated role
        if (!isAuthorizedApprover(approverId)) {
            return "Error: Only Admin / HR / Supervisor accounts can approve leave requests.";
        }

        List<LeaveRequest> requests = leaveRepo.loadAll();
        List<LeaveBalance> balances = balanceRepo.loadAll();

        for (int i = 0; i < requests.size(); i++) {
            LeaveRequest req = requests.get(i);

            // Skip non-matching requests
            if (!req.getRequestId().equals(requestId)) {
                continue;
            }

            // Only pending requests can be approved
            if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
                return "Error: Only PENDING requests can be approved.";
            }

            // Emergency leave = unpaid and unlimited
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

                // Replace the old request object with the updated one
                requests.set(i, updated);

                // Save updated request list
                leaveRepo.saveAll(requests);

                // Log activity
                logRepo.log(approverId, "LEAVE_APPROVED", "Emergency leave approved for " + req.getEmployeeId());

                return "Leave approved successfully. Emergency leave is unpaid and no balance was deducted.";
            }

            // Find matching employee balance
            LeaveBalance targetBalance = findBalanceByEmployeeId(balances, req.getEmployeeId());

            if (targetBalance == null) {
                return "Error: Leave balance record not found.";
            }

            // Check current balance for the specific leave type
            double currentBalance = getBalanceByType(targetBalance, req.getLeaveType());

            // Auto-reject if balance is insufficient
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

                // Replace request and save
                requests.set(i, updated);
                leaveRepo.saveAll(requests);

                // Log activity
                logRepo.log(approverId, "LEAVE_AUTO_REJECTED", "Insufficient balance for " + req.getEmployeeId());

                return "Error: Insufficient leave balance. Request was auto-rejected.";
            }

            // Deduct the approved leave balance
            deductBalanceByType(targetBalance, req.getLeaveType(), req.getDaysRequested());

            // Save updated balances
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

            // Replace request and save
            requests.set(i, updated);
            leaveRepo.saveAll(requests);

            // Log activity
            logRepo.log(approverId, "LEAVE_APPROVED", "Leave approved for " + req.getEmployeeId());

            return "Leave approved successfully. Balance deducted.";
        }

        return "Error: Request ID not found.";
    }

    /*
     * Reject a leave request.
     *
     * Rules:
     * - only authorized approvers can reject
     * - only PENDING requests can be rejected
     * - rejection does not change leave balance
     */
    public String rejectLeave(String requestId, String approverId, String remarks) {

        // Validate if the approver has elevated role
        if (!isAuthorizedApprover(approverId)) {
            return "Error: Only Admin / HR / Supervisor accounts can reject leave requests.";
        }

        List<LeaveRequest> requests = leaveRepo.loadAll();

        for (int i = 0; i < requests.size(); i++) {
            LeaveRequest req = requests.get(i);

            // Skip non-matching requests
            if (!req.getRequestId().equals(requestId)) {
                continue;
            }

            // Only pending requests can be rejected
            if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
                return "Error: Only PENDING requests can be rejected.";
            }

            // Default remarks if user leaves it blank
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

            // Replace request and save
            requests.set(i, updated);
            leaveRepo.saveAll(requests);

            // Log activity
            logRepo.log(approverId, "LEAVE_REJECTED", "Leave rejected for " + req.getEmployeeId());

            return "Leave rejected successfully.";
        }

        return "Error: Request ID not found.";
    }

    /*
     * View current leave balance of one employee.
     */
    public LeaveBalance viewLeaveBalance(String employeeId) {
        return balanceRepo.findByEmployeeId(employeeId);
    }

    /*
     * View request history.
     * For console testing, returning all requests is okay for now.
     */
    public List<LeaveRequest> viewRequestHistory() {
        return leaveRepo.loadAll();
    }

    /*
     * Helper method: find the employee's LeaveBalance row.
     */
    private LeaveBalance findBalanceByEmployeeId(List<LeaveBalance> balances, String employeeId) {
        for (LeaveBalance balance : balances) {
            if (balance.getEmployeeId().equals(employeeId)) {
                return balance;
            }
        }
        return null;
    }

    /*
     * Helper method: return the correct balance based on leave type.
     */
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
                // Unlimited and unpaid
                return Double.MAX_VALUE;
            default:
                return 0.0;
        }
    }

    /*
     * Helper method: deduct the approved days from the correct leave type.
     */
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
                // Unknown leave type = no deduction
                break;
        }
    }

    /*
     * Helper method: check if the employeeId belongs to an allowed approver role.
     *
     * We include:
     * - admin
     * - hr
     * - supervisor
     *
     * This keeps your current data working while also matching your final rules.
     */
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

            return "admin".equalsIgnoreCase(role)
                    || "hr".equalsIgnoreCase(role)
                    || "supervisor".equalsIgnoreCase(role);
        }

        return false;
    }
}