package motorph.ui.panel;

import motorph.model.AttendanceAdjustmentRequest;
import motorph.model.Employee;
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
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * AttendanceAdjustmentPanel
 *
 * HR submits requests.
 * Admin approves/rejects.
 * Supervisor views only.
 */
public class AttendanceAdjustmentPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final UserSession session;
    private final EmployeeRepository employeeRepository;
    private final AttendanceAdjustmentService adjustmentService;

    private final JComboBox<EmployeeItem> employeeComboBox;
    private final JSpinner dateSpinner;
    private final JSpinner proposedTimeInSpinner;
    private final JSpinner proposedTimeOutSpinner;
    private final JTextArea reasonArea;
    private final JTextField remarksField;

    private final DefaultTableModel tableModel;
    private final JTable requestTable;

    public AttendanceAdjustmentPanel(UserSession session) {
        this.session = session;
        this.employeeRepository = new CsvEmployeeRepository();
        this.adjustmentService = new AttendanceAdjustmentService();

        this.employeeComboBox = new JComboBox<>();
        this.dateSpinner = new JSpinner(new SpinnerDateModel());
        this.proposedTimeInSpinner = new JSpinner(new SpinnerDateModel());
        this.proposedTimeOutSpinner = new JSpinner(new SpinnerDateModel());
        this.reasonArea = new JTextArea(4, 20);
        this.remarksField = new JTextField();

        this.tableModel = new DefaultTableModel(
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

        this.requestTable = new JTable(tableModel);

        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        buildUI();
        populateEmployeeDropdown();
        loadRequestTable();
    }

    /**
     * Builds the full panel UI.
     */
    private void buildUI() {

        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setBackground(Theme.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel("Attendance Adjustments");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("HR submits requests, Admin approves/rejects, Supervisor views.");
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);

        topWrapper.add(titleLabel);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 6)));
        topWrapper.add(subtitleLabel);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 18)));

        if (canSubmitRequests()) {
            topWrapper.add(buildSubmitCard());
            topWrapper.add(Box.createRigidArea(new Dimension(0, 16)));
        }

        if (canApproveRequests()) {
            topWrapper.add(buildApproveCard());
            topWrapper.add(Box.createRigidArea(new Dimension(0, 16)));
        }

        add(topWrapper, BorderLayout.NORTH);

        requestTable.setRowHeight(24);
        requestTable.setFont(Theme.FONT_BODY);
        requestTable.getTableHeader().setFont(Theme.FONT_BUTTON);
        requestTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(requestTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Builds the HR submit request card.
     */
    private JPanel buildSubmitCard() {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        card.setMaximumSize(new Dimension(1000, 320));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel heading = new JLabel("Submit Adjustment Request");
        heading.setFont(Theme.FONT_HEADING);
        heading.setForeground(Theme.TEXT_PRIMARY);

        // Row 1
        JPanel row1 = new JPanel();
        row1.setBackground(Theme.CARD_BACKGROUND);
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));

        JLabel employeeLabel = new JLabel("Employee:");
        employeeLabel.setFont(Theme.FONT_BODY);

        employeeComboBox.setFont(Theme.FONT_BODY);
        employeeComboBox.setMaximumSize(new Dimension(360, 34));
        employeeComboBox.setPreferredSize(new Dimension(360, 34));

        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setFont(Theme.FONT_BODY);

        dateSpinner.setFont(Theme.FONT_BODY);
        dateSpinner.setMaximumSize(new Dimension(170, 34));
        dateSpinner.setPreferredSize(new Dimension(170, 34));

        // Date display format
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);

        row1.add(employeeLabel);
        row1.add(Box.createRigidArea(new Dimension(10, 0)));
        row1.add(employeeComboBox);
        row1.add(Box.createRigidArea(new Dimension(24, 0)));
        row1.add(dateLabel);
        row1.add(Box.createRigidArea(new Dimension(10, 0)));
        row1.add(dateSpinner);

        // Row 2
        JPanel row2 = new JPanel();
        row2.setBackground(Theme.CARD_BACKGROUND);
        row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));

        JLabel timeInLabel = new JLabel("Proposed Time In:");
        timeInLabel.setFont(Theme.FONT_BODY);

        proposedTimeInSpinner.setFont(Theme.FONT_BODY);
        proposedTimeInSpinner.setMaximumSize(new Dimension(140, 34));
        proposedTimeInSpinner.setPreferredSize(new Dimension(140, 34));

        // 12-hour format with AM/PM
        JSpinner.DateEditor timeInEditor = new JSpinner.DateEditor(proposedTimeInSpinner, "hh:mm a");
        proposedTimeInSpinner.setEditor(timeInEditor);

        JLabel timeOutLabel = new JLabel("Proposed Time Out:");
        timeOutLabel.setFont(Theme.FONT_BODY);

        proposedTimeOutSpinner.setFont(Theme.FONT_BODY);
        proposedTimeOutSpinner.setMaximumSize(new Dimension(140, 34));
        proposedTimeOutSpinner.setPreferredSize(new Dimension(140, 34));

        // 12-hour format with AM/PM
        JSpinner.DateEditor timeOutEditor = new JSpinner.DateEditor(proposedTimeOutSpinner, "hh:mm a");
        proposedTimeOutSpinner.setEditor(timeOutEditor);

        row2.add(timeInLabel);
        row2.add(Box.createRigidArea(new Dimension(10, 0)));
        row2.add(proposedTimeInSpinner);
        row2.add(Box.createRigidArea(new Dimension(24, 0)));
        row2.add(timeOutLabel);
        row2.add(Box.createRigidArea(new Dimension(10, 0)));
        row2.add(proposedTimeOutSpinner);

        JLabel reasonLabel = new JLabel("Reason:");
        reasonLabel.setFont(Theme.FONT_BODY);

        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setFont(Theme.FONT_BODY);

        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setPreferredSize(new Dimension(720, 100));
        reasonScroll.setMaximumSize(new Dimension(820, 100));
        reasonScroll.setAlignmentX(LEFT_ALIGNMENT);

        JButton submitButton = new JButton("Submit Request");
        submitButton.setFont(Theme.FONT_BUTTON);
        submitButton.setBackground(Theme.PRIMARY);
        submitButton.setForeground(java.awt.Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setAlignmentX(LEFT_ALIGNMENT);

        submitButton.addActionListener(e -> submitRequest());

        card.add(heading);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(row1);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(row2);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(reasonLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(reasonScroll);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(submitButton);

        return card;
    }

    /**
     * Builds the Admin approval/rejection card.
     */
    private JPanel buildApproveCard() {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        card.setMaximumSize(new Dimension(1000, 140));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel heading = new JLabel("Approve / Reject Request");
        heading.setFont(Theme.FONT_HEADING);
        heading.setForeground(Theme.TEXT_PRIMARY);

        JLabel remarksLabel = new JLabel("Remarks:");
        remarksLabel.setFont(Theme.FONT_BODY);

        remarksField.setFont(Theme.FONT_BODY);
        remarksField.setMaximumSize(new Dimension(520, 34));
        remarksField.setPreferredSize(new Dimension(520, 34));

        JPanel buttonRow = new JPanel();
        buttonRow.setBackground(Theme.CARD_BACKGROUND);
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));

        JButton approveButton = new JButton("Approve Selected");
        approveButton.setFont(Theme.FONT_BUTTON);
        approveButton.setBackground(Theme.PRIMARY);
        approveButton.setForeground(java.awt.Color.WHITE);
        approveButton.setFocusPainted(false);

        JButton rejectButton = new JButton("Reject Selected");
        rejectButton.setFont(Theme.FONT_BUTTON);
        rejectButton.setBackground(Theme.LOGOUT_BACKGROUND);
        rejectButton.setForeground(Theme.LOGOUT_TEXT);
        rejectButton.setFocusPainted(false);

        approveButton.addActionListener(e -> approveSelectedRequest());
        rejectButton.addActionListener(e -> rejectSelectedRequest());

        buttonRow.add(approveButton);
        buttonRow.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonRow.add(rejectButton);

        card.add(heading);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(remarksLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(remarksField);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(buttonRow);

        return card;
    }

    /**
     * Loads employees into the dropdown.
     */
    private void populateEmployeeDropdown() {

        employeeComboBox.removeAllItems();

        List<Employee> employees = new ArrayList<>(employeeRepository.findAll());
        employees.sort(Comparator.comparing(Employee::getFullName));

        for (Employee employee : employees) {
            employeeComboBox.addItem(new EmployeeItem(employee));
        }
    }

    /**
     * Loads requests into the table.
     */
    private void loadRequestTable() {

        tableModel.setRowCount(0);

        List<AttendanceAdjustmentRequest> requests = adjustmentService.findAll();

        for (AttendanceAdjustmentRequest request : requests) {
            tableModel.addRow(new Object[]{
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
     * HR submits a request.
     */
    private void submitRequest() {

        EmployeeItem selectedItem = (EmployeeItem) employeeComboBox.getSelectedItem();
        Employee currentUser = employeeRepository.findById(session.getEmployeeId());

        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an employee.");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

        String selectedDate = dateFormat.format((Date) dateSpinner.getValue());
        String proposedTimeIn = timeFormat.format((Date) proposedTimeInSpinner.getValue());
        String proposedTimeOut = timeFormat.format((Date) proposedTimeOutSpinner.getValue());

        boolean success = adjustmentService.submitRequest(
                selectedItem.getEmployee(),
                currentUser,
                selectedDate,
                proposedTimeIn,
                proposedTimeOut,
                reasonArea.getText().trim()
        );

        JOptionPane.showMessageDialog(this, adjustmentService.getLastMessage());

        if (success) {
            dateSpinner.setValue(new Date());
            proposedTimeInSpinner.setValue(new Date());
            proposedTimeOutSpinner.setValue(new Date());
            reasonArea.setText("");
            loadRequestTable();
        }
    }

    /**
     * Admin approves selected request.
     */
    private void approveSelectedRequest() {

        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a request first.");
            return;
        }

        String requestId = String.valueOf(tableModel.getValueAt(selectedRow, 0));
        Employee currentUser = employeeRepository.findById(session.getEmployeeId());

        boolean success = adjustmentService.approveRequest(
                requestId,
                currentUser,
                remarksField.getText().trim()
        );

        JOptionPane.showMessageDialog(this, adjustmentService.getLastMessage());

        if (success) {
            remarksField.setText("");
            loadRequestTable();
        }
    }

    /**
     * Admin rejects selected request.
     */
    private void rejectSelectedRequest() {

        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a request first.");
            return;
        }

        String requestId = String.valueOf(tableModel.getValueAt(selectedRow, 0));
        Employee currentUser = employeeRepository.findById(session.getEmployeeId());

        boolean success = adjustmentService.rejectRequest(
                requestId,
                currentUser,
                remarksField.getText().trim()
        );

        JOptionPane.showMessageDialog(this, adjustmentService.getLastMessage());

        if (success) {
            remarksField.setText("");
            loadRequestTable();
        }
    }

    /**
     * HR can submit.
     */
    private boolean canSubmitRequests() {
        String role = safeLower(session.getRole());
        return role.contains("hr");
    }

    /**
     * Admin can approve/reject.
     */
    private boolean canApproveRequests() {
        String role = safeLower(session.getRole());
        return role.contains("admin");
    }

    private String safeLower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Dropdown wrapper for employee selection.
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
}