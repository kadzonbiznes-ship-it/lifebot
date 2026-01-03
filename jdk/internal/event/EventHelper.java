/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.event;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import jdk.internal.access.JavaUtilJarAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.misc.ThreadTracker;

public final class EventHelper {
    private static final JavaUtilJarAccess JUJA = SharedSecrets.javaUtilJarAccess();
    private static volatile boolean loggingSecurity;
    private static volatile System.Logger securityLogger;
    private static final VarHandle LOGGER_HANDLE;
    private static final System.Logger.Level LOG_LEVEL;
    private static final String SECURITY_LOGGER_NAME = "jdk.event.security";

    public static void logTLSHandshakeEvent(Instant start, String peerHost, int peerPort, String cipherSuite, String protocolVersion, long peerCertId) {
        assert (securityLogger != null);
        String prepend = EventHelper.getDurationString(start);
        securityLogger.log(LOG_LEVEL, prepend + " TLSHandshake: {0}:{1,number,#}, {2}, {3}, {4,number,#}", peerHost, peerPort, protocolVersion, cipherSuite, peerCertId);
    }

    public static void logSecurityPropertyEvent(String key, String value) {
        assert (securityLogger != null);
        securityLogger.log(LOG_LEVEL, "SecurityPropertyModification: key:{0}, value:{1}", key, value);
    }

    public static void logX509ValidationEvent(long anchorCertId, long[] certIds) {
        assert (securityLogger != null);
        String codes = LongStream.of(certIds).mapToObj(Long::toString).collect(Collectors.joining(", "));
        securityLogger.log(LOG_LEVEL, "ValidationChain: {0,number,#}, {1}", anchorCertId, codes);
    }

    public static void logX509CertificateEvent(String algId, String serialNum, String subject, String issuer, String keyType, int length, long certId, long beginDate, long endDate) {
        assert (securityLogger != null);
        securityLogger.log(LOG_LEVEL, "X509Certificate: Alg:{0}, Serial:{1}, Subject:{2}, Issuer:{3}, Key type:{4}, Length:{5,number,#}, Cert Id:{6,number,#}, Valid from:{7}, Valid until:{8}", algId, serialNum, subject, issuer, keyType, length, certId, new Date(beginDate), new Date(endDate));
    }

    private static String getDurationString(Instant start) {
        if (start != null) {
            if (start.equals(Instant.MIN)) {
                return "N/A";
            }
            Duration duration = Duration.between(start, Instant.now());
            long micros = duration.toNanos() / 1000L;
            if (micros < 1000000L) {
                return "duration = " + (double)micros / 1000.0 + " ms:";
            }
            return "duration = " + (double)(micros / 1000L) / 1000.0 + " s:";
        }
        return "";
    }

    private static Object tryBeginLookup() {
        return ThreadTrackHolder.TRACKER.tryBegin();
    }

    private static void endLookup(Object key) {
        ThreadTrackHolder.TRACKER.end(key);
    }

    public static boolean isLoggingSecurity() {
        Object key;
        if (securityLogger == null && !JUJA.isInitializing() && (key = EventHelper.tryBeginLookup()) != null) {
            try {
                LOGGER_HANDLE.compareAndSet(null, System.getLogger(SECURITY_LOGGER_NAME));
                loggingSecurity = securityLogger.isLoggable(LOG_LEVEL);
            }
            finally {
                EventHelper.endLookup(key);
            }
        }
        return loggingSecurity;
    }

    static {
        try {
            LOGGER_HANDLE = MethodHandles.lookup().findStaticVarHandle(EventHelper.class, "securityLogger", System.Logger.class);
        }
        catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
        LOG_LEVEL = System.Logger.Level.DEBUG;
    }

    private static class ThreadTrackHolder {
        static final ThreadTracker TRACKER = new ThreadTracker();

        private ThreadTrackHolder() {
        }
    }
}

