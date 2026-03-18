package motorph.repository;

import java.util.List;
import motorph.model.Employee;

public interface EmployeeRepository {

    // Find 1 employee using employeeId
    Employee findById(String employeeId);

    // Find 1 employee using login username
    Employee findByUsername(String username);

    // Load all employees for dropdown/search usage in GUI
    List<Employee> findAll();

    // Save all employees back to employees.csv
    void saveAll(List<Employee> employees);
}