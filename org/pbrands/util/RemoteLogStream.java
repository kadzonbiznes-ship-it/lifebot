/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.pbrands.model.PacketType;
import org.pbrands.netty.Packet;

public class RemoteLogStream
extends PrintStream {
    private static final ConcurrentLinkedQueue<String> pendingLogs = new ConcurrentLinkedQueue();
    private static volatile Channel channel;
    private static final AtomicBoolean initialized;
    private static int userId;
    private static PrintStream originalOut;
    private static PrintStream originalErr;
    private static boolean developmentMode;
    private final boolean isError;

    private RemoteLogStream(boolean isError) {
        super(new NullOutputStream());
        this.isError = isError;
    }

    public static void initialize(boolean devMode) {
        if (initialized.compareAndSet(false, true)) {
            developmentMode = devMode;
            originalOut = System.out;
            originalErr = System.err;
            System.setOut(new RemoteLogStream(false));
            System.setErr(new RemoteLogStream(true));
            RemoteLogStream.setupCrashHandler();
            if (devMode) {
                originalOut.println("[RemoteLog] Initialized in development mode");
            }
        }
    }

    private static void setupCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            block5: {
                try {
                    StringBuilder report = new StringBuilder();
                    report.append("\n");
                    report.append("\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557\n");
                    report.append("\u2551                    CRASH REPORT                                 \u2551\n");
                    report.append("\u255a\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255d\n");
                    report.append("Thread: ").append(thread.getName()).append("\n");
                    report.append("Time: ").append(new Date()).append("\n");
                    report.append("Exception: ").append(throwable.getClass().getName()).append("\n");
                    report.append("Message: ").append(throwable.getMessage()).append("\n");
                    report.append("\n=== Stack Trace ===\n");
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    throwable.printStackTrace(pw);
                    report.append(sw.toString());
                    report.append("\n=== System Info ===\n");
                    report.append("Java: ").append(System.getProperty("java.version")).append("\n");
                    report.append("OS: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append("\n");
                    report.append("Arch: ").append(System.getProperty("os.arch")).append("\n");
                    Runtime rt = Runtime.getRuntime();
                    long totalMB = rt.totalMemory() / 1024L / 1024L;
                    long freeMB = rt.freeMemory() / 1024L / 1024L;
                    long usedMB = totalMB - freeMB;
                    report.append("Memory: ").append(usedMB).append("MB / ").append(totalMB).append("MB\n");
                    report.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\n");
                    String crashReport = report.toString();
                    System.err.println("[CRASH] " + crashReport);
                    if (developmentMode && originalErr != null) {
                        originalErr.println(crashReport);
                    }
                    try {
                        Thread.sleep(500L);
                    }
                    catch (InterruptedException interruptedException) {}
                }
                catch (Exception e) {
                    if (originalErr == null) break block5;
                    originalErr.println("Crash handler failed: " + e.getMessage());
                    throwable.printStackTrace(originalErr);
                }
            }
        });
    }

    public static boolean hasDevFlag(String[] args) {
        for (String arg : args) {
            if (!"--dev".equalsIgnoreCase(arg)) continue;
            return true;
        }
        return false;
    }

    public static String findEncodedParams(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--")) continue;
            return arg;
        }
        return null;
    }

    public static void setChannel(Channel ch, int uid) {
        channel = ch;
        userId = uid;
        RemoteLogStream.flushPendingLogs();
    }

    private static void flushPendingLogs() {
        String log;
        if (channel == null || !channel.isActive()) {
            return;
        }
        while ((log = pendingLogs.poll()) != null) {
            RemoteLogStream.sendLogToServer(log);
        }
    }

    private static void sendLogToServer(String message) {
        if (channel == null || !channel.isActive()) {
            if (pendingLogs.size() < 1000) {
                pendingLogs.offer(message);
            }
            return;
        }
        try {
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            ByteBuf buf = Unpooled.buffer(8 + messageBytes.length);
            buf.writeInt(userId);
            buf.writeInt(messageBytes.length);
            buf.writeBytes(messageBytes);
            Packet packet = new Packet(PacketType.CLIENT_LOG.getOpcode(), buf);
            packet.writeAndFlush(channel);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Override
    public void println(String x) {
        String prefix = this.isError ? "[ERR] " : "[OUT] ";
        String logLine = prefix + System.currentTimeMillis() + " " + x;
        if (developmentMode && originalOut != null) {
            (this.isError ? originalErr : originalOut).println(x);
        }
        RemoteLogStream.sendLogToServer(logLine);
    }

    @Override
    public void println(Object x) {
        this.println(String.valueOf(x));
    }

    @Override
    public void println() {
        this.println("");
    }

    @Override
    public void print(String s) {
        String prefix = this.isError ? "[ERR] " : "[OUT] ";
        String logLine = prefix + System.currentTimeMillis() + " " + s;
        if (developmentMode && originalOut != null) {
            (this.isError ? originalErr : originalOut).print(s);
        }
        RemoteLogStream.sendLogToServer(logLine);
    }

    @Override
    public void print(Object obj) {
        this.print(String.valueOf(obj));
    }

    @Override
    public PrintStream printf(String format, Object ... args) {
        this.print(String.format(format, args));
        return this;
    }

    public static void restore() {
        if (originalOut != null) {
            System.setOut(originalOut);
        }
        if (originalErr != null) {
            System.setErr(originalErr);
        }
    }

    public static void reportException(String context, Throwable throwable) {
        try {
            StringBuilder report = new StringBuilder();
            report.append("[EXCEPTION REPORT] Context: ").append(context).append("\n");
            report.append("Exception: ").append(throwable.getClass().getName()).append("\n");
            report.append("Message: ").append(throwable.getMessage()).append("\n");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            report.append("Stack trace:\n").append(sw.toString());
            System.err.println(report.toString());
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void reportError(String errorType, String message) {
        System.err.println("[ERROR REPORT] " + errorType + ": " + message);
    }

    static {
        initialized = new AtomicBoolean(false);
        userId = -1;
        developmentMode = false;
    }

    private static class NullOutputStream
    extends OutputStream {
        private NullOutputStream() {
        }

        @Override
        public void write(int b) {
        }

        @Override
        public void write(byte[] b, int off, int len) {
        }
    }
}

