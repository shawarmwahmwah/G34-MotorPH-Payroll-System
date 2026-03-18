package motorph.service;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import motorph.util.CsvUtil;
import motorph.util.PathHelper;

public class PayrollCalculator {

    private static final double DEFAULT_OVERTIME_MULTIPLIER = 1.25;
 
     //Regular Pay = Regular Hours × Hourly Rate
    public double computeRegularPay(double regularHours, double hourlyRate) {

        return regularHours * hourlyRate;
    }

    //overtime pay
    public double computeOvertimePay(double overtimeHours, double hourlyRate) {

        double overtimeRate = hourlyRate * loadOvertimeMultiplier();

        return overtimeHours * overtimeRate;
    }

    private double loadOvertimeMultiplier() {
        Path settingsFile = PathHelper.getDataFile("payroll_config.csv");

        if (!Files.exists(settingsFile)) {
            return DEFAULT_OVERTIME_MULTIPLIER;
        }

        try (BufferedReader br = Files.newBufferedReader(settingsFile)) {
            String header = br.readLine();
            if (header == null) {
                return DEFAULT_OVERTIME_MULTIPLIER;
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                java.util.List<String> values = CsvUtil.splitCsvLine(line);
                if (values.size() < 2) {
                    continue;
                }

                String key = values.get(0).trim();
                String value = values.get(1).trim();

                if ("overtimeMultiplier".equalsIgnoreCase(key)) {
                    double parsed = Double.parseDouble(value);
                    return parsed > 0 ? parsed : DEFAULT_OVERTIME_MULTIPLIER;
                }
            }
        } catch (Exception e) {
            System.out.println("[PayrollCalculator] Unable to read payroll_config.csv: " + e.getMessage());
        }

        return DEFAULT_OVERTIME_MULTIPLIER;
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