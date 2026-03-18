package motorph.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import motorph.model.AttendanceRecord;
import motorph.repository.AttendanceRepository;
import motorph.repository.CsvAttendanceRepository;
import motorph.ui.Theme;
import motorph.ui.security.RoleAccess;
import motorph.ui.session.UserSession;

public class AttendancePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final LocalTime LATE_CUTOFF = LocalTime.of(9, 0);
    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_END = LocalTime.of(13, 0);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public enum ViewScope {
        SELF("Attendance", "View your own attendance records and filter them by payroll period."),
        TEAM("Team Attendance", "Review attendance records for your direct reports."),
        ALL("Attendance Monitoring", "Monitor attendance records across the organization.");

        private final String title;
        private final String subtitle;

        ViewScope(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    private final UserSession session;
    private final ViewScope viewScope;
    private final AttendanceRepository attendanceRepository;

    private final JComboBox<Integer> yearComboBox;
    private final JComboBox<Integer> monthComboBox;
    private final JCheckBox showAllCheckBox;
    private final JTextField searchField;
    private final DefaultTableModel tableModel;
    private final JTable attendanceTable;

    private final JLabel totalDaysPresentValueLabel;
    private final JLabel totalHoursWorkedValueLabel;
    private final JLabel lateCountValueLabel;
    private final JLabel overtimeHoursValueLabel;

    private final List<RowMetric> displayedMetrics;
    private List<AttendanceRecord> scopedRecords;

    public AttendancePanel(UserSession session) {
        this(session, resolveDefaultScope(session));
    }

    public AttendancePanel(UserSession session, ViewScope viewScope) {
        this.session = session;
        this.viewScope = viewScope;
        this.attendanceRepository = new CsvAttendanceRepository();
        this.yearComboBox = new JComboBox<>();
        this.monthComboBox = new JComboBox<>();
        this.showAllCheckBox = new JCheckBox("Show All");
        this.searchField = new JTextField();
        this.tableModel = new DefaultTableModel(
                new Object[] {"Employee ID", "Date", "Log In", "Log Out", "Total Hours", "Late", "Overtime Hrs"},
                0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.attendanceTable = new JTable(tableModel);
        this.totalDaysPresentValueLabel = new JLabel("0");
        this.totalHoursWorkedValueLabel = new JLabel("0.00");
        this.lateCountValueLabel = new JLabel("0");
        this.overtimeHoursValueLabel = new JLabel("0.00");
        this.displayedMetrics = new ArrayList<>();
        this.scopedRecords = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        buildUi();
        loadRecordsFromCsv();
        initializeFilterOptions();
        refreshAttendanceTable();
    }

    private static ViewScope resolveDefaultScope(UserSession session) {
        if (RoleAccess.canViewAllAttendance(session)) {
            return ViewScope.ALL;
        }
        if (RoleAccess.canViewTeamAttendance(session)) {
            return ViewScope.TEAM;
        }
        return ViewScope.SELF;
    }

    private void buildUi() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Theme.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel(viewScope.title);
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(viewScope.subtitle);
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);

        wrapper.add(titleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 6)));
        wrapper.add(subtitleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));
        wrapper.add(buildSummaryCard());
        wrapper.add(Box.createRigidArea(new Dimension(0, 14)));
        wrapper.add(buildFilterCard());
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));
        wrapper.add(buildTableCard());

        add(wrapper, BorderLayout.CENTER);
    }

    private JPanel buildSummaryCard() {
        JPanel summaryCard = new JPanel(new java.awt.GridLayout(1, 4, 12, 0));
        summaryCard.setBackground(Theme.CARD_BACKGROUND);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        summaryCard.setAlignmentX(LEFT_ALIGNMENT);
        summaryCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 104));

        summaryCard.add(createSummaryTile("Total Days Present", totalDaysPresentValueLabel));
        summaryCard.add(createSummaryTile("Total Hours Worked", totalHoursWorkedValueLabel));
        summaryCard.add(createSummaryTile("Late Count", lateCountValueLabel));
        summaryCard.add(createSummaryTile("Overtime Hours", overtimeHoursValueLabel));
        return summaryCard;
    }

    private JPanel buildFilterCard() {
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        filterCard.setBackground(Theme.CARD_BACKGROUND);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        filterCard.setAlignmentX(LEFT_ALIGNMENT);
        filterCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel scopeLabel = new JLabel("Scope: " + buildScopeCaption());
        scopeLabel.setFont(Theme.FONT_BODY);
        scopeLabel.setForeground(Theme.TEXT_PRIMARY);

        yearComboBox.setFont(Theme.FONT_BODY);
        yearComboBox.setPreferredSize(new Dimension(120, 34));
        monthComboBox.setFont(Theme.FONT_BODY);
        monthComboBox.setPreferredSize(new Dimension(120, 34));

        searchField.setFont(Theme.FONT_BODY);
        searchField.setPreferredSize(new Dimension(180, 34));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshAttendanceTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshAttendanceTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshAttendanceTable();
            }
        });

        showAllCheckBox.setFont(Theme.FONT_BODY);
        showAllCheckBox.setOpaque(false);
        showAllCheckBox.addActionListener(e -> {
            boolean showAll = showAllCheckBox.isSelected();
            yearComboBox.setEnabled(!showAll);
            monthComboBox.setEnabled(!showAll);
            refreshAttendanceTable();
        });

        JButton applyButton = new JButton("Apply Filter");
        applyButton.setFont(Theme.FONT_BUTTON);
        applyButton.setBackground(Theme.PRIMARY);
        applyButton.setForeground(Color.WHITE);
        applyButton.setFocusPainted(false);
        applyButton.addActionListener(e -> refreshAttendanceTable());

        filterCard.add(scopeLabel);
        filterCard.add(Box.createRigidArea(new Dimension(12, 0)));
        filterCard.add(new JLabel("Year:"));
        filterCard.add(yearComboBox);
        filterCard.add(new JLabel("Month:"));
        filterCard.add(monthComboBox);
        filterCard.add(new JLabel("Search:"));
        filterCard.add(searchField);
        filterCard.add(showAllCheckBox);
        filterCard.add(applyButton);
        return filterCard;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);

        attendanceTable.setFont(Theme.FONT_BODY);
        attendanceTable.setRowHeight(30);
        attendanceTable.getTableHeader().setFont(Theme.FONT_BUTTON);
        attendanceTable.getTableHeader().setBackground(new Color(244, 247, 255));
        attendanceTable.getTableHeader().setForeground(Theme.TEXT_PRIMARY);
        attendanceTable.setFillsViewportHeight(true);
        attendanceTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column
            ) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    return c;
                }

                int modelRow = table.convertRowIndexToModel(row);
                if (modelRow < 0 || modelRow >= displayedMetrics.size()) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Theme.TEXT_PRIMARY);
                    return c;
                }

                RowMetric metric = displayedMetrics.get(modelRow);
                if (metric.isLate) {
                    c.setBackground(new Color(255, 235, 235));
                    c.setForeground(new Color(140, 35, 35));
                } else if (metric.overtimeHours > 0.0) {
                    c.setBackground(new Color(232, 248, 232));
                    c.setForeground(new Color(28, 92, 28));
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Theme.TEXT_PRIMARY);
                }
                return c;
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(attendanceTable);
        tableScrollPane.setPreferredSize(new Dimension(1050, 480));
        card.add(tableScrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSummaryTile(String labelText, JLabel valueLabel) {
        JPanel itemPanel = new JPanel();
        itemPanel.setBackground(Theme.CARD_BACKGROUND);
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

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

    private void loadRecordsFromCsv() {
        Set<String> scopedEmployeeIds = resolveScopedEmployeeIds();
        List<AttendanceRecord> loaded = attendanceRepository.findAll();

        scopedRecords = new ArrayList<>();
        for (AttendanceRecord record : loaded) {
            if (scopedEmployeeIds.contains(record.getEmployeeId())) {
                scopedRecords.add(record);
            }
        }

        scopedRecords.sort(Comparator.comparing(AttendanceRecord::getDate));
    }

    private Set<String> resolveScopedEmployeeIds() {
        Set<String> ids = new LinkedHashSet<>();
        ids.add(session.getEmployeeId());

        if (viewScope == ViewScope.TEAM) {
            ids.addAll(RoleAccess.resolveSubordinateEmployeeIds(session));
        } else if (viewScope == ViewScope.ALL) {
            ids.clear();
            for (AttendanceRecord record : attendanceRepository.findAll()) {
                ids.add(record.getEmployeeId());
            }
        }

        return ids;
    }

    private void initializeFilterOptions() {
        yearComboBox.removeAllItems();
        monthComboBox.removeAllItems();

        Set<Integer> years = new LinkedHashSet<>();
        for (AttendanceRecord record : scopedRecords) {
            years.add(record.getDate().getYear());
        }

        if (years.isEmpty()) {
            years.add(java.time.LocalDate.now().getYear());
        }

        for (Integer year : years) {
            yearComboBox.addItem(year);
        }
        for (int month = 1; month <= 12; month++) {
            monthComboBox.addItem(month);
        }

        if (!scopedRecords.isEmpty()) {
            AttendanceRecord latest = scopedRecords.get(scopedRecords.size() - 1);
            yearComboBox.setSelectedItem(latest.getDate().getYear());
            monthComboBox.setSelectedItem(latest.getDate().getMonthValue());
        }
    }

    private void refreshAttendanceTable() {
        tableModel.setRowCount(0);
        displayedMetrics.clear();

        Integer selectedYear = (Integer) yearComboBox.getSelectedItem();
        Integer selectedMonth = (Integer) monthComboBox.getSelectedItem();
        boolean showAll = showAllCheckBox.isSelected();
        String query = safeLower(searchField.getText());

        if (!showAll && (selectedYear == null || selectedMonth == null)) {
            return;
        }

        int totalDaysPresent = 0;
        int lateCount = 0;
        double totalHoursWorked = 0.0;
        double totalOvertimeHours = 0.0;

        for (AttendanceRecord record : scopedRecords) {
            if (!showAll && (record.getDate().getYear() != selectedYear || record.getDate().getMonthValue() != selectedMonth)) {
                continue;
            }
            if (!query.isEmpty() && !matchesSearch(record, query)) {
                continue;
            }

            double workedHours = computeWorkedHours(record);
            double overtimeHours = computeOvertimeHours(workedHours);
            boolean isLate = isLate(record);

            if (workedHours > 0.0) {
                totalDaysPresent++;
            }

            totalHoursWorked += workedHours;
            totalOvertimeHours += overtimeHours;
            if (isLate) {
                lateCount++;
            }

            displayedMetrics.add(new RowMetric(overtimeHours, isLate));
            tableModel.addRow(new Object[] {
                    record.getEmployeeId(),
                    record.getDate().format(DATE_FMT),
                    record.getTimeIn() == null ? "" : record.getTimeIn().format(TIME_FMT),
                    record.getTimeOut() == null ? "" : record.getTimeOut().format(TIME_FMT),
                    String.format(Locale.ENGLISH, "%.2f", workedHours),
                    isLate ? "YES" : "NO",
                    String.format(Locale.ENGLISH, "%.2f", overtimeHours)
            });
        }

        totalDaysPresentValueLabel.setText(String.valueOf(totalDaysPresent));
        totalHoursWorkedValueLabel.setText(String.format(Locale.ENGLISH, "%.2f", totalHoursWorked));
        lateCountValueLabel.setText(String.valueOf(lateCount));
        overtimeHoursValueLabel.setText(String.format(Locale.ENGLISH, "%.2f", totalOvertimeHours));
        attendanceTable.repaint();
    }

    private boolean matchesSearch(AttendanceRecord record, String query) {
        String employeeId = safeLower(record.getEmployeeId());
        String firstName = safeLower(record.getFirstName());
        String lastName = safeLower(record.getLastName());
        String fullName = safeLower(record.getFirstName() + " " + record.getLastName());
        String dateText = safeLower(record.getDate().format(DATE_FMT));

        return employeeId.contains(query)
                || firstName.contains(query)
                || lastName.contains(query)
                || fullName.contains(query)
                || dateText.contains(query);
    }

    private String buildScopeCaption() {
        if (viewScope == ViewScope.ALL) {
            return "ALL EMPLOYEES";
        }
        if (viewScope == ViewScope.TEAM) {
            return "SELF + TEAM";
        }
        return "SELF";
    }

    private String safeLower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ENGLISH).trim();
    }

    private double computeWorkedHours(AttendanceRecord record) {
        if (record == null || record.getTimeIn() == null || record.getTimeOut() == null) {
            return 0.0;
        }

        long workedMinutes = Duration.between(record.getTimeIn(), record.getTimeOut()).toMinutes();
        if (workedMinutes <= 0) {
            return 0.0;
        }

        long lunchOverlap = computeOverlapMinutes(record.getTimeIn(), record.getTimeOut(), LUNCH_START, LUNCH_END);
        return Math.max(0, workedMinutes - lunchOverlap) / 60.0;
    }

    private long computeOverlapMinutes(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        LocalTime overlapStart = startA.isAfter(startB) ? startA : startB;
        LocalTime overlapEnd = endA.isBefore(endB) ? endA : endB;
        if (!overlapEnd.isAfter(overlapStart)) {
            return 0L;
        }
        return Duration.between(overlapStart, overlapEnd).toMinutes();
    }

    private boolean isLate(AttendanceRecord record) {
        return record != null && record.getTimeIn() != null && record.getTimeIn().isAfter(LATE_CUTOFF);
    }

    private double computeOvertimeHours(double workedHours) {
        return Math.max(0.0, workedHours - 8.0);
    }

    private static class RowMetric {
        private final double overtimeHours;
        private final boolean isLate;

        private RowMetric(double overtimeHours, boolean isLate) {
            this.overtimeHours = overtimeHours;
            this.isLate = isLate;
        }
    }
}
