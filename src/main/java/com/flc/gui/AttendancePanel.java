package com.flc.gui;

import com.flc.model.*;
import com.flc.service.BookingSystem;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel for managing lesson attendance. Allows marking booked lessons as
 * attended.
 *
 * @author FLC Development Team
 */
public class AttendancePanel extends JPanel {

    private final BookingSystem bookingSystem;
    private JComboBox<Member> memberCombo;
    private JTable attendanceTable;
    private AttendanceTableModel tableModel;
    private JLabel statusLabel;

    public AttendancePanel(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
        setLayout(new BorderLayout());
        setBackground(FLCTheme.CONTENT_BG);
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        JPanel content = UIHelper.createContentPanel();

        // ─── Top: Member Selector ────────────────────────────────
        JPanel topCard = FLCTheme.createCardPanel();
        topCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 5));
        memberCombo = new JComboBox<>();
        for (Member m : bookingSystem.getMembers())
            memberCombo.addItem(m);
        FLCTheme.styleComboBox(memberCombo);
        topCard.add(FLCTheme.createFieldLabel("Member:"));
        topCard.add(memberCombo);
        JButton loadBtn = FLCTheme.createPrimaryButton("\uD83D\uDCE5  Load My Bookings");
        loadBtn.addActionListener(e -> loadBookings());
        topCard.add(loadBtn);
        content.add(topCard, BorderLayout.NORTH);

        // ─── Center: Attendance Table ─────────────────────────────
        JPanel tableCard = FLCTheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(FLCTheme.createSectionHeader("\u2705", "Record Attendance"), BorderLayout.NORTH);

        tableModel = new AttendanceTableModel();
        attendanceTable = new JTable(tableModel);
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCard.add(FLCTheme.createStyledScrollPane(attendanceTable), BorderLayout.CENTER);

        // Bottom bar
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setOpaque(false);

        statusLabel = FLCTheme.createStatusLabel("Select a member to manage their attendance.");
        bottomBar.add(statusLabel, BorderLayout.CENTER);

        JButton attendBtn = FLCTheme.createSuccessButton("\u2714  Mark as Attended");
        attendBtn.addActionListener(e -> markAsAttended());
        bottomBar.add(attendBtn, BorderLayout.EAST);

        tableCard.add(bottomBar, BorderLayout.SOUTH);
        content.add(tableCard, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    private void loadBookings() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null)
            return;

        List<Lesson> activeBookings = member.getBookedLessons().stream()
                .filter(l -> member.getBookingStatus(l) == BookingStatus.BOOKED)
                .collect(Collectors.toList());

        tableModel.setLessons(activeBookings, member);
        statusLabel.setText(member.getName() + " has " + activeBookings.size() + " pending lesson(s).");
    }

    private void markAsAttended() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null)
            return;

        int selectedRow = attendanceTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a lesson to mark as attended.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = attendanceTable.convertRowIndexToModel(selectedRow);
        Lesson lesson = tableModel.getLessonAt(modelRow);

        bookingSystem.attendLesson(member, lesson);
        JOptionPane.showMessageDialog(this,
                "Successfully marked " + lesson.getLessonId() + " as Attended for " + member.getName() + ".",
                "Attendance Recorded", JOptionPane.INFORMATION_MESSAGE);

        loadBookings(); // refresh
    }

    private static class AttendanceTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = { "Lesson ID", "Exercise", "Day", "Time Slot", "Week", "Status" };
        private List<Lesson> lessons = new ArrayList<>();
        private Member currentMember;

        @Override
        public int getRowCount() {
            return lessons.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int col) {
            return COLUMNS[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            Lesson l = lessons.get(row);
            Member member = currentMember;
            switch (col) {
                case 0:
                    return l.getLessonId();
                case 1:
                    return l.getExerciseType().getDisplayName();
                case 2:
                    return l.getDay().getDisplayName();
                case 3:
                    return l.getTimeSlot().getDisplayName();
                case 4:
                    return l.getWeekNumber();
                case 5:
                    return member != null ? member.getBookingStatus(l) : "N/A";
                default:
                    return "";
            }
        }

        public void setLessons(List<Lesson> lessons, Member member) {
            this.lessons = lessons;
            this.currentMember = member;
            fireTableDataChanged();
        }

        public Lesson getLessonAt(int row) {
            return lessons.get(row);
        }
    }
}
