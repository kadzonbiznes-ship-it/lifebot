/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath;

import java.math.BigInteger;
import java.security.AlgorithmConstraints;
import java.security.AlgorithmParameters;
import java.security.CryptoPrimitive;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import sun.security.provider.certpath.CertPathConstraintsParameters;
import sun.security.provider.certpath.PKIX;
import sun.security.util.Debug;
import sun.security.util.DisabledAlgorithmConstraints;
import sun.security.util.KeyUtil;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X509CertImpl;

public final class AlgorithmChecker
extends PKIXCertPathChecker {
    private static final Debug debug = Debug.getInstance("certpath");
    private final AlgorithmConstraints constraints;
    private final Date date;
    private final String variant;
    private PublicKey trustedPubKey;
    private PublicKey prevPubKey;
    private TrustAnchor anchor;
    private static final Set<CryptoPrimitive> SIGNATURE_PRIMITIVE_SET = Collections.unmodifiableSet(EnumSet.of(CryptoPrimitive.SIGNATURE));
    private static final Set<CryptoPrimitive> KU_PRIMITIVE_SET = Collections.unmodifiableSet(EnumSet.of(CryptoPrimitive.SIGNATURE, CryptoPrimitive.KEY_ENCAPSULATION, CryptoPrimitive.PUBLIC_KEY_ENCRYPTION, CryptoPrimitive.KEY_AGREEMENT));

    public AlgorithmChecker(TrustAnchor anchor, String variant) {
        this(anchor, null, null, variant);
    }

    public AlgorithmChecker(AlgorithmConstraints constraints, String variant) {
        this(null, constraints, null, variant);
    }

    public AlgorithmChecker(TrustAnchor anchor, AlgorithmConstraints constraints, Date date, String variant) {
        if (anchor != null) {
            this.setTrustAnchorAndKeys(anchor);
        }
        this.constraints = constraints == null ? DisabledAlgorithmConstraints.certPathConstraints() : constraints;
        this.date = date;
        this.variant = variant == null ? "generic" : variant;
    }

    public AlgorithmChecker(TrustAnchor anchor, Date date, String variant) {
        this(anchor, null, date, variant);
    }

    @Override
    public void init(boolean forward) throws CertPathValidatorException {
        if (forward) {
            throw new CertPathValidatorException("forward checking not supported");
        }
        this.prevPubKey = this.trustedPubKey;
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
        AlgorithmId algorithmId;
        X509CertImpl x509Cert;
        if (!(cert instanceof X509Certificate)) {
            return;
        }
        boolean[] keyUsage = ((X509Certificate)cert).getKeyUsage();
        if (keyUsage != null && keyUsage.length < 9) {
            throw new CertPathValidatorException("incorrect KeyUsage extension", null, null, -1, PKIXReason.INVALID_KEY_USAGE);
        }
        try {
            x509Cert = X509CertImpl.toImpl((X509Certificate)cert);
            algorithmId = x509Cert.getSigAlg();
        }
        catch (CertificateException ce) {
            throw new CertPathValidatorException(ce);
        }
        AlgorithmParameters currSigAlgParams = algorithmId.getParameters();
        PublicKey currPubKey = cert.getPublicKey();
        String currSigAlg = x509Cert.getSigAlgName();
        AlgorithmConstraints algorithmConstraints = this.constraints;
        if (algorithmConstraints instanceof DisabledAlgorithmConstraints) {
            CertPathConstraintsParameters cp;
            DisabledAlgorithmConstraints dac = (DisabledAlgorithmConstraints)algorithmConstraints;
            if (this.prevPubKey != null && this.prevPubKey == this.trustedPubKey) {
                cp = new CertPathConstraintsParameters(this.trustedPubKey, this.variant, this.anchor, this.date);
                dac.permits(this.trustedPubKey.getAlgorithm(), cp, true);
            }
            cp = new CertPathConstraintsParameters(x509Cert, this.variant, this.anchor, this.date);
            dac.permits(currSigAlg, currSigAlgParams, cp, true);
        } else {
            if (this.prevPubKey != null) {
                if (!this.constraints.permits(SIGNATURE_PRIMITIVE_SET, currSigAlg, this.prevPubKey, currSigAlgParams)) {
                    throw new CertPathValidatorException("Algorithm constraints check failed on " + currSigAlg + "signature and " + currPubKey.getAlgorithm() + " key with size of " + KeyUtil.getKeySize(currPubKey) + "bits", null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
                }
            } else if (!this.constraints.permits(SIGNATURE_PRIMITIVE_SET, currSigAlg, currSigAlgParams)) {
                throw new CertPathValidatorException("Algorithm constraints check failed on signature algorithm: " + currSigAlg, null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
            }
            Set<CryptoPrimitive> primitives = KU_PRIMITIVE_SET;
            if (keyUsage != null) {
                primitives = EnumSet.noneOf(CryptoPrimitive.class);
                if (keyUsage[0] || keyUsage[1] || keyUsage[5] || keyUsage[6]) {
                    primitives.add(CryptoPrimitive.SIGNATURE);
                }
                if (keyUsage[2]) {
                    primitives.add(CryptoPrimitive.KEY_ENCAPSULATION);
                }
                if (keyUsage[3]) {
                    primitives.add(CryptoPrimitive.PUBLIC_KEY_ENCRYPTION);
                }
                if (keyUsage[4]) {
                    primitives.add(CryptoPrimitive.KEY_AGREEMENT);
                }
                if (primitives.isEmpty()) {
                    throw new CertPathValidatorException("incorrect KeyUsage extension bits", null, null, -1, PKIXReason.INVALID_KEY_USAGE);
                }
            }
            if (!this.constraints.permits(primitives, currPubKey)) {
                throw new CertPathValidatorException("Algorithm constraints check failed on " + currPubKey.getAlgorithm() + " key with size of " + KeyUtil.getKeySize(currPubKey) + "bits", null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
            }
        }
        if (this.prevPubKey != null && PKIX.isDSAPublicKeyWithoutParams(currPubKey)) {
            if (!(this.prevPubKey instanceof DSAPublicKey)) {
                throw new CertPathValidatorException("Input key is not of a appropriate type for inheriting parameters");
            }
            DSAParams params = ((DSAPublicKey)this.prevPubKey).getParams();
            if (params == null) {
                throw new CertPathValidatorException("Key parameters missing from public key.");
            }
            try {
                BigInteger y = ((DSAPublicKey)currPubKey).getY();
                KeyFactory kf = KeyFactory.getInstance("DSA");
                DSAPublicKeySpec ks = new DSAPublicKeySpec(y, params.getP(), params.getQ(), params.getG());
                currPubKey = kf.generatePublic(ks);
            }
            catch (GeneralSecurityException e) {
                throw new CertPathValidatorException("Unable to generate key with inherited parameters: " + e.getMessage(), e);
            }
        }
        this.prevPubKey = currPubKey;
    }

    private void setTrustAnchorAndKeys(TrustAnchor anchor) {
        this.trustedPubKey = anchor.getTrustedCert() != null ? anchor.getTrustedCert().getPublicKey() : anchor.getCAPublicKey();
        this.anchor = anchor;
        this.prevPubKey = this.trustedPubKey;
    }

    void trySetTrustAnchor(TrustAnchor anchor) {
        if (this.trustedPubKey == null) {
            this.setTrustAnchorAndKeys(anchor);
        }
    }

    static void check(PublicKey key, AlgorithmId algorithmId, String variant, TrustAnchor anchor) throws CertPathValidatorException {
        DisabledAlgorithmConstraints.certPathConstraints().permits(algorithmId.getName(), algorithmId.getParameters(), new CertPathConstraintsParameters(key, variant, anchor, null), true);
    }
}

