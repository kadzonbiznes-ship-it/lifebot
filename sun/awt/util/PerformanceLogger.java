/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.util;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Vector;
import sun.security.action.GetPropertyAction;

public class PerformanceLogger {
    private static final int START_INDEX = 0;
    private static final int LAST_RESERVED = 0;
    private static boolean perfLoggingOn = false;
    private static boolean useNanoTime = false;
    private static Vector<TimeData> times;
    private static String logFileName;
    private static Writer logWriter;
    private static long baseTime;

    public static boolean loggingEnabled() {
        return perfLoggingOn;
    }

    private static long getCurrentTime() {
        if (useNanoTime) {
            return System.nanoTime();
        }
        return System.currentTimeMillis();
    }

    public static void setStartTime(String message) {
        if (PerformanceLogger.loggingEnabled()) {
            long nowTime = PerformanceLogger.getCurrentTime();
            PerformanceLogger.setStartTime(message, nowTime);
        }
    }

    public static void setBaseTime(long time) {
        if (PerformanceLogger.loggingEnabled()) {
            baseTime = time;
        }
    }

    public static void setStartTime(String message, long time) {
        if (PerformanceLogger.loggingEnabled()) {
            times.set(0, new TimeData(message, time));
        }
    }

    public static long getStartTime() {
        if (PerformanceLogger.loggingEnabled()) {
            return times.get(0).getTime();
        }
        return 0L;
    }

    public static int setTime(String message) {
        if (PerformanceLogger.loggingEnabled()) {
            long nowTime = PerformanceLogger.getCurrentTime();
            return PerformanceLogger.setTime(message, nowTime);
        }
        return 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int setTime(String message, long time) {
        if (PerformanceLogger.loggingEnabled()) {
            Vector<TimeData> vector = times;
            synchronized (vector) {
                times.add(new TimeData(message, time));
                return times.size() - 1;
            }
        }
        return 0;
    }

    public static long getTimeAtIndex(int index) {
        if (PerformanceLogger.loggingEnabled()) {
            return times.get(index).getTime();
        }
        return 0L;
    }

    public static String getMessageAtIndex(int index) {
        if (PerformanceLogger.loggingEnabled()) {
            return times.get(index).getMessage();
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void outputLog(Writer writer) {
        if (PerformanceLogger.loggingEnabled()) {
            try {
                Vector<TimeData> vector = times;
                synchronized (vector) {
                    for (int i = 0; i < times.size(); ++i) {
                        TimeData td = times.get(i);
                        if (td == null) continue;
                        writer.write(i + " " + td.getMessage() + ": " + (td.getTime() - baseTime) + "\n");
                    }
                }
                writer.flush();
            }
            catch (Exception e) {
                System.out.println(String.valueOf(e) + ": Writing performance log to " + String.valueOf(writer));
            }
        }
    }

    public static void outputLog() {
        PerformanceLogger.outputLog(logWriter);
    }

    static {
        logFileName = null;
        logWriter = null;
        String perfLoggingProp = AccessController.doPrivileged(new GetPropertyAction("sun.perflog"));
        if (perfLoggingProp != null) {
            perfLoggingOn = true;
            String perfNanoProp = AccessController.doPrivileged(new GetPropertyAction("sun.perflog.nano"));
            if (perfNanoProp != null) {
                useNanoTime = true;
            }
            if (perfLoggingProp.regionMatches(true, 0, "file:", 0, 5)) {
                logFileName = perfLoggingProp.substring(5);
            }
            if (logFileName != null && logWriter == null) {
                AccessController.doPrivileged(new PrivilegedAction<Void>(){

                    @Override
                    public Void run() {
                        try {
                            File logFile = new File(logFileName);
                            logFile.createNewFile();
                            logWriter = new FileWriter(logFile);
                        }
                        catch (Exception e) {
                            System.out.println(String.valueOf(e) + ": Creating logfile " + logFileName + ".  Log to console");
                        }
                        return null;
                    }
                });
            }
            if (logWriter == null) {
                logWriter = new OutputStreamWriter(System.out);
            }
        }
        times = new Vector(10);
        for (int i = 0; i <= 0; ++i) {
            times.add(new TimeData("Time " + i + " not set", 0L));
        }
    }

    static class TimeData {
        String message;
        long time;

        TimeData(String message, long time) {
            this.message = message;
            this.time = time;
        }

        String getMessage() {
            return this.message;
        }

        long getTime() {
            return this.time;
        }
    }
}

