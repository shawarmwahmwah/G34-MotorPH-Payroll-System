package motorph.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceRecord {

    private final String employeeId;
    private final String lastName;
    private final String firstName;

    private final LocalDate date;
    private final LocalTime timeIn;
    private final LocalTime timeOut;

    // Computed values (in minutes)
    private final int lateMinutes;
    private final int workedMinutes;   // after removing lunch + late deduction is NOT removed here
    private final int regularMinutes;  // weekday only, max 8 hours (480 minutes)
    private final int overtimeMinutes; // weekday overtime OR weekend work treated as overtime

    public AttendanceRecord(
            String employeeId,
            String lastName,
            String firstName,
            LocalDate date,
            LocalTime timeIn,
            LocalTime timeOut,
            int lateMinutes,
            int workedMinutes,
            int regularMinutes,
            int overtimeMinutes
    ) {
        this.employeeId = employeeId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.lateMinutes = lateMinutes;
        this.workedMinutes = workedMinutes;
        this.regularMinutes = regularMinutes;
        this.overtimeMinutes = overtimeMinutes;
    }

    // Basic getters
    public String getEmployeeId() { return employeeId; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public LocalDate getDate() { return date; }
    public LocalTime getTimeIn() { return timeIn; }
    public LocalTime getTimeOut() { return timeOut; }

    // Computed getters
    public int getLateMinutes() { return lateMinutes; }
    public int getWorkedMinutes() { return workedMinutes; }
    public int getRegularMinutes() { return regularMinutes; }
    public int getOvertimeMinutes() { return overtimeMinutes; }

    // Convenience helpers (hours as double)
    public double getWorkedHours() { return workedMinutes / 60.0; }
    public double getRegularHours() { return regularMinutes / 60.0; }
    public double getOvertimeHours() { return overtimeMinutes / 60.0; }

    public String getFullName() {
        return lastName + ", " + firstName;
    }
}