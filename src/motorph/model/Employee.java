package motorph.model;

/**
 * Employee model (DATA OBJECT).
 * - Stores employee info from employees.csv
 * - NO CSV reading here
 * - NO GUI here
 */
public class Employee {

    // pacheck kung nandito lahat ng info sa employees.csv
    private final String employeeId;
    private final String lastName;
    private final String firstName;
    private final String birthday; // keep as String for now (MM/DD/YYYY)
    private final String address;

    // eto yong govt contri tsaka contact info
    private final String phoneNumber;
    private final String sssNumber;
    private final String philhealthNumber;
    private final String tinNumber;
    private final String pagibigNumber;

    // employment details eto
    private final String status; // Regular / Probationary
    private final String position;
    private final String immediateSupervisor;

    // Money fields (double for computation)
    private final double basicSalary;
    private final double riceSubsidy;
    private final double phoneAllowance;
    private final double clothingAllowance;
    private final double grossSemiMonthlyRate;
    private final double hourlyRate;

    // Authentication fields now sourced from employees.csv
    private String username;
    private String password;
    private String role;

    public Employee(
            String employeeId,
            String lastName,
            String firstName,
            String birthday,
            String address,
            String phoneNumber,
            String sssNumber,
            String philhealthNumber,
            String tinNumber,
            String pagibigNumber,
            String status,
            String position,
            String immediateSupervisor,
            double basicSalary,
            double riceSubsidy,
            double phoneAllowance,
            double clothingAllowance,
            double grossSemiMonthlyRate,
                double hourlyRate,
                String username,
                String password,
                String role
    ) {
        this.employeeId = employeeId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.sssNumber = sssNumber;
        this.philhealthNumber = philhealthNumber;
        this.tinNumber = tinNumber;
        this.pagibigNumber = pagibigNumber;
        this.status = status;
        this.position = position;
        this.immediateSupervisor = immediateSupervisor;
        this.basicSalary = basicSalary;
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
        this.grossSemiMonthlyRate = grossSemiMonthlyRate;
        this.hourlyRate = hourlyRate;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters (read-only)
    public String getEmployeeId() { return employeeId; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getBirthday() { return birthday; }
    public String getAddress() { return address; }

    public String getPhoneNumber() { return phoneNumber; }
    public String getSssNumber() { return sssNumber; }
    public String getPhilhealthNumber() { return philhealthNumber; }
    public String getTinNumber() { return tinNumber; }
    public String getPagibigNumber() { return pagibigNumber; }

    public String getStatus() { return status; }
    public String getPosition() { return position; }
    public String getImmediateSupervisor() { return immediateSupervisor; }

    public double getBasicSalary() { return basicSalary; }
    public double getRiceSubsidy() { return riceSubsidy; }
    public double getPhoneAllowance() { return phoneAllowance; }
    public double getClothingAllowance() { return clothingAllowance; }
    public double getGrossSemiMonthlyRate() { return grossSemiMonthlyRate; }
    public double getHourlyRate() { return hourlyRate; }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }

    // Convenience helper: total allowances
    public double getTotalAllowances() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }

    // Convenience helper: "Last, First"
    public String getFullName() {
        return lastName + ", " + firstName;
    }

    @Override
    public String toString() {
        return "Employee{"
                + "employeeId='" + employeeId + '\''
                + ", fullName='" + getFullName() + '\''
                + ", position='" + position + '\''
                + ", role='" + role + '\''
                + ", status='" + status + '\''
                + "}";
    }
}
