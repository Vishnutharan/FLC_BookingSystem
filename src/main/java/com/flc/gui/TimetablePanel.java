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
 * or by exercise type, displaying results in a styled JTable with stats cards.
 *
 * @author FLC Development Team
 */
public class TimetablePanel extends JPanel {

    private final BookingSystem bookingSystem;
    private JRadioButton searchByDayRadio;
    private JRadioButton searchByExerciseRadio;
    private JComboBox<DayOfWeek> dayCombo;
    private JComboBox<Object> weekCombo;
    private JComboBox<ExerciseType> exerciseCombo;
    private JPanel daySearchPanel;
    private JPanel exerciseSearchPanel;
    private JTable resultTable;
    private TimetableTableModel tableModel;
    private JLabel statusLabel;
    private JPanel statsRow;

    /**
     * Constructs the TimetablePanel.
     *
     * @param bookingSystem the booking system instance
     */
    public TimetablePanel(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
        setLayout(new BorderLayout());
        setBackground(FLCTheme.CONTENT_BG);
        setOpaque(false);
        buildUI();
    }

    /**
     * Builds the complete user interface for the timetable panel.
     */
    private void buildUI() {
        JPanel content = UIHelper.createContentPanel();

        // ─── Stats Row ───────────────────────────────────────────
        statsRow = buildStatsRow();
        content.add(statsRow, BorderLayout.NORTH);

        // ─── Center: Search Options + Table ──────────────────────
        JPanel centerCard = FLCTheme.createCardPanel();
        centerCard.setLayout(new BorderLayout(0, 12));

        // Search controls
        JPanel searchSection = new JPanel(new BorderLayout(0, 8));
        searchSection.setOpaque(false);

        // Radio buttons
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        radioPanel.setOpaque(false);
        searchByDayRadio = new JRadioButton("Search by Day", true);
        searchByExerciseRadio = new JRadioButton("Search by Exercise");
        searchByDayRadio.setFont(FLCTheme.FONT_BODY_BOLD);
        searchByExerciseRadio.setFont(FLCTheme.FONT_BODY_BOLD);
        searchByDayRadio.setForeground(FLCTheme.TEXT_PRIMARY);
        searchByExerciseRadio.setForeground(FLCTheme.TEXT_PRIMARY);
        searchByDayRadio.setOpaque(false);
        searchByExerciseRadio.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        group.add(searchByDayRadio);
        group.add(searchByExerciseRadio);
        radioPanel.add(searchByDayRadio);
        radioPanel.add(searchByExerciseRadio);
        searchSection.add(radioPanel, BorderLayout.NORTH);

        // Search fields
        JPanel searchFields = new JPanel(new CardLayout());
        searchFields.setOpaque(false);

        // Day search
        daySearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        daySearchPanel.setOpaque(false);
        dayCombo = new JComboBox<>(DayOfWeek.values());
        FLCTheme.styleComboBox(dayCombo);
        weekCombo = new JComboBox<>();
        weekCombo.addItem("All Weeks");
        for (int i = 1; i <= Math.max(bookingSystem.getWeekCount(), 1); i++)
            weekCombo.addItem(i);
        FLCTheme.styleComboBox(weekCombo);
        JButton searchByDayBtn = FLCTheme.createPrimaryButton("Search");
        searchByDayBtn.addActionListener(e -> searchByDay());
        daySearchPanel.add(FLCTheme.createFieldLabel("Day:"));
        daySearchPanel.add(dayCombo);
        daySearchPanel.add(FLCTheme.createFieldLabel("  Week:"));
        daySearchPanel.add(weekCombo);
        daySearchPanel.add(searchByDayBtn);

        // Exercise search
        exerciseSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        exerciseSearchPanel.setOpaque(false);
        exerciseCombo = new JComboBox<>(ExerciseType.values());
        FLCTheme.styleComboBox(exerciseCombo);
        JButton searchByExBtn = FLCTheme.createPrimaryButton("Search");
        searchByExBtn.addActionListener(e -> searchByExercise());
        exerciseSearchPanel.add(FLCTheme.createFieldLabel("Exercise:"));
        exerciseSearchPanel.add(exerciseCombo);
        exerciseSearchPanel.add(searchByExBtn);

        searchFields.add(daySearchPanel, "DAY");
        searchFields.add(exerciseSearchPanel, "EXERCISE");
        searchSection.add(searchFields, BorderLayout.CENTER);

        // Radio listeners
        CardLayout cl = (CardLayout) searchFields.getLayout();
        searchByDayRadio.addActionListener(e -> cl.show(searchFields, "DAY"));
        searchByExerciseRadio.addActionListener(e -> cl.show(searchFields, "EXERCISE"));

        centerCard.add(searchSection, BorderLayout.NORTH);

        // Table
        tableModel = new TimetableTableModel();
        resultTable = new JTable(tableModel);
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = FLCTheme.createStyledScrollPane(resultTable);
        centerCard.add(scrollPane, BorderLayout.CENTER);

        // Status
        statusLabel = FLCTheme.createStatusLabel("Use the search options above to view the timetable.");
        centerCard.add(statusLabel, BorderLayout.SOUTH);

        content.add(centerCard, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        // Show all by default
        showAllLessons();
    }

    /**
     * Builds the stats row with summary cards.
     */
    private JPanel buildStatsRow() {
        List<Lesson> all = bookingSystem.getTimetable().getAllLessons();
        int totalLessons = all.size();
        int totalBooked = all.stream().mapToInt(l -> l.getBookedMembers().size()).sum();
        double avgRating = all.stream().filter(l -> l.getAverageRating() > 0)
                .mapToDouble(Lesson::getAverageRating).average().orElse(0.0);
        int totalMembers = bookingSystem.getMembers().size();

        return UIHelper.createStatsRow(
                FLCTheme.createStatsCard("\uD83D\uDCDA", String.valueOf(totalLessons), "Total Lessons",
                        FLCTheme.STATS_BLUE_BG, FLCTheme.PRIMARY),
                FLCTheme.createStatsCard("\uD83D\uDCCB", String.valueOf(totalBooked), "Total Bookings",
                        FLCTheme.STATS_GREEN_BG, FLCTheme.SUCCESS),
                FLCTheme.createStatsCard("\u2B50", avgRating > 0 ? String.format("%.1f", avgRating) : "N/A",
                        "Avg Rating",
                        FLCTheme.STATS_AMBER_BG, FLCTheme.WARNING),
                FLCTheme.createStatsCard("\uD83D\uDC65", String.valueOf(totalMembers), "Members",
                        FLCTheme.STATS_PURPLE_BG, FLCTheme.PURPLE));
    }

    private void searchByDay() {
        DayOfWeek day = (DayOfWeek) dayCombo.getSelectedItem();
        Object selectedWeek = weekCombo.getSelectedItem();
        List<Lesson> results;

        if (selectedWeek instanceof Integer) {
            int week = (Integer) selectedWeek;
            results = bookingSystem.getTimetable().getLessonsByWeekAndDay(week, day);
            statusLabel.setText(String.format("\u2705 Found %d lesson(s) for %s, Week %d.",
                    results.size(), day.getDisplayName(), week));
        } else {
            results = bookingSystem.searchTimetableByDay(day);
            statusLabel.setText(String.format("\u2705 Found %d lesson(s) for %s across all weeks.",
                    results.size(), day.getDisplayName()));
        }

        tableModel.setLessons(results);
    }

    private void searchByExercise() {
        ExerciseType type = (ExerciseType) exerciseCombo.getSelectedItem();
        List<Lesson> results = bookingSystem.searchTimetableByExercise(type);
        tableModel.setLessons(results);
        statusLabel.setText(String.format("\u2705 Found %d lesson(s) for '%s'.",
                results.size(), type.getDisplayName()));
    }

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
                "Week", "Day", "Time Slot", "Exercise", "Price (\u00A3)", "Booked", "Available", "Avg Rating"
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
                    return "\u00A3" + df.format(lesson.getExerciseType().getPrice());
                case 5:
                    return lesson.getBookedMembers().size();
                case 6:
                    return lesson.getAvailableSpaces();
                case 7:
                    double avg = lesson.getAverageRating();
                    return avg == 0.0 ? "N/A" : df.format(avg) + " " + UIHelper.getStarRatingDouble(avg);
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
            if (columnIndex == 0 || columnIndex == 5 || columnIndex == 6)
                return Integer.class;
            return String.class;
        }

        public void setLessons(List<Lesson> lessons) {
            this.lessons = lessons;
            fireTableDataChanged();
        }
    }
}
