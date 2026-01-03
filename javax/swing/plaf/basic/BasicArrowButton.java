/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import sun.swing.SwingUtilities2;

public class BasicArrowButton
extends JButton
implements SwingConstants {
    protected int direction;
    private Color shadow;
    private Color darkShadow;
    private Color highlight;

    public BasicArrowButton(int direction, Color background, Color shadow, Color darkShadow, Color highlight) {
        this.setRequestFocusEnabled(false);
        this.setDirection(direction);
        this.setBackground(background);
        this.shadow = shadow;
        this.darkShadow = darkShadow;
        this.highlight = highlight;
    }

    public BasicArrowButton(int direction) {
        this(direction, UIManager.getColor("control"), UIManager.getColor("controlShadow"), UIManager.getColor("controlDkShadow"), UIManager.getColor("controlLtHighlight"));
    }

    public int getDirection() {
        return this.direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    @Override
    public void paint(Graphics g) {
        int w = this.getSize().width;
        int h = this.getSize().height;
        Color origColor = g.getColor();
        boolean isPressed = this.getModel().isPressed();
        boolean isEnabled = this.isEnabled();
        g.setColor(this.getBackground());
        g.fillRect(1, 1, w - 2, h - 2);
        if (this.getBorder() != null && !(this.getBorder() instanceof UIResource)) {
            this.paintBorder(g);
        } else if (isPressed) {
            g.setColor(this.shadow);
            g.drawRect(0, 0, w - 1, h - 1);
        } else {
            g.drawLine(0, 0, 0, h - 1);
            g.drawLine(1, 0, w - 2, 0);
            g.setColor(this.highlight);
            g.drawLine(1, 1, 1, h - 3);
            g.drawLine(2, 1, w - 3, 1);
            g.setColor(this.shadow);
            g.drawLine(1, h - 2, w - 2, h - 2);
            g.drawLine(w - 2, 1, w - 2, h - 3);
            g.setColor(this.darkShadow);
            g.drawLine(0, h - 1, w - 1, h - 1);
            g.drawLine(w - 1, h - 1, w - 1, 0);
        }
        if (h < 5 || w < 5) {
            g.setColor(origColor);
            return;
        }
        if (isPressed) {
            g.translate(1, 1);
        }
        int size = Math.min((h - 4) / 3, (w - 4) / 3);
        size = Math.max(size, 2);
        this.paintTriangle(g, (w - size) / 2, (h - size) / 2, size, this.direction, isEnabled);
        if (isPressed) {
            g.translate(-1, -1);
        }
        g.setColor(origColor);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(16, 16);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(5, 5);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public boolean isFocusTraversable() {
        return false;
    }

    public void paintTriangle(Graphics g, int x, int y, int size, int direction, boolean isEnabled) {
        if (SwingUtilities2.isScaledGraphics(g)) {
            this.paintScaledTriangle(g, x, y, size, direction, isEnabled);
        } else {
            this.paintUnscaledTriangle(g, x, y, size, direction, isEnabled);
        }
    }

    private void paintUnscaledTriangle(Graphics g, int x, int y, int size, int direction, boolean isEnabled) {
        Color oldColor = g.getColor();
        int j = 0;
        size = Math.max(size, 2);
        int mid = size / 2 - 1;
        g.translate(x, y);
        if (isEnabled) {
            g.setColor(this.darkShadow);
        } else {
            g.setColor(this.shadow);
        }
        switch (direction) {
            case 1: {
                int i;
                for (i = 0; i < size; ++i) {
                    g.drawLine(mid - i, i, mid + i, i);
                }
                if (isEnabled) break;
                g.setColor(this.highlight);
                g.drawLine(mid - i + 2, i, mid + i, i);
                break;
            }
            case 5: {
                int i;
                if (!isEnabled) {
                    g.translate(1, 1);
                    g.setColor(this.highlight);
                    for (i = size - 1; i >= 0; --i) {
                        g.drawLine(mid - i, j, mid + i, j);
                        ++j;
                    }
                    g.translate(-1, -1);
                    g.setColor(this.shadow);
                }
                j = 0;
                for (i = size - 1; i >= 0; --i) {
                    g.drawLine(mid - i, j, mid + i, j);
                    ++j;
                }
                break;
            }
            case 7: {
                int i;
                for (i = 0; i < size; ++i) {
                    g.drawLine(i, mid - i, i, mid + i);
                }
                if (isEnabled) break;
                g.setColor(this.highlight);
                g.drawLine(i, mid - i + 2, i, mid + i);
                break;
            }
            case 3: {
                int i;
                if (!isEnabled) {
                    g.translate(1, 1);
                    g.setColor(this.highlight);
                    for (i = size - 1; i >= 0; --i) {
                        g.drawLine(j, mid - i, j, mid + i);
                        ++j;
                    }
                    g.translate(-1, -1);
                    g.setColor(this.shadow);
                }
                j = 0;
                for (i = size - 1; i >= 0; --i) {
                    g.drawLine(j, mid - i, j, mid + i);
                    ++j;
                }
                break;
            }
        }
        g.translate(-x, -y);
        g.setColor(oldColor);
    }

    private void paintScaledTriangle(Graphics g, double x, double y, double size, int direction, boolean isEnabled) {
        size = Math.max(size, 2.0);
        Path2D.Double path = new Path2D.Double();
        path.moveTo(-size, size / 2.0);
        path.lineTo(size, size / 2.0);
        path.lineTo(0.0, -size / 2.0);
        path.closePath();
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(Math.PI * (double)(direction - 1) / 4.0);
        path.transform(affineTransform);
        Graphics2D g2d = (Graphics2D)g;
        double tx = x + size / 2.0;
        double ty = y + size / 2.0;
        g2d.translate(tx, ty);
        Color oldColor = g.getColor();
        if (!isEnabled) {
            g2d.translate(1, 0);
            g2d.setColor(this.highlight);
            g2d.fill(path);
            g2d.translate(-1, 0);
        }
        g2d.setColor(isEnabled ? this.darkShadow : this.shadow);
        g2d.fill(path);
        g2d.translate(-tx, -ty);
        g2d.setColor(oldColor);
    }
}

