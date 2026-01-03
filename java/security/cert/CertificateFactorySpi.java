/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.io.InputStream;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class CertificateFactorySpi {
    public abstract Certificate engineGenerateCertificate(InputStream var1) throws CertificateException;

    public CertPath engineGenerateCertPath(InputStream inStream) throws CertificateException {
        throw new UnsupportedOperationException();
    }

    public CertPath engineGenerateCertPath(InputStream inStream, String encoding) throws CertificateException {
        throw new UnsupportedOperationException();
    }

    public CertPath engineGenerateCertPath(List<? extends Certificate> certificates) throws CertificateException {
        throw new UnsupportedOperationException();
    }

    public Iterator<String> engineGetCertPathEncodings() {
        throw new UnsupportedOperationException();
    }

    public abstract Collection<? extends Certificate> engineGenerateCertificates(InputStream var1) throws CertificateException;

    public abstract CRL engineGenerateCRL(InputStream var1) throws CRLException;

    public abstract Collection<? extends CRL> engineGenerateCRLs(InputStream var1) throws CRLException;
}

