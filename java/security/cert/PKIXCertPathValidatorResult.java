/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.security.PublicKey;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.PolicyNode;
import java.security.cert.TrustAnchor;

public class PKIXCertPathValidatorResult
implements CertPathValidatorResult {
    private final TrustAnchor trustAnchor;
    private final PolicyNode policyTree;
    private final PublicKey subjectPublicKey;

    public PKIXCertPathValidatorResult(TrustAnchor trustAnchor, PolicyNode policyTree, PublicKey subjectPublicKey) {
        if (subjectPublicKey == null) {
            throw new NullPointerException("subjectPublicKey must be non-null");
        }
        if (trustAnchor == null) {
            throw new NullPointerException("trustAnchor must be non-null");
        }
        this.trustAnchor = trustAnchor;
        this.policyTree = policyTree;
        this.subjectPublicKey = subjectPublicKey;
    }

    public TrustAnchor getTrustAnchor() {
        return this.trustAnchor;
    }

    public PolicyNode getPolicyTree() {
        return this.policyTree;
    }

    public PublicKey getPublicKey() {
        return this.subjectPublicKey;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString(), e);
        }
    }

    public String toString() {
        return "PKIXCertPathValidatorResult: [\n  Trust Anchor: " + this.trustAnchor + "\n  Policy Tree: " + this.policyTree + "\n  Subject Public Key: " + this.subjectPublicKey + "\n]";
    }
}

