package motorph.service;

import java.util.List;
import java.util.UUID;

import motorph.model.Employee;
import motorph.model.LeaveBalance;
import motorph.model.LeaveRequest;
import motorph.repository.ActivityLogRepository;
import motorph.repository.LeaveBalanceRepository;
import motorph.repository.LeaveRepository;

/*
 * LeaveService
 *
 * Handles leave request submission, approval, rejection,
 * balance checking, auto-rejection on insufficient balance,
 * and balance deduction after approval.
 */
public class LeaveService {

    private final LeaveRepository leaveRepo = new LeaveRepository();
    private final LeaveBalanceRepository balanceRepo = new LeaveBalanceRepository();
    private final ActivityLogRepository logRepo = new ActivityLogRepository();

    /*
     * Employee submits a leave request.
     * This only stores the request as PENDING.
     */
    public String submitLeaveRequest(
            Employee emp,
            String leaveType,
            String startDate,
            String endDate,
            double daysRequested,
            String reason
    ) {
        // Probationary employees cannot request leave
        if ("Probationary".equalsIgnoreCase(emp.getStatus())) {
            return "Error: Probationary employees cannot submit leave requests.";
        }

        String requestId = UUID.randomUUID().toString().substring(0, 8);

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

        leaveRepo.addRequest(request);
        logRepo.log(emp.getEmployeeId(), "LEAVE_SUBMITTED", "Leave request submitted: " + leaveType);

        return "Leave request submitted successfully. Status: PENDING";
    }

    /*
     * Approve request:
     * - check balance
     * - if insufficient, auto reject
     * - if sufficient, deduct balance and approve
     */
    public String approveLeave(String requestId, String approverId) {

        List<LeaveRequest> requests = leaveRepo.loadAll();
        List<LeaveBalance> balances = balanceRepo.loadAll();

        for (int i = 0; i < requests.size(); i++) {
            LeaveRequest req = requests.get(i);

            if (!req.getRequestId().equals(requestId)) {
                continue;
            }

            if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
                return "Error: Only PENDING requests can be approved.";
            }

            // Emergency leave = unpaid, unlimited, auto-approve
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

                requests.set(i, updated);
                leaveRepo.saveAll(requests);
                logRepo.log(approverId, "LEAVE_APPROVED", "Emergency leave approved for " + req.getEmployeeId());

                return "Leave approved successfully.";
            }

            // Find employee balance
            LeaveBalance targetBalance = null;
            for (LeaveBalance b : balances) {
                if (b.getEmployeeId().equals(req.getEmployeeId())) {
                    targetBalance = b;
                    break;
                }
            }

            if (targetBalance == null) {
                return "Error: Leave balance record not found.";
            }

            double currentBalance = getBalanceByType(targetBalance, req.getLeaveType());

            // Auto reject if insufficient
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

                requests.set(i, updated);
                leaveRepo.saveAll(requests);
                logRepo.log(approverId, "LEAVE_AUTO_REJECTED",
                        "Insufficient balance for " + req.getEmployeeId());

                return "Error: Insufficient leave balance. Request was auto-rejected.";
            }

            // Deduct balance
            deductBalanceByType(targetBalance, req.getLeaveType(), req.getDaysRequested());

            // Save updated balance list
            balanceRepo.saveAll(balances);

            // Mark request approved
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

            requests.set(i, updated);
            leaveRepo.saveAll(requests);
            logRepo.log(approverId, "LEAVE_APPROVED", "Leave approved for " + req.getEmployeeId());

            return "Leave approved successfully.";
        }

        return "Error: Request ID not found.";
    }

    public String rejectLeave(String requestId, String approverId, String remarks) {
        List<LeaveRequest> requests = leaveRepo.loadAll();

        for (int i = 0; i < requests.size(); i++) {
            LeaveRequest req = requests.get(i);

            if (!req.getRequestId().equals(requestId)) {
                continue;
            }

            if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
                return "Error: Only PENDING requests can be rejected.";
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

            requests.set(i, updated);
            leaveRepo.saveAll(requests);
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

    private double getBalanceByType(LeaveBalance b, String leaveType) {
        switch (leaveType) {
            case "Sick Leave":
                return b.getSickLeave();
            case "Vacation Leave":
                return b.getVacationLeave();
            case "Maternity Leave":
                return b.getMaternityLeave();
            case "Paternity Leave":
                return b.getPaternityLeave();
            case "Bereavement Leave":
                return b.getBereavementLeave();
            default:
                return 0.0;
        }
    }

    private void deductBalanceByType(LeaveBalance b, String leaveType, double days) {
        switch (leaveType) {
            case "Sick Leave":
                b.setSickLeave(b.getSickLeave() - days);
                break;
            case "Vacation Leave":
                b.setVacationLeave(b.getVacationLeave() - days);
                break;
            case "Maternity Leave":
                b.setMaternityLeave(b.getMaternityLeave() - days);
                break;
            case "Paternity Leave":
                b.setPaternityLeave(b.getPaternityLeave() - days);
                break;
            case "Bereavement Leave":
                b.setBereavementLeave(b.getBereavementLeave() - days);
                break;
            default:
                break;
        }
    }
}