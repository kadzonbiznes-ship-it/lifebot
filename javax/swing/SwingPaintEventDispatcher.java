/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.PaintEvent;
import java.security.AccessController;
import javax.swing.RepaintManager;
import javax.swing.RootPaneContainer;
import javax.swing.SwingHeavyWeight;
import sun.awt.AppContext;
import sun.awt.PaintEventDispatcher;
import sun.awt.SunToolkit;
import sun.awt.event.IgnorePaintEvent;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetPropertyAction;

class SwingPaintEventDispatcher
extends PaintEventDispatcher {
    private static final boolean SHOW_FROM_DOUBLE_BUFFER = "true".equals(AccessController.doPrivileged(new GetPropertyAction("swing.showFromDoubleBuffer", "true")));
    private static final boolean ERASE_BACKGROUND = AccessController.doPrivileged(new GetBooleanAction("swing.nativeErase"));

    SwingPaintEventDispatcher() {
    }

    @Override
    public PaintEvent createPaintEvent(Component component, int x, int y, int w, int h) {
        if (component instanceof RootPaneContainer) {
            AppContext appContext = SunToolkit.targetToAppContext(component);
            RepaintManager rm = RepaintManager.currentManager(appContext);
            if (!SHOW_FROM_DOUBLE_BUFFER || !rm.show((Container)component, x, y, w, h)) {
                rm.nativeAddDirtyRegion(appContext, (Container)component, x, y, w, h);
            }
            return new IgnorePaintEvent(component, 800, new Rectangle(x, y, w, h));
        }
        if (component instanceof SwingHeavyWeight) {
            AppContext appContext = SunToolkit.targetToAppContext(component);
            RepaintManager rm = RepaintManager.currentManager(appContext);
            rm.nativeAddDirtyRegion(appContext, (Container)component, x, y, w, h);
            return new IgnorePaintEvent(component, 800, new Rectangle(x, y, w, h));
        }
        return super.createPaintEvent(component, x, y, w, h);
    }

    @Override
    public boolean shouldDoNativeBackgroundErase(Component c) {
        return ERASE_BACKGROUND || !(c instanceof RootPaneContainer);
    }

    @Override
    public boolean queueSurfaceDataReplacing(Component c, Runnable r) {
        if (c instanceof RootPaneContainer) {
            AppContext appContext = SunToolkit.targetToAppContext(c);
            RepaintManager.currentManager(appContext).nativeQueueSurfaceDataRunnable(appContext, c, r);
            return true;
        }
        return super.queueSurfaceDataReplacing(c, r);
    }
}

