package motorph.service;

import motorph.model.Employee;
import motorph.repository.ActivityLogRepository;
import motorph.repository.EmployeeRepository;

/*
 * AuthService
 *
 * Handles:
 * - login authentication from employees.csv data
 * - role-based authorization checks for admin operations
 */
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final ActivityLogRepository logRepo = new ActivityLogRepository();

    // Store the latest login result message for GUI display
    private String lastLoginMessage = "";

    public AuthService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /*
     * Login method used by console or GUI.
     * username input is now EmployeeID.
     */
    public Employee login(String username, String password) {

        if (isBlank(username) || isBlank(password)) {
            lastLoginMessage = "Invalid username or password, please try again.";
            return null;
        }

        String employeeIdAsUsername = username.trim();
        Employee employee = employeeRepository.findById(employeeIdAsUsername);

        if (employee == null) {
            lastLoginMessage = "Invalid username or password, please try again.";
            return null;
        }

        if (!employeeIdAsUsername.equals(employee.getUsername())) {
            lastLoginMessage = "Invalid account configuration. Username must match Employee ID.";
            logRepo.log(employee.getEmployeeId(), "LOGIN_CONFIG_ERROR", "Username does not match Employee ID");
            return null;
        }

        if (!password.equals(employee.getPassword())) {
            lastLoginMessage = "Invalid username or password, please try again.";
            logRepo.log(employee.getEmployeeId(), "LOGIN_FAILED", "Wrong password attempt");
            return null;
        }

        lastLoginMessage = "Login successful.";
        logRepo.log(employee.getEmployeeId(), "LOGIN_SUCCESS", "User logged in");

        return employee;
    }

    public String unlockAccount(String actorEmployeeId, String targetEmployeeId) {
        Employee actor = employeeRepository.findById(actorEmployeeId);

        if (actor == null) {
            return "Error: Unlocking user not found.";
        }

        if (!isAuthorizedUnlocker(actor)) {
            return "Error: Only Admin or IT can unlock accounts.";
        }

        Employee target = employeeRepository.findById(targetEmployeeId);
        if (target == null) {
            return "Error: Target account not found.";
        }

        // Lockout persistence was removed in the employee-based authentication migration.
        logRepo.log(actorEmployeeId, "ACCOUNT_UNLOCK_DEPRECATED", "Unlock requested for employeeId " + targetEmployeeId);

        return "Account lock/unlock is deprecated in the employee-based login design.";
    }

    /*
     * Returns the last login message.
     * Useful for Swing popups or labels.
     */
    public String getLastLoginMessage() {
        return lastLoginMessage;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isAuthorizedUnlocker(Employee employee) {
        if (employee == null || employee.getRole() == null) {
            return false;
        }

        String role = employee.getRole();
        return "admin".equalsIgnoreCase(role)
                || "it".equalsIgnoreCase(role);
    }
}
