package com.flc.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Utility helper class for reusable UI behavior.
 */
public final class UIHelper {

    private UIHelper() {
    }

    /**
     * Creates a titled card wrapper.
     */
    public static JPanel wrapInTitledCard(String icon, String title, JComponent content) {
        JPanel card = FLCTheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));
        card.add(FLCTheme.createSectionHeader(icon, title), BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    /**
     * Creates a horizontal row of stat cards.
     */
    public static JPanel createStatsRow(JPanel... cards) {
        JPanel row = new JPanel(new GridLayout(1, cards.length, 15, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        for (JPanel card : cards) {
            row.add(card);
        }
        return row;
    }

    /**
     * Creates default padded content panel.
     */
    public static JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        return panel;
    }

    /**
     * Creates a separator with theme color.
     */
    public static JSeparator createSeparator() {
        JSeparator separator = new JSeparator();
        separator.setForeground(FLCTheme.BORDER_COLOR);
        return separator;
    }

    /**
     * Creates a labeled form row.
     */
    public static JPanel createFormRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row.setOpaque(false);
        row.add(FLCTheme.createFieldLabel(labelText));
        row.add(field);
        return row;
    }

    /**
     * Creates right-aligned action panel.
     */
    public static JPanel createActionPanel(JButton... buttons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setOpaque(false);
        for (JButton button : buttons) {
            panel.add(button);
        }
        return panel;
    }

    /**
     * Returns a star string for a 1-5 rating.
     */
    public static String getStarRating(int rating) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= rating ? '*' : '-');
        }
        return sb.toString();
    }

    /**
     * Returns a star string for a floating-point rating.
     */
    public static String getStarRatingDouble(double rating) {
        int rounded = (int) Math.round(rating);
        return getStarRating(rounded);
    }

    /**
     * Animates a smooth background color transition.
     *
     * @param component component to animate
     * @param start     start color
     * @param end       end color
     * @param duration  duration in milliseconds
     */
    public static void animateBackground(JComponent component, Color start, Color end, int duration) {
        final int frames = 12;
        final int delay = Math.max(10, duration / frames);

        Timer timer = new Timer(delay, null);
        timer.addActionListener(new ActionListener() {
            int frame = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                frame++;
                float progress = Math.min(1f, frame / (float) frames);

                int red = (int) (start.getRed() + ((end.getRed() - start.getRed()) * progress));
                int green = (int) (start.getGreen() + ((end.getGreen() - start.getGreen()) * progress));
                int blue = (int) (start.getBlue() + ((end.getBlue() - start.getBlue()) * progress));

                component.setBackground(new Color(red, green, blue));
                component.repaint();

                if (progress >= 1f) {
                    timer.stop();
                }
            }
        });
        timer.start();
    }

    /**
     * Creates a compact metric label for header ribbons.
     */
    public static JLabel createMetricLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FLCTheme.FONT_SMALL);
        label.setForeground(FLCTheme.TEXT_SECONDARY);
        return label;
    }
}
