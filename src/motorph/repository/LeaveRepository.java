package motorph.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import motorph.model.LeaveRequest;
import motorph.util.CsvUtil;
import motorph.util.PathHelper;

public class LeaveRepository {

    private final Path filePath;

    public LeaveRepository() {
        this.filePath = PathHelper.getDataFile("leave_requests.csv");
    }
    public void addRequest(LeaveRequest request) {
        try (FileWriter fw = new FileWriter(filePath.toFile(), true)) {

            String row =
                    escapeCsv(request.getRequestId()) + "," +
                    escapeCsv(request.getEmployeeId()) + "," +
                    escapeCsv(request.getEmployeeName()) + "," +
                    escapeCsv(request.getLeaveType()) + "," +
                    escapeCsv(request.getStartDate()) + "," +
                    escapeCsv(request.getEndDate()) + "," +
                    request.getDaysRequested() + "," +
                    escapeCsv(request.getStatus()) + "," +
                    escapeCsv(request.getReason()) + "," +
                    escapeCsv(request.getApproverId()) + "," +
                    escapeCsv(request.getRemarks()) + "\n";

            fw.write(row);
            fw.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<LeaveRequest> loadAll() {
        List<LeaveRequest> requests = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {

            String header = br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> p = CsvUtil.splitCsvLine(line);

                if (p.size() < 11) {
                    continue;
                }

                requests.add(new LeaveRequest(
                        p.get(0),
                        p.get(1),
                        p.get(2),
                        p.get(3),
                        p.get(4),
                        p.get(5),
                        Double.parseDouble(p.get(6)),
                        p.get(7),
                        p.get(8),
                        p.get(9),
                        p.get(10)
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return requests;
    }

    public void saveAll(List<LeaveRequest> requests) {
        try (FileWriter fw = new FileWriter(filePath.toFile(), false)) {

            fw.write("requestId,employeeId,employeeName,leaveType,startDate,endDate,daysRequested,status,reason,approverId,remarks\n");

            for (LeaveRequest request : requests) {
                fw.write(
                        escapeCsv(request.getRequestId()) + "," +
                        escapeCsv(request.getEmployeeId()) + "," +
                        escapeCsv(request.getEmployeeName()) + "," +
                        escapeCsv(request.getLeaveType()) + "," +
                        escapeCsv(request.getStartDate()) + "," +
                        escapeCsv(request.getEndDate()) + "," +
                        request.getDaysRequested() + "," +
                        escapeCsv(request.getStatus()) + "," +
                        escapeCsv(request.getReason()) + "," +
                        escapeCsv(request.getApproverId()) + "," +
                        escapeCsv(request.getRemarks()) + "\n"
                );
            }

            fw.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
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