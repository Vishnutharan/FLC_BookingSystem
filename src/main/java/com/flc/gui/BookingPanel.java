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

    /**
     * Constructs the BookingPanel.
     *
     * @param bookingSystem the booking system instance
     */
    public BookingPanel(BookingSystem bookingSystem) {
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
        JLabel title = new JLabel("Book a Lesson", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        // Controls panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Booking Options"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Member selector
        gbc.gridx = 0;
        gbc.gridy = 0;
        controlPanel.add(new JLabel("Member:"), gbc);
        gbc.gridx = 1;
        memberCombo = new JComboBox<>();
        refreshMemberCombo();
        controlPanel.add(memberCombo, gbc);

        // Week selector
        gbc.gridx = 2;
        gbc.gridy = 0;
        controlPanel.add(new JLabel("Week:"), gbc);
        gbc.gridx = 3;
        weekCombo = new JComboBox<>();
        for (int i = 1; i <= 8; i++)
            weekCombo.addItem(i);
        controlPanel.add(weekCombo, gbc);

        // Day selector
        gbc.gridx = 0;
        gbc.gridy = 1;
        controlPanel.add(new JLabel("Day:"), gbc);
        gbc.gridx = 1;
        dayCombo = new JComboBox<>(DayOfWeek.values());
        controlPanel.add(dayCombo, gbc);

        // Time slot filter (optional)
        gbc.gridx = 2;
        gbc.gridy = 1;
        controlPanel.add(new JLabel("Time Slot:"), gbc);
        gbc.gridx = 3;
        timeSlotCombo = new JComboBox<>();
        timeSlotCombo.addItem("All");
        for (TimeSlot ts : TimeSlot.values()) {
            timeSlotCombo.addItem(ts.getDisplayName());
        }
        controlPanel.add(timeSlotCombo, gbc);

        // Search button
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        JButton searchBtn = new JButton("Search Available Lessons");
        searchBtn.addActionListener(e -> searchAvailableLessons());
        controlPanel.add(searchBtn, gbc);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(controlPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Results table
        tableModel = new LessonTableModel();
        lessonTable = new JTable(tableModel);
        lessonTable.setAutoCreateRowSorter(true);
        lessonTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lessonTable.setRowHeight(25);
        lessonTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        add(new JScrollPane(lessonTable), BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        JButton bookBtn = new JButton("Book Selected Lesson");
        bookBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        bookBtn.addActionListener(e -> bookSelectedLesson());
        bottomPanel.add(bookBtn, BorderLayout.EAST);

        statusLabel = new JLabel("Select a member and search for available lessons.");
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Refreshes the member combo box.
     */
    private void refreshMemberCombo() {
        memberCombo.removeAllItems();
        for (Member m : bookingSystem.getMembers()) {
            memberCombo.addItem(m);
        }
    }

    /**
     * Searches for available lessons based on filters.
     */
    private void searchAvailableLessons() {
        int week = (Integer) weekCombo.getSelectedItem();
        DayOfWeek day = (DayOfWeek) dayCombo.getSelectedItem();
        String timeFilter = (String) timeSlotCombo.getSelectedItem();

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

        tableModel.setLessons(lessons);
        statusLabel.setText(String.format("Found %d available lesson(s).", lessons.size()));
    }

    /**
     * Books the selected lesson for the selected member.
     */
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
                    return "£" + df.format(l.getExerciseType().getPrice());
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
