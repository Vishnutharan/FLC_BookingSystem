package com.flc.gui;

import com.flc.model.*;
import com.flc.service.BookingSystem;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for viewing the timetable. Supports searching by day (with week filter)
 * or by exercise type, displaying results in a sortable JTable.
 *
 * @author FLC Development Team
 */
public class TimetablePanel extends JPanel {

    private final BookingSystem bookingSystem;
    private JRadioButton searchByDayRadio;
    private JRadioButton searchByExerciseRadio;
    private JComboBox<DayOfWeek> dayCombo;
    private JComboBox<Integer> weekCombo;
    private JComboBox<ExerciseType> exerciseCombo;
    private JPanel daySearchPanel;
    private JPanel exerciseSearchPanel;
    private JTable resultTable;
    private TimetableTableModel tableModel;
    private JLabel statusLabel;

    /**
     * Constructs the TimetablePanel.
     *
     * @param bookingSystem the booking system instance
     */
    public TimetablePanel(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    /**
     * Builds the complete user interface for the timetable panel.
     */
    private void buildUI() {
        // Title
        JLabel title = new JLabel("Timetable Viewer", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        // Search controls
        JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Search Options"));

        // Radio buttons
        searchByDayRadio = new JRadioButton("Search by Day", true);
        searchByExerciseRadio = new JRadioButton("Search by Exercise");
        ButtonGroup group = new ButtonGroup();
        group.add(searchByDayRadio);
        group.add(searchByExerciseRadio);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(searchByDayRadio);
        radioPanel.add(searchByExerciseRadio);
        controlPanel.add(radioPanel, BorderLayout.NORTH);

        // Day search panel
        daySearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dayCombo = new JComboBox<>(DayOfWeek.values());
        weekCombo = new JComboBox<>();
        for (int i = 1; i <= 8; i++)
            weekCombo.addItem(i);
        JButton searchByDayBtn = new JButton("Search");
        searchByDayBtn.addActionListener(e -> searchByDay());
        daySearchPanel.add(new JLabel("Day:"));
        daySearchPanel.add(dayCombo);
        daySearchPanel.add(new JLabel("  Week:"));
        daySearchPanel.add(weekCombo);
        daySearchPanel.add(searchByDayBtn);

        // Exercise search panel
        exerciseSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        exerciseCombo = new JComboBox<>(ExerciseType.values());
        JButton searchByExBtn = new JButton("Search");
        searchByExBtn.addActionListener(e -> searchByExercise());
        exerciseSearchPanel.add(new JLabel("Exercise:"));
        exerciseSearchPanel.add(exerciseCombo);
        exerciseSearchPanel.add(searchByExBtn);
        exerciseSearchPanel.setVisible(false);

        JPanel searchCards = new JPanel(new CardLayout());
        searchCards.add(daySearchPanel, "DAY");
        searchCards.add(exerciseSearchPanel, "EXERCISE");
        controlPanel.add(searchCards, BorderLayout.CENTER);

        // Radio button listeners
        searchByDayRadio.addActionListener(e -> {
            daySearchPanel.setVisible(true);
            exerciseSearchPanel.setVisible(false);
            searchCards.revalidate();
            searchCards.repaint();
        });
        searchByExerciseRadio.addActionListener(e -> {
            daySearchPanel.setVisible(false);
            exerciseSearchPanel.setVisible(true);
            searchCards.revalidate();
            searchCards.repaint();
        });

        add(controlPanel, BorderLayout.NORTH);

        // Results panel
        JPanel resultsPanel = new JPanel(new BorderLayout(5, 5));

        // Create combined panel for title + controls at top
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(controlPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new TimetableTableModel();
        resultTable = new JTable(tableModel);
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultTable.setRowHeight(25);
        resultTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        JScrollPane scrollPane = new JScrollPane(resultTable);
        resultsPanel.add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel("Use the search options above to view the timetable.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        resultsPanel.add(statusLabel, BorderLayout.SOUTH);

        add(resultsPanel, BorderLayout.CENTER);

        // Show all by default
        showAllLessons();
    }

    /**
     * Searches lessons by the selected day and week.
     */
    private void searchByDay() {
        DayOfWeek day = (DayOfWeek) dayCombo.getSelectedItem();
        int week = (Integer) weekCombo.getSelectedItem();
        List<Lesson> results = bookingSystem.getTimetable().getLessonsByWeekAndDay(week, day);
        tableModel.setLessons(results);
        statusLabel.setText(String.format("Found %d lesson(s) for %s, Week %d.",
                results.size(), day.getDisplayName(), week));
    }

    /**
     * Searches lessons by the selected exercise type.
     */
    private void searchByExercise() {
        ExerciseType type = (ExerciseType) exerciseCombo.getSelectedItem();
        List<Lesson> results = bookingSystem.searchTimetableByExercise(type);
        tableModel.setLessons(results);
        statusLabel.setText(String.format("Found %d lesson(s) for '%s'.",
                results.size(), type.getDisplayName()));
    }

    /**
     * Shows all lessons in the timetable.
     */
    private void showAllLessons() {
        List<Lesson> all = bookingSystem.getTimetable().getAllLessons();
        tableModel.setLessons(all);
        statusLabel.setText(String.format("Showing all %d lessons.", all.size()));
    }

    /**
     * Table model for the timetable results.
     */
    private static class TimetableTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {
                "Week", "Day", "Time Slot", "Exercise", "Price (£)", "Booked", "Available", "Avg Rating"
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
            Lesson lesson = lessons.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return lesson.getWeekNumber();
                case 1:
                    return lesson.getDay().getDisplayName();
                case 2:
                    return lesson.getTimeSlot().getDisplayName();
                case 3:
                    return lesson.getExerciseType().getDisplayName();
                case 4:
                    return "£" + df.format(lesson.getExerciseType().getPrice());
                case 5:
                    return lesson.getBookedMembers().size();
                case 6:
                    return lesson.getAvailableSpaces();
                case 7:
                    double avg = lesson.getAverageRating();
                    return avg == 0.0 ? "N/A" : df.format(avg);
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
            if (columnIndex == 0 || columnIndex == 5 || columnIndex == 6) {
                return Integer.class;
            }
            return String.class;
        }

        public void setLessons(List<Lesson> lessons) {
            this.lessons = lessons;
            fireTableDataChanged();
        }
    }
}
