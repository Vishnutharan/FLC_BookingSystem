package com.flc.gui;

import com.flc.exception.InvalidRatingException;
import com.flc.model.*;
import com.flc.service.BookingSystem;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for writing a review for an attended lesson. Includes lesson selection,
 * rating (1–5), comment with character counter, and duplicate review checking.
 *
 * @author FLC Development Team
 */
public class ReviewPanel extends JPanel {

    private static final int MAX_COMMENT_LENGTH = 500;
    private final BookingSystem bookingSystem;
    private JComboBox<Member> memberCombo;
    private JTable lessonsTable;
    private LessonTableModel tableModel;
    private JComboBox<Integer> ratingCombo;
    private JTextArea commentArea;
    private JLabel charCountLabel;
    private JLabel ratingMeaningLabel;

    /**
     * Constructs the ReviewPanel.
     *
     * @param bookingSystem the booking system instance
     */
    public ReviewPanel(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    /**
     * Builds the UI components.
     */
    private void buildUI() {
        JLabel title = new JLabel("Write a Review", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        // Top: member selection
        JPanel topControl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topControl.setBorder(BorderFactory.createTitledBorder("Select Member"));
        memberCombo = new JComboBox<>();
        for (Member m : bookingSystem.getMembers())
            memberCombo.addItem(m);
        topControl.add(new JLabel("Member:"));
        topControl.add(memberCombo);
        JButton loadBtn = new JButton("Load Attended Lessons");
        loadBtn.addActionListener(e -> loadLessons());
        topControl.add(loadBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(topControl, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Center: split between lessons table and review form
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.45);

        // Lessons table
        JPanel lessonPanel = new JPanel(new BorderLayout());
        lessonPanel.setBorder(BorderFactory.createTitledBorder("Attended Lessons (select one to review)"));
        tableModel = new LessonTableModel();
        lessonsTable = new JTable(tableModel);
        lessonsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lessonsTable.setRowHeight(25);
        lessonPanel.add(new JScrollPane(lessonsTable), BorderLayout.CENTER);
        splitPane.setTopComponent(lessonPanel);

        // Review form
        JPanel reviewForm = new JPanel(new BorderLayout(5, 5));
        reviewForm.setBorder(BorderFactory.createTitledBorder("Review Details"));

        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ratingCombo = new JComboBox<>(new Integer[] { 1, 2, 3, 4, 5 });
        ratingCombo.setSelectedIndex(4); // default to 5
        ratingMeaningLabel = new JLabel("5 = Very Satisfied");
        ratingCombo.addActionListener(e -> updateRatingMeaning());
        ratingPanel.add(new JLabel("Rating:"));
        ratingPanel.add(ratingCombo);
        ratingPanel.add(ratingMeaningLabel);
        reviewForm.add(ratingPanel, BorderLayout.NORTH);

        // Comment area
        JPanel commentPanel = new JPanel(new BorderLayout(3, 3));
        commentPanel.add(new JLabel("Comment:"), BorderLayout.NORTH);
        commentArea = new JTextArea(5, 40);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        charCountLabel = new JLabel("0 / " + MAX_COMMENT_LENGTH + " characters");
        commentArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateCharCount();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateCharCount();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateCharCount();
            }
        });
        commentPanel.add(new JScrollPane(commentArea), BorderLayout.CENTER);
        commentPanel.add(charCountLabel, BorderLayout.SOUTH);
        reviewForm.add(commentPanel, BorderLayout.CENTER);

        splitPane.setBottomComponent(reviewForm);
        add(splitPane, BorderLayout.CENTER);

        // Submit button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitBtn = new JButton("Submit Review");
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitBtn.addActionListener(e -> submitReview());
        bottomPanel.add(submitBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Loads the selected member's booked lessons.
     */
    private void loadLessons() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null)
            return;
        tableModel.setLessons(new ArrayList<>(member.getBookedLessons()));
    }

    /**
     * Updates the rating meaning label.
     */
    private void updateRatingMeaning() {
        int rating = (Integer) ratingCombo.getSelectedItem();
        String[] meanings = { "", "1 = Very Dissatisfied", "2 = Dissatisfied",
                "3 = Neutral", "4 = Satisfied", "5 = Very Satisfied" };
        ratingMeaningLabel.setText(meanings[rating]);
    }

    /**
     * Updates the character count label.
     */
    private void updateCharCount() {
        int len = commentArea.getText().length();
        charCountLabel.setText(len + " / " + MAX_COMMENT_LENGTH + " characters");
        if (len > MAX_COMMENT_LENGTH) {
            charCountLabel.setForeground(Color.RED);
        } else {
            charCountLabel.setForeground(Color.BLACK);
        }
    }

    /**
     * Submits the review with full validation.
     */
    private void submitReview() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null) {
            JOptionPane.showMessageDialog(this, "Please select a member.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = lessonsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a lesson to review.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = lessonsTable.convertRowIndexToModel(selectedRow);
        Lesson lesson = tableModel.getLessonAt(modelRow);
        int rating = (Integer) ratingCombo.getSelectedItem();
        String comment = commentArea.getText().trim();

        if (comment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a comment.",
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
            bookingSystem.addReview(member, lesson, rating, comment);
            JOptionPane.showMessageDialog(this,
                    String.format("Review submitted successfully!\nLesson: %s\nRating: %d/5",
                            lesson.getLessonId(), rating),
                    "Review Submitted", JOptionPane.INFORMATION_MESSAGE);
            commentArea.setText("");
            ratingCombo.setSelectedIndex(4);
        } catch (InvalidRatingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Invalid Rating", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Review Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Table model for attended lessons.
     */
    private static class LessonTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
                "Lesson ID", "Exercise", "Day", "Time Slot", "Week"
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
