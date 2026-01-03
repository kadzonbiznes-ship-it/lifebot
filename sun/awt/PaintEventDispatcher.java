/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.PaintEvent;

public class PaintEventDispatcher {
    private static PaintEventDispatcher dispatcher;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setPaintEventDispatcher(PaintEventDispatcher dispatcher) {
        Class<PaintEventDispatcher> clazz = PaintEventDispatcher.class;
        synchronized (PaintEventDispatcher.class) {
            PaintEventDispatcher.dispatcher = dispatcher;
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static PaintEventDispatcher getPaintEventDispatcher() {
        Class<PaintEventDispatcher> clazz = PaintEventDispatcher.class;
        synchronized (PaintEventDispatcher.class) {
            if (dispatcher == null) {
                dispatcher = new PaintEventDispatcher();
            }
            // ** MonitorExit[var0] (shouldn't be in output)
            return dispatcher;
        }
    }

    public PaintEvent createPaintEvent(Component target, int x, int y, int w, int h) {
        return new PaintEvent(target, 800, new Rectangle(x, y, w, h));
    }

    public boolean shouldDoNativeBackgroundErase(Component c) {
        return true;
    }

    public boolean queueSurfaceDataReplacing(Component c, Runnable r) {
        return false;
    }
}

