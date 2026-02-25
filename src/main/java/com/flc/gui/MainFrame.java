package com.flc.gui;

import com.flc.service.BookingSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Main application frame for the Furzefield Leisure Centre Booking System.
 * Contains a JTabbedPane with all functional panels and a menu bar.
 *
 * @author FLC Development Team
 */
public class MainFrame extends JFrame {

    private final BookingSystem bookingSystem;
    private JTabbedPane tabbedPane;

    /**
     * Constructs the main frame with all panels.
     *
     * @param bookingSystem the booking system instance
     */
    public MainFrame(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
        initializeFrame();
        createMenuBar();
        createTabbedPane();
    }

    /**
     * Initializes the frame properties.
     */
    private void initializeFrame() {
        setTitle("Furzefield Leisure Centre - Booking System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setMinimumSize(new Dimension(900, 600));
    }

    /**
     * Creates the menu bar with File and Help menus.
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit?", "Exit",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Furzefield Leisure Centre - Booking System\n"
                        + "Version 1.0\n\n"
                        + "Developed for 7COM1025 – Programming for Software Engineers\n"
                        + "University of Hertfordshire\n\n"
                        + "This application manages lesson bookings, reviews,\n"
                        + "and generates attendance and income reports.",
                "About FLC Booking System",
                JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    /**
     * Creates the tabbed pane with all panels.
     */
    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 13));

        tabbedPane.addTab("Timetable", new TimetablePanel(bookingSystem));
        tabbedPane.addTab("Book Lesson", new BookingPanel(bookingSystem));
        tabbedPane.addTab("Change Booking", new ChangeBookingPanel(bookingSystem));
        tabbedPane.addTab("Cancel Booking", new CancelBookingPanel(bookingSystem));
        tabbedPane.addTab("Write Review", new ReviewPanel(bookingSystem));
        tabbedPane.addTab("Members", new MemberPanel(bookingSystem));
        tabbedPane.addTab("Reports", new ReportPanel(bookingSystem));

        add(tabbedPane);
    }
}
