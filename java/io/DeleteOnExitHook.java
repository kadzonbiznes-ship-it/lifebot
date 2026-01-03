/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import jdk.internal.access.SharedSecrets;

class DeleteOnExitHook {
    private static LinkedHashSet<String> files = new LinkedHashSet();

    private DeleteOnExitHook() {
    }

    static synchronized void add(String file) {
        if (files == null) {
            throw new IllegalStateException("Shutdown in progress");
        }
        files.add(file);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void runHooks() {
        Class<DeleteOnExitHook> clazz = DeleteOnExitHook.class;
        synchronized (DeleteOnExitHook.class) {
            LinkedHashSet<String> theFiles = files;
            files = null;
            // ** MonitorExit[var1] (shouldn't be in output)
            ArrayList<String> toBeDeleted = new ArrayList<String>(theFiles);
            Collections.reverse(toBeDeleted);
            for (String filename : toBeDeleted) {
                new File(filename).delete();
            }
            return;
        }
    }

    static {
        SharedSecrets.getJavaLangAccess().registerShutdownHook(2, true, new Runnable(){

            @Override
            public void run() {
                DeleteOnExitHook.runHooks();
            }
        });
    }
}

