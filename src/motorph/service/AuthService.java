package motorph.service;

import java.util.List;

import motorph.model.UserAccount;
import motorph.repository.CsvUserRepository;

public class AuthService {

    private CsvUserRepository repo;

    public AuthService(CsvUserRepository repo) {
        this.repo = repo;
    }

    public UserAccount login(String username, String password) {

        List<UserAccount> users = repo.loadUsers();

        for (UserAccount user : users) {

            if (user.getUsername().equals(username)) {

                // Check if account is locked
                if (user.isLocked()) {
                    System.out.println("Account is locked. Please contact IT for further assistance.");
                    return null;
                }

                // Correct password
                if (user.getPassword().equals(password)) {

                    user.resetAttempts();
                    repo.saveUsers(users);

                    return user;
                }

                // Wrong password
                int attempts = user.getFailedAttempts() + 1;
                user.setFailedAttempts(attempts);

                if (attempts >= 3) {
                    user.setLocked(true);
                    System.out.println("Account locked after 3 failed attempts.");
                } else {
                    System.out.println("Wrong password (" + attempts + "/3)");
                    System.out.println("Acccount will be locked after third failed attempt.");
                }

                repo.saveUsers(users);
                return null;
            }
        }

        System.out.println("User not found.");
        return null;
    }
}