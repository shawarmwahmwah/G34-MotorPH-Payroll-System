package motorph.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.List;

import motorph.model.Employee;
import motorph.util.CsvUtil;
import motorph.util.MoneyUtil;
import motorph.util.PathHelper;

public class CsvEmployeeRepository implements EmployeeRepository {

    private final Path employeesFile;

    public CsvEmployeeRepository() {
        this.employeesFile = PathHelper.getDataFile("employees.csv");
    }

    @Override
    public Employee findById(String employeeId) {

        try (BufferedReader br = new BufferedReader(new FileReader(employeesFile.toFile()))) {

            // 1) Read header line and ignore it
            String header = br.readLine();
            if (header == null) return null;

            // 2) Read each row
            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                // employees.csv has quotes, so need natin ng CsvUtil splitter
                List<String> p = CsvUtil.splitCsvLine(line);

                // ang bilang ko is 19 columns, pacheck dito
                if (p.size() < 19) continue;

                // Column 0 = EmployeeID
                String id = p.get(0);

                // Only return the row that matches the requested employeeId
                if (!id.equals(employeeId)) continue;

                // Parse money columns with 2 decimal rounding
                double basicSalary = MoneyUtil.parseMoney2dp(p.get(13));
                double rice = MoneyUtil.parseMoney2dp(p.get(14));
                double phone = MoneyUtil.parseMoney2dp(p.get(15));
                double clothing = MoneyUtil.parseMoney2dp(p.get(16));
                double grossSemi = MoneyUtil.parseMoney2dp(p.get(17));
                double hourly = MoneyUtil.parseMoney2dp(p.get(18));

                // Create Employee object and return it
                return new Employee(
                        p.get(0),  // EmployeeID
                        p.get(1),  // Last Name
                        p.get(2),  // First Name
                        p.get(3),  // Birthday
                        p.get(4),  // Address
                        p.get(5),  // Phone Number
                        p.get(6),  // SSS #
                        p.get(7),  // Philhealth #
                        p.get(8),  // TIN #
                        p.get(9),  // Pag-ibig #
                        p.get(10), // Status
                        p.get(11), // Position
                        p.get(12), // Immediate Supervisor
                        basicSalary,
                        rice,
                        phone,
                        clothing,
                        grossSemi,
                        hourly
                );
            }

        } catch (Exception e) {
            System.out.println("Error could not locate: " + e.getMessage());
        }

        // If employeeId not found
        return null;
    }
}