package motorph.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceCalculator {

    // Standard schedule
    private static final LocalTime SHIFT_START = LocalTime.of(8, 0);
    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_END = LocalTime.of(13, 0);

    // Regular max per day: 8 hours = 480 minutes
    private static final int REGULAR_MAX_MINUTES = 8 * 60;

    //computation ng late
    public int computeLateMinutes(LocalTime timeIn) {
        if (timeIn == null) return 0;
        if (!timeIn.isAfter(SHIFT_START)) return 0;

        return (int) Duration.between(SHIFT_START, timeIn).toMinutes();
    }

    //total worked time excluding lunch dito
    public int computeWorkedMinutesMinusLunch(LocalTime timeIn, LocalTime timeOut) {

        if (timeIn == null || timeOut == null) return 0;

        // If timeOut is before timeIn, treat as invalid for now
        if (timeOut.isBefore(timeIn)) return 0;

        int totalMinutes = (int) Duration.between(timeIn, timeOut).toMinutes();

        // Subtract lunch only if work time overlaps lunch window
        int lunchOverlap = computeOverlapMinutes(timeIn, timeOut, LUNCH_START, LUNCH_END);

        int paidMinutes = totalMinutes - lunchOverlap;

        if (paidMinutes < 0) paidMinutes = 0;

        return paidMinutes;
    }

    //weekend computation
    public int computeRegularMinutes(LocalDate date, int workedMinutes) {
        if (date == null) return 0;

        DayOfWeek d = date.getDayOfWeek();
        boolean isWeekend = (d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY);

        if (isWeekend) return 0; // weekend has no regular hours
        return Math.min(workedMinutes, REGULAR_MAX_MINUTES);
    }

    public int computeOvertimeMinutes(LocalDate date, int workedMinutes) {
        if (date == null) return 0;

        DayOfWeek d = date.getDayOfWeek();
        boolean isWeekend = (d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY);

        if (isWeekend) {
            // All weekend worked minutes are overtime
            return workedMinutes;
        }

        // Weekday overtime = worked minus regular cap
        return Math.max(0, workedMinutes - REGULAR_MAX_MINUTES);
    }

    private int computeOverlapMinutes(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {

        LocalTime overlapStart = startA.isAfter(startB) ? startA : startB;
        LocalTime overlapEnd = endA.isBefore(endB) ? endA : endB;

        if (!overlapEnd.isAfter(overlapStart)) return 0;

        return (int) Duration.between(overlapStart, overlapEnd).toMinutes();
    }
}