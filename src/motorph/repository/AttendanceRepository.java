package motorph.repository;

import java.time.LocalDate;
import java.util.List;
import motorph.model.AttendanceRecord;

public interface AttendanceRepository {

    // Get all attendance records of a specific employee
    List<AttendanceRecord> findByEmployeeId(String employeeId);

    // Get records of an employee for a specific month/year
    List<AttendanceRecord> findByEmployeeIdAndMonth(String employeeId, int year, int month);

    // Get one record of an employee for a specific date
    AttendanceRecord findByEmployeeIdAndDate(String employeeId, LocalDate date);
}