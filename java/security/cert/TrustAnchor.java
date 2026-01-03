/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertPathHelperImpl;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import sun.security.util.AnchorCertificates;
import sun.security.x509.NameConstraintsExtension;

public class TrustAnchor {
    private final PublicKey pubKey;
    private final String caName;
    private final X500Principal caPrincipal;
    private final X509Certificate trustedCert;
    private byte[] ncBytes;
    private NameConstraintsExtension nc;
    private boolean jdkCA;
    private boolean hasJdkCABeenChecked;

    public TrustAnchor(X509Certificate trustedCert, byte[] nameConstraints) {
        if (trustedCert == null) {
            throw new NullPointerException("the trustedCert parameter must be non-null");
        }
        this.trustedCert = trustedCert;
        this.pubKey = null;
        this.caName = null;
        this.caPrincipal = null;
        this.setNameConstraints(nameConstraints);
    }

    public TrustAnchor(X500Principal caPrincipal, PublicKey pubKey, byte[] nameConstraints) {
        if (caPrincipal == null || pubKey == null) {
            throw new NullPointerException();
        }
        this.trustedCert = null;
        this.caPrincipal = caPrincipal;
        this.caName = caPrincipal.getName();
        this.pubKey = pubKey;
        this.setNameConstraints(nameConstraints);
    }

    public TrustAnchor(String caName, PublicKey pubKey, byte[] nameConstraints) {
        if (pubKey == null) {
            throw new NullPointerException("the pubKey parameter must be non-null");
        }
        if (caName == null) {
            throw new NullPointerException("the caName parameter must be non-null");
        }
        if (caName.isEmpty()) {
            throw new IllegalArgumentException("the caName parameter must be a non-empty String");
        }
        this.caPrincipal = new X500Principal(caName);
        this.pubKey = pubKey;
        this.caName = caName;
        this.trustedCert = null;
        this.setNameConstraints(nameConstraints);
    }

    public final X509Certificate getTrustedCert() {
        return this.trustedCert;
    }

    public final X500Principal getCA() {
        return this.caPrincipal;
    }

    public final String getCAName() {
        return this.caName;
    }

    public final PublicKey getCAPublicKey() {
        return this.pubKey;
    }

    private void setNameConstraints(byte[] bytes) {
        if (bytes == null) {
            this.ncBytes = null;
            this.nc = null;
        } else {
            this.ncBytes = (byte[])bytes.clone();
            try {
                this.nc = new NameConstraintsExtension(Boolean.FALSE, bytes);
            }
            catch (IOException ioe) {
                throw new IllegalArgumentException(ioe.getMessage(), ioe);
            }
        }
    }

    public final byte[] getNameConstraints() {
        return this.ncBytes == null ? null : (byte[])this.ncBytes.clone();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        if (this.pubKey != null) {
            sb.append("  Trusted CA Public Key: " + this.pubKey + "\n");
            sb.append("  Trusted CA Issuer Name: " + this.caName + "\n");
        } else {
            sb.append("  Trusted CA cert: " + this.trustedCert + "\n");
        }
        if (this.nc != null) {
            sb.append("  Name Constraints: " + this.nc + "\n");
        }
        return sb.toString();
    }

    synchronized boolean isJdkCA() {
        if (!this.hasJdkCABeenChecked) {
            if (this.trustedCert != null) {
                this.jdkCA = AnchorCertificates.contains(this.trustedCert);
            }
            this.hasJdkCABeenChecked = true;
        }
        return this.jdkCA;
    }

    static {
        CertPathHelperImpl.initialize();
    }
}

