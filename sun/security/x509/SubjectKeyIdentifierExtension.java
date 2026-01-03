/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.Extension;
import sun.security.x509.KeyIdentifier;
import sun.security.x509.PKIXExtensions;

public class SubjectKeyIdentifierExtension
extends Extension {
    public static final String NAME = "SubjectKeyIdentifier";
    private KeyIdentifier id;

    private void encodeThis() {
        if (this.id == null) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream os = new DerOutputStream();
        this.id.encode(os);
        this.extensionValue = os.toByteArray();
    }

    public SubjectKeyIdentifierExtension(byte[] octetString) {
        this.id = new KeyIdentifier(octetString);
        this.extensionId = PKIXExtensions.SubjectKey_Id;
        this.critical = false;
        this.encodeThis();
    }

    public SubjectKeyIdentifierExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = PKIXExtensions.SubjectKey_Id;
        this.critical = critical;
        this.extensionValue = (byte[])value;
        DerValue val = new DerValue(this.extensionValue);
        this.id = new KeyIdentifier(val);
    }

    @Override
    public String toString() {
        return super.toString() + "SubjectKeyIdentifier [\n" + this.id + "]\n";
    }

    @Override
    public void encode(DerOutputStream out) {
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.SubjectKey_Id;
            this.critical = false;
            this.encodeThis();
        }
        super.encode(out);
    }

    public KeyIdentifier getKeyIdentifier() {
        return this.id;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

