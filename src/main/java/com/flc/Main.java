package com.flc;

import com.flc.gui.MainFrame;
import com.flc.service.BookingSystem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the Furzefield Leisure Centre Booking System.
 * Initializes sample data and launches the GUI on the Event Dispatch Thread.
 *
 * @author FLC Development Team
 */
public class Main {

    /**
     * Main method. Creates the BookingSystem, loads sample data,
     * and launches the MainFrame GUI.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Set system look and feel for native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back to default look and feel
        }

        SwingUtilities.invokeLater(() -> {
            BookingSystem system = new BookingSystem();
            system.initializeSampleData();
            MainFrame frame = new MainFrame(system);
            frame.setVisible(true);
        });
    }
}
