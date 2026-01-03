/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class Extension
implements java.security.cert.Extension,
DerEncoder {
    protected ObjectIdentifier extensionId = null;
    protected boolean critical = false;
    protected byte[] extensionValue = null;
    private static final int hashMagic = 31;

    public Extension() {
    }

    public Extension(DerValue derVal) throws IOException {
        DerInputStream in = derVal.toDerInputStream();
        this.extensionId = in.getOID();
        DerValue val = in.getDerValue();
        if (val.tag == 1) {
            this.critical = val.getBoolean();
            val = in.getDerValue();
        } else {
            this.critical = false;
        }
        this.extensionValue = val.getOctetString();
    }

    public Extension(ObjectIdentifier extensionId, boolean critical, byte[] extensionValue) throws IOException {
        this.extensionId = extensionId;
        this.critical = critical;
        DerValue inDerVal = new DerValue(extensionValue);
        this.extensionValue = inDerVal.getOctetString();
    }

    public Extension(Extension ext) {
        this.extensionId = ext.extensionId;
        this.critical = ext.critical;
        this.extensionValue = ext.extensionValue;
    }

    public static Extension newExtension(ObjectIdentifier extensionId, boolean critical, byte[] rawExtensionValue) throws IOException {
        Extension ext = new Extension();
        ext.extensionId = extensionId;
        ext.critical = critical;
        ext.extensionValue = rawExtensionValue;
        return ext;
    }

    @Override
    public final void encode(OutputStream out) throws IOException {
        if (out == null) {
            throw new NullPointerException();
        }
        if (out instanceof DerOutputStream) {
            DerOutputStream dos = (DerOutputStream)out;
            this.encode(dos);
        } else {
            DerOutputStream dos = new DerOutputStream();
            this.encode(dos);
            out.write(dos.toByteArray());
        }
    }

    @Override
    public void encode(DerOutputStream out) {
        Objects.requireNonNull(this.extensionId, "No OID to encode for the extension");
        Objects.requireNonNull(this.extensionValue, "No value to encode for the extension");
        DerOutputStream dos = new DerOutputStream();
        dos.putOID(this.extensionId);
        if (this.critical) {
            dos.putBoolean(true);
        }
        dos.putOctetString(this.extensionValue);
        out.write((byte)48, dos);
    }

    @Override
    public boolean isCritical() {
        return this.critical;
    }

    public ObjectIdentifier getExtensionId() {
        return this.extensionId;
    }

    @Override
    public byte[] getValue() {
        return (byte[])this.extensionValue.clone();
    }

    public byte[] getExtensionValue() {
        return this.extensionValue;
    }

    public String getName() {
        return this.getId();
    }

    @Override
    public String getId() {
        return this.extensionId.toString();
    }

    public String toString() {
        return "ObjectId: " + this.extensionId + " Criticality=" + this.critical + '\n';
    }

    public int hashCode() {
        int h = 0;
        if (this.extensionValue != null) {
            byte[] val = this.extensionValue;
            int len = val.length;
            while (len > 0) {
                h += len-- * val[len];
            }
        }
        h = h * 31 + this.extensionId.hashCode();
        h = h * 31 + (this.critical ? 1231 : 1237);
        return h;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Extension)) {
            return false;
        }
        Extension otherExt = (Extension)other;
        if (this.critical != otherExt.critical) {
            return false;
        }
        if (!this.extensionId.equals(otherExt.extensionId)) {
            return false;
        }
        return Arrays.equals(this.extensionValue, otherExt.extensionValue);
    }
}

