/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.Provider;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Extension;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import sun.security.util.SignatureUtil;
import sun.security.x509.X509CertImpl;

public abstract class X509Certificate
extends Certificate
implements X509Extension {
    private static final long serialVersionUID = -2491127588187038216L;
    private transient X500Principal subjectX500Principal;
    private transient X500Principal issuerX500Principal;

    protected X509Certificate() {
        super("X.509");
    }

    public abstract void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException;

    public abstract void checkValidity(Date var1) throws CertificateExpiredException, CertificateNotYetValidException;

    public abstract int getVersion();

    public abstract BigInteger getSerialNumber();

    @Deprecated(since="16")
    public abstract Principal getIssuerDN();

    public X500Principal getIssuerX500Principal() {
        if (this.issuerX500Principal == null) {
            this.issuerX500Principal = X509CertImpl.getIssuerX500Principal(this);
        }
        return this.issuerX500Principal;
    }

    @Deprecated(since="16")
    public abstract Principal getSubjectDN();

    public X500Principal getSubjectX500Principal() {
        if (this.subjectX500Principal == null) {
            this.subjectX500Principal = X509CertImpl.getSubjectX500Principal(this);
        }
        return this.subjectX500Principal;
    }

    public abstract Date getNotBefore();

    public abstract Date getNotAfter();

    public abstract byte[] getTBSCertificate() throws CertificateEncodingException;

    public abstract byte[] getSignature();

    public abstract String getSigAlgName();

    public abstract String getSigAlgOID();

    public abstract byte[] getSigAlgParams();

    public abstract boolean[] getIssuerUniqueID();

    public abstract boolean[] getSubjectUniqueID();

    public abstract boolean[] getKeyUsage();

    public List<String> getExtendedKeyUsage() throws CertificateParsingException {
        return X509CertImpl.getExtendedKeyUsage(this);
    }

    public abstract int getBasicConstraints();

    public Collection<List<?>> getSubjectAlternativeNames() throws CertificateParsingException {
        return X509CertImpl.getSubjectAlternativeNames(this);
    }

    public Collection<List<?>> getIssuerAlternativeNames() throws CertificateParsingException {
        return X509CertImpl.getIssuerAlternativeNames(this);
    }

    @Override
    public void verify(PublicKey key, Provider sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String sigName = this.getSigAlgName();
        Signature sig = sigProvider == null ? Signature.getInstance(sigName) : Signature.getInstance(sigName, sigProvider);
        try {
            SignatureUtil.initVerifyWithParam(sig, key, SignatureUtil.getParamSpec(sigName, this.getSigAlgParams()));
        }
        catch (ProviderException e) {
            throw new CertificateException(e.getMessage(), e.getCause());
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new CertificateException(e);
        }
        byte[] tbsCert = this.getTBSCertificate();
        sig.update(tbsCert, 0, tbsCert.length);
        if (!sig.verify(this.getSignature())) {
            throw new SignatureException("Signature does not match.");
        }
    }
}

