package motorph.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    public Employee findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        String target = username.trim();

        for (Employee employee : findAll()) {
            // Username is now strictly the same as EmployeeID
            if (target.equalsIgnoreCase(employee.getEmployeeId())) {
                return employee;
            }
        }

        return null;
    }

    @Override
    public List<Employee> findAll() {

        List<Employee> employees = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(employeesFile.toFile()))) {

            // Read and map header positions for backward compatibility
            String headerLine = br.readLine();
            if (headerLine == null) {
                return employees;
            }

            List<String> headerColumns = CsvUtil.splitCsvLine(headerLine);
            Map<String, Integer> indexByName = new HashMap<>();

            for (int i = 0; i < headerColumns.size(); i++) {
                indexByName.put(normalizeHeader(headerColumns.get(i)), i);
            }

            int idxEmployeeId = findIndex(indexByName, "EmployeeID", 0);
            int idxLastName = findIndex(indexByName, "Last Name", 1);
            int idxFirstName = findIndex(indexByName, "First Name", 2);
            int idxBirthday = findIndex(indexByName, "Birthday", 3);
            int idxAddress = findIndex(indexByName, "Address", 4);
            int idxPhone = findIndex(indexByName, "Phone Number", 5);
            int idxSss = findIndex(indexByName, "SSS #", 6);
            int idxPhilhealth = findIndex(indexByName, "Philhealth #", 7);
            int idxTin = findIndex(indexByName, "TIN #", 8);
            int idxPagibig = findIndex(indexByName, "Pag-ibig #", 9);
            int idxStatus = findIndex(indexByName, "Status", 10);
            int idxPosition = findIndex(indexByName, "Position", 11);
            int idxSupervisor = findIndex(indexByName, "Immediate Supervisor", 12);
            int idxBasic = findIndex(indexByName, "Basic Salary", 13);
            int idxRice = findIndex(indexByName, "Rice Subsidy", 14);
            int idxPhoneAllowance = findIndex(indexByName, "Phone Allowance", 15);
            int idxClothing = findIndex(indexByName, "Clothing Allowance", 16);
            int idxGrossSemi = findIndex(indexByName, "Gross Semi-monthly Rate", 17);
            int idxHourly = findIndex(indexByName, "Hourly Rate", 18);
            int idxUsername = findIndex(indexByName, "username", -1);
            int idxPassword = findIndex(indexByName, "password", -1);
            int idxRole = findIndex(indexByName, "role", -1);

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

                String employeeId = getValue(p, idxEmployeeId);
                if (employeeId.isEmpty()) {
                    continue;
                }

                String lastName = getValue(p, idxLastName);
                String firstName = getValue(p, idxFirstName);

                // Parse money columns safely
                double basicSalary = MoneyUtil.parseMoney2dp(getValue(p, idxBasic));
                double rice = MoneyUtil.parseMoney2dp(getValue(p, idxRice));
                double phone = MoneyUtil.parseMoney2dp(getValue(p, idxPhoneAllowance));
                double clothing = MoneyUtil.parseMoney2dp(getValue(p, idxClothing));
                double grossSemi = MoneyUtil.parseMoney2dp(getValue(p, idxGrossSemi));
                double hourly = MoneyUtil.parseMoney2dp(getValue(p, idxHourly));

                String csvUsername = getValue(p, idxUsername);
                String username = employeeId;

                if (!csvUsername.isEmpty() && !employeeId.equals(csvUsername)) {
                    System.out.println("Warning: username mismatch for employeeId " + employeeId
                            + "; forcing username to EmployeeID.");
                }

                String password = getValue(p, idxPassword);
                if (password.isEmpty()) {
                    password = "admin";
                }

                String role = getValue(p, idxRole);
                if (role.isEmpty()) {
                    role = "employee";
                }

                Employee employee = new Employee(
                        employeeId,
                        lastName,
                        firstName,
                        getValue(p, idxBirthday),
                        getValue(p, idxAddress),
                        getValue(p, idxPhone),
                        getValue(p, idxSss),
                        getValue(p, idxPhilhealth),
                        getValue(p, idxTin),
                        getValue(p, idxPagibig),
                        getValue(p, idxStatus),
                        getValue(p, idxPosition),
                        getValue(p, idxSupervisor),
                        basicSalary,
                        rice,
                        phone,
                        clothing,
                        grossSemi,
                        hourly,
                        username,
                        password,
                        role
                );

                employees.add(employee);
            }

        } catch (Exception e) {
            System.out.println("Error reading employees.csv: " + e.getMessage());
        }

        return employees;
    }

    @Override
    public void saveAll(List<Employee> employees) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(employeesFile.toFile(), false))) {
            pw.println(
                    "\"EmployeeID\",\"Last Name\",\"First Name\",\"Birthday\",\"Address\",\"Phone Number\",\"SSS #\",\"Philhealth #\",\"TIN #\",\"Pag-ibig #\",\"Status\",\"Position\",\"Immediate Supervisor\",\"Basic Salary\",\"Rice Subsidy\",\"Phone Allowance\",\"Clothing Allowance\",\"Gross Semi-monthly Rate\",\"Hourly Rate\",\"username\",\"password\",\"role\""
            );

            for (Employee e : employees) {
                String[] fields = new String[] {
                        e.getEmployeeId(),
                        e.getLastName(),
                        e.getFirstName(),
                        e.getBirthday(),
                        e.getAddress(),
                        e.getPhoneNumber(),
                        e.getSssNumber(),
                        e.getPhilhealthNumber(),
                        e.getTinNumber(),
                        e.getPagibigNumber(),
                        e.getStatus(),
                        e.getPosition(),
                        e.getImmediateSupervisor(),
                        String.valueOf(e.getBasicSalary()),
                        String.valueOf(e.getRiceSubsidy()),
                        String.valueOf(e.getPhoneAllowance()),
                        String.valueOf(e.getClothingAllowance()),
                        String.valueOf(e.getGrossSemiMonthlyRate()),
                        String.valueOf(e.getHourlyRate()),
                        e.getEmployeeId(),
                        e.getPassword(),
                        e.getRole()
                };

                StringBuilder line = new StringBuilder();
                for (int i = 0; i < fields.length; i++) {
                    if (i > 0) {
                        line.append(',');
                    }
                    line.append(toCsvValue(fields[i]));
                }

                pw.println(line);
            }
        } catch (Exception e) {
            System.out.println("Error writing employees.csv: " + e.getMessage());
        }
    }

    private int findIndex(Map<String, Integer> indexByName, String headerName, int fallback) {
        Integer index = indexByName.get(normalizeHeader(headerName));
        return index == null ? fallback : index;
    }

    private String normalizeHeader(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ENGLISH);
    }

    private String getValue(List<String> values, int index) {
        if (index < 0 || index >= values.size()) {
            return "";
        }
        String value = values.get(index);
        return value == null ? "" : value.trim();
    }

    private String toCsvValue(String value) {
        String safe = value == null ? "" : value;
        return '"' + safe.replace("\"", "\"\"") + '"';
    }
}