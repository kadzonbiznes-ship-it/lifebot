/*
 * Decompiled with CFR 0.152.
 */
package sun.security.validator;

import java.security.AlgorithmConstraints;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathValidator;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.action.GetBooleanAction;
import sun.security.provider.certpath.AlgorithmChecker;
import sun.security.provider.certpath.PKIXExtendedParameters;
import sun.security.util.SecurityProperties;
import sun.security.validator.Validator;
import sun.security.validator.ValidatorException;

public final class PKIXValidator
extends Validator {
    private static final boolean checkTLSRevocation = GetBooleanAction.privilegedGetProperty("com.sun.net.ssl.checkRevocation");
    private static final boolean ALLOW_NON_CA_ANCHOR = PKIXValidator.allowNonCaAnchor();
    private final Set<X509Certificate> trustedCerts;
    private final PKIXBuilderParameters parameterTemplate;
    private int certPathLength = -1;
    private final Map<X500Principal, List<PublicKey>> trustedSubjects;
    private final CertificateFactory factory;

    private static boolean allowNonCaAnchor() {
        String prop = SecurityProperties.privilegedGetOverridable("jdk.security.allowNonCaAnchor");
        return prop != null && (prop.isEmpty() || prop.equalsIgnoreCase("true"));
    }

    PKIXValidator(String variant, Collection<X509Certificate> trustedCerts) {
        super("PKIX", variant);
        this.trustedCerts = trustedCerts instanceof Set ? (Set<Object>)trustedCerts : new HashSet<X509Certificate>(trustedCerts);
        HashSet<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
        for (X509Certificate cert : trustedCerts) {
            trustAnchors.add(new TrustAnchor(cert, null));
        }
        try {
            this.parameterTemplate = new PKIXBuilderParameters(trustAnchors, null);
            this.factory = CertificateFactory.getInstance("X.509");
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Unexpected error: " + e.toString(), e);
        }
        catch (CertificateException e) {
            throw new RuntimeException("Internal error", e);
        }
        this.setDefaultParameters(variant);
        this.trustedSubjects = this.setTrustedSubjects();
    }

    PKIXValidator(String variant, PKIXBuilderParameters params) {
        super("PKIX", variant);
        this.trustedCerts = new HashSet<X509Certificate>();
        for (TrustAnchor anchor : params.getTrustAnchors()) {
            X509Certificate cert = anchor.getTrustedCert();
            if (cert == null) continue;
            this.trustedCerts.add(cert);
        }
        this.parameterTemplate = params;
        try {
            this.factory = CertificateFactory.getInstance("X.509");
        }
        catch (CertificateException e) {
            throw new RuntimeException("Internal error", e);
        }
        this.trustedSubjects = this.setTrustedSubjects();
    }

    private Map<X500Principal, List<PublicKey>> setTrustedSubjects() {
        HashMap<X500Principal, List<PublicKey>> subjectMap = new HashMap<X500Principal, List<PublicKey>>();
        for (X509Certificate cert : this.trustedCerts) {
            List<PublicKey> keys;
            X500Principal dn = cert.getSubjectX500Principal();
            if (subjectMap.containsKey(dn)) {
                keys = (List)subjectMap.get(dn);
            } else {
                keys = new ArrayList();
                subjectMap.put(dn, keys);
            }
            keys.add(cert.getPublicKey());
        }
        return subjectMap;
    }

    @Override
    public Collection<X509Certificate> getTrustedCertificates() {
        return this.trustedCerts;
    }

    public int getCertPathLength() {
        return this.certPathLength;
    }

    private void setDefaultParameters(String variant) {
        if (variant == "tls server" || variant == "tls client") {
            this.parameterTemplate.setRevocationEnabled(checkTLSRevocation);
        } else {
            this.parameterTemplate.setRevocationEnabled(false);
        }
    }

    public PKIXBuilderParameters getParameters() {
        return this.parameterTemplate;
    }

    @Override
    X509Certificate[] engineValidate(X509Certificate[] chain, Collection<X509Certificate> otherCerts, List<byte[]> responseList, AlgorithmConstraints constraints, Object parameter) throws CertificateException {
        if (chain == null || chain.length == 0) {
            throw new CertificateException("null or zero-length certificate chain");
        }
        PKIXParameters pkixParameters = null;
        try {
            pkixParameters = new PKIXExtendedParameters((PKIXBuilderParameters)this.parameterTemplate.clone(), parameter instanceof Timestamp ? (Timestamp)parameter : null, this.variant);
        }
        catch (InvalidAlgorithmParameterException invalidAlgorithmParameterException) {
            // empty catch block
        }
        if (constraints != null) {
            pkixParameters.addCertPathChecker(new AlgorithmChecker(constraints, this.variant));
        }
        if (!responseList.isEmpty()) {
            PKIXValidator.addResponses(pkixParameters, chain, responseList);
        }
        X500Principal prevIssuer = null;
        for (int i = 0; i < chain.length; ++i) {
            X509Certificate cert = chain[i];
            X500Principal dn = cert.getSubjectX500Principal();
            if (i == 0) {
                if (this.trustedCerts.contains(cert)) {
                    return new X509Certificate[]{chain[0]};
                }
            } else {
                if (!dn.equals(prevIssuer)) {
                    return this.doBuild(chain, otherCerts, (PKIXBuilderParameters)pkixParameters);
                }
                if (this.trustedCerts.contains(cert) || this.trustedSubjects.containsKey(dn) && this.trustedSubjects.get(dn).contains(cert.getPublicKey())) {
                    X509Certificate[] newChain = new X509Certificate[i];
                    System.arraycopy(chain, 0, newChain, 0, i);
                    return this.doValidate(newChain, (PKIXBuilderParameters)pkixParameters);
                }
            }
            prevIssuer = cert.getIssuerX500Principal();
        }
        X509Certificate last = chain[chain.length - 1];
        X500Principal issuer = last.getIssuerX500Principal();
        X500Principal subject = last.getSubjectX500Principal();
        if (this.trustedSubjects.containsKey(issuer)) {
            return this.doValidate(chain, (PKIXBuilderParameters)pkixParameters);
        }
        return this.doBuild(chain, otherCerts, (PKIXBuilderParameters)pkixParameters);
    }

    private static X509Certificate[] toArray(CertPath path, TrustAnchor anchor) throws CertificateException {
        X509Certificate trustedCert = anchor.getTrustedCert();
        if (trustedCert == null) {
            throw new ValidatorException("TrustAnchor must be specified as certificate");
        }
        PKIXValidator.verifyTrustAnchor(trustedCert);
        List<? extends Certificate> list = path.getCertificates();
        X509Certificate[] chain = new X509Certificate[list.size() + 1];
        list.toArray(chain);
        chain[chain.length - 1] = trustedCert;
        return chain;
    }

    private void setDate(PKIXBuilderParameters params) {
        Date date = this.validationDate;
        if (date != null) {
            params.setDate(date);
        }
    }

    private X509Certificate[] doValidate(X509Certificate[] chain, PKIXBuilderParameters params) throws CertificateException {
        try {
            this.setDate(params);
            CertPathValidator validator = CertPathValidator.getInstance("PKIX");
            CertPath path = this.factory.generateCertPath(Arrays.asList(chain));
            this.certPathLength = chain.length;
            PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult)validator.validate(path, params);
            return PKIXValidator.toArray(path, result.getTrustAnchor());
        }
        catch (GeneralSecurityException e) {
            throw new ValidatorException("PKIX path validation failed: " + e.toString(), e);
        }
    }

    private static void verifyTrustAnchor(X509Certificate trustedCert) throws ValidatorException {
        if (ALLOW_NON_CA_ANCHOR) {
            return;
        }
        if (trustedCert.getVersion() < 3) {
            return;
        }
        if (trustedCert.getBasicConstraints() == -1) {
            throw new ValidatorException("TrustAnchor with subject \"" + trustedCert.getSubjectX500Principal() + "\" is not a CA certificate");
        }
        boolean[] keyUsageBits = trustedCert.getKeyUsage();
        if (keyUsageBits != null && !keyUsageBits[5]) {
            throw new ValidatorException("TrustAnchor with subject \"" + trustedCert.getSubjectX500Principal() + "\" does not have keyCertSign bit set in KeyUsage extension");
        }
    }

    private X509Certificate[] doBuild(X509Certificate[] chain, Collection<X509Certificate> otherCerts, PKIXBuilderParameters params) throws CertificateException {
        try {
            this.setDate(params);
            X509CertSelector selector = new X509CertSelector();
            selector.setCertificate(chain[0]);
            params.setTargetCertConstraints(selector);
            ArrayList<X509Certificate> certs = new ArrayList<X509Certificate>();
            certs.addAll(Arrays.asList(chain));
            if (otherCerts != null) {
                certs.addAll(otherCerts);
            }
            CertStore store = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certs));
            params.addCertStore(store);
            CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
            PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult)builder.build(params);
            return PKIXValidator.toArray(result.getCertPath(), result.getTrustAnchor());
        }
        catch (GeneralSecurityException e) {
            throw new ValidatorException("PKIX path building failed: " + e.toString(), e);
        }
    }

    private static void addResponses(PKIXBuilderParameters pkixParams, X509Certificate[] chain, List<byte[]> responseList) {
        try {
            boolean createdRevChk = false;
            PKIXRevocationChecker revChecker = null;
            List<PKIXCertPathChecker> checkerList = pkixParams.getCertPathCheckers();
            for (PKIXCertPathChecker checker : checkerList) {
                if (!(checker instanceof PKIXRevocationChecker)) continue;
                revChecker = (PKIXRevocationChecker)checker;
                break;
            }
            if (revChecker == null) {
                if (pkixParams.isRevocationEnabled()) {
                    revChecker = (PKIXRevocationChecker)CertPathValidator.getInstance("PKIX").getRevocationChecker();
                    createdRevChk = true;
                } else {
                    return;
                }
            }
            Map<X509Certificate, byte[]> responseMap = revChecker.getOcspResponses();
            int limit = Integer.min(chain.length, responseList.size());
            for (int idx = 0; idx < limit; ++idx) {
                byte[] respBytes = responseList.get(idx);
                if (respBytes == null || respBytes.length <= 0 || responseMap.containsKey(chain[idx])) continue;
                responseMap.put(chain[idx], respBytes);
            }
            revChecker.setOcspResponses(responseMap);
            if (createdRevChk) {
                pkixParams.addCertPathChecker(revChecker);
            } else {
                pkixParams.setCertPathCheckers(checkerList);
            }
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            // empty catch block
        }
    }
}

