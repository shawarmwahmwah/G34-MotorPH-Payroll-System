package motorph.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import motorph.model.AttendanceRecord;
import motorph.model.Employee;
import motorph.model.LeaveRequest;
import motorph.repository.AttendanceRepository;
import motorph.repository.CsvAttendanceRepository;
import motorph.repository.LeaveRepository;
import motorph.ui.Theme;
import motorph.ui.session.UserSession;

public class DashboardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int AVATAR_SIZE = 120;
    private static final DateTimeFormatter INPUT_DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter OUTPUT_DATE_FMT = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH);
    private static final Map<String, BufferedImage> PROFILE_PHOTO_CACHE = new HashMap<>();

    private final Employee employee;
    private final DashboardStats stats;
    private final JLabel avatarLabel;

    public DashboardPanel(UserSession session) {
        this.employee = session.getEmployee();
        this.stats = buildDashboardStats(employee.getEmployeeId());
        this.avatarLabel = new JLabel("", SwingConstants.CENTER);

        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(Theme.CONTENT_BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(6, 6, 8, 6));

        page.add(buildTopProfileCard());
        page.add(Box.createRigidArea(new Dimension(0, 8)));
        page.add(buildBodyGrid());

        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.CONTENT_BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildTopProfileCard() {
        JPanel card = createCardPanel(new BorderLayout(12, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        styleAvatarLabel();
        loadAvatar();

        JButton uploadPhotoButton = new JButton("Upload Photo");
        uploadPhotoButton.setFont(Theme.FONT_BUTTON);
        uploadPhotoButton.setBackground(Theme.BUTTON_BACKGROUND);
        uploadPhotoButton.setForeground(Theme.BUTTON_TEXT);
        uploadPhotoButton.setFocusPainted(false);
        uploadPhotoButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        uploadPhotoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadPhotoButton.addActionListener(e -> uploadPhoto());

        left.add(Box.createRigidArea(new Dimension(0, 2)));
        left.add(avatarLabel);
        left.add(Box.createRigidArea(new Dimension(0, 8)));
        left.add(uploadPhotoButton);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(safe(employee.getFullName()));
        nameLabel.setFont(Theme.FONT_TITLE.deriveFont(26f));
        nameLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel identityLine = new JLabel("Employee ID: " + safe(employee.getEmployeeId()));
        identityLine.setFont(Theme.FONT_BODY);
        identityLine.setForeground(Theme.TEXT_SECONDARY);

        JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chipRow.setOpaque(false);
        chipRow.add(createChip("Position", safe(employee.getPosition())));
        chipRow.add(createChip("Status", safe(employee.getStatus())));
        chipRow.add(createChip("Role", safe(employee.getRole()).toUpperCase(Locale.ENGLISH)));

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 8, 0));
        statsRow.setOpaque(false);
        statsRow.add(createStatCard("Days Worked", String.valueOf(stats.totalDaysWorked)));
        statsRow.add(createStatCard("Leaves Taken", String.format(Locale.ENGLISH, "%.2f", stats.totalLeavesTaken)));
        statsRow.add(createStatCard("Overtime Hrs", String.format(Locale.ENGLISH, "%.2f", stats.totalOvertimeHours)));

        right.add(nameLabel);
        right.add(Box.createRigidArea(new Dimension(0, 2)));
        right.add(identityLine);
        right.add(Box.createRigidArea(new Dimension(0, 8)));
        right.add(chipRow);
        right.add(Box.createRigidArea(new Dimension(0, 10)));
        right.add(statsRow);

        card.add(left, BorderLayout.WEST);
        card.add(right, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildBodyGrid() {
        JPanel columns = new JPanel(new GridLayout(1, 2, 8, 0));
        columns.setOpaque(false);
        columns.setAlignmentX(LEFT_ALIGNMENT);
        columns.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel leftColumn = new JPanel();
        leftColumn.setOpaque(false);
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.add(createSectionCard("Personal Information", new String[][] {
                {"First Name", safe(employee.getFirstName())},
                {"Last Name", safe(employee.getLastName())},
                {"Birthday", formatDate(safe(employee.getBirthday()))},
                {"Address", safe(employee.getAddress())},
                {"Phone Number", safe(employee.getPhoneNumber())}
        }));
        leftColumn.add(Box.createRigidArea(new Dimension(0, 8)));
        leftColumn.add(createSectionCard("Government IDs", new String[][] {
                {"SSS #", safe(employee.getSssNumber())},
                {"PhilHealth #", safe(employee.getPhilhealthNumber())},
                {"TIN #", safe(employee.getTinNumber())},
                {"Pag-IBIG #", safe(employee.getPagibigNumber())}
        }));

        JPanel rightColumn = new JPanel();
        rightColumn.setOpaque(false);
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.add(createSectionCard("Employment Details", new String[][] {
                {"Employee ID", safe(employee.getEmployeeId())},
                {"Position", safe(employee.getPosition())},
                {"Status", safe(employee.getStatus())},
                {"Immediate Supervisor", safe(employee.getImmediateSupervisor())},
                {"Role", safe(employee.getRole()).toUpperCase(Locale.ENGLISH)}
        }));
        rightColumn.add(Box.createRigidArea(new Dimension(0, 8)));
        rightColumn.add(createSectionCard("Salary Details", new String[][] {
                {"Basic Salary", formatMoney(employee.getBasicSalary())},
                {"Rice Subsidy", formatMoney(employee.getRiceSubsidy())},
                {"Phone Allowance", formatMoney(employee.getPhoneAllowance())},
                {"Clothing Allowance", formatMoney(employee.getClothingAllowance())},
                {"Gross Semi-monthly", formatMoney(employee.getGrossSemiMonthlyRate())},
                {"Hourly Rate", formatMoney(employee.getHourlyRate())}
        }));

        columns.add(leftColumn);
        columns.add(rightColumn);
        return columns;
    }

    private JPanel createSectionCard(String title, String[][] rows) {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_HEADING.deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(Theme.TEXT_PRIMARY);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 0, 3, 8);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < rows.length; i++) {
            JLabel key = new JLabel(rows[i][0] + ":");
            key.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD));
            key.setForeground(Theme.TEXT_SECONDARY);

            JLabel value = new JLabel("<html><div style='width:270px;'>" + escapeHtml(rows[i][1]) + "</div></html>");
            value.setFont(Theme.FONT_BODY);
            value.setForeground(Theme.TEXT_PRIMARY);

            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            grid.add(key, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            grid.add(value, gbc);
        }

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(Box.createRigidArea(new Dimension(0, 6)), BorderLayout.CENTER);
        card.add(grid, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createCardPanel(BorderLayout layout) {
        JPanel card = new JPanel(layout);
        card.setBackground(Theme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);
        return card;
    }

    private JPanel createStatCard(String label, String value) {
        JPanel card = new JPanel();
        card.setOpaque(true);
        card.setBackground(new Color(249, 251, 255));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 247), 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(Theme.FONT_SMALL);
        labelComp.setForeground(Theme.TEXT_SECONDARY);

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(Theme.FONT_HEADING);
        valueComp.setForeground(Theme.TEXT_PRIMARY);

        card.add(labelComp);
        card.add(Box.createRigidArea(new Dimension(0, 3)));
        card.add(valueComp);
        return card;
    }

    private JLabel createChip(String key, String value) {
        JLabel chip = new JLabel(key + " • " + value);
        chip.setFont(Theme.FONT_SMALL);
        chip.setForeground(Theme.PRIMARY_DARK);
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 204, 246), 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        chip.setOpaque(true);
        chip.setBackground(new Color(236, 242, 255));
        return chip;
    }

    private void styleAvatarLabel() {
        avatarLabel.setPreferredSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        avatarLabel.setMinimumSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        avatarLabel.setMaximumSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(new Color(233, 239, 255));
        avatarLabel.setForeground(Theme.PRIMARY_DARK);
        avatarLabel.setFont(Theme.FONT_TITLE);
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarLabel.setBorder(BorderFactory.createLineBorder(new Color(186, 204, 252), 1, true));
    }

    private void uploadPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Profile Photo");
        chooser.setFileFilter(new FileNameExtensionFilter("Image files (JPG, JPEG, PNG)", "jpg", "jpeg", "png"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return;
            }

            PROFILE_PHOTO_CACHE.put(employee.getEmployeeId(), image);
            avatarLabel.setText("");
            avatarLabel.setIcon(new ImageIcon(createCircularImage(image, AVATAR_SIZE)));
        } catch (Exception e) {
            System.out.println("[DashboardPanel] Failed to load profile image: " + e.getMessage());
        }
    }

    private void loadAvatar() {
        BufferedImage image = PROFILE_PHOTO_CACHE.get(employee.getEmployeeId());

        if (image != null) {
            avatarLabel.setText("");
            avatarLabel.setIcon(new ImageIcon(createCircularImage(image, AVATAR_SIZE)));
            return;
        }

        avatarLabel.setIcon(null);
        avatarLabel.setText(buildInitials(employee.getFirstName(), employee.getLastName()));
    }

    private BufferedImage createCircularImage(BufferedImage source, int size) {
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Double(0, 0, size, size));
        g2.drawImage(source, 0, 0, size, size, null);
        g2.dispose();
        return scaled;
    }

    private DashboardStats buildDashboardStats(String employeeId) {
        AttendanceRepository attendanceRepository = new CsvAttendanceRepository();
        LeaveRepository leaveRepository = new LeaveRepository();

        List<AttendanceRecord> attendanceRecords = attendanceRepository.findByEmployeeId(employeeId);
        List<LeaveRequest> leaveRequests = leaveRepository.loadAll();

        int totalDaysWorked = 0;
        double totalOvertimeHours = 0.0;
        double totalLeavesTaken = 0.0;

        for (AttendanceRecord record : attendanceRecords) {
            if (record.getWorkedMinutes() > 0) {
                totalDaysWorked++;
            }
            totalOvertimeHours += record.getOvertimeHours();
        }

        for (LeaveRequest request : leaveRequests) {
            if (!employeeId.equals(request.getEmployeeId())) {
                continue;
            }
            if (!"APPROVED".equalsIgnoreCase(request.getStatus())) {
                continue;
            }
            totalLeavesTaken += request.getDaysRequested();
        }

        return new DashboardStats(totalDaysWorked, totalLeavesTaken, totalOvertimeHours);
    }

    private String buildInitials(String firstName, String lastName) {
        String first = safe(firstName);
        String last = safe(lastName);

        String a = first.isEmpty() || "N/A".equals(first) ? "E" : first.substring(0, 1).toUpperCase(Locale.ENGLISH);
        String b = last.isEmpty() || "N/A".equals(last) ? "M" : last.substring(0, 1).toUpperCase(Locale.ENGLISH);
        return a + b;
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return "N/A";
        }

        try {
            return OUTPUT_DATE_FMT.format(LocalDate.parse(rawDate.trim(), INPUT_DATE_FMT));
        } catch (DateTimeParseException e) {
            return rawDate;
        }
    }

    private String formatMoney(double amount) {
        return String.format(Locale.ENGLISH, "%,.2f", amount);
    }

    private String safe(String text) {
        return text == null || text.trim().isEmpty() ? "N/A" : text.trim();
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static class DashboardStats {
        private final int totalDaysWorked;
        private final double totalLeavesTaken;
        private final double totalOvertimeHours;

        private DashboardStats(int totalDaysWorked, double totalLeavesTaken, double totalOvertimeHours) {
            this.totalDaysWorked = totalDaysWorked;
            this.totalLeavesTaken = totalLeavesTaken;
            this.totalOvertimeHours = totalOvertimeHours;
        }
    }
}
