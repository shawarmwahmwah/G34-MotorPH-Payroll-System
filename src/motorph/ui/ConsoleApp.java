package motorph.ui;

import java.util.List;
import java.util.Scanner;

import motorph.model.AttendanceRecord;
import motorph.model.Employee;
import motorph.model.UserAccount;
import motorph.repository.CsvAttendanceRepository;
import motorph.repository.CsvEmployeeRepository;
import motorph.repository.CsvUserRepository;
import motorph.service.AuthService;

/*
 * ConsoleApp
 *
 * This class is ONLY for backend testing.
 * Later, the GUI login screen will replace this.
 *
 * Purpose:
 * 1) Test login system
 * 2) Load employee information
 * 3) Load attendance records
 * 4) Compute attendance summary for a payroll period
 */
public class ConsoleApp {

    public static void main(String[] args) {

        System.out.println("MotorPH Payroll System - Backend Login Test");
        System.out.println();

        Scanner sc = new Scanner(System.in);

        System.out.print("Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        System.out.println();

        // AuthService checks credentials using user.cs
        AuthService auth = new AuthService(new CsvUserRepository());

        // Attempt login
        UserAccount account = auth.login(username, password);

        // If login failed, stop the program
        if (account == null) {
            System.out.println("Login failed: Wrong username or password.");
            System.out.println("Please try again.");
            sc.close();
            return;
        }
        
        // Employee repository reads employees.csv
        CsvEmployeeRepository empRepo = new CsvEmployeeRepository();

        // Find employee using employeeId from the login account
        Employee emp = empRepo.findById(account.getEmployeeId());

        if (emp == null) {
            System.out.println("Employee record not found for ID: " + account.getEmployeeId());
            sc.close();
            return;
        }

        System.out.println("Login successful!");
        System.out.println("Welcome, " + emp.getFirstName() + " " + emp.getLastName() + ".");
        System.out.println();

        System.out.println("Employee Information");
        System.out.println();

        System.out.printf("%-16s : %s%n", "Name", emp.getFullName());
        System.out.printf("%-16s : %s%n", "Position", emp.getPosition());
        System.out.printf("%-16s : %s%n", "Status", emp.getStatus());
        System.out.printf("%-16s : %.2f%n", "Hourly Rate", emp.getHourlyRate());
        System.out.printf("%-16s : %.2f%n", "Allowances Total", emp.getTotalAllowances());

        System.out.println();

        // Ask user which payroll period to compute
        System.out.print("Enter Year (example 2024): ");
        int year = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Enter Month (1-12): ");
        int month = Integer.parseInt(sc.nextLine().trim());

        System.out.println();

        // Attendance repository reads employee_attendance.csv
        CsvAttendanceRepository attRepo = new CsvAttendanceRepository();

        // Load only the records for that employee and month
        List<AttendanceRecord> records =
                attRepo.findByEmployeeIdAndMonth(emp.getEmployeeId(), year, month);

        // If no records found
        if (records.isEmpty()) {

            System.out.println("No attendance records found for " + month + "/" + year + ".");

        } else {

            int totalWorked = 0;
            int totalRegular = 0;
            int totalOvertime = 0;
            int totalLate = 0;
            int totalUndertime = 0;

            // Loop through attendance records and sum values
            for (AttendanceRecord r : records) {

                totalWorked += r.getWorkedMinutes();
                totalRegular += r.getRegularMinutes();
                totalOvertime += r.getOvertimeMinutesRounded();
                totalLate += r.getLateMinutesRounded();
                totalUndertime += r.getUndertimeMinutesRounded();
            }

            System.out.println("Attendance Summary (" + month + "/" + year + ")");
            System.out.println();

            System.out.printf("%-16s : %.2f%n", "Worked Hours", totalWorked / 60.0);
            System.out.printf("%-16s : %.2f%n", "Regular Hours", totalRegular / 60.0);
            System.out.printf("%-16s : %.2f%n", "Overtime Hours", totalOvertime / 60.0);
            System.out.printf("%-16s : %.2f%n", "Late Hours", totalLate / 60.0);
            System.out.printf("%-16s : %.2f%n", "Undertime Hours", totalUndertime / 60.0);
            System.out.printf("%-16s : %d%n", "Days Counted", records.size());
        }

        sc.close();
    }
}