/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.provider.certpath.PKIX;
import sun.security.util.Debug;
import sun.security.x509.X500Name;

class BasicChecker
extends PKIXCertPathChecker {
    private static final Debug debug = Debug.getInstance("certpath");
    private final PublicKey trustedPubKey;
    private final X500Principal caName;
    private final Date date;
    private final String sigProvider;
    private final boolean sigOnly;
    private X500Principal prevSubject;
    private PublicKey prevPubKey;

    BasicChecker(TrustAnchor anchor, Date date, String sigProvider, boolean sigOnly) {
        if (anchor.getTrustedCert() != null) {
            this.trustedPubKey = anchor.getTrustedCert().getPublicKey();
            this.caName = anchor.getTrustedCert().getSubjectX500Principal();
        } else {
            this.trustedPubKey = anchor.getCAPublicKey();
            this.caName = anchor.getCA();
        }
        this.date = date;
        this.sigProvider = sigProvider;
        this.sigOnly = sigOnly;
        this.prevPubKey = this.trustedPubKey;
    }

    @Override
    public void init(boolean forward) throws CertPathValidatorException {
        if (!forward) {
            this.prevPubKey = this.trustedPubKey;
            if (PKIX.isDSAPublicKeyWithoutParams(this.prevPubKey)) {
                throw new CertPathValidatorException("Key parameters missing");
            }
        } else {
            throw new CertPathValidatorException("forward checking not supported");
        }
        this.prevSubject = this.caName;
    }

    @Override
    public boolean isForwardCheckingSupported() {
        return false;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return null;
    }

    @Override
    public void check(Certificate cert, Collection<String> unresolvedCritExts) throws CertPathValidatorException {
        X509Certificate currCert = (X509Certificate)cert;
        if (!this.sigOnly) {
            this.verifyValidity(currCert);
            this.verifyNameChaining(currCert);
        }
        this.verifySignature(currCert);
        this.updateState(currCert);
    }

    private void verifySignature(X509Certificate cert) throws CertPathValidatorException {
        String msg = "signature";
        if (debug != null) {
            debug.println("---checking " + msg + "...");
        }
        try {
            cert.verify(this.prevPubKey, this.sigProvider);
        }
        catch (SignatureException e) {
            throw new CertPathValidatorException(msg + " check failed", e, null, -1, CertPathValidatorException.BasicReason.INVALID_SIGNATURE);
        }
        catch (GeneralSecurityException e) {
            throw new CertPathValidatorException(msg + " check failed", e);
        }
        if (debug != null) {
            debug.println(msg + " verified.");
        }
    }

    private void verifyValidity(X509Certificate cert) throws CertPathValidatorException {
        String msg = "validity";
        if (debug != null) {
            debug.println("---checking " + msg + ":" + this.date.toString() + "...");
        }
        try {
            cert.checkValidity(this.date);
        }
        catch (CertificateExpiredException e) {
            throw new CertPathValidatorException(msg + " check failed", e, null, -1, CertPathValidatorException.BasicReason.EXPIRED);
        }
        catch (CertificateNotYetValidException e) {
            throw new CertPathValidatorException(msg + " check failed", e, null, -1, CertPathValidatorException.BasicReason.NOT_YET_VALID);
        }
        if (debug != null) {
            debug.println(msg + " verified.");
        }
    }

    private void verifyNameChaining(X509Certificate cert) throws CertPathValidatorException {
        if (this.prevSubject != null) {
            X500Principal currIssuer;
            String msg = "subject/issuer name chaining";
            if (debug != null) {
                debug.println("---checking " + msg + "...");
            }
            if (X500Name.asX500Name(currIssuer = cert.getIssuerX500Principal()).isEmpty()) {
                throw new CertPathValidatorException(msg + " check failed: empty/null issuer DN in certificate is invalid", null, null, -1, PKIXReason.NAME_CHAINING);
            }
            if (!currIssuer.equals(this.prevSubject)) {
                throw new CertPathValidatorException(msg + " check failed", null, null, -1, PKIXReason.NAME_CHAINING);
            }
            if (debug != null) {
                debug.println(msg + " verified.");
            }
        }
    }

    private void updateState(X509Certificate currCert) throws CertPathValidatorException {
        PublicKey cKey = currCert.getPublicKey();
        if (debug != null) {
            debug.println("BasicChecker.updateState issuer: " + currCert.getIssuerX500Principal().toString() + "; subject: " + currCert.getSubjectX500Principal() + "; serial#: " + Debug.toString((BigInteger)currCert.getSerialNumber()));
        }
        if (PKIX.isDSAPublicKeyWithoutParams(cKey)) {
            cKey = BasicChecker.makeInheritedParamsKey(cKey, this.prevPubKey);
            if (debug != null) {
                debug.println("BasicChecker.updateState Made key with inherited params");
            }
        }
        this.prevPubKey = cKey;
        this.prevSubject = currCert.getSubjectX500Principal();
    }

    static PublicKey makeInheritedParamsKey(PublicKey keyValueKey, PublicKey keyParamsKey) throws CertPathValidatorException {
        if (!(keyValueKey instanceof DSAPublicKey) || !(keyParamsKey instanceof DSAPublicKey)) {
            throw new CertPathValidatorException("Input key is not appropriate type for inheriting parameters");
        }
        DSAParams params = ((DSAPublicKey)keyParamsKey).getParams();
        if (params == null) {
            throw new CertPathValidatorException("Key parameters missing");
        }
        try {
            BigInteger y = ((DSAPublicKey)keyValueKey).getY();
            KeyFactory kf = KeyFactory.getInstance("DSA");
            DSAPublicKeySpec ks = new DSAPublicKeySpec(y, params.getP(), params.getQ(), params.getG());
            return kf.generatePublic(ks);
        }
        catch (GeneralSecurityException e) {
            throw new CertPathValidatorException("Unable to generate key with inherited parameters: " + e.getMessage(), e);
        }
    }

    PublicKey getPublicKey() {
        return this.prevPubKey;
    }
}

