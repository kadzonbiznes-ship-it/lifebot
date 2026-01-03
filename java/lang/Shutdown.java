/*
 * Decompiled with CFR 0.152.
 */
package java.lang;

import jdk.internal.misc.VM;

class Shutdown {
    private static final int MAX_SYSTEM_HOOKS = 10;
    private static final Runnable[] hooks = new Runnable[10];
    private static int currentRunningHook = -1;
    private static Object lock = new Lock();
    private static Object haltLock = new Lock();

    Shutdown() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void add(int slot, boolean registerShutdownInProgress, Runnable hook) {
        if (slot < 0 || slot >= 10) {
            throw new IllegalArgumentException("Invalid slot: " + slot);
        }
        Object object = lock;
        synchronized (object) {
            if (hooks[slot] != null) {
                throw new InternalError("Shutdown hook at slot " + slot + " already registered");
            }
            if (!registerShutdownInProgress ? currentRunningHook >= 0 : VM.isShutdown() || slot <= currentRunningHook) {
                throw new IllegalStateException("Shutdown in progress");
            }
            Shutdown.hooks[slot] = hook;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void runHooks() {
        Object object = lock;
        synchronized (object) {
            if (VM.isShutdown()) {
                return;
            }
        }
        for (int i = 0; i < 10; ++i) {
            try {
                Runnable hook;
                Object object2 = lock;
                synchronized (object2) {
                    currentRunningHook = i;
                    hook = hooks[i];
                }
                if (hook == null) continue;
                hook.run();
                continue;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        VM.shutdown();
    }

    static native void beforeHalt();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void halt(int status) {
        Object object = haltLock;
        synchronized (object) {
            Shutdown.halt0(status);
        }
    }

    static native void halt0(int var0);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void exit(int status) {
        Shutdown.logRuntimeExit(status);
        Class<Shutdown> clazz = Shutdown.class;
        synchronized (Shutdown.class) {
            Shutdown.beforeHalt();
            Shutdown.runHooks();
            Shutdown.halt(status);
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return;
        }
    }

    private static void logRuntimeExit(int status) {
        try {
            System.Logger log = System.getLogger("java.lang.Runtime");
            if (log.isLoggable(System.Logger.Level.DEBUG)) {
                Throwable throwable = new Throwable("Runtime.exit(" + status + ")");
                log.log(System.Logger.Level.DEBUG, "Runtime.exit() called with status: " + status, throwable);
            }
        }
        catch (Throwable throwable) {
            try {
                System.err.println("Runtime.exit(" + status + ") logging failed: " + throwable.getMessage());
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void shutdown() {
        Class<Shutdown> clazz = Shutdown.class;
        synchronized (Shutdown.class) {
            Shutdown.runHooks();
            // ** MonitorExit[var0] (shouldn't be in output)
            return;
        }
    }

    private static class Lock {
        private Lock() {
        }
    }
}

