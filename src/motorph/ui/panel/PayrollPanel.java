package motorph.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import motorph.model.AttendanceRecord;
import motorph.model.Employee;
import motorph.model.LeaveRequest;
import motorph.repository.ActivityLogRepository;
import motorph.repository.CsvAttendanceRepository;
import motorph.repository.CsvEmployeeRepository;
import motorph.repository.LeaveRepository;
import motorph.service.ContributionCalculator;
import motorph.service.PayrollCalculator;
import motorph.ui.Theme;
import motorph.ui.session.UserSession;
import motorph.util.PathHelper;

public class PayrollPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public enum ViewMode {
        SELF_SERVICE("Payslip", "Review your own payroll records, filter by period, and inspect detailed payslips."),
        ALL_RECORDS("Employee Payroll View", "Review payroll records across employees for the selected payroll period."),
        MANAGE("Manage Payroll", "Generate, review, and export payroll output for the selected payroll period.");

        private final String title;
        private final String subtitle;

        ViewMode(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    private final UserSession session;
    private final ViewMode viewMode;
    private final CsvEmployeeRepository employeeRepository;
    private final CsvAttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final PayrollCalculator payrollCalculator;
    private final ContributionCalculator contributionCalculator;
    private final ActivityLogRepository activityLogRepository;

    private final JComboBox<Integer> yearComboBox;
    private final JComboBox<Integer> monthComboBox;
    private final DefaultTableModel payrollTableModel;
    private final JTable payrollTable;
    private final JTextArea payslipArea;
    private final List<PayrollSummary> displayedSummaries;

    public PayrollPanel(UserSession session) {
        this(session, ViewMode.SELF_SERVICE);
    }

    public PayrollPanel(UserSession session, ViewMode viewMode) {
        this.session = session;
        this.viewMode = viewMode;
        this.employeeRepository = new CsvEmployeeRepository();
        this.attendanceRepository = new CsvAttendanceRepository();
        this.leaveRepository = new LeaveRepository();
        this.payrollCalculator = new PayrollCalculator();
        this.contributionCalculator = new ContributionCalculator();
        this.activityLogRepository = new ActivityLogRepository();
        this.yearComboBox = new JComboBox<>();
        this.monthComboBox = new JComboBox<>();
        this.payrollTableModel = new DefaultTableModel(
                new Object[] {"Employee ID", "Name", "Days Worked", "Total Hours", "Overtime Pay", "Leave Deduction", "Gross Pay", "Net Pay"},
                0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.payrollTable = new JTable(payrollTableModel);
        this.payslipArea = new JTextArea();
        this.displayedSummaries = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        buildUi();
        initializeFilters();
        refreshPayrollTable();
    }

    private void buildUi() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Theme.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel(viewMode.title);
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(viewMode.subtitle);
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);

        wrapper.add(titleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 6)));
        wrapper.add(subtitleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));
        wrapper.add(buildFilterCard());
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));
        wrapper.add(buildTableCard());
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));
        wrapper.add(buildPayslipCard());

        add(wrapper, BorderLayout.CENTER);
    }

    private JPanel buildFilterCard() {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        card.setBackground(Theme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel scopeLabel = new JLabel("Scope: " + buildScopeLabel());
        scopeLabel.setFont(Theme.FONT_BODY);
        scopeLabel.setForeground(Theme.TEXT_PRIMARY);

        yearComboBox.setFont(Theme.FONT_BODY);
        yearComboBox.setPreferredSize(new Dimension(120, 34));
        monthComboBox.setFont(Theme.FONT_BODY);
        monthComboBox.setPreferredSize(new Dimension(120, 34));

        JButton applyButton = new JButton("Refresh Payroll");
        applyButton.setFont(Theme.FONT_BUTTON);
        applyButton.setBackground(Theme.PRIMARY);
        applyButton.setForeground(Color.WHITE);
        applyButton.setFocusPainted(false);
        applyButton.addActionListener(e -> refreshPayrollTable());

        JButton exportButton = new JButton("Export CSV");
        exportButton.setFont(Theme.FONT_BUTTON);
        exportButton.setBackground(Theme.BUTTON_BACKGROUND);
        exportButton.setForeground(Theme.BUTTON_TEXT);
        exportButton.setFocusPainted(false);
        exportButton.addActionListener(e -> exportPayrollToCsv());

        card.add(scopeLabel);
        card.add(new JLabel("Year:"));
        card.add(yearComboBox);
        card.add(new JLabel("Month:"));
        card.add(monthComboBox);
        card.add(applyButton);
        card.add(exportButton);

        if (canManagePayroll()) {
            JButton generateButton = new JButton("Generate Payroll");
            generateButton.setFont(Theme.FONT_BUTTON);
            generateButton.setBackground(new Color(56, 128, 77));
            generateButton.setForeground(Color.WHITE);
            generateButton.setFocusPainted(false);
            generateButton.addActionListener(e -> generatePayrollHistory());
            card.add(generateButton);
        }

        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);

        payrollTable.setFont(Theme.FONT_BODY);
        payrollTable.setRowHeight(30);
        payrollTable.getTableHeader().setFont(Theme.FONT_BUTTON);
        payrollTable.getTableHeader().setBackground(new Color(244, 247, 255));
        payrollTable.getTableHeader().setForeground(Theme.TEXT_PRIMARY);
        payrollTable.setFillsViewportHeight(true);
        payrollTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        payrollTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedPayslip();
            }
        });

        JScrollPane scrollPane = new JScrollPane(payrollTable);
        scrollPane.setPreferredSize(new Dimension(1020, 360));
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildPayslipCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel heading = new JLabel("Detailed Payslip Breakdown");
        heading.setFont(Theme.FONT_HEADING);
        heading.setForeground(Theme.TEXT_PRIMARY);

        payslipArea.setFont(Theme.FONT_BODY);
        payslipArea.setEditable(false);
        payslipArea.setLineWrap(true);
        payslipArea.setWrapStyleWord(true);
        payslipArea.setText("No row selected.");
        payslipArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(payslipArea);
        scrollPane.setPreferredSize(new Dimension(1020, 240));

        card.add(heading, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private void initializeFilters() {
        yearComboBox.removeAllItems();
        monthComboBox.removeAllItems();

        Set<Integer> years = new LinkedHashSet<>();
        List<AttendanceRecord> allAttendance = attendanceRepository.findAll();
        LocalDate latestDate = null;

        for (AttendanceRecord record : allAttendance) {
            years.add(record.getDate().getYear());
            LocalDate date = record.getDate();
            if (latestDate == null || date.isAfter(latestDate)) {
                latestDate = date;
            }
        }

        if (years.isEmpty()) {
            years.add(LocalDate.now().getYear());
        }

        for (Integer year : years) {
            yearComboBox.addItem(year);
        }
        for (int month = 1; month <= 12; month++) {
            monthComboBox.addItem(month);
        }

        int defaultYear = latestDate == null ? LocalDate.now().getYear() : latestDate.getYear();
        int defaultMonth = latestDate == null ? LocalDate.now().getMonthValue() : latestDate.getMonthValue();
        yearComboBox.setSelectedItem(defaultYear);
        monthComboBox.setSelectedItem(defaultMonth);
    }

    private void refreshPayrollTable() {
        payrollTableModel.setRowCount(0);
        displayedSummaries.clear();

        Integer selectedYear = (Integer) yearComboBox.getSelectedItem();
        Integer selectedMonth = (Integer) monthComboBox.getSelectedItem();
        if (selectedYear == null || selectedMonth == null) {
            return;
        }

        List<Employee> employees = getEmployeesForCurrentSession();
        List<LeaveRequest> leaveRequests = leaveRepository.loadAll();

        for (Employee employee : employees) {
            List<AttendanceRecord> attendanceRecords =
                    attendanceRepository.findByEmployeeIdAndMonth(employee.getEmployeeId(), selectedYear, selectedMonth);

            PayrollSummary summary = buildSummary(employee, attendanceRecords, leaveRequests, selectedYear, selectedMonth);
            displayedSummaries.add(summary);

            payrollTableModel.addRow(new Object[] {
                    employee.getEmployeeId(),
                    employee.getFirstName() + " " + employee.getLastName(),
                    summary.daysWorked,
                    String.format(Locale.ENGLISH, "%.2f", summary.totalHoursWorked),
                    formatMoney(summary.overtimePay),
                    formatMoney(summary.leaveDeduction),
                    formatMoney(summary.grossPay),
                    formatMoney(summary.netPay)
            });
        }

        if (!displayedSummaries.isEmpty()) {
            payrollTable.setRowSelectionInterval(0, 0);
            showSelectedPayslip();
        } else {
            payslipArea.setText("No payroll rows available for the selected period.");
        }
    }

    private List<Employee> getEmployeesForCurrentSession() {
        List<Employee> allEmployees = new ArrayList<>(employeeRepository.findAll());
        allEmployees.sort(Comparator.comparing(Employee::getEmployeeId));

        if (canViewAllPayroll()) {
            return allEmployees;
        }

        Employee self = employeeRepository.findById(session.getEmployeeId());
        if (self == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(self);
    }

    private boolean canViewAllPayroll() {
        return viewMode == ViewMode.ALL_RECORDS || viewMode == ViewMode.MANAGE;
    }

    private boolean canManagePayroll() {
        return viewMode == ViewMode.MANAGE;
    }

    private String buildScopeLabel() {
        if (viewMode == ViewMode.MANAGE) {
            return "ALL PAYROLL RECORDS + PROCESSING";
        }
        if (viewMode == ViewMode.ALL_RECORDS) {
            return "ALL PAYROLL RECORDS";
        }
        return "SELF";
    }

    private PayrollSummary buildSummary(
            Employee employee,
            List<AttendanceRecord> attendanceRecords,
            List<LeaveRequest> leaveRequests,
            int selectedYear,
            int selectedMonth
    ) {
        int daysWorked = 0;
        double overtimeHours = 0;
        double regularHours = 0;
        double totalHoursWorked = 0;
        double lateHours = 0;
        double undertimeHours = 0;

        for (AttendanceRecord record : attendanceRecords) {
            if (record.getWorkedMinutes() > 0) {
                daysWorked++;
            }
            totalHoursWorked += record.getWorkedHours();
            overtimeHours += record.getOvertimeHours();
            regularHours += record.getRegularHours();
            lateHours += record.getLateHours();
            undertimeHours += record.getUndertimeHours();
        }

        LeaveDaysSummary leaveDaysSummary = computeLeaveDays(employee.getEmployeeId(), leaveRequests, selectedYear, selectedMonth);
        double paidLeaveHours = leaveDaysSummary.paidLeaveDays * 8.0;
        double unpaidLeaveHours = leaveDaysSummary.unpaidLeaveDays * 8.0;

        double regularPay = payrollCalculator.computeRegularPay(regularHours + paidLeaveHours, employee.getHourlyRate());
        double overtimePay = payrollCalculator.computeOvertimePay(overtimeHours, employee.getHourlyRate());
        double lateDeduction = payrollCalculator.computeLateDeduction(lateHours, employee.getHourlyRate());
        double undertimeDeduction = payrollCalculator.computeUndertimeDeduction(undertimeHours, employee.getHourlyRate());
        double leaveDeduction = payrollCalculator.computeRegularPay(unpaidLeaveHours, employee.getHourlyRate());
        double allowances = employee.getRiceSubsidy() + employee.getPhoneAllowance() + employee.getClothingAllowance();

        double grossPay = Math.max(0.0, regularPay + overtimePay + allowances - lateDeduction - undertimeDeduction - leaveDeduction);
        double sss = contributionCalculator.computeSSS(grossPay);
        double philhealth = contributionCalculator.computePhilHealth(grossPay);
        double pagibig = contributionCalculator.computePagibig(grossPay);
        double taxableIncome = Math.max(0.0, grossPay - sss - philhealth - pagibig);
        double withholdingTax = contributionCalculator.computeWithholdingTax(taxableIncome);
        double statutoryDeductions = sss + philhealth + pagibig + withholdingTax;
        double totalDeductions = lateDeduction + undertimeDeduction + leaveDeduction + statutoryDeductions;
        double netPay = grossPay - statutoryDeductions;

        return new PayrollSummary(
                employee, selectedYear, selectedMonth, daysWorked, totalHoursWorked, regularHours, overtimeHours,
                leaveDaysSummary.paidLeaveDays, leaveDaysSummary.unpaidLeaveDays, regularPay, overtimePay, allowances,
                leaveDeduction, grossPay, lateDeduction, undertimeDeduction, sss, philhealth, pagibig,
                withholdingTax, totalDeductions, netPay
        );
    }

    private LeaveDaysSummary computeLeaveDays(String employeeId, List<LeaveRequest> leaveRequests, int selectedYear, int selectedMonth) {
        double paidLeaveDays = 0;
        double unpaidLeaveDays = 0;
        YearMonth targetMonth = YearMonth.of(selectedYear, selectedMonth);

        for (LeaveRequest request : leaveRequests) {
            if (!employeeId.equals(request.getEmployeeId()) || !"APPROVED".equalsIgnoreCase(request.getStatus())) {
                continue;
            }

            LocalDate startDate = parseDateQuietly(request.getStartDate());
            LocalDate endDate = parseDateQuietly(request.getEndDate());
            if (startDate == null) {
                continue;
            }
            if (endDate == null) {
                endDate = startDate;
            }
            if (endDate.isBefore(startDate)) {
                continue;
            }

            long daysInMonth = countDaysInMonthRange(startDate, endDate, targetMonth);
            if (daysInMonth <= 0) {
                continue;
            }

            if ("Emergency Leave".equalsIgnoreCase(request.getLeaveType())) {
                unpaidLeaveDays += daysInMonth;
            } else {
                paidLeaveDays += daysInMonth;
            }
        }

        return new LeaveDaysSummary(paidLeaveDays, unpaidLeaveDays);
    }

    private long countDaysInMonthRange(LocalDate startDate, LocalDate endDate, YearMonth targetMonth) {
        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();
        LocalDate overlapStart = startDate.isAfter(monthStart) ? startDate : monthStart;
        LocalDate overlapEnd = endDate.isBefore(monthEnd) ? endDate : monthEnd;
        if (overlapEnd.isBefore(overlapStart)) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
    }

    private LocalDate parseDateQuietly(String raw) {
        try {
            return LocalDate.parse(raw, DATE_FMT);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(raw);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private void showSelectedPayslip() {
        int selectedRow = payrollTable.getSelectedRow();
        if (selectedRow < 0) {
            payslipArea.setText("No row selected.");
            return;
        }

        int modelRow = payrollTable.convertRowIndexToModel(selectedRow);
        if (modelRow < 0 || modelRow >= displayedSummaries.size()) {
            payslipArea.setText("No payroll details available for the selected row.");
            return;
        }

        PayrollSummary s = displayedSummaries.get(modelRow);
        StringBuilder text = new StringBuilder();
        text.append("MOTORPH PAYSLIP BREAKDOWN\n");
        text.append("========================================\n");
        text.append("Employee ID: ").append(s.employee.getEmployeeId()).append("\n");
        text.append("Name: ").append(s.employee.getFullName()).append("\n");
        text.append("Position: ").append(s.employee.getPosition()).append("\n");
        text.append("Period: ").append(String.format(Locale.ENGLISH, "%02d/%d", s.month, s.year)).append("\n\n");
        text.append("ATTENDANCE & LEAVE\n");
        text.append("- Days Worked: ").append(s.daysWorked).append("\n");
        text.append("- Total Hours Worked: ").append(String.format(Locale.ENGLISH, "%.2f", s.totalHoursWorked)).append("\n");
        text.append("- Regular Hours: ").append(String.format(Locale.ENGLISH, "%.2f", s.regularHours)).append("\n");
        text.append("- Overtime Hours: ").append(String.format(Locale.ENGLISH, "%.2f", s.overtimeHours)).append("\n");
        text.append("- Paid Leave Days: ").append(String.format(Locale.ENGLISH, "%.2f", s.paidLeaveDays)).append("\n");
        text.append("- Unpaid Leave Days: ").append(String.format(Locale.ENGLISH, "%.2f", s.unpaidLeaveDays)).append("\n\n");
        text.append("EARNINGS\n");
        text.append("- Regular Pay: ").append(formatMoney(s.regularPay)).append("\n");
        text.append("- Overtime Pay: ").append(formatMoney(s.overtimePay)).append("\n");
        text.append("- Allowances: ").append(formatMoney(s.allowances)).append("\n");
        text.append("- Gross Pay: ").append(formatMoney(s.grossPay)).append("\n\n");
        text.append("DEDUCTIONS\n");
        text.append("- Late Deduction: ").append(formatMoney(s.lateDeduction)).append("\n");
        text.append("- Undertime Deduction: ").append(formatMoney(s.undertimeDeduction)).append("\n");
        text.append("- Leave Deduction: ").append(formatMoney(s.leaveDeduction)).append("\n");
        text.append("- SSS: ").append(formatMoney(s.sss)).append("\n");
        text.append("- PhilHealth: ").append(formatMoney(s.philhealth)).append("\n");
        text.append("- Pag-IBIG: ").append(formatMoney(s.pagibig)).append("\n");
        text.append("- Withholding Tax: ").append(formatMoney(s.withholdingTax)).append("\n");
        text.append("- Total Deductions: ").append(formatMoney(s.totalDeductions)).append("\n\n");
        text.append("NET PAY: ").append(formatMoney(s.netPay)).append("\n");
        payslipArea.setText(text.toString());
        payslipArea.setCaretPosition(0);
    }

    private void exportPayrollToCsv() {
        Integer selectedYear = (Integer) yearComboBox.getSelectedItem();
        Integer selectedMonth = (Integer) monthComboBox.getSelectedItem();
        if (selectedYear == null || selectedMonth == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid payroll period before exporting.");
            return;
        }
        if (displayedSummaries.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No payroll rows available to export.");
            return;
        }

        String fileName = String.format(Locale.ENGLISH, "payroll_export_%d_%02d.csv", selectedYear, selectedMonth);
        try (FileWriter fw = new FileWriter(PathHelper.getDataFile(fileName).toFile(), false)) {
            fw.write("employeeId,employeeName,year,month,daysWorked,totalHours,overtimePay,leaveDeduction,grossPay,netPay\n");
            for (PayrollSummary s : displayedSummaries) {
                fw.write(
                        escapeCsv(s.employee.getEmployeeId()) + ","
                                + escapeCsv(s.employee.getFullName()) + ","
                                + s.year + ","
                                + s.month + ","
                                + s.daysWorked + ","
                                + String.format(Locale.ENGLISH, "%.2f", s.totalHoursWorked) + ","
                                + String.format(Locale.ENGLISH, "%.2f", s.overtimePay) + ","
                                + String.format(Locale.ENGLISH, "%.2f", s.leaveDeduction) + ","
                                + String.format(Locale.ENGLISH, "%.2f", s.grossPay) + ","
                                + String.format(Locale.ENGLISH, "%.2f", s.netPay)
                                + "\n"
                );
            }
            fw.flush();
            JOptionPane.showMessageDialog(this, "Payroll exported to data/" + fileName);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to export payroll CSV: " + e.getMessage());
        }
    }

    private void generatePayrollHistory() {
        Integer selectedYear = (Integer) yearComboBox.getSelectedItem();
        Integer selectedMonth = (Integer) monthComboBox.getSelectedItem();
        if (selectedYear == null || selectedMonth == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid payroll period first.");
            return;
        }
        if (displayedSummaries.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No payroll summaries available for the selected period.");
            return;
        }

        Path payrollFile = PathHelper.getDataFile("payroll.csv");
        boolean exists = Files.exists(payrollFile);
        try (FileWriter fw = new FileWriter(payrollFile.toFile(), true)) {
            if (!exists) {
                fw.write("employeeId,employeeName,year,month,daysWorked,totalHours,overtimePay,leaveDeduction,grossPay,netPay\n");
            }

            for (PayrollSummary s : displayedSummaries) {
                fw.write(
                        escapeCsv(s.employee.getEmployeeId()) + ","
                                + escapeCsv(s.employee.getFullName()) + ","
                                + s.year + ","
                                + s.month + ","
                                + s.daysWorked + ","
                                + String.format(Locale.ENGLISH, "%.2f", s.totalHoursWorked) + ","
                                + String.format(Locale.ENGLISH, "%.2f", s.overtimePay) + ","
                                + String.format(Locale.ENGLISH, "%.2f", s.leaveDeduction) + ","
                                + String.format(Locale.ENGLISH, "%.2f", s.grossPay) + ","
                                + String.format(Locale.ENGLISH, "%.2f", s.netPay)
                                + "\n"
                );
            }

            fw.flush();
            activityLogRepository.log(session.getEmployeeId(), "PAYROLL_GENERATED", "Payroll generated for " + selectedMonth + "/" + selectedYear);
            JOptionPane.showMessageDialog(this, "Payroll history generated in data/payroll.csv.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to generate payroll history: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String formatMoney(double value) {
        return String.format(Locale.ENGLISH, "%,.2f", value);
    }

    private static class LeaveDaysSummary {
        private final double paidLeaveDays;
        private final double unpaidLeaveDays;

        private LeaveDaysSummary(double paidLeaveDays, double unpaidLeaveDays) {
            this.paidLeaveDays = paidLeaveDays;
            this.unpaidLeaveDays = unpaidLeaveDays;
        }
    }

    private static class PayrollSummary {
        private final Employee employee;
        private final int year;
        private final int month;
        private final int daysWorked;
        private final double totalHoursWorked;
        private final double regularHours;
        private final double overtimeHours;
        private final double paidLeaveDays;
        private final double unpaidLeaveDays;
        private final double regularPay;
        private final double overtimePay;
        private final double allowances;
        private final double leaveDeduction;
        private final double grossPay;
        private final double lateDeduction;
        private final double undertimeDeduction;
        private final double sss;
        private final double philhealth;
        private final double pagibig;
        private final double withholdingTax;
        private final double totalDeductions;
        private final double netPay;

        private PayrollSummary(
                Employee employee,
                int year,
                int month,
                int daysWorked,
                double totalHoursWorked,
                double regularHours,
                double overtimeHours,
                double paidLeaveDays,
                double unpaidLeaveDays,
                double regularPay,
                double overtimePay,
                double allowances,
                double leaveDeduction,
                double grossPay,
                double lateDeduction,
                double undertimeDeduction,
                double sss,
                double philhealth,
                double pagibig,
                double withholdingTax,
                double totalDeductions,
                double netPay
        ) {
            this.employee = employee;
            this.year = year;
            this.month = month;
            this.daysWorked = daysWorked;
            this.totalHoursWorked = totalHoursWorked;
            this.regularHours = regularHours;
            this.overtimeHours = overtimeHours;
            this.paidLeaveDays = paidLeaveDays;
            this.unpaidLeaveDays = unpaidLeaveDays;
            this.regularPay = regularPay;
            this.overtimePay = overtimePay;
            this.allowances = allowances;
            this.leaveDeduction = leaveDeduction;
            this.grossPay = grossPay;
            this.lateDeduction = lateDeduction;
            this.undertimeDeduction = undertimeDeduction;
            this.sss = sss;
            this.philhealth = philhealth;
            this.pagibig = pagibig;
            this.withholdingTax = withholdingTax;
            this.totalDeductions = totalDeductions;
            this.netPay = netPay;
        }
    }
}
