package motorph.ui.session;

import motorph.model.Employee;

/**
 * UserSession
 *
 * Stores the currently logged-in employee details.
 */
public class UserSession {

    private final Employee employee;

    public UserSession(Employee employee) {
        this.employee = employee;
    }

    public Employee getEmployee() {
        return employee;
    }

    public String getEmployeeName() {
        return employee.getFullName();
    }

    public String getEmployeeId() {
        return employee.getEmployeeId();
    }

    public String getPosition() {
        return employee.getPosition();
    }

    public String getRole() {
        return employee.getRole();
    }
}