package motorph.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
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
        List<Employee> employees = findAll();

        for (Employee employee : employees) {
            if (employee.getEmployeeId().equals(employeeId)) {
                return employee;
            }
        }

        return null;
    }

    @Override
    public List<Employee> findAll() {

        List<Employee> employees = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(employeesFile.toFile()))) {

            // Read and skip header
            String header = br.readLine();
            if (header == null) {
                return employees;
            }

            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                // Safe CSV split because some fields may contain commas
                List<String> p = CsvUtil.splitCsvLine(line);

                if (p.size() < 19) {
                    continue;
                }

                // Parse money columns safely
                double basicSalary = MoneyUtil.parseMoney2dp(p.get(13));
                double rice = MoneyUtil.parseMoney2dp(p.get(14));
                double phone = MoneyUtil.parseMoney2dp(p.get(15));
                double clothing = MoneyUtil.parseMoney2dp(p.get(16));
                double grossSemi = MoneyUtil.parseMoney2dp(p.get(17));
                double hourly = MoneyUtil.parseMoney2dp(p.get(18));

                Employee employee = new Employee(
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

                employees.add(employee);
            }

        } catch (Exception e) {
            System.out.println("Error reading employees.csv: " + e.getMessage());
        }

        return employees;
    }
}