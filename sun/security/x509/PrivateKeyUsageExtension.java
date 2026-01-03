/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.util.Date;
import java.util.Objects;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.Extension;
import sun.security.x509.PKIXExtensions;

public class PrivateKeyUsageExtension
extends Extension {
    public static final String NAME = "PrivateKeyUsage";
    private static final byte TAG_BEFORE = 0;
    private static final byte TAG_AFTER = 1;
    private Date notBefore = null;
    private Date notAfter = null;

    private void encodeThis() {
        DerOutputStream tmp;
        if (this.notBefore == null && this.notAfter == null) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream seq = new DerOutputStream();
        DerOutputStream tagged = new DerOutputStream();
        if (this.notBefore != null) {
            tmp = new DerOutputStream();
            tmp.putGeneralizedTime(this.notBefore);
            tagged.writeImplicit(DerValue.createTag((byte)-128, false, (byte)0), tmp);
        }
        if (this.notAfter != null) {
            tmp = new DerOutputStream();
            tmp.putGeneralizedTime(this.notAfter);
            tagged.writeImplicit(DerValue.createTag((byte)-128, false, (byte)1), tmp);
        }
        seq.write((byte)48, tagged);
        this.extensionValue = seq.toByteArray();
    }

    public PrivateKeyUsageExtension(Date notBefore, Date notAfter) {
        if (notBefore == null && notAfter == null) {
            throw new IllegalArgumentException("notBefore and notAfter cannot both be null");
        }
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.extensionId = PKIXExtensions.PrivateKeyUsage_Id;
        this.critical = false;
        this.encodeThis();
    }

    public PrivateKeyUsageExtension(Boolean critical, Object value) throws CertificateException, IOException {
        this.extensionId = PKIXExtensions.PrivateKeyUsage_Id;
        this.critical = critical;
        this.extensionValue = (byte[])value;
        DerInputStream str = new DerInputStream(this.extensionValue);
        DerValue[] seq = str.getSequence(2);
        for (int i = 0; i < seq.length; ++i) {
            DerValue opt = seq[i];
            if (opt.isContextSpecific((byte)0) && !opt.isConstructed()) {
                if (this.notBefore != null) {
                    throw new CertificateParsingException("Duplicate notBefore in PrivateKeyUsage.");
                }
                opt.resetTag((byte)24);
                str = new DerInputStream(opt.toByteArray());
                this.notBefore = str.getGeneralizedTime();
                continue;
            }
            if (opt.isContextSpecific((byte)1) && !opt.isConstructed()) {
                if (this.notAfter != null) {
                    throw new CertificateParsingException("Duplicate notAfter in PrivateKeyUsage.");
                }
                opt.resetTag((byte)24);
                str = new DerInputStream(opt.toByteArray());
                this.notAfter = str.getGeneralizedTime();
                continue;
            }
            throw new IOException("Invalid encoding of PrivateKeyUsageExtension");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append("PrivateKeyUsage: [\n");
        if (this.notBefore != null) {
            sb.append("From: ").append(this.notBefore);
            if (this.notAfter != null) {
                sb.append(", ");
            }
        }
        if (this.notAfter != null) {
            sb.append("To: ").append(this.notAfter);
        }
        sb.append("]\n");
        return sb.toString();
    }

    public void valid() throws CertificateNotYetValidException, CertificateExpiredException {
        Date now = new Date();
        this.valid(now);
    }

    public void valid(Date now) throws CertificateNotYetValidException, CertificateExpiredException {
        Objects.requireNonNull(now);
        if (this.notBefore != null && this.notBefore.after(now)) {
            throw new CertificateNotYetValidException("NotBefore: " + this.notBefore.toString());
        }
        if (this.notAfter != null && this.notAfter.before(now)) {
            throw new CertificateExpiredException("NotAfter: " + this.notAfter.toString());
        }
    }

    @Override
    public void encode(DerOutputStream out) {
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.PrivateKeyUsage_Id;
            this.critical = false;
            this.encodeThis();
        }
        super.encode(out);
    }

    public Date getNotBefore() {
        return new Date(this.notBefore.getTime());
    }

    public Date getNotAfter() {
        return new Date(this.notAfter.getTime());
    }

    @Override
    public String getName() {
        return NAME;
    }
}

