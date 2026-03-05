package motorph.repository;

import motorph.model.UserAccount;

public interface UserRepository {
    UserAccount findByUsername(String username);
}