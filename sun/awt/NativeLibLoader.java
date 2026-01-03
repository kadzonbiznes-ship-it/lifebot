/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.security.AccessController;
import java.security.PrivilegedAction;

class NativeLibLoader {
    NativeLibLoader() {
    }

    static void loadLibraries() {
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                System.loadLibrary("awt");
                return null;
            }
        });
    }
}

