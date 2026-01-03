/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.peer.ComponentPeer;
import java.awt.peer.KeyboardFocusManagerPeer;
import sun.awt.AWTAccessor;
import sun.awt.SunToolkit;
import sun.util.logging.PlatformLogger;

public abstract class KeyboardFocusManagerPeerImpl
implements KeyboardFocusManagerPeer {
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("sun.awt.focus.KeyboardFocusManagerPeerImpl");
    public static final int SNFH_FAILURE = 0;
    public static final int SNFH_SUCCESS_HANDLED = 1;
    public static final int SNFH_SUCCESS_PROCEED = 2;

    @Override
    public void clearGlobalFocusOwner(Window activeWindow) {
        if (activeWindow != null) {
            Component focusOwner = activeWindow.getFocusOwner();
            if (focusLog.isLoggable(PlatformLogger.Level.FINE)) {
                focusLog.fine("Clearing global focus owner " + String.valueOf(focusOwner));
            }
            if (focusOwner != null) {
                FocusEvent fl = new FocusEvent(focusOwner, 1005, false, null, FocusEvent.Cause.CLEAR_GLOBAL_FOCUS_OWNER);
                SunToolkit.postPriorityEvent(fl);
            }
        }
    }

    public static boolean shouldFocusOnClick(Component component) {
        boolean acceptFocusOnClick = false;
        AWTAccessor.ComponentAccessor acc = AWTAccessor.getComponentAccessor();
        if (component instanceof Canvas || component instanceof Scrollbar) {
            acceptFocusOnClick = true;
        } else if (component instanceof Panel) {
            acceptFocusOnClick = ((Panel)component).getComponentCount() == 0;
        } else {
            ComponentPeer peer = component != null ? (ComponentPeer)acc.getPeer(component) : null;
            acceptFocusOnClick = peer != null ? peer.isFocusable() : false;
        }
        return acceptFocusOnClick && acc.canBeFocusOwner(component);
    }

    public static boolean deliverFocus(Component lightweightChild, Component target, boolean temporary, boolean focusedWindowChangeAllowed, long time, FocusEvent.Cause cause, Component currentFocusOwner) {
        Component currentOwner;
        if (lightweightChild == null) {
            lightweightChild = target;
        }
        if ((currentOwner = currentFocusOwner) != null && !currentOwner.isDisplayable()) {
            currentOwner = null;
        }
        if (currentOwner != null) {
            FocusEvent fl = new FocusEvent(currentOwner, 1005, false, lightweightChild, cause);
            if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                focusLog.finer("Posting focus event: " + String.valueOf(fl));
            }
            SunToolkit.postEvent(SunToolkit.targetToAppContext(currentOwner), fl);
        }
        FocusEvent fg = new FocusEvent(lightweightChild, 1004, false, currentOwner, cause);
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("Posting focus event: " + String.valueOf(fg));
        }
        SunToolkit.postEvent(SunToolkit.targetToAppContext(lightweightChild), fg);
        return true;
    }

    public static void requestFocusFor(Component target, FocusEvent.Cause cause) {
        AWTAccessor.getComponentAccessor().requestFocus(target, cause);
    }

    public static int shouldNativelyFocusHeavyweight(Component heavyweight, Component descendant, boolean temporary, boolean focusedWindowChangeAllowed, long time, FocusEvent.Cause cause) {
        return KfmAccessor.instance.shouldNativelyFocusHeavyweight(heavyweight, descendant, temporary, focusedWindowChangeAllowed, time, cause);
    }

    public static void removeLastFocusRequest(Component heavyweight) {
        KfmAccessor.instance.removeLastFocusRequest(heavyweight);
    }

    public static boolean processSynchronousLightweightTransfer(Component heavyweight, Component descendant, boolean temporary, boolean focusedWindowChangeAllowed, long time) {
        return KfmAccessor.instance.processSynchronousLightweightTransfer(heavyweight, descendant, temporary, focusedWindowChangeAllowed, time);
    }

    private static class KfmAccessor {
        private static AWTAccessor.KeyboardFocusManagerAccessor instance = AWTAccessor.getKeyboardFocusManagerAccessor();

        private KfmAccessor() {
        }
    }
}

