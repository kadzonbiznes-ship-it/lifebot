/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath;

import java.io.IOException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import sun.security.util.Debug;
import sun.security.x509.NameConstraintsExtension;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.X509CertImpl;

class ConstraintsChecker
extends PKIXCertPathChecker {
    private static final Debug debug = Debug.getInstance("certpath");
    private final int certPathLength;
    private int maxPathLength;
    private int i;
    private NameConstraintsExtension prevNC;
    private Set<String> supportedExts;

    ConstraintsChecker(int certPathLength) {
        this.certPathLength = certPathLength;
    }

    @Override
    public void init(boolean forward) throws CertPathValidatorException {
        if (forward) {
            throw new CertPathValidatorException("forward checking not supported");
        }
        this.i = 0;
        this.maxPathLength = this.certPathLength;
        this.prevNC = null;
    }

    @Override
    public boolean isForwardCheckingSupported() {
        return false;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        if (this.supportedExts == null) {
            this.supportedExts = HashSet.newHashSet(2);
            this.supportedExts.add(PKIXExtensions.BasicConstraints_Id.toString());
            this.supportedExts.add(PKIXExtensions.NameConstraints_Id.toString());
            this.supportedExts = Collections.unmodifiableSet(this.supportedExts);
        }
        return this.supportedExts;
    }

    @Override
    public void check(Certificate cert, Collection<String> unresCritExts) throws CertPathValidatorException {
        X509Certificate currCert = (X509Certificate)cert;
        ++this.i;
        this.checkBasicConstraints(currCert);
        this.verifyNameConstraints(currCert);
        if (unresCritExts != null && !unresCritExts.isEmpty()) {
            unresCritExts.remove(PKIXExtensions.BasicConstraints_Id.toString());
            unresCritExts.remove(PKIXExtensions.NameConstraints_Id.toString());
        }
    }

    private void verifyNameConstraints(X509Certificate currCert) throws CertPathValidatorException {
        String msg = "name constraints";
        if (debug != null) {
            debug.println("---checking " + msg + "...");
        }
        if (!(this.prevNC == null || this.i != this.certPathLength && X509CertImpl.isSelfIssued(currCert))) {
            if (debug != null) {
                debug.println("prevNC = " + this.prevNC + ", currDN = " + currCert.getSubjectX500Principal());
            }
            try {
                if (!this.prevNC.verify(currCert)) {
                    throw new CertPathValidatorException(msg + " check failed", null, null, -1, PKIXReason.INVALID_NAME);
                }
            }
            catch (IOException ioe) {
                throw new CertPathValidatorException(ioe);
            }
        }
        this.prevNC = ConstraintsChecker.mergeNameConstraints(currCert, this.prevNC);
        if (debug != null) {
            debug.println(msg + " verified.");
        }
    }

    static NameConstraintsExtension mergeNameConstraints(X509Certificate currCert, NameConstraintsExtension prevNC) throws CertPathValidatorException {
        X509CertImpl currCertImpl;
        try {
            currCertImpl = X509CertImpl.toImpl(currCert);
        }
        catch (CertificateException ce) {
            throw new CertPathValidatorException(ce);
        }
        NameConstraintsExtension newConstraints = currCertImpl.getNameConstraintsExtension();
        if (debug != null) {
            debug.println("prevNC = " + prevNC + ", newNC = " + newConstraints);
        }
        if (prevNC == null) {
            if (debug != null) {
                debug.println("mergedNC = " + newConstraints);
            }
            if (newConstraints == null) {
                return null;
            }
            return (NameConstraintsExtension)newConstraints.clone();
        }
        try {
            prevNC.merge(newConstraints);
        }
        catch (IOException ioe) {
            throw new CertPathValidatorException(ioe);
        }
        if (debug != null) {
            debug.println("mergedNC = " + prevNC);
        }
        return prevNC;
    }

    private void checkBasicConstraints(X509Certificate currCert) throws CertPathValidatorException {
        String msg = "basic constraints";
        if (debug != null) {
            debug.println("---checking " + msg + "...");
            debug.println("i = " + this.i + ", maxPathLength = " + this.maxPathLength);
        }
        if (this.i < this.certPathLength) {
            int pathLenConstraint = -1;
            if (currCert.getVersion() < 3) {
                if (this.i == 1 && X509CertImpl.isSelfIssued(currCert)) {
                    pathLenConstraint = Integer.MAX_VALUE;
                }
            } else {
                pathLenConstraint = currCert.getBasicConstraints();
            }
            if (pathLenConstraint == -1) {
                throw new CertPathValidatorException(msg + " check failed: this is not a CA certificate", null, null, -1, PKIXReason.NOT_CA_CERT);
            }
            if (!X509CertImpl.isSelfIssued(currCert)) {
                if (this.maxPathLength <= 0) {
                    throw new CertPathValidatorException(msg + " check failed: pathLenConstraint violated - this cert must be the last cert in the certification path", null, null, -1, PKIXReason.PATH_TOO_LONG);
                }
                --this.maxPathLength;
            }
            if (pathLenConstraint < this.maxPathLength) {
                this.maxPathLength = pathLenConstraint;
            }
        }
        if (debug != null) {
            debug.println("after processing, maxPathLength = " + this.maxPathLength);
            debug.println(msg + " verified.");
        }
    }

    static int mergeBasicConstraints(X509Certificate cert, int maxPathLength) {
        int pathLenConstraint = cert.getBasicConstraints();
        if (!X509CertImpl.isSelfIssued(cert)) {
            --maxPathLength;
        }
        if (pathLenConstraint < maxPathLength) {
            maxPathLength = pathLenConstraint;
        }
        return maxPathLength;
    }
}

