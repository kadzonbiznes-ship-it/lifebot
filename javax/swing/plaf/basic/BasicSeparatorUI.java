/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.LookAndFeel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.SeparatorUI;

public class BasicSeparatorUI
extends SeparatorUI {
    protected Color shadow;
    protected Color highlight;

    public static ComponentUI createUI(JComponent c) {
        return new BasicSeparatorUI();
    }

    @Override
    public void installUI(JComponent c) {
        this.installDefaults((JSeparator)c);
        this.installListeners((JSeparator)c);
    }

    @Override
    public void uninstallUI(JComponent c) {
        this.uninstallDefaults((JSeparator)c);
        this.uninstallListeners((JSeparator)c);
    }

    protected void installDefaults(JSeparator s) {
        LookAndFeel.installColors(s, "Separator.background", "Separator.foreground");
        LookAndFeel.installProperty(s, "opaque", Boolean.FALSE);
    }

    protected void uninstallDefaults(JSeparator s) {
    }

    protected void installListeners(JSeparator s) {
    }

    protected void uninstallListeners(JSeparator s) {
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Dimension s = c.getSize();
        if (((JSeparator)c).getOrientation() == 1) {
            g.setColor(c.getForeground());
            g.drawLine(0, 0, 0, s.height);
            g.setColor(c.getBackground());
            g.drawLine(1, 0, 1, s.height);
        } else {
            g.setColor(c.getForeground());
            g.drawLine(0, 0, s.width, 0);
            g.setColor(c.getBackground());
            g.drawLine(0, 1, s.width, 1);
        }
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        if (((JSeparator)c).getOrientation() == 1) {
            return new Dimension(2, 0);
        }
        return new Dimension(0, 2);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return null;
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        return null;
    }
}

