/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificateVersion
implements DerEncoder {
    public static final int V1 = 0;
    public static final int V2 = 1;
    public static final int V3 = 2;
    public static final String NAME = "version";
    int version = 0;

    public int getVersion() {
        return this.version;
    }

    private void construct(DerValue derVal) throws IOException {
        if (derVal.isConstructed() && derVal.isContextSpecific()) {
            derVal = derVal.data.getDerValue();
            this.version = derVal.getInteger();
            if (derVal.data.available() != 0) {
                throw new IOException("X.509 version, bad format");
            }
        }
    }

    public CertificateVersion() {
        this.version = 0;
    }

    public CertificateVersion(int version) throws IOException {
        if (version != 0 && version != 1 && version != 2) {
            throw new IOException("X.509 Certificate version " + version + " not supported.\n");
        }
        this.version = version;
    }

    public CertificateVersion(DerInputStream in) throws IOException {
        this.version = 0;
        DerValue derVal = in.getDerValue();
        this.construct(derVal);
    }

    public CertificateVersion(InputStream in) throws IOException {
        this.version = 0;
        DerValue derVal = new DerValue(in);
        this.construct(derVal);
    }

    public CertificateVersion(DerValue val) throws IOException {
        this.version = 0;
        this.construct(val);
    }

    public String toString() {
        return "Version: V" + (this.version + 1);
    }

    @Override
    public void encode(DerOutputStream out) {
        if (this.version == 0) {
            return;
        }
        DerOutputStream tmp = new DerOutputStream();
        tmp.putInteger(this.version);
        out.write(DerValue.createTag((byte)-128, true, (byte)0), tmp);
    }

    public int compare(int vers) {
        return this.version - vers;
    }
}

