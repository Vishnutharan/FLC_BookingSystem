package com.flc.gui;

import com.flc.service.BookingSystem;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Panel for generating and displaying reports. Supports Attendance &amp; Rating
 * reports
 * and Income reports with export to TXT functionality.
 *
 * @author FLC Development Team
 */
public class ReportPanel extends JPanel {

    private final BookingSystem bookingSystem;
    private JTextArea reportArea;
    private String currentReportType = "";

    /**
     * Constructs the ReportPanel.
     *
     * @param bookingSystem the booking system instance
     */
    public ReportPanel(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    /**
     * Builds the UI components.
     */
    private void buildUI() {
        JLabel title = new JLabel("Reports", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton attendanceBtn = new JButton("Generate Attendance & Rating Report");
        attendanceBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        attendanceBtn.addActionListener(e -> generateAttendanceReport());
        buttonPanel.add(attendanceBtn);

        JButton incomeBtn = new JButton("Generate Highest Income Report");
        incomeBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        incomeBtn.addActionListener(e -> generateIncomeReport());
        buttonPanel.add(incomeBtn);

        JButton exportBtn = new JButton("Export Report to TXT");
        exportBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        exportBtn.addActionListener(e -> exportToTxt());
        buttonPanel.add(exportBtn);

        // Report area
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setMargin(new Insets(10, 10, 10, 10));
        reportArea.setText("Click a button above to generate a report.");

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(buttonPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Generates and displays the attendance and rating report.
     */
    private void generateAttendanceReport() {
        String report = bookingSystem.generateAttendanceReport();
        reportArea.setText(report);
        reportArea.setCaretPosition(0);
        currentReportType = "Attendance_Rating_Report";
    }

    /**
     * Generates and displays the income report.
     */
    private void generateIncomeReport() {
        String report = bookingSystem.generateIncomeReport();
        reportArea.setText(report);
        reportArea.setCaretPosition(0);
        currentReportType = "Income_Report";
    }

    /**
     * Exports the current report text to a TXT file using JFileChooser.
     */
    private void exportToTxt() {
        String text = reportArea.getText();
        if (text.isEmpty() || text.startsWith("Click a button")) {
            JOptionPane.showMessageDialog(this, "Please generate a report first before exporting.",
                    "No Report", JOptionPane.WARNING_MESSAGE);
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
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting report: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
