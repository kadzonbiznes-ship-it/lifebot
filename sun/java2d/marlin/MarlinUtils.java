/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import sun.java2d.marlin.MarlinConst;
import sun.util.logging.PlatformLogger;

public final class MarlinUtils {
    private static final PlatformLogger LOG = MarlinConst.USE_LOGGER ? PlatformLogger.getLogger("sun.java2d.marlin") : null;

    private MarlinUtils() {
    }

    public static void logInfo(String msg) {
        if (MarlinConst.USE_LOGGER) {
            LOG.info(msg);
        } else if (MarlinConst.ENABLE_LOGS) {
            System.out.print("INFO: ");
            System.out.println(msg);
        }
    }

    public static void logException(String msg, Throwable th) {
        if (MarlinConst.USE_LOGGER) {
            LOG.warning(msg, th);
        } else if (MarlinConst.ENABLE_LOGS) {
            System.out.print("WARNING: ");
            System.out.println(msg);
            th.printStackTrace(System.err);
        }
    }

    public static ThreadGroup getRootThreadGroup() {
        ThreadGroup currentTG = Thread.currentThread().getThreadGroup();
        ThreadGroup parentTG = currentTG.getParent();
        while (parentTG != null) {
            currentTG = parentTG;
            parentTG = currentTG.getParent();
        }
        return currentTG;
    }
}

