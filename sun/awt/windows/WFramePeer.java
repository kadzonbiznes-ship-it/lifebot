/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.MenuBar;
import java.awt.Rectangle;
import java.awt.peer.FramePeer;
import java.security.AccessController;
import sun.awt.AWTAccessor;
import sun.awt.im.InputMethodManager;
import sun.awt.windows.WComponentPeer;
import sun.awt.windows.WMenuBarPeer;
import sun.awt.windows.WToolkit;
import sun.awt.windows.WWindowPeer;
import sun.java2d.SunGraphicsEnvironment;
import sun.security.action.GetPropertyAction;

class WFramePeer
extends WWindowPeer
implements FramePeer {
    private static final boolean keepOnMinimize;

    private static native void initIDs();

    @Override
    public native void setState(int var1);

    @Override
    public native int getState();

    public void setExtendedState(int state) {
        AWTAccessor.getFrameAccessor().setExtendedState((Frame)this.target, state);
    }

    public int getExtendedState() {
        return AWTAccessor.getFrameAccessor().getExtendedState((Frame)this.target);
    }

    private native void setMaximizedBounds(int var1, int var2, int var3, int var4);

    private native void clearMaximizedBounds();

    @Override
    public final void setMaximizedBounds(Rectangle b) {
        if (b == null) {
            this.clearMaximizedBounds();
        } else {
            b = this.adjustMaximizedBounds(b);
            this.setMaximizedBounds(b.x, b.y, b.width, b.height);
        }
    }

    private Rectangle adjustMaximizedBounds(Rectangle bounds) {
        bounds = SunGraphicsEnvironment.toDeviceSpaceAbs(bounds);
        GraphicsConfiguration gc = this.getGraphicsConfiguration();
        Rectangle currentDevBounds = SunGraphicsEnvironment.getGCDeviceBounds(gc);
        bounds.x -= currentDevBounds.x;
        bounds.y -= currentDevBounds.y;
        return bounds;
    }

    @Override
    public boolean updateGraphicsData(GraphicsConfiguration gc) {
        boolean result = super.updateGraphicsData(gc);
        Rectangle bounds = AWTAccessor.getFrameAccessor().getMaximizedBounds((Frame)this.target);
        if (bounds != null) {
            this.setMaximizedBounds(bounds);
        }
        return result;
    }

    @Override
    boolean isTargetUndecorated() {
        return ((Frame)this.target).isUndecorated();
    }

    @Override
    public void reshape(int x, int y, int width, int height) {
        if (((Frame)this.target).isUndecorated()) {
            super.reshape(x, y, width, height);
        } else {
            this.reshapeFrame(x, y, width, height);
        }
    }

    @Override
    public final Dimension getMinimumSize() {
        GraphicsConfiguration gc = this.getGraphicsConfiguration();
        Dimension d = new Dimension();
        if (!((Frame)this.target).isUndecorated()) {
            d.setSize(SunGraphicsEnvironment.toUserSpace(gc, WFramePeer.getSysMinWidth(), WFramePeer.getSysMinHeight()));
        }
        if (((Frame)this.target).getMenuBar() != null) {
            d.height += SunGraphicsEnvironment.toUserSpace((GraphicsConfiguration)gc, (int)0, (int)WFramePeer.getSysMenuHeight()).height;
        }
        return d;
    }

    @Override
    public void setMenuBar(MenuBar mb) {
        WMenuBarPeer mbPeer = (WMenuBarPeer)WToolkit.targetToPeer(mb);
        if (mbPeer != null) {
            if (mbPeer.framePeer != this) {
                mb.removeNotify();
                mb.addNotify();
                mbPeer = (WMenuBarPeer)WToolkit.targetToPeer(mb);
                if (mbPeer != null && mbPeer.framePeer != this) {
                    throw new IllegalStateException("Wrong parent peer");
                }
            }
            if (mbPeer != null) {
                this.addChildPeer(mbPeer);
            }
        }
        this.setMenuBar0(mbPeer);
        this.updateInsets(this.insets_);
    }

    private native void setMenuBar0(WMenuBarPeer var1);

    WFramePeer(Frame target) {
        super(target);
        InputMethodManager imm = InputMethodManager.getInstance();
        String menuString = imm.getTriggerMenuString();
        if (menuString != null) {
            this.pSetIMMOption(menuString);
        }
    }

    native void createAwtFrame(WComponentPeer var1);

    @Override
    void create(WComponentPeer parent) {
        this.preCreate(parent);
        this.createAwtFrame(parent);
    }

    @Override
    void initialize() {
        super.initialize();
        Frame target = (Frame)this.target;
        if (target.getTitle() != null) {
            this.setTitle(target.getTitle());
        }
        this.setResizable(target.isResizable());
        this.setState(target.getExtendedState());
    }

    private static native int getSysMenuHeight();

    native void pSetIMMOption(String var1);

    void notifyIMMOptionChange() {
        InputMethodManager.getInstance().notifyChangeRequest((Component)this.target);
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
    public void emulateActivation(boolean activate) {
        this.synthesizeWmActivate(activate);
    }

    private native void synthesizeWmActivate(boolean var1);

    static {
        WFramePeer.initIDs();
        keepOnMinimize = "true".equals(AccessController.doPrivileged(new GetPropertyAction("sun.awt.keepWorkingSetOnMinimize")));
    }
}

