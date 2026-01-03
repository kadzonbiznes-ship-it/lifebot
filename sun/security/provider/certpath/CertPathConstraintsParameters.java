/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath;

import java.security.Key;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;
import sun.security.provider.certpath.CertPathHelper;
import sun.security.util.ConstraintsParameters;

public class CertPathConstraintsParameters
implements ConstraintsParameters {
    private final Key key;
    private final TrustAnchor anchor;
    private final Date date;
    private final String variant;
    private final X509Certificate cert;

    public CertPathConstraintsParameters(X509Certificate cert, String variant, TrustAnchor anchor, Date date) {
        this(cert.getPublicKey(), variant, anchor, date, cert);
    }

    public CertPathConstraintsParameters(Key key, String variant, TrustAnchor anchor, Date date) {
        this(key, variant, anchor, date, null);
    }

    private CertPathConstraintsParameters(Key key, String variant, TrustAnchor anchor, Date date, X509Certificate cert) {
        this.key = key;
        this.variant = variant == null ? "generic" : variant;
        this.anchor = anchor;
        this.date = date;
        this.cert = cert;
    }

    @Override
    public boolean anchorIsJdkCA() {
        return CertPathHelper.isJdkCA(this.anchor);
    }

    @Override
    public Set<Key> getKeys() {
        return this.key == null ? Set.of() : Set.of(this.key);
    }

    @Override
    public Date getDate() {
        return this.date;
    }

    @Override
    public String getVariant() {
        return this.variant;
    }

    @Override
    public String extendedExceptionMsg() {
        return this.cert == null ? "." : " used with certificate: " + this.cert.getSubjectX500Principal();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[\n");
        sb.append("  Variant: ").append(this.variant);
        if (this.anchor != null) {
            sb.append("\n  Anchor: ").append(this.anchor);
        }
        if (this.cert != null) {
            sb.append("\n  Cert Issuer: ").append(this.cert.getIssuerX500Principal());
            sb.append("\n  Cert Subject: ").append(this.cert.getSubjectX500Principal());
        }
        if (this.key != null) {
            sb.append("\n  Key: ").append(this.key.getAlgorithm());
        }
        if (this.date != null) {
            sb.append("\n  Date: ").append(this.date);
        }
        sb.append("\n]");
        return sb.toString();
    }
}

