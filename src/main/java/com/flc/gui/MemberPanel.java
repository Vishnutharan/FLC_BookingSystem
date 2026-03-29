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
 * Panel for viewing member details and their bookings. Shows a member list on
 * the left and booking details on the right. Includes an "Add New Member"
 * feature.
 *
 * @author FLC Development Team
 */
public class MemberPanel extends JPanel {

    private final BookingSystem bookingSystem;
    private JTable memberTable;
    private MemberListTableModel memberTableModel;
    private JLabel memberDetailLabel;
    private JTable bookingsTable;
    private MemberBookingsTableModel bookingsTableModel;
    private JLabel summaryLabel;

    public MemberPanel(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
        setLayout(new BorderLayout());
        setBackground(FLCTheme.CONTENT_BG);
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        JPanel content = UIHelper.createContentPanel();

        // ─── Split: Members List | Member Details ────────────────
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.35);
        splitPane.setBorder(null);
        splitPane.setDividerSize(10);

        // Left: member list card
        JPanel leftCard = FLCTheme.createCardPanel();
        leftCard.setLayout(new BorderLayout(0, 10));
        leftCard.setPreferredSize(new Dimension(350, 0));
        leftCard.add(FLCTheme.createSectionHeader("\uD83D\uDC65", "All Members"), BorderLayout.NORTH);

        memberTableModel = new MemberListTableModel(bookingSystem.getMembers());
        memberTable = new JTable(memberTableModel);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                showMemberDetails();
        });
        leftCard.add(FLCTheme.createStyledScrollPane(memberTable), BorderLayout.CENTER);

        JButton addMemberBtn = FLCTheme.createSuccessButton("\u2795  Add New Member");
        addMemberBtn.addActionListener(e -> addNewMember());
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addPanel.setOpaque(false);
        addPanel.add(addMemberBtn);
        leftCard.add(addPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftCard);

        // Right: member details card
        JPanel rightCard = FLCTheme.createCardPanel();
        rightCard.setLayout(new BorderLayout(0, 12));
        rightCard.add(FLCTheme.createSectionHeader("\uD83D\uDCCB", "Member Details"), BorderLayout.NORTH);

        // Detail info
        memberDetailLabel = new JLabel(
                "<html><span style='color:#64748B;'>Select a member to view details.</span></html>");
        memberDetailLabel.setFont(FLCTheme.FONT_BODY);
        memberDetailLabel.setVerticalAlignment(SwingConstants.TOP);
        memberDetailLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FLCTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        memberDetailLabel.setOpaque(true);
        memberDetailLabel.setBackground(FLCTheme.STATS_BLUE_BG);

        JPanel detailArea = new JPanel(new BorderLayout(0, 12));
        detailArea.setOpaque(false);
        detailArea.add(memberDetailLabel, BorderLayout.NORTH);

        // Bookings table
        bookingsTableModel = new MemberBookingsTableModel();
        bookingsTable = new JTable(bookingsTableModel);
        detailArea.add(FLCTheme.createStyledScrollPane(bookingsTable), BorderLayout.CENTER);

        summaryLabel = new JLabel(" ");
        summaryLabel.setFont(FLCTheme.FONT_BODY_BOLD);
        summaryLabel.setForeground(FLCTheme.TEXT_PRIMARY);
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        detailArea.add(summaryLabel, BorderLayout.SOUTH);

        rightCard.add(detailArea, BorderLayout.CENTER);
        splitPane.setRightComponent(rightCard);

        content.add(splitPane, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private void showMemberDetails() {
        int row = memberTable.getSelectedRow();
        if (row < 0)
            return;

        int modelRow = memberTable.convertRowIndexToModel(row);
        Member member = bookingSystem.getMembers().get(modelRow);

        memberDetailLabel.setText(String.format(
                "<html>"
                        + "<div style='font-family:Segoe UI;'>"
                        + "<b style='font-size:11px;color:#64748B;'>ID</b><br/>"
                        + "<span style='font-size:13px;color:#0F172A;'>%s</span><br/><br/>"
                        + "<b style='font-size:11px;color:#64748B;'>NAME</b><br/>"
                        + "<span style='font-size:13px;color:#0F172A;'>%s</span><br/><br/>"
                        + "<b style='font-size:11px;color:#64748B;'>EMAIL</b><br/>"
                        + "<span style='font-size:13px;color:#0F172A;'>%s</span>"
                        + "</div></html>",
                member.getMemberId(), member.getName(), member.getEmail()));

        bookingsTableModel.setBookings(new ArrayList<>(member.getActiveBookings()));

        DecimalFormat df = new DecimalFormat("0.00");
        double totalSpent = 0;
        for (Booking booking : member.getActiveBookings()) {
            totalSpent += booking.getLesson().getExerciseType().getPrice();
        }
        summaryLabel.setText(String.format("\uD83D\uDCB0 Total lessons booked: %d  |  Total amount spent: \u00A3%s",
                member.getActiveBookings().size(), df.format(totalSpent)));
    }

    private void addNewMember() {
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField nameField = new JTextField(20);
        nameField.setFont(FLCTheme.FONT_BODY);
        JTextField emailField = new JTextField(20);
        emailField.setFont(FLCTheme.FONT_BODY);
        formPanel.add(FLCTheme.createFieldLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(FLCTheme.createFieldLabel("Email:"));
        formPanel.add(emailField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Add New Member",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and email are required.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            for (Member m : bookingSystem.getMembers()) {
                if (m.getEmail().equalsIgnoreCase(email)) {
                    JOptionPane.showMessageDialog(this,
                            "A member with email '" + email + "' already exists.",
                            "Duplicate Email", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            String newId = bookingSystem.getNextMemberId();
            Member newMember = new Member(newId, name, email);
            bookingSystem.addMember(newMember);
            memberTableModel.refreshData(bookingSystem.getMembers());
            JOptionPane.showMessageDialog(this,
                    "Member added successfully!\nID: " + newId + "\nName: " + name,
                    "Member Added", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static class MemberListTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = { "ID", "Name", "Email" };
        private List<Member> members;

        public MemberListTableModel(List<Member> members) {
            this.members = new ArrayList<>(members);
        }

        @Override
        public int getRowCount() {
            return members.size();
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
            Member m = members.get(row);
            switch (col) {
                case 0:
                    return m.getMemberId();
                case 1:
                    return m.getName();
                case 2:
                    return m.getEmail();
                default:
                    return "";
            }
        }

        public void refreshData(List<Member> members) {
            this.members = new ArrayList<>(members);
            fireTableDataChanged();
        }
    }

    private static class MemberBookingsTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = { "Booking ID", "Lesson ID", "Exercise", "Day", "Time Slot",
                "Week", "Status", "Price" };
        private final DecimalFormat df = new DecimalFormat("0.00");
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
                case 7:
                    return "\u00A3" + df.format(lesson.getExerciseType().getPrice());
                default:
                    return "";
            }
        }

        public void setBookings(List<Booking> bookings) {
            this.bookings = bookings;
            fireTableDataChanged();
        }
    }
}
