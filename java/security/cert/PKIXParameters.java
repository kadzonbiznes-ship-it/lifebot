/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertPathParameters;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PKIXParameters
implements CertPathParameters {
    private Set<TrustAnchor> unmodTrustAnchors;
    private Date date;
    private List<PKIXCertPathChecker> certPathCheckers;
    private String sigProvider;
    private boolean revocationEnabled = true;
    private Set<String> unmodInitialPolicies;
    private boolean explicitPolicyRequired = false;
    private boolean policyMappingInhibited = false;
    private boolean anyPolicyInhibited = false;
    private boolean policyQualifiersRejected = true;
    private List<CertStore> certStores;
    private CertSelector certSelector;

    public PKIXParameters(Set<TrustAnchor> trustAnchors) throws InvalidAlgorithmParameterException {
        this.setTrustAnchors(trustAnchors);
        this.unmodInitialPolicies = Collections.emptySet();
        this.certPathCheckers = new ArrayList<PKIXCertPathChecker>();
        this.certStores = new ArrayList<CertStore>();
    }

    public PKIXParameters(KeyStore keystore) throws KeyStoreException, InvalidAlgorithmParameterException {
        if (keystore == null) {
            throw new NullPointerException("the keystore parameter must be non-null");
        }
        HashSet<TrustAnchor> hashSet = new HashSet<TrustAnchor>();
        Enumeration<String> aliases = keystore.aliases();
        while (aliases.hasMoreElements()) {
            Certificate cert;
            String alias = aliases.nextElement();
            if (!keystore.isCertificateEntry(alias) || !((cert = keystore.getCertificate(alias)) instanceof X509Certificate)) continue;
            hashSet.add(new TrustAnchor((X509Certificate)cert, null));
        }
        this.setTrustAnchors(hashSet);
        this.unmodInitialPolicies = Collections.emptySet();
        this.certPathCheckers = new ArrayList<PKIXCertPathChecker>();
        this.certStores = new ArrayList<CertStore>();
    }

    public Set<TrustAnchor> getTrustAnchors() {
        return this.unmodTrustAnchors;
    }

    public void setTrustAnchors(Set<TrustAnchor> trustAnchors) throws InvalidAlgorithmParameterException {
        if (trustAnchors == null) {
            throw new NullPointerException("the trustAnchors parameters must be non-null");
        }
        if (trustAnchors.isEmpty()) {
            throw new InvalidAlgorithmParameterException("the trustAnchors parameter must be non-empty");
        }
        for (TrustAnchor trustAnchor : trustAnchors) {
            if (trustAnchor instanceof TrustAnchor) continue;
            throw new ClassCastException("all elements of set must be of type java.security.cert.TrustAnchor");
        }
        this.unmodTrustAnchors = Collections.unmodifiableSet(new HashSet<TrustAnchor>(trustAnchors));
    }

    public Set<String> getInitialPolicies() {
        return this.unmodInitialPolicies;
    }

    public void setInitialPolicies(Set<String> initialPolicies) {
        if (initialPolicies != null) {
            for (String initialPolicy : initialPolicies) {
                if (initialPolicy instanceof String) continue;
                throw new ClassCastException("all elements of set must be of type java.lang.String");
            }
            this.unmodInitialPolicies = Collections.unmodifiableSet(new HashSet<String>(initialPolicies));
        } else {
            this.unmodInitialPolicies = Collections.emptySet();
        }
    }

    public void setCertStores(List<CertStore> stores) {
        if (stores == null) {
            this.certStores = new ArrayList<CertStore>();
        } else {
            for (CertStore store : stores) {
                if (store instanceof CertStore) continue;
                throw new ClassCastException("all elements of list must be of type java.security.cert.CertStore");
            }
            this.certStores = new ArrayList<CertStore>(stores);
        }
    }

    public void addCertStore(CertStore store) {
        if (store != null) {
            this.certStores.add(store);
        }
    }

    public List<CertStore> getCertStores() {
        return Collections.unmodifiableList(new ArrayList<CertStore>(this.certStores));
    }

    public void setRevocationEnabled(boolean val) {
        this.revocationEnabled = val;
    }

    public boolean isRevocationEnabled() {
        return this.revocationEnabled;
    }

    public void setExplicitPolicyRequired(boolean val) {
        this.explicitPolicyRequired = val;
    }

    public boolean isExplicitPolicyRequired() {
        return this.explicitPolicyRequired;
    }

    public void setPolicyMappingInhibited(boolean val) {
        this.policyMappingInhibited = val;
    }

    public boolean isPolicyMappingInhibited() {
        return this.policyMappingInhibited;
    }

    public void setAnyPolicyInhibited(boolean val) {
        this.anyPolicyInhibited = val;
    }

    public boolean isAnyPolicyInhibited() {
        return this.anyPolicyInhibited;
    }

    public void setPolicyQualifiersRejected(boolean qualifiersRejected) {
        this.policyQualifiersRejected = qualifiersRejected;
    }

    public boolean getPolicyQualifiersRejected() {
        return this.policyQualifiersRejected;
    }

    public Date getDate() {
        if (this.date == null) {
            return null;
        }
        return (Date)this.date.clone();
    }

    public void setDate(Date date) {
        if (date != null) {
            this.date = (Date)date.clone();
        } else {
            date = null;
        }
    }

    public void setCertPathCheckers(List<PKIXCertPathChecker> checkers) {
        if (checkers != null) {
            ArrayList<PKIXCertPathChecker> tmpList = new ArrayList<PKIXCertPathChecker>();
            for (PKIXCertPathChecker checker : checkers) {
                tmpList.add((PKIXCertPathChecker)checker.clone());
            }
            this.certPathCheckers = tmpList;
        } else {
            this.certPathCheckers = new ArrayList<PKIXCertPathChecker>();
        }
    }

    public List<PKIXCertPathChecker> getCertPathCheckers() {
        ArrayList<PKIXCertPathChecker> tmpList = new ArrayList<PKIXCertPathChecker>();
        for (PKIXCertPathChecker ck : this.certPathCheckers) {
            tmpList.add((PKIXCertPathChecker)ck.clone());
        }
        return Collections.unmodifiableList(tmpList);
    }

    public void addCertPathChecker(PKIXCertPathChecker checker) {
        if (checker != null) {
            this.certPathCheckers.add((PKIXCertPathChecker)checker.clone());
        }
    }

    public String getSigProvider() {
        return this.sigProvider;
    }

    public void setSigProvider(String sigProvider) {
        this.sigProvider = sigProvider;
    }

    public CertSelector getTargetCertConstraints() {
        if (this.certSelector != null) {
            return (CertSelector)this.certSelector.clone();
        }
        return null;
    }

    public void setTargetCertConstraints(CertSelector selector) {
        this.certSelector = selector != null ? (CertSelector)selector.clone() : null;
    }

    @Override
    public Object clone() {
        try {
            PKIXParameters copy = (PKIXParameters)super.clone();
            if (this.certStores != null) {
                copy.certStores = new ArrayList<CertStore>(this.certStores);
            }
            if (this.certPathCheckers != null) {
                copy.certPathCheckers = new ArrayList<PKIXCertPathChecker>(this.certPathCheckers.size());
                for (PKIXCertPathChecker checker : this.certPathCheckers) {
                    copy.certPathCheckers.add((PKIXCertPathChecker)checker.clone());
                }
            }
            return copy;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString(), e);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        if (this.unmodTrustAnchors != null) {
            sb.append("  Trust Anchors: " + this.unmodTrustAnchors + "\n");
        }
        if (this.unmodInitialPolicies != null) {
            if (this.unmodInitialPolicies.isEmpty()) {
                sb.append("  Initial Policy OIDs: any\n");
            } else {
                sb.append("  Initial Policy OIDs: [" + this.unmodInitialPolicies + "]\n");
            }
        }
        sb.append("  Validity Date: " + this.date + "\n");
        sb.append("  Signature Provider: " + this.sigProvider + "\n");
        sb.append("  Default Revocation Enabled: " + this.revocationEnabled + "\n");
        sb.append("  Explicit Policy Required: " + this.explicitPolicyRequired + "\n");
        sb.append("  Policy Mapping Inhibited: " + this.policyMappingInhibited + "\n");
        sb.append("  Any Policy Inhibited: " + this.anyPolicyInhibited + "\n");
        sb.append("  Policy Qualifiers Rejected: " + this.policyQualifiersRejected + "\n");
        sb.append("  Target Cert Constraints: " + this.certSelector + "\n");
        if (this.certPathCheckers != null) {
            sb.append("  Certification Path Checkers: [" + this.certPathCheckers + "]\n");
        }
        if (this.certStores != null) {
            sb.append("  CertStores: [" + this.certStores + "]\n");
        }
        sb.append("]");
        return sb.toString();
    }
}

