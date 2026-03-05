package motorph.model;

public class UserAccount {
    private final String username;
    private final String password; 
    private final String role;
    private final String employeeId;

    public UserAccount(String username, String password, String role, String employeeId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.employeeId = employeeId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getEmployeeId() {
        return employeeId;
    }
}