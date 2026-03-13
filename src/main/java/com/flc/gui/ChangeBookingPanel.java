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

    public ChangeBookingPanel(BookingSystem bookingSystem) {
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
        loadBtn.addActionListener(e -> loadCurrentBookings());
        topCard.add(loadBtn);
        content.add(topCard, BorderLayout.NORTH);

        // ─── Center: Split ───────────────────────────────────────
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);

        // Current bookings card
        JPanel currentCard = FLCTheme.createCardPanel();
        currentCard.setLayout(new BorderLayout(0, 8));
        currentCard.add(
                FLCTheme.createSectionHeader("\uD83D\uDCCB", "Current Bookings  \u2014  Select the one to change FROM"),
                BorderLayout.NORTH);
        currentBookingsModel = new BookingTableModel();
        currentBookingsTable = new JTable(currentBookingsModel);
        currentBookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        currentCard.add(FLCTheme.createStyledScrollPane(currentBookingsTable), BorderLayout.CENTER);
        splitPane.setTopComponent(currentCard);

        // New lesson finder card
        JPanel newCard = FLCTheme.createCardPanel();
        newCard.setLayout(new BorderLayout(0, 8));
        newCard.add(
                FLCTheme.createSectionHeader("\uD83D\uDD0D", "Find New Lesson  \u2014  Select the one to change TO"),
                BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setOpaque(false);
        newWeekCombo = new JComboBox<>();
        for (int i = 1; i <= Math.max(bookingSystem.getWeekCount(), 1); i++)
            newWeekCombo.addItem(i);
        FLCTheme.styleComboBox(newWeekCombo);
        newDayCombo = new JComboBox<>(DayOfWeek.values());
        FLCTheme.styleComboBox(newDayCombo);
        newTimeCombo = new JComboBox<>();
        newTimeCombo.addItem("All");
        for (TimeSlot ts : TimeSlot.values())
            newTimeCombo.addItem(ts.getDisplayName());
        FLCTheme.styleComboBox(newTimeCombo);

        filterPanel.add(FLCTheme.createFieldLabel("Week:"));
        filterPanel.add(newWeekCombo);
        filterPanel.add(FLCTheme.createFieldLabel("Day:"));
        filterPanel.add(newDayCombo);
        filterPanel.add(FLCTheme.createFieldLabel("Time:"));
        filterPanel.add(newTimeCombo);
        JButton findBtn = FLCTheme.createPrimaryButton("Find Available");
        findBtn.addActionListener(e -> findAvailableLessons());
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

        // ─── Bottom: Confirm Button ──────────────────────────────
        JButton confirmBtn = FLCTheme.createPrimaryButton("\u2714  Confirm Change");
        content.add(UIHelper.createActionPanel(confirmBtn), BorderLayout.SOUTH);
        confirmBtn.addActionListener(e -> confirmChange());

        add(content, BorderLayout.CENTER);
    }

    private void loadCurrentBookings() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null)
            return;
        currentBookingsModel.setLessons(member.getBookedLessons().stream()
                .filter(lesson -> member.getBookingStatus(lesson) == BookingStatus.BOOKED)
                .collect(Collectors.toList()));
    }

    private void findAvailableLessons() {
        int week = (Integer) newWeekCombo.getSelectedItem();
        DayOfWeek day = (DayOfWeek) newDayCombo.getSelectedItem();
        String timeFilter = (String) newTimeCombo.getSelectedItem();

        List<Lesson> lessons = bookingSystem.getTimetable()
                .getLessonsByWeekAndDay(week, day).stream()
                .filter(Lesson::isAvailable).collect(Collectors.toList());

        if (!"All".equals(timeFilter)) {
            lessons = lessons.stream()
                    .filter(l -> l.getTimeSlot().getDisplayName().equals(timeFilter))
                    .collect(Collectors.toList());
        }
        availableModel.setLessons(lessons);
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

    private static class BookingTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = { "Lesson ID", "Exercise", "Day", "Time", "Week", "Booked" };
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

    private static class AvailableLessonTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = { "Lesson ID", "Exercise", "Day", "Time", "Week", "Spaces Left",
                "Price" };
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
                    return "\u00A3" + df.format(l.getExerciseType().getPrice());
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
