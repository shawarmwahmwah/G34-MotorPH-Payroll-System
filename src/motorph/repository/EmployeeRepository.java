package motorph.repository;

import motorph.model.Employee;

public interface EmployeeRepository {

    // Find 1 employee using employeeId
    Employee findById(String employeeId);
}