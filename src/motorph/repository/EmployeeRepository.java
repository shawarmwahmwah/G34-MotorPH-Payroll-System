package motorph.repository;

import java.util.List;
import motorph.model.Employee;

public interface EmployeeRepository {

    // Find 1 employee using employeeId
    Employee findById(String employeeId);

    // Load all employees for dropdown/search usage in GUI
    List<Employee> findAll();
}