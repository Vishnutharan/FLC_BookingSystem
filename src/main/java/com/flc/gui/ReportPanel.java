package com.flc.gui;

import com.flc.service.BookingSystem;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Panel for generating and displaying cycle reports.
 */
public class ReportPanel extends JPanel {

    private final BookingSystem bookingSystem;
    private JTextArea reportArea;
    private JComboBox<Integer> cycleCombo;
    private String currentReportType = "";

    public ReportPanel(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
        setLayout(new BorderLayout());
        setBackground(FLCTheme.CONTENT_BG);
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        JPanel content = UIHelper.createContentPanel();

        JPanel topSection = new JPanel(new BorderLayout(0, 10));
        topSection.setOpaque(false);

        JPanel cyclePanel = FLCTheme.createCardPanel();
        cyclePanel.setLayout(new BorderLayout(10, 0));
        cyclePanel.add(FLCTheme.createFieldLabel("Select 4-Week Cycle:"), BorderLayout.WEST);

        cycleCombo = new JComboBox<>();
        int cycleCount = Math.max(bookingSystem.getCycleCount(), 1);
        for (int i = 1; i <= cycleCount; i++) {
            cycleCombo.addItem(i);
        }
        FLCTheme.styleComboBox(cycleCombo);
        cyclePanel.add(cycleCombo, BorderLayout.CENTER);

        JLabel cycleHint = new JLabel("Each cycle covers 4 timetable weeks.");
        cycleHint.setFont(FLCTheme.FONT_SMALL);
        cycleHint.setForeground(FLCTheme.TEXT_SECONDARY);
        cyclePanel.add(cycleHint, BorderLayout.EAST);

        topSection.add(cyclePanel, BorderLayout.NORTH);

        JPanel actionsRow = new JPanel(new GridLayout(2, 2, 15, 15));
        actionsRow.setOpaque(false);
        actionsRow.add(createReportCard(
                "Requirement Audit",
                "Verify members, lessons, weekends, and reviews",
                FLCTheme.STATS_PURPLE_BG,
                FLCTheme.PURPLE,
                this::generateRequirementAudit));
        actionsRow.add(createReportCard(
                "Attendance and Rating",
                "Report 1 for the selected 4-week cycle",
                FLCTheme.STATS_BLUE_BG,
                FLCTheme.PRIMARY,
                this::generateAttendanceReport));
        actionsRow.add(createReportCard(
                "Income by Exercise",
                "Report 2 and highest-income exercise type",
                FLCTheme.STATS_GREEN_BG,
                FLCTheme.SUCCESS,
                this::generateIncomeReport));
        actionsRow.add(createReportCard(
                "Export to TXT",
                "Save the current report as a text file",
                FLCTheme.STATS_AMBER_BG,
                FLCTheme.WARNING,
                this::exportToTxt));

        topSection.add(actionsRow, BorderLayout.CENTER);
        content.add(topSection, BorderLayout.NORTH);

        JPanel reportCard = FLCTheme.createCardPanel();
        reportCard.setLayout(new BorderLayout(0, 10));
        reportCard.add(FLCTheme.createSectionHeader("RE", "Report Output"), BorderLayout.NORTH);

        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(FLCTheme.FONT_MONO);
        reportArea.setBackground(new Color(252, 253, 255));
        reportArea.setForeground(FLCTheme.TEXT_PRIMARY);
        reportArea.setMargin(new Insets(15, 15, 15, 15));
        reportArea.setText(bookingSystem.generateRequirementAuditReport());
        reportArea.setBorder(BorderFactory.createLineBorder(FLCTheme.BORDER_COLOR));
        currentReportType = "Requirement_Audit_Report";

        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(FLCTheme.BORDER_COLOR));
        reportCard.add(scrollPane, BorderLayout.CENTER);

        content.add(reportCard, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel createReportCard(String title, String desc, Color bgColor, Color accentColor, Runnable action) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                        FLCTheme.CARD_RADIUS, FLCTheme.CARD_RADIUS));
                g2.setColor(accentColor);
                g2.fillRoundRect(15, getHeight() - 4, getWidth() - 30, 3, 3, 3);
                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBackground(bgColor);
        card.setLayout(new BorderLayout(10, 8));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FLCTheme.FONT_SUBHEADER);
        titleLabel.setForeground(FLCTheme.TEXT_PRIMARY);
        card.add(titleLabel, BorderLayout.NORTH);

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(FLCTheme.FONT_SMALL);
        descLabel.setForeground(FLCTheme.TEXT_SECONDARY);
        card.add(descLabel, BorderLayout.CENTER);

        Color hoverBg = new Color(
                Math.max(0, bgColor.getRed() - 12),
                Math.max(0, bgColor.getGreen() - 12),
                Math.max(0, bgColor.getBlue() - 12));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(hoverBg);
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(bgColor);
                card.repaint();
            }
        });

        return card;
    }

    private int selectedCycle() {
        Integer selected = (Integer) cycleCombo.getSelectedItem();
        return selected == null ? 1 : selected;
    }

    private void generateRequirementAudit() {
        String report = bookingSystem.generateRequirementAuditReport();
        reportArea.setText(report);
        reportArea.setCaretPosition(0);
        currentReportType = "Requirement_Audit_Report";
    }

    private void generateAttendanceReport() {
        int cycle = selectedCycle();
        String report = bookingSystem.generateAttendanceReportForCycle(cycle);
        reportArea.setText(report);
        reportArea.setCaretPosition(0);
        currentReportType = "Attendance_Rating_Report_Cycle_" + cycle;
    }

    private void generateIncomeReport() {
        int cycle = selectedCycle();
        String report = bookingSystem.generateIncomeReportForCycle(cycle);
        reportArea.setText(report);
        reportArea.setCaretPosition(0);
        currentReportType = "Income_Report_Cycle_" + cycle;
    }

    private void exportToTxt() {
        String text = reportArea.getText();
        if (text.isEmpty() || text.startsWith("Select a cycle")) {
            JOptionPane.showMessageDialog(this,
                    "Please generate a report first before exporting.",
                    "No Report",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report to TXT");
        String defaultName = currentReportType.isEmpty() ? "FLC_Report" : currentReportType;
        fileChooser.setSelectedFile(new File(defaultName + ".txt"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(text);
                JOptionPane.showMessageDialog(this,
                        "Report exported successfully to:\n" + file.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting report: " + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
