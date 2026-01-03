/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath;

import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Set;
import sun.security.util.Debug;
import sun.security.util.UntrustedCertificates;

public final class UntrustedChecker
extends PKIXCertPathChecker {
    private static final Debug debug = Debug.getInstance("certpath");

    @Override
    public void init(boolean forward) throws CertPathValidatorException {
    }

    @Override
    public boolean isForwardCheckingSupported() {
        return true;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return null;
    }

    @Override
    public void check(Certificate cert, Collection<String> unresolvedCritExts) throws CertPathValidatorException {
        X509Certificate currCert = (X509Certificate)cert;
        if (UntrustedCertificates.isUntrusted(currCert)) {
            if (debug != null) {
                debug.println("UntrustedChecker: untrusted certificate " + currCert.getSubjectX500Principal());
            }
            throw new CertPathValidatorException("Untrusted certificate: " + currCert.getSubjectX500Principal());
        }
    }
}

