/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import sun.awt.GlobalCursorManager;

final class WGlobalCursorManager
extends GlobalCursorManager {
    private static WGlobalCursorManager manager;

    WGlobalCursorManager() {
    }

    public static GlobalCursorManager getCursorManager() {
        if (manager == null) {
            manager = new WGlobalCursorManager();
        }
        return manager;
    }

    public static void nativeUpdateCursor(Component heavy) {
        WGlobalCursorManager.getCursorManager().updateCursorLater(heavy);
    }

    @Override
    protected native void setCursor(Component var1, Cursor var2, boolean var3);

    @Override
    protected native void getCursorPos(Point var1);

    @Override
    protected native Component findHeavyweightUnderCursor(boolean var1);

    @Override
    protected native Point getLocationOnScreen(Component var1);
}

