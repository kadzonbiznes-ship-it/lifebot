/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import sun.awt.Win32GraphicsEnvironment;
import sun.awt.windows.WToolkit;

public class PlatformGraphicsInfo {
    public static GraphicsEnvironment createGE() {
        return new Win32GraphicsEnvironment();
    }

    public static Toolkit createToolkit() {
        return new WToolkit();
    }

    public static boolean getDefaultHeadlessProperty() {
        return false;
    }

    public static String getDefaultHeadlessMessage() {
        return "\nThe application does not have desktop access,\nbut this program performed an operation which requires it.";
    }
}

