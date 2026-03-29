package com.flc.gui;

import com.flc.model.Booking;
import com.flc.model.Lesson;
import com.flc.model.Member;
import com.flc.service.BookingSystem;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

/**
 * Panel for cancelling a pending booking.
 */
public class CancelBookingPanel extends JPanel {

    private final BookingSystem bookingSystem;
    private JComboBox<Member> memberCombo;
    private JTable bookingsTable;
    private CancelTableModel tableModel;
    private javax.swing.JLabel statusLabel;

    public CancelBookingPanel(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
        setLayout(new BorderLayout());
        setBackground(FLCTheme.CONTENT_BG);
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        JPanel content = UIHelper.createContentPanel();

        JPanel topCard = FLCTheme.createCardPanel();
        topCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 5));
        memberCombo = new JComboBox<>();
        for (Member member : bookingSystem.getMembers()) {
            memberCombo.addItem(member);
        }
        FLCTheme.styleComboBox(memberCombo);
        topCard.add(FLCTheme.createFieldLabel("Member:"));
        topCard.add(memberCombo);
        javax.swing.JButton loadBtn = FLCTheme.createPrimaryButton("\uD83D\uDD04  Load Cancelable Bookings");
        loadBtn.addActionListener(e -> loadBookings());
        topCard.add(loadBtn);
        content.add(topCard, BorderLayout.NORTH);

        JPanel tableCard = FLCTheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(FLCTheme.createSectionHeader("\uD83D\uDCCB", "Pending Bookings"), BorderLayout.NORTH);

        tableModel = new CancelTableModel();
        bookingsTable = new JTable(tableModel);
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCard.add(FLCTheme.createStyledScrollPane(bookingsTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setOpaque(false);
        statusLabel = FLCTheme.createStatusLabel("Select a member and load their pending bookings.");
        bottomBar.add(statusLabel, BorderLayout.CENTER);

        javax.swing.JButton cancelBtn = FLCTheme.createDangerButton("\u274C  Cancel Selected Booking");
        cancelBtn.addActionListener(e -> cancelSelectedBooking());
        bottomBar.add(cancelBtn, BorderLayout.EAST);

        tableCard.add(bottomBar, BorderLayout.SOUTH);
        content.add(tableCard, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    private void loadBookings() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null) {
            return;
        }

        List<Booking> cancelableBookings = member.getPendingBookings().stream()
                .sorted(Comparator.comparing((Booking booking) -> booking.getLesson().getWeekNumber())
                        .thenComparing(booking -> booking.getLesson().getDay().ordinal())
                        .thenComparing(booking -> booking.getLesson().getTimeSlot().ordinal()))
                .collect(Collectors.toList());
        tableModel.setBookings(cancelableBookings);
        statusLabel.setText(member.getName() + " has " + cancelableBookings.size() + " cancelable booking(s).");
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

        Booking booking = tableModel.getBookingAt(bookingsTable.convertRowIndexToModel(selectedRow));
        Lesson lesson = booking.getLesson();

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Cancel booking %s for:\n%s (%s - %s %s, Week %d)?",
                        booking.getBookingId(),
                        lesson.getLessonId(),
                        lesson.getExerciseType().getDisplayName(),
                        lesson.getDay().getDisplayName(),
                        lesson.getTimeSlot().getDisplayName(),
                        lesson.getWeekNumber()),
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bookingSystem.cancelBooking(member, lesson);
                JOptionPane.showMessageDialog(this,
                        "Booking " + booking.getBookingId() + " cancelled successfully.",
                        "Cancellation Successful", JOptionPane.INFORMATION_MESSAGE);
                loadBookings();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Cancellation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class CancelTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = { "Booking ID", "Lesson ID", "Exercise", "Day", "Time Slot",
                "Week", "Status" };
        private List<Booking> bookings = new ArrayList<>();

        @Override
        public int getRowCount() {
            return bookings.size();
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
            Booking booking = bookings.get(row);
            Lesson lesson = booking.getLesson();
            switch (col) {
                case 0:
                    return booking.getBookingId();
                case 1:
                    return lesson.getLessonId();
                case 2:
                    return lesson.getExerciseType().getDisplayName();
                case 3:
                    return lesson.getDay().getDisplayName();
                case 4:
                    return lesson.getTimeSlot().getDisplayName();
                case 5:
                    return lesson.getWeekNumber();
                case 6:
                    return booking.getStatus().getDisplayName();
                default:
                    return "";
            }
        }

        public void setBookings(List<Booking> bookings) {
            this.bookings = new ArrayList<>(bookings);
            fireTableDataChanged();
        }

        public Booking getBookingAt(int row) {
            return bookings.get(row);
        }
    }
}
