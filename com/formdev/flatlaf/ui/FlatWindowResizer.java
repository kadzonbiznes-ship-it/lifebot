/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.ui.FlatWindowResizer$DragBorderComponent
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.FlatWindowResizer;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.UIManager;

public abstract class FlatWindowResizer
implements PropertyChangeListener,
ComponentListener {
    protected static final Integer WINDOW_RESIZER_LAYER = JLayeredPane.DRAG_LAYER + 1;
    protected final JComponent resizeComp;
    protected final int borderDragThickness = FlatUIUtils.getUIInt("RootPane.borderDragThickness", 5);
    protected final int cornerDragWidth = FlatUIUtils.getUIInt("RootPane.cornerDragWidth", 16);
    protected final boolean honorFrameMinimumSizeOnResize = UIManager.getBoolean("RootPane.honorFrameMinimumSizeOnResize");
    protected final boolean honorDialogMinimumSizeOnResize = UIManager.getBoolean("RootPane.honorDialogMinimumSizeOnResize");
    protected final DragBorderComponent topDragComp;
    protected final DragBorderComponent bottomDragComp;
    protected final DragBorderComponent leftDragComp;
    protected final DragBorderComponent rightDragComp;

    protected FlatWindowResizer(JComponent resizeComp) {
        this.resizeComp = resizeComp;
        this.topDragComp = this.createDragBorderComponent(6, 8, 7);
        this.bottomDragComp = this.createDragBorderComponent(4, 9, 5);
        this.leftDragComp = this.createDragBorderComponent(6, 10, 4);
        this.rightDragComp = this.createDragBorderComponent(7, 11, 5);
        JComponent cont = resizeComp instanceof JRootPane ? ((JRootPane)resizeComp).getLayeredPane() : resizeComp;
        Integer cons = cont instanceof JLayeredPane ? WINDOW_RESIZER_LAYER : null;
        cont.add((Component)this.topDragComp, cons, 0);
        cont.add((Component)this.bottomDragComp, cons, 1);
        cont.add((Component)this.leftDragComp, cons, 2);
        cont.add((Component)this.rightDragComp, cons, 3);
        resizeComp.addComponentListener(this);
        resizeComp.addPropertyChangeListener("ancestor", this);
        if (resizeComp.isDisplayable()) {
            this.addNotify();
        }
    }

    protected DragBorderComponent createDragBorderComponent(int leadingResizeDir, int centerResizeDir, int trailingResizeDir) {
        return new DragBorderComponent(this, leadingResizeDir, centerResizeDir, trailingResizeDir);
    }

    public void uninstall() {
        this.removeNotify();
        this.resizeComp.removeComponentListener(this);
        this.resizeComp.removePropertyChangeListener("ancestor", this);
        Container cont = this.topDragComp.getParent();
        cont.remove((Component)this.topDragComp);
        cont.remove((Component)this.bottomDragComp);
        cont.remove((Component)this.leftDragComp);
        cont.remove((Component)this.rightDragComp);
    }

    public void doLayout() {
        if (!this.topDragComp.isVisible()) {
            return;
        }
        int x = 0;
        int y = 0;
        int width = this.resizeComp.getWidth();
        int height = this.resizeComp.getHeight();
        if (width == 0 || height == 0) {
            return;
        }
        Insets resizeInsets = this.getResizeInsets();
        int thickness = UIScale.scale(this.borderDragThickness);
        int topThickness = Math.max(resizeInsets.top, thickness);
        int bottomThickness = Math.max(resizeInsets.bottom, thickness);
        int leftThickness = Math.max(resizeInsets.left, thickness);
        int rightThickness = Math.max(resizeInsets.right, thickness);
        int y2 = y + topThickness;
        int height2 = height - topThickness - bottomThickness;
        this.topDragComp.setBounds(x, y, width, topThickness);
        this.bottomDragComp.setBounds(x, y + height - bottomThickness, width, bottomThickness);
        this.leftDragComp.setBounds(x, y2, leftThickness, height2);
        this.rightDragComp.setBounds(x + width - rightThickness, y2, rightThickness, height2);
        int cornerDelta = UIScale.scale(this.cornerDragWidth - this.borderDragThickness);
        this.topDragComp.setCornerDragWidths(leftThickness + cornerDelta, rightThickness + cornerDelta);
        this.bottomDragComp.setCornerDragWidths(leftThickness + cornerDelta, rightThickness + cornerDelta);
        this.leftDragComp.setCornerDragWidths(cornerDelta, cornerDelta);
        this.rightDragComp.setCornerDragWidths(cornerDelta, cornerDelta);
    }

    protected Insets getResizeInsets() {
        return new Insets(0, 0, 0, 0);
    }

    protected void addNotify() {
        this.updateVisibility();
    }

    protected void removeNotify() {
        this.updateVisibility();
    }

    protected void updateVisibility() {
        boolean visible = this.isWindowResizable();
        if (visible == this.topDragComp.isVisible()) {
            return;
        }
        this.topDragComp.setVisible(visible);
        this.bottomDragComp.setVisible(visible);
        this.leftDragComp.setVisible(visible);
        this.rightDragComp.setEnabled(visible);
        if (visible) {
            this.rightDragComp.setVisible(true);
            this.doLayout();
        } else {
            this.rightDragComp.setBounds(0, 0, 1, 1);
        }
    }

    boolean isDialog() {
        return false;
    }

    protected abstract boolean isWindowResizable();

    protected abstract Rectangle getWindowBounds();

    protected abstract void setWindowBounds(Rectangle var1);

    protected abstract boolean limitToParentBounds();

    protected abstract Rectangle getParentBounds();

    protected abstract boolean honorMinimumSizeOnResize();

    protected abstract boolean honorMaximumSizeOnResize();

    protected abstract Dimension getWindowMinimumSize();

    protected abstract Dimension getWindowMaximumSize();

    protected void beginResizing(int direction) {
    }

    protected void endResizing() {
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        switch (e.getPropertyName()) {
            case "ancestor": {
                if (e.getNewValue() != null) {
                    this.addNotify();
                    break;
                }
                this.removeNotify();
                break;
            }
            case "resizable": {
                this.updateVisibility();
            }
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        this.doLayout();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    public static class WindowResizer
    extends FlatWindowResizer
    implements WindowStateListener {
        protected Window window;
        private final boolean limitResizeToScreenBounds = SystemInfo.isLinux;

        public WindowResizer(JRootPane rootPane) {
            super(rootPane);
        }

        @Override
        protected void addNotify() {
            Container parent = this.resizeComp.getParent();
            Window window = this.window = parent instanceof Window ? (Window)parent : null;
            if (this.window instanceof Frame) {
                this.window.addPropertyChangeListener("resizable", this);
                this.window.addWindowStateListener(this);
            }
            super.addNotify();
        }

        @Override
        protected void removeNotify() {
            if (this.window instanceof Frame) {
                this.window.removePropertyChangeListener("resizable", this);
                this.window.removeWindowStateListener(this);
            }
            this.window = null;
            super.removeNotify();
        }

        @Override
        protected boolean isWindowResizable() {
            if (FlatUIUtils.isFullScreen(this.resizeComp)) {
                return false;
            }
            if (this.window instanceof Frame) {
                return ((Frame)this.window).isResizable() && (((Frame)this.window).getExtendedState() & 6) == 0;
            }
            if (this.window instanceof Dialog) {
                return ((Dialog)this.window).isResizable();
            }
            return false;
        }

        @Override
        protected Rectangle getWindowBounds() {
            return this.window.getBounds();
        }

        @Override
        protected void setWindowBounds(Rectangle r) {
            this.window.setBounds(r);
            this.doLayout();
            if (Toolkit.getDefaultToolkit().isDynamicLayoutActive()) {
                this.window.validate();
                this.resizeComp.repaint();
            }
        }

        @Override
        protected boolean limitToParentBounds() {
            return this.limitResizeToScreenBounds && this.window != null;
        }

        @Override
        protected Rectangle getParentBounds() {
            if (this.limitResizeToScreenBounds && this.window != null) {
                GraphicsConfiguration gc = this.window.getGraphicsConfiguration();
                Rectangle bounds = gc.getBounds();
                Insets insets = this.window.getToolkit().getScreenInsets(gc);
                return new Rectangle(bounds.x + insets.left, bounds.y + insets.top, bounds.width - insets.left - insets.right, bounds.height - insets.top - insets.bottom);
            }
            return null;
        }

        @Override
        protected boolean honorMinimumSizeOnResize() {
            return this.honorFrameMinimumSizeOnResize && this.window instanceof Frame || this.honorDialogMinimumSizeOnResize && this.window instanceof Dialog;
        }

        @Override
        protected boolean honorMaximumSizeOnResize() {
            return false;
        }

        @Override
        protected Dimension getWindowMinimumSize() {
            return this.window.getMinimumSize();
        }

        @Override
        protected Dimension getWindowMaximumSize() {
            return this.window.getMaximumSize();
        }

        @Override
        boolean isDialog() {
            return this.window instanceof Dialog;
        }

        @Override
        public void windowStateChanged(WindowEvent e) {
            this.updateVisibility();
        }
    }
}

