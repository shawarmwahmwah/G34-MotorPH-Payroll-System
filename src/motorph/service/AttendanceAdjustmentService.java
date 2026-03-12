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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * AttendanceAdjustmentService
 *
 * Business rules:
 * - UI controls who can access submit / approve / reject
 * - Service handles validation, CSV persistence, and activity logs
 */
public class AttendanceAdjustmentService {

    private final AttendanceAdjustmentRepository adjustmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final ActivityLogRepository activityLogRepository;

    private String lastMessage;

    private static final DateTimeFormatter UI_REQUEST_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter CSV_DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public AttendanceAdjustmentService() {
        this.adjustmentRepository = new CsvAttendanceAdjustmentRepository();
        this.attendanceRepository = new CsvAttendanceRepository();
        this.activityLogRepository = new ActivityLogRepository();
        this.lastMessage = "";
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public List<AttendanceAdjustmentRequest> findAll() {
        return adjustmentRepository.findAll();
    }

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

        LocalDate parsedRequestDate = parseFlexibleDate(date);
        if (parsedRequestDate == null) {
            lastMessage = "Invalid request date format.";
            return false;
        }

        AttendanceRecord currentRecord = attendanceRepository.findByEmployeeIdAndDate(
                targetEmployee.getEmployeeId(),
                parsedRequestDate
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

    public boolean approveRequest(String requestId, Employee approver, String remarks) {

        if (approver == null) {
            lastMessage = "Approver is missing.";
            return false;
        }

        List<AttendanceAdjustmentRequest> requests = adjustmentRepository.findAll();

        for (AttendanceAdjustmentRequest request : requests) {
            if (request.getRequestId().equals(requestId)) {

                if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
                    lastMessage = "Only pending requests can be approved.";
                    return false;
                }

                LocalDate attendanceDate = parseFlexibleDate(request.getDate());
                if (attendanceDate == null) {
                    lastMessage = "Invalid request date format.";
                    return false;
                }

                boolean attendanceUpdated = attendanceRepository.updateAttendanceTime(
                        request.getEmployeeId(),
                        attendanceDate,
                        request.getProposedTimeIn(),
                        request.getProposedTimeOut()
                );

                if (!attendanceUpdated) {
                    lastMessage = "Attendance record update failed.";
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
                        "Approved request " + requestId
                );

                activityLogRepository.log(
                        approver.getEmployeeId(),
                        "ATTENDANCE_RECORD_UPDATED",
                        "Updated employee " + request.getEmployeeId()
                                + " attendance on " + attendanceDate.format(CSV_DATE_FMT)
                );

                lastMessage = "Attendance adjustment approved and applied successfully.";
                return true;
            }
        }

        lastMessage = "Request ID not found.";
        return false;
    }

    public boolean rejectRequest(String requestId, Employee approver, String remarks) {

        if (approver == null) {
            lastMessage = "Approver is missing.";
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
                        "Rejected request " + requestId
                );

                lastMessage = "Attendance adjustment request rejected.";
                return true;
            }
        }

        lastMessage = "Request ID not found.";
        return false;
    }

    private LocalDate parseFlexibleDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        String cleaned = raw.trim();

        try {
            return LocalDate.parse(cleaned, UI_REQUEST_DATE_FMT);
        } catch (Exception ignored) {
        }

        try {
            return LocalDate.parse(cleaned, CSV_DATE_FMT);
        } catch (Exception ignored) {
        }

        return null;
    }
}