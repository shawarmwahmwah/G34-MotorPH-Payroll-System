package motorph.service;

import motorph.model.UserAccount;
import motorph.repository.UserRepository;

public class AuthService {

    private final UserRepository userRepo;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public UserAccount login(String username, String password) {
        UserAccount account = userRepo.findByUsername(username);
        if (account == null) return null;

        // simple check (school-safe)
        if (!account.getPassword().equals(password)) return null;

        return account;
    }
}