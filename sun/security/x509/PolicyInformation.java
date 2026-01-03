/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.security.cert.PolicyQualifierInfo;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import sun.security.util.DerEncoder;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.CertificatePolicyId;

public class PolicyInformation
implements DerEncoder {
    public static final String NAME = "PolicyInformation";
    public static final String ID = "id";
    public static final String QUALIFIERS = "qualifiers";
    private CertificatePolicyId policyIdentifier;
    private Set<PolicyQualifierInfo> policyQualifiers;

    public PolicyInformation(CertificatePolicyId policyIdentifier, Set<PolicyQualifierInfo> policyQualifiers) throws IOException {
        if (policyQualifiers == null) {
            throw new NullPointerException("policyQualifiers is null");
        }
        this.policyQualifiers = new LinkedHashSet<PolicyQualifierInfo>(policyQualifiers);
        this.policyIdentifier = Objects.requireNonNull(policyIdentifier);
    }

    public PolicyInformation(DerValue val) throws IOException {
        if (val.tag != 48) {
            throw new IOException("Invalid encoding of PolicyInformation");
        }
        this.policyIdentifier = new CertificatePolicyId(val.data.getDerValue());
        if (val.data.available() != 0) {
            this.policyQualifiers = new LinkedHashSet<PolicyQualifierInfo>();
            DerValue opt = val.data.getDerValue();
            if (opt.tag != 48) {
                throw new IOException("Invalid encoding of PolicyInformation");
            }
            if (opt.data.available() == 0) {
                throw new IOException("No data available in policyQualifiers");
            }
            while (opt.data.available() != 0) {
                this.policyQualifiers.add(new PolicyQualifierInfo(opt.data.getDerValue().toByteArray()));
            }
        } else {
            this.policyQualifiers = Collections.emptySet();
        }
    }

    public boolean equals(Object other) {
        if (!(other instanceof PolicyInformation)) {
            return false;
        }
        PolicyInformation piOther = (PolicyInformation)other;
        if (!this.policyIdentifier.equals(piOther.getPolicyIdentifier())) {
            return false;
        }
        return this.policyQualifiers.equals(piOther.getPolicyQualifiers());
    }

    public int hashCode() {
        int myhash = 37 + this.policyIdentifier.hashCode();
        myhash = 37 * myhash + this.policyQualifiers.hashCode();
        return myhash;
    }

    public CertificatePolicyId getPolicyIdentifier() {
        return this.policyIdentifier;
    }

    public Set<PolicyQualifierInfo> getPolicyQualifiers() {
        return this.policyQualifiers;
    }

    public String toString() {
        return "  [" + this.policyIdentifier + this.policyQualifiers + "  ]\n";
    }

    @Override
    public void encode(DerOutputStream out) {
        DerOutputStream tmp = new DerOutputStream();
        this.policyIdentifier.encode(tmp);
        if (!this.policyQualifiers.isEmpty()) {
            DerOutputStream tmp2 = new DerOutputStream();
            for (PolicyQualifierInfo pq : this.policyQualifiers) {
                tmp2.writeBytes(pq.getEncoded());
            }
            tmp.write((byte)48, tmp2);
        }
        out.write((byte)48, tmp);
    }
}

