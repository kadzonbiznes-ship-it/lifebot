/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.im;

import java.awt.Component;
import java.util.Locale;
import sun.awt.im.ExecutableInputMethodManager;
import sun.awt.im.InputContext;
import sun.awt.im.InputMethodLocator;

public abstract class InputMethodManager {
    private static final String threadName = "AWT-InputMethodManager";
    private static final Object LOCK = new Object();
    private static InputMethodManager inputMethodManager;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final InputMethodManager getInstance() {
        if (inputMethodManager != null) {
            return inputMethodManager;
        }
        Object object = LOCK;
        synchronized (object) {
            if (inputMethodManager == null) {
                ExecutableInputMethodManager imm = new ExecutableInputMethodManager();
                if (imm.hasMultipleInputMethods()) {
                    imm.initialize();
                    Thread immThread = new Thread(null, imm, threadName, 0L, false);
                    immThread.setDaemon(true);
                    immThread.setPriority(6);
                    immThread.start();
                }
                inputMethodManager = imm;
            }
        }
        return inputMethodManager;
    }

    public abstract String getTriggerMenuString();

    public abstract void notifyChangeRequest(Component var1);

    public abstract void notifyChangeRequestByHotKey(Component var1);

    abstract void setInputContext(InputContext var1);

    abstract InputMethodLocator findInputMethod(Locale var1);

    abstract Locale getDefaultKeyboardLocale();

    abstract boolean hasMultipleInputMethods();
}

