/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Date;
import java.util.Objects;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificateValidity
implements DerEncoder {
    public static final String NAME = "validity";
    static final long YR_2050 = 2524608000000L;
    private final Date notBefore;
    private final Date notAfter;

    public Date getNotBefore() {
        return new Date(this.notBefore.getTime());
    }

    public Date getNotAfter() {
        return new Date(this.notAfter.getTime());
    }

    public CertificateValidity(Date notBefore, Date notAfter) {
        this.notBefore = Objects.requireNonNull(notBefore);
        this.notAfter = Objects.requireNonNull(notAfter);
    }

    public CertificateValidity(DerInputStream in) throws IOException {
        DerValue derVal = in.getDerValue();
        if (derVal.tag != 48) {
            throw new IOException("Invalid encoded CertificateValidity, starting sequence tag missing.");
        }
        if (derVal.data.available() == 0) {
            throw new IOException("No data encoded for CertificateValidity");
        }
        DerInputStream derIn = new DerInputStream(derVal.toByteArray());
        DerValue[] seq = derIn.getSequence(2);
        if (seq.length != 2) {
            throw new IOException("Invalid encoding for CertificateValidity");
        }
        if (seq[0].tag == 23) {
            this.notBefore = derVal.data.getUTCTime();
        } else if (seq[0].tag == 24) {
            this.notBefore = derVal.data.getGeneralizedTime();
        } else {
            throw new IOException("Invalid encoding for CertificateValidity");
        }
        if (seq[1].tag == 23) {
            this.notAfter = derVal.data.getUTCTime();
        } else if (seq[1].tag == 24) {
            this.notAfter = derVal.data.getGeneralizedTime();
        } else {
            throw new IOException("Invalid encoding for CertificateValidity");
        }
    }

    public String toString() {
        return "Validity: [From: " + this.notBefore + ",\n               To: " + this.notAfter + ']';
    }

    @Override
    public void encode(DerOutputStream out) {
        DerOutputStream pair = new DerOutputStream();
        if (this.notBefore.getTime() < 2524608000000L) {
            pair.putUTCTime(this.notBefore);
        } else {
            pair.putGeneralizedTime(this.notBefore);
        }
        if (this.notAfter.getTime() < 2524608000000L) {
            pair.putUTCTime(this.notAfter);
        } else {
            pair.putGeneralizedTime(this.notAfter);
        }
        out.write((byte)48, pair);
    }

    public void valid() throws CertificateNotYetValidException, CertificateExpiredException {
        Date now = new Date();
        this.valid(now);
    }

    public void valid(Date now) throws CertificateNotYetValidException, CertificateExpiredException {
        if (this.notBefore.after(now)) {
            throw new CertificateNotYetValidException("NotBefore: " + this.notBefore.toString());
        }
        if (this.notAfter.before(now)) {
            throw new CertificateExpiredException("NotAfter: " + this.notAfter.toString());
        }
    }
}

