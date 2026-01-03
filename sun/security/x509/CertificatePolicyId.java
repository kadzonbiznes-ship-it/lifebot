/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.util.Objects;
import sun.security.util.DerEncoder;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class CertificatePolicyId
implements DerEncoder {
    private final ObjectIdentifier id;

    public CertificatePolicyId(ObjectIdentifier id) {
        this.id = Objects.requireNonNull(id);
    }

    public CertificatePolicyId(DerValue val) throws IOException {
        this.id = val.getOID();
    }

    public ObjectIdentifier getIdentifier() {
        return this.id;
    }

    public String toString() {
        return "CertificatePolicyId: [" + this.id.toString() + "]\n";
    }

    @Override
    public void encode(DerOutputStream out) {
        out.putOID(this.id);
    }

    public boolean equals(Object other) {
        if (other instanceof CertificatePolicyId) {
            return this.id.equals(((CertificatePolicyId)other).getIdentifier());
        }
        return false;
    }

    public int hashCode() {
        return this.id.hashCode();
    }
}

