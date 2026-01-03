/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath;

import java.security.InvalidAlgorithmParameterException;
import java.security.PublicKey;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertPathParameters;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.provider.certpath.PKIXExtendedParameters;
import sun.security.util.Debug;

class PKIX {
    private static final Debug debug = Debug.getInstance("certpath");

    private PKIX() {
    }

    static boolean isDSAPublicKeyWithoutParams(PublicKey publicKey) {
        return publicKey instanceof DSAPublicKey && ((DSAPublicKey)publicKey).getParams() == null;
    }

    static ValidatorParams checkParams(CertPath cp, CertPathParameters params) throws InvalidAlgorithmParameterException {
        if (!(params instanceof PKIXParameters)) {
            throw new InvalidAlgorithmParameterException("inappropriate params, must be an instance of PKIXParameters");
        }
        return new ValidatorParams(cp, (PKIXParameters)params);
    }

    static BuilderParams checkBuilderParams(CertPathParameters params) throws InvalidAlgorithmParameterException {
        if (!(params instanceof PKIXBuilderParameters)) {
            throw new InvalidAlgorithmParameterException("inappropriate params, must be an instance of PKIXBuilderParameters");
        }
        return new BuilderParams((PKIXBuilderParameters)params);
    }

    static class ValidatorParams {
        private final PKIXParameters params;
        private CertPath certPath;
        private List<PKIXCertPathChecker> checkers;
        private List<CertStore> stores;
        private boolean gotDate;
        private Date date;
        private Set<String> policies;
        private boolean gotConstraints;
        private CertSelector constraints;
        private final Set<TrustAnchor> anchors;
        private List<X509Certificate> certs;
        private Timestamp timestamp;
        private Date timestampDate;
        private String variant = "generic";

        ValidatorParams(CertPath cp, PKIXParameters params) throws InvalidAlgorithmParameterException {
            this(params);
            if (!cp.getType().equals("X.509") && !cp.getType().equals("X509")) {
                throw new InvalidAlgorithmParameterException("inappropriate CertPath type specified, must be X.509 or X509");
            }
            this.certPath = cp;
        }

        ValidatorParams(PKIXParameters params) throws InvalidAlgorithmParameterException {
            if (params instanceof PKIXExtendedParameters) {
                this.timestamp = ((PKIXExtendedParameters)params).getTimestamp();
                this.variant = ((PKIXExtendedParameters)params).getVariant();
            }
            this.anchors = params.getTrustAnchors();
            for (TrustAnchor anchor : this.anchors) {
                if (anchor.getNameConstraints() == null) continue;
                throw new InvalidAlgorithmParameterException("name constraints in trust anchor not supported");
            }
            this.params = params;
        }

        CertPath certPath() {
            return this.certPath;
        }

        void setCertPath(CertPath cp) {
            this.certPath = cp;
        }

        List<X509Certificate> certificates() {
            if (this.certs == null) {
                if (this.certPath == null) {
                    this.certs = Collections.emptyList();
                } else {
                    ArrayList<? extends Certificate> xc = new ArrayList<Certificate>(this.certPath.getCertificates());
                    Collections.reverse(xc);
                    this.certs = xc;
                }
            }
            return this.certs;
        }

        List<PKIXCertPathChecker> certPathCheckers() {
            if (this.checkers == null) {
                this.checkers = this.params.getCertPathCheckers();
            }
            return this.checkers;
        }

        List<CertStore> certStores() {
            if (this.stores == null) {
                this.stores = this.params.getCertStores();
            }
            return this.stores;
        }

        Date date() {
            if (!this.gotDate) {
                if (this.timestamp != null && this.variant.equals("code signing")) {
                    this.date = this.timestamp.getTimestamp();
                } else {
                    this.date = this.params.getDate();
                    if (this.date == null) {
                        this.date = new Date();
                    }
                }
                this.gotDate = true;
            }
            return this.date;
        }

        Set<String> initialPolicies() {
            if (this.policies == null) {
                this.policies = this.params.getInitialPolicies();
            }
            return this.policies;
        }

        CertSelector targetCertConstraints() {
            if (!this.gotConstraints) {
                this.constraints = this.params.getTargetCertConstraints();
                this.gotConstraints = true;
            }
            return this.constraints;
        }

        Set<TrustAnchor> trustAnchors() {
            return this.anchors;
        }

        boolean revocationEnabled() {
            return this.params.isRevocationEnabled();
        }

        boolean policyMappingInhibited() {
            return this.params.isPolicyMappingInhibited();
        }

        boolean explicitPolicyRequired() {
            return this.params.isExplicitPolicyRequired();
        }

        boolean policyQualifiersRejected() {
            return this.params.getPolicyQualifiersRejected();
        }

        String sigProvider() {
            return this.params.getSigProvider();
        }

        boolean anyPolicyInhibited() {
            return this.params.isAnyPolicyInhibited();
        }

        PKIXParameters getPKIXParameters() {
            return this.params;
        }

        String variant() {
            return this.variant;
        }

        Date timestamp() {
            if (this.timestampDate == null) {
                this.timestampDate = this.timestamp != null ? this.timestamp.getTimestamp() : this.date();
            }
            return this.timestampDate;
        }
    }

    static class BuilderParams
    extends ValidatorParams {
        private PKIXBuilderParameters params;
        private List<CertStore> stores;
        private X500Principal targetSubject;

        BuilderParams(PKIXBuilderParameters params) throws InvalidAlgorithmParameterException {
            super(params);
            this.checkParams(params);
        }

        private void checkParams(PKIXBuilderParameters params) throws InvalidAlgorithmParameterException {
            CertSelector sel = this.targetCertConstraints();
            if (!(sel instanceof X509CertSelector)) {
                throw new InvalidAlgorithmParameterException("the targetCertConstraints parameter must be an X509CertSelector");
            }
            this.params = params;
            this.targetSubject = BuilderParams.getTargetSubject(this.certStores(), (X509CertSelector)this.targetCertConstraints());
        }

        @Override
        List<CertStore> certStores() {
            if (this.stores == null) {
                this.stores = new ArrayList<CertStore>(this.params.getCertStores());
                this.stores.sort(new CertStoreComparator());
            }
            return this.stores;
        }

        int maxPathLength() {
            return this.params.getMaxPathLength();
        }

        PKIXBuilderParameters params() {
            return this.params;
        }

        X500Principal targetSubject() {
            return this.targetSubject;
        }

        private static X500Principal getTargetSubject(List<CertStore> stores, X509CertSelector sel) throws InvalidAlgorithmParameterException {
            X500Principal subject = sel.getSubject();
            if (subject != null) {
                return subject;
            }
            X509Certificate cert = sel.getCertificate();
            if (cert != null) {
                subject = cert.getSubjectX500Principal();
            }
            if (subject != null) {
                return subject;
            }
            for (CertStore store : stores) {
                try {
                    Collection<? extends Certificate> certs = store.getCertificates(sel);
                    if (certs.isEmpty()) continue;
                    X509Certificate xc = (X509Certificate)certs.iterator().next();
                    return xc.getSubjectX500Principal();
                }
                catch (CertStoreException e) {
                    if (debug == null) continue;
                    debug.println("BuilderParams.getTargetSubjectDN: non-fatal exception retrieving certs: " + e);
                    e.printStackTrace();
                }
            }
            throw new InvalidAlgorithmParameterException("Could not determine unique target subject");
        }
    }

    private static class CertStoreComparator
    implements Comparator<CertStore> {
        private CertStoreComparator() {
        }

        @Override
        public int compare(CertStore store1, CertStore store2) {
            if (store1.getType().equals("Collection") || store1.getCertStoreParameters() instanceof CollectionCertStoreParameters) {
                return -1;
            }
            return 1;
        }
    }

    static class CertStoreTypeException
    extends CertStoreException {
        private static final long serialVersionUID = 7463352639238322556L;
        private final String type;

        CertStoreTypeException(String type, CertStoreException cse) {
            super(cse.getMessage(), cse.getCause());
            this.type = type;
        }

        String getType() {
            return this.type;
        }
    }
}

