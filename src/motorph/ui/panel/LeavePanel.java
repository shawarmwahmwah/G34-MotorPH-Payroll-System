package motorph.ui.panel;

import motorph.model.Employee;
import motorph.model.LeaveBalance;
import motorph.model.LeaveRequest;
import motorph.repository.CsvEmployeeRepository;
import motorph.repository.EmployeeRepository;
import motorph.service.LeaveService;
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
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class LeavePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final UserSession session;
    private final LeaveService leaveService;
    private final EmployeeRepository employeeRepository;

    private final JComboBox<EmployeeItem> employeeComboBox;
    private final JComboBox<String> leaveTypeComboBox;
    private final JTextField startDateField;
    private final JTextField endDateField;
    private final JTextField daysRequestedField;
    private final JTextArea reasonArea;

    private final JTextField remarksField;

    private final JLabel sickLeaveValueLabel;
    private final JLabel vacationLeaveValueLabel;
    private final JLabel maternityLeaveValueLabel;
    private final JLabel paternityLeaveValueLabel;
    private final JLabel bereavementLeaveValueLabel;

    private final DefaultTableModel leaveTableModel;

    private final JPanel approvalContainer;
    private final CardLayout approvalCardLayout;
    private static final String APPROVAL_HIDDEN = "HIDDEN";
    private static final String APPROVAL_VISIBLE = "VISIBLE";
    private boolean approvalVisible = false;

    private final List<Employee> allEmployees;

    public LeavePanel(UserSession session) {
        this.session = session;
        this.leaveService = new LeaveService();
        this.employeeRepository = new CsvEmployeeRepository();

        this.employeeComboBox = new JComboBox<>();
        this.leaveTypeComboBox = new JComboBox<>(new String[]{
                "Sick Leave",
                "Vacation Leave",
                "Maternity Leave",
                "Paternity Leave",
                "Bereavement Leave",
                "Emergency Leave"
        });
        this.startDateField = new JTextField();
        this.endDateField = new JTextField();
        this.daysRequestedField = new JTextField();
        this.reasonArea = new JTextArea(4, 20);

        this.remarksField = new JTextField();

        this.sickLeaveValueLabel = new JLabel("0.00");
        this.vacationLeaveValueLabel = new JLabel("0.00");
        this.maternityLeaveValueLabel = new JLabel("0.00");
        this.paternityLeaveValueLabel = new JLabel("0.00");
        this.bereavementLeaveValueLabel = new JLabel("0.00");

        this.leaveTableModel = new DefaultTableModel(
                new Object[]{
                        "Request ID",
                        "Employee ID",
                        "Employee Name",
                        "Leave Type",
                        "Start Date",
                        "End Date",
                        "Days Requested",
                        "Status",
                        "Reason",
                        "Approver ID",
                        "Remarks"
                },
                0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.approvalCardLayout = new CardLayout();
        this.approvalContainer = new JPanel(approvalCardLayout);

        this.allEmployees = new ArrayList<>(employeeRepository.findAll());

        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        buildUI();
        loadEmployeeDropdown();
        loadLeaveBalance();
        loadLeaveHistoryTable();
    }

    private void buildUI() {

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Theme.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel("Leave");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Submit leave requests, view balances, and manage approvals.");
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);

        wrapper.add(titleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 6)));
        wrapper.add(subtitleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 18)));

        JPanel topActionCard = new JPanel(new BorderLayout());
        topActionCard.setBackground(Theme.CARD_BACKGROUND);
        topActionCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        topActionCard.setAlignmentX(LEFT_ALIGNMENT);
        topActionCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel noteLabel = new JLabel(canApproveLeaveRequests()
                ? "You can submit leave requests and open the embedded leave approval section."
                : "You can submit leave requests and view your leave history.");
        noteLabel.setFont(Theme.FONT_BODY);
        noteLabel.setForeground(Theme.TEXT_SECONDARY);

        topActionCard.add(noteLabel, BorderLayout.WEST);

        if (canApproveLeaveRequests()) {
            JButton approvalToggleButton = new JButton("Leave Approval");
            approvalToggleButton.setFont(Theme.FONT_BUTTON);
            approvalToggleButton.setBackground(Theme.PRIMARY);
            approvalToggleButton.setForeground(Color.WHITE);
            approvalToggleButton.setFocusPainted(false);
            approvalToggleButton.setPreferredSize(new Dimension(160, 40));
            approvalToggleButton.addActionListener(e -> toggleApprovalSection());

            topActionCard.add(approvalToggleButton, BorderLayout.EAST);
        }

        wrapper.add(topActionCard);
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel balanceCard = new JPanel(new GridLayout(1, 5, 14, 0));
        balanceCard.setBackground(Theme.CARD_BACKGROUND);
        balanceCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        balanceCard.setAlignmentX(LEFT_ALIGNMENT);
        balanceCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        balanceCard.add(createBalanceItem("Sick Leave", sickLeaveValueLabel));
        balanceCard.add(createBalanceItem("Vacation Leave", vacationLeaveValueLabel));
        balanceCard.add(createBalanceItem("Maternity Leave", maternityLeaveValueLabel));
        balanceCard.add(createBalanceItem("Paternity Leave", paternityLeaveValueLabel));
        balanceCard.add(createBalanceItem("Bereavement Leave", bereavementLeaveValueLabel));

        wrapper.add(balanceCard);
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel submitCard = new JPanel();
        submitCard.setLayout(new BoxLayout(submitCard, BoxLayout.Y_AXIS));
        submitCard.setBackground(Theme.CARD_BACKGROUND);
        submitCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        submitCard.setAlignmentX(LEFT_ALIGNMENT);
        submitCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        JLabel submitHeading = new JLabel("Submit Leave Request");
        submitHeading.setFont(Theme.FONT_HEADING);
        submitHeading.setForeground(Theme.TEXT_PRIMARY);
        submitHeading.setAlignmentX(LEFT_ALIGNMENT);

        JPanel row1 = new JPanel(new GridBagLayout());
        row1.setBackground(Theme.CARD_BACKGROUND);
        row1.setAlignmentX(LEFT_ALIGNMENT);

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.insets = new java.awt.Insets(4, 4, 4, 12);
        gbc1.anchor = GridBagConstraints.WEST;
        gbc1.fill = GridBagConstraints.HORIZONTAL;

        JLabel employeeLabel = new JLabel("Employee:");
        employeeLabel.setFont(Theme.FONT_BODY);

        employeeComboBox.setFont(Theme.FONT_BODY);
        employeeComboBox.setPreferredSize(new Dimension(320, 34));

        JLabel leaveTypeLabel = new JLabel("Leave Type:");
        leaveTypeLabel.setFont(Theme.FONT_BODY);

        leaveTypeComboBox.setFont(Theme.FONT_BODY);
        leaveTypeComboBox.setPreferredSize(new Dimension(220, 34));

        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 0;
        row1.add(employeeLabel, gbc1);

        gbc1.gridx = 1;
        gbc1.weightx = 1.0;
        row1.add(employeeComboBox, gbc1);

        gbc1.gridx = 2;
        gbc1.weightx = 0;
        row1.add(leaveTypeLabel, gbc1);

        gbc1.gridx = 3;
        gbc1.weightx = 0.5;
        row1.add(leaveTypeComboBox, gbc1);

        JPanel row2 = new JPanel(new GridBagLayout());
        row2.setBackground(Theme.CARD_BACKGROUND);
        row2.setAlignmentX(LEFT_ALIGNMENT);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new java.awt.Insets(4, 4, 4, 12);
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        JLabel startDateLabel = new JLabel("Start Date:");
        startDateLabel.setFont(Theme.FONT_BODY);

        startDateField.setFont(Theme.FONT_BODY);
        startDateField.setPreferredSize(new Dimension(150, 34));
        startDateField.setText("MM/DD/YYYY");

        JLabel endDateLabel = new JLabel("End Date:");
        endDateLabel.setFont(Theme.FONT_BODY);

        endDateField.setFont(Theme.FONT_BODY);
        endDateField.setPreferredSize(new Dimension(150, 34));
        endDateField.setText("MM/DD/YYYY");

        JLabel daysLabel = new JLabel("Days Requested:");
        daysLabel.setFont(Theme.FONT_BODY);

        daysRequestedField.setFont(Theme.FONT_BODY);
        daysRequestedField.setPreferredSize(new Dimension(120, 34));

        gbc2.gridx = 0;
        gbc2.gridy = 0;
        gbc2.weightx = 0;
        row2.add(startDateLabel, gbc2);

        gbc2.gridx = 1;
        gbc2.weightx = 0.35;
        row2.add(startDateField, gbc2);

        gbc2.gridx = 2;
        gbc2.weightx = 0;
        row2.add(endDateLabel, gbc2);

        gbc2.gridx = 3;
        gbc2.weightx = 0.35;
        row2.add(endDateField, gbc2);

        gbc2.gridx = 4;
        gbc2.weightx = 0;
        row2.add(daysLabel, gbc2);

        gbc2.gridx = 5;
        gbc2.weightx = 0.2;
        row2.add(daysRequestedField, gbc2);

        JLabel reasonLabel = new JLabel("Reason:");
        reasonLabel.setFont(Theme.FONT_BODY);
        reasonLabel.setAlignmentX(LEFT_ALIGNMENT);

        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setFont(Theme.FONT_BODY);
        reasonArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setPreferredSize(new Dimension(900, 100));
        reasonScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        reasonScroll.setAlignmentX(LEFT_ALIGNMENT);

        JButton submitButton = new JButton("Submit Leave Request");
        submitButton.setFont(Theme.FONT_BUTTON);
        submitButton.setBackground(Theme.PRIMARY);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setPreferredSize(new Dimension(190, 40));
        submitButton.setMaximumSize(new Dimension(190, 40));
        submitButton.setAlignmentX(LEFT_ALIGNMENT);

        submitButton.addActionListener(e -> submitLeaveRequest());

        submitCard.add(submitHeading);
        submitCard.add(Box.createRigidArea(new Dimension(0, 12)));
        submitCard.add(row1);
        submitCard.add(Box.createRigidArea(new Dimension(0, 12)));
        submitCard.add(row2);
        submitCard.add(Box.createRigidArea(new Dimension(0, 12)));
        submitCard.add(reasonLabel);
        submitCard.add(Box.createRigidArea(new Dimension(0, 6)));
        submitCard.add(reasonScroll);
        submitCard.add(Box.createRigidArea(new Dimension(0, 12)));
        submitCard.add(submitButton);

        wrapper.add(submitCard);
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));

        approvalContainer.setBackground(Theme.CONTENT_BACKGROUND);
        approvalContainer.setAlignmentX(LEFT_ALIGNMENT);

        JPanel hiddenPanel = new JPanel();
        hiddenPanel.setBackground(Theme.CONTENT_BACKGROUND);

        JPanel approvalPanel = buildApprovalSection();

        approvalContainer.add(hiddenPanel, APPROVAL_HIDDEN);
        approvalContainer.add(approvalPanel, APPROVAL_VISIBLE);
        approvalCardLayout.show(approvalContainer, APPROVAL_HIDDEN);

        wrapper.add(approvalContainer);
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel historyCard = new JPanel(new BorderLayout());
        historyCard.setBackground(Theme.CARD_BACKGROUND);
        historyCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        historyCard.setAlignmentX(LEFT_ALIGNMENT);

        JTable historyTable = new JTable(leaveTableModel);
        historyTable.setRowHeight(28);
        historyTable.setFont(Theme.FONT_BODY);
        historyTable.getTableHeader().setFont(Theme.FONT_BUTTON);
        historyTable.setFillsViewportHeight(true);
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane historyScrollPane = new JScrollPane(historyTable);
        historyScrollPane.setPreferredSize(new Dimension(1000, 320));

        historyCard.add(historyScrollPane, BorderLayout.CENTER);

        wrapper.add(historyCard);

        JScrollPane pageScrollPane = new JScrollPane(wrapper);
        pageScrollPane.setBorder(null);
        pageScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(pageScrollPane, BorderLayout.CENTER);
    }

    private JPanel buildApprovalSection() {
        JPanel sectionWrapper = new JPanel();
        sectionWrapper.setLayout(new BoxLayout(sectionWrapper, BoxLayout.Y_AXIS));
        sectionWrapper.setBackground(Theme.CONTENT_BACKGROUND);
        sectionWrapper.setAlignmentX(LEFT_ALIGNMENT);

        JPanel approvalCard = new JPanel();
        approvalCard.setLayout(new BoxLayout(approvalCard, BoxLayout.Y_AXIS));
        approvalCard.setBackground(Theme.CARD_BACKGROUND);
        approvalCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        approvalCard.setAlignmentX(LEFT_ALIGNMENT);
        approvalCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel heading = new JLabel("Leave Approval");
        heading.setFont(Theme.FONT_HEADING);
        heading.setForeground(Theme.TEXT_PRIMARY);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JPanel actionRow = new JPanel(new BorderLayout());
        actionRow.setBackground(Theme.CARD_BACKGROUND);

        JPanel rightButtons = new JPanel();
        rightButtons.setBackground(Theme.CARD_BACKGROUND);
        rightButtons.setLayout(new BoxLayout(rightButtons, BoxLayout.X_AXIS));

        JLabel remarksLabel = new JLabel("Remarks:");
        remarksLabel.setFont(Theme.FONT_BODY);

        remarksField.setFont(Theme.FONT_BODY);
        remarksField.setPreferredSize(new Dimension(250, 34));
        remarksField.setMaximumSize(new Dimension(250, 34));

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

        rightButtons.add(remarksLabel);
        rightButtons.add(Box.createRigidArea(new Dimension(8, 0)));
        rightButtons.add(remarksField);
        rightButtons.add(Box.createRigidArea(new Dimension(10, 0)));
        rightButtons.add(approveButton);
        rightButtons.add(Box.createRigidArea(new Dimension(8, 0)));
        rightButtons.add(rejectButton);

        actionRow.add(rightButtons, BorderLayout.EAST);

        approvalCard.add(heading);
        approvalCard.add(Box.createRigidArea(new Dimension(0, 12)));
        approvalCard.add(actionRow);

        sectionWrapper.add(approvalCard);

        approveButton.addActionListener(e -> approveSelectedLeaveRequest());
        rejectButton.addActionListener(e -> rejectSelectedLeaveRequest());

        return sectionWrapper;
    }

    private JPanel createBalanceItem(String labelText, JLabel valueLabel) {
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

    private void loadEmployeeDropdown() {
        employeeComboBox.removeAllItems();

        List<Employee> employeesToShow = new ArrayList<>();

        if (canViewOtherEmployees()) {
            employeesToShow.addAll(allEmployees);
        } else {
            Employee self = employeeRepository.findById(session.getEmployeeId());
            if (self != null) {
                employeesToShow.add(self);
            }
        }

        employeesToShow.sort(Comparator.comparing(Employee::getFullName));

        for (Employee employee : employeesToShow) {
            employeeComboBox.addItem(new EmployeeItem(employee));
        }

        if (employeeComboBox.getItemCount() > 0) {
            employeeComboBox.setSelectedIndex(0);
        }
    }

    private void loadLeaveBalance() {
        LeaveBalance balance = leaveService.viewLeaveBalance(session.getEmployeeId());

        if (balance == null) {
            sickLeaveValueLabel.setText("0.00");
            vacationLeaveValueLabel.setText("0.00");
            maternityLeaveValueLabel.setText("0.00");
            paternityLeaveValueLabel.setText("0.00");
            bereavementLeaveValueLabel.setText("0.00");
            return;
        }

        sickLeaveValueLabel.setText(String.format("%.2f", balance.getSickLeave()));
        vacationLeaveValueLabel.setText(String.format("%.2f", balance.getVacationLeave()));
        maternityLeaveValueLabel.setText(String.format("%.2f", balance.getMaternityLeave()));
        paternityLeaveValueLabel.setText(String.format("%.2f", balance.getPaternityLeave()));
        bereavementLeaveValueLabel.setText(String.format("%.2f", balance.getBereavementLeave()));
    }

    private void loadLeaveHistoryTable() {
        leaveTableModel.setRowCount(0);

        List<LeaveRequest> requests = leaveService.viewRequestHistory();

        for (LeaveRequest request : requests) {
            if (!canApproveLeaveRequests() && !request.getEmployeeId().equals(session.getEmployeeId())) {
                continue;
            }

            leaveTableModel.addRow(new Object[]{
                    request.getRequestId(),
                    request.getEmployeeId(),
                    request.getEmployeeName(),
                    request.getLeaveType(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getDaysRequested(),
                    request.getStatus(),
                    request.getReason(),
                    request.getApproverId(),
                    request.getRemarks()
            });
        }
    }

    private void submitLeaveRequest() {
        EmployeeItem selectedItem = (EmployeeItem) employeeComboBox.getSelectedItem();

        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an employee.");
            return;
        }

        double daysRequested;
        try {
            daysRequested = Double.parseDouble(daysRequestedField.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Days Requested must be a valid number.");
            return;
        }

        String result = leaveService.submitLeaveRequest(
                selectedItem.getEmployee(),
                String.valueOf(leaveTypeComboBox.getSelectedItem()),
                startDateField.getText().trim(),
                endDateField.getText().trim(),
                daysRequested,
                reasonArea.getText().trim()
        );

        JOptionPane.showMessageDialog(this, result);
        loadLeaveHistoryTable();
        loadLeaveBalance();

        if (!result.toLowerCase(Locale.ENGLISH).startsWith("error")) {
            startDateField.setText("MM/DD/YYYY");
            endDateField.setText("MM/DD/YYYY");
            daysRequestedField.setText("");
            reasonArea.setText("");
        }
    }

    private void approveSelectedLeaveRequest() {
        int selectedRow = getSelectedHistoryRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a leave request from the table first.");
            return;
        }

        String requestId = String.valueOf(leaveTableModel.getValueAt(selectedRow, 0));
        String result = leaveService.approveLeave(requestId, session.getEmployeeId());

        JOptionPane.showMessageDialog(this, result);
        remarksField.setText("");
        loadLeaveHistoryTable();
        loadLeaveBalance();
    }

    private void rejectSelectedLeaveRequest() {
        int selectedRow = getSelectedHistoryRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a leave request from the table first.");
            return;
        }

        String requestId = String.valueOf(leaveTableModel.getValueAt(selectedRow, 0));
        String result = leaveService.rejectLeave(
                requestId,
                session.getEmployeeId(),
                remarksField.getText().trim()
        );

        JOptionPane.showMessageDialog(this, result);
        remarksField.setText("");
        loadLeaveHistoryTable();
        loadLeaveBalance();
    }

    private int getSelectedHistoryRow() {
        // Table selection is not directly stored, so scan through the visible tables in this panel
        for (java.awt.Component component : getComponents()) {
            if (component instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) component;
                java.awt.Component view = scrollPane.getViewport().getView();
                if (view instanceof JPanel) {
                    // ignore page wrapper
                }
            }
        }

        // We use the currently visible request table row selection via helper search
        JTable table = findLeaveHistoryTable(this);
        if (table == null) {
            return -1;
        }
        return table.getSelectedRow();
    }

    private JTable findLeaveHistoryTable(java.awt.Container container) {
        for (java.awt.Component component : container.getComponents()) {
            if (component instanceof JTable) {
                JTable table = (JTable) component;
                if (table.getModel() == leaveTableModel) {
                    return table;
                }
            } else if (component instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) component;
                java.awt.Component view = scrollPane.getViewport().getView();
                if (view instanceof JTable) {
                    JTable table = (JTable) view;
                    if (table.getModel() == leaveTableModel) {
                        return table;
                    }
                }
            } else if (component instanceof java.awt.Container) {
                JTable found = findLeaveHistoryTable((java.awt.Container) component);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void toggleApprovalSection() {
        approvalVisible = !approvalVisible;

        if (approvalVisible) {
            approvalCardLayout.show(approvalContainer, APPROVAL_VISIBLE);
        } else {
            approvalCardLayout.show(approvalContainer, APPROVAL_HIDDEN);
        }

        revalidate();
        repaint();
    }

    private boolean canViewOtherEmployees() {
        String role = safeLower(session.getRole());
        return role.contains("hr") || role.contains("admin") || role.contains("supervisor");
    }

    private boolean canApproveLeaveRequests() {
        String role = safeLower(session.getRole());
        return role.contains("admin") || role.contains("hr") || role.contains("supervisor");
    }

    private String safeLower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ENGLISH);
    }

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
}