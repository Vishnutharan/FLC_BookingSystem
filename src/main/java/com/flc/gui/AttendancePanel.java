package com.flc.gui;

import com.flc.exception.InvalidRatingException;
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
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

/**
 * Panel for recording attendance together with the required review and rating.
 */
public class AttendancePanel extends JPanel {

    private static final int MAX_COMMENT_LENGTH = 500;

    private final BookingSystem bookingSystem;
    private JComboBox<Member> memberCombo;
    private JTable attendanceTable;
    private AttendanceTableModel tableModel;
    private javax.swing.JLabel statusLabel;

    public AttendancePanel(BookingSystem bookingSystem) {
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
        javax.swing.JButton loadBtn = FLCTheme.createPrimaryButton("\uD83D\uDCE5  Load Pending Lessons");
        loadBtn.addActionListener(e -> loadBookings());
        topCard.add(loadBtn);
        content.add(topCard, BorderLayout.NORTH);

        JPanel tableCard = FLCTheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(FLCTheme.createSectionHeader("\u2705", "Attend Lesson and Submit Review"), BorderLayout.NORTH);

        tableModel = new AttendanceTableModel();
        attendanceTable = new JTable(tableModel);
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCard.add(FLCTheme.createStyledScrollPane(attendanceTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setOpaque(false);
        statusLabel = FLCTheme.createStatusLabel("Select a member to record attendance and review details.");
        bottomBar.add(statusLabel, BorderLayout.CENTER);

        javax.swing.JButton attendBtn = FLCTheme.createSuccessButton("\u2714  Attend Selected Lesson");
        attendBtn.addActionListener(e -> markAsAttended());
        bottomBar.add(attendBtn, BorderLayout.EAST);

        tableCard.add(bottomBar, BorderLayout.SOUTH);
        content.add(tableCard, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    private void loadBookings() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null) {
            return;
        }

        List<Booking> pendingBookings = member.getPendingBookings().stream()
                .sorted(Comparator.comparing((Booking booking) -> booking.getLesson().getWeekNumber())
                        .thenComparing(booking -> booking.getLesson().getDay().ordinal())
                        .thenComparing(booking -> booking.getLesson().getTimeSlot().ordinal()))
                .collect(Collectors.toList());

        tableModel.setBookings(pendingBookings);
        statusLabel.setText(member.getName() + " has " + pendingBookings.size() + " lesson(s) awaiting attendance.");
    }

    private void markAsAttended() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null) {
            JOptionPane.showMessageDialog(this, "Please select a member.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = attendanceTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a lesson to attend.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Booking booking = tableModel.getBookingAt(attendanceTable.convertRowIndexToModel(selectedRow));
        Lesson lesson = booking.getLesson();

        JPanel formPanel = new JPanel(new BorderLayout(0, 10));
        formPanel.setOpaque(false);

        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        ratingPanel.setOpaque(false);
        ratingPanel.add(FLCTheme.createFieldLabel("Rating:"));
        JComboBox<Integer> ratingCombo = new JComboBox<>(new Integer[] { 1, 2, 3, 4, 5 });
        ratingCombo.setSelectedItem(5);
        FLCTheme.styleComboBox(ratingCombo);
        ratingPanel.add(ratingCombo);
        formPanel.add(ratingPanel, BorderLayout.NORTH);

        JTextArea commentArea = new JTextArea(5, 36);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setFont(FLCTheme.FONT_BODY);
        commentArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FLCTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        formPanel.add(new JScrollPane(commentArea), BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this,
                formPanel,
                "Attend " + lesson.getLessonId() + " and Submit Review",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String comment = commentArea.getText().trim();
        if (comment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "A review comment is required when attending a lesson.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (comment.length() > MAX_COMMENT_LENGTH) {
            JOptionPane.showMessageDialog(this,
                    "Comment exceeds maximum length of " + MAX_COMMENT_LENGTH + " characters.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            bookingSystem.attendLessonWithReview(member, lesson, (Integer) ratingCombo.getSelectedItem(), comment);
            JOptionPane.showMessageDialog(this,
                    String.format("Booking %s marked as attended and reviewed successfully.", booking.getBookingId()),
                    "Attendance Recorded", JOptionPane.INFORMATION_MESSAGE);
            loadBookings();
        } catch (InvalidRatingException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Attendance Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class AttendanceTableModel extends AbstractTableModel {
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
