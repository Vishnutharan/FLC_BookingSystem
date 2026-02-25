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
 * the
 * left and booking details on the right. Includes an "Add New Member" feature.
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

    /**
     * Constructs the MemberPanel.
     *
     * @param bookingSystem the booking system instance
     */
    public MemberPanel(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    /**
     * Builds the UI components.
     */
    private void buildUI() {
        JLabel title = new JLabel("Members", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        // Left: member list
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("All Members"));
        leftPanel.setPreferredSize(new Dimension(350, 0));

        memberTableModel = new MemberListTableModel(bookingSystem.getMembers());
        memberTable = new JTable(memberTableModel);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.setRowHeight(25);
        memberTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                showMemberDetails();
        });
        leftPanel.add(new JScrollPane(memberTable), BorderLayout.CENTER);

        JButton addMemberBtn = new JButton("Add New Member");
        addMemberBtn.addActionListener(e -> addNewMember());
        leftPanel.add(addMemberBtn, BorderLayout.SOUTH);

        // Right: member details
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Member Details"));

        memberDetailLabel = new JLabel("Select a member to view details.");
        memberDetailLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        memberDetailLabel.setVerticalAlignment(SwingConstants.TOP);
        rightPanel.add(memberDetailLabel, BorderLayout.NORTH);

        bookingsTableModel = new MemberBookingsTableModel();
        bookingsTable = new JTable(bookingsTableModel);
        bookingsTable.setRowHeight(25);
        bookingsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        rightPanel.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);

        summaryLabel = new JLabel(" ");
        summaryLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        rightPanel.add(summaryLabel, BorderLayout.SOUTH);

        // Split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.35);
        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Shows the selected member's details and bookings.
     */
    private void showMemberDetails() {
        int row = memberTable.getSelectedRow();
        if (row < 0)
            return;

        int modelRow = memberTable.convertRowIndexToModel(row);
        Member member = bookingSystem.getMembers().get(modelRow);

        memberDetailLabel.setText(String.format(
                "<html><b>ID:</b> %s<br/><b>Name:</b> %s<br/><b>Email:</b> %s</html>",
                member.getMemberId(), member.getName(), member.getEmail()));

        bookingsTableModel.setLessons(new ArrayList<>(member.getBookedLessons()));

        DecimalFormat df = new DecimalFormat("0.00");
        double totalSpent = 0;
        for (Lesson l : member.getBookedLessons()) {
            totalSpent += l.getExerciseType().getPrice();
        }
        summaryLabel.setText(String.format("Total lessons booked: %d  |  Total amount spent: £%s",
                member.getBookedLessons().size(), df.format(totalSpent)));
    }

    /**
     * Opens a dialog to add a new member.
     */
    private void addNewMember() {
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
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

            // Basic email format validation
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check unique email
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

    /**
     * Table model for the member list.
     */
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

    /**
     * Table model for a member's bookings.
     */
    private static class MemberBookingsTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
                "Lesson ID", "Exercise", "Day", "Time Slot", "Week", "Price"
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
                    return "£" + df.format(l.getExerciseType().getPrice());
                default:
                    return "";
            }
        }

        public void setLessons(List<Lesson> lessons) {
            this.lessons = lessons;
            fireTableDataChanged();
        }
    }
}
