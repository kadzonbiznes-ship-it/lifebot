/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.awt.OSInfo;

public class FontManagerNativeLibrary {
    public static void load() {
    }

    static {
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                System.loadLibrary("awt");
                if (OSInfo.getOSType() == OSInfo.OSType.WINDOWS) {
                    System.loadLibrary("freetype");
                }
                System.loadLibrary("fontmanager");
                return null;
            }
        });
    }
}

