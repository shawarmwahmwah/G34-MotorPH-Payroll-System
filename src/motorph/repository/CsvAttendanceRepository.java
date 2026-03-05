package motorph.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import motorph.model.AttendanceRecord;
import motorph.service.AttendanceCalculator;
import motorph.util.CsvUtil;
import motorph.util.PathHelper;
import motorph.util.TimeUtil;

public class CsvAttendanceRepository implements AttendanceRepository {

    private final Path attendanceFile;
    private final AttendanceCalculator calculator;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public CsvAttendanceRepository() {
        this.attendanceFile = PathHelper.getDataFile("employee_attendance.csv");
        this.calculator = new AttendanceCalculator();
    }

    @Override
    public List<AttendanceRecord> findByEmployeeId(String employeeId) {
        return loadRecords(employeeId, null, null);
    }

    @Override
    public List<AttendanceRecord> findByEmployeeIdAndMonth(String employeeId, int year, int month) {
        return loadRecords(employeeId, year, month);
    }

    private List<AttendanceRecord> loadRecords(String employeeId, Integer year, Integer month) {

        List<AttendanceRecord> out = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(attendanceFile.toFile()))) {

            // skip header
            String header = br.readLine();
            if (header == null) return out;

            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                List<String> p = CsvUtil.splitCsvLine(line);
                if (p.size() < 6) continue;

                String id = p.get(0).trim();
                if (!id.equals(employeeId)) continue;

                String lastName = p.get(1).trim();
                String firstName = p.get(2).trim();

                LocalDate date = LocalDate.parse(p.get(3).trim(), DATE_FMT);

                // If filtering by year/month, apply filter
                if (year != null && month != null) {
                    if (date.getYear() != year || date.getMonthValue() != month) continue;
                }

                // Parse time in/out
                var timeIn = TimeUtil.parseTime(p.get(4));
                var timeOut = TimeUtil.parseTime(p.get(5));

                // Compute late + worked + regular + overtime
             // Compute day status first (NORMAL or HALF_DAY_PM)
                String dayStatus = calculator.computeDayStatus(timeIn);

                // Compute worked minutes (minus lunch overlap)
                int workedMinutes = calculator.computeWorkedMinutesMinusLunch(timeIn, timeOut);

                // Compute late (rounded, morning only)
                int lateMinutesRounded = calculator.computeLateMinutesRounded(timeIn);

                // Compute undertime (rounded)
                int undertimeMinutesRounded = calculator.computeUndertimeMinutesRounded(date, timeOut, dayStatus);

                // Compute overtime (rounded) - includes weekend rule
                int overtimeMinutesRounded = calculator.computeOvertimeMinutesRounded(date, timeOut, workedMinutes);

                // Compute regular minutes (paid)
                int regularMinutes = calculator.computeRegularMinutes(
                        date,
                        workedMinutes,
                        lateMinutesRounded,
                        undertimeMinutesRounded,
                        dayStatus
                );

                // Build record
                AttendanceRecord rec = new AttendanceRecord(
                        id, lastName, firstName,
                        date, timeIn, timeOut,
                        workedMinutes,
                        lateMinutesRounded,
                        undertimeMinutesRounded,
                        regularMinutes,
                        overtimeMinutesRounded,
                        dayStatus
                );

                out.add(rec);
            }

        } catch (Exception e) {
            System.out.println("Error reading employee_attendance.csv: " + e.getMessage());
        }

        return out;
    }
}