package motorph.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.nio.file.Files;
import java.nio.file.Path;
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import motorph.ui.Theme;
import motorph.ui.session.UserSession;
import motorph.util.CsvUtil;
import motorph.util.PathHelper;

public class SystemLogsPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final String LOG_HEADER = "timestamp,actorId,action,details";

    private final UserSession session;
    private final JTextField actorFilterField;
    private final JTextField keywordFilterField;
    private final JComboBox<String> actionFilterCombo;
    private final DefaultTableModel tableModel;
    private final JTable logsTable;

    private List<LogEntry> allLogs;

    public SystemLogsPanel(UserSession session) {
        this.session = session;
        this.actorFilterField = new JTextField();
        this.keywordFilterField = new JTextField();
        this.actionFilterCombo = new JComboBox<>();
        this.tableModel = new DefaultTableModel(new Object[] {"Timestamp", "Actor ID", "Action", "Details"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.logsTable = new JTable(tableModel);
        this.allLogs = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        buildUI();
        loadLogsFromCsv();
        applyFilters();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Theme.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel("System Logs");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Track login actions, data edits, approvals, and payroll processing events.");
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

        JLabel actorLabel = new JLabel("Actor ID:");
        actorLabel.setFont(Theme.FONT_BODY);

        actorFilterField.setFont(Theme.FONT_BODY);
        actorFilterField.setPreferredSize(new Dimension(120, 34));

        JLabel actionLabel = new JLabel("Action:");
        actionLabel.setFont(Theme.FONT_BODY);

        actionFilterCombo.setFont(Theme.FONT_BODY);
        actionFilterCombo.setPreferredSize(new Dimension(210, 34));

        JLabel keywordLabel = new JLabel("Keyword:");
        keywordLabel.setFont(Theme.FONT_BODY);

        keywordFilterField.setFont(Theme.FONT_BODY);
        keywordFilterField.setPreferredSize(new Dimension(220, 34));

        JButton applyButton = new JButton("Apply");
        stylePrimaryButton(applyButton);
        applyButton.addActionListener(e -> applyFilters());

        JButton refreshButton = new JButton("Reload Logs");
        styleSecondaryButton(refreshButton);
        refreshButton.addActionListener(e -> {
            loadLogsFromCsv();
            applyFilters();
        });

        filterCard.add(actorLabel);
        filterCard.add(actorFilterField);
        filterCard.add(actionLabel);
        filterCard.add(actionFilterCombo);
        filterCard.add(keywordLabel);
        filterCard.add(keywordFilterField);
        filterCard.add(applyButton);
        filterCard.add(refreshButton);

        wrapper.add(filterCard);
        wrapper.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Theme.CARD_BACKGROUND);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        tableCard.setAlignmentX(LEFT_ALIGNMENT);

        logsTable.setFont(Theme.FONT_BODY);
        logsTable.setRowHeight(28);
        logsTable.getTableHeader().setFont(Theme.FONT_BUTTON);
        logsTable.setFillsViewportHeight(true);
        logsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane tableScrollPane = new JScrollPane(logsTable);
        tableScrollPane.setPreferredSize(new Dimension(1020, 560));

        tableCard.add(tableScrollPane, BorderLayout.CENTER);
        wrapper.add(tableCard);

        add(wrapper, BorderLayout.CENTER);
    }

    private void loadLogsFromCsv() {
        allLogs = new ArrayList<>();

        Path file = PathHelper.getDataFile("activity_log.csv");
        if (!Files.exists(file)) {
            actionFilterCombo.removeAllItems();
            actionFilterCombo.addItem("ALL");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(file);
            if (lines.isEmpty()) {
                actionFilterCombo.removeAllItems();
                actionFilterCombo.addItem("ALL");
                return;
            }

            List<String> dataLines = new ArrayList<>();
            String firstLine = lines.get(0);
            if (firstLine.startsWith(LOG_HEADER)) {
                String remainder = firstLine.substring(LOG_HEADER.length()).trim();
                if (!remainder.isEmpty()) {
                    dataLines.add(remainder);
                }
            } else {
                dataLines.add(firstLine);
            }

            for (int i = 1; i < lines.size(); i++) {
                dataLines.add(lines.get(i));
            }

            for (String line : dataLines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }

                List<String> values = CsvUtil.splitCsvLine(line);
                if (values.size() < 4) {
                    continue;
                }

                String timestamp = values.get(0).trim();
                String actorId = values.get(1).trim();
                String action = values.get(2).trim();

                StringBuilder details = new StringBuilder(values.get(3).trim());
                for (int i = 4; i < values.size(); i++) {
                    details.append(",").append(values.get(i).trim());
                }

                allLogs.add(new LogEntry(timestamp, actorId, action, details.toString()));
            }

            allLogs.sort(Comparator.comparing((LogEntry log) -> log.timestamp).reversed());
            buildActionFilterOptions();

            System.out.println("[SystemLogsPanel] actor=" + session.getEmployeeId() + " loadedLogs=" + allLogs.size());

        } catch (Exception e) {
            System.out.println("[SystemLogsPanel] Failed reading activity_log.csv: " + e.getMessage());
        }
    }

    private void buildActionFilterOptions() {
        Object selected = actionFilterCombo.getSelectedItem();

        Set<String> actions = new LinkedHashSet<>();
        actions.add("ALL");
        for (LogEntry log : allLogs) {
            if (!log.action.isEmpty()) {
                actions.add(log.action.toUpperCase(Locale.ENGLISH));
            }
        }

        actionFilterCombo.removeAllItems();
        for (String action : actions) {
            actionFilterCombo.addItem(action);
        }

        if (selected != null) {
            actionFilterCombo.setSelectedItem(selected.toString());
        }
        if (actionFilterCombo.getSelectedItem() == null && actionFilterCombo.getItemCount() > 0) {
            actionFilterCombo.setSelectedIndex(0);
        }
    }

    private void applyFilters() {
        tableModel.setRowCount(0);

        String actorFilter = safeLower(actorFilterField.getText().trim());
        String keyword = safeLower(keywordFilterField.getText().trim());
        String action = actionFilterCombo.getSelectedItem() == null
                ? "ALL"
                : actionFilterCombo.getSelectedItem().toString().trim().toUpperCase(Locale.ENGLISH);

        int shown = 0;
        for (LogEntry log : allLogs) {
            if (!actorFilter.isEmpty() && !safeLower(log.actorId).contains(actorFilter)) {
                continue;
            }

            if (!"ALL".equals(action) && !action.equals(log.action.toUpperCase(Locale.ENGLISH))) {
                continue;
            }

            if (!keyword.isEmpty()) {
                String haystack = safeLower(log.timestamp + " " + log.actorId + " " + log.action + " " + log.details);
                if (!haystack.contains(keyword)) {
                    continue;
                }
            }

            tableModel.addRow(new Object[] {
                    log.timestamp,
                    log.actorId,
                    log.action,
                    log.details
            });
            shown++;
        }

        System.out.println("[SystemLogsPanel] filter actor='" + actorFilter + "' action='" + action + "' keyword='" + keyword + "' shown=" + shown);
    }

    private String safeLower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ENGLISH).trim();
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

    private static class LogEntry {
        private final String timestamp;
        private final String actorId;
        private final String action;
        private final String details;

        private LogEntry(String timestamp, String actorId, String action, String details) {
            this.timestamp = timestamp;
            this.actorId = actorId;
            this.action = action;
            this.details = details;
        }
    }
}
