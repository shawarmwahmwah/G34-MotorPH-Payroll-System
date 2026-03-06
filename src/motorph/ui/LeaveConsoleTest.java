package motorph.ui;

import java.util.List;
import java.util.Scanner;

import motorph.model.Employee;
import motorph.model.LeaveBalance;
import motorph.model.LeaveRequest;
import motorph.repository.CsvEmployeeRepository;
import motorph.service.LeaveService;

/*
 * LeaveConsoleTest
 *
 * This class is only for backend testing of the leave module.
 * It allows us to:
 * 1) view leave balance
 * 2) submit leave request
 * 3) approve leave request
 * 4) reject leave request
 * 5) view request history
 *
 * IMPORTANT:
 * This is not the final GUI.
 */
public class LeaveConsoleTest {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // Service object that handles leave business logic
        LeaveService leaveService = new LeaveService();

        // Repository to load employee info
        CsvEmployeeRepository empRepo = new CsvEmployeeRepository();

        System.out.println("MotorPH Leave Module - Console Test");
        System.out.println();

        // Ask which employee we are testing as requester
        System.out.print("Enter Employee ID: ");
        String employeeId = sc.nextLine().trim();

        // Load employee record
        Employee emp = empRepo.findById(employeeId);

        if (emp == null) {
            System.out.println("Error: Employee not found.");
            sc.close();
            return;
        }

        System.out.println("Testing as: " + emp.getFullName());
        System.out.println("Status    : " + emp.getStatus());
        System.out.println();

        // Simple loop for backend testing
        boolean running = true;

        while (running) {

            System.out.println("Choose an option:");
            System.out.println("1 - View Leave Balance");
            System.out.println("2 - Submit Leave Request");
            System.out.println("3 - Approve Leave Request");
            System.out.println("4 - Reject Leave Request");
            System.out.println("5 - View Request History");
            System.out.println("0 - Exit");
            System.out.print("Choice: ");

            String choice = sc.nextLine().trim();
            System.out.println();

            switch (choice) {

                case "1":
                    // ----------------------------------------
                    // VIEW LEAVE BALANCE
                    // ----------------------------------------
                    LeaveBalance balance = leaveService.viewLeaveBalance(employeeId);

                    if (balance == null) {
                        System.out.println("No leave balance record found.");
                    } else {
                        System.out.println("Leave Balance");
                        System.out.println();
                        System.out.printf("%-20s : %.2f%n", "Sick Leave", balance.getSickLeave());
                        System.out.printf("%-20s : %.2f%n", "Vacation Leave", balance.getVacationLeave());
                        System.out.printf("%-20s : %.2f%n", "Maternity Leave", balance.getMaternityLeave());
                        System.out.printf("%-20s : %.2f%n", "Paternity Leave", balance.getPaternityLeave());
                        System.out.printf("%-20s : %.2f%n", "Bereavement Leave", balance.getBereavementLeave());
                        System.out.printf("%-20s : %s%n", "Emergency Leave", "unlimited");
                    }

                    System.out.println();
                    break;

                case "2":
                    // ----------------------------------------
                    // SUBMIT LEAVE REQUEST
                    // ----------------------------------------
                    System.out.print("Leave Type: ");
                    String leaveType = sc.nextLine().trim();

                    System.out.print("Start Date (MM/DD/YYYY): ");
                    String startDate = sc.nextLine().trim();

                    System.out.print("End Date (MM/DD/YYYY): ");
                    String endDate = sc.nextLine().trim();

                    System.out.print("Days Requested: ");
                    double daysRequested = Double.parseDouble(sc.nextLine().trim());

                    System.out.print("Reason: ");
                    String reason = sc.nextLine().trim();

                    String submitResult = leaveService.submitLeaveRequest(
                            emp,
                            leaveType,
                            startDate,
                            endDate,
                            daysRequested,
                            reason
                    );

                    System.out.println(submitResult);
                    System.out.println();
                    break;

                case "3":
                    // ----------------------------------------
                    // APPROVE LEAVE REQUEST
                    // ----------------------------------------
                    System.out.println("Tip: View Request History first so you can copy the Request ID.");
                    System.out.println("Tip: Based on your current uploaded user.csv, the only elevated test role is admin.");
                    System.out.println();

                    System.out.print("Enter Request ID to approve: ");
                    String approveId = sc.nextLine().trim();

                    System.out.print("Approver Employee ID: ");
                    String approverId = sc.nextLine().trim();

                    String approveResult = leaveService.approveLeave(approveId, approverId);

                    System.out.println(approveResult);
                    System.out.println();
                    break;

                case "4":
                    // ----------------------------------------
                    // REJECT LEAVE REQUEST
                    // ----------------------------------------
                    System.out.println("Tip: View Request History first so you can copy the Request ID.");
                    System.out.println("Tip: Based on your current uploaded user.csv, the only elevated test role is admin.");
                    System.out.println();

                    System.out.print("Enter Request ID to reject: ");
                    String rejectId = sc.nextLine().trim();

                    System.out.print("Approver Employee ID: ");
                    String rejectApproverId = sc.nextLine().trim();

                    System.out.print("Remarks: ");
                    String remarks = sc.nextLine().trim();

                    String rejectResult = leaveService.rejectLeave(rejectId, rejectApproverId, remarks);

                    System.out.println(rejectResult);
                    System.out.println();
                    break;

                case "5":
                    // ----------------------------------------
                    // VIEW REQUEST HISTORY
                    // ----------------------------------------
                    List<LeaveRequest> history = leaveService.viewRequestHistory();

                    if (history.isEmpty()) {
                        System.out.println("No leave requests found.");
                    } else {
                        System.out.println("Leave Request History");
                        System.out.println();

                        for (LeaveRequest req : history) {
                            System.out.println("----------------------------------------");
                            System.out.printf("%-18s : %s%n", "Request ID", req.getRequestId());
                            System.out.printf("%-18s : %s%n", "Employee ID", req.getEmployeeId());
                            System.out.printf("%-18s : %s%n", "Employee Name", req.getEmployeeName());
                            System.out.printf("%-18s : %s%n", "Leave Type", req.getLeaveType());
                            System.out.printf("%-18s : %s%n", "Start Date", req.getStartDate());
                            System.out.printf("%-18s : %s%n", "End Date", req.getEndDate());
                            System.out.printf("%-18s : %.2f%n", "Days Requested", req.getDaysRequested());
                            System.out.printf("%-18s : %s%n", "Status", req.getStatus());
                            System.out.printf("%-18s : %s%n", "Reason", req.getReason());
                            System.out.printf("%-18s : %s%n", "Approver ID", req.getApproverId());
                            System.out.printf("%-18s : %s%n", "Remarks", req.getRemarks());
                        }

                        System.out.println("----------------------------------------");
                    }

                    System.out.println();
                    break;

                case "0":
                    running = false;
                    System.out.println("Exiting Leave Module Test.");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
                    System.out.println();
                    break;
            }
        }

        sc.close();
    }
}