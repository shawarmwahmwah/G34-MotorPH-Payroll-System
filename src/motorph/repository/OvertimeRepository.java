package motorph.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import motorph.model.OvertimeRequest;
import motorph.util.CsvUtil;
import motorph.util.PathHelper;

public class OvertimeRepository {

    private static final String HEADER =
            "requestId,employeeId,employeeName,date,timeRange,hoursRequested,reason,status,requestedAt,approverId,approvedAt,remarks";

    private final Path filePath;

    public OvertimeRepository() {
        this.filePath = PathHelper.getDataFile("overtime_requests.csv");
    }

    public List<OvertimeRequest> loadAll() {
        ensureHeaderExists();

        List<OvertimeRequest> requests = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
            String header = br.readLine();
            if (header == null) {
                return requests;
            }

            Map<String, Integer> indexByName = mapHeaderIndexes(header);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> values = CsvUtil.splitCsvLine(line);
                String requestId = getValue(values, indexByName, "requestid");
                String employeeId = getValue(values, indexByName, "employeeid");
                String employeeName = getValue(values, indexByName, "employeename");
                String date = getValue(values, indexByName, "date");
                String timeRange = getValue(values, indexByName, "timerange");
                double hoursRequested = parseHours(getValue(values, indexByName, "hoursrequested"));
                String reason = getValue(values, indexByName, "reason");
                String status = getValue(values, indexByName, "status");
                String requestedAt = getValue(values, indexByName, "requestedat");
                String approverId = getValue(values, indexByName, "approverid");
                if (approverId.isEmpty()) {
                    approverId = getValue(values, indexByName, "approvedby");
                }
                String approvedAt = getValue(values, indexByName, "approvedat");
                String remarks = getValue(values, indexByName, "remarks");

                // Backward compatibility for the previous CSV shape.
                if (timeRange.isEmpty() && hoursRequested > 0) {
                    timeRange = String.format(Locale.ENGLISH, "%.2f hr(s)", hoursRequested);
                }

                requests.add(new OvertimeRequest(
                        requestId,
                        employeeId,
                        employeeName,
                        date,
                        timeRange,
                        hoursRequested,
                        reason,
                        status,
                        requestedAt,
                        approverId,
                        approvedAt,
                        remarks
                ));
            }
        } catch (Exception e) {
            System.out.println("Error reading overtime_requests.csv: " + e.getMessage());
        }

        return requests;
    }

    public void addRequest(OvertimeRequest request) {
        ensureHeaderExists();

        try (FileWriter fw = new FileWriter(filePath.toFile(), true)) {
            fw.write(toCsvRow(request) + "\n");
            fw.flush();
        } catch (Exception e) {
            System.out.println("Error writing overtime_requests.csv: " + e.getMessage());
        }
    }

    public void saveAll(List<OvertimeRequest> requests) {
        ensureHeaderExists();

        try (FileWriter fw = new FileWriter(filePath.toFile(), false)) {
            fw.write(HEADER + "\n");
            for (OvertimeRequest request : requests) {
                fw.write(toCsvRow(request) + "\n");
            }
            fw.flush();
        } catch (Exception e) {
            System.out.println("Error rewriting overtime_requests.csv: " + e.getMessage());
        }
    }

    private void ensureHeaderExists() {
        try {
            if (!Files.exists(filePath) || Files.size(filePath) == 0) {
                try (FileWriter fw = new FileWriter(filePath.toFile(), false)) {
                    fw.write(HEADER + "\n");
                }
            }
        } catch (Exception e) {
            System.out.println("Error ensuring overtime_requests.csv header: " + e.getMessage());
        }
    }

    private Map<String, Integer> mapHeaderIndexes(String header) {
        Map<String, Integer> indexByName = new HashMap<>();
        List<String> headers = CsvUtil.splitCsvLine(header);
        for (int i = 0; i < headers.size(); i++) {
            indexByName.put(normalize(headers.get(i)), i);
        }
        return indexByName;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ENGLISH);
    }

    private String getValue(List<String> values, Map<String, Integer> indexByName, String key) {
        Integer index = indexByName.get(key);
        if (index == null || index < 0 || index >= values.size()) {
            return "";
        }
        String value = values.get(index);
        return value == null ? "" : value.trim();
    }

    private double parseHours(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return 0.0;
        }

        try {
            return Double.parseDouble(raw.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String toCsvRow(OvertimeRequest request) {
        return escapeCsv(request.getRequestId()) + ","
                + escapeCsv(request.getEmployeeId()) + ","
                + escapeCsv(request.getEmployeeName()) + ","
                + escapeCsv(request.getDate()) + ","
                + escapeCsv(request.getTimeRange()) + ","
                + request.getHoursRequested() + ","
                + escapeCsv(request.getReason()) + ","
                + escapeCsv(request.getStatus()) + ","
                + escapeCsv(request.getRequestedAt()) + ","
                + escapeCsv(request.getApproverId()) + ","
                + escapeCsv(request.getApprovedAt()) + ","
                + escapeCsv(request.getRemarks());
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        String escaped = value.replace("\"", "\"\"");

        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }
}
