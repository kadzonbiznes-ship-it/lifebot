/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.Extension;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNames;
import sun.security.x509.PKIXExtensions;

public class SubjectAlternativeNameExtension
extends Extension {
    public static final String NAME = "SubjectAlternativeName";
    GeneralNames names;

    private void encodeThis() {
        if (this.names == null || this.names.isEmpty()) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream os = new DerOutputStream();
        this.names.encode(os);
        this.extensionValue = os.toByteArray();
    }

    public SubjectAlternativeNameExtension(GeneralNames names) {
        this(Boolean.FALSE, names);
    }

    public SubjectAlternativeNameExtension(Boolean critical, GeneralNames names) {
        if (names == null || names.isEmpty()) {
            throw new IllegalArgumentException("names cannot be null or empty");
        }
        this.names = names;
        this.extensionId = PKIXExtensions.SubjectAlternativeName_Id;
        this.critical = critical;
        this.encodeThis();
    }

    public SubjectAlternativeNameExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = PKIXExtensions.SubjectAlternativeName_Id;
        this.critical = critical;
        this.extensionValue = (byte[])value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.data == null) {
            this.names = new GeneralNames();
            return;
        }
        this.names = new GeneralNames(val);
    }

    @Override
    public String toString() {
        String result = super.toString() + "SubjectAlternativeName [\n";
        if (this.names == null) {
            result = result + "  null\n";
        } else {
            for (GeneralName name : this.names.names()) {
                result = result + "  " + name + "\n";
            }
        }
        result = result + "]\n";
        return result;
    }

    @Override
    public void encode(DerOutputStream out) {
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.SubjectAlternativeName_Id;
            this.critical = false;
            this.encodeThis();
        }
        super.encode(out);
    }

    public GeneralNames getNames() {
        return this.names;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

