/*
 * Decompiled with CFR 0.152.
 */
package sun.security.jca;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import jdk.internal.event.EventHelper;
import jdk.internal.event.X509CertificateEvent;
import sun.security.util.Debug;
import sun.security.util.KeyUtil;

public final class JCAUtil {
    private static final int ARRAY_SIZE = 4096;
    private static volatile SecureRandom def = null;

    private JCAUtil() {
    }

    public static int getTempArraySize(int totalSize) {
        return Math.min(4096, totalSize);
    }

    public static SecureRandom getSecureRandom() {
        return CachedSecureRandomHolder.instance;
    }

    static void clearDefSecureRandom() {
        def = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static SecureRandom getDefSecureRandom() {
        SecureRandom result = def;
        if (result != null) return result;
        Class<JCAUtil> clazz = JCAUtil.class;
        synchronized (JCAUtil.class) {
            result = def;
            if (result != null) return result;
            def = result = new SecureRandom();
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return result;
        }
    }

    public static void tryCommitCertEvent(Certificate cert) {
        if ((X509CertificateEvent.isTurnedOn() || EventHelper.isLoggingSecurity()) && cert instanceof X509Certificate) {
            X509Certificate x509 = (X509Certificate)cert;
            PublicKey pKey = x509.getPublicKey();
            String algId = x509.getSigAlgName();
            String serNum = Debug.toString((BigInteger)x509.getSerialNumber());
            String subject = x509.getSubjectX500Principal().toString();
            String issuer = x509.getIssuerX500Principal().toString();
            String keyType = pKey.getAlgorithm();
            int length = KeyUtil.getKeySize(pKey);
            int hashCode = x509.hashCode();
            long certifcateId = Integer.toUnsignedLong(hashCode);
            long beginDate = x509.getNotBefore().getTime();
            long endDate = x509.getNotAfter().getTime();
            if (X509CertificateEvent.isTurnedOn()) {
                X509CertificateEvent xce = new X509CertificateEvent();
                xce.algorithm = algId;
                xce.serialNumber = serNum;
                xce.subject = subject;
                xce.issuer = issuer;
                xce.keyType = keyType;
                xce.keyLength = length;
                xce.certificateId = certifcateId;
                xce.validFrom = beginDate;
                xce.validUntil = endDate;
                xce.commit();
            }
            if (EventHelper.isLoggingSecurity()) {
                EventHelper.logX509CertificateEvent(algId, serNum, subject, issuer, keyType, length, certifcateId, beginDate, endDate);
            }
        }
    }

    private static class CachedSecureRandomHolder {
        public static SecureRandom instance = new SecureRandom();

        private CachedSecureRandomHolder() {
        }
    }
}

