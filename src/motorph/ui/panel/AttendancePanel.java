package motorph.ui.panel;

import motorph.model.AttendanceRecord;
import motorph.model.Employee;
import motorph.repository.AttendanceRepository;
import motorph.repository.CsvAttendanceRepository;
import motorph.repository.CsvEmployeeRepository;
import motorph.repository.EmployeeRepository;
import motorph.ui.Theme;
import motorph.ui.session.UserSession;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * AttendancePanel
 *
 * Real attendance module panel with role-based viewing rules:
 * - Employee and IT can only view their own attendance
 * - HR, Admin, and Supervisor can view other employees' attendance
 *
 * Late and undertime are displayed in hours using 0.25 increments,
 * based on the already-rounded 15-minute backend logic.
 */
public class AttendancePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final UserSession session;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    private final JTextField employeeSearchField;
    private final JComboBox<EmployeeItem> employeeComboBox;
    private final JComboBox<Integer> yearComboBox;
    private final JComboBox<MonthItem> monthComboBox;

    private final JLabel selectedEmployeeValueLabel;
    private final JLabel daysWithLogsValueLabel;
    private final JLabel totalWorkedHoursValueLabel;
    private final JLabel totalLateHoursValueLabel;
    private final JLabel totalUndertimeHoursValueLabel;
    private final JLabel totalOvertimeHoursValueLabel;

    private final DefaultTableModel tableModel;

    private List<Employee> allEmployees;
    private List<AttendanceRecord> currentEmployeeRecords;

    private static final DecimalFormat HOURS_FORMAT = new DecimalFormat("0.00");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public AttendancePanel(UserSession session) {
        this.session = session;
        this.attendanceRepository = new CsvAttendanceRepository();
        this.employeeRepository = new CsvEmployeeRepository();

        this.employeeSearchField = new JTextField();
        this.employeeComboBox = new JComboBox<>();
        this.yearComboBox = new JComboBox<>();
        this.monthComboBox = new JComboBox<>();

        this.selectedEmployeeValueLabel = new JLabel("-");
        this.daysWithLogsValueLabel = new JLabel("0");
        this.totalWorkedHoursValueLabel = new JLabel("0.00");
        this.totalLateHoursValueLabel = new JLabel("0.00");
        this.totalUndertimeHoursValueLabel = new JLabel("0.00");
        this.totalOvertimeHoursValueLabel = new JLabel("0.00");

        this.tableModel = new DefaultTableModel(
                new Object[]{
                        "Date",
                        "Time In",
                        "Time Out",
                        "Status",
                        "Worked Hours",
                        "Late Hours",
                        "Undertime Hours",
                        "Regular Hours",
                        "Overtime Hours"
                },
                0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.allEmployees = employeeRepository.findAll();
        this.currentEmployeeRecords = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        buildUI();
        initializeEmployeeSelection();
        loadAttendanceForCurrentSelection();
    }

    /**
     * Builds the full Attendance panel UI.
     */
    private void buildUI() {

        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setBackground(Theme.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel("Attendance");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("View employee attendance records and monthly summaries.");
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);

        topWrapper.add(titleLabel);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 6)));
        topWrapper.add(subtitleLabel);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 18)));

        // Employee selector card
        JPanel employeeCard = new JPanel();
        employeeCard.setLayout(new BoxLayout(employeeCard, BoxLayout.Y_AXIS));
        employeeCard.setBackground(Theme.CARD_BACKGROUND);
        employeeCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel employeeSectionTitle = new JLabel("Employee Selection");
        employeeSectionTitle.setFont(Theme.FONT_HEADING);
        employeeSectionTitle.setForeground(Theme.TEXT_PRIMARY);

        employeeCard.add(employeeSectionTitle);
        employeeCard.add(Box.createRigidArea(new Dimension(0, 12)));

        if (canViewOtherEmployees()) {
            JPanel employeeFilterRow = new JPanel();
            employeeFilterRow.setBackground(Theme.CARD_BACKGROUND);
            employeeFilterRow.setLayout(new BoxLayout(employeeFilterRow, BoxLayout.X_AXIS));

            JLabel searchLabel = new JLabel("Search:");
            searchLabel.setFont(Theme.FONT_BODY);
            searchLabel.setForeground(Theme.TEXT_PRIMARY);

            employeeSearchField.setFont(Theme.FONT_BODY);
            employeeSearchField.setMaximumSize(new Dimension(220, 34));
            employeeSearchField.setPreferredSize(new Dimension(220, 34));

            JButton searchButton = new JButton("Search");
            searchButton.setFont(Theme.FONT_BUTTON);
            searchButton.setBackground(Theme.PRIMARY);
            searchButton.setForeground(java.awt.Color.WHITE);
            searchButton.setFocusPainted(false);
            searchButton.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

            JLabel employeeLabel = new JLabel("Employee:");
            employeeLabel.setFont(Theme.FONT_BODY);
            employeeLabel.setForeground(Theme.TEXT_PRIMARY);

            employeeComboBox.setFont(Theme.FONT_BODY);
            employeeComboBox.setMaximumSize(new Dimension(320, 34));
            employeeComboBox.setPreferredSize(new Dimension(320, 34));

            employeeFilterRow.add(searchLabel);
            employeeFilterRow.add(Box.createRigidArea(new Dimension(10, 0)));
            employeeFilterRow.add(employeeSearchField);
            employeeFilterRow.add(Box.createRigidArea(new Dimension(10, 0)));
            employeeFilterRow.add(searchButton);
            employeeFilterRow.add(Box.createRigidArea(new Dimension(20, 0)));
            employeeFilterRow.add(employeeLabel);
            employeeFilterRow.add(Box.createRigidArea(new Dimension(10, 0)));
            employeeFilterRow.add(employeeComboBox);

            employeeCard.add(employeeFilterRow);

            searchButton.addActionListener(e -> filterEmployeeDropdown());

            employeeComboBox.addActionListener(e -> {
                populateYearOptions();
                refreshMonthOptions();
                loadAttendanceData();
                updateSelectedEmployeeLabel();
            });

        } else {
            // Employee and IT only see their own employee information
            employeeSearchField.setVisible(false);
            employeeComboBox.setVisible(false);

            JLabel selfOnlyLabel = new JLabel(
                    "<html><div style='width:500px;'>You can only view your own attendance records.</div></html>"
            );
            selfOnlyLabel.setFont(Theme.FONT_BODY);
            selfOnlyLabel.setForeground(Theme.TEXT_SECONDARY);

            employeeCard.add(selfOnlyLabel);
        }

        topWrapper.add(employeeCard);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 16)));

        // Filter card for year/month
        JPanel filterCard = new JPanel(new BorderLayout());
        filterCard.setBackground(Theme.CARD_BACKGROUND);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JPanel filterLeft = new JPanel();
        filterLeft.setBackground(Theme.CARD_BACKGROUND);
        filterLeft.setLayout(new BoxLayout(filterLeft, BoxLayout.X_AXIS));

        JLabel yearLabel = new JLabel("Year:");
        yearLabel.setFont(Theme.FONT_BODY);
        yearLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel monthLabel = new JLabel("Month:");
        monthLabel.setFont(Theme.FONT_BODY);
        monthLabel.setForeground(Theme.TEXT_PRIMARY);

        yearComboBox.setFont(Theme.FONT_BODY);
        yearComboBox.setPreferredSize(new Dimension(120, 34));
        yearComboBox.setMaximumSize(new Dimension(120, 34));

        monthComboBox.setFont(Theme.FONT_BODY);
        monthComboBox.setPreferredSize(new Dimension(180, 34));
        monthComboBox.setMaximumSize(new Dimension(180, 34));

        JButton reloadButton = new JButton("Load Attendance");
        reloadButton.setFont(Theme.FONT_BUTTON);
        reloadButton.setBackground(Theme.PRIMARY);
        reloadButton.setForeground(java.awt.Color.WHITE);
        reloadButton.setFocusPainted(false);
        reloadButton.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        filterLeft.add(yearLabel);
        filterLeft.add(Box.createRigidArea(new Dimension(10, 0)));
        filterLeft.add(yearComboBox);
        filterLeft.add(Box.createRigidArea(new Dimension(20, 0)));
        filterLeft.add(monthLabel);
        filterLeft.add(Box.createRigidArea(new Dimension(10, 0)));
        filterLeft.add(monthComboBox);

        filterCard.add(filterLeft, BorderLayout.WEST);
        filterCard.add(reloadButton, BorderLayout.EAST);

        topWrapper.add(filterCard);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 16)));

        // Summary card
        JPanel summaryCard = new JPanel();
        summaryCard.setBackground(Theme.CARD_BACKGROUND);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.X_AXIS));

        summaryCard.add(createSummaryItem("Employee", selectedEmployeeValueLabel));
        summaryCard.add(Box.createRigidArea(new Dimension(20, 0)));
        summaryCard.add(createSummaryItem("Days with Logs", daysWithLogsValueLabel));
        summaryCard.add(Box.createRigidArea(new Dimension(20, 0)));
        summaryCard.add(createSummaryItem("Worked Hours", totalWorkedHoursValueLabel));
        summaryCard.add(Box.createRigidArea(new Dimension(20, 0)));
        summaryCard.add(createSummaryItem("Late Hours", totalLateHoursValueLabel));
        summaryCard.add(Box.createRigidArea(new Dimension(20, 0)));
        summaryCard.add(createSummaryItem("Undertime Hours", totalUndertimeHoursValueLabel));
        summaryCard.add(Box.createRigidArea(new Dimension(20, 0)));
        summaryCard.add(createSummaryItem("Overtime Hours", totalOvertimeHoursValueLabel));

        topWrapper.add(summaryCard);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 18)));

        add(topWrapper, BorderLayout.NORTH);

        // Real attendance table
        JTable attendanceTable = new JTable(tableModel);
        attendanceTable.setRowHeight(24);
        attendanceTable.setFont(Theme.FONT_BODY);
        attendanceTable.getTableHeader().setFont(Theme.FONT_BUTTON);
        attendanceTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        add(scrollPane, BorderLayout.CENTER);

        // Events
        yearComboBox.addActionListener(e -> {
            refreshMonthOptions();
            loadAttendanceData();
        });

        monthComboBox.addActionListener(e -> loadAttendanceData());
        reloadButton.addActionListener(e -> loadAttendanceForCurrentSelection());
    }

    /**
     * Creates a summary item block.
     */
    private JPanel createSummaryItem(String labelText, JLabel valueLabel) {

        JPanel itemPanel = new JPanel();
        itemPanel.setBackground(Theme.CARD_BACKGROUND);
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_SMALL);
        label.setForeground(Theme.TEXT_SECONDARY);

        valueLabel.setFont(Theme.FONT_HEADING);
        valueLabel.setForeground(Theme.TEXT_PRIMARY);

        itemPanel.add(label);
        itemPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        itemPanel.add(valueLabel);

        return itemPanel;
    }

    /**
     * Initializes employee selection depending on role rules.
     */
    private void initializeEmployeeSelection() {

        if (canViewOtherEmployees()) {
            // HR, Admin, Supervisor can view other employees
            populateEmployeeDropdown(allEmployees);

            // Default to logged-in employee if available
            selectEmployeeInDropdown(session.getEmployeeId());

        } else {
            // Employee and IT can only view their own records
            Employee self = employeeRepository.findById(session.getEmployeeId());
            List<Employee> selfOnly = new ArrayList<>();

            if (self != null) {
                selfOnly.add(self);
            }

            populateEmployeeDropdown(selfOnly);
            selectEmployeeInDropdown(session.getEmployeeId());
        }

        updateSelectedEmployeeLabel();
        populateYearOptions();
        refreshMonthOptions();
    }

    /**
     * Filters the employee dropdown based on search text.
     */
    private void filterEmployeeDropdown() {

        String keyword = employeeSearchField.getText() == null
                ? ""
                : employeeSearchField.getText().trim().toLowerCase(Locale.ENGLISH);

        List<Employee> filtered = new ArrayList<>();

        for (Employee employee : allEmployees) {
            String fullText = (employee.getEmployeeId() + " " + employee.getFullName() + " " + employee.getPosition())
                    .toLowerCase(Locale.ENGLISH);

            if (keyword.isEmpty() || fullText.contains(keyword)) {
                filtered.add(employee);
            }
        }

        populateEmployeeDropdown(filtered);
    }

    /**
     * Loads the dropdown with employees.
     */
    private void populateEmployeeDropdown(List<Employee> employees) {
        employeeComboBox.removeAllItems();

        employees.sort(Comparator.comparing(Employee::getFullName));

        for (Employee employee : employees) {
            employeeComboBox.addItem(new EmployeeItem(employee));
        }
    }

    /**
     * Selects one employee in the dropdown by employee ID.
     */
    private void selectEmployeeInDropdown(String employeeId) {
        for (int i = 0; i < employeeComboBox.getItemCount(); i++) {
            EmployeeItem item = employeeComboBox.getItemAt(i);
            if (item.getEmployee().getEmployeeId().equals(employeeId)) {
                employeeComboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Loads real records for the currently selected employee.
     */
    private void loadAttendanceForCurrentSelection() {
        populateYearOptions();
        refreshMonthOptions();
        loadAttendanceData();
        updateSelectedEmployeeLabel();
    }

    /**
     * Loads year options from the currently selected employee.
     */
    private void populateYearOptions() {
        yearComboBox.removeAllItems();

        currentEmployeeRecords = attendanceRepository.findByEmployeeId(getSelectedEmployeeId());

        List<Integer> years = new ArrayList<>();

        for (AttendanceRecord record : currentEmployeeRecords) {
            int year = record.getDate().getYear();
            if (!years.contains(year)) {
                years.add(year);
            }
        }

        years.sort(Integer::compareTo);

        for (Integer year : years) {
            yearComboBox.addItem(year);
        }

        if (yearComboBox.getItemCount() > 0) {
            yearComboBox.setSelectedIndex(yearComboBox.getItemCount() - 1);
        }
    }

    /**
     * Loads month options based on selected year.
     */
    private void refreshMonthOptions() {
        monthComboBox.removeAllItems();

        Integer selectedYear = (Integer) yearComboBox.getSelectedItem();
        if (selectedYear == null) {
            return;
        }

        List<Integer> availableMonths = new ArrayList<>();

        for (AttendanceRecord record : currentEmployeeRecords) {
            if (record.getDate().getYear() == selectedYear) {
                int month = record.getDate().getMonthValue();
                if (!availableMonths.contains(month)) {
                    availableMonths.add(month);
                }
            }
        }

        availableMonths.sort(Integer::compareTo);

        for (Integer monthNumber : availableMonths) {
            monthComboBox.addItem(new MonthItem(monthNumber));
        }

        if (monthComboBox.getItemCount() > 0) {
            monthComboBox.setSelectedIndex(monthComboBox.getItemCount() - 1);
        }
    }

    /**
     * Loads attendance table and summaries.
     */
    private void loadAttendanceData() {
        tableModel.setRowCount(0);

        Integer selectedYear = (Integer) yearComboBox.getSelectedItem();
        MonthItem selectedMonth = (MonthItem) monthComboBox.getSelectedItem();

        if (selectedYear == null || selectedMonth == null) {
            updateSummary(0, 0.0, 0.0, 0.0, 0.0);
            return;
        }

        List<AttendanceRecord> records = attendanceRepository.findByEmployeeIdAndMonth(
                getSelectedEmployeeId(),
                selectedYear,
                selectedMonth.getMonthNumber()
        );

        if (records.isEmpty()) {
            updateSummary(0, 0.0, 0.0, 0.0, 0.0);
            return;
        }

        double totalWorkedHours = 0.0;
        double totalLateHours = 0.0;
        double totalUndertimeHours = 0.0;
        double totalOvertimeHours = 0.0;

        for (AttendanceRecord record : records) {

            totalWorkedHours += record.getWorkedHours();
            totalLateHours += record.getLateHours();
            totalUndertimeHours += record.getUndertimeHours();
            totalOvertimeHours += record.getOvertimeHours();

            tableModel.addRow(new Object[]{
                    record.getDate().format(DATE_FORMAT),
                    record.getTimeIn() == null ? "" : record.getTimeIn().format(TIME_FORMAT),
                    record.getTimeOut() == null ? "" : record.getTimeOut().format(TIME_FORMAT),
                    record.getDayStatus(),
                    HOURS_FORMAT.format(record.getWorkedHours()),
                    HOURS_FORMAT.format(record.getLateHours()),
                    HOURS_FORMAT.format(record.getUndertimeHours()),
                    HOURS_FORMAT.format(record.getRegularHours()),
                    HOURS_FORMAT.format(record.getOvertimeHours())
            });
        }

        updateSummary(
                records.size(),
                totalWorkedHours,
                totalLateHours,
                totalUndertimeHours,
                totalOvertimeHours
        );
    }

    /**
     * Updates summary labels.
     */
    private void updateSummary(
            int daysWithLogs,
            double totalWorkedHours,
            double totalLateHours,
            double totalUndertimeHours,
            double totalOvertimeHours
    ) {
        daysWithLogsValueLabel.setText(String.valueOf(daysWithLogs));
        totalWorkedHoursValueLabel.setText(HOURS_FORMAT.format(totalWorkedHours));
        totalLateHoursValueLabel.setText(HOURS_FORMAT.format(totalLateHours));
        totalUndertimeHoursValueLabel.setText(HOURS_FORMAT.format(totalUndertimeHours));
        totalOvertimeHoursValueLabel.setText(HOURS_FORMAT.format(totalOvertimeHours));
    }

    /**
     * Updates selected employee summary label.
     */
    private void updateSelectedEmployeeLabel() {
        EmployeeItem selectedItem = (EmployeeItem) employeeComboBox.getSelectedItem();

        if (selectedItem == null) {
            selectedEmployeeValueLabel.setText("-");
            return;
        }

        selectedEmployeeValueLabel.setText(selectedItem.getEmployee().getEmployeeId());
    }

    /**
     * Returns selected employee ID safely.
     */
    private String getSelectedEmployeeId() {
        EmployeeItem selectedItem = (EmployeeItem) employeeComboBox.getSelectedItem();

        if (selectedItem == null) {
            return session.getEmployeeId();
        }

        return selectedItem.getEmployee().getEmployeeId();
    }

    /**
     * Employee and IT can only view own attendance.
     */
    private boolean canViewOtherEmployees() {
        String role = safeLower(session.getRole());
        return role.contains("hr") || role.contains("admin") || role.contains("supervisor");
    }

    /**
     * Lowercase helper with null safety.
     */
    private String safeLower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Wrapper for employee dropdown display.
     */
    private static class EmployeeItem {
        private final Employee employee;

        public EmployeeItem(Employee employee) {
            this.employee = employee;
        }

        public Employee getEmployee() {
            return employee;
        }

        @Override
        public String toString() {
            return employee.getEmployeeId() + " - " + employee.getFullName();
        }
    }

    /**
     * Wrapper for month dropdown display.
     */
    private static class MonthItem {
        private final int monthNumber;

        public MonthItem(int monthNumber) {
            this.monthNumber = monthNumber;
        }

        public int getMonthNumber() {
            return monthNumber;
        }

        @Override
        public String toString() {
            return monthNumber + " - " + formatMonthName(monthNumber);
        }

        private static String formatMonthName(int monthNumber) {
            String raw = Month.of(monthNumber).name().toLowerCase(Locale.ENGLISH);
            return raw.substring(0, 1).toUpperCase(Locale.ENGLISH) + raw.substring(1);
        }
    }
}