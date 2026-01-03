/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 */
package oshi.util;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class Util {
    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    private Util() {
    }

    public static void sleep(long ms) {
        try {
            LOG.trace("Sleeping for {} ms", (Object)ms);
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            LOG.warn("Interrupted while sleeping for {} ms: {}", (Object)ms, (Object)e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public static boolean wildcardMatch(String text, String pattern) {
        if (pattern.length() > 0 && pattern.charAt(0) == '^') {
            return !Util.wildcardMatch(text, pattern.substring(1));
        }
        return text.matches(pattern.replace("?", ".?").replace("*", ".*?"));
    }

    public static boolean isBlank(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isBlankOrUnknown(String s) {
        return Util.isBlank(s) || "unknown".equals(s);
    }

    public static void freeMemory(Pointer p) {
        if (p instanceof Memory) {
            ((Memory)p).close();
        }
    }

    public static boolean isSessionValid(String user, String device, Long loginTime) {
        return !user.isEmpty() && !device.isEmpty() && loginTime >= 0L && loginTime <= System.currentTimeMillis();
    }
}

