/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.ui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

public class ParticleBackgroundPanel
extends JPanel
implements ActionListener {
    private final List<Particle> particles = new ArrayList<Particle>();
    private final Timer timer;
    private final Random random = new Random();

    public ParticleBackgroundPanel() {
        this.setBackground(Color.decode("#1E1E1E"));
        this.timer = new Timer(16, this);
        this.timer.start();
    }

    public void setAnimationPaused(boolean paused) {
        if (paused) {
            this.timer.stop();
        } else {
            this.timer.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        for (int i = 0; i < this.particles.size(); ++i) {
            Particle p = this.particles.get(i);
            g2d.setColor(new Color(255, 255, 255, 40));
            g2d.fill(new Ellipse2D.Double(p.x, p.y, 3.0, 3.0));
            for (int j = i + 1; j < this.particles.size(); ++j) {
                Particle other = this.particles.get(j);
                double dx = p.x - other.x;
                double dy = p.y - other.y;
                double distSq = dx * dx + dy * dy;
                if (!(distSq < 14400.0)) continue;
                double dist = Math.sqrt(distSq);
                int alpha = (int)((1.0 - dist / 120.0) * 30.0);
                g2d.setColor(new Color(255, 255, 255, alpha));
                g2d.draw(new Line2D.Double(p.x + 1.5, p.y + 1.5, other.x + 1.5, other.y + 1.5));
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int width = this.getWidth();
        int height = this.getHeight();
        if (this.particles.isEmpty() && width > 0) {
            for (int i = 0; i < 60; ++i) {
                this.particles.add(new Particle(this, width, height));
            }
        }
        for (Particle p : this.particles) {
            p.update(width, height);
        }
        this.repaint();
    }

    private class Particle {
        double x;
        double y;
        double dx;
        double dy;

        Particle(ParticleBackgroundPanel particleBackgroundPanel, int w, int h) {
            this.x = particleBackgroundPanel.random.nextInt(w);
            this.y = particleBackgroundPanel.random.nextInt(h);
            this.dx = (particleBackgroundPanel.random.nextDouble() - 0.5) * 1.0;
            this.dy = (particleBackgroundPanel.random.nextDouble() - 0.5) * 1.0;
        }

        void update(int w, int h) {
            this.x += this.dx;
            this.y += this.dy;
            if (this.x < 0.0 || this.x > (double)w) {
                this.dx = -this.dx;
            }
            if (this.y < 0.0 || this.y > (double)h) {
                this.dy = -this.dy;
            }
        }

        double distance(Particle other) {
            return Math.sqrt(Math.pow(this.x - other.x, 2.0) + Math.pow(this.y - other.y, 2.0));
        }
    }
}

