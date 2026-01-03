/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import sun.awt.ModalExclude;

public class Popup {
    private Component component;

    protected Popup(Component owner, Component contents, int x, int y) {
        this();
        if (contents == null) {
            throw new IllegalArgumentException("Contents must be non-null");
        }
        this.reset(owner, contents, x, y);
    }

    protected Popup() {
    }

    public void show() {
        Component component = this.getComponent();
        if (component != null) {
            component.show();
        }
    }

    public void hide() {
        Component component = this.getComponent();
        if (component instanceof JWindow) {
            component.hide();
            ((JWindow)component).getContentPane().removeAll();
        }
        this.dispose();
    }

    void dispose() {
        Component component = this.getComponent();
        Window window = SwingUtilities.getWindowAncestor(component);
        if (component instanceof JWindow) {
            ((Window)component).dispose();
            component = null;
        }
        if (window instanceof DefaultFrame) {
            window.dispose();
        }
    }

    void reset(Component owner, Component contents, int ownerX, int ownerY) {
        Component c;
        if (this.getComponent() == null) {
            this.component = this.createComponent(owner);
        }
        if ((c = this.getComponent()) instanceof JWindow) {
            JWindow component = (JWindow)this.getComponent();
            component.setBounds(ownerX, ownerY, 1, 1);
            component.getContentPane().add(contents, "Center");
            component.invalidate();
            component.validate();
            if (component.isVisible()) {
                this.pack();
            }
        }
    }

    void pack() {
        Component component = this.getComponent();
        if (component instanceof Window) {
            ((Window)component).pack();
        }
    }

    private Window getParentWindow(Component owner) {
        Window window = null;
        if (owner instanceof Window) {
            window = (Window)owner;
        } else if (owner != null) {
            window = SwingUtilities.getWindowAncestor(owner);
        }
        if (window == null) {
            window = new DefaultFrame();
        }
        return window;
    }

    Component createComponent(Component owner) {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        return new HeavyWeightWindow(this.getParentWindow(owner));
    }

    Component getComponent() {
        return this.component;
    }

    static class DefaultFrame
    extends Frame {
        DefaultFrame() {
        }
    }

    static class HeavyWeightWindow
    extends JWindow
    implements ModalExclude {
        HeavyWeightWindow(Window parent) {
            super(parent);
            this.setFocusableWindowState(false);
            this.setType(Window.Type.POPUP);
            this.getRootPane().setUseTrueDoubleBuffering(false);
            try {
                this.setAlwaysOnTop(true);
            }
            catch (SecurityException securityException) {
                // empty catch block
            }
        }

        @Override
        public void update(Graphics g) {
            this.paint(g);
        }

        @Override
        public void show() {
            this.pack();
            if (this.getWidth() > 0 && this.getHeight() > 0) {
                super.show();
            }
        }
    }
}

