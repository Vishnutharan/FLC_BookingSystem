package com.flc.gui;

import com.flc.exception.LessonFullException;
import com.flc.exception.TimeConflictException;
import com.flc.model.*;
import com.flc.service.BookingSystem;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel for changing an existing booking. Allows selecting a member,
 * viewing their current bookings, and changing to a different available lesson.
 *
 * @author FLC Development Team
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

    /**
     * Constructs the ChangeBookingPanel.
     *
     * @param bookingSystem the booking system instance
     */
    public ChangeBookingPanel(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    /**
     * Builds the UI components.
     */
    private void buildUI() {
        // Title
        JLabel title = new JLabel("Change a Booking", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        // Top control panel
        JPanel topControl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topControl.setBorder(BorderFactory.createTitledBorder("Select Member"));
        memberCombo = new JComboBox<>();
        for (Member m : bookingSystem.getMembers())
            memberCombo.addItem(m);
        topControl.add(new JLabel("Member:"));
        topControl.add(memberCombo);
        JButton loadBtn = new JButton("Load My Bookings");
        loadBtn.addActionListener(e -> loadCurrentBookings());
        topControl.add(loadBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(topControl, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Split pane - current bookings (top) and available lessons (bottom)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.4);

        // Current bookings
        JPanel currentPanel = new JPanel(new BorderLayout());
        currentPanel.setBorder(BorderFactory.createTitledBorder("Current Bookings (select the one to change FROM)"));
        currentBookingsModel = new BookingTableModel();
        currentBookingsTable = new JTable(currentBookingsModel);
        currentBookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        currentBookingsTable.setRowHeight(25);
        currentPanel.add(new JScrollPane(currentBookingsTable), BorderLayout.CENTER);
        splitPane.setTopComponent(currentPanel);

        // New lesson finder
        JPanel newPanel = new JPanel(new BorderLayout(5, 5));
        newPanel.setBorder(BorderFactory.createTitledBorder("Find New Lesson (select the one to change TO)"));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        newWeekCombo = new JComboBox<>();
        for (int i = 1; i <= 8; i++)
            newWeekCombo.addItem(i);
        newDayCombo = new JComboBox<>(DayOfWeek.values());
        newTimeCombo = new JComboBox<>();
        newTimeCombo.addItem("All");
        for (TimeSlot ts : TimeSlot.values())
            newTimeCombo.addItem(ts.getDisplayName());

        filterPanel.add(new JLabel("Week:"));
        filterPanel.add(newWeekCombo);
        filterPanel.add(new JLabel("Day:"));
        filterPanel.add(newDayCombo);
        filterPanel.add(new JLabel("Time:"));
        filterPanel.add(newTimeCombo);
        JButton findBtn = new JButton("Find Available Lessons");
        findBtn.addActionListener(e -> findAvailableLessons());
        filterPanel.add(findBtn);
        newPanel.add(filterPanel, BorderLayout.NORTH);

        availableModel = new AvailableLessonTableModel();
        availableLessonsTable = new JTable(availableModel);
        availableLessonsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availableLessonsTable.setRowHeight(25);
        newPanel.add(new JScrollPane(availableLessonsTable), BorderLayout.CENTER);
        splitPane.setBottomComponent(newPanel);

        add(splitPane, BorderLayout.CENTER);

        // Confirm button
        JButton confirmBtn = new JButton("Confirm Change");
        confirmBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        confirmBtn.addActionListener(e -> confirmChange());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(confirmBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Loads the selected member's current bookings.
     */
    private void loadCurrentBookings() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null)
            return;
        currentBookingsModel.setLessons(new ArrayList<>(member.getBookedLessons()));
    }

    /**
     * Finds available lessons based on selected filters.
     */
    private void findAvailableLessons() {
        int week = (Integer) newWeekCombo.getSelectedItem();
        DayOfWeek day = (DayOfWeek) newDayCombo.getSelectedItem();
        String timeFilter = (String) newTimeCombo.getSelectedItem();

        List<Lesson> lessons = bookingSystem.getTimetable()
                .getLessonsByWeekAndDay(week, day)
                .stream()
                .filter(Lesson::isAvailable)
                .collect(Collectors.toList());

        if (!"All".equals(timeFilter)) {
            lessons = lessons.stream()
                    .filter(l -> l.getTimeSlot().getDisplayName().equals(timeFilter))
                    .collect(Collectors.toList());
        }
        availableModel.setLessons(lessons);
    }

    /**
     * Confirms the booking change.
     */
    private void confirmChange() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null) {
            JOptionPane.showMessageDialog(this, "Please select a member.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int currentRow = currentBookingsTable.getSelectedRow();
        if (currentRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a current booking to change FROM.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int newRow = availableLessonsTable.getSelectedRow();
        if (newRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a new lesson to change TO.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelCurrentRow = currentBookingsTable.convertRowIndexToModel(currentRow);
        int modelNewRow = availableLessonsTable.convertRowIndexToModel(newRow);

        Lesson oldLesson = currentBookingsModel.getLessonAt(modelCurrentRow);
        Lesson newLesson = availableModel.getLessonAt(modelNewRow);

        if (oldLesson.getLessonId().equals(newLesson.getLessonId())) {
            JOptionPane.showMessageDialog(this,
                    "Cannot change to the same lesson. Please select a different lesson.",
                    "Invalid Change", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            bookingSystem.changeBooking(member, oldLesson, newLesson);
            JOptionPane.showMessageDialog(this,
                    String.format("Booking changed successfully!\nFrom: %s\nTo: %s",
                            oldLesson.toString(), newLesson.toString()),
                    "Change Successful", JOptionPane.INFORMATION_MESSAGE);
            loadCurrentBookings();
            findAvailableLessons();
        } catch (LessonFullException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Lesson Full", JOptionPane.ERROR_MESSAGE);
        } catch (TimeConflictException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Time Conflict", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Table model for member's current bookings.
     */
    private static class BookingTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
                "Lesson ID", "Exercise", "Day", "Time", "Week", "Booked"
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

    /**
     * Table model for available lessons.
     */
    private static class AvailableLessonTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
                "Lesson ID", "Exercise", "Day", "Time", "Week", "Spaces Left", "Price"
        };
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
                    return l.getAvailableSpaces();
                case 6:
                    return "£" + df.format(l.getExerciseType().getPrice());
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
