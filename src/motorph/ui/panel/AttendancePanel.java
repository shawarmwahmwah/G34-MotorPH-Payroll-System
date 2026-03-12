package motorph.ui.panel;

import motorph.model.AttendanceAdjustmentRequest;
import motorph.model.AttendanceRecord;
import motorph.model.Employee;
import motorph.repository.AttendanceRepository;
import motorph.repository.CsvAttendanceRepository;
import motorph.repository.CsvEmployeeRepository;
import motorph.repository.EmployeeRepository;
import motorph.service.AttendanceAdjustmentService;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * AttendancePanel
 *
 * Compact attendance view with embedded adjustment section.
 * - Employee / IT: own attendance only
 * - HR / Admin / Supervisor: can view other employees
 * - HR / Admin / Supervisor: can open embedded adjustment section
 * - HR: can submit adjustment requests
 * - Admin: can approve/reject requests
 * - Supervisor: view adjustment requests only
 */
public class AttendancePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final UserSession session;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceAdjustmentService adjustmentService;

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

    private final DefaultTableModel attendanceTableModel;
    private final DefaultTableModel adjustmentTableModel;

    private final JPanel adjustmentContainer;
    private final CardLayout adjustmentCardLayout;
    private static final String ADJUSTMENT_HIDDEN = "HIDDEN";
    private static final String ADJUSTMENT_VISIBLE = "VISIBLE";
    private boolean adjustmentVisible = false;

    // Embedded adjustment form fields
    private final JComboBox<EmployeeItem> adjustmentEmployeeComboBox;
    private final JSpinner adjustmentDateSpinner;
    private final JSpinner proposedTimeInSpinner;
    private final JSpinner proposedTimeOutSpinner;
    private final JTextArea adjustmentReasonArea;
    private final JTextField adjustmentRemarksField;

    private List<Employee> allEmployees;
    private List<AttendanceRecord> currentEmployeeRecords;

    private static final DecimalFormat HOURS_FORMAT = new DecimalFormat("0.00");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public AttendancePanel(UserSession session) {
        this.session = session;
        this.attendanceRepository = new CsvAttendanceRepository();
        this.employeeRepository = new CsvEmployeeRepository();
        this.adjustmentService = new AttendanceAdjustmentService();

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

        this.attendanceTableModel = new DefaultTableModel(
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

        this.adjustmentTableModel = new DefaultTableModel(
                new Object[]{
                        "Request ID",
                        "Employee ID",
                        "Employee Name",
                        "Date",
                        "Current In",
                        "Current Out",
                        "Proposed In",
                        "Proposed Out",
                        "Requested By",
                        "Status"
                },
                0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.adjustmentEmployeeComboBox = new JComboBox<>();
        this.adjustmentDateSpinner = new JSpinner(new SpinnerDateModel());
        this.proposedTimeInSpinner = new JSpinner(new SpinnerDateModel());
        this.proposedTimeOutSpinner = new JSpinner(new SpinnerDateModel());
        this.adjustmentReasonArea = new JTextArea(4, 20);
        this.adjustmentRemarksField = new JTextField();

        this.adjustmentCardLayout = new CardLayout();
        this.adjustmentContainer = new JPanel(adjustmentCardLayout);

        this.allEmployees = employeeRepository.findAll();
        this.currentEmployeeRecords = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        buildUI();
        initializeEmployeeSelection();
        loadAttendanceForCurrentSelection();
        loadAdjustmentEmployeeDropdown();
        loadAdjustmentTable();
    }

    /**
     * Builds the full Attendance panel UI.
     */
    private void buildUI() {

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Theme.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel("Attendance");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("View employee attendance records and monthly summaries.");
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);

        wrapper.add(titleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 6)));
        wrapper.add(subtitleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 18)));

        // Top compact toolbar card
        JPanel toolbarCard = new JPanel();
        toolbarCard.setLayout(new BoxLayout(toolbarCard, BoxLayout.Y_AXIS));
        toolbarCard.setBackground(Theme.CARD_BACKGROUND);
        toolbarCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        toolbarCard.setAlignmentX(LEFT_ALIGNMENT);
        toolbarCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // Row 1: employee area
        JPanel employeeRow = new JPanel(new GridBagLayout());
        employeeRow.setBackground(Theme.CARD_BACKGROUND);
        employeeRow.setAlignmentX(LEFT_ALIGNMENT);

        GridBagConstraints gbcEmp = new GridBagConstraints();
        gbcEmp.insets = new java.awt.Insets(4, 4, 4, 12);
        gbcEmp.anchor = GridBagConstraints.WEST;
        gbcEmp.fill = GridBagConstraints.HORIZONTAL;
        gbcEmp.gridy = 0;

        if (canViewOtherEmployees()) {
            JLabel searchLabel = new JLabel("Search:");
            searchLabel.setFont(Theme.FONT_BODY);
            searchLabel.setForeground(Theme.TEXT_PRIMARY);

            employeeSearchField.setFont(Theme.FONT_BODY);
            employeeSearchField.setPreferredSize(new Dimension(220, 34));
            employeeSearchField.setMaximumSize(new Dimension(220, 34));

            JButton searchButton = new JButton("Search");
            searchButton.setFont(Theme.FONT_BUTTON);
            searchButton.setBackground(Theme.PRIMARY);
            searchButton.setForeground(Color.WHITE);
            searchButton.setFocusPainted(false);
            searchButton.setPreferredSize(new Dimension(110, 36));

            JLabel employeeLabel = new JLabel("Employee:");
            employeeLabel.setFont(Theme.FONT_BODY);
            employeeLabel.setForeground(Theme.TEXT_PRIMARY);

            employeeComboBox.setFont(Theme.FONT_BODY);
            employeeComboBox.setPreferredSize(new Dimension(360, 34));
            employeeComboBox.setMaximumSize(new Dimension(360, 34));

            gbcEmp.gridx = 0;
            gbcEmp.weightx = 0;
            employeeRow.add(searchLabel, gbcEmp);

            gbcEmp.gridx = 1;
            gbcEmp.weightx = 0.25;
            employeeRow.add(employeeSearchField, gbcEmp);

            gbcEmp.gridx = 2;
            gbcEmp.weightx = 0;
            employeeRow.add(searchButton, gbcEmp);

            gbcEmp.gridx = 3;
            gbcEmp.weightx = 0;
            employeeRow.add(employeeLabel, gbcEmp);

            gbcEmp.gridx = 4;
            gbcEmp.weightx = 0.45;
            employeeRow.add(employeeComboBox, gbcEmp);

            searchButton.addActionListener(e -> filterEmployeeDropdown());

            employeeComboBox.addActionListener(e -> {
                populateYearOptions();
                refreshMonthOptions();
                loadAttendanceData();
                updateSelectedEmployeeLabel();
            });

        } else {
            JLabel selfOnlyLabel = new JLabel("You can only view your own attendance records.");
            selfOnlyLabel.setFont(Theme.FONT_BODY);
            selfOnlyLabel.setForeground(Theme.TEXT_SECONDARY);

            gbcEmp.gridx = 0;
            gbcEmp.weightx = 1.0;
            employeeRow.add(selfOnlyLabel, gbcEmp);

            Employee self = employeeRepository.findById(session.getEmployeeId());
            employeeComboBox.removeAllItems();
            if (self != null) {
                employeeComboBox.addItem(new EmployeeItem(self));
                employeeComboBox.setSelectedIndex(0);
            }
        }

        // Row 2: filters and actions
        JPanel filterRow = new JPanel(new BorderLayout());
        filterRow.setBackground(Theme.CARD_BACKGROUND);
        filterRow.setAlignmentX(LEFT_ALIGNMENT);

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
        monthComboBox.setPreferredSize(new Dimension(190, 34));
        monthComboBox.setMaximumSize(new Dimension(190, 34));

        filterLeft.add(yearLabel);
        filterLeft.add(Box.createRigidArea(new Dimension(10, 0)));
        filterLeft.add(yearComboBox);
        filterLeft.add(Box.createRigidArea(new Dimension(20, 0)));
        filterLeft.add(monthLabel);
        filterLeft.add(Box.createRigidArea(new Dimension(10, 0)));
        filterLeft.add(monthComboBox);

        JPanel filterRight = new JPanel();
        filterRight.setBackground(Theme.CARD_BACKGROUND);
        filterRight.setLayout(new BoxLayout(filterRight, BoxLayout.X_AXIS));

        JButton adjustAttendanceButton = new JButton("Adjust Attendance");
        adjustAttendanceButton.setFont(Theme.FONT_BUTTON);
        adjustAttendanceButton.setBackground(Theme.PRIMARY);
        adjustAttendanceButton.setForeground(Color.WHITE);
        adjustAttendanceButton.setFocusPainted(false);
        adjustAttendanceButton.setPreferredSize(new Dimension(170, 40));
        adjustAttendanceButton.setMinimumSize(new Dimension(170, 40));
        adjustAttendanceButton.setMaximumSize(new Dimension(170, 40));

        JButton reloadButton = new JButton("Load Attendance");
        reloadButton.setFont(Theme.FONT_BUTTON);
        reloadButton.setBackground(Theme.PRIMARY);
        reloadButton.setForeground(Color.WHITE);
        reloadButton.setFocusPainted(false);
        reloadButton.setPreferredSize(new Dimension(170, 40));
        reloadButton.setMinimumSize(new Dimension(170, 40));
        reloadButton.setMaximumSize(new Dimension(170, 40));

        if (canAdjustAttendance()) {
            filterRight.add(adjustAttendanceButton);
            filterRight.add(Box.createRigidArea(new Dimension(10, 0)));
        }

        filterRight.add(reloadButton);

        filterRow.add(filterLeft, BorderLayout.WEST);
        filterRow.add(filterRight, BorderLayout.EAST);

        toolbarCard.add(employeeRow);
        toolbarCard.add(Box.createRigidArea(new Dimension(0, 10)));
        toolbarCard.add(filterRow);

        wrapper.add(toolbarCard);
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));

        // Embedded adjustment section
        adjustmentContainer.setBackground(Theme.CONTENT_BACKGROUND);
        adjustmentContainer.setAlignmentX(LEFT_ALIGNMENT);

        JPanel hiddenPanel = new JPanel();
        hiddenPanel.setBackground(Theme.CONTENT_BACKGROUND);

        JPanel visiblePanel = buildEmbeddedAdjustmentSection();

        adjustmentContainer.add(hiddenPanel, ADJUSTMENT_HIDDEN);
        adjustmentContainer.add(visiblePanel, ADJUSTMENT_VISIBLE);
        adjustmentCardLayout.show(adjustmentContainer, ADJUSTMENT_HIDDEN);

        wrapper.add(adjustmentContainer);
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));

        // Summary card
        JPanel summaryCard = new JPanel(new GridLayout(1, 6, 14, 0));
        summaryCard.setBackground(Theme.CARD_BACKGROUND);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        summaryCard.setAlignmentX(LEFT_ALIGNMENT);
        summaryCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        summaryCard.add(createSummaryItem("Employee", selectedEmployeeValueLabel));
        summaryCard.add(createSummaryItem("Days with Logs", daysWithLogsValueLabel));
        summaryCard.add(createSummaryItem("Worked Hours", totalWorkedHoursValueLabel));
        summaryCard.add(createSummaryItem("Late Hours", totalLateHoursValueLabel));
        summaryCard.add(createSummaryItem("Undertime Hours", totalUndertimeHoursValueLabel));
        summaryCard.add(createSummaryItem("Overtime Hours", totalOvertimeHoursValueLabel));

        wrapper.add(summaryCard);
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));

        // Attendance table card
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Theme.CARD_BACKGROUND);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        tableCard.setAlignmentX(LEFT_ALIGNMENT);

        JTable attendanceTable = new JTable(attendanceTableModel);
        attendanceTable.setRowHeight(28);
        attendanceTable.setFont(Theme.FONT_BODY);
        attendanceTable.getTableHeader().setFont(Theme.FONT_BUTTON);
        attendanceTable.setFillsViewportHeight(true);
        attendanceTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane attendanceScrollPane = new JScrollPane(attendanceTable);
        attendanceScrollPane.setPreferredSize(new Dimension(1000, 360));

        tableCard.add(attendanceScrollPane, BorderLayout.CENTER);

        wrapper.add(tableCard);

        JScrollPane pageScrollPane = new JScrollPane(wrapper);
        pageScrollPane.setBorder(null);
        pageScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(pageScrollPane, BorderLayout.CENTER);

        // Events
        yearComboBox.addActionListener(e -> {
            refreshMonthOptions();
            loadAttendanceData();
        });

        monthComboBox.addActionListener(e -> loadAttendanceData());
        reloadButton.addActionListener(e -> loadAttendanceForCurrentSelection());

        if (canAdjustAttendance()) {
            adjustAttendanceButton.addActionListener(e -> toggleAdjustmentSection());
        }
    }

    /**
     * Embedded adjustment section shown inside Attendance page.
     */
    private JPanel buildEmbeddedAdjustmentSection() {

        JPanel sectionWrapper = new JPanel();
        sectionWrapper.setLayout(new BoxLayout(sectionWrapper, BoxLayout.Y_AXIS));
        sectionWrapper.setBackground(Theme.CONTENT_BACKGROUND);
        sectionWrapper.setAlignmentX(LEFT_ALIGNMENT);

        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(Theme.CARD_BACKGROUND);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        formCard.setAlignmentX(LEFT_ALIGNMENT);
        formCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        JLabel heading = new JLabel("Attendance Adjustment");
        heading.setFont(Theme.FONT_HEADING);
        heading.setForeground(Theme.TEXT_PRIMARY);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JPanel row1 = new JPanel(new GridBagLayout());
        row1.setBackground(Theme.CARD_BACKGROUND);
        row1.setAlignmentX(LEFT_ALIGNMENT);

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.insets = new java.awt.Insets(4, 4, 4, 12);
        gbc1.anchor = GridBagConstraints.WEST;
        gbc1.fill = GridBagConstraints.HORIZONTAL;

        JLabel employeeLabel = new JLabel("Employee:");
        employeeLabel.setFont(Theme.FONT_BODY);

        adjustmentEmployeeComboBox.setFont(Theme.FONT_BODY);
        adjustmentEmployeeComboBox.setPreferredSize(new Dimension(360, 34));
        adjustmentEmployeeComboBox.setMaximumSize(new Dimension(360, 34));

        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setFont(Theme.FONT_BODY);

        adjustmentDateSpinner.setFont(Theme.FONT_BODY);
        adjustmentDateSpinner.setPreferredSize(new Dimension(170, 34));
        adjustmentDateSpinner.setMaximumSize(new Dimension(170, 34));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(adjustmentDateSpinner, "yyyy-MM-dd");
        adjustmentDateSpinner.setEditor(dateEditor);

        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 0;
        row1.add(employeeLabel, gbc1);

        gbc1.gridx = 1;
        gbc1.weightx = 1.0;
        row1.add(adjustmentEmployeeComboBox, gbc1);

        gbc1.gridx = 2;
        gbc1.weightx = 0;
        row1.add(dateLabel, gbc1);

        gbc1.gridx = 3;
        gbc1.weightx = 0.35;
        row1.add(adjustmentDateSpinner, gbc1);

        JPanel row2 = new JPanel(new GridBagLayout());
        row2.setBackground(Theme.CARD_BACKGROUND);
        row2.setAlignmentX(LEFT_ALIGNMENT);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new java.awt.Insets(4, 4, 4, 12);
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        JLabel timeInLabel = new JLabel("Proposed Time In:");
        timeInLabel.setFont(Theme.FONT_BODY);

        proposedTimeInSpinner.setFont(Theme.FONT_BODY);
        proposedTimeInSpinner.setPreferredSize(new Dimension(160, 34));
        proposedTimeInSpinner.setMaximumSize(new Dimension(160, 34));
        JSpinner.DateEditor timeInEditor = new JSpinner.DateEditor(proposedTimeInSpinner, "hh:mm a");
        proposedTimeInSpinner.setEditor(timeInEditor);

        JLabel timeOutLabel = new JLabel("Proposed Time Out:");
        timeOutLabel.setFont(Theme.FONT_BODY);

        proposedTimeOutSpinner.setFont(Theme.FONT_BODY);
        proposedTimeOutSpinner.setPreferredSize(new Dimension(160, 34));
        proposedTimeOutSpinner.setMaximumSize(new Dimension(160, 34));
        JSpinner.DateEditor timeOutEditor = new JSpinner.DateEditor(proposedTimeOutSpinner, "hh:mm a");
        proposedTimeOutSpinner.setEditor(timeOutEditor);

        gbc2.gridx = 0;
        gbc2.gridy = 0;
        gbc2.weightx = 0;
        row2.add(timeInLabel, gbc2);

        gbc2.gridx = 1;
        gbc2.weightx = 1.0;
        row2.add(proposedTimeInSpinner, gbc2);

        gbc2.gridx = 2;
        gbc2.weightx = 0;
        row2.add(timeOutLabel, gbc2);

        gbc2.gridx = 3;
        gbc2.weightx = 0.35;
        row2.add(proposedTimeOutSpinner, gbc2);

        JLabel reasonLabel = new JLabel("Reason:");
        reasonLabel.setFont(Theme.FONT_BODY);
        reasonLabel.setAlignmentX(LEFT_ALIGNMENT);

        adjustmentReasonArea.setLineWrap(true);
        adjustmentReasonArea.setWrapStyleWord(true);
        adjustmentReasonArea.setFont(Theme.FONT_BODY);
        adjustmentReasonArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane reasonScroll = new JScrollPane(adjustmentReasonArea);
        reasonScroll.setPreferredSize(new Dimension(900, 100));
        reasonScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        reasonScroll.setAlignmentX(LEFT_ALIGNMENT);

        JPanel actionRow = new JPanel(new BorderLayout());
        actionRow.setBackground(Theme.CARD_BACKGROUND);
        actionRow.setAlignmentX(LEFT_ALIGNMENT);

        JPanel leftButtons = new JPanel();
        leftButtons.setBackground(Theme.CARD_BACKGROUND);
        leftButtons.setLayout(new BoxLayout(leftButtons, BoxLayout.X_AXIS));

        JButton submitButton = new JButton("Submit Request");
        submitButton.setFont(Theme.FONT_BUTTON);
        submitButton.setBackground(Theme.PRIMARY);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setPreferredSize(new Dimension(160, 40));
        submitButton.setMaximumSize(new Dimension(160, 40));

        if (canSubmitAdjustmentRequests()) {
            leftButtons.add(submitButton);
        }

        JPanel rightButtons = new JPanel();
        rightButtons.setBackground(Theme.CARD_BACKGROUND);
        rightButtons.setLayout(new BoxLayout(rightButtons, BoxLayout.X_AXIS));

        JLabel remarksLabel = new JLabel("Remarks:");
        remarksLabel.setFont(Theme.FONT_BODY);

        adjustmentRemarksField.setFont(Theme.FONT_BODY);
        adjustmentRemarksField.setPreferredSize(new Dimension(220, 34));
        adjustmentRemarksField.setMaximumSize(new Dimension(220, 34));

        JButton approveButton = new JButton("Approve Selected");
        approveButton.setFont(Theme.FONT_BUTTON);
        approveButton.setBackground(Theme.PRIMARY);
        approveButton.setForeground(Color.WHITE);
        approveButton.setFocusPainted(false);
        approveButton.setPreferredSize(new Dimension(160, 40));

        JButton rejectButton = new JButton("Reject Selected");
        rejectButton.setFont(Theme.FONT_BUTTON);
        rejectButton.setBackground(new Color(200, 80, 80));
        rejectButton.setForeground(Color.WHITE);
        rejectButton.setFocusPainted(false);
        rejectButton.setPreferredSize(new Dimension(150, 40));

        if (canApproveAdjustmentRequests()) {
            rightButtons.add(remarksLabel);
            rightButtons.add(Box.createRigidArea(new Dimension(8, 0)));
            rightButtons.add(adjustmentRemarksField);
            rightButtons.add(Box.createRigidArea(new Dimension(10, 0)));
            rightButtons.add(approveButton);
            rightButtons.add(Box.createRigidArea(new Dimension(8, 0)));
            rightButtons.add(rejectButton);
        }

        actionRow.add(leftButtons, BorderLayout.WEST);
        actionRow.add(rightButtons, BorderLayout.EAST);

        formCard.add(heading);
        formCard.add(Box.createRigidArea(new Dimension(0, 12)));
        formCard.add(row1);
        formCard.add(Box.createRigidArea(new Dimension(0, 12)));
        formCard.add(row2);
        formCard.add(Box.createRigidArea(new Dimension(0, 12)));
        formCard.add(reasonLabel);
        formCard.add(Box.createRigidArea(new Dimension(0, 6)));
        formCard.add(reasonScroll);
        formCard.add(Box.createRigidArea(new Dimension(0, 12)));
        formCard.add(actionRow);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Theme.CARD_BACKGROUND);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        tableCard.setAlignmentX(LEFT_ALIGNMENT);

        JTable adjustmentTable = new JTable(adjustmentTableModel);
        adjustmentTable.setRowHeight(28);
        adjustmentTable.setFont(Theme.FONT_BODY);
        adjustmentTable.getTableHeader().setFont(Theme.FONT_BUTTON);
        adjustmentTable.setFillsViewportHeight(true);
        adjustmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane adjustmentScrollPane = new JScrollPane(adjustmentTable);
        adjustmentScrollPane.setPreferredSize(new Dimension(1000, 220));

        tableCard.add(adjustmentScrollPane, BorderLayout.CENTER);

        sectionWrapper.add(formCard);
        sectionWrapper.add(Box.createRigidArea(new Dimension(0, 14)));
        sectionWrapper.add(tableCard);

        submitButton.addActionListener(e -> submitEmbeddedAdjustmentRequest());
        approveButton.addActionListener(e -> approveSelectedAdjustmentRequest(adjustmentTable));
        rejectButton.addActionListener(e -> rejectSelectedAdjustmentRequest(adjustmentTable));

        return sectionWrapper;
    }

    /**
     * Creates one summary block.
     */
    private JPanel createSummaryItem(String labelText, JLabel valueLabel) {

        JPanel itemPanel = new JPanel();
        itemPanel.setBackground(Theme.CARD_BACKGROUND);
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

        JLabel label = new JLabel("<html><div style='text-align:left;'>" + labelText + "</div></html>");
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
     * Initializes employee selection depending on role.
     */
    private void initializeEmployeeSelection() {

        if (canViewOtherEmployees()) {
            populateEmployeeDropdown(allEmployees);
            selectEmployeeInDropdown(session.getEmployeeId());
        } else {
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
     * Loads adjustment employee dropdown.
     */
    private void loadAdjustmentEmployeeDropdown() {
        adjustmentEmployeeComboBox.removeAllItems();

        List<Employee> employees = new ArrayList<>(employeeRepository.findAll());
        employees.sort(Comparator.comparing(Employee::getFullName));

        for (Employee employee : employees) {
            adjustmentEmployeeComboBox.addItem(new EmployeeItem(employee));
        }
    }

    /**
     * Filters employee dropdown.
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
     * Fills employee selector.
     */
    private void populateEmployeeDropdown(List<Employee> employees) {
        employeeComboBox.removeAllItems();

        employees.sort(Comparator.comparing(Employee::getFullName));

        for (Employee employee : employees) {
            employeeComboBox.addItem(new EmployeeItem(employee));
        }
    }

    /**
     * Select employee by ID.
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
     * Toggle embedded adjustment section.
     */
    private void toggleAdjustmentSection() {
        adjustmentVisible = !adjustmentVisible;

        if (adjustmentVisible) {
            adjustmentCardLayout.show(adjustmentContainer, ADJUSTMENT_VISIBLE);
            loadAdjustmentTable();
        } else {
            adjustmentCardLayout.show(adjustmentContainer, ADJUSTMENT_HIDDEN);
        }

        revalidate();
        repaint();
    }

    /**
     * Loads attendance for current filter.
     */
    private void loadAttendanceForCurrentSelection() {
        populateYearOptions();
        refreshMonthOptions();
        loadAttendanceData();
        updateSelectedEmployeeLabel();
    }

    /**
     * Loads year options.
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
     * Loads month options.
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
     * Loads attendance table and summary.
     */
    private void loadAttendanceData() {
        attendanceTableModel.setRowCount(0);

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

            attendanceTableModel.addRow(new Object[]{
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
     * Loads adjustment requests into embedded table.
     */
    private void loadAdjustmentTable() {
        adjustmentTableModel.setRowCount(0);

        List<AttendanceAdjustmentRequest> requests = adjustmentService.findAll();

        for (AttendanceAdjustmentRequest request : requests) {
            adjustmentTableModel.addRow(new Object[]{
                    request.getRequestId(),
                    request.getEmployeeId(),
                    request.getEmployeeName(),
                    request.getDate(),
                    request.getCurrentTimeIn(),
                    request.getCurrentTimeOut(),
                    request.getProposedTimeIn(),
                    request.getProposedTimeOut(),
                    request.getRequestedByName(),
                    request.getStatus()
            });
        }
    }

    /**
     * Submit request from embedded section.
     */
    private void submitEmbeddedAdjustmentRequest() {

        EmployeeItem selectedItem = (EmployeeItem) adjustmentEmployeeComboBox.getSelectedItem();
        Employee currentUser = employeeRepository.findById(session.getEmployeeId());

        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an employee.");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

        String selectedDate = dateFormat.format((Date) adjustmentDateSpinner.getValue());
        String proposedTimeIn = timeFormat.format((Date) proposedTimeInSpinner.getValue());
        String proposedTimeOut = timeFormat.format((Date) proposedTimeOutSpinner.getValue());

        boolean success = adjustmentService.submitRequest(
                selectedItem.getEmployee(),
                currentUser,
                selectedDate,
                proposedTimeIn,
                proposedTimeOut,
                adjustmentReasonArea.getText().trim()
        );

        JOptionPane.showMessageDialog(this, adjustmentService.getLastMessage());

        if (success) {
            adjustmentDateSpinner.setValue(new Date());
            proposedTimeInSpinner.setValue(new Date());
            proposedTimeOutSpinner.setValue(new Date());
            adjustmentReasonArea.setText("");
            loadAdjustmentTable();
            loadAttendanceForCurrentSelection();
        }
    }

    /**
     * Approve selected request from embedded section.
     */
    private void approveSelectedAdjustmentRequest(JTable adjustmentTable) {

        int selectedRow = adjustmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a request first.");
            return;
        }

        String requestId = String.valueOf(adjustmentTableModel.getValueAt(selectedRow, 0));
        Employee currentUser = employeeRepository.findById(session.getEmployeeId());

        boolean success = adjustmentService.approveRequest(
                requestId,
                currentUser,
                adjustmentRemarksField.getText().trim()
        );
        JOptionPane.showMessageDialog(this, adjustmentService.getLastMessage());

        if (success) {
            adjustmentRemarksField.setText("");
            loadAdjustmentTable();
            loadAttendanceForCurrentSelection();
        }
    }

    /**
     * Reject selected request from embedded section.
     */
    private void rejectSelectedAdjustmentRequest(JTable adjustmentTable) {

        int selectedRow = adjustmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a request first.");
            return;
        }

        String requestId = String.valueOf(adjustmentTableModel.getValueAt(selectedRow, 0));
        Employee currentUser = employeeRepository.findById(session.getEmployeeId());

        boolean success = adjustmentService.rejectRequest(
                requestId,
                currentUser,
                adjustmentRemarksField.getText().trim()
        );

        JOptionPane.showMessageDialog(this, adjustmentService.getLastMessage());

        if (success) {
            adjustmentRemarksField.setText("");
            loadAdjustmentTable();
        }
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
     * Updates selected employee label.
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
     * HR / Admin / Supervisor can open embedded adjustment section.
     */
    private boolean canAdjustAttendance() {
        String role = safeLower(session.getRole());
        return role.contains("hr") || role.contains("admin") || role.contains("supervisor");
    }

    /**
     * HR can submit.
     */
    private boolean canSubmitAdjustmentRequests() {
        String role = safeLower(session.getRole());
        return role.contains("hr");
    }

    /**
     * Admin can approve/reject.
     */
    private boolean canApproveAdjustmentRequests() {
        String role = safeLower(session.getRole());
        return role.contains("admin");
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
            return employee.getFullName() + " (" + employee.getEmployeeId() + ")";
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