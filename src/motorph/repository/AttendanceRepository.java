package motorph.repository;

import java.util.List;

import motorph.model.AttendanceRecord;

public interface AttendanceRepository {

    // Get all attendance records of a specific employee
    List<AttendanceRecord> findByEmployeeId(String employeeId);

    // Get records of an employee for a specific month/year (for payslip period)
    List<AttendanceRecord> findByEmployeeIdAndMonth(String employeeId, int year, int month);
}