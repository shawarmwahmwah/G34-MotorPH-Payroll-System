package motorph.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import motorph.model.LeaveBalance;
import motorph.util.PathHelper;

public class LeaveBalanceRepository {

    // Use PathHelper so file access is consistent with the project standard
    private final Path filePath;

    public LeaveBalanceRepository() {
        this.filePath = PathHelper.getDataFile("leave_balances.csv");
    }

    /*
     * Find one employee's leave balance record.
     */
    public LeaveBalance findByEmployeeId(String employeeId) {
        List<LeaveBalance> balances = loadAll();

        for (LeaveBalance balance : balances) {
            if (balance.getEmployeeId().equals(employeeId)) {
                return balance;
            }
        }

        return null;
    }

    /*
     * Load all leave balance rows from CSV.
     */
    public List<LeaveBalance> loadAll() {
        List<LeaveBalance> balances = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {

            String line;

            // Skip header row
            br.readLine();

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] p = line.split(",", -1);

                if (p.length < 6) {
                    continue;
                }

                LeaveBalance balance = new LeaveBalance(
                        p[0].trim(),
                        Double.parseDouble(p[1].trim()),
                        Double.parseDouble(p[2].trim()),
                        Double.parseDouble(p[3].trim()),
                        Double.parseDouble(p[4].trim()),
                        Double.parseDouble(p[5].trim())
                );

                balances.add(balance);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return balances;
    }

    /*
     * Save the full leave balance list back to CSV.
     * We rewrite the file so updated balances replace old values.
     */
    public void saveAll(List<LeaveBalance> balances) {
        try (FileWriter fw = new FileWriter(filePath.toFile(), false)) {

            // Write header
            fw.write("employeeId,sickLeave,vacationLeave,maternityLeave,paternityLeave,bereavementLeave\n");

            for (LeaveBalance balance : balances) {
                fw.write(
                        balance.getEmployeeId() + "," +
                        balance.getSickLeave() + "," +
                        balance.getVacationLeave() + "," +
                        balance.getMaternityLeave() + "," +
                        balance.getPaternityLeave() + "," +
                        balance.getBereavementLeave() + "\n"
                );
            }

            fw.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}