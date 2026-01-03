/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.applet.Applet;
import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MenuBar;
import java.awt.MenuComponent;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.peer.ComponentPeer;
import java.awt.peer.FramePeer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Set;
import sun.awt.AWTAccessor;
import sun.awt.NullComponentPeer;
import sun.awt.SunToolkit;

public abstract class EmbeddedFrame
extends Frame
implements KeyEventDispatcher,
PropertyChangeListener {
    private boolean isCursorAllowed = true;
    private boolean supportsXEmbed = false;
    private KeyboardFocusManager appletKFM;
    private static final long serialVersionUID = 2967042741780317130L;
    protected static final boolean FORWARD = true;
    protected static final boolean BACKWARD = false;

    public boolean supportsXEmbed() {
        return this.supportsXEmbed && SunToolkit.needsXEmbed();
    }

    protected EmbeddedFrame(boolean supportsXEmbed) {
        this(0L, supportsXEmbed);
    }

    protected EmbeddedFrame() {
        this(0L);
    }

    @Deprecated
    protected EmbeddedFrame(int handle) {
        this((long)handle);
    }

    protected EmbeddedFrame(long handle) {
        this(handle, false);
    }

    protected EmbeddedFrame(long handle, boolean supportsXEmbed) {
        this.supportsXEmbed = supportsXEmbed;
        this.registerListeners();
    }

    @Override
    public Container getParent() {
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!evt.getPropertyName().equals("managingFocus")) {
            return;
        }
        if (evt.getNewValue() == Boolean.TRUE) {
            return;
        }
        this.removeTraversingOutListeners((KeyboardFocusManager)evt.getSource());
        this.appletKFM = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        if (this.isVisible()) {
            this.addTraversingOutListeners(this.appletKFM);
        }
    }

    private void addTraversingOutListeners(KeyboardFocusManager kfm) {
        kfm.addKeyEventDispatcher(this);
        kfm.addPropertyChangeListener("managingFocus", this);
    }

    private void removeTraversingOutListeners(KeyboardFocusManager kfm) {
        kfm.removeKeyEventDispatcher(this);
        kfm.removePropertyChangeListener("managingFocus", this);
    }

    public void registerListeners() {
        if (this.appletKFM != null) {
            this.removeTraversingOutListeners(this.appletKFM);
        }
        this.appletKFM = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        if (this.isVisible()) {
            this.addTraversingOutListeners(this.appletKFM);
        }
    }

    @Override
    public void show() {
        if (this.appletKFM != null) {
            this.addTraversingOutListeners(this.appletKFM);
        }
        super.show();
    }

    @Override
    public void hide() {
        if (this.appletKFM != null) {
            this.removeTraversingOutListeners(this.appletKFM);
        }
        super.hide();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        Component first;
        Component last;
        Container currentRoot = AWTAccessor.getKeyboardFocusManagerAccessor().getCurrentFocusCycleRoot();
        if (this != currentRoot) {
            return false;
        }
        if (e.getID() == 400) {
            return false;
        }
        if (!this.getFocusTraversalKeysEnabled() || e.isConsumed()) {
            return false;
        }
        AWTKeyStroke stroke = AWTKeyStroke.getAWTKeyStrokeForEvent(e);
        Component currentFocused = e.getComponent();
        Set<AWTKeyStroke> toTest = this.getFocusTraversalKeys(0);
        if (toTest.contains(stroke) && (currentFocused == (last = this.getFocusTraversalPolicy().getLastComponent(this)) || last == null) && this.traverseOut(true)) {
            e.consume();
            return true;
        }
        toTest = this.getFocusTraversalKeys(1);
        if (toTest.contains(stroke) && (currentFocused == (first = this.getFocusTraversalPolicy().getFirstComponent(this)) || first == null) && this.traverseOut(false)) {
            e.consume();
            return true;
        }
        return false;
    }

    public boolean traverseIn(boolean direction) {
        Component comp = null;
        comp = direction ? this.getFocusTraversalPolicy().getFirstComponent(this) : this.getFocusTraversalPolicy().getLastComponent(this);
        if (comp != null) {
            AWTAccessor.getKeyboardFocusManagerAccessor().setMostRecentFocusOwner(this, comp);
            this.synthesizeWindowActivation(true);
        }
        return null != comp;
    }

    protected boolean traverseOut(boolean direction) {
        return false;
    }

    @Override
    public void setTitle(String title) {
    }

    @Override
    public void setIconImage(Image image) {
    }

    @Override
    public void setIconImages(List<? extends Image> icons) {
    }

    @Override
    public void setMenuBar(MenuBar mb) {
    }

    @Override
    public void setResizable(boolean resizable) {
    }

    @Override
    public void remove(MenuComponent m) {
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addNotify() {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (!this.isDisplayable()) {
                this.setPeer(new NullEmbeddedFramePeer());
            }
            super.addNotify();
        }
    }

    public void setCursorAllowed(boolean isCursorAllowed) {
        this.isCursorAllowed = isCursorAllowed;
        FramePeer peer = (FramePeer)AWTAccessor.getComponentAccessor().getPeer(this);
        peer.updateCursorImmediately();
    }

    public boolean isCursorAllowed() {
        return this.isCursorAllowed;
    }

    @Override
    public Cursor getCursor() {
        return this.isCursorAllowed ? super.getCursor() : Cursor.getPredefinedCursor(0);
    }

    protected void setPeer(ComponentPeer p) {
        AWTAccessor.getComponentAccessor().setPeer(this, p);
    }

    public void synthesizeWindowActivation(boolean doActivate) {
    }

    protected void setLocationPrivate(int x, int y) {
        Dimension size = this.getSize();
        this.setBoundsPrivate(x, y, size.width, size.height);
    }

    protected Point getLocationPrivate() {
        Rectangle bounds = this.getBoundsPrivate();
        return new Point(bounds.x, bounds.y);
    }

    protected void setBoundsPrivate(int x, int y, int width, int height) {
        FramePeer peer = (FramePeer)AWTAccessor.getComponentAccessor().getPeer(this);
        if (peer != null) {
            peer.setBoundsPrivate(x, y, width, height);
        }
    }

    protected Rectangle getBoundsPrivate() {
        FramePeer peer = (FramePeer)AWTAccessor.getComponentAccessor().getPeer(this);
        if (peer != null) {
            return peer.getBoundsPrivate();
        }
        return this.getBounds();
    }

    @Override
    public void toFront() {
    }

    @Override
    public void toBack() {
    }

    public abstract void registerAccelerator(AWTKeyStroke var1);

    public abstract void unregisterAccelerator(AWTKeyStroke var1);

    @Deprecated(since="9", forRemoval=true)
    public static Applet getAppletIfAncestorOf(Component comp) {
        Container parent;
        Applet applet = null;
        for (parent = comp.getParent(); parent != null && !(parent instanceof EmbeddedFrame); parent = parent.getParent()) {
            if (!(parent instanceof Applet)) continue;
            applet = (Applet)parent;
        }
        return parent == null ? null : applet;
    }

    public void notifyModalBlocked(Dialog blocker, boolean blocked) {
    }

    private static class NullEmbeddedFramePeer
    extends NullComponentPeer
    implements FramePeer {
        private NullEmbeddedFramePeer() {
        }

        @Override
        public void setTitle(String title) {
        }

        public void setIconImage(Image im) {
        }

        @Override
        public void updateIconImages() {
        }

        @Override
        public void setMenuBar(MenuBar mb) {
        }

        @Override
        public void setResizable(boolean resizeable) {
        }

        @Override
        public void setState(int state) {
        }

        @Override
        public int getState() {
            return 0;
        }

        @Override
        public void setMaximizedBounds(Rectangle b) {
        }

        @Override
        public void toFront() {
        }

        @Override
        public void toBack() {
        }

        @Override
        public void updateFocusableWindowState() {
        }

        public void updateAlwaysOnTop() {
        }

        @Override
        public void updateAlwaysOnTopState() {
        }

        public Component getGlobalHeavyweightFocusOwner() {
            return null;
        }

        @Override
        public void setBoundsPrivate(int x, int y, int width, int height) {
            this.setBounds(x, y, width, height, 3);
        }

        @Override
        public Rectangle getBoundsPrivate() {
            return this.getBounds();
        }

        @Override
        public void setModalBlocked(Dialog blocker, boolean blocked) {
        }

        public void restack() {
            throw new UnsupportedOperationException();
        }

        public boolean isRestackSupported() {
            return false;
        }

        public boolean requestWindowFocus() {
            return false;
        }

        @Override
        public void updateMinimumSize() {
        }

        @Override
        public void setOpacity(float opacity) {
        }

        @Override
        public void setOpaque(boolean isOpaque) {
        }

        @Override
        public void updateWindow() {
        }

        @Override
        public void repositionSecurityWarning() {
        }

        @Override
        public void emulateActivation(boolean activate) {
        }
    }
}

