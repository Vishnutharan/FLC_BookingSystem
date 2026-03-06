package com.flc.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Centralized theme and design token class for the FLC Booking System.
 */
public final class FLCTheme {

    private FLCTheme() {
    }

    // Light palette
    public static final Color SIDEBAR_BG = new Color(245, 248, 252);
    public static final Color SIDEBAR_HOVER = new Color(231, 239, 250);
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235);
    public static final Color CONTENT_BG = new Color(244, 248, 255);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color HEADER_BG = new Color(255, 255, 255);

    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color SUCCESS = new Color(5, 150, 105);
    public static final Color SUCCESS_HOVER = new Color(4, 120, 87);
    public static final Color WARNING = new Color(217, 119, 6);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color DANGER_HOVER = new Color(185, 28, 28);
    public static final Color PURPLE = new Color(109, 40, 217);

    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    public static final Color TEXT_SECONDARY = new Color(71, 85, 105);
    public static final Color TEXT_LIGHT = new Color(51, 65, 85);

    public static final Color TABLE_HEADER_BG = new Color(247, 250, 255);
    public static final Color TABLE_ALT_ROW = new Color(250, 252, 255);
    public static final Color TABLE_GRID = new Color(226, 232, 240);
    public static final Color BORDER_COLOR = new Color(226, 232, 240);

    public static final Color STATS_BLUE_BG = new Color(239, 246, 255);
    public static final Color STATS_GREEN_BG = new Color(236, 253, 245);
    public static final Color STATS_AMBER_BG = new Color(255, 251, 235);
    public static final Color STATS_PURPLE_BG = new Color(245, 243, 255);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_NAV = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_NAV_ACTIVE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_STATS_VALUE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_STATS_LABEL = new Font("Segoe UI", Font.PLAIN, 12);

    // Dimensions
    public static final int SIDEBAR_WIDTH = 250;
    public static final int HEADER_HEIGHT = 68;
    public static final int CARD_RADIUS = 14;
    public static final int BUTTON_RADIUS = 10;

    /**
     * Creates a styled rounded button with hover effects.
     */
    public static JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BUTTON_RADIUS, BUTTON_RADIUS));
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(FONT_BUTTON);
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY, PRIMARY_HOVER);
    }

    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS, SUCCESS_HOVER);
    }

    public static JButton createDangerButton(String text) {
        return createStyledButton(text, DANGER, DANGER_HOVER);
    }

    /**
     * Creates a card panel with rounded background.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 4, CARD_RADIUS, CARD_RADIUS);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, CARD_RADIUS, CARD_RADIUS));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return card;
    }

    /**
     * Applies table styling.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setGridColor(TABLE_GRID);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setBackground(CARD_BG);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(0, 40));

        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        table.setDefaultRenderer(Integer.class, new AlternatingRowRenderer());
        table.setDefaultRenderer(String.class, new AlternatingRowRenderer());
    }

    /**
     * Wraps a component in a styled scroll pane.
     */
    public static JScrollPane createStyledScrollPane(JComponent component) {
        if (component instanceof JTable) {
            styleTable((JTable) component);
        }
        JScrollPane pane = new JScrollPane(component);
        pane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        pane.getViewport().setBackground(CARD_BG);
        pane.getVerticalScrollBar().setUnitIncrement(16);
        pane.getHorizontalScrollBar().setUnitIncrement(16);
        return pane;
    }

    /**
     * Styles a combo box.
     */
    public static <T> void styleComboBox(JComboBox<T> combo) {
        combo.setFont(FONT_BODY);
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    /**
     * Creates section header label.
     */
    public static JLabel createSectionHeader(String icon, String text) {
        JLabel label = new JLabel(icon + "  " + text);
        label.setFont(FONT_HEADER);
        label.setForeground(TEXT_PRIMARY);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return label;
    }

    /**
     * Creates field label.
     */
    public static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY_BOLD);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    /**
     * Creates stat card.
     */
    public static JPanel createStatsCard(String icon, String value, String label, Color bgColor, Color iconColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CARD_RADIUS, CARD_RADIUS));
                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setLayout(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        iconLabel.setForeground(iconColor);
        card.add(iconLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new java.awt.GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_STATS_VALUE);
        valueLabel.setForeground(TEXT_PRIMARY);
        textPanel.add(valueLabel);

        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(FONT_STATS_LABEL);
        labelLabel.setForeground(TEXT_SECONDARY);
        textPanel.add(labelLabel);

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Creates status label.
     */
    public static JLabel createStatusLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_SECONDARY);
        label.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        return label;
    }

    /**
     * Alternating row renderer.
     */
    public static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                component.setBackground(row % 2 == 0 ? CARD_BG : TABLE_ALT_ROW);
            }
            component.setForeground(TEXT_PRIMARY);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return component;
        }
    }

    /**
     * Applies UI defaults globally.
     */
    public static void applyGlobalDefaults() {
        UIManager.put("Panel.background", CONTENT_BG);
        UIManager.put("OptionPane.background", CARD_BG);
        UIManager.put("OptionPane.messageFont", FONT_BODY);
        UIManager.put("OptionPane.buttonFont", FONT_BUTTON);
        UIManager.put("Button.font", FONT_BODY);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("TextArea.font", FONT_BODY);
        UIManager.put("ComboBox.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_BODY_BOLD);
    }
}
