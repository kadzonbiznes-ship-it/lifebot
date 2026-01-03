/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.accessibility.Accessible;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public abstract class ComponentUI {
    public void installUI(JComponent c) {
    }

    public void uninstallUI(JComponent c) {
    }

    public void paint(Graphics g, JComponent c) {
    }

    public void update(Graphics g, JComponent c) {
        if (c.isOpaque()) {
            g.setColor(c.getBackground());
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }
        this.paint(g, c);
    }

    public Dimension getPreferredSize(JComponent c) {
        return null;
    }

    public Dimension getMinimumSize(JComponent c) {
        return this.getPreferredSize(c);
    }

    public Dimension getMaximumSize(JComponent c) {
        return this.getPreferredSize(c);
    }

    public boolean contains(JComponent c, int x, int y) {
        return c.inside(x, y);
    }

    public static ComponentUI createUI(JComponent c) {
        throw new Error("ComponentUI.createUI not implemented.");
    }

    public int getBaseline(JComponent c, int width, int height) {
        if (c == null) {
            throw new NullPointerException("Component must be non-null");
        }
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Width and height must be >= 0");
        }
        return -1;
    }

    public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
        if (c == null) {
            throw new NullPointerException("Component must be non-null");
        }
        return Component.BaselineResizeBehavior.OTHER;
    }

    public int getAccessibleChildrenCount(JComponent c) {
        return SwingUtilities.getAccessibleChildrenCount(c);
    }

    public Accessible getAccessibleChild(JComponent c, int i) {
        return SwingUtilities.getAccessibleChild(c, i);
    }
}

