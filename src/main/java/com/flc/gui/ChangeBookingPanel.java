package com.flc.gui;

import com.flc.exception.LessonFullException;
import com.flc.exception.TimeConflictException;
import com.flc.model.Booking;
import com.flc.model.DayOfWeek;
import com.flc.model.Lesson;
import com.flc.model.Member;
import com.flc.model.TimeSlot;
import com.flc.service.BookingSystem;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

/**
 * Panel for changing an existing booking while preserving its booking ID.
 */
public class ChangeBookingPanel extends JPanel {

    private final BookingSystem bookingSystem;
    private JComboBox<Member> memberCombo;
    private JTable currentBookingsTable;
    private BookingTableModel currentBookingsModel;
    private JComboBox<Integer> newWeekCombo;
    private JComboBox<DayOfWeek> newDayCombo;
    private JComboBox<String> newTimeCombo;
    private JTable availableLessonsTable;
    private AvailableLessonTableModel availableModel;

    public ChangeBookingPanel(BookingSystem bookingSystem) {
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
        javax.swing.JButton loadBtn = FLCTheme.createPrimaryButton("\uD83D\uDD04  Load Changeable Bookings");
        loadBtn.addActionListener(e -> loadCurrentBookings());
        topCard.add(loadBtn);
        content.add(topCard, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.42);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);

        JPanel currentCard = FLCTheme.createCardPanel();
        currentCard.setLayout(new BorderLayout(0, 8));
        currentCard.add(
                FLCTheme.createSectionHeader("\uD83D\uDCCB", "Current Bookings  \u2014  Select the booking to change"),
                BorderLayout.NORTH);
        currentBookingsModel = new BookingTableModel();
        currentBookingsTable = new JTable(currentBookingsModel);
        currentBookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        currentCard.add(FLCTheme.createStyledScrollPane(currentBookingsTable), BorderLayout.CENTER);
        splitPane.setTopComponent(currentCard);

        JPanel newCard = FLCTheme.createCardPanel();
        newCard.setLayout(new BorderLayout(0, 8));
        newCard.add(
                FLCTheme.createSectionHeader("\uD83D\uDD0D", "Find a Replacement Lesson"),
                BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setOpaque(false);
        newWeekCombo = new JComboBox<>();
        for (int week = 1; week <= Math.max(bookingSystem.getWeekCount(), 1); week++) {
            newWeekCombo.addItem(week);
        }
        FLCTheme.styleComboBox(newWeekCombo);
        newDayCombo = new JComboBox<>(DayOfWeek.values());
        FLCTheme.styleComboBox(newDayCombo);
        newTimeCombo = new JComboBox<>();
        newTimeCombo.addItem("All");
        for (TimeSlot slot : TimeSlot.values()) {
            newTimeCombo.addItem(slot.getDisplayName());
        }
        FLCTheme.styleComboBox(newTimeCombo);

        filterPanel.add(FLCTheme.createFieldLabel("Week:"));
        filterPanel.add(newWeekCombo);
        filterPanel.add(FLCTheme.createFieldLabel("Day:"));
        filterPanel.add(newDayCombo);
        filterPanel.add(FLCTheme.createFieldLabel("Time:"));
        filterPanel.add(newTimeCombo);
        javax.swing.JButton findBtn = FLCTheme.createPrimaryButton("Find Lessons");
        findBtn.addActionListener(e -> findMatchingLessons());
        filterPanel.add(findBtn);

        JPanel newContent = new JPanel(new BorderLayout(0, 8));
        newContent.setOpaque(false);
        newContent.add(filterPanel, BorderLayout.NORTH);

        availableModel = new AvailableLessonTableModel();
        availableLessonsTable = new JTable(availableModel);
        availableLessonsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        newContent.add(FLCTheme.createStyledScrollPane(availableLessonsTable), BorderLayout.CENTER);
        newCard.add(newContent, BorderLayout.CENTER);
        splitPane.setBottomComponent(newCard);

        content.add(splitPane, BorderLayout.CENTER);

        javax.swing.JButton confirmBtn = FLCTheme.createPrimaryButton("\u2714  Confirm Change");
        confirmBtn.addActionListener(e -> confirmChange());
        content.add(UIHelper.createActionPanel(confirmBtn), BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);
    }

    private void loadCurrentBookings() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null) {
            return;
        }

        List<Booking> pendingBookings = member.getPendingBookings().stream()
                .sorted(Comparator.comparing((Booking booking) -> booking.getLesson().getWeekNumber())
                        .thenComparing(booking -> booking.getLesson().getDay().ordinal())
                        .thenComparing(booking -> booking.getLesson().getTimeSlot().ordinal()))
                .collect(Collectors.toList());
        currentBookingsModel.setBookings(pendingBookings);
    }

    private void findMatchingLessons() {
        int week = (Integer) newWeekCombo.getSelectedItem();
        DayOfWeek day = (DayOfWeek) newDayCombo.getSelectedItem();
        String timeFilter = (String) newTimeCombo.getSelectedItem();

        List<Lesson> lessons = bookingSystem.getTimetable().getLessonsByWeekAndDay(week, day);
        if (!"All".equals(timeFilter)) {
            lessons = lessons.stream()
                    .filter(lesson -> lesson.getTimeSlot().getDisplayName().equals(timeFilter))
                    .collect(Collectors.toList());
        }

        availableModel.setLessons(lessons.stream()
                .sorted(Comparator.comparingInt(Lesson::getWeekNumber)
                        .thenComparing(lesson -> lesson.getDay().ordinal())
                        .thenComparing(lesson -> lesson.getTimeSlot().ordinal()))
                .collect(Collectors.toList()));
    }

    private void confirmChange() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null) {
            JOptionPane.showMessageDialog(this, "Please select a member.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int currentRow = currentBookingsTable.getSelectedRow();
        if (currentRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select the booking you want to change.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int newRow = availableLessonsTable.getSelectedRow();
        if (newRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a replacement lesson.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Booking booking = currentBookingsModel.getBookingAt(currentBookingsTable.convertRowIndexToModel(currentRow));
        Lesson oldLesson = booking.getLesson();
        Lesson newLesson = availableModel.getLessonAt(availableLessonsTable.convertRowIndexToModel(newRow));

        if (oldLesson.getLessonId().equals(newLesson.getLessonId())) {
            JOptionPane.showMessageDialog(this,
                    "Cannot change to the same lesson. Please choose a different lesson.",
                    "Invalid Change", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String bookingId = booking.getBookingId();
            bookingSystem.changeBooking(member, oldLesson, newLesson);
            JOptionPane.showMessageDialog(this,
                    String.format("Booking %s changed successfully.\nNew lesson: %s (%s - %s %s, Week %d)",
                            bookingId,
                            newLesson.getLessonId(),
                            newLesson.getExerciseType().getDisplayName(),
                            newLesson.getDay().getDisplayName(),
                            newLesson.getTimeSlot().getDisplayName(),
                            newLesson.getWeekNumber()),
                    "Change Successful", JOptionPane.INFORMATION_MESSAGE);
            loadCurrentBookings();
            findMatchingLessons();
        } catch (LessonFullException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Lesson Full", JOptionPane.ERROR_MESSAGE);
        } catch (TimeConflictException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Time Conflict", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class BookingTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = { "Booking ID", "Lesson ID", "Exercise", "Day", "Time", "Week",
                "Status" };
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

    private static class AvailableLessonTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = { "Lesson ID", "Exercise", "Day", "Time", "Week", "Spaces Left",
                "Price", "Status" };
        private final DecimalFormat df = new DecimalFormat("0.00");
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
            Lesson lesson = lessons.get(row);
            switch (col) {
                case 0:
                    return lesson.getLessonId();
                case 1:
                    return lesson.getExerciseType().getDisplayName();
                case 2:
                    return lesson.getDay().getDisplayName();
                case 3:
                    return lesson.getTimeSlot().getDisplayName();
                case 4:
                    return lesson.getWeekNumber();
                case 5:
                    return lesson.getAvailableSpaces();
                case 6:
                    return "\u00A3" + df.format(lesson.getExerciseType().getPrice());
                case 7:
                    return lesson.isAvailable() ? "Available" : "Full";
                default:
                    return "";
            }
        }

        public void setLessons(List<Lesson> lessons) {
            this.lessons = new ArrayList<>(lessons);
            fireTableDataChanged();
        }

        public Lesson getLessonAt(int row) {
            return lessons.get(row);
        }
    }
}
