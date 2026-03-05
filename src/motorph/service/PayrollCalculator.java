package motorph.service;

import motorph.model.Employee;

public class PayrollCalculator {
 
     //Regular Pay = Regular Hours × Hourly Rate
    public double computeRegularPay(double regularHours, double hourlyRate) {

        return regularHours * hourlyRate;
    }

    //overtime pay
    public double computeOvertimePay(double overtimeHours, double hourlyRate) {

        double overtimeRate = hourlyRate * 1.25;

        return overtimeHours * overtimeRate;
    }

    //late deductions
    public double computeLateDeduction(double lateHours, double hourlyRate) {

        return lateHours * hourlyRate;
    }

    //undertime caclulation
    public double computeUndertimeDeduction(double undertimeHours, double hourlyRate) {

        return undertimeHours * hourlyRate;
    }

    //computation ng grosspay
    public double computeGrossPay(
            double regularPay,
            double overtimePay,
            double lateDeduction,
            double undertimeDeduction,
            double allowances) {

        return regularPay
                + overtimePay
                - lateDeduction
                - undertimeDeduction
                + allowances;
    }

}