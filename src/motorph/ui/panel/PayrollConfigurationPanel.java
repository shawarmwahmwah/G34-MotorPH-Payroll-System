package motorph.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

import motorph.ui.Theme;
import motorph.ui.session.UserSession;
import motorph.util.CsvUtil;
import motorph.util.PathHelper;

public class PayrollConfigurationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final UserSession session;
    private final JComboBox<String> fileSelector;
    private final DefaultTableModel tableModel;
    private final JTable configTable;
    private final Map<String, String> selectorToFile;

    private List<String> currentHeaders;

    public PayrollConfigurationPanel(UserSession session) {
        this.session = session;
        this.fileSelector = new JComboBox<>();
        this.tableModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        this.configTable = new JTable(tableModel);
        this.selectorToFile = new LinkedHashMap<>();
        this.currentHeaders = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        initializeConfigSelector();
        ensurePayrollSettingsFileExists();
        buildUI();
        loadSelectedConfig();
    }

    private void initializeConfigSelector() {
        selectorToFile.put("SSS Contribution Rates", "sss_contribution.csv");
        selectorToFile.put("PhilHealth Contribution Rates", "philhealth_contribution.csv");
        selectorToFile.put("Pag-IBIG Contribution Rates", "pagibig_contribution.csv");
        selectorToFile.put("Withholding Tax Brackets", "withholding_tax.csv");
        selectorToFile.put("Payroll Settings", "payroll_config.csv");

        for (String key : selectorToFile.keySet()) {
            fileSelector.addItem(key);
        }

        fileSelector.setSelectedIndex(0);
    }

    private void ensurePayrollSettingsFileExists() {
        Path file = PathHelper.getDataFile("payroll_config.csv");

        try {
            if (!Files.exists(file)) {
                try (FileWriter fw = new FileWriter(file.toFile(), false)) {
                    fw.write("setting,value\n");
                    fw.write("overtimeMultiplier,1.25\n");
                    fw.flush();
                }
            }
        } catch (Exception e) {
            System.out.println("[PayrollConfigurationPanel] Unable to initialize payroll_config.csv: " + e.getMessage());
        }
    }

    private void buildUI() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Theme.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel("Payroll Configuration");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Maintain contribution tables, tax brackets, allowances and overtime multiplier from CSV.");
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);

        wrapper.add(titleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 6)));
        wrapper.add(subtitleLabel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterCard.setBackground(Theme.CARD_BACKGROUND);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        filterCard.setAlignmentX(LEFT_ALIGNMENT);
        filterCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel selectorLabel = new JLabel("Configuration File:");
        selectorLabel.setFont(Theme.FONT_BODY);

        fileSelector.setFont(Theme.FONT_BODY);
        fileSelector.setPreferredSize(new Dimension(280, 34));
        fileSelector.addActionListener(e -> loadSelectedConfig());

        JButton reloadButton = new JButton("Reload");
        styleSecondaryButton(reloadButton);
        reloadButton.addActionListener(e -> loadSelectedConfig());

        JButton addRowButton = new JButton("Add Row");
        styleSecondaryButton(addRowButton);
        addRowButton.addActionListener(e -> addBlankRow());

        JButton deleteRowButton = new JButton("Delete Row");
        styleDangerButton(deleteRowButton);
        deleteRowButton.addActionListener(e -> deleteSelectedRow());

        JButton saveButton = new JButton("Save Changes");
        stylePrimaryButton(saveButton);
        saveButton.addActionListener(e -> saveCurrentConfig());

        filterCard.add(selectorLabel);
        filterCard.add(fileSelector);
        filterCard.add(reloadButton);
        filterCard.add(addRowButton);
        filterCard.add(deleteRowButton);
        filterCard.add(saveButton);

        wrapper.add(filterCard);
        wrapper.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Theme.CARD_BACKGROUND);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        tableCard.setAlignmentX(LEFT_ALIGNMENT);

        configTable.setFont(Theme.FONT_BODY);
        configTable.setRowHeight(28);
        configTable.getTableHeader().setFont(Theme.FONT_BUTTON);
        configTable.setFillsViewportHeight(true);

        JScrollPane tableScrollPane = new JScrollPane(configTable);
        tableScrollPane.setPreferredSize(new Dimension(1020, 560));

        tableCard.add(tableScrollPane, BorderLayout.CENTER);
        wrapper.add(tableCard);

        add(wrapper, BorderLayout.CENTER);
    }

    private void loadSelectedConfig() {
        String selectedKey = (String) fileSelector.getSelectedItem();
        if (selectedKey == null) {
            return;
        }

        String filename = selectorToFile.get(selectedKey);
        if (filename == null) {
            return;
        }

        Path file = PathHelper.getDataFile(filename);
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        currentHeaders.clear();

        try {
            if (!Files.exists(file)) {
                JOptionPane.showMessageDialog(this, "Missing file: data/" + filename);
                return;
            }

            try (BufferedReader br = Files.newBufferedReader(file)) {
                String headerLine = br.readLine();
                if (headerLine == null || headerLine.trim().isEmpty()) {
                    return;
                }

                List<String> headers = CsvUtil.splitCsvLine(headerLine);
                currentHeaders.addAll(headers);

                for (String header : headers) {
                    tableModel.addColumn(header);
                }

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    List<String> values = CsvUtil.splitCsvLine(line);
                    Object[] row = new Object[headers.size()];
                    for (int i = 0; i < headers.size(); i++) {
                        row[i] = i < values.size() ? values.get(i) : "";
                    }
                    tableModel.addRow(row);
                }
            }

            System.out.println("[PayrollConfigurationPanel] loaded file=" + filename + " rows=" + tableModel.getRowCount());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load data/" + filename + ": " + e.getMessage());
            System.out.println("[PayrollConfigurationPanel] load error file=" + filename + " error=" + e.getMessage());
        }
    }

    private void addBlankRow() {
        if (tableModel.getColumnCount() == 0) {
            return;
        }

        Object[] row = new Object[tableModel.getColumnCount()];
        for (int i = 0; i < row.length; i++) {
            row[i] = "";
        }
        tableModel.addRow(row);
    }

    private void deleteSelectedRow() {
        int row = configTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a row to delete.");
            return;
        }

        int modelRow = configTable.convertRowIndexToModel(row);
        tableModel.removeRow(modelRow);
    }

    private void saveCurrentConfig() {
        String selectedKey = (String) fileSelector.getSelectedItem();
        if (selectedKey == null) {
            return;
        }

        String filename = selectorToFile.get(selectedKey);
        if (filename == null) {
            return;
        }

        Path file = PathHelper.getDataFile(filename);

        try (FileWriter fw = new FileWriter(file.toFile(), false)) {
            fw.write(String.join(",", currentHeaders));
            fw.write("\n");

            for (int row = 0; row < tableModel.getRowCount(); row++) {
                StringBuilder line = new StringBuilder();
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    if (col > 0) {
                        line.append(',');
                    }
                    Object value = tableModel.getValueAt(row, col);
                    line.append(escapeCsv(value == null ? "" : value.toString().trim()));
                }
                fw.write(line.toString());
                fw.write("\n");
            }

            fw.flush();
            JOptionPane.showMessageDialog(this, "Saved data/" + filename + " successfully.");
            System.out.println("[PayrollConfigurationPanel] saved file=" + filename
                    + " rows=" + tableModel.getRowCount()
                    + " actor=" + session.getEmployeeId());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to save data/" + filename + ": " + e.getMessage());
            System.out.println("[PayrollConfigurationPanel] save error file=" + filename + " error=" + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(Theme.FONT_BUTTON);
        button.setBackground(Theme.PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(Theme.FONT_BUTTON);
        button.setBackground(Theme.BUTTON_BACKGROUND);
        button.setForeground(Theme.BUTTON_TEXT);
        button.setFocusPainted(false);
    }

    private void styleDangerButton(JButton button) {
        button.setFont(Theme.FONT_BUTTON);
        button.setBackground(new Color(200, 80, 80));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }
}
