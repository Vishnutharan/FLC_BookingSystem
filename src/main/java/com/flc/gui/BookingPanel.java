package com.flc.gui;

import com.flc.exception.LessonFullException;
import com.flc.exception.TimeConflictException;
import com.flc.model.DayOfWeek;
import com.flc.model.ExerciseType;
import com.flc.model.Lesson;
import com.flc.model.Member;
import com.flc.service.BookingSystem;
import com.flc.service.Timetable;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

/**
 * Panel for booking a lesson after searching the timetable by day or exercise
 * type, matching the coursework flow more closely.
 */
public class BookingPanel extends JPanel {

    private final BookingSystem bookingSystem;
    private JComboBox<Member> memberCombo;
    private JRadioButton searchByDayRadio;
    private JRadioButton searchByExerciseRadio;
    private JComboBox<Object> weekCombo;
    private JComboBox<DayOfWeek> dayCombo;
    private JComboBox<ExerciseType> exerciseCombo;
    private JTable lessonTable;
    private LessonTableModel tableModel;
    private javax.swing.JLabel statusLabel;

    public BookingPanel(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
        setLayout(new BorderLayout());
        setBackground(FLCTheme.CONTENT_BG);
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        JPanel content = UIHelper.createContentPanel();

        JPanel topCard = FLCTheme.createCardPanel();
        topCard.setLayout(new BorderLayout(0, 10));

        JPanel memberRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        memberRow.setOpaque(false);
        memberCombo = new JComboBox<>();
        refreshMemberCombo();
        FLCTheme.styleComboBox(memberCombo);
        memberRow.add(FLCTheme.createFieldLabel("Member:"));
        memberRow.add(memberCombo);
        topCard.add(memberRow, BorderLayout.NORTH);

        JPanel searchSection = new JPanel(new BorderLayout(0, 8));
        searchSection.setOpaque(false);

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

        JPanel searchFields = new JPanel(new CardLayout());
        searchFields.setOpaque(false);

        JPanel dayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        dayPanel.setOpaque(false);
        weekCombo = new JComboBox<>();
        weekCombo.addItem("All Weeks");
        for (int week = 1; week <= Math.max(bookingSystem.getWeekCount(), 1); week++) {
            weekCombo.addItem(week);
        }
        FLCTheme.styleComboBox(weekCombo);
        dayCombo = new JComboBox<>(DayOfWeek.values());
        FLCTheme.styleComboBox(dayCombo);
        dayPanel.add(FLCTheme.createFieldLabel("Day:"));
        dayPanel.add(dayCombo);
        dayPanel.add(FLCTheme.createFieldLabel("Week:"));
        dayPanel.add(weekCombo);
        javax.swing.JButton daySearchBtn = FLCTheme.createPrimaryButton("Find Lessons");
        daySearchBtn.addActionListener(e -> searchByDay());
        dayPanel.add(daySearchBtn);

        JPanel exercisePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        exercisePanel.setOpaque(false);
        exerciseCombo = new JComboBox<>(ExerciseType.values());
        FLCTheme.styleComboBox(exerciseCombo);
        exercisePanel.add(FLCTheme.createFieldLabel("Exercise:"));
        exercisePanel.add(exerciseCombo);
        javax.swing.JButton exerciseSearchBtn = FLCTheme.createPrimaryButton("Find Lessons");
        exerciseSearchBtn.addActionListener(e -> searchByExercise());
        exercisePanel.add(exerciseSearchBtn);

        searchFields.add(dayPanel, "DAY");
        searchFields.add(exercisePanel, "EXERCISE");
        searchSection.add(searchFields, BorderLayout.CENTER);

        CardLayout searchCards = (CardLayout) searchFields.getLayout();
        searchByDayRadio.addActionListener(e -> searchCards.show(searchFields, "DAY"));
        searchByExerciseRadio.addActionListener(e -> searchCards.show(searchFields, "EXERCISE"));

        topCard.add(searchSection, BorderLayout.CENTER);
        content.add(topCard, BorderLayout.NORTH);

        JPanel tableCard = FLCTheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        tableModel = new LessonTableModel();
        lessonTable = new JTable(tableModel);
        lessonTable.setAutoCreateRowSorter(true);
        lessonTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCard.add(FLCTheme.createStyledScrollPane(lessonTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setOpaque(false);
        statusLabel = FLCTheme.createStatusLabel(
                "Search by Saturday/Sunday or exercise type, then select a lesson to book.");
        bottomBar.add(statusLabel, BorderLayout.CENTER);

        javax.swing.JButton bookBtn = FLCTheme.createSuccessButton("\u2714  Book Selected Lesson");
        bookBtn.addActionListener(e -> bookSelectedLesson());
        bottomBar.add(bookBtn, BorderLayout.EAST);

        tableCard.add(bottomBar, BorderLayout.SOUTH);
        content.add(tableCard, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    private void refreshMemberCombo() {
        memberCombo.removeAllItems();
        for (Member member : bookingSystem.getMembers()) {
            memberCombo.addItem(member);
        }
    }

    private void searchByDay() {
        DayOfWeek day = (DayOfWeek) dayCombo.getSelectedItem();
        Object selectedWeek = weekCombo.getSelectedItem();
        Timetable timetable = bookingSystem.getTimetable();
        List<Lesson> lessons;

        if (selectedWeek instanceof Integer) {
            int week = (Integer) selectedWeek;
            lessons = timetable.getLessonsByWeekAndDay(week, day);
            statusLabel.setText(String.format("Showing %d lesson(s) for %s in Week %d.",
                    lessons.size(), day.getDisplayName(), week));
        } else {
            lessons = bookingSystem.searchTimetableByDay(day);
            statusLabel.setText(String.format("Showing %d %s lesson(s) across the full timetable.",
                    lessons.size(), day.getDisplayName()));
        }

        tableModel.setLessons(sortLessons(lessons));
    }

    private void searchByExercise() {
        ExerciseType type = (ExerciseType) exerciseCombo.getSelectedItem();
        List<Lesson> lessons = sortLessons(bookingSystem.searchTimetableByExercise(type));
        tableModel.setLessons(lessons);
        statusLabel.setText(String.format("Showing %d '%s' lesson(s) across the full timetable.",
                lessons.size(), type.getDisplayName()));
    }

    private List<Lesson> sortLessons(List<Lesson> lessons) {
        return lessons.stream()
                .sorted(Comparator.comparingInt(Lesson::getWeekNumber)
                        .thenComparing(lesson -> lesson.getDay().ordinal())
                        .thenComparing(lesson -> lesson.getTimeSlot().ordinal()))
                .collect(Collectors.toList());
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
                    String.format("Successfully booked %s into %s (%s - %s %s, Week %d).",
                            member.getName(),
                            lesson.getLessonId(),
                            lesson.getExerciseType().getDisplayName(),
                            lesson.getDay().getDisplayName(),
                            lesson.getTimeSlot().getDisplayName(),
                            lesson.getWeekNumber()),
                    "Booking Successful", JOptionPane.INFORMATION_MESSAGE);
            tableModel.fireTableRowsUpdated(modelRow, modelRow);
        } catch (LessonFullException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Lesson Full", JOptionPane.ERROR_MESSAGE);
        } catch (TimeConflictException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Time Conflict", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class LessonTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
                "Lesson ID", "Week", "Day", "Time", "Exercise", "Spaces Left", "Price", "Status"
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
                    return lesson.getLessonId();
                case 1:
                    return lesson.getWeekNumber();
                case 2:
                    return lesson.getDay().getDisplayName();
                case 3:
                    return lesson.getTimeSlot().getDisplayName();
                case 4:
                    return lesson.getExerciseType().getDisplayName();
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

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 1 || columnIndex == 5) {
                return Integer.class;
            }
            return String.class;
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
