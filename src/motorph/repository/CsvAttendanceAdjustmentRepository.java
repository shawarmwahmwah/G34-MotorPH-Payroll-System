package motorph.repository;

import motorph.model.AttendanceAdjustmentRequest;
import motorph.util.CsvUtil;
import motorph.util.PathHelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * CsvAttendanceAdjustmentRepository
 *
 * Handles CSV reading/writing for attendance adjustment requests.
 */
public class CsvAttendanceAdjustmentRepository implements AttendanceAdjustmentRepository {

    private final Path filePath;

    public CsvAttendanceAdjustmentRepository() {
        this.filePath = PathHelper.getDataFile("attendance_adjustment_requests.csv");
        ensureFileExists();
    }

    /**
     * Creates the CSV file with header if it does not exist yet.
     */
    private void ensureFileExists() {
        try {
            if (!Files.exists(filePath)) {
                FileWriter fw = new FileWriter(filePath.toFile());
                fw.write("requestId,employeeId,employeeName,date,currentTimeIn,currentTimeOut,proposedTimeIn,proposedTimeOut,reason,requestedById,requestedByName,status,approverId,approverRemarks,requestedAt,decidedAt\n");
                fw.close();
            }
        } catch (Exception e) {
            System.out.println("Error creating attendance_adjustment_requests.csv: " + e.getMessage());
        }
    }

    @Override
    public List<AttendanceAdjustmentRequest> findAll() {

        List<AttendanceAdjustmentRequest> requests = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {

            String header = br.readLine();
            if (header == null) {
                return requests;
            }

            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> p = CsvUtil.splitCsvLine(line);
                if (p.size() < 16) {
                    continue;
                }

                AttendanceAdjustmentRequest request = new AttendanceAdjustmentRequest(
                        p.get(0),
                        p.get(1),
                        p.get(2),
                        p.get(3),
                        p.get(4),
                        p.get(5),
                        p.get(6),
                        p.get(7),
                        p.get(8),
                        p.get(9),
                        p.get(10),
                        p.get(11),
                        p.get(12),
                        p.get(13),
                        p.get(14),
                        p.get(15)
                );

                requests.add(request);
            }

        } catch (Exception e) {
            System.out.println("Error reading attendance adjustment requests: " + e.getMessage());
        }

        return requests;
    }

    @Override
    public void save(AttendanceAdjustmentRequest request) {
        try (FileWriter fw = new FileWriter(filePath.toFile(), true)) {
            fw.write(toCsvRow(request) + "\n");
        } catch (Exception e) {
            System.out.println("Error saving attendance adjustment request: " + e.getMessage());
        }
    }

    @Override
    public void saveAll(List<AttendanceAdjustmentRequest> requests) {
        try (FileWriter fw = new FileWriter(filePath.toFile(), false)) {

            fw.write("requestId,employeeId,employeeName,date,currentTimeIn,currentTimeOut,proposedTimeIn,proposedTimeOut,reason,requestedById,requestedByName,status,approverId,approverRemarks,requestedAt,decidedAt\n");

            for (AttendanceAdjustmentRequest request : requests) {
                fw.write(toCsvRow(request) + "\n");
            }

        } catch (Exception e) {
            System.out.println("Error rewriting attendance adjustment requests: " + e.getMessage());
        }
    }

    /**
     * Converts one request to a safe CSV row.
     */
    private String toCsvRow(AttendanceAdjustmentRequest request) {
        return escape(request.getRequestId()) + "," +
                escape(request.getEmployeeId()) + "," +
                escape(request.getEmployeeName()) + "," +
                escape(request.getDate()) + "," +
                escape(request.getCurrentTimeIn()) + "," +
                escape(request.getCurrentTimeOut()) + "," +
                escape(request.getProposedTimeIn()) + "," +
                escape(request.getProposedTimeOut()) + "," +
                escape(request.getReason()) + "," +
                escape(request.getRequestedById()) + "," +
                escape(request.getRequestedByName()) + "," +
                escape(request.getStatus()) + "," +
                escape(request.getApproverId()) + "," +
                escape(request.getApproverRemarks()) + "," +
                escape(request.getRequestedAt()) + "," +
                escape(request.getDecidedAt());
    }

    /**
     * Escapes CSV values safely.
     */
    private String escape(String value) {
        if (value == null) {
            return "";
        }

        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}