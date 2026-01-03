/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.Extension;
import sun.security.x509.PKIXExtensions;

public class BasicConstraintsExtension
extends Extension {
    public static final String NAME = "BasicConstraints";
    private boolean ca = false;
    private int pathLen = -1;

    private void encodeThis() {
        DerOutputStream out = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        if (this.ca) {
            tmp.putBoolean(true);
            if (this.pathLen >= 0) {
                tmp.putInteger(this.pathLen);
            }
        }
        out.write((byte)48, tmp);
        this.extensionValue = out.toByteArray();
    }

    public BasicConstraintsExtension(boolean ca, int len) {
        this(ca, ca, len);
    }

    public BasicConstraintsExtension(Boolean critical, boolean ca, int len) {
        this.ca = ca;
        this.pathLen = len;
        this.extensionId = PKIXExtensions.BasicConstraints_Id;
        this.critical = critical;
        this.encodeThis();
    }

    public BasicConstraintsExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = PKIXExtensions.BasicConstraints_Id;
        this.critical = critical;
        this.extensionValue = (byte[])value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag != 48) {
            throw new IOException("Invalid encoding of BasicConstraints");
        }
        if (val.data == null || val.data.available() == 0) {
            return;
        }
        DerValue opt = val.data.getDerValue();
        if (opt.tag != 1) {
            return;
        }
        this.ca = opt.getBoolean();
        if (val.data.available() == 0) {
            this.pathLen = Integer.MAX_VALUE;
            return;
        }
        opt = val.data.getDerValue();
        if (opt.tag != 2) {
            throw new IOException("Invalid encoding of BasicConstraints");
        }
        this.pathLen = opt.getInteger();
    }

    @Override
    public String toString() {
        String pathLenAsString = this.pathLen < 0 ? " undefined" : (this.pathLen == Integer.MAX_VALUE ? " no limit" : String.valueOf(this.pathLen));
        return super.toString() + "BasicConstraints:[\n  CA:" + this.ca + "\n  PathLen:" + pathLenAsString + "\n]\n";
    }

    @Override
    public void encode(DerOutputStream out) {
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.BasicConstraints_Id;
            this.critical = this.ca;
            this.encodeThis();
        }
        super.encode(out);
    }

    public boolean isCa() {
        return this.ca;
    }

    public int getPathLen() {
        return this.pathLen;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

