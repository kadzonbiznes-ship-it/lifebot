/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath;

import java.security.cert.CertPathValidatorException;
import java.security.cert.CertSelector;
import java.security.cert.Certificate;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import sun.security.util.Debug;
import sun.security.x509.PKIXExtensions;

class KeyChecker
extends PKIXCertPathChecker {
    private static final Debug debug = Debug.getInstance("certpath");
    private final int certPathLen;
    private final CertSelector targetConstraints;
    private int remainingCerts;
    private Set<String> supportedExts;
    private static final int KEY_CERT_SIGN = 5;

    KeyChecker(int certPathLen, CertSelector targetCertSel) {
        this.certPathLen = certPathLen;
        this.targetConstraints = targetCertSel;
    }

    @Override
    public void init(boolean forward) throws CertPathValidatorException {
        if (forward) {
            throw new CertPathValidatorException("forward checking not supported");
        }
        this.remainingCerts = this.certPathLen;
    }

    @Override
    public boolean isForwardCheckingSupported() {
        return false;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        if (this.supportedExts == null) {
            this.supportedExts = HashSet.newHashSet(3);
            this.supportedExts.add(PKIXExtensions.KeyUsage_Id.toString());
            this.supportedExts.add(PKIXExtensions.ExtendedKeyUsage_Id.toString());
            this.supportedExts.add(PKIXExtensions.SubjectAlternativeName_Id.toString());
            this.supportedExts = Collections.unmodifiableSet(this.supportedExts);
        }
        return this.supportedExts;
    }

    @Override
    public void check(Certificate cert, Collection<String> unresCritExts) throws CertPathValidatorException {
        X509Certificate currCert = (X509Certificate)cert;
        --this.remainingCerts;
        if (this.remainingCerts == 0) {
            if (this.targetConstraints != null && !this.targetConstraints.match(currCert)) {
                throw new CertPathValidatorException("target certificate constraints check failed");
            }
        } else {
            KeyChecker.verifyCAKeyUsage(currCert);
        }
        if (unresCritExts != null && !unresCritExts.isEmpty()) {
            unresCritExts.remove(PKIXExtensions.KeyUsage_Id.toString());
            unresCritExts.remove(PKIXExtensions.ExtendedKeyUsage_Id.toString());
            unresCritExts.remove(PKIXExtensions.SubjectAlternativeName_Id.toString());
        }
    }

    static void verifyCAKeyUsage(X509Certificate cert) throws CertPathValidatorException {
        boolean[] keyUsageBits;
        String msg = "CA key usage";
        if (debug != null) {
            debug.println("KeyChecker.verifyCAKeyUsage() ---checking " + msg + "...");
        }
        if ((keyUsageBits = cert.getKeyUsage()) == null) {
            return;
        }
        if (!keyUsageBits[5]) {
            throw new CertPathValidatorException(msg + " check failed: keyCertSign bit is not set", null, null, -1, PKIXReason.INVALID_KEY_USAGE);
        }
        if (debug != null) {
            debug.println("KeyChecker.verifyCAKeyUsage() " + msg + " verified.");
        }
    }
}

