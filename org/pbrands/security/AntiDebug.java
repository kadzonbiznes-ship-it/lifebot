/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.security;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AntiDebug {
    private static volatile boolean debuggerDetected = false;
    private static ScheduledExecutorService watchdog;
    private static Runnable onDebuggerDetected;
    private static final String[] SUSPICIOUS_ARGS;
    private static final String[] SUSPICIOUS_PROPERTIES;

    public static void init() {
        AntiDebug.init(() -> {
            System.err.println("Security violation detected. Exiting.");
            Runtime.getRuntime().halt(1);
        });
    }

    public static void init(Runnable onDetected) {
        onDebuggerDetected = onDetected;
        if (AntiDebug.isDebuggerPresent()) {
            debuggerDetected = true;
            AntiDebug.triggerProtection();
            return;
        }
        AntiDebug.startWatchdog();
    }

    public static boolean isDebuggerPresent() {
        return AntiDebug.checkJvmArguments() || AntiDebug.checkSystemProperties() || AntiDebug.checkManagementAgent() || AntiDebug.checkDebuggerThread() || AntiDebug.checkTimingAnomaly();
    }

    private static boolean checkJvmArguments() {
        try {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            List<String> arguments = runtimeMXBean.getInputArguments();
            for (String arg : arguments) {
                String lowerArg = arg.toLowerCase();
                for (String suspicious : SUSPICIOUS_ARGS) {
                    if (!lowerArg.contains(suspicious.toLowerCase())) continue;
                    return true;
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    private static boolean checkSystemProperties() {
        try {
            for (String prop : SUSPICIOUS_PROPERTIES) {
                if (System.getProperty(prop) == null) continue;
                return true;
            }
            String jdwp = System.getProperty("sun.jdwp.listenerAddress");
            if (jdwp != null && !jdwp.isEmpty()) {
                return true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    private static boolean checkManagementAgent() {
        try {
            Class<?> vmClass = Class.forName("com.sun.tools.attach.VirtualMachine");
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            String vmName = runtime.getVmName().toLowerCase();
            if (vmName.contains("debug") || vmName.contains("jdwp")) {
                return true;
            }
        }
        catch (ClassNotFoundException classNotFoundException) {
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    private static boolean checkDebuggerThread() {
        try {
            ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
            while (rootGroup.getParent() != null) {
                rootGroup = rootGroup.getParent();
            }
            Thread[] threads = new Thread[rootGroup.activeCount() + 10];
            int count = rootGroup.enumerate(threads, true);
            for (int i = 0; i < count; ++i) {
                String name;
                Thread t = threads[i];
                if (t == null || !(name = t.getName().toLowerCase()).contains("jdwp") && !name.contains("debugger") && !name.contains("attach listener") && !name.contains("signal dispatcher") || !name.contains("jdwp") && !name.contains("debugger")) continue;
                return true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    private static boolean checkTimingAnomaly() {
        try {
            long start = System.nanoTime();
            int sum = 0;
            for (int i = 0; i < 1000; ++i) {
                sum += i;
            }
            long elapsed = System.nanoTime() - start;
            if (elapsed > 10000000L) {
                return true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    private static void startWatchdog() {
        if (watchdog != null) {
            return;
        }
        watchdog = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("JVM-Monitor");
            return t;
        });
        watchdog.scheduleAtFixedRate(() -> {
            if (!debuggerDetected && AntiDebug.isDebuggerPresent()) {
                debuggerDetected = true;
                AntiDebug.triggerProtection();
            }
        }, 2L, 2L, TimeUnit.SECONDS);
    }

    private static void triggerProtection() {
        if (onDebuggerDetected != null) {
            try {
                onDebuggerDetected.run();
            }
            catch (Exception e) {
                Runtime.getRuntime().halt(1);
            }
        } else {
            Runtime.getRuntime().halt(1);
        }
    }

    public static void stop() {
        if (watchdog != null) {
            watchdog.shutdownNow();
            watchdog = null;
        }
    }

    public static String[] getSecureJvmArgs() {
        return new String[]{"-XX:+DisableAttachMechanism", "-Xverify:all", "-Djdk.attach.allowAttachSelf=false"};
    }

    static {
        SUSPICIOUS_ARGS = new String[]{"-agentlib:jdwp", "-Xdebug", "-Xrunjdwp", "-javaagent:", "transport=dt_socket", "transport=dt_shmem", "-agentpath:", "suspend=", "server=y", "-Xnoagent"};
        SUSPICIOUS_PROPERTIES = new String[]{"intellij.debug.agent", "idea.debugger.dispatch.addr", "java.compiler", "eclipse.vmargs"};
    }
}

