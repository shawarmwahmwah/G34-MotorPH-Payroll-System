package motorph.ui.session;

import motorph.model.Employee;
import motorph.model.UserAccount;

/**
 * UserSession
 *
 * Stores the currently logged-in account and employee details.
 */
public class UserSession {

    private final UserAccount account;
    private final Employee employee;

    public UserSession(UserAccount account, Employee employee) {
        this.account = account;
        this.employee = employee;
    }

    public UserAccount getAccount() {
        return account;
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
        return account.getRole();
    }
}