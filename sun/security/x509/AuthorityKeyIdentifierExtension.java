/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.Extension;
import sun.security.x509.GeneralNames;
import sun.security.x509.KeyIdentifier;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.SerialNumber;

public class AuthorityKeyIdentifierExtension
extends Extension {
    public static final String NAME = "AuthorityKeyIdentifier";
    private static final byte TAG_ID = 0;
    private static final byte TAG_NAMES = 1;
    private static final byte TAG_SERIAL_NUM = 2;
    private KeyIdentifier id = null;
    private GeneralNames names = null;
    private SerialNumber serialNum = null;

    private void encodeThis() {
        DerOutputStream tmp1;
        if (this.id == null && this.names == null && this.serialNum == null) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream seq = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        if (this.id != null) {
            tmp1 = new DerOutputStream();
            this.id.encode(tmp1);
            tmp.writeImplicit(DerValue.createTag((byte)-128, false, (byte)0), tmp1);
        }
        if (this.names != null) {
            tmp1 = new DerOutputStream();
            this.names.encode(tmp1);
            tmp.writeImplicit(DerValue.createTag((byte)-128, true, (byte)1), tmp1);
        }
        if (this.serialNum != null) {
            tmp1 = new DerOutputStream();
            this.serialNum.encode(tmp1);
            tmp.writeImplicit(DerValue.createTag((byte)-128, false, (byte)2), tmp1);
        }
        seq.write((byte)48, tmp);
        this.extensionValue = seq.toByteArray();
    }

    public AuthorityKeyIdentifierExtension(KeyIdentifier kid, GeneralNames names, SerialNumber sn) {
        if (kid == null && names == null && sn == null) {
            throw new IllegalArgumentException("AuthorityKeyIdentifierExtension cannot be empty");
        }
        this.id = kid;
        this.names = names;
        this.serialNum = sn;
        this.extensionId = PKIXExtensions.AuthorityKey_Id;
        this.critical = false;
        this.encodeThis();
    }

    public AuthorityKeyIdentifierExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = PKIXExtensions.AuthorityKey_Id;
        this.critical = critical;
        this.extensionValue = (byte[])value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag != 48) {
            throw new IOException("Invalid encoding for AuthorityKeyIdentifierExtension.");
        }
        while (val.data != null && val.data.available() != 0) {
            DerValue opt = val.data.getDerValue();
            if (opt.isContextSpecific((byte)0) && !opt.isConstructed()) {
                if (this.id != null) {
                    throw new IOException("Duplicate KeyIdentifier in AuthorityKeyIdentifier.");
                }
                opt.resetTag((byte)4);
                this.id = new KeyIdentifier(opt);
                continue;
            }
            if (opt.isContextSpecific((byte)1) && opt.isConstructed()) {
                if (this.names != null) {
                    throw new IOException("Duplicate GeneralNames in AuthorityKeyIdentifier.");
                }
                opt.resetTag((byte)48);
                this.names = new GeneralNames(opt);
                continue;
            }
            if (opt.isContextSpecific((byte)2) && !opt.isConstructed()) {
                if (this.serialNum != null) {
                    throw new IOException("Duplicate SerialNumber in AuthorityKeyIdentifier.");
                }
                opt.resetTag((byte)2);
                this.serialNum = new SerialNumber(opt);
                continue;
            }
            throw new IOException("Invalid encoding of AuthorityKeyIdentifierExtension.");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append("AuthorityKeyIdentifier [\n");
        if (this.id != null) {
            sb.append(this.id);
        }
        if (this.names != null) {
            sb.append(this.names).append('\n');
        }
        if (this.serialNum != null) {
            sb.append(this.serialNum).append('\n');
        }
        sb.append("]\n");
        return sb.toString();
    }

    @Override
    public void encode(DerOutputStream out) {
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.AuthorityKey_Id;
            this.critical = false;
            this.encodeThis();
        }
        super.encode(out);
    }

    public KeyIdentifier getKeyIdentifier() {
        return this.id;
    }

    public GeneralNames getAuthName() {
        return this.names;
    }

    public SerialNumber getSerialNumber() {
        return this.serialNum;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public byte[] getEncodedKeyIdentifier() throws IOException {
        if (this.id != null) {
            DerOutputStream derOut = new DerOutputStream();
            this.id.encode(derOut);
            return derOut.toByteArray();
        }
        return null;
    }
}

