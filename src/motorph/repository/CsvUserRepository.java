package motorph.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;

import motorph.model.UserAccount;
import motorph.util.CsvUtil;
import motorph.util.PathHelper;

public class CsvUserRepository implements UserRepository {

    private final Path userFile;

    public CsvUserRepository() {
        this.userFile = PathHelper.getDataFile("user.csv");
    }

    @Override
    public UserAccount findByUsername(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(userFile.toFile()))) {
            String line = br.readLine(); // header
            if (line == null) return null;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // user.csv is not quoted, but this works either way
                var parts = CsvUtil.splitCsvLine(line);
                if (parts.size() < 4) continue;

                String fileUsername = parts.get(0);
                String password = parts.get(1);
                String role = parts.get(2);
                String employeeId = parts.get(3);

                if (fileUsername.equalsIgnoreCase(username)) {
                    return new UserAccount(fileUsername, password, role, employeeId);
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading user.csv: " + e.getMessage());
        }
        return null;
    }
}