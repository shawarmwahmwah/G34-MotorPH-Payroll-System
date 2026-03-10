package motorph.service;

import motorph.model.AttendanceAdjustmentRequest;
import motorph.model.AttendanceRecord;
import motorph.model.Employee;
import motorph.repository.ActivityLogRepository;
import motorph.repository.AttendanceAdjustmentRepository;
import motorph.repository.AttendanceRepository;
import motorph.repository.CsvAttendanceAdjustmentRepository;
import motorph.repository.CsvAttendanceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AttendanceAdjustmentService
 *
 * Business rules:
 * - HR can submit adjustment requests
 * - Admin can approve or reject them
 * - Supervisor can view only
 */
public class AttendanceAdjustmentService {

    private final AttendanceAdjustmentRepository adjustmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final ActivityLogRepository activityLogRepository;

    private String lastMessage;

    public AttendanceAdjustmentService() {
        this.adjustmentRepository = new CsvAttendanceAdjustmentRepository();
        this.attendanceRepository = new CsvAttendanceRepository();
        this.activityLogRepository = new ActivityLogRepository();
        this.lastMessage = "";
    }

    public String getLastMessage() {
        return lastMessage;
    }

    /**
     * Returns all attendance adjustment requests.
     */
    public List<AttendanceAdjustmentRequest> findAll() {
        return adjustmentRepository.findAll();
    }

    /**
     * HR submits a new attendance adjustment request.
     */
    public boolean submitRequest(
            Employee targetEmployee,
            Employee requester,
            String date,
            String proposedTimeIn,
            String proposedTimeOut,
            String reason
    ) {

        if (targetEmployee == null) {
            lastMessage = "Please select an employee.";
            return false;
        }

        if (requester == null) {
            lastMessage = "Requester is missing.";
            return false;
        }

        if (!isHr(requester.getPosition()) && !isRoleTextHr(requester.getPosition())) {
            lastMessage = "Only HR can submit attendance adjustment requests.";
            return false;
        }

        if (date == null || date.trim().isEmpty()) {
            lastMessage = "Date is required.";
            return false;
        }

        if (proposedTimeIn == null || proposedTimeIn.trim().isEmpty()) {
            lastMessage = "Proposed Time In is required.";
            return false;
        }

        if (proposedTimeOut == null || proposedTimeOut.trim().isEmpty()) {
            lastMessage = "Proposed Time Out is required.";
            return false;
        }

        if (reason == null || reason.trim().isEmpty()) {
            lastMessage = "Reason is required.";
            return false;
        }

        // Check if the selected attendance date exists for that employee
        AttendanceRecord currentRecord = attendanceRepository.findByEmployeeIdAndDate(
                targetEmployee.getEmployeeId(),
                LocalDate.parse(date)
        );

        if (currentRecord == null) {
            lastMessage = "No attendance record found for the selected employee and date.";
            return false;
        }

        AttendanceAdjustmentRequest request = new AttendanceAdjustmentRequest(
                UUID.randomUUID().toString().substring(0, 8),
                targetEmployee.getEmployeeId(),
                targetEmployee.getFullName(),
                date,
                currentRecord.getTimeIn() == null ? "" : currentRecord.getTimeIn().toString(),
                currentRecord.getTimeOut() == null ? "" : currentRecord.getTimeOut().toString(),
                proposedTimeIn,
                proposedTimeOut,
                reason,
                requester.getEmployeeId(),
                requester.getFullName(),
                "PENDING",
                "",
                "",
                LocalDateTime.now().toString(),
                ""
        );

        adjustmentRepository.save(request);

        activityLogRepository.log(
                requester.getEmployeeId(),
                "ATTENDANCE_ADJUSTMENT_SUBMITTED",
                "Submitted attendance adjustment request " + request.getRequestId()
        );

        lastMessage = "Attendance adjustment request submitted successfully.";
        return true;
    }

    /**
     * Admin approves a pending request.
     */
    public boolean approveRequest(String requestId, Employee approver, String remarks) {

        if (approver == null) {
            lastMessage = "Approver is missing.";
            return false;
        }

        if (!isAdmin(approver.getPosition()) && !isRoleTextAdmin(approver.getPosition())) {
            lastMessage = "Only Admin can approve attendance adjustment requests.";
            return false;
        }

        List<AttendanceAdjustmentRequest> requests = adjustmentRepository.findAll();

        for (AttendanceAdjustmentRequest request : requests) {
            if (request.getRequestId().equals(requestId)) {

                if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
                    lastMessage = "Only pending requests can be approved.";
                    return false;
                }

                request.setStatus("APPROVED");
                request.setApproverId(approver.getEmployeeId());
                request.setApproverRemarks(remarks == null ? "" : remarks);
                request.setDecidedAt(LocalDateTime.now().toString());

                adjustmentRepository.saveAll(requests);

                activityLogRepository.log(
                        approver.getEmployeeId(),
                        "ATTENDANCE_ADJUSTMENT_APPROVED",
                        "Approved attendance adjustment request " + requestId
                );

                lastMessage = "Attendance adjustment request approved.";
                return true;
            }
        }

        lastMessage = "Request ID not found.";
        return false;
    }

    /**
     * Admin rejects a pending request.
     */
    public boolean rejectRequest(String requestId, Employee approver, String remarks) {

        if (approver == null) {
            lastMessage = "Approver is missing.";
            return false;
        }

        if (!isAdmin(approver.getPosition()) && !isRoleTextAdmin(approver.getPosition())) {
            lastMessage = "Only Admin can reject attendance adjustment requests.";
            return false;
        }

        List<AttendanceAdjustmentRequest> requests = adjustmentRepository.findAll();

        for (AttendanceAdjustmentRequest request : requests) {
            if (request.getRequestId().equals(requestId)) {

                if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
                    lastMessage = "Only pending requests can be rejected.";
                    return false;
                }

                request.setStatus("REJECTED");
                request.setApproverId(approver.getEmployeeId());
                request.setApproverRemarks(remarks == null ? "" : remarks);
                request.setDecidedAt(LocalDateTime.now().toString());

                adjustmentRepository.saveAll(requests);

                activityLogRepository.log(
                        approver.getEmployeeId(),
                        "ATTENDANCE_ADJUSTMENT_REJECTED",
                        "Rejected attendance adjustment request " + requestId
                );

                lastMessage = "Attendance adjustment request rejected.";
                return true;
            }
        }

        lastMessage = "Request ID not found.";
        return false;
    }

    /**
     * Very simple role helpers.
     * We are checking both position text and common role text
     * to stay flexible with your current CSV structure.
     */
    private boolean isHr(String text) {
        return safe(text).contains("hr");
    }

    private boolean isAdmin(String text) {
        return safe(text).contains("admin");
    }

    private boolean isRoleTextHr(String text) {
        return safe(text).contains("human resource");
    }

    private boolean isRoleTextAdmin(String text) {
        return safe(text).contains("administrator");
    }

    private String safe(String text) {
        return text == null ? "" : text.toLowerCase();
    }
}