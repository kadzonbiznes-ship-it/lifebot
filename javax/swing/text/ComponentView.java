/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Shape;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;

public class ComponentView
extends View {
    private Component createdC;
    private Invalidator c;

    public ComponentView(Element elem) {
        super(elem);
    }

    protected Component createComponent() {
        AttributeSet attr = this.getElement().getAttributes();
        Component comp = StyleConstants.getComponent(attr);
        return comp;
    }

    public final Component getComponent() {
        return this.createdC;
    }

    @Override
    public void paint(Graphics g, Shape a) {
        if (this.c != null) {
            Rectangle alloc = a instanceof Rectangle ? (Rectangle)a : a.getBounds();
            this.c.setBounds(alloc.x, alloc.y, alloc.width, alloc.height);
        }
    }

    @Override
    public float getPreferredSpan(int axis) {
        if (axis != 0 && axis != 1) {
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }
        if (this.c != null) {
            Dimension size = this.c.getPreferredSize();
            if (axis == 0) {
                return size.width;
            }
            return size.height;
        }
        return 0.0f;
    }

    @Override
    public float getMinimumSpan(int axis) {
        if (axis != 0 && axis != 1) {
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }
        if (this.c != null) {
            Dimension size = this.c.getMinimumSize();
            if (axis == 0) {
                return size.width;
            }
            return size.height;
        }
        return 0.0f;
    }

    @Override
    public float getMaximumSpan(int axis) {
        if (axis != 0 && axis != 1) {
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }
        if (this.c != null) {
            Dimension size = this.c.getMaximumSize();
            if (axis == 0) {
                return size.width;
            }
            return size.height;
        }
        return 0.0f;
    }

    @Override
    public float getAlignment(int axis) {
        if (this.c != null) {
            switch (axis) {
                case 0: {
                    return this.c.getAlignmentX();
                }
                case 1: {
                    return this.c.getAlignmentY();
                }
            }
        }
        return super.getAlignment(axis);
    }

    @Override
    public void setParent(View p) {
        super.setParent(p);
        if (SwingUtilities.isEventDispatchThread()) {
            this.setComponentParent();
        } else {
            Runnable callSetComponentParent = new Runnable(){

                @Override
                public void run() {
                    Document doc = ComponentView.this.getDocument();
                    try {
                        if (doc instanceof AbstractDocument) {
                            ((AbstractDocument)doc).readLock();
                        }
                        ComponentView.this.setComponentParent();
                        Container host = ComponentView.this.getContainer();
                        if (host != null) {
                            ComponentView.this.preferenceChanged(null, true, true);
                            host.repaint();
                        }
                    }
                    finally {
                        if (doc instanceof AbstractDocument) {
                            ((AbstractDocument)doc).readUnlock();
                        }
                    }
                }
            };
            SwingUtilities.invokeLater(callSetComponentParent);
        }
    }

    void setComponentParent() {
        Container parent;
        View p = this.getParent();
        if (p != null) {
            Container parent2 = this.getContainer();
            if (parent2 != null) {
                Component comp;
                if (this.c == null && (comp = this.createComponent()) != null) {
                    this.createdC = comp;
                    this.c = new Invalidator(comp);
                }
                if (this.c != null && this.c.getParent() == null) {
                    parent2.add((Component)this.c, this);
                    parent2.addPropertyChangeListener("enabled", this.c);
                }
            }
        } else if (this.c != null && (parent = this.c.getParent()) != null) {
            parent.remove(this.c);
            parent.removePropertyChangeListener("enabled", this.c);
        }
    }

    @Override
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        int p0 = this.getStartOffset();
        int p1 = this.getEndOffset();
        if (pos >= p0 && pos <= p1) {
            Rectangle r = a.getBounds();
            if (pos == p1) {
                r.x += r.width;
            }
            r.width = 0;
            return r;
        }
        throw new BadLocationException(pos + " not in range " + p0 + "," + p1, pos);
    }

    @Override
    public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
        Rectangle alloc = (Rectangle)a;
        if (x < (float)(alloc.x + alloc.width / 2)) {
            bias[0] = Position.Bias.Forward;
            return this.getStartOffset();
        }
        bias[0] = Position.Bias.Backward;
        return this.getEndOffset();
    }

    class Invalidator
    extends Container
    implements PropertyChangeListener {
        Dimension min;
        Dimension pref;
        Dimension max;
        float yalign;
        float xalign;

        Invalidator(Component child) {
            this.setLayout(null);
            this.add(child);
            this.cacheChildSizes();
        }

        @Override
        public void invalidate() {
            super.invalidate();
            if (this.getParent() != null) {
                ComponentView.this.preferenceChanged(null, true, true);
            }
        }

        @Override
        public void doLayout() {
            this.cacheChildSizes();
        }

        @Override
        public void setBounds(int x, int y, int w, int h) {
            super.setBounds(x, y, w, h);
            if (this.getComponentCount() > 0) {
                this.getComponent(0).setSize(w, h);
            }
            this.cacheChildSizes();
        }

        public void validateIfNecessary() {
            if (!this.isValid()) {
                this.validate();
            }
        }

        private void cacheChildSizes() {
            if (this.getComponentCount() > 0) {
                Component child = this.getComponent(0);
                this.min = child.getMinimumSize();
                this.pref = child.getPreferredSize();
                this.max = child.getMaximumSize();
                this.yalign = child.getAlignmentY();
                this.xalign = child.getAlignmentX();
            } else {
                this.pref = this.max = new Dimension(0, 0);
                this.min = this.max;
            }
        }

        @Override
        public void setVisible(boolean b) {
            super.setVisible(b);
            if (this.getComponentCount() > 0) {
                this.getComponent(0).setVisible(b);
            }
        }

        @Override
        public boolean isShowing() {
            return true;
        }

        @Override
        public Dimension getMinimumSize() {
            this.validateIfNecessary();
            return this.min;
        }

        @Override
        public Dimension getPreferredSize() {
            this.validateIfNecessary();
            return this.pref;
        }

        @Override
        public Dimension getMaximumSize() {
            this.validateIfNecessary();
            return this.max;
        }

        @Override
        public float getAlignmentX() {
            this.validateIfNecessary();
            return this.xalign;
        }

        @Override
        public float getAlignmentY() {
            this.validateIfNecessary();
            return this.yalign;
        }

        @Override
        public Set<AWTKeyStroke> getFocusTraversalKeys(int id) {
            return KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(id);
        }

        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            Boolean enable = (Boolean)ev.getNewValue();
            if (this.getComponentCount() > 0) {
                this.getComponent(0).setEnabled(enable);
            }
        }
    }
}

