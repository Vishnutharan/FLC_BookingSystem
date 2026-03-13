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
import java.util.stream.Collectors;

/**
 * Panel for writing a review for an attended lesson. Includes lesson selection,
 * rating (1-5) with star display, comment with character counter, and
 * duplicate review checking.
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
    private JLabel ratingStarsLabel;
    private JLabel statusLabel;

    public ReviewPanel(BookingSystem bookingSystem) {
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
        JButton loadBtn = FLCTheme.createPrimaryButton("\uD83D\uDD04  Load Attended Lessons");
        loadBtn.addActionListener(e -> loadLessons());
        topCard.add(loadBtn);
        content.add(topCard, BorderLayout.NORTH);
        addStatusLabel(content);

        // ─── Center: Split - Lessons + Review Form ───────────────
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.45);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);

        // Lessons table card
        JPanel lessonCard = FLCTheme.createCardPanel();
        lessonCard.setLayout(new BorderLayout(0, 8));
        lessonCard.add(FLCTheme.createSectionHeader("\uD83D\uDCDA", "Attended Lessons  \u2014  Select one to review"),
                BorderLayout.NORTH);
        tableModel = new LessonTableModel();
        lessonsTable = new JTable(tableModel);
        lessonsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lessonCard.add(FLCTheme.createStyledScrollPane(lessonsTable), BorderLayout.CENTER);
        splitPane.setTopComponent(lessonCard);

        // Review form card
        JPanel reviewCard = FLCTheme.createCardPanel();
        reviewCard.setLayout(new BorderLayout(0, 10));
        reviewCard.add(FLCTheme.createSectionHeader("\u2B50", "Review Details"), BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new BorderLayout(0, 10));
        formPanel.setOpaque(false);

        // Rating row
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        ratingPanel.setOpaque(false);
        ratingPanel.add(FLCTheme.createFieldLabel("Rating:"));
        ratingCombo = new JComboBox<>(new Integer[] { 1, 2, 3, 4, 5 });
        ratingCombo.setSelectedIndex(4);
        FLCTheme.styleComboBox(ratingCombo);
        ratingPanel.add(ratingCombo);

        ratingStarsLabel = new JLabel("\u2605\u2605\u2605\u2605\u2605  Very Satisfied");
        ratingStarsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        ratingStarsLabel.setForeground(FLCTheme.WARNING);
        ratingPanel.add(ratingStarsLabel);
        ratingCombo.addActionListener(e -> updateRatingDisplay());
        formPanel.add(ratingPanel, BorderLayout.NORTH);

        // Comment area
        JPanel commentPanel = new JPanel(new BorderLayout(0, 5));
        commentPanel.setOpaque(false);
        commentPanel.add(FLCTheme.createFieldLabel("Comment:"), BorderLayout.NORTH);
        commentArea = new JTextArea(5, 40);
        commentArea.setFont(FLCTheme.FONT_BODY);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FLCTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        charCountLabel = new JLabel("0 / " + MAX_COMMENT_LENGTH + " characters");
        charCountLabel.setFont(FLCTheme.FONT_SMALL);
        charCountLabel.setForeground(FLCTheme.TEXT_SECONDARY);

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
        formPanel.add(commentPanel, BorderLayout.CENTER);

        reviewCard.add(formPanel, BorderLayout.CENTER);
        splitPane.setBottomComponent(reviewCard);

        content.add(splitPane, BorderLayout.CENTER);

        // ─── Bottom: Submit Button ───────────────────────────────
        JButton submitBtn = FLCTheme.createStyledButton("\u2714  Submit Review", FLCTheme.PURPLE,
                new Color(124, 58, 237));
        content.add(UIHelper.createActionPanel(submitBtn), BorderLayout.SOUTH);
        submitBtn.addActionListener(e -> submitReview());

        add(content, BorderLayout.CENTER);
    }

    private void addStatusLabel(JPanel content) {
        statusLabel = FLCTheme.createStatusLabel("Select a member and load their attended lessons.");
        content.add(statusLabel, BorderLayout.SOUTH);
    }

    private void loadLessons() {
        Member member = (Member) memberCombo.getSelectedItem();
        if (member == null)
            return;
        List<Lesson> reviewableLessons = bookingSystem.getReviewableLessons(member);
        tableModel.setLessons(reviewableLessons);
        statusLabel.setText(member.getName() + " has " + reviewableLessons.size()
                + " attended lesson(s) awaiting review.");
    }

    private void updateRatingDisplay() {
        int rating = (Integer) ratingCombo.getSelectedItem();
        String stars = UIHelper.getStarRating(rating);
        String[] meanings = { "", "Very Dissatisfied", "Dissatisfied", "Neutral", "Satisfied", "Very Satisfied" };
        ratingStarsLabel.setText(stars + "  " + meanings[rating]);
    }

    private void updateCharCount() {
        int len = commentArea.getText().length();
        charCountLabel.setText(len + " / " + MAX_COMMENT_LENGTH + " characters");
        charCountLabel.setForeground(len > MAX_COMMENT_LENGTH ? FLCTheme.DANGER : FLCTheme.TEXT_SECONDARY);
    }

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
                    String.format("Review submitted successfully!\nLesson: %s\nRating: %d/5 %s",
                            lesson.getLessonId(), rating, UIHelper.getStarRating(rating)),
                    "Review Submitted", JOptionPane.INFORMATION_MESSAGE);
            commentArea.setText("");
            ratingCombo.setSelectedIndex(4);
            loadLessons();
        } catch (InvalidRatingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Invalid Rating", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Review Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class LessonTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = { "Lesson ID", "Exercise", "Day", "Time Slot", "Week" };
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
