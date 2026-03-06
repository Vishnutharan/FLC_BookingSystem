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
 * Panel for booking a new lesson. Allows selecting a member, filtering
 * available lessons by week/day/time, and booking with full validation.
 *
 * @author FLC Development Team
 */
public class BookingPanel extends JPanel {

    private final BookingSystem bookingSystem;
    private JComboBox<Member> memberCombo;
    private JComboBox<Integer> weekCombo;
    private JComboBox<DayOfWeek> dayCombo;
    private JComboBox<String> timeSlotCombo;
    private JTable lessonTable;
    private LessonTableModel tableModel;
    private JLabel statusLabel;

    public BookingPanel(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
        setLayout(new BorderLayout());
        setBackground(FLCTheme.CONTENT_BG);
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        JPanel content = UIHelper.createContentPanel();

        // ─── Controls Card ───────────────────────────────────────
        JPanel controlCard = FLCTheme.createCardPanel();
        controlCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Member
        gbc.gridx = 0;
        gbc.gridy = 0;
        controlCard.add(FLCTheme.createFieldLabel("Member:"), gbc);
        gbc.gridx = 1;
        memberCombo = new JComboBox<>();
        refreshMemberCombo();
        FLCTheme.styleComboBox(memberCombo);
        controlCard.add(memberCombo, gbc);

        // Week
        gbc.gridx = 2;
        gbc.gridy = 0;
        controlCard.add(FLCTheme.createFieldLabel("Week:"), gbc);
        gbc.gridx = 3;
        weekCombo = new JComboBox<>();
        for (int i = 1; i <= 8; i++)
            weekCombo.addItem(i);
        FLCTheme.styleComboBox(weekCombo);
        controlCard.add(weekCombo, gbc);

        // Day
        gbc.gridx = 0;
        gbc.gridy = 1;
        controlCard.add(FLCTheme.createFieldLabel("Day:"), gbc);
        gbc.gridx = 1;
        dayCombo = new JComboBox<>(DayOfWeek.values());
        FLCTheme.styleComboBox(dayCombo);
        controlCard.add(dayCombo, gbc);

        // Time Slot
        gbc.gridx = 2;
        gbc.gridy = 1;
        controlCard.add(FLCTheme.createFieldLabel("Time Slot:"), gbc);
        gbc.gridx = 3;
        timeSlotCombo = new JComboBox<>();
        timeSlotCombo.addItem("All");
        for (TimeSlot ts : TimeSlot.values())
            timeSlotCombo.addItem(ts.getDisplayName());
        FLCTheme.styleComboBox(timeSlotCombo);
        controlCard.add(timeSlotCombo, gbc);

        // Search button
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        JButton searchBtn = FLCTheme.createPrimaryButton("\uD83D\uDD0D  Search Available");
        searchBtn.addActionListener(e -> searchAvailableLessons());
        controlCard.add(searchBtn, gbc);

        content.add(controlCard, BorderLayout.NORTH);

        // ─── Results Card ────────────────────────────────────────
        JPanel tableCard = FLCTheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        tableModel = new LessonTableModel();
        lessonTable = new JTable(tableModel);
        lessonTable.setAutoCreateRowSorter(true);
        lessonTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = FLCTheme.createStyledScrollPane(lessonTable);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom bar
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setOpaque(false);

        statusLabel = FLCTheme.createStatusLabel("Select a member and search for available lessons.");
        bottomBar.add(statusLabel, BorderLayout.CENTER);

        JButton bookBtn = FLCTheme.createSuccessButton("\u2714  Book Selected Lesson");
        bookBtn.addActionListener(e -> bookSelectedLesson());
        bottomBar.add(bookBtn, BorderLayout.EAST);

        tableCard.add(bottomBar, BorderLayout.SOUTH);
        content.add(tableCard, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    private void refreshMemberCombo() {
        memberCombo.removeAllItems();
        for (Member m : bookingSystem.getMembers())
            memberCombo.addItem(m);
    }

    private void searchAvailableLessons() {
        int week = (Integer) weekCombo.getSelectedItem();
        DayOfWeek day = (DayOfWeek) dayCombo.getSelectedItem();
        String timeFilter = (String) timeSlotCombo.getSelectedItem();

        List<Lesson> lessons = bookingSystem.getTimetable()
                .getLessonsByWeekAndDay(week, day).stream()
                .filter(Lesson::isAvailable).collect(Collectors.toList());

        if (!"All".equals(timeFilter)) {
            lessons = lessons.stream()
                    .filter(l -> l.getTimeSlot().getDisplayName().equals(timeFilter))
                    .collect(Collectors.toList());
        }

        tableModel.setLessons(lessons);
        statusLabel.setText(String.format("\u2705 Found %d available lesson(s).", lessons.size()));
    }

    private void bookSelectedLesson() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null) {
            JOptionPane.showMessageDialog(this, "Please select a member.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = lessonTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a lesson from the table.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = lessonTable.convertRowIndexToModel(selectedRow);
        Lesson lesson = tableModel.getLessonAt(modelRow);

        try {
            bookingSystem.bookLesson(member, lesson);
            JOptionPane.showMessageDialog(this,
                    String.format("Successfully booked %s into %s (%s - %s %s, Week %d)!",
                            member.getName(), lesson.getLessonId(),
                            lesson.getExerciseType().getDisplayName(),
                            lesson.getDay().getDisplayName(),
                            lesson.getTimeSlot().getDisplayName(),
                            lesson.getWeekNumber()),
                    "Booking Successful", JOptionPane.INFORMATION_MESSAGE);
            searchAvailableLessons(); // refresh
        } catch (LessonFullException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Lesson Full", JOptionPane.ERROR_MESSAGE);
        } catch (TimeConflictException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Time Conflict", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Table model for available lessons.
     */
    static class LessonTableModel extends AbstractTableModel {

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
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Lesson l = lessons.get(rowIndex);
            switch (columnIndex) {
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

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 4 || columnIndex == 5)
                return Integer.class;
            return String.class;
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
