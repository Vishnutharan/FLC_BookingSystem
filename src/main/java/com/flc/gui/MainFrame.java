package com.flc.gui;

import com.flc.service.BookingSystem;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Timer;

/**
 * Main application frame for the Furzefield Leisure Centre Booking System.
 */
public class MainFrame extends JFrame {

    private final BookingSystem bookingSystem;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel headerTitle;
    private JLabel headerSubtitle;
    private JLabel dateLabel;
    private JPanel navPanel;
    private JPanel activeNavButton;

    private final Map<String, AnimatedLayerUI> cardAnimations = new HashMap<>();
    private final Map<String, JLayer<JComponent>> cardLayers = new HashMap<>();

    private static final String[][] NAV_ITEMS = {
            { "TT", "Timetable", "TIMETABLE" },
            { "BK", "Book Lesson", "BOOK" },
            { "AT", "Attendance", "ATTENDANCE" },
            { "CH", "Change Booking", "CHANGE" },
            { "CA", "Cancel Booking", "CANCEL" },
            { "RV", "Write Review", "REVIEW" },
            { "MB", "Members", "MEMBERS" },
            { "RP", "Reports", "REPORTS" }
    };

    public MainFrame(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
        initializeFrame();
        createMenuBar();
        buildDashboardLayout();
        startClock();
    }

    private void initializeFrame() {
        setTitle("Furzefield Leisure Centre - Booking System");
        setSize(1280, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1080, 700));
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, FLCTheme.BORDER_COLOR));

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit?",
                    "Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Furzefield Leisure Centre - Booking System\n"
                        + "Version 1.1\n\n"
                        + "Manages lessons, bookings, attendance, reviews, and reports\n"
                        + "for repeating weekend exercise schedules.",
                "About FLC Booking System",
                JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void buildDashboardLayout() {
        JPanel mainContainer = new GradientPanel();
        mainContainer.setLayout(new BorderLayout());

        JPanel sidebar = createSidebar();
        mainContainer.add(sidebar, BorderLayout.WEST);

        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setOpaque(false);

        JPanel headerBar = createHeaderBar();
        rightSide.add(headerBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        addAnimatedCard(new TimetablePanel(bookingSystem), "TIMETABLE");
        addAnimatedCard(new BookingPanel(bookingSystem), "BOOK");
        addAnimatedCard(new AttendancePanel(bookingSystem), "ATTENDANCE");
        addAnimatedCard(new ChangeBookingPanel(bookingSystem), "CHANGE");
        addAnimatedCard(new CancelBookingPanel(bookingSystem), "CANCEL");
        addAnimatedCard(new ReviewPanel(bookingSystem), "REVIEW");
        addAnimatedCard(new MemberPanel(bookingSystem), "MEMBERS");
        addAnimatedCard(new ReportPanel(bookingSystem), "REPORTS");

        rightSide.add(contentPanel, BorderLayout.CENTER);
        mainContainer.add(rightSide, BorderLayout.CENTER);

        setContentPane(mainContainer);

        if (navPanel.getComponentCount() > 0) {
            selectNavItem((JPanel) navPanel.getComponent(0), NAV_ITEMS[0][1], NAV_ITEMS[0][2]);
        }
    }

    private void addAnimatedCard(JComponent panel, String cardKey) {
        AnimatedLayerUI ui = new AnimatedLayerUI();
        JLayer<JComponent> layer = new JLayer<>(panel, ui);
        cardAnimations.put(cardKey, ui);
        cardLayers.put(cardKey, layer);
        contentPanel.add(layer, cardKey);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(FLCTheme.SIDEBAR_WIDTH, 0));
        sidebar.setBackground(FLCTheme.SIDEBAR_BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, FLCTheme.BORDER_COLOR));

        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        brandPanel.setMaximumSize(new Dimension(FLCTheme.SIDEBAR_WIDTH, 90));

        JLabel logoBadge = new JLabel("FLC", JLabel.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FLCTheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBadge.setOpaque(false);
        logoBadge.setForeground(Color.WHITE);
        logoBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoBadge.setPreferredSize(new Dimension(48, 38));
        brandPanel.add(logoBadge, BorderLayout.WEST);

        JPanel brandText = new JPanel(new GridLayout(2, 1));
        brandText.setOpaque(false);
        brandText.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        JLabel appName = new JLabel("Furzefield Centre");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        appName.setForeground(FLCTheme.TEXT_PRIMARY);
        brandText.add(appName);

        JLabel appSub = new JLabel("Weekend Booking Suite");
        appSub.setFont(FLCTheme.FONT_SMALL);
        appSub.setForeground(FLCTheme.TEXT_SECONDARY);
        brandText.add(appSub);

        brandPanel.add(brandText, BorderLayout.CENTER);
        sidebar.add(brandPanel);

        JSeparator separator = new JSeparator();
        separator.setForeground(FLCTheme.BORDER_COLOR);
        separator.setMaximumSize(new Dimension(FLCTheme.SIDEBAR_WIDTH, 1));
        sidebar.add(separator);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);

        for (String[] item : NAV_ITEMS) {
            JPanel navButton = createNavButton(item[0], item[1], item[2]);
            navPanel.add(navButton);
            navPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        sidebar.add(navPanel);
        sidebar.add(Box.createVerticalGlue());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 16, 20));
        footer.setMaximumSize(new Dimension(FLCTheme.SIDEBAR_WIDTH, 56));

        JLabel version = new JLabel("v1.1 | UoH 2026");
        version.setFont(FLCTheme.FONT_SMALL);
        version.setForeground(FLCTheme.TEXT_SECONDARY);
        footer.add(version, BorderLayout.SOUTH);
        sidebar.add(footer);

        return sidebar;
    }

    private JPanel createNavButton(String icon, String label, String cardKey) {
        JPanel button = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(10, 0, getWidth() - 20, getHeight(), 10, 10));
                g2.dispose();
            }
        };

        button.setOpaque(false);
        button.setBackground(FLCTheme.SIDEBAR_BG);
        button.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        button.setMaximumSize(new Dimension(FLCTheme.SIDEBAR_WIDTH, 46));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        iconLabel.setForeground(FLCTheme.TEXT_SECONDARY);
        iconLabel.setPreferredSize(new Dimension(34, 20));
        button.add(iconLabel, BorderLayout.WEST);

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(FLCTheme.FONT_NAV);
        textLabel.setForeground(FLCTheme.TEXT_LIGHT);
        button.add(textLabel, BorderLayout.CENTER);

        button.putClientProperty("iconLabel", iconLabel);
        button.putClientProperty("textLabel", textLabel);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectNavItem(button, label, cardKey);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != activeNavButton) {
                    UIHelper.animateBackground(button, button.getBackground(), FLCTheme.SIDEBAR_HOVER, 120);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button != activeNavButton) {
                    UIHelper.animateBackground(button, button.getBackground(), FLCTheme.SIDEBAR_BG, 120);
                }
            }
        });

        return button;
    }

    private void selectNavItem(JPanel button, String label, String cardKey) {
        if (activeNavButton != null) {
            styleNavButton(activeNavButton, false);
        }

        activeNavButton = button;
        styleNavButton(button, true);

        headerTitle.setText(label);
        headerSubtitle.setText(getSubtitleForCard(cardKey));

        cardLayout.show(contentPanel, cardKey);

        JLayer<JComponent> layer = cardLayers.get(cardKey);
        AnimatedLayerUI animation = cardAnimations.get(cardKey);
        if (layer != null && animation != null) {
            animation.animateIn(layer);
        }
    }

    private void styleNavButton(JPanel button, boolean active) {
        JLabel textLabel = (JLabel) button.getClientProperty("textLabel");
        JLabel iconLabel = (JLabel) button.getClientProperty("iconLabel");

        if (active) {
            UIHelper.animateBackground(button, button.getBackground(), FLCTheme.SIDEBAR_ACTIVE, 140);
            textLabel.setFont(FLCTheme.FONT_NAV_ACTIVE);
            textLabel.setForeground(Color.WHITE);
            iconLabel.setForeground(Color.WHITE);
        } else {
            UIHelper.animateBackground(button, button.getBackground(), FLCTheme.SIDEBAR_BG, 140);
            textLabel.setFont(FLCTheme.FONT_NAV);
            textLabel.setForeground(FLCTheme.TEXT_LIGHT);
            iconLabel.setForeground(FLCTheme.TEXT_SECONDARY);
        }
        button.repaint();
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(FLCTheme.HEADER_BG);
        header.setPreferredSize(new Dimension(0, FLCTheme.HEADER_HEIGHT));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, FLCTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 26, 10, 26)));

        JPanel titleArea = new JPanel(new GridLayout(2, 1));
        titleArea.setOpaque(false);

        headerTitle = new JLabel("Timetable");
        headerTitle.setFont(FLCTheme.FONT_HEADER);
        headerTitle.setForeground(FLCTheme.TEXT_PRIMARY);
        titleArea.add(headerTitle);

        headerSubtitle = new JLabel("View and search the lesson timetable");
        headerSubtitle.setFont(FLCTheme.FONT_SMALL);
        headerSubtitle.setForeground(FLCTheme.TEXT_SECONDARY);
        titleArea.add(headerSubtitle);

        header.add(titleArea, BorderLayout.WEST);

        dateLabel = new JLabel();
        dateLabel.setFont(FLCTheme.FONT_BODY);
        dateLabel.setForeground(FLCTheme.TEXT_SECONDARY);
        header.add(dateLabel, BorderLayout.EAST);

        return header;
    }

    private void startClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss");
        Timer timer = new Timer(1000, e -> dateLabel.setText(LocalDateTime.now().format(formatter)));
        timer.setRepeats(true);
        timer.start();
        dateLabel.setText(LocalDateTime.now().format(formatter));
    }

    private String getSubtitleForCard(String cardKey) {
        switch (cardKey) {
            case "TIMETABLE":
                return "View lessons by day/week or by exercise type";
            case "BOOK":
                return "Book a lesson with capacity and time-conflict validation";
            case "ATTENDANCE":
                return "Attend a lesson and capture the required review and rating";
            case "CHANGE":
                return "Move a booking to another available lesson";
            case "CANCEL":
                return "Cancel a booking and free the lesson space";
            case "REVIEW":
                return "Submit one review and rating for attended sessions";
            case "MEMBERS":
                return "Inspect member profiles and current bookings";
            case "REPORTS":
                return "Generate monthly lesson reports and champion exercise reports";
            default:
                return "";
        }
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint paint = new GradientPaint(
                    0,
                    0,
                    new Color(246, 250, 255),
                    getWidth(),
                    getHeight(),
                    new Color(238, 245, 255));
            g2.setPaint(paint);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
