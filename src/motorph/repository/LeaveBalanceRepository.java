package motorph.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import motorph.model.LeaveBalance;

public class LeaveBalanceRepository {

    private static final String FILE = "data/leave_balances.csv";

    public LeaveBalance findByEmployeeId(String employeeId) {
        List<LeaveBalance> balances = loadAll();

        for (LeaveBalance balance : balances) {
            if (balance.getEmployeeId().equals(employeeId)) {
                return balance;
            }
        }

        return null;
    }

    public List<LeaveBalance> loadAll() {
        List<LeaveBalance> balances = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", -1);

                if (p.length < 6) continue;

                balances.add(new LeaveBalance(
                        p[0],
                        Double.parseDouble(p[1]),
                        Double.parseDouble(p[2]),
                        Double.parseDouble(p[3]),
                        Double.parseDouble(p[4]),
                        Double.parseDouble(p[5])
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return balances;
    }

    public void saveAll(List<LeaveBalance> balances) {
        try (FileWriter fw = new FileWriter(FILE, false)) {

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}