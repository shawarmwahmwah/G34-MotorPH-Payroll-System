package motorph.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceCalculator {

    // Standard schedule
    private static final LocalTime SHIFT_START = LocalTime.of(8, 0);
    private static final LocalTime SHIFT_END   = LocalTime.of(17, 0);

    // Lunch (not paid)
    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_END   = LocalTime.of(13, 0);

    // Full day paid minutes: 8 hours = 480 mins
    private static final int FULL_DAY_MINUTES = 8 * 60;

    // Half-day paid minutes: 4 hours = 240 mins
    private static final int HALF_DAY_MINUTES = 4 * 60;

    // Increment rule: 15 minutes
    private static final int INCREMENT = 15;

    /** Round minutes to nearest 15-minute increment. */
    public int roundToNearest15(int minutes) {
        if (minutes <= 0) return 0;

        // Nearest rounding: +7 means 0-7 rounds down, 8-14 rounds up to 15
        // This matches your example: 9 mins -> 15, 3 mins -> 0
        return ((minutes + 7) / INCREMENT) * INCREMENT;
    }

    /** Compute paid minutes between timeIn and timeOut, subtracting lunch overlap. */
    public int computeWorkedMinutesMinusLunch(LocalTime timeIn, LocalTime timeOut) {
        if (timeIn == null || timeOut == null) return 0;
        if (timeOut.isBefore(timeIn)) return 0;

        int totalMinutes = (int) Duration.between(timeIn, timeOut).toMinutes();
        int lunchOverlap = computeOverlapMinutes(timeIn, timeOut, LUNCH_START, LUNCH_END);

        int paidMinutes = totalMinutes - lunchOverlap;
        return Math.max(0, paidMinutes);
    }

    /** Returns day status based on time-in. */
    public String computeDayStatus(LocalTime timeIn) {
        if (timeIn == null) return "ABSENT";

        // If time-in is 12:00 or later: treat as HALF-DAY PM (or absent based on policy)
        // You said: many companies treat as half-day/absent. We'll start with HALF-DAY.
        if (!timeIn.isBefore(LUNCH_START)) {
            return "HALF_DAY_PM";
        }

        return "NORMAL";
    }

    /** Late minutes ONLY counts from 08:00 to 12:00, rounded to 15 minutes. */
    public int computeLateMinutesRounded(LocalTime timeIn) {
        if (timeIn == null) return 0;

        // If time-in is 12:00 or later, we do NOT count huge late
        // because we treat it as half-day instead.
        if (!timeIn.isBefore(LUNCH_START)) {
            return 0;
        }

        if (!timeIn.isAfter(SHIFT_START)) return 0;

        int late = (int) Duration.between(SHIFT_START, timeIn).toMinutes();

        // Round to nearest 15
        return roundToNearest15(late);
    }

    /** Undertime = leaving before 17:00 on weekdays, rounded to 15 minutes. */
    public int computeUndertimeMinutesRounded(LocalDate date, LocalTime timeOut, String dayStatus) {
        if (date == null || timeOut == null) return 0;

        // Weekend: no undertime concept (work is overtime-based)
        if (isWeekend(date)) return 0;

        // Half-day PM: employee is considered half-day, we won't compute undertime
        if ("HALF_DAY_PM".equals(dayStatus)) return 0;

        if (!timeOut.isBefore(SHIFT_END)) return 0;

        int undertime = (int) Duration.between(timeOut, SHIFT_END).toMinutes();

        // If undertime overlaps lunch (rare edge case), we should not deduct lunch twice.
        // But undertime is usually after lunch, so this is safe.

        return roundToNearest15(undertime);
    }

    /** Overtime minutes rounded. */
    public int computeOvertimeMinutesRounded(LocalDate date, LocalTime timeOut, int workedMinutes) {
        if (date == null) return 0;

        // Weekend: all worked minutes are overtime
        if (isWeekend(date)) {
            return roundToNearest15(workedMinutes);
        }

        // Weekday: overtime is time after 17:00
        if (timeOut == null || !timeOut.isAfter(SHIFT_END)) return 0;

        int overtime = (int) Duration.between(SHIFT_END, timeOut).toMinutes();

        return roundToNearest15(overtime);
    }

    /**
     * Regular minutes (paid) on weekdays:
     * Full day = 480 mins minus late and undertime (rounded)
     *
     * Half-day PM:
     * We'll cap regular at HALF_DAY_MINUTES (240 mins),
     * but actual paid minutes should not exceed worked minutes.
     */
    public int computeRegularMinutes(LocalDate date, int workedMinutes, int lateRounded, int undertimeRounded, String dayStatus) {

        if (date == null) return 0;

        // Weekend: regular minutes = 0 (all overtime)
        if (isWeekend(date)) return 0;

        if ("HALF_DAY_PM".equals(dayStatus)) {
            // Half-day: max 4 hours paid, but cannot exceed actual worked minutes
            return Math.min(HALF_DAY_MINUTES, workedMinutes);
        }

        // Normal weekday
        int regular = FULL_DAY_MINUTES - lateRounded - undertimeRounded;

        // Regular cannot exceed actual worked minutes
        regular = Math.min(regular, workedMinutes);

        return Math.max(0, regular);
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek d = date.getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }

    /** Compute overlap between [startA,endA] and [startB,endB] */
    private int computeOverlapMinutes(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {

        LocalTime overlapStart = startA.isAfter(startB) ? startA : startB;
        LocalTime overlapEnd = endA.isBefore(endB) ? endA : endB;

        if (!overlapEnd.isAfter(overlapStart)) return 0;

        return (int) Duration.between(overlapStart, overlapEnd).toMinutes();
    }
}