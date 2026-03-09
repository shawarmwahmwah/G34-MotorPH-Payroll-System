package motorph.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import motorph.model.UserAccount;
import motorph.util.PathHelper;

public class CsvUserRepository {

    // Use PathHelper so we avoid hardcoded paths
    private final Path filePath;

    public CsvUserRepository() {
        this.filePath = PathHelper.getDataFile("user.csv");
    }

    /*
     * Load all users from user.csv
     */
    public List<UserAccount> loadUsers() {

        List<UserAccount> users = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {

            String line;

            // Skip header row
            br.readLine();

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                // Simple split is okay here because current user.csv format is plain
                String[] parts = line.split(",", -1);

                if (parts.length < 6) {
                    continue;
                }

                String username = parts[0].trim();
                String password = parts[1].trim();
                String role = parts[2].trim();
                String employeeId = parts[3].trim();
                int failedAttempts = Integer.parseInt(parts[4].trim());
                boolean locked = Boolean.parseBoolean(parts[5].trim());

                users.add(new UserAccount(
                        username,
                        password,
                        role,
                        employeeId,
                        failedAttempts,
                        locked
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    /*
     * Save all users back to user.csv
     */
    public void saveUsers(List<UserAccount> users) {

        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath.toFile(), false))) {

            // Write header row
            pw.println("username,password,role,employeeId,failedAttempts,isLocked");

            for (UserAccount u : users) {
                pw.println(
                        u.getUsername() + "," +
                        u.getPassword() + "," +
                        u.getRole() + "," +
                        u.getEmployeeId() + "," +
                        u.getFailedAttempts() + "," +
                        u.isLocked()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}