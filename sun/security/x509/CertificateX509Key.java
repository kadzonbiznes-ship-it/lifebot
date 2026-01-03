/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.X509Key;

public class CertificateX509Key
implements DerEncoder {
    public static final String NAME = "key";
    private PublicKey key;

    public CertificateX509Key(PublicKey key) {
        this.key = key;
    }

    public CertificateX509Key(DerInputStream in) throws IOException {
        DerValue val = in.getDerValue();
        this.key = X509Key.parse(val);
    }

    public CertificateX509Key(InputStream in) throws IOException {
        DerValue val = new DerValue(in);
        this.key = X509Key.parse(val);
    }

    public String toString() {
        if (this.key == null) {
            return "";
        }
        return this.key.toString();
    }

    @Override
    public void encode(DerOutputStream out) {
        out.writeBytes(this.key.getEncoded());
    }

    public PublicKey getKey() {
        return this.key;
    }
}

