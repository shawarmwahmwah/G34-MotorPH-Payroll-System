package motorph.ui.panel;

import motorph.model.AttendanceRecord;
import motorph.repository.AttendanceRepository;
import motorph.repository.CsvAttendanceRepository;
import motorph.ui.Theme;
import motorph.ui.session.UserSession;

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
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * AttendancePanel
 *
 * Real attendance module panel.
 * Loads the logged-in employee's attendance records from the repository
 * and displays monthly attendance plus summary values.
 */
public class AttendancePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final UserSession session;
    private final AttendanceRepository attendanceRepository;

    private final JComboBox<Integer> yearComboBox;
    private final JComboBox<MonthItem> monthComboBox;

    private final JLabel daysWithLogsValueLabel;
    private final JLabel totalWorkedHoursValueLabel;
    private final JLabel totalLateMinutesValueLabel;
    private final JLabel totalUndertimeMinutesValueLabel;
    private final JLabel totalOvertimeHoursValueLabel;

    private final DefaultTableModel tableModel;

    private final List<AttendanceRecord> allEmployeeRecords;

    private static final DecimalFormat HOURS_FORMAT = new DecimalFormat("0.00");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public AttendancePanel(UserSession session) {
        this.session = session;
        this.attendanceRepository = new CsvAttendanceRepository();
        this.allEmployeeRecords = attendanceRepository.findByEmployeeId(session.getEmployeeId());

        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        // Top controls
        yearComboBox = new JComboBox<>();
        monthComboBox = new JComboBox<>();

        // Summary labels
        daysWithLogsValueLabel = new JLabel("0");
        totalWorkedHoursValueLabel = new JLabel("0.00");
        totalLateMinutesValueLabel = new JLabel("0");
        totalUndertimeMinutesValueLabel = new JLabel("0");
        totalOvertimeHoursValueLabel = new JLabel("0.00");

        // Table model with non-editable cells
        tableModel = new DefaultTableModel(
                new Object[]{
                        "Date",
                        "Time In",
                        "Time Out",
                        "Status",
                        "Worked Hours",
                        "Late (min)",
                        "Undertime (min)",
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

        buildUI();
        populateYearOptions();
        refreshMonthOptions();
        loadAttendanceData();
    }

    /**
     * Builds the panel layout.
     */
    private void buildUI() {

        // Main wrapper for title + filters
        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setBackground(Theme.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel("Attendance");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("View your monthly attendance summary and records.");
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);

        topWrapper.add(titleLabel);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 6)));
        topWrapper.add(subtitleLabel);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 20)));

        // Filter card
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
        topWrapper.add(Box.createRigidArea(new Dimension(0, 18)));

        // Summary card
        JPanel summaryCard = new JPanel();
        summaryCard.setBackground(Theme.CARD_BACKGROUND);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.X_AXIS));

        summaryCard.add(createSummaryItem("Days with Logs", daysWithLogsValueLabel));
        summaryCard.add(Box.createRigidArea(new Dimension(20, 0)));
        summaryCard.add(createSummaryItem("Worked Hours", totalWorkedHoursValueLabel));
        summaryCard.add(Box.createRigidArea(new Dimension(20, 0)));
        summaryCard.add(createSummaryItem("Late Minutes", totalLateMinutesValueLabel));
        summaryCard.add(Box.createRigidArea(new Dimension(20, 0)));
        summaryCard.add(createSummaryItem("Undertime Minutes", totalUndertimeMinutesValueLabel));
        summaryCard.add(Box.createRigidArea(new Dimension(20, 0)));
        summaryCard.add(createSummaryItem("Overtime Hours", totalOvertimeHoursValueLabel));

        topWrapper.add(summaryCard);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 18)));

        add(topWrapper, BorderLayout.NORTH);

        // Table
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

        // Listeners
        yearComboBox.addActionListener(e -> {
            refreshMonthOptions();
            loadAttendanceData();
        });

        monthComboBox.addActionListener(e -> loadAttendanceData());

        reloadButton.addActionListener(e -> loadAttendanceData());
    }

    /**
     * Creates one summary block.
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
     * Loads year options based on the actual employee attendance data.
     */
    private void populateYearOptions() {
        yearComboBox.removeAllItems();

        List<Integer> years = new ArrayList<>();

        for (AttendanceRecord record : allEmployeeRecords) {
            int year = record.getDate().getYear();
            if (!years.contains(year)) {
                years.add(year);
            }
        }

        Collections.sort(years);

        for (Integer year : years) {
            yearComboBox.addItem(year);
        }

        // If no data exists, keep the combo empty
        if (yearComboBox.getItemCount() == 0) {
            return;
        }

        // Select the latest available year
        yearComboBox.setSelectedIndex(yearComboBox.getItemCount() - 1);
    }

    /**
     * Refreshes month options depending on the selected year.
     */
    private void refreshMonthOptions() {
        monthComboBox.removeAllItems();

        Integer selectedYear = (Integer) yearComboBox.getSelectedItem();
        if (selectedYear == null) {
            return;
        }

        Map<Integer, MonthItem> availableMonths = new TreeMap<>();

        for (AttendanceRecord record : allEmployeeRecords) {
            if (record.getDate().getYear() == selectedYear) {
                int monthNumber = record.getDate().getMonthValue();

                if (!availableMonths.containsKey(monthNumber)) {
                    availableMonths.put(monthNumber, new MonthItem(monthNumber));
                }
            }
        }

        for (MonthItem item : availableMonths.values()) {
            monthComboBox.addItem(item);
        }

        // Auto-select the last available month
        if (monthComboBox.getItemCount() > 0) {
            monthComboBox.setSelectedIndex(monthComboBox.getItemCount() - 1);
        }
    }

    /**
     * Loads attendance records for the selected year and month.
     */
    private void loadAttendanceData() {

        // Clear existing rows first
        tableModel.setRowCount(0);

        Integer selectedYear = (Integer) yearComboBox.getSelectedItem();
        MonthItem selectedMonth = (MonthItem) monthComboBox.getSelectedItem();

        // If there is no available selection, show zeros and stop
        if (selectedYear == null || selectedMonth == null) {
            updateSummary(0, 0.0, 0, 0, 0.0);
            return;
        }

        List<AttendanceRecord> records = attendanceRepository.findByEmployeeIdAndMonth(
                session.getEmployeeId(),
                selectedYear,
                selectedMonth.getMonthNumber()
        );

        if (records.isEmpty()) {
            updateSummary(0, 0.0, 0, 0, 0.0);
            return;
        }

        int totalLateMinutes = 0;
        int totalUndertimeMinutes = 0;
        double totalWorkedHours = 0.0;
        double totalOvertimeHours = 0.0;

        for (AttendanceRecord record : records) {

            totalLateMinutes += record.getLateMinutesRounded();
            totalUndertimeMinutes += record.getUndertimeMinutesRounded();
            totalWorkedHours += record.getWorkedHours();
            totalOvertimeHours += record.getOvertimeHours();

            tableModel.addRow(new Object[]{
                    record.getDate().format(DATE_FORMAT),
                    record.getTimeIn() == null ? "" : record.getTimeIn().format(TIME_FORMAT),
                    record.getTimeOut() == null ? "" : record.getTimeOut().format(TIME_FORMAT),
                    record.getDayStatus(),
                    HOURS_FORMAT.format(record.getWorkedHours()),
                    record.getLateMinutesRounded(),
                    record.getUndertimeMinutesRounded(),
                    HOURS_FORMAT.format(record.getRegularHours()),
                    HOURS_FORMAT.format(record.getOvertimeHours())
            });
        }

        updateSummary(
                records.size(),
                totalWorkedHours,
                totalLateMinutes,
                totalUndertimeMinutes,
                totalOvertimeHours
        );
    }

    /**
     * Updates the summary values shown above the table.
     */
    private void updateSummary(
            int daysWithLogs,
            double totalWorkedHours,
            int totalLateMinutes,
            int totalUndertimeMinutes,
            double totalOvertimeHours
    ) {
        daysWithLogsValueLabel.setText(String.valueOf(daysWithLogs));
        totalWorkedHoursValueLabel.setText(HOURS_FORMAT.format(totalWorkedHours));
        totalLateMinutesValueLabel.setText(String.valueOf(totalLateMinutes));
        totalUndertimeMinutesValueLabel.setText(String.valueOf(totalUndertimeMinutes));
        totalOvertimeHoursValueLabel.setText(HOURS_FORMAT.format(totalOvertimeHours));
    }

    /**
     * Small helper class for month combo box display.
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
            return monthNumber + " - " + Month.of(monthNumber).name().substring(0, 1)
                    + Month.of(monthNumber).name().substring(1).toLowerCase();
        }
    }
}