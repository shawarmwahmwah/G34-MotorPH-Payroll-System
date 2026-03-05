package motorph.service;

import java.util.List;

import motorph.model.UserAccount;
import motorph.repository.ActivityLogRepository;
import motorph.repository.CsvUserRepository;

/*Features checklist natin: 
 * Handles login authentication + lockout rule:
 * - Wrong password increments failedAttempts
 * - On 3rd failed attempt, account becomes locked
 * - Locked account cannot login unless IT unlocks it (future feature)
 */
public class AuthService {

    private final CsvUserRepository repo;
    private final ActivityLogRepository logRepo = new ActivityLogRepository();

    public AuthService(CsvUserRepository repo) {
        this.repo = repo;
    }

    public UserAccount login(String username, String password) {

        List<UserAccount> users = repo.loadUsers();

        for (UserAccount user : users) {

            // Find the user by username
            if (user.getUsername().equals(username)) {

                // 1) If locked, block login
                if (user.isLocked()) {
                    System.out.println("Account is locked. Please contact IT for further assistance.");
                    logRepo.log(user.getEmployeeId(), "LOGIN_BLOCKED", "Login blocked because account is locked");
                    return null;
                }

                // 2) If password is correct, reset attempts and allow login
                if (user.getPassword().equals(password)) {

                    user.resetAttempts();        // back to 0
                    repo.saveUsers(users);       // save reset attempts to CSV
                    logRepo.log(user.getEmployeeId(), "LOGIN_SUCCESS", "User logged in");

                    return user;
                }

                // 3) Wrong password: increase attempts
                int attempts = user.getFailedAttempts() + 1;
                user.setFailedAttempts(attempts);

                // Lock the account ON the 3rd failed attempt
                if (attempts >= 3) {
                    user.setLocked(true);
                    repo.saveUsers(users);

                    logRepo.log(user.getEmployeeId(), "ACCOUNT_LOCKED", "Account locked after 3 failed attempts");
                    System.out.println("Account locked after 3 failed attempts.");

                } else {
                    repo.saveUsers(users);

                    logRepo.log(user.getEmployeeId(), "LOGIN_FAILED", "Wrong password attempt (" + attempts + "/3)");
                    System.out.println("Wrong password (" + attempts + "/3)");
                    System.out.println("Account will be locked after 3 failed attempts.");
                }

                return null;
            }
        }

        // Username not found (no employeeId to log)
        System.out.println("User not found.");
        return null;
    }
}