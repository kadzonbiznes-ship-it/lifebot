/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath;

import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.PolicyNode;
import java.security.cert.PolicyQualifierInfo;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import sun.security.provider.certpath.PolicyNodeImpl;
import sun.security.util.Debug;
import sun.security.util.KnownOIDs;
import sun.security.x509.CertificatePoliciesExtension;
import sun.security.x509.CertificatePolicyMap;
import sun.security.x509.InhibitAnyPolicyExtension;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.PolicyConstraintsExtension;
import sun.security.x509.PolicyInformation;
import sun.security.x509.PolicyMappingsExtension;
import sun.security.x509.X509CertImpl;

class PolicyChecker
extends PKIXCertPathChecker {
    private final Set<String> initPolicies;
    private final int certPathLen;
    private final boolean expPolicyRequired;
    private final boolean polMappingInhibited;
    private final boolean anyPolicyInhibited;
    private final boolean rejectPolicyQualifiers;
    private PolicyNodeImpl rootNode;
    private int explicitPolicy;
    private int policyMapping;
    private int inhibitAnyPolicy;
    private int certIndex;
    private Set<String> supportedExts;
    private static final Debug debug = Debug.getInstance("certpath");
    static final String ANY_POLICY = KnownOIDs.CE_CERT_POLICIES_ANY.value();

    PolicyChecker(Set<String> initialPolicies, int certPathLen, boolean expPolicyRequired, boolean polMappingInhibited, boolean anyPolicyInhibited, boolean rejectPolicyQualifiers, PolicyNodeImpl rootNode) {
        if (initialPolicies.isEmpty()) {
            this.initPolicies = HashSet.newHashSet(1);
            this.initPolicies.add(ANY_POLICY);
        } else {
            this.initPolicies = new HashSet<String>(initialPolicies);
        }
        this.certPathLen = certPathLen;
        this.expPolicyRequired = expPolicyRequired;
        this.polMappingInhibited = polMappingInhibited;
        this.anyPolicyInhibited = anyPolicyInhibited;
        this.rejectPolicyQualifiers = rejectPolicyQualifiers;
        this.rootNode = rootNode;
    }

    @Override
    public void init(boolean forward) throws CertPathValidatorException {
        if (forward) {
            throw new CertPathValidatorException("forward checking not supported");
        }
        this.certIndex = 1;
        this.explicitPolicy = this.expPolicyRequired ? 0 : this.certPathLen + 1;
        this.policyMapping = this.polMappingInhibited ? 0 : this.certPathLen + 1;
        this.inhibitAnyPolicy = this.anyPolicyInhibited ? 0 : this.certPathLen + 1;
    }

    @Override
    public boolean isForwardCheckingSupported() {
        return false;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        if (this.supportedExts == null) {
            this.supportedExts = HashSet.newHashSet(4);
            this.supportedExts.add(PKIXExtensions.CertificatePolicies_Id.toString());
            this.supportedExts.add(PKIXExtensions.PolicyMappings_Id.toString());
            this.supportedExts.add(PKIXExtensions.PolicyConstraints_Id.toString());
            this.supportedExts.add(PKIXExtensions.InhibitAnyPolicy_Id.toString());
            this.supportedExts = Collections.unmodifiableSet(this.supportedExts);
        }
        return this.supportedExts;
    }

    @Override
    public void check(Certificate cert, Collection<String> unresCritExts) throws CertPathValidatorException {
        this.checkPolicy((X509Certificate)cert);
        if (unresCritExts != null && !unresCritExts.isEmpty()) {
            unresCritExts.remove(PKIXExtensions.CertificatePolicies_Id.toString());
            unresCritExts.remove(PKIXExtensions.PolicyMappings_Id.toString());
            unresCritExts.remove(PKIXExtensions.PolicyConstraints_Id.toString());
            unresCritExts.remove(PKIXExtensions.InhibitAnyPolicy_Id.toString());
        }
    }

    private void checkPolicy(X509Certificate currCert) throws CertPathValidatorException {
        X509CertImpl currCertImpl;
        String msg = "certificate policies";
        if (debug != null) {
            debug.println("PolicyChecker.checkPolicy() ---checking " + msg + "...");
            debug.println("PolicyChecker.checkPolicy() certIndex = " + this.certIndex);
            debug.println("PolicyChecker.checkPolicy() BEFORE PROCESSING: explicitPolicy = " + this.explicitPolicy);
            debug.println("PolicyChecker.checkPolicy() BEFORE PROCESSING: policyMapping = " + this.policyMapping);
            debug.println("PolicyChecker.checkPolicy() BEFORE PROCESSING: inhibitAnyPolicy = " + this.inhibitAnyPolicy);
            debug.println("PolicyChecker.checkPolicy() BEFORE PROCESSING: policyTree = " + this.rootNode);
        }
        try {
            currCertImpl = X509CertImpl.toImpl(currCert);
        }
        catch (CertificateException ce) {
            throw new CertPathValidatorException(ce);
        }
        boolean finalCert = this.certIndex == this.certPathLen;
        this.rootNode = PolicyChecker.processPolicies(this.certIndex, this.initPolicies, this.explicitPolicy, this.policyMapping, this.inhibitAnyPolicy, this.rejectPolicyQualifiers, this.rootNode, currCertImpl, finalCert);
        if (!finalCert) {
            this.explicitPolicy = PolicyChecker.mergeExplicitPolicy(this.explicitPolicy, currCertImpl, false);
            this.policyMapping = PolicyChecker.mergePolicyMapping(this.policyMapping, currCertImpl);
            this.inhibitAnyPolicy = PolicyChecker.mergeInhibitAnyPolicy(this.inhibitAnyPolicy, currCertImpl);
        }
        ++this.certIndex;
        if (debug != null) {
            debug.println("PolicyChecker.checkPolicy() AFTER PROCESSING: explicitPolicy = " + this.explicitPolicy);
            debug.println("PolicyChecker.checkPolicy() AFTER PROCESSING: policyMapping = " + this.policyMapping);
            debug.println("PolicyChecker.checkPolicy() AFTER PROCESSING: inhibitAnyPolicy = " + this.inhibitAnyPolicy);
            debug.println("PolicyChecker.checkPolicy() AFTER PROCESSING: policyTree = " + this.rootNode);
            debug.println("PolicyChecker.checkPolicy() " + msg + " verified");
        }
    }

    static int mergeExplicitPolicy(int explicitPolicy, X509CertImpl currCert, boolean finalCert) throws CertPathValidatorException {
        PolicyConstraintsExtension polConstExt;
        if (explicitPolicy > 0 && !X509CertImpl.isSelfIssued(currCert)) {
            --explicitPolicy;
        }
        if ((polConstExt = currCert.getPolicyConstraintsExtension()) == null) {
            return explicitPolicy;
        }
        int require = polConstExt.getRequire();
        if (debug != null) {
            debug.println("PolicyChecker.mergeExplicitPolicy() require Index from cert = " + require);
        }
        if (!finalCert) {
            if (require != -1 && (explicitPolicy == -1 || require < explicitPolicy)) {
                explicitPolicy = require;
            }
        } else if (require == 0) {
            explicitPolicy = require;
        }
        return explicitPolicy;
    }

    static int mergePolicyMapping(int policyMapping, X509CertImpl currCert) throws CertPathValidatorException {
        PolicyConstraintsExtension polConstExt;
        if (policyMapping > 0 && !X509CertImpl.isSelfIssued(currCert)) {
            --policyMapping;
        }
        if ((polConstExt = currCert.getPolicyConstraintsExtension()) == null) {
            return policyMapping;
        }
        int inhibit = polConstExt.getInhibit();
        if (debug != null) {
            debug.println("PolicyChecker.mergePolicyMapping() inhibit Index from cert = " + inhibit);
        }
        if (inhibit != -1 && (policyMapping == -1 || inhibit < policyMapping)) {
            policyMapping = inhibit;
        }
        return policyMapping;
    }

    static int mergeInhibitAnyPolicy(int inhibitAnyPolicy, X509CertImpl currCert) throws CertPathValidatorException {
        InhibitAnyPolicyExtension inhAnyPolExt;
        if (inhibitAnyPolicy > 0 && !X509CertImpl.isSelfIssued(currCert)) {
            --inhibitAnyPolicy;
        }
        if ((inhAnyPolExt = (InhibitAnyPolicyExtension)currCert.getExtension(PKIXExtensions.InhibitAnyPolicy_Id)) == null) {
            return inhibitAnyPolicy;
        }
        int skipCerts = inhAnyPolExt.getSkipCerts();
        if (debug != null) {
            debug.println("PolicyChecker.mergeInhibitAnyPolicy() skipCerts Index from cert = " + skipCerts);
        }
        if (skipCerts != -1 && skipCerts < inhibitAnyPolicy) {
            inhibitAnyPolicy = skipCerts;
        }
        return inhibitAnyPolicy;
    }

    static PolicyNodeImpl processPolicies(int certIndex, Set<String> initPolicies, int explicitPolicy, int policyMapping, int inhibitAnyPolicy, boolean rejectPolicyQualifiers, PolicyNodeImpl origRootNode, X509CertImpl currCert, boolean finalCert) throws CertPathValidatorException {
        boolean policiesCritical = false;
        Set<PolicyQualifierInfo> anyQuals = new HashSet<PolicyQualifierInfo>();
        PolicyNodeImpl rootNode = origRootNode == null ? null : origRootNode.copyTree();
        CertificatePoliciesExtension currCertPolicies = currCert.getCertificatePoliciesExtension();
        if (currCertPolicies != null && rootNode != null) {
            policiesCritical = currCertPolicies.isCritical();
            if (debug != null) {
                debug.println("PolicyChecker.processPolicies() policiesCritical = " + policiesCritical);
            }
            List<PolicyInformation> policyInfo = currCertPolicies.getCertPolicies();
            if (debug != null) {
                debug.println("PolicyChecker.processPolicies() rejectPolicyQualifiers = " + rejectPolicyQualifiers);
            }
            boolean foundAnyPolicy = false;
            for (PolicyInformation curPolInfo : policyInfo) {
                Set<PolicyQualifierInfo> pQuals;
                String curPolicy = curPolInfo.getPolicyIdentifier().getIdentifier().toString();
                if (curPolicy.equals(ANY_POLICY)) {
                    foundAnyPolicy = true;
                    anyQuals = curPolInfo.getPolicyQualifiers();
                    continue;
                }
                if (debug != null) {
                    debug.println("PolicyChecker.processPolicies() processing policy: " + curPolicy);
                }
                if (!(pQuals = curPolInfo.getPolicyQualifiers()).isEmpty() && rejectPolicyQualifiers && policiesCritical) {
                    throw new CertPathValidatorException("critical policy qualifiers present in certificate", null, null, -1, PKIXReason.INVALID_POLICY);
                }
                boolean foundMatch = PolicyChecker.processParents(certIndex, policiesCritical, rejectPolicyQualifiers, rootNode, curPolicy, pQuals, false);
                if (foundMatch) continue;
                PolicyChecker.processParents(certIndex, policiesCritical, rejectPolicyQualifiers, rootNode, curPolicy, pQuals, true);
            }
            if (foundAnyPolicy && (inhibitAnyPolicy > 0 || !finalCert && X509CertImpl.isSelfIssued(currCert))) {
                if (debug != null) {
                    debug.println("PolicyChecker.processPolicies() processing policy: " + ANY_POLICY);
                }
                PolicyChecker.processParents(certIndex, policiesCritical, rejectPolicyQualifiers, rootNode, ANY_POLICY, anyQuals, true);
            }
            rootNode.prune(certIndex);
            if (!rootNode.getChildren().hasNext()) {
                rootNode = null;
            }
        } else if (currCertPolicies == null) {
            if (debug != null) {
                debug.println("PolicyChecker.processPolicies() no policies present in cert");
            }
            rootNode = null;
        }
        if (rootNode != null && !finalCert) {
            rootNode = PolicyChecker.processPolicyMappings(currCert, certIndex, policyMapping, rootNode, policiesCritical, anyQuals);
        }
        if (rootNode != null && !initPolicies.contains(ANY_POLICY) && (rootNode = PolicyChecker.removeInvalidNodes(rootNode, certIndex, initPolicies, currCertPolicies)) != null && finalCert) {
            rootNode = PolicyChecker.rewriteLeafNodes(certIndex, initPolicies, rootNode);
        }
        if (finalCert) {
            explicitPolicy = PolicyChecker.mergeExplicitPolicy(explicitPolicy, currCert, true);
        }
        if (explicitPolicy == 0 && rootNode == null) {
            throw new CertPathValidatorException("non-null policy tree required and policy tree is null", null, null, -1, PKIXReason.INVALID_POLICY);
        }
        return rootNode;
    }

    private static PolicyNodeImpl rewriteLeafNodes(int certIndex, Set<String> initPolicies, PolicyNodeImpl rootNode) {
        Set<PolicyNodeImpl> anyNodes = rootNode.getPolicyNodesValid(certIndex, ANY_POLICY);
        if (anyNodes.isEmpty()) {
            return rootNode;
        }
        PolicyNodeImpl anyNode = anyNodes.iterator().next();
        PolicyNodeImpl parentNode = (PolicyNodeImpl)anyNode.getParent();
        parentNode.deleteChild(anyNode);
        HashSet<String> initial = new HashSet<String>(initPolicies);
        for (PolicyNodeImpl node : rootNode.getPolicyNodes(certIndex)) {
            initial.remove(node.getValidPolicy());
        }
        if (initial.isEmpty()) {
            rootNode.prune(certIndex);
            if (!rootNode.getChildren().hasNext()) {
                rootNode = null;
            }
        } else {
            boolean anyCritical = anyNode.isCritical();
            Set<PolicyQualifierInfo> anyQualifiers = anyNode.getPolicyQualifiers();
            for (String policy : initial) {
                Set<String> expectedPolicies = Collections.singleton(policy);
                new PolicyNodeImpl(parentNode, policy, anyQualifiers, anyCritical, expectedPolicies, false);
            }
        }
        return rootNode;
    }

    private static boolean processParents(int certIndex, boolean policiesCritical, boolean rejectPolicyQualifiers, PolicyNodeImpl rootNode, String curPolicy, Set<PolicyQualifierInfo> pQuals, boolean matchAny) {
        boolean foundMatch = false;
        if (debug != null) {
            debug.println("PolicyChecker.processParents(): matchAny = " + matchAny);
        }
        Set<PolicyNodeImpl> parentNodes = rootNode.getPolicyNodesExpected(certIndex - 1, curPolicy, matchAny);
        for (PolicyNodeImpl curParent : parentNodes) {
            if (debug != null) {
                debug.println("PolicyChecker.processParents() found parent:\n" + curParent.asString());
            }
            foundMatch = true;
            if (curPolicy.equals(ANY_POLICY)) {
                Set<String> parExpPols = curParent.getExpectedPolicies();
                block1: for (String curParExpPol : parExpPols) {
                    Iterator<PolicyNodeImpl> childIter = curParent.getChildren();
                    while (childIter.hasNext()) {
                        PolicyNodeImpl childNode = childIter.next();
                        String childPolicy = childNode.getValidPolicy();
                        if (!curParExpPol.equals(childPolicy)) continue;
                        if (debug == null) continue block1;
                        debug.println(childPolicy + " in parent's expected policy set already appears in child node");
                        continue block1;
                    }
                    HashSet<String> expPols = new HashSet<String>();
                    expPols.add(curParExpPol);
                    new PolicyNodeImpl(curParent, curParExpPol, pQuals, policiesCritical, expPols, false);
                }
                continue;
            }
            HashSet<String> curExpPols = new HashSet<String>();
            curExpPols.add(curPolicy);
            new PolicyNodeImpl(curParent, curPolicy, pQuals, policiesCritical, curExpPols, false);
        }
        return foundMatch;
    }

    private static PolicyNodeImpl processPolicyMappings(X509CertImpl currCert, int certIndex, int policyMapping, PolicyNodeImpl rootNode, boolean policiesCritical, Set<PolicyQualifierInfo> anyQuals) throws CertPathValidatorException {
        PolicyMappingsExtension polMappingsExt = currCert.getPolicyMappingsExtension();
        if (polMappingsExt == null) {
            return rootNode;
        }
        if (debug != null) {
            debug.println("PolicyChecker.processPolicyMappings() inside policyMapping check");
        }
        List<CertificatePolicyMap> maps = polMappingsExt.getMaps();
        boolean childDeleted = false;
        for (CertificatePolicyMap polMap : maps) {
            String issuerDomain = polMap.getIssuerIdentifier().getIdentifier().toString();
            String subjectDomain = polMap.getSubjectIdentifier().getIdentifier().toString();
            if (debug != null) {
                debug.println("PolicyChecker.processPolicyMappings() issuerDomain = " + issuerDomain);
                debug.println("PolicyChecker.processPolicyMappings() subjectDomain = " + subjectDomain);
            }
            if (issuerDomain.equals(ANY_POLICY)) {
                throw new CertPathValidatorException("encountered an issuerDomainPolicy of ANY_POLICY", null, null, -1, PKIXReason.INVALID_POLICY);
            }
            if (subjectDomain.equals(ANY_POLICY)) {
                throw new CertPathValidatorException("encountered a subjectDomainPolicy of ANY_POLICY", null, null, -1, PKIXReason.INVALID_POLICY);
            }
            Set<PolicyNodeImpl> validNodes = rootNode.getPolicyNodesValid(certIndex, issuerDomain);
            if (!validNodes.isEmpty()) {
                for (PolicyNodeImpl curNode : validNodes) {
                    if (policyMapping > 0 || policyMapping == -1) {
                        curNode.addExpectedPolicy(subjectDomain);
                        continue;
                    }
                    if (policyMapping != 0) continue;
                    PolicyNodeImpl parentNode = (PolicyNodeImpl)curNode.getParent();
                    if (debug != null) {
                        debug.println("PolicyChecker.processPolicyMappings() before deleting: policy tree = " + rootNode);
                    }
                    parentNode.deleteChild(curNode);
                    childDeleted = true;
                    if (debug == null) continue;
                    debug.println("PolicyChecker.processPolicyMappings() after deleting: policy tree = " + rootNode);
                }
                continue;
            }
            if (policyMapping <= 0 && policyMapping != -1) continue;
            Set<PolicyNodeImpl> validAnyNodes = rootNode.getPolicyNodesValid(certIndex, ANY_POLICY);
            for (PolicyNodeImpl curAnyNode : validAnyNodes) {
                PolicyNodeImpl curAnyNodeParent = (PolicyNodeImpl)curAnyNode.getParent();
                HashSet<String> expPols = new HashSet<String>();
                expPols.add(subjectDomain);
                new PolicyNodeImpl(curAnyNodeParent, issuerDomain, anyQuals, policiesCritical, expPols, true);
            }
        }
        if (childDeleted) {
            rootNode.prune(certIndex);
            if (!rootNode.getChildren().hasNext()) {
                if (debug != null) {
                    debug.println("setting rootNode to null");
                }
                rootNode = null;
            }
        }
        return rootNode;
    }

    private static PolicyNodeImpl removeInvalidNodes(PolicyNodeImpl rootNode, int certIndex, Set<String> initPolicies, CertificatePoliciesExtension currCertPolicies) throws CertPathValidatorException {
        List<PolicyInformation> policyInfo = currCertPolicies.getCertPolicies();
        boolean childDeleted = false;
        for (PolicyInformation curPolInfo : policyInfo) {
            String curPolicy = curPolInfo.getPolicyIdentifier().getIdentifier().toString();
            if (debug != null) {
                debug.println("PolicyChecker.processPolicies() processing policy second time: " + curPolicy);
            }
            Set<PolicyNodeImpl> validNodes = rootNode.getPolicyNodesValid(certIndex, curPolicy);
            for (PolicyNodeImpl curNode : validNodes) {
                PolicyNodeImpl parentNode = (PolicyNodeImpl)curNode.getParent();
                if (!parentNode.getValidPolicy().equals(ANY_POLICY) || initPolicies.contains(curPolicy) || curPolicy.equals(ANY_POLICY)) continue;
                if (debug != null) {
                    debug.println("PolicyChecker.processPolicies() before deleting: policy tree = " + rootNode);
                }
                parentNode.deleteChild(curNode);
                childDeleted = true;
                if (debug == null) continue;
                debug.println("PolicyChecker.processPolicies() after deleting: policy tree = " + rootNode);
            }
        }
        if (childDeleted) {
            rootNode.prune(certIndex);
            if (!rootNode.getChildren().hasNext()) {
                rootNode = null;
            }
        }
        return rootNode;
    }

    PolicyNode getPolicyTree() {
        if (this.rootNode == null) {
            return null;
        }
        PolicyNodeImpl policyTree = this.rootNode.copyTree();
        policyTree.setImmutable();
        return policyTree;
    }
}

