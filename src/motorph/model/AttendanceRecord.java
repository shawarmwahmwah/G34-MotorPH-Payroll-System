package motorph.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceRecord {

    // Raw data
    private final String employeeId;
    private final String lastName;
    private final String firstName;
    private final LocalDate date;
    private final LocalTime timeIn;
    private final LocalTime timeOut;

    // Computed values (minutes)
    private final int workedMinutes;       // paid minutes after subtracting lunch overlap
    private final int lateMinutesRounded;  // late rounded to 15-minute increments (morning only)
    private final int undertimeMinutesRounded; // early-leave rounded to 15-minute increments
    private final int regularMinutes;      // paid regular minutes (after late/undertime deductions)
    private final int overtimeMinutesRounded;  // overtime rounded to 15-minute increments

    // Day status: NORMAL / HALF_DAY_PM / ABSENT etc.
    private final String dayStatus;

    public AttendanceRecord(
            String employeeId,
            String lastName,
            String firstName,
            LocalDate date,
            LocalTime timeIn,
            LocalTime timeOut,
            int workedMinutes,
            int lateMinutesRounded,
            int undertimeMinutesRounded,
            int regularMinutes,
            int overtimeMinutesRounded,
            String dayStatus
    ) {
        this.employeeId = employeeId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.workedMinutes = workedMinutes;
        this.lateMinutesRounded = lateMinutesRounded;
        this.undertimeMinutesRounded = undertimeMinutesRounded;
        this.regularMinutes = regularMinutes;
        this.overtimeMinutesRounded = overtimeMinutesRounded;
        this.dayStatus = dayStatus;
    }

    // Getters
    public String getEmployeeId() { return employeeId; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public LocalDate getDate() { return date; }
    public LocalTime getTimeIn() { return timeIn; }
    public LocalTime getTimeOut() { return timeOut; }

    public int getWorkedMinutes() { return workedMinutes; }
    public int getLateMinutesRounded() { return lateMinutesRounded; }
    public int getUndertimeMinutesRounded() { return undertimeMinutesRounded; }
    public int getRegularMinutes() { return regularMinutes; }
    public int getOvertimeMinutesRounded() { return overtimeMinutesRounded; }

    public String getDayStatus() { return dayStatus; }

    // Convenience helpers: convert minutes -> hours
    public double getWorkedHours() { return workedMinutes / 60.0; }
    public double getRegularHours() { return regularMinutes / 60.0; }
    public double getOvertimeHours() { return overtimeMinutesRounded / 60.0; }
    public double getLateHours() { return lateMinutesRounded / 60.0; }
    public double getUndertimeHours() { return undertimeMinutesRounded / 60.0; }

    public String getFullName() {
        return lastName + ", " + firstName;
    }
}