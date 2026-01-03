/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath;

import java.security.InvalidAlgorithmParameterException;
import java.security.Timestamp;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.TrustAnchor;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class PKIXExtendedParameters
extends PKIXBuilderParameters {
    private final PKIXBuilderParameters p;
    private Timestamp jarTimestamp;
    private final String variant;

    public PKIXExtendedParameters(PKIXBuilderParameters params, Timestamp timestamp, String variant) throws InvalidAlgorithmParameterException {
        super(params.getTrustAnchors(), null);
        this.p = params;
        this.jarTimestamp = timestamp;
        this.variant = variant;
    }

    public Timestamp getTimestamp() {
        return this.jarTimestamp;
    }

    public void setTimestamp(Timestamp t) {
        this.jarTimestamp = t;
    }

    public String getVariant() {
        return this.variant;
    }

    @Override
    public void setDate(Date d) {
        this.p.setDate(d);
    }

    @Override
    public void addCertPathChecker(PKIXCertPathChecker c) {
        this.p.addCertPathChecker(c);
    }

    @Override
    public void setMaxPathLength(int maxPathLength) {
        this.p.setMaxPathLength(maxPathLength);
    }

    @Override
    public int getMaxPathLength() {
        return this.p.getMaxPathLength();
    }

    @Override
    public String toString() {
        return this.p.toString();
    }

    @Override
    public Set<TrustAnchor> getTrustAnchors() {
        return this.p.getTrustAnchors();
    }

    @Override
    public void setTrustAnchors(Set<TrustAnchor> trustAnchors) throws InvalidAlgorithmParameterException {
        if (this.p == null) {
            return;
        }
        this.p.setTrustAnchors(trustAnchors);
    }

    @Override
    public Set<String> getInitialPolicies() {
        return this.p.getInitialPolicies();
    }

    @Override
    public void setInitialPolicies(Set<String> initialPolicies) {
        this.p.setInitialPolicies(initialPolicies);
    }

    @Override
    public void setCertStores(List<CertStore> stores) {
        this.p.setCertStores(stores);
    }

    @Override
    public void addCertStore(CertStore store) {
        this.p.addCertStore(store);
    }

    @Override
    public List<CertStore> getCertStores() {
        return this.p.getCertStores();
    }

    @Override
    public void setRevocationEnabled(boolean val) {
        this.p.setRevocationEnabled(val);
    }

    @Override
    public boolean isRevocationEnabled() {
        return this.p.isRevocationEnabled();
    }

    @Override
    public void setExplicitPolicyRequired(boolean val) {
        this.p.setExplicitPolicyRequired(val);
    }

    @Override
    public boolean isExplicitPolicyRequired() {
        return this.p.isExplicitPolicyRequired();
    }

    @Override
    public void setPolicyMappingInhibited(boolean val) {
        this.p.setPolicyMappingInhibited(val);
    }

    @Override
    public boolean isPolicyMappingInhibited() {
        return this.p.isPolicyMappingInhibited();
    }

    @Override
    public void setAnyPolicyInhibited(boolean val) {
        this.p.setAnyPolicyInhibited(val);
    }

    @Override
    public boolean isAnyPolicyInhibited() {
        return this.p.isAnyPolicyInhibited();
    }

    @Override
    public void setPolicyQualifiersRejected(boolean qualifiersRejected) {
        this.p.setPolicyQualifiersRejected(qualifiersRejected);
    }

    @Override
    public boolean getPolicyQualifiersRejected() {
        return this.p.getPolicyQualifiersRejected();
    }

    @Override
    public Date getDate() {
        return this.p.getDate();
    }

    @Override
    public void setCertPathCheckers(List<PKIXCertPathChecker> checkers) {
        this.p.setCertPathCheckers(checkers);
    }

    @Override
    public List<PKIXCertPathChecker> getCertPathCheckers() {
        return this.p.getCertPathCheckers();
    }

    @Override
    public String getSigProvider() {
        return this.p.getSigProvider();
    }

    @Override
    public void setSigProvider(String sigProvider) {
        this.p.setSigProvider(sigProvider);
    }

    @Override
    public CertSelector getTargetCertConstraints() {
        return this.p.getTargetCertConstraints();
    }

    @Override
    public void setTargetCertConstraints(CertSelector selector) {
        if (this.p == null) {
            return;
        }
        this.p.setTargetCertConstraints(selector);
    }
}

