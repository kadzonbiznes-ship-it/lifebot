/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MenuBar;
import java.awt.MenuComponent;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.peer.FramePeer;
import java.util.List;
import sun.awt.AWTAccessor;
import sun.awt.SunToolkit;

public abstract class LightweightFrame
extends Frame {
    private int hostX;
    private int hostY;
    private int hostW;
    private int hostH;

    public LightweightFrame() {
        this.setUndecorated(true);
        this.setResizable(true);
        this.setEnabled(true);
    }

    @Override
    public final Container getParent() {
        return null;
    }

    @Override
    public Graphics getGraphics() {
        return null;
    }

    @Override
    public final boolean isResizable() {
        return true;
    }

    @Override
    public final void setTitle(String title) {
    }

    @Override
    public final void setIconImage(Image image) {
    }

    @Override
    public final void setIconImages(List<? extends Image> icons) {
    }

    @Override
    public final void setMenuBar(MenuBar mb) {
    }

    @Override
    public final void setResizable(boolean resizable) {
    }

    @Override
    public final void remove(MenuComponent m) {
    }

    @Override
    public final void toFront() {
    }

    @Override
    public final void toBack() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addNotify() {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (!this.isDisplayable()) {
                SunToolkit stk = (SunToolkit)Toolkit.getDefaultToolkit();
                try {
                    this.setPeer(stk.createLightweightFrame(this));
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            super.addNotify();
        }
    }

    private void setPeer(FramePeer p) {
        AWTAccessor.getComponentAccessor().setPeer(this, p);
    }

    public void emulateActivation(boolean activate) {
        FramePeer peer = (FramePeer)AWTAccessor.getComponentAccessor().getPeer(this);
        peer.emulateActivation(activate);
    }

    public abstract void grabFocus();

    public abstract void ungrabFocus();

    @Deprecated(since="9")
    public abstract int getScaleFactor();

    public abstract double getScaleFactorX();

    public abstract double getScaleFactorY();

    @Deprecated(since="9")
    public abstract void notifyDisplayChanged(int var1);

    public abstract void notifyDisplayChanged(double var1, double var3);

    public Rectangle getHostBounds() {
        if (this.hostX == 0 && this.hostY == 0 && this.hostW == 0 && this.hostH == 0) {
            return this.getBounds();
        }
        return new Rectangle(this.hostX, this.hostY, this.hostW, this.hostH);
    }

    public void setHostBounds(int x, int y, int w, int h) {
        this.hostX = x;
        this.hostY = y;
        this.hostW = w;
        this.hostH = h;
    }

    public abstract <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> var1, DragSource var2, Component var3, int var4, DragGestureListener var5);

    public abstract DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent var1) throws InvalidDnDOperationException;

    public abstract void addDropTarget(DropTarget var1);

    public abstract void removeDropTarget(DropTarget var1);
}

