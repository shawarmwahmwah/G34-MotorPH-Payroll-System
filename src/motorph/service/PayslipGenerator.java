package motorph.service;

import motorph.model.Employee;

public class PayslipGenerator {

    public void printPayslip(
            Employee emp,
            int month,
            int year,
            double regularPay,
            double overtimePay,
            double allowances,
            double lateDeduction,
            double undertimeDeduction,
            double sss,
            double philhealth,
            double pagibig,
            double tax,
            double netPay) {

        System.out.println();
        System.out.println("==============================================");
        System.out.println("               MOTORPH PAYSLIP               ");
        System.out.println("==============================================");
        System.out.println();

        System.out.printf("%-20s : %s%n", "Employee", emp.getFullName());
        System.out.printf("%-20s : %s%n", "Position", emp.getPosition());
        System.out.printf("%-20s : %02d/%d%n", "Payroll Period", month, year);

        System.out.println();
        System.out.println("--------------- EARNINGS --------------------");

        System.out.printf("%-20s : %.2f%n", "Regular Pay", regularPay);
        System.out.printf("%-20s : %.2f%n", "Overtime Pay", overtimePay);
        System.out.printf("%-20s : %.2f%n", "Allowances", allowances);

        System.out.println();
        System.out.println("-------------- DEDUCTIONS -------------------");

        System.out.printf("%-20s : %.2f%n", "Late Deduction", lateDeduction);
        System.out.printf("%-20s : %.2f%n", "Undertime Deduction", undertimeDeduction);
        System.out.printf("%-20s : %.2f%n", "SSS", sss);
        System.out.printf("%-20s : %.2f%n", "PhilHealth", philhealth);
        System.out.printf("%-20s : %.2f%n", "Pag-IBIG", pagibig);
        System.out.printf("%-20s : %.2f%n", "Withholding Tax", tax);

        System.out.println();
        System.out.println("----------------------------------------------");

        System.out.printf("%-20s : %.2f%n", "NET PAY", netPay);

        System.out.println("----------------------------------------------");
        System.out.println();
    }
}