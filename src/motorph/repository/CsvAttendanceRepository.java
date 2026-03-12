package motorph.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private static final DateTimeFormatter CSV_TIME_FMT = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter AMPM_TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    public CsvAttendanceRepository() {
        this.attendanceFile = PathHelper.getDataFile("employee_attendance.csv");
        this.calculator = new AttendanceCalculator();
    }

    @Override
    public List<AttendanceRecord> findAll() {
        return loadRecords(null, null, null);
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

    @Override
    public boolean updateAttendanceTime(String employeeId, LocalDate date, String newTimeIn, String newTimeOut) {

        if (employeeId == null || employeeId.trim().isEmpty() || date == null) {
            return false;
        }

        String formattedDate = date.format(DATE_FMT);
        String csvTimeIn = normalizeToCsvTime(newTimeIn);
        String csvTimeOut = normalizeToCsvTime(newTimeOut);

        if (csvTimeIn == null || csvTimeOut == null) {
            return false;
        }

        List<String> rewrittenLines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader br = new BufferedReader(new FileReader(attendanceFile.toFile()))) {

            String header = br.readLine();
            if (header == null) {
                return false;
            }

            rewrittenLines.add(header);

            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    rewrittenLines.add(line);
                    continue;
                }

                List<String> p = CsvUtil.splitCsvLine(line);

                if (p.size() < 6) {
                    rewrittenLines.add(line);
                    continue;
                }

                String rowEmployeeId = p.get(0).trim();
                String rowDate = p.get(3).trim();

                if (rowEmployeeId.equals(employeeId) && rowDate.equals(formattedDate)) {
                    p.set(4, csvTimeIn);
                    p.set(5, csvTimeOut);
                    updated = true;
                }

                rewrittenLines.add(toCsvRow(p));
            }

        } catch (Exception e) {
            System.out.println("Error reading employee_attendance.csv for update: " + e.getMessage());
            return false;
        }

        if (!updated) {
            return false;
        }

        try (FileWriter fw = new FileWriter(attendanceFile.toFile(), false)) {
            for (String rewrittenLine : rewrittenLines) {
                fw.write(rewrittenLine + "\n");
            }
            fw.flush();
            return true;
        } catch (Exception e) {
            System.out.println("Error rewriting employee_attendance.csv: " + e.getMessage());
            return false;
        }
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

                if (employeeId != null && !employeeId.trim().isEmpty() && !id.equals(employeeId)) {
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

                LocalTime timeIn = TimeUtil.parseTime(p.get(4));
                LocalTime timeOut = TimeUtil.parseTime(p.get(5));

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

    private String normalizeToCsvTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        String cleaned = raw.trim().toUpperCase();

        try {
            java.time.LocalTime parsed12Hour = java.time.LocalTime.parse(
                    cleaned,
                    java.time.format.DateTimeFormatter.ofPattern("h:mm a")
            );
            return parsed12Hour.format(java.time.format.DateTimeFormatter.ofPattern("H:mm"));
        } catch (Exception ignored) {
        }

        try {
            java.time.LocalTime parsed24Hour = TimeUtil.parseTime(cleaned);
            if (parsed24Hour == null) {
                return null;
            }
            return parsed24Hour.format(java.time.format.DateTimeFormatter.ofPattern("H:mm"));
        } catch (Exception e) {
            return null;
        }
    }

    private String toCsvRow(List<String> values) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(escape(values.get(i)));
        }

        return sb.toString();
    }

    private String escape(String value) {
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