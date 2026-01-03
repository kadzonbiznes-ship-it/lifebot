/*
 * Decompiled with CFR 0.152.
 */
package sun.security.validator;

import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Set;
import sun.security.util.Debug;
import sun.security.validator.ValidatorException;
import sun.security.x509.X509CertImpl;

final class EntrustTLSPolicy {
    private static final Debug debug = Debug.getInstance("certpath");
    private static final Set<String> FINGERPRINTS = Set.of("73C176434F1BC6D5ADF45B0E76E727287C8DE57616C1E6E6141A2B2CBC7D8E4C", "02ED0EB28C14DA45165C566791700D6451D7FB56F0B2AB1D3B8EB070E56EDFF5", "43DF5774B03E7FEF5FE40D931A7BEDF1BB2E6B42738C4E6D3841103D3AA7F339", "DB3517D1F6732A2D5AB97C533EC70779EE3270A62FB4AC4238372460E6F01E88", "6DC47172E01CBCB0BF62580D895FE2B8AC9AD4F873801E0C10B9C837D21EB177");
    private static final LocalDate NOVEMBER_11_2024 = LocalDate.of(2024, Month.NOVEMBER, 11);

    static void checkDistrust(X509Certificate[] chain) throws ValidatorException {
        X509Certificate anchor = chain[chain.length - 1];
        String fp = EntrustTLSPolicy.fingerprint(anchor);
        if (fp == null) {
            throw new ValidatorException("Cannot generate fingerprint for trust anchor of TLS server certificate");
        }
        if (FINGERPRINTS.contains(fp)) {
            Date notBefore = chain[0].getNotBefore();
            LocalDate ldNotBefore = LocalDate.ofInstant(notBefore.toInstant(), ZoneOffset.UTC);
            EntrustTLSPolicy.checkNotBefore(ldNotBefore, NOVEMBER_11_2024, anchor);
        }
    }

    private static String fingerprint(X509Certificate cert) {
        return X509CertImpl.getFingerprint("SHA-256", cert, debug);
    }

    private static void checkNotBefore(LocalDate notBeforeDate, LocalDate distrustDate, X509Certificate anchor) throws ValidatorException {
        if (notBeforeDate.isAfter(distrustDate)) {
            throw new ValidatorException("TLS Server certificate issued after " + distrustDate + " and anchored by a distrusted legacy Entrust root CA: " + anchor.getSubjectX500Principal(), ValidatorException.T_UNTRUSTED_CERT, anchor);
        }
    }

    private EntrustTLSPolicy() {
    }
}

