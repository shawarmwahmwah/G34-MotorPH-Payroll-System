package motorph.repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import motorph.model.UserAccount;

public class CsvUserRepository {

    private static final String FILE_PATH = "data/user.csv";

    public List<UserAccount> loadUsers() {

        List<UserAccount> users = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {

            String line;

            // Skip header
            br.readLine();

            while ((line = br.readLine()) != null) {

                String[] parts = line.split(",");

                String username = parts[0];
                String password = parts[1];
                String role = parts[2];
                String employeeId = parts[3];
                int failedAttempts = Integer.parseInt(parts[4]);
                boolean locked = Boolean.parseBoolean(parts[5]);

                users.add(new UserAccount(
                        username,
                        password,
                        role,
                        employeeId,
                        failedAttempts,
                        locked));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public void saveUsers(List<UserAccount> users) {

        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {

            // Write header
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