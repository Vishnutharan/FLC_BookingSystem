package com.flc;

import com.flc.gui.FLCTheme;
import com.flc.gui.MainFrame;
import com.flc.service.BookingSystem;
import javax.swing.SwingUtilities;

/**
 * Entry point for the Furzefield Leisure Centre Booking System.
 * Initializes sample data, applies the custom FLC theme, and launches
 * the GUI on the Event Dispatch Thread.
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
        BookingSystem system = new BookingSystem();
        system.initializeSampleData();

        if (args.length > 0 && "--console".equalsIgnoreCase(args[0])) {
            System.out.println(system.generateRequirementAuditReport());
            for (int cycle = 1; cycle <= system.getCycleCount(); cycle++) {
                System.out.println(system.generateAttendanceReportForCycle(cycle));
                System.out.println(system.generateIncomeReportForCycle(cycle));
            }
            return;
        }

        // Apply FLC custom theme defaults
        FLCTheme.applyGlobalDefaults();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(system);
            frame.setVisible(true);
        });
    }
}
