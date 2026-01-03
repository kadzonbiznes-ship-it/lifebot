/*
 * Decompiled with CFR 0.152.
 */
package sun.security.validator;

import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Date;
import sun.security.util.Debug;
import sun.security.validator.ValidatorException;
import sun.security.x509.X509CertImpl;

final class CamerfirmaTLSPolicy {
    private static final Debug debug = Debug.getInstance("certpath");
    private static final String FINGERPRINT = "063E4AFAC491DFD332F3089B8542E94617D893D7FE944E10A7937EE29D9693C0";
    private static final LocalDate APRIL_15_2025 = LocalDate.of(2025, Month.APRIL, 15);

    static void checkDistrust(X509Certificate[] chain) throws ValidatorException {
        X509Certificate anchor = chain[chain.length - 1];
        String fp = CamerfirmaTLSPolicy.fingerprint(anchor);
        if (fp == null) {
            throw new ValidatorException("Cannot generate fingerprint for trust anchor of TLS server certificate");
        }
        if (FINGERPRINT.equalsIgnoreCase(fp)) {
            Date notBefore = chain[0].getNotBefore();
            LocalDate ldNotBefore = LocalDate.ofInstant(notBefore.toInstant(), ZoneOffset.UTC);
            CamerfirmaTLSPolicy.checkNotBefore(ldNotBefore, APRIL_15_2025, anchor);
        }
    }

    private static String fingerprint(X509Certificate cert) {
        return X509CertImpl.getFingerprint("SHA-256", cert, debug);
    }

    private static void checkNotBefore(LocalDate notBeforeDate, LocalDate distrustDate, X509Certificate anchor) throws ValidatorException {
        if (notBeforeDate.isAfter(distrustDate)) {
            throw new ValidatorException("TLS Server certificate issued after " + distrustDate + " and anchored by a distrusted legacy Camerfirma root CA: " + anchor.getSubjectX500Principal(), ValidatorException.T_UNTRUSTED_CERT, anchor);
        }
    }

    private CamerfirmaTLSPolicy() {
    }
}

