/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Log {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static boolean debugEnabled = true;

    private Log() {
    }

    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    public static void info(String message) {
        Log.log("INFO", message, null);
    }

    public static void info(String format, Object ... args) {
        Log.log("INFO", Log.format(format, args), null);
    }

    public static void warn(String message) {
        Log.log("WARN", message, null);
    }

    public static void warn(String format, Object ... args) {
        Log.log("WARN", Log.format(format, args), null);
    }

    public static void warn(String message, Throwable t) {
        Log.log("WARN", message, t);
    }

    public static void error(String message) {
        Log.logErr("ERROR", message, null);
    }

    public static void error(String format, Object ... args) {
        Log.logErr("ERROR", Log.format(format, args), null);
    }

    public static void error(String message, Throwable t) {
        Log.logErr("ERROR", message, t);
    }

    public static void debug(String message) {
        if (debugEnabled) {
            Log.log("DEBUG", message, null);
        }
    }

    public static void debug(String format, Object ... args) {
        if (debugEnabled) {
            Log.log("DEBUG", Log.format(format, args), null);
        }
    }

    private static void log(String level, String message, Throwable t) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String threadName = Thread.currentThread().getName();
        System.out.println(String.format("[%s] [%s] [%s] %s", timestamp, level, threadName, message));
        if (t != null) {
            System.out.println(Log.getStackTrace(t));
        }
    }

    private static void logErr(String level, String message, Throwable t) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String threadName = Thread.currentThread().getName();
        System.err.println(String.format("[%s] [%s] [%s] %s", timestamp, level, threadName, message));
        if (t != null) {
            t.printStackTrace(System.err);
        }
    }

    private static String format(String format, Object ... args) {
        if (args == null || args.length == 0) {
            return format;
        }
        StringBuilder sb = new StringBuilder();
        int argIndex = 0;
        int i = 0;
        while (i < format.length()) {
            if (i < format.length() - 1 && format.charAt(i) == '{' && format.charAt(i + 1) == '}') {
                if (argIndex < args.length) {
                    sb.append(args[argIndex++]);
                } else {
                    sb.append("{}");
                }
                i += 2;
                continue;
            }
            sb.append(format.charAt(i));
            ++i;
        }
        return sb.toString();
    }

    private static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}

