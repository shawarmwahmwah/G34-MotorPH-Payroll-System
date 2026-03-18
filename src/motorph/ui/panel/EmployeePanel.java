package motorph.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import motorph.model.Employee;
import motorph.repository.CsvEmployeeRepository;
import motorph.repository.EmployeeRepository;
import motorph.ui.Theme;
import motorph.ui.session.UserSession;
import motorph.util.MoneyUtil;

public class EmployeePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final UserSession session;
    private final EmployeeRepository employeeRepository;
    private final DefaultTableModel employeeTableModel;
    private final JTable employeeTable;
    private final JTextField searchField;

    private List<Employee> employees;
    private List<Employee> displayedEmployees;

    public EmployeePanel(UserSession session) {
        this.session = session;
        this.employeeRepository = new CsvEmployeeRepository();
        this.employeeTableModel = new DefaultTableModel(
                new Object[] {"Employee ID", "Last Name", "First Name"},
                0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.employeeTable = new JTable(employeeTableModel);
        this.searchField = new JTextField();
        this.employees = new ArrayList<>();
        this.displayedEmployees = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        buildUi();
        loadEmployees();
    }

    private void buildUi() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Theme.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel("Manage Employees");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Read and maintain employees.csv records.");
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);

        wrapper.add(titleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 6)));
        wrapper.add(subtitleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));
        wrapper.add(buildActionCard());
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));
        wrapper.add(buildTableCard());

        add(wrapper, BorderLayout.CENTER);
    }

    private JPanel buildActionCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel summaryLabel = new JLabel("Admin/HR employee maintenance using the live CSV repository.");
        summaryLabel.setFont(Theme.FONT_BODY);
        summaryLabel.setForeground(Theme.TEXT_SECONDARY);

        JPanel searchRow = new JPanel();
        searchRow.setOpaque(false);
        searchRow.setLayout(new BoxLayout(searchRow, BoxLayout.X_AXIS));

        JLabel searchLabel = new JLabel("Search (ID / Name):");
        searchLabel.setFont(Theme.FONT_BODY);

        searchField.setFont(Theme.FONT_BODY);
        searchField.setMaximumSize(new Dimension(300, 34));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applySearchFilter();
            }
        });

        JButton clearSearchButton = new JButton("Clear");
        stylePrimaryButton(clearSearchButton);
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            applySearchFilter();
        });

        searchRow.add(searchLabel);
        searchRow.add(Box.createRigidArea(new Dimension(8, 0)));
        searchRow.add(searchField);
        searchRow.add(Box.createRigidArea(new Dimension(8, 0)));
        searchRow.add(clearSearchButton);
        searchRow.add(Box.createHorizontalGlue());

        JPanel buttonRow = new JPanel();
        buttonRow.setOpaque(false);
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));

        JButton viewButton = new JButton("View Profile");
        JButton editButton = new JButton("Edit Employee");
        JButton createButton = new JButton("Create Employee");
        JButton deleteButton = new JButton("Delete Employee");

        stylePrimaryButton(viewButton);
        stylePrimaryButton(editButton);
        stylePrimaryButton(createButton);
        styleDangerButton(deleteButton);

        viewButton.addActionListener(e -> viewSelectedEmployee());
        editButton.addActionListener(e -> editSelectedEmployee());
        createButton.addActionListener(e -> createEmployee());
        deleteButton.addActionListener(e -> deleteSelectedEmployee());

        buttonRow.add(viewButton);
        buttonRow.add(Box.createRigidArea(new Dimension(8, 0)));
        buttonRow.add(editButton);
        buttonRow.add(Box.createRigidArea(new Dimension(8, 0)));
        buttonRow.add(createButton);
        buttonRow.add(Box.createRigidArea(new Dimension(8, 0)));
        buttonRow.add(deleteButton);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(summaryLabel);
        content.add(Box.createRigidArea(new Dimension(0, 8)));
        content.add(searchRow);

        card.add(content, BorderLayout.CENTER);
        card.add(buttonRow, BorderLayout.EAST);
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

        employeeTable.setFont(Theme.FONT_BODY);
        employeeTable.setRowHeight(28);
        employeeTable.getTableHeader().setFont(Theme.FONT_BUTTON);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setPreferredSize(new Dimension(1020, 520));
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private void loadEmployees() {
        System.out.println("[EmployeePanel] actor employeeId=" + session.getEmployeeId() + " role=" + normalizeRole(session.getRole()));
        employees = new ArrayList<>(employeeRepository.findAll());
        employees.sort(Comparator.comparing(Employee::getEmployeeId));
        applySearchFilter();

        System.out.println("[EmployeePanel] Loaded employees=" + employees.size());
        for (Employee employee : employees) {
            System.out.println("[EmployeePanel] row=" + employee);
        }
    }

    private void applySearchFilter() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ENGLISH);

        System.out.println("[EmployeePanel] filter actor employeeId=" + session.getEmployeeId() + " query='" + query + "'");

        displayedEmployees = new ArrayList<>();
        for (Employee employee : employees) {
            if (query.isEmpty() || matchesSearch(employee, query)) {
                displayedEmployees.add(employee);
            }
        }

        employeeTableModel.setRowCount(0);
        for (Employee employee : displayedEmployees) {
            employeeTableModel.addRow(new Object[] {
                    employee.getEmployeeId(),
                    employee.getLastName(),
                    employee.getFirstName()
            });
        }

        System.out.println("[EmployeePanel] Search query='" + query + "' visibleRows=" + displayedEmployees.size());
    }

    private boolean matchesSearch(Employee employee, String query) {
        String employeeId = safeLower(employee.getEmployeeId());
        String lastName = safeLower(employee.getLastName());
        String firstName = safeLower(employee.getFirstName());
        String fullName1 = safeLower(employee.getFirstName() + " " + employee.getLastName());
        String fullName2 = safeLower(employee.getLastName() + ", " + employee.getFirstName());

        return employeeId.contains(query)
                || lastName.contains(query)
                || firstName.contains(query)
                || fullName1.contains(query)
                || fullName2.contains(query);
    }

    private void viewSelectedEmployee() {
        Employee employee = getSelectedEmployee();
        if (employee == null) {
            JOptionPane.showMessageDialog(this, "Select an employee first.");
            return;
        }

        JTextArea textArea = new JTextArea(buildEmployeeProfile(employee));
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(620, 420));

        JOptionPane.showMessageDialog(this, scrollPane, "Employee Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    private void createEmployee() {
        List<JTextField> fields = new ArrayList<>();
        String[] labels = {
                "Employee ID", "Last Name", "First Name", "Birthday", "Address", "Phone Number",
                "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
                "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
                "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate", "Password", "Role"
        };

        JPanel form = new JPanel(new GridLayout(labels.length, 2, 8, 8));
        for (String labelText : labels) {
            JLabel label = new JLabel(labelText + ":");
            label.setFont(Theme.FONT_BODY);
            JTextField field = new JTextField();
            field.setFont(Theme.FONT_BODY);

            if ("Password".equals(labelText)) {
                field.setText("admin");
            } else if ("Role".equals(labelText)) {
                field.setText("employee");
            }

            form.add(label);
            form.add(field);
            fields.add(field);
        }

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setPreferredSize(new Dimension(650, 500));

        int option = JOptionPane.showConfirmDialog(
                this,
                scrollPane,
                "Create New Employee",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String employeeId = fields.get(0).getText().trim();
        if (employeeId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Employee ID is required.");
            return;
        }
        if (employeeRepository.findById(employeeId) != null) {
            JOptionPane.showMessageDialog(this, "Employee ID already exists.");
            return;
        }

        Employee newEmployee = new Employee(
                employeeId,
                fields.get(1).getText().trim(),
                fields.get(2).getText().trim(),
                fields.get(3).getText().trim(),
                fields.get(4).getText().trim(),
                fields.get(5).getText().trim(),
                fields.get(6).getText().trim(),
                fields.get(7).getText().trim(),
                fields.get(8).getText().trim(),
                fields.get(9).getText().trim(),
                fields.get(10).getText().trim(),
                fields.get(11).getText().trim(),
                fields.get(12).getText().trim(),
                MoneyUtil.parseMoney2dp(fields.get(13).getText().trim()),
                MoneyUtil.parseMoney2dp(fields.get(14).getText().trim()),
                MoneyUtil.parseMoney2dp(fields.get(15).getText().trim()),
                MoneyUtil.parseMoney2dp(fields.get(16).getText().trim()),
                MoneyUtil.parseMoney2dp(fields.get(17).getText().trim()),
                MoneyUtil.parseMoney2dp(fields.get(18).getText().trim()),
                employeeId,
                fields.get(19).getText().trim().isEmpty() ? "admin" : fields.get(19).getText().trim(),
                fields.get(20).getText().trim().isEmpty() ? "employee" : fields.get(20).getText().trim()
        );

        List<Employee> updatedEmployees = new ArrayList<>(employees);
        updatedEmployees.add(newEmployee);
        updatedEmployees.sort(Comparator.comparing(Employee::getEmployeeId));
        employeeRepository.saveAll(updatedEmployees);

        System.out.println("[EmployeePanel] Created employee=" + newEmployee);
        JOptionPane.showMessageDialog(this, "Employee created successfully.");
        loadEmployees();
    }

    private void editSelectedEmployee() {
        Employee employee = getSelectedEmployee();
        if (employee == null) {
            JOptionPane.showMessageDialog(this, "Select an employee first.");
            return;
        }

        List<JTextField> fields = new ArrayList<>();
        String[] labels = {
                "Employee ID", "Last Name", "First Name", "Birthday", "Address", "Phone Number",
                "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
                "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
                "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate", "Password", "Role"
        };

        String[] values = {
                employee.getEmployeeId(), employee.getLastName(), employee.getFirstName(), employee.getBirthday(), employee.getAddress(),
                employee.getPhoneNumber(), employee.getSssNumber(), employee.getPhilhealthNumber(), employee.getTinNumber(), employee.getPagibigNumber(),
                employee.getStatus(), employee.getPosition(), employee.getImmediateSupervisor(),
                String.valueOf(employee.getBasicSalary()), String.valueOf(employee.getRiceSubsidy()), String.valueOf(employee.getPhoneAllowance()),
                String.valueOf(employee.getClothingAllowance()), String.valueOf(employee.getGrossSemiMonthlyRate()), String.valueOf(employee.getHourlyRate()),
                employee.getPassword(), employee.getRole()
        };

        JPanel form = new JPanel(new GridLayout(labels.length, 2, 8, 8));
        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i] + ":");
            label.setFont(Theme.FONT_BODY);
            JTextField field = new JTextField(values[i]);
            field.setFont(Theme.FONT_BODY);

            if ("Role".equals(labels[i]) && !canManageRoles()) {
                field.setEditable(false);
            }

            form.add(label);
            form.add(field);
            fields.add(field);
        }

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setPreferredSize(new Dimension(650, 500));

        int option = JOptionPane.showConfirmDialog(
                this,
                scrollPane,
                "Edit Employee",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String newEmployeeId = fields.get(0).getText().trim();
        if (newEmployeeId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Employee ID is required.");
            return;
        }

        if (!newEmployeeId.equals(employee.getEmployeeId()) && employeeRepository.findById(newEmployeeId) != null) {
            JOptionPane.showMessageDialog(this, "Duplicate Employee ID is not allowed.");
            return;
        }

        String editedRole = canManageRoles()
                ? normalizeRole(fields.get(20).getText().trim())
                : employee.getRole();

        Employee updatedEmployee = new Employee(
                newEmployeeId,
                fields.get(1).getText().trim(),
                fields.get(2).getText().trim(),
                fields.get(3).getText().trim(),
                fields.get(4).getText().trim(),
                fields.get(5).getText().trim(),
                fields.get(6).getText().trim(),
                fields.get(7).getText().trim(),
                fields.get(8).getText().trim(),
                fields.get(9).getText().trim(),
                fields.get(10).getText().trim(),
                fields.get(11).getText().trim(),
                fields.get(12).getText().trim(),
                MoneyUtil.parseMoney2dp(fields.get(13).getText().trim()),
                MoneyUtil.parseMoney2dp(fields.get(14).getText().trim()),
                MoneyUtil.parseMoney2dp(fields.get(15).getText().trim()),
                MoneyUtil.parseMoney2dp(fields.get(16).getText().trim()),
                MoneyUtil.parseMoney2dp(fields.get(17).getText().trim()),
                MoneyUtil.parseMoney2dp(fields.get(18).getText().trim()),
                newEmployeeId,
                fields.get(19).getText().trim().isEmpty() ? "admin" : fields.get(19).getText().trim(),
                editedRole
        );

        List<Employee> updatedEmployees = new ArrayList<>();
        for (Employee current : employees) {
            if (current.getEmployeeId().equals(employee.getEmployeeId())) {
                updatedEmployees.add(updatedEmployee);
            } else {
                updatedEmployees.add(current);
            }
        }

        updatedEmployees.sort(Comparator.comparing(Employee::getEmployeeId));
        employeeRepository.saveAll(updatedEmployees);

        System.out.println("[EmployeePanel] Updated employee=" + updatedEmployee);
        JOptionPane.showMessageDialog(this, "Employee updated successfully.");
        loadEmployees();
    }

    private void deleteSelectedEmployee() {
        Employee employee = getSelectedEmployee();
        if (employee == null) {
            JOptionPane.showMessageDialog(this, "Select an employee first.");
            return;
        }

        if (session.getEmployeeId().equals(employee.getEmployeeId())) {
            JOptionPane.showMessageDialog(this, "You cannot delete the currently logged-in employee.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                "Delete employee " + employee.getEmployeeId() + " - " + employee.getFullName() + "?",
                "Delete Employee",
                JOptionPane.YES_NO_OPTION
        );

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        List<Employee> updatedEmployees = new ArrayList<>();
        for (Employee current : employees) {
            if (!current.getEmployeeId().equals(employee.getEmployeeId())) {
                updatedEmployees.add(current);
            }
        }

        employeeRepository.saveAll(updatedEmployees);
        System.out.println("[EmployeePanel] Deleted employee=" + employee);
        JOptionPane.showMessageDialog(this, "Employee deleted successfully.");
        loadEmployees();
    }

    private Employee getSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= displayedEmployees.size()) {
            return null;
        }
        return displayedEmployees.get(selectedRow);
    }

    private String buildEmployeeProfile(Employee employee) {
        return "Employee ID: " + employee.getEmployeeId() + "\n"
                + "Last Name: " + employee.getLastName() + "\n"
                + "First Name: " + employee.getFirstName() + "\n"
                + "Birthday: " + employee.getBirthday() + "\n"
                + "Address: " + employee.getAddress() + "\n"
                + "Phone Number: " + employee.getPhoneNumber() + "\n"
                + "SSS #: " + employee.getSssNumber() + "\n"
                + "Philhealth #: " + employee.getPhilhealthNumber() + "\n"
                + "TIN #: " + employee.getTinNumber() + "\n"
                + "Pag-ibig #: " + employee.getPagibigNumber() + "\n"
                + "Status: " + employee.getStatus() + "\n"
                + "Position: " + employee.getPosition() + "\n"
                + "Immediate Supervisor: " + employee.getImmediateSupervisor() + "\n"
                + "Basic Salary: " + formatMoney(employee.getBasicSalary()) + "\n"
                + "Rice Subsidy: " + formatMoney(employee.getRiceSubsidy()) + "\n"
                + "Phone Allowance: " + formatMoney(employee.getPhoneAllowance()) + "\n"
                + "Clothing Allowance: " + formatMoney(employee.getClothingAllowance()) + "\n"
                + "Gross Semi-monthly Rate: " + formatMoney(employee.getGrossSemiMonthlyRate()) + "\n"
                + "Hourly Rate: " + formatMoney(employee.getHourlyRate()) + "\n"
                + "Username: " + employee.getUsername() + "\n"
                + "Password: " + employee.getPassword() + "\n"
                + "Role: " + employee.getRole();
    }

    private String formatMoney(double value) {
        return String.format(Locale.ENGLISH, "%,.2f", value);
    }

    private boolean canManageRoles() {
        String role = normalizeRole(session.getRole());
        return "admin".equals(role) || "hr".equals(role);
    }

    private String normalizeRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return "employee";
        }
        return role.trim().toLowerCase(Locale.ENGLISH);
    }

    private String safeLower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ENGLISH);
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(Theme.FONT_BUTTON);
        button.setBackground(Theme.PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }

    private void styleDangerButton(JButton button) {
        button.setFont(Theme.FONT_BUTTON);
        button.setBackground(new Color(192, 72, 72));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }
}
