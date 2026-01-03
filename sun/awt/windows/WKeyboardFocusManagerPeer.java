/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.peer.ComponentPeer;
import sun.awt.AWTAccessor;
import sun.awt.KeyboardFocusManagerPeerImpl;

final class WKeyboardFocusManagerPeer
extends KeyboardFocusManagerPeerImpl {
    private static final WKeyboardFocusManagerPeer inst = new WKeyboardFocusManagerPeer();

    static native void setNativeFocusOwner(ComponentPeer var0);

    static native Component getNativeFocusOwner();

    static native Window getNativeFocusedWindow();

    public static WKeyboardFocusManagerPeer getInstance() {
        return inst;
    }

    private WKeyboardFocusManagerPeer() {
    }

    @Override
    public void setCurrentFocusOwner(Component comp) {
        AWTAccessor.ComponentAccessor acc = AWTAccessor.getComponentAccessor();
        WKeyboardFocusManagerPeer.setNativeFocusOwner(comp != null ? (ComponentPeer)acc.getPeer(comp) : null);
    }

    @Override
    public Component getCurrentFocusOwner() {
        return WKeyboardFocusManagerPeer.getNativeFocusOwner();
    }

    @Override
    public void setCurrentFocusedWindow(Window win) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Window getCurrentFocusedWindow() {
        return WKeyboardFocusManagerPeer.getNativeFocusedWindow();
    }

    public static boolean deliverFocus(Component lightweightChild, Component target, boolean temporary, boolean focusedWindowChangeAllowed, long time, FocusEvent.Cause cause) {
        return KeyboardFocusManagerPeerImpl.deliverFocus(lightweightChild, target, temporary, focusedWindowChangeAllowed, time, cause, WKeyboardFocusManagerPeer.getNativeFocusOwner());
    }
}

