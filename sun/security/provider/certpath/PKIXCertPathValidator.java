/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertPath;
import java.security.cert.CertPathChecker;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertPathValidatorSpi;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXReason;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import jdk.internal.event.EventHelper;
import jdk.internal.event.X509ValidationEvent;
import sun.security.provider.certpath.AdaptableX509CertSelector;
import sun.security.provider.certpath.AlgorithmChecker;
import sun.security.provider.certpath.BasicChecker;
import sun.security.provider.certpath.ConstraintsChecker;
import sun.security.provider.certpath.KeyChecker;
import sun.security.provider.certpath.PKIX;
import sun.security.provider.certpath.PKIXMasterCertPathValidator;
import sun.security.provider.certpath.PolicyChecker;
import sun.security.provider.certpath.PolicyNodeImpl;
import sun.security.provider.certpath.RevocationChecker;
import sun.security.provider.certpath.UntrustedChecker;
import sun.security.util.Debug;
import sun.security.x509.X509CertImpl;

public final class PKIXCertPathValidator
extends CertPathValidatorSpi {
    private static final Debug debug = Debug.getInstance("certpath");
    private static final AtomicLong validationCounter = new AtomicLong();

    @Override
    public CertPathChecker engineGetRevocationChecker() {
        return new RevocationChecker();
    }

    @Override
    public CertPathValidatorResult engineValidate(CertPath cp, CertPathParameters params) throws CertPathValidatorException, InvalidAlgorithmParameterException {
        PKIX.ValidatorParams valParams = PKIX.checkParams(cp, params);
        return PKIXCertPathValidator.validate(valParams);
    }

    private static PKIXCertPathValidatorResult validate(PKIX.ValidatorParams params) throws CertPathValidatorException {
        if (debug != null) {
            debug.println("PKIXCertPathValidator.engineValidate()...");
        }
        AdaptableX509CertSelector selector = null;
        List<X509Certificate> certList = params.certificates();
        if (!certList.isEmpty()) {
            selector = new AdaptableX509CertSelector();
            X509Certificate firstCert = certList.get(0);
            selector.setSubject(firstCert.getIssuerX500Principal());
            try {
                X509CertImpl firstCertImpl = X509CertImpl.toImpl(firstCert);
                selector.setSkiAndSerialNumber(firstCertImpl.getAuthorityKeyIdentifierExtension());
            }
            catch (IOException | CertificateException exception) {
                // empty catch block
            }
        }
        CertPathValidatorException lastException = null;
        for (TrustAnchor anchor : params.trustAnchors()) {
            X509Certificate trustedCert = anchor.getTrustedCert();
            if (trustedCert != null) {
                if (selector != null && !selector.match(trustedCert)) {
                    if (debug == null || !Debug.isVerbose()) continue;
                    debug.println("NO - don't try this trustedCert");
                    continue;
                }
                if (debug != null) {
                    debug.println("YES - try this trustedCert");
                    debug.println("anchor.getTrustedCert().getSubjectX500Principal() = " + trustedCert.getSubjectX500Principal());
                }
            } else if (debug != null) {
                debug.println("PKIXCertPathValidator.engineValidate(): anchor.getTrustedCert() == null");
            }
            try {
                return PKIXCertPathValidator.validate(anchor, params);
            }
            catch (CertPathValidatorException cpe) {
                lastException = cpe;
            }
        }
        if (lastException != null) {
            throw lastException;
        }
        throw new CertPathValidatorException("Path does not chain with any of the trust anchors", null, null, -1, PKIXReason.NO_TRUST_ANCHOR);
    }

    private static PKIXCertPathValidatorResult validate(TrustAnchor anchor, PKIX.ValidatorParams params) throws CertPathValidatorException {
        UntrustedChecker untrustedChecker = new UntrustedChecker();
        X509Certificate anchorCert = anchor.getTrustedCert();
        if (anchorCert != null) {
            untrustedChecker.check(anchorCert);
        }
        int certPathLen = params.certificates().size();
        ArrayList<PKIXCertPathChecker> certPathCheckers = new ArrayList<PKIXCertPathChecker>();
        certPathCheckers.add(untrustedChecker);
        certPathCheckers.add(new AlgorithmChecker(anchor, params.timestamp(), params.variant()));
        certPathCheckers.add(new KeyChecker(certPathLen, params.targetCertConstraints()));
        certPathCheckers.add(new ConstraintsChecker(certPathLen));
        PolicyNodeImpl rootNode = new PolicyNodeImpl(null, PolicyChecker.ANY_POLICY, null, false, Collections.singleton(PolicyChecker.ANY_POLICY), false);
        PolicyChecker pc = new PolicyChecker(params.initialPolicies(), certPathLen, params.explicitPolicyRequired(), params.policyMappingInhibited(), params.anyPolicyInhibited(), params.policyQualifiersRejected(), rootNode);
        certPathCheckers.add(pc);
        BasicChecker bc = new BasicChecker(anchor, params.date(), params.sigProvider(), false);
        certPathCheckers.add(bc);
        boolean revCheckerAdded = false;
        List<PKIXCertPathChecker> checkers = params.certPathCheckers();
        for (PKIXCertPathChecker checker : checkers) {
            if (!(checker instanceof PKIXRevocationChecker)) continue;
            if (revCheckerAdded) {
                throw new CertPathValidatorException("Only one PKIXRevocationChecker can be specified");
            }
            revCheckerAdded = true;
            if (!(checker instanceof RevocationChecker)) continue;
            ((RevocationChecker)checker).init(anchor, params);
        }
        if (params.revocationEnabled() && !revCheckerAdded) {
            certPathCheckers.add(new RevocationChecker(anchor, params));
        }
        certPathCheckers.addAll(checkers);
        PKIXMasterCertPathValidator.validate(params.certPath(), params.certificates(), certPathCheckers);
        X509ValidationEvent xve = new X509ValidationEvent();
        if (xve.shouldCommit() || EventHelper.isLoggingSecurity()) {
            long[] certIds = params.certificates().stream().mapToInt(Certificate::hashCode).mapToLong(Integer::toUnsignedLong).toArray();
            int hash = anchorCert != null ? anchorCert.hashCode() : anchor.getCAPublicKey().hashCode();
            long anchorCertId = Integer.toUnsignedLong(hash);
            if (xve.shouldCommit()) {
                int certificatePos;
                xve.certificateId = anchorCertId;
                xve.certificatePosition = certificatePos = 1;
                xve.validationCounter = validationCounter.incrementAndGet();
                xve.commit();
                long[] lArray = certIds;
                int n = lArray.length;
                for (int i = 0; i < n; ++i) {
                    long id;
                    xve.certificateId = id = lArray[i];
                    xve.certificatePosition = ++certificatePos;
                    xve.commit();
                }
            }
            if (EventHelper.isLoggingSecurity()) {
                EventHelper.logX509ValidationEvent(anchorCertId, certIds);
            }
        }
        return new PKIXCertPathValidatorResult(anchor, pc.getPolicyTree(), bc.getPublicKey());
    }
}

