package com.flc.gui;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.Timer;
import javax.swing.plaf.LayerUI;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * LayerUI that provides a fade-and-slide entrance animation.
 */
public class AnimatedLayerUI extends LayerUI<JComponent> {

    private float alpha = 1.0f;
    private int yOffset = 0;
    private Timer timer;

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.translate(0, yOffset);
        super.paint(g2, c);
        g2.dispose();
    }

    /**
     * Starts the entrance animation for the target layer.
     *
     * @param layer target JLayer
     */
    public void animateIn(JLayer<? extends JComponent> layer) {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        alpha = 0.0f;
        yOffset = 22;

        final int frames = 18;
        timer = new Timer(16, null);
        timer.addActionListener(new ActionListener() {
            int frame = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                frame++;
                float progress = Math.min(1.0f, frame / (float) frames);

                // Cubic ease-out.
                float eased = 1.0f - (float) Math.pow(1.0f - progress, 3);
                alpha = eased;
                yOffset = Math.round((1.0f - eased) * 22);

                layer.repaint();
                if (progress >= 1.0f) {
                    timer.stop();
                }
            }
        });
        timer.start();
    }
}
