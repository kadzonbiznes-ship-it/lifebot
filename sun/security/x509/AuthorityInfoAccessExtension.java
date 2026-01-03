/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AccessDescription;
import sun.security.x509.Extension;
import sun.security.x509.PKIXExtensions;

public class AuthorityInfoAccessExtension
extends Extension {
    public static final String NAME = "AuthorityInfoAccess";
    private List<AccessDescription> accessDescriptions;

    public AuthorityInfoAccessExtension(List<AccessDescription> accessDescriptions) {
        if (accessDescriptions == null || accessDescriptions.isEmpty()) {
            throw new IllegalArgumentException("accessDescriptions is null or empty");
        }
        this.extensionId = PKIXExtensions.AuthInfoAccess_Id;
        this.critical = false;
        this.accessDescriptions = accessDescriptions;
        this.encodeThis();
    }

    public AuthorityInfoAccessExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = PKIXExtensions.AuthInfoAccess_Id;
        this.critical = critical;
        if (!(value instanceof byte[])) {
            throw new IOException("Illegal argument type");
        }
        this.extensionValue = (byte[])value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag != 48) {
            throw new IOException("Invalid encoding for AuthorityInfoAccessExtension.");
        }
        this.accessDescriptions = new ArrayList<AccessDescription>();
        while (val.data.available() != 0) {
            DerValue seq = val.data.getDerValue();
            AccessDescription accessDescription = new AccessDescription(seq);
            this.accessDescriptions.add(accessDescription);
        }
    }

    public List<AccessDescription> getAccessDescriptions() {
        return this.accessDescriptions;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void encode(DerOutputStream out) {
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.AuthInfoAccess_Id;
            this.critical = false;
            this.encodeThis();
        }
        super.encode(out);
    }

    private void encodeThis() {
        if (this.accessDescriptions.isEmpty()) {
            this.extensionValue = null;
        } else {
            DerOutputStream ads = new DerOutputStream();
            for (AccessDescription accessDescription : this.accessDescriptions) {
                accessDescription.encode(ads);
            }
            DerOutputStream seq = new DerOutputStream();
            seq.write((byte)48, ads);
            this.extensionValue = seq.toByteArray();
        }
    }

    @Override
    public String toString() {
        return super.toString() + "AuthorityInfoAccess [\n  " + this.accessDescriptions + "\n]\n";
    }
}

