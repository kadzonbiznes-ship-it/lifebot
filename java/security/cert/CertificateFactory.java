/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactorySpi;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import sun.security.jca.GetInstance;
import sun.security.jca.JCAUtil;

public class CertificateFactory {
    private final String type;
    private final Provider provider;
    private final CertificateFactorySpi certFacSpi;

    protected CertificateFactory(CertificateFactorySpi certFacSpi, Provider provider, String type) {
        this.certFacSpi = certFacSpi;
        this.provider = provider;
        this.type = type;
    }

    public static final CertificateFactory getInstance(String type) throws CertificateException {
        Objects.requireNonNull(type, "null type name");
        try {
            GetInstance.Instance instance = GetInstance.getInstance("CertificateFactory", CertificateFactorySpi.class, type);
            return new CertificateFactory((CertificateFactorySpi)instance.impl, instance.provider, type);
        }
        catch (NoSuchAlgorithmException e) {
            throw new CertificateException(type + " not found", e);
        }
    }

    public static final CertificateFactory getInstance(String type, String provider) throws CertificateException, NoSuchProviderException {
        Objects.requireNonNull(type, "null type name");
        try {
            GetInstance.Instance instance = GetInstance.getInstance("CertificateFactory", CertificateFactorySpi.class, type, provider);
            return new CertificateFactory((CertificateFactorySpi)instance.impl, instance.provider, type);
        }
        catch (NoSuchAlgorithmException e) {
            throw new CertificateException(type + " not found", e);
        }
    }

    public static final CertificateFactory getInstance(String type, Provider provider) throws CertificateException {
        Objects.requireNonNull(type, "null type name");
        try {
            GetInstance.Instance instance = GetInstance.getInstance("CertificateFactory", CertificateFactorySpi.class, type, provider);
            return new CertificateFactory((CertificateFactorySpi)instance.impl, instance.provider, type);
        }
        catch (NoSuchAlgorithmException e) {
            throw new CertificateException(type + " not found", e);
        }
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public final String getType() {
        return this.type;
    }

    public final Certificate generateCertificate(InputStream inStream) throws CertificateException {
        Certificate c = this.certFacSpi.engineGenerateCertificate(inStream);
        JCAUtil.tryCommitCertEvent(c);
        return c;
    }

    public final Iterator<String> getCertPathEncodings() {
        return this.certFacSpi.engineGetCertPathEncodings();
    }

    public final CertPath generateCertPath(InputStream inStream) throws CertificateException {
        return this.certFacSpi.engineGenerateCertPath(inStream);
    }

    public final CertPath generateCertPath(InputStream inStream, String encoding) throws CertificateException {
        return this.certFacSpi.engineGenerateCertPath(inStream, encoding);
    }

    public final CertPath generateCertPath(List<? extends Certificate> certificates) throws CertificateException {
        return this.certFacSpi.engineGenerateCertPath(certificates);
    }

    public final Collection<? extends Certificate> generateCertificates(InputStream inStream) throws CertificateException {
        return this.certFacSpi.engineGenerateCertificates(inStream);
    }

    public final CRL generateCRL(InputStream inStream) throws CRLException {
        return this.certFacSpi.engineGenerateCRL(inStream);
    }

    public final Collection<? extends CRL> generateCRLs(InputStream inStream) throws CRLException {
        return this.certFacSpi.engineGenerateCRLs(inStream);
    }
}

