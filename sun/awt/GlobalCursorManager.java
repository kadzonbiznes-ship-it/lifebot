/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.InvocationEvent;
import sun.awt.AWTAccessor;
import sun.awt.SunToolkit;

public abstract class GlobalCursorManager {
    private final NativeUpdater nativeUpdater = new NativeUpdater();
    private long lastUpdateMillis;
    private final Object lastUpdateLock = new Object();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void updateCursorImmediately() {
        NativeUpdater nativeUpdater = this.nativeUpdater;
        synchronized (nativeUpdater) {
            this.nativeUpdater.pending = false;
        }
        this._updateCursor(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void updateCursorImmediately(InputEvent e) {
        boolean shouldUpdate;
        Object object = this.lastUpdateLock;
        synchronized (object) {
            shouldUpdate = e.getWhen() >= this.lastUpdateMillis;
        }
        if (shouldUpdate) {
            this._updateCursor(true);
        }
    }

    public void updateCursorLater(Component heavy) {
        this.nativeUpdater.postIfNotPending(heavy, new InvocationEvent((Object)Toolkit.getDefaultToolkit(), this.nativeUpdater));
    }

    protected GlobalCursorManager() {
    }

    protected abstract void setCursor(Component var1, Cursor var2, boolean var3);

    protected abstract void getCursorPos(Point var1);

    protected abstract Point getLocationOnScreen(Component var1);

    protected abstract Component findHeavyweightUnderCursor(boolean var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void _updateCursor(boolean useCache) {
        Object object = this.lastUpdateLock;
        synchronized (object) {
            this.lastUpdateMillis = System.currentTimeMillis();
        }
        Point queryPos = null;
        Point p = null;
        try {
            Component comp = this.findHeavyweightUnderCursor(useCache);
            if (comp == null) {
                this.updateCursorOutOfJava();
                return;
            }
            if (comp instanceof Window) {
                p = AWTAccessor.getComponentAccessor().getLocation(comp);
            } else if (comp instanceof Container) {
                p = this.getLocationOnScreen(comp);
            }
            if (p != null) {
                queryPos = new Point();
                this.getCursorPos(queryPos);
                Component c = AWTAccessor.getContainerAccessor().findComponentAt((Container)comp, queryPos.x - p.x, queryPos.y - p.y, false);
                if (c != null) {
                    comp = c;
                }
            }
            this.setCursor(comp, AWTAccessor.getComponentAccessor().getCursor(comp), useCache);
        }
        catch (IllegalComponentStateException illegalComponentStateException) {
            // empty catch block
        }
    }

    protected void updateCursorOutOfJava() {
    }

    class NativeUpdater
    implements Runnable {
        boolean pending = false;

        NativeUpdater() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            boolean shouldUpdate = false;
            NativeUpdater nativeUpdater = this;
            synchronized (nativeUpdater) {
                if (this.pending) {
                    this.pending = false;
                    shouldUpdate = true;
                }
            }
            if (shouldUpdate) {
                GlobalCursorManager.this._updateCursor(false);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void postIfNotPending(Component heavy, InvocationEvent in) {
            boolean shouldPost = false;
            NativeUpdater nativeUpdater = this;
            synchronized (nativeUpdater) {
                if (!this.pending) {
                    shouldPost = true;
                    this.pending = true;
                }
            }
            if (shouldPost) {
                SunToolkit.postEvent(SunToolkit.targetToAppContext(heavy), in);
            }
        }
    }
}

