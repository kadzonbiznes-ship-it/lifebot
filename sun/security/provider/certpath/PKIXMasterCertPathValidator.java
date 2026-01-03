/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath;

import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import sun.security.util.Debug;

class PKIXMasterCertPathValidator {
    private static final Debug debug = Debug.getInstance("certpath");

    PKIXMasterCertPathValidator() {
    }

    static void validate(CertPath cpOriginal, List<X509Certificate> reversedCertList, List<PKIXCertPathChecker> certPathCheckers) throws CertPathValidatorException {
        int cpSize = reversedCertList.size();
        if (debug != null) {
            debug.println("--------------------------------------------------------------");
            debug.println("Executing PKIX certification path validation algorithm.");
        }
        for (int i = 0; i < cpSize; ++i) {
            Set<String> unresCritExts;
            X509Certificate currCert = reversedCertList.get(i);
            if (debug != null) {
                debug.println("Checking cert" + (i + 1) + " - Subject: " + currCert.getSubjectX500Principal());
            }
            if ((unresCritExts = currCert.getCriticalExtensionOIDs()) == null) {
                unresCritExts = Collections.emptySet();
            }
            if (debug != null && !unresCritExts.isEmpty()) {
                StringJoiner joiner = new StringJoiner(", ", "{", "}");
                for (String oid : unresCritExts) {
                    joiner.add(oid);
                }
                debug.println("Set of critical extensions: " + joiner.toString());
            }
            for (int j = 0; j < certPathCheckers.size(); ++j) {
                PKIXCertPathChecker currChecker = certPathCheckers.get(j);
                if (debug != null) {
                    debug.println("-Using checker" + (j + 1) + " ... [" + currChecker.getClass().getName() + "]");
                }
                if (i == 0) {
                    currChecker.init(false);
                }
                try {
                    currChecker.check(currCert, unresCritExts);
                    if (debug == null) continue;
                    debug.println("-checker" + (j + 1) + " validation succeeded");
                    continue;
                }
                catch (CertPathValidatorException cpve) {
                    throw new CertPathValidatorException(cpve.getMessage(), cpve.getCause() != null ? cpve.getCause() : cpve, cpOriginal, cpSize - (i + 1), cpve.getReason());
                }
            }
            if (!unresCritExts.isEmpty()) {
                throw new CertPathValidatorException("unrecognized critical extension(s)", null, cpOriginal, cpSize - (i + 1), PKIXReason.UNRECOGNIZED_CRIT_EXT);
            }
            if (debug == null) continue;
            debug.println("\ncert" + (i + 1) + " validation succeeded.\n");
        }
        if (debug != null) {
            debug.println("Cert path validation succeeded. (PKIX validation algorithm)");
            debug.println("--------------------------------------------------------------");
        }
    }
}

