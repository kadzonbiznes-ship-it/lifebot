/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.Extension;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.PolicyInformation;

public class CertificatePoliciesExtension
extends Extension {
    public static final String NAME = "CertificatePolicies";
    private List<PolicyInformation> certPolicies;

    private void encodeThis() {
        if (this.certPolicies == null || this.certPolicies.isEmpty()) {
            this.extensionValue = null;
        } else {
            DerOutputStream os = new DerOutputStream();
            DerOutputStream tmp = new DerOutputStream();
            for (PolicyInformation info : this.certPolicies) {
                info.encode(tmp);
            }
            os.write((byte)48, tmp);
            this.extensionValue = os.toByteArray();
        }
    }

    public CertificatePoliciesExtension(List<PolicyInformation> certPolicies) {
        this(Boolean.FALSE, certPolicies);
    }

    public CertificatePoliciesExtension(Boolean critical, List<PolicyInformation> certPolicies) {
        if (certPolicies == null || certPolicies.isEmpty()) {
            throw new IllegalArgumentException("certificate policies cannot be null or empty");
        }
        this.certPolicies = certPolicies;
        this.extensionId = PKIXExtensions.CertificatePolicies_Id;
        this.critical = critical;
        this.encodeThis();
    }

    public CertificatePoliciesExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = PKIXExtensions.CertificatePolicies_Id;
        this.critical = critical;
        this.extensionValue = (byte[])value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag != 48) {
            throw new IOException("Invalid encoding for CertificatePoliciesExtension.");
        }
        this.certPolicies = new ArrayList<PolicyInformation>();
        while (val.data.available() != 0) {
            DerValue seq = val.data.getDerValue();
            PolicyInformation policy = new PolicyInformation(seq);
            this.certPolicies.add(policy);
        }
    }

    @Override
    public String toString() {
        if (this.certPolicies == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append("CertificatePolicies [\n");
        for (PolicyInformation info : this.certPolicies) {
            sb.append(info);
        }
        sb.append("]\n");
        return sb.toString();
    }

    @Override
    public void encode(DerOutputStream out) {
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.CertificatePolicies_Id;
            this.critical = false;
            this.encodeThis();
        }
        super.encode(out);
    }

    public List<PolicyInformation> getCertPolicies() {
        return this.certPolicies;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

