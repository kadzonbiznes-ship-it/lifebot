/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.AWTError;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.beans.BeanProperty;
import java.beans.ConstructorProperties;
import java.beans.JavaBean;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

@JavaBean(defaultProperty="accessibleContext")
public class Box
extends JComponent
implements Accessible {
    public Box(int axis) {
        super.setLayout(new BoxLayout(this, axis));
    }

    public static Box createHorizontalBox() {
        return new Box(0);
    }

    public static Box createVerticalBox() {
        return new Box(1);
    }

    public static Component createRigidArea(Dimension d) {
        return new Filler(d, d, d);
    }

    public static Component createHorizontalStrut(int width) {
        return new Filler(new Dimension(width, 0), new Dimension(width, 0), new Dimension(width, Short.MAX_VALUE));
    }

    public static Component createVerticalStrut(int height) {
        return new Filler(new Dimension(0, height), new Dimension(0, height), new Dimension(Short.MAX_VALUE, height));
    }

    public static Component createGlue() {
        return new Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
    }

    public static Component createHorizontalGlue() {
        return new Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(Short.MAX_VALUE, 0));
    }

    public static Component createVerticalGlue() {
        return new Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, Short.MAX_VALUE));
    }

    @Override
    public void setLayout(LayoutManager l) {
        throw new AWTError("Illegal request");
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (this.ui != null) {
            super.paintComponent(g);
        } else if (this.isOpaque()) {
            g.setColor(this.getBackground());
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleBox();
        }
        return this.accessibleContext;
    }

    public static class Filler
    extends JComponent
    implements Accessible {
        @ConstructorProperties(value={"minimumSize", "preferredSize", "maximumSize"})
        public Filler(Dimension min, Dimension pref, Dimension max) {
            this.setMinimumSize(min);
            this.setPreferredSize(pref);
            this.setMaximumSize(max);
            this.setFocusable(false);
        }

        public void changeShape(Dimension min, Dimension pref, Dimension max) {
            this.setMinimumSize(min);
            this.setPreferredSize(pref);
            this.setMaximumSize(max);
            this.revalidate();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (this.ui != null) {
                super.paintComponent(g);
            } else if (this.isOpaque()) {
                g.setColor(this.getBackground());
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
            }
        }

        @Override
        public AccessibleContext getAccessibleContext() {
            if (this.accessibleContext == null) {
                this.accessibleContext = new AccessibleBoxFiller();
            }
            return this.accessibleContext;
        }

        protected class AccessibleBoxFiller
        extends Component.AccessibleAWTComponent {
            protected AccessibleBoxFiller() {
            }

            @Override
            public AccessibleRole getAccessibleRole() {
                return AccessibleRole.FILLER;
            }
        }
    }

    protected class AccessibleBox
    extends Container.AccessibleAWTContainer {
        protected AccessibleBox() {
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.FILLER;
        }
    }
}

