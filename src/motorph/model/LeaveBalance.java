package motorph.model;

public class LeaveBalance {

    private final String employeeId;
    private double sickLeave;
    private double vacationLeave;
    private double maternityLeave;
    private double paternityLeave;
    private double bereavementLeave;

    public LeaveBalance(
            String employeeId,
            double sickLeave,
            double vacationLeave,
            double maternityLeave,
            double paternityLeave,
            double bereavementLeave
    ) {
        this.employeeId = employeeId;
        this.sickLeave = sickLeave;
        this.vacationLeave = vacationLeave;
        this.maternityLeave = maternityLeave;
        this.paternityLeave = paternityLeave;
        this.bereavementLeave = bereavementLeave;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public double getSickLeave() {
        return sickLeave;
    }

    public double getVacationLeave() {
        return vacationLeave;
    }

    public double getMaternityLeave() {
        return maternityLeave;
    }

    public double getPaternityLeave() {
        return paternityLeave;
    }

    public double getBereavementLeave() {
        return bereavementLeave;
    }

    public void setSickLeave(double sickLeave) {
        this.sickLeave = sickLeave;
    }

    public void setVacationLeave(double vacationLeave) {
        this.vacationLeave = vacationLeave;
    }

    public void setMaternityLeave(double maternityLeave) {
        this.maternityLeave = maternityLeave;
    }

    public void setPaternityLeave(double paternityLeave) {
        this.paternityLeave = paternityLeave;
    }

    public void setBereavementLeave(double bereavementLeave) {
        this.bereavementLeave = bereavementLeave;
    }
}