package com.flc.gui;

import com.flc.model.*;
import com.flc.service.BookingSystem;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for cancelling an existing booking. Shows a member's current bookings
 * and allows cancellation with a confirmation dialog.
 *
 * @author FLC Development Team
 */
public class CancelBookingPanel extends JPanel {

    private final BookingSystem bookingSystem;
    private JComboBox<Member> memberCombo;
    private JTable bookingsTable;
    private CancelTableModel tableModel;
    private JLabel statusLabel;

    public CancelBookingPanel(BookingSystem bookingSystem) {
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
        JButton loadBtn = FLCTheme.createPrimaryButton("\uD83D\uDD04  Load My Bookings");
        loadBtn.addActionListener(e -> loadBookings());
        topCard.add(loadBtn);
        content.add(topCard, BorderLayout.NORTH);

        // ─── Center: Bookings Table ──────────────────────────────
        JPanel tableCard = FLCTheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(FLCTheme.createSectionHeader("\uD83D\uDCCB", "Your Bookings"), BorderLayout.NORTH);

        tableModel = new CancelTableModel();
        bookingsTable = new JTable(tableModel);
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCard.add(FLCTheme.createStyledScrollPane(bookingsTable), BorderLayout.CENTER);

        // Bottom bar
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setOpaque(false);

        statusLabel = FLCTheme.createStatusLabel("Select a member and load their bookings.");
        bottomBar.add(statusLabel, BorderLayout.CENTER);

        JButton cancelBtn = FLCTheme.createDangerButton("\u274C  Cancel Selected Booking");
        cancelBtn.addActionListener(e -> cancelSelectedBooking());
        bottomBar.add(cancelBtn, BorderLayout.EAST);

        tableCard.add(bottomBar, BorderLayout.SOUTH);
        content.add(tableCard, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    private void loadBookings() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null)
            return;
        List<Lesson> cancelableLessons = member.getBookedLessons().stream()
                .filter(lesson -> member.getBookingStatus(lesson) == BookingStatus.BOOKED)
                .collect(java.util.stream.Collectors.toList());
        tableModel.setLessons(cancelableLessons);
        statusLabel.setText(member.getName() + " has " + cancelableLessons.size() + " cancelable booking(s).");
    }

    private void cancelSelectedBooking() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null) {
            JOptionPane.showMessageDialog(this, "Please select a member.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = bookingsTable.convertRowIndexToModel(selectedRow);
        Lesson lesson = tableModel.getLessonAt(modelRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Are you sure you want to cancel your booking for:\n%s\n(%s - %s %s, Week %d)?",
                        lesson.getLessonId(),
                        lesson.getExerciseType().getDisplayName(),
                        lesson.getDay().getDisplayName(),
                        lesson.getTimeSlot().getDisplayName(),
                        lesson.getWeekNumber()),
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bookingSystem.cancelBooking(member, lesson);
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully.",
                        "Cancellation Successful", JOptionPane.INFORMATION_MESSAGE);
                loadBookings();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Cancellation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class CancelTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = { "Lesson ID", "Exercise", "Day", "Time Slot", "Week", "Booked" };
        private List<Lesson> lessons = new ArrayList<>();

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
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Lesson l = lessons.get(row);
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
                    return l.getBookedMembers().size() + "/4";
                default:
                    return "";
            }
        }

        public void setLessons(List<Lesson> lessons) {
            this.lessons = lessons;
            fireTableDataChanged();
        }

        public Lesson getLessonAt(int row) {
            return lessons.get(row);
        }
    }
}
