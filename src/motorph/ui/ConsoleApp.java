package motorph.ui;

import java.util.Scanner;

import motorph.model.Employee;
import motorph.model.UserAccount;
import motorph.repository.CsvEmployeeRepository;
import motorph.repository.CsvUserRepository;
import motorph.service.AuthService;

public class ConsoleApp {

    public static void main(String[] args) {

        System.out.println("MotorPH Payroll System - Backend Login Test");
        System.out.println();
        Scanner sc = new Scanner(System.in);

        // 1) Ask for login info
        System.out.print("Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine().trim();
        System.out.println();
        // 2) Login using AuthService
        AuthService auth = new AuthService(new CsvUserRepository());
        UserAccount account = auth.login(username, password);

        if (account == null) {
            System.out.println("Login failed: Wrong username or password.");
            sc.close();
            return;
        }

        // 3) Load employee using employeeId from the account
        CsvEmployeeRepository empRepo = new CsvEmployeeRepository();
        Employee emp = empRepo.findById(account.getEmployeeId());

        if (emp == null) {
            System.out.println("Employee record not found " + account.getEmployeeId());
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
        	
        	sc.close();
    }
}