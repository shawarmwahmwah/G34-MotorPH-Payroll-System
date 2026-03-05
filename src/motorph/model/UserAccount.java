package motorph.model;

public class UserAccount {

    private String username;
    private String password;
    private String role;
    private String employeeId;

    // Track login attempts
    private int failedAttempts;

    //user cannot login once nafail na
    private boolean locked;

    public UserAccount(String username,
                       String password,
                       String role,
                       String employeeId,
                       int failedAttempts,
                       boolean locked) {

        this.username = username;
        this.password = password;
        this.role = role;
        this.employeeId = employeeId;
        this.failedAttempts = failedAttempts;
        this.locked = locked;
    }

    public String getUsername() { return username; }

    public String getPassword() { return password; }

    public String getRole() { return role; }

    public String getEmployeeId() { return employeeId; }

    public int getFailedAttempts() { return failedAttempts; }

    public boolean isLocked() { return locked; }


    public void setFailedAttempts(int attempts) {
        this.failedAttempts = attempts;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /*
     * Reset attempts after successful login
     */
    public void resetAttempts() {
        this.failedAttempts = 0;
    }

}