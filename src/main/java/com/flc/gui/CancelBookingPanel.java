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

    /**
     * Constructs the CancelBookingPanel.
     *
     * @param bookingSystem the booking system instance
     */
    public CancelBookingPanel(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    /**
     * Builds the UI components.
     */
    private void buildUI() {
        JLabel title = new JLabel("Cancel a Booking", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JPanel topControl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topControl.setBorder(BorderFactory.createTitledBorder("Select Member"));
        memberCombo = new JComboBox<>();
        for (Member m : bookingSystem.getMembers())
            memberCombo.addItem(m);
        topControl.add(new JLabel("Member:"));
        topControl.add(memberCombo);
        JButton loadBtn = new JButton("Load My Bookings");
        loadBtn.addActionListener(e -> loadBookings());
        topControl.add(loadBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(topControl, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new CancelTableModel();
        bookingsTable = new JTable(tableModel);
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingsTable.setRowHeight(25);
        bookingsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        add(new JScrollPane(bookingsTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton cancelBtn = new JButton("Cancel Selected Booking");
        cancelBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        cancelBtn.addActionListener(e -> cancelSelectedBooking());
        bottomPanel.add(cancelBtn, BorderLayout.EAST);

        statusLabel = new JLabel("Select a member and load their bookings.");
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Loads the selected member's bookings into the table.
     */
    private void loadBookings() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null)
            return;
        tableModel.setLessons(new ArrayList<>(member.getBookedLessons()));
        statusLabel.setText(member.getName() + " has " + member.getBookedLessons().size() + " booking(s).");
    }

    /**
     * Cancels the selected booking after confirmation.
     */
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
            bookingSystem.cancelBooking(member, lesson);
            JOptionPane.showMessageDialog(this, "Booking cancelled successfully.",
                    "Cancellation Successful", JOptionPane.INFORMATION_MESSAGE);
            loadBookings();
        }
    }

    /**
     * Table model for member's bookings.
     */
    private static class CancelTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
                "Lesson ID", "Exercise", "Day", "Time Slot", "Week", "Booked"
        };
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
