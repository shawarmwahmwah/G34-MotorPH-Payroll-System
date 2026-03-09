package motorph.service;

import java.util.List;

import motorph.model.UserAccount;
import motorph.repository.ActivityLogRepository;
import motorph.repository.CsvUserRepository;

/*
 * AuthService
 *
 * Handles:
 * - login authentication
 * - failed login counting
 * - lockout after 3 failed attempts
 * - unlock account feature for Admin / IT
 */
public class AuthService {

    private final CsvUserRepository repo;
    private final ActivityLogRepository logRepo = new ActivityLogRepository();

    // Store the latest login result message for GUI display
    private String lastLoginMessage = "";

    public AuthService(CsvUserRepository repo) {
        this.repo = repo;
    }

    /*
     * Login method used by console or GUI.
     */
    public UserAccount login(String username, String password) {

        List<UserAccount> users = repo.loadUsers();

        for (UserAccount user : users) {

            // Find the user by username
            if (user.getUsername().equals(username)) {

                // 1) If account is locked, block login
            	if (user.isLocked()) {

            	    // Account is already locked, so block the login immediately
            	    lastLoginMessage = "Account is locked. Please contact your IT/Admin.";

            	    // Save activity log
            	    logRepo.log(
            	            user.getEmployeeId(),
            	            "LOGIN_BLOCKED",
            	            "Login blocked because account is locked"
            	    );

            	    return null;
            	}
            	
                // 2) If password is correct, reset attempts and allow login
                if (user.getPassword().equals(password)) {

                    // Reset failed attempts after successful login
                    user.resetAttempts();
                    repo.saveUsers(users);

                    lastLoginMessage = "Login successful.";
                    logRepo.log(user.getEmployeeId(), "LOGIN_SUCCESS", "User logged in");

                    return user;
                }

                // 3) Wrong password: increase attempts
                int attempts = user.getFailedAttempts() + 1;
                user.setFailedAttempts(attempts);

                // Lock account on the 3rd failed attempt
                if (attempts >= 3) {

                    // Lock the account on the 3rd failed attempt
                    user.setLocked(true);
                    repo.saveUsers(users);

                    // Store a GUI-friendly message
                    lastLoginMessage = "Account is locked. Please contact your IT/Admin.";

                    // Save activity log
                    logRepo.log(
                            user.getEmployeeId(),
                            "ACCOUNT_LOCKED",
                            "Account locked after 3 failed attempts"
                    );

                } else {

                    // Save updated failed-attempt count
                    repo.saveUsers(users);

                    // Generic retry message for attempts below 3
                    lastLoginMessage = "Invalid username or password, please try again.";

                    // Save activity log
                    logRepo.log(
                            user.getEmployeeId(),
                            "LOGIN_FAILED",
                            "Wrong password attempt (" + attempts + "/3)"
                    );
                }
                return null;
            }
        }

        // Username not found
        lastLoginMessage = "User not found.";
        return null;
    }

    /*
     * Returns the last login message.
     * Useful for Swing popups or labels.
     */
    public String getLastLoginMessage() {
        return lastLoginMessage;
    }

    /*
     * Unlock a locked account.
     *
     * Current compatibility:
     * - admin
     * - it
     *
     * This keeps your current dataset usable even if it still uses "admin".
     */
    public String unlockAccount(String actorEmployeeId, String targetEmployeeId) {

        List<UserAccount> users = repo.loadUsers();

        // Check if actor has authority to unlock
        UserAccount actor = findByEmployeeId(users, actorEmployeeId);
        if (actor == null) {
            return "Error: Unlocking user not found.";
        }

        if (!isAuthorizedUnlocker(actor)) {
            return "Error: Only Admin or IT can unlock accounts.";
        }

        // Find target account
        UserAccount target = findByEmployeeId(users, targetEmployeeId);
        if (target == null) {
            return "Error: Target account not found.";
        }

        // If already unlocked, tell the user
        if (!target.isLocked()) {
            return "Account is already unlocked.";
        }

        // Unlock and reset failed attempts
        target.setLocked(false);
        target.setFailedAttempts(0);

        // Save updated list
        repo.saveUsers(users);

        // Save activity logs
        logRepo.log(actorEmployeeId, "ACCOUNT_UNLOCKED", "Unlocked account for employeeId " + targetEmployeeId);
        logRepo.log(targetEmployeeId, "ACCOUNT_UNLOCKED_BY_IT", "Account unlocked by authorized personnel");

        return "Account unlocked successfully for employee ID: " + targetEmployeeId;
    }

    /*
     * Helper: find a user by employeeId
     */
    private UserAccount findByEmployeeId(List<UserAccount> users, String employeeId) {
        for (UserAccount user : users) {
            if (user.getEmployeeId().equals(employeeId)) {
                return user;
            }
        }
        return null;
    }

    /*
     * Helper: check if user role is allowed to unlock accounts
     */
    private boolean isAuthorizedUnlocker(UserAccount user) {
        String role = user.getRole();

        if (role == null) {
            return false;
        }

        return "admin".equalsIgnoreCase(role)
                || "it".equalsIgnoreCase(role);
    }
}