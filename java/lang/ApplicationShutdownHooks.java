/*
 * Decompiled with CFR 0.152.
 */
package java.lang;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

class ApplicationShutdownHooks {
    private static IdentityHashMap<Thread, Thread> hooks;

    private ApplicationShutdownHooks() {
    }

    static synchronized void add(Thread hook) {
        if (hooks == null) {
            throw new IllegalStateException("Shutdown in progress");
        }
        if (hook.isAlive()) {
            throw new IllegalArgumentException("Hook already running");
        }
        if (hooks.containsKey(hook)) {
            throw new IllegalArgumentException("Hook previously registered");
        }
        hooks.put(hook, hook);
    }

    static synchronized boolean remove(Thread hook) {
        if (hooks == null) {
            throw new IllegalStateException("Shutdown in progress");
        }
        if (hook == null) {
            throw new NullPointerException();
        }
        return hooks.remove(hook) != null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void runHooks() {
        Iterator iterator = ApplicationShutdownHooks.class;
        synchronized (ApplicationShutdownHooks.class) {
            Set<Thread> threads = hooks.keySet();
            hooks = null;
            // ** MonitorExit[var1] (shouldn't be in output)
            for (Thread hook : threads) {
                try {
                    hook.start();
                }
                catch (IllegalThreadStateException illegalThreadStateException) {
                }
                catch (RejectedExecutionException ignore) {
                    assert (hook.isVirtual());
                }
            }
            block9: for (Thread hook : threads) {
                while (true) {
                    try {
                        hook.join();
                        continue block9;
                    }
                    catch (InterruptedException interruptedException) {
                        continue;
                    }
                    break;
                }
            }
            return;
        }
    }

    static {
        try {
            Shutdown.add(1, false, new Runnable(){

                @Override
                public void run() {
                    ApplicationShutdownHooks.runHooks();
                }
            });
            hooks = new IdentityHashMap();
        }
        catch (IllegalStateException e) {
            hooks = null;
        }
    }
}

