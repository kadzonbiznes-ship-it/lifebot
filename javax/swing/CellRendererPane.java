/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;

public class CellRendererPane
extends Container
implements Accessible {
    protected AccessibleContext accessibleContext = null;

    public CellRendererPane() {
        this.setLayout(null);
        this.setVisible(false);
    }

    @Override
    public void invalidate() {
    }

    @Override
    public void paint(Graphics g) {
    }

    @Override
    public void update(Graphics g) {
    }

    @Override
    protected void addImpl(Component x, Object constraints, int index) {
        if (x.getParent() == this) {
            return;
        }
        super.addImpl(x, constraints, index);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void paintComponent(Graphics g, Component c, Container p, int x, int y, int w, int h, boolean shouldValidate) {
        if (c == null) {
            if (p != null) {
                Color oldColor = g.getColor();
                g.setColor(p.getBackground());
                g.fillRect(x, y, w, h);
                g.setColor(oldColor);
            }
            return;
        }
        if (c.getParent() != this) {
            this.add(c);
        }
        c.setBounds(x, y, w, h);
        if (shouldValidate) {
            c.validate();
        }
        boolean wasDoubleBuffered = false;
        if (c instanceof JComponent && ((JComponent)c).isDoubleBuffered()) {
            wasDoubleBuffered = true;
            ((JComponent)c).setDoubleBuffered(false);
        }
        Graphics cg = g.create(x, y, w, h);
        try {
            c.paint(cg);
        }
        finally {
            cg.dispose();
        }
        if (wasDoubleBuffered && c instanceof JComponent) {
            ((JComponent)c).setDoubleBuffered(true);
        }
        c.setBounds(-w, -h, 0, 0);
    }

    public void paintComponent(Graphics g, Component c, Container p, int x, int y, int w, int h) {
        this.paintComponent(g, c, p, x, y, w, h, false);
    }

    public void paintComponent(Graphics g, Component c, Container p, Rectangle r) {
        this.paintComponent(g, c, p, r.x, r.y, r.width, r.height);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        this.removeAll();
        s.defaultWriteObject();
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleCellRendererPane();
        }
        return this.accessibleContext;
    }

    protected class AccessibleCellRendererPane
    extends Container.AccessibleAWTContainer {
        protected AccessibleCellRendererPane() {
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PANEL;
        }
    }
}

