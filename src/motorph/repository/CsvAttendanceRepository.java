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

    @Override
    public AttendanceRecord findByEmployeeIdAndDate(String employeeId, LocalDate date) {
        List<AttendanceRecord> records = loadRecords(employeeId, null, null);

        for (AttendanceRecord record : records) {
            if (record.getDate().equals(date)) {
                return record;
            }
        }

        return null;
    }

    private List<AttendanceRecord> loadRecords(String employeeId, Integer year, Integer month) {

        List<AttendanceRecord> out = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(attendanceFile.toFile()))) {

            String header = br.readLine();
            if (header == null) {
                return out;
            }

            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> p = CsvUtil.splitCsvLine(line);
                if (p.size() < 6) {
                    continue;
                }

                String id = p.get(0).trim();
                if (!id.equals(employeeId)) {
                    continue;
                }

                String lastName = p.get(1).trim();
                String firstName = p.get(2).trim();

                LocalDate parsedDate = LocalDate.parse(p.get(3).trim(), DATE_FMT);

                if (year != null && month != null) {
                    if (parsedDate.getYear() != year || parsedDate.getMonthValue() != month) {
                        continue;
                    }
                }

                var timeIn = TimeUtil.parseTime(p.get(4));
                var timeOut = TimeUtil.parseTime(p.get(5));

                String dayStatus = calculator.computeDayStatus(timeIn);
                int workedMinutes = calculator.computeWorkedMinutesMinusLunch(timeIn, timeOut);
                int lateMinutesRounded = calculator.computeLateMinutesRounded(timeIn);
                int undertimeMinutesRounded = calculator.computeUndertimeMinutesRounded(parsedDate, timeOut, dayStatus);
                int overtimeMinutesRounded = calculator.computeOvertimeMinutesRounded(parsedDate, timeOut, workedMinutes);

                int regularMinutes = calculator.computeRegularMinutes(
                        parsedDate,
                        workedMinutes,
                        lateMinutesRounded,
                        undertimeMinutesRounded,
                        dayStatus
                );

                AttendanceRecord rec = new AttendanceRecord(
                        id,
                        lastName,
                        firstName,
                        parsedDate,
                        timeIn,
                        timeOut,
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