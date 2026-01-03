/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.DefaultFocusTraversalPolicy;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyboardFocusManager;
import javax.swing.DelegatingDefaultFocusManager;

public abstract class FocusManager
extends DefaultKeyboardFocusManager {
    public static final String FOCUS_MANAGER_CLASS_PROPERTY = "FocusManagerClassName";
    private static boolean enabled = true;

    protected FocusManager() {
    }

    public static FocusManager getCurrentManager() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        if (manager instanceof FocusManager) {
            return (FocusManager)manager;
        }
        return new DelegatingDefaultFocusManager(manager);
    }

    public static void setCurrentManager(FocusManager aFocusManager) throws SecurityException {
        FocusManager toSet = aFocusManager instanceof DelegatingDefaultFocusManager ? ((DelegatingDefaultFocusManager)aFocusManager).getDelegate() : aFocusManager;
        KeyboardFocusManager.setCurrentKeyboardFocusManager(toSet);
    }

    @Deprecated
    public static void disableSwingFocusManager() {
        if (enabled) {
            enabled = false;
            KeyboardFocusManager.getCurrentKeyboardFocusManager().setDefaultFocusTraversalPolicy(new DefaultFocusTraversalPolicy());
        }
    }

    @Deprecated
    public static boolean isFocusManagerEnabled() {
        return enabled;
    }
}

