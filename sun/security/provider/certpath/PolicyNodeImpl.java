/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath;

import java.security.cert.PolicyNode;
import java.security.cert.PolicyQualifierInfo;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import sun.security.util.KnownOIDs;

final class PolicyNodeImpl
implements PolicyNode {
    private static final String ANY_POLICY = KnownOIDs.CE_CERT_POLICIES_ANY.value();
    private final PolicyNodeImpl mParent;
    private final HashSet<PolicyNodeImpl> mChildren;
    private final String mValidPolicy;
    private final HashSet<PolicyQualifierInfo> mQualifierSet;
    private final boolean mCriticalityIndicator;
    private final HashSet<String> mExpectedPolicySet;
    private boolean mOriginalExpectedPolicySet;
    private final int mDepth;
    private boolean isImmutable = false;

    PolicyNodeImpl(PolicyNodeImpl parent, String validPolicy, Set<PolicyQualifierInfo> qualifierSet, boolean criticalityIndicator, Set<String> expectedPolicySet, boolean generatedByPolicyMapping) {
        this.mParent = parent;
        this.mChildren = new HashSet();
        this.mValidPolicy = validPolicy != null ? validPolicy : "";
        this.mQualifierSet = qualifierSet != null ? new HashSet<PolicyQualifierInfo>(qualifierSet) : new HashSet();
        this.mCriticalityIndicator = criticalityIndicator;
        this.mExpectedPolicySet = expectedPolicySet != null ? new HashSet<String>(expectedPolicySet) : new HashSet();
        boolean bl = this.mOriginalExpectedPolicySet = !generatedByPolicyMapping;
        if (this.mParent != null) {
            this.mDepth = this.mParent.getDepth() + 1;
            this.mParent.addChild(this);
        } else {
            this.mDepth = 0;
        }
    }

    PolicyNodeImpl(PolicyNodeImpl parent, PolicyNodeImpl node) {
        this(parent, node.mValidPolicy, node.mQualifierSet, node.mCriticalityIndicator, node.mExpectedPolicySet, false);
    }

    @Override
    public PolicyNode getParent() {
        return this.mParent;
    }

    public Iterator<PolicyNodeImpl> getChildren() {
        return Collections.unmodifiableSet(this.mChildren).iterator();
    }

    @Override
    public int getDepth() {
        return this.mDepth;
    }

    @Override
    public String getValidPolicy() {
        return this.mValidPolicy;
    }

    public Set<PolicyQualifierInfo> getPolicyQualifiers() {
        return Collections.unmodifiableSet(this.mQualifierSet);
    }

    @Override
    public Set<String> getExpectedPolicies() {
        return Collections.unmodifiableSet(this.mExpectedPolicySet);
    }

    @Override
    public boolean isCritical() {
        return this.mCriticalityIndicator;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(this.asString());
        for (PolicyNodeImpl node : this.mChildren) {
            buffer.append(node);
        }
        return buffer.toString();
    }

    boolean isImmutable() {
        return this.isImmutable;
    }

    void setImmutable() {
        if (this.isImmutable) {
            return;
        }
        for (PolicyNodeImpl node : this.mChildren) {
            node.setImmutable();
        }
        this.isImmutable = true;
    }

    private void addChild(PolicyNodeImpl child) {
        if (this.isImmutable) {
            throw new IllegalStateException("PolicyNode is immutable");
        }
        this.mChildren.add(child);
    }

    void addExpectedPolicy(String expectedPolicy) {
        if (this.isImmutable) {
            throw new IllegalStateException("PolicyNode is immutable");
        }
        if (this.mOriginalExpectedPolicySet) {
            this.mExpectedPolicySet.clear();
            this.mOriginalExpectedPolicySet = false;
        }
        this.mExpectedPolicySet.add(expectedPolicy);
    }

    void prune(int depth) {
        if (this.isImmutable) {
            throw new IllegalStateException("PolicyNode is immutable");
        }
        if (this.mChildren.size() == 0) {
            return;
        }
        Iterator<PolicyNodeImpl> it = this.mChildren.iterator();
        while (it.hasNext()) {
            PolicyNodeImpl node = it.next();
            node.prune(depth);
            if (node.mChildren.size() != 0 || depth <= this.mDepth + 1) continue;
            it.remove();
        }
    }

    void deleteChild(PolicyNode childNode) {
        if (this.isImmutable) {
            throw new IllegalStateException("PolicyNode is immutable");
        }
        this.mChildren.remove(childNode);
    }

    PolicyNodeImpl copyTree() {
        return this.copyTree(null);
    }

    private PolicyNodeImpl copyTree(PolicyNodeImpl parent) {
        PolicyNodeImpl newNode = new PolicyNodeImpl(parent, this);
        for (PolicyNodeImpl node : this.mChildren) {
            node.copyTree(newNode);
        }
        return newNode;
    }

    Set<PolicyNodeImpl> getPolicyNodes(int depth) {
        HashSet<PolicyNodeImpl> set = new HashSet<PolicyNodeImpl>();
        this.getPolicyNodes(depth, set);
        return set;
    }

    private void getPolicyNodes(int depth, Set<PolicyNodeImpl> set) {
        if (this.mDepth == depth) {
            set.add(this);
        } else {
            for (PolicyNodeImpl node : this.mChildren) {
                node.getPolicyNodes(depth, set);
            }
        }
    }

    Set<PolicyNodeImpl> getPolicyNodesExpected(int depth, String expectedOID, boolean matchAny) {
        if (expectedOID.equals(ANY_POLICY)) {
            return this.getPolicyNodes(depth);
        }
        return this.getPolicyNodesExpectedHelper(depth, expectedOID, matchAny);
    }

    private Set<PolicyNodeImpl> getPolicyNodesExpectedHelper(int depth, String expectedOID, boolean matchAny) {
        HashSet<PolicyNodeImpl> set = new HashSet<PolicyNodeImpl>();
        if (this.mDepth < depth) {
            for (PolicyNodeImpl node : this.mChildren) {
                set.addAll(node.getPolicyNodesExpectedHelper(depth, expectedOID, matchAny));
            }
        } else if (matchAny) {
            if (this.mExpectedPolicySet.contains(ANY_POLICY)) {
                set.add(this);
            }
        } else if (this.mExpectedPolicySet.contains(expectedOID)) {
            set.add(this);
        }
        return set;
    }

    Set<PolicyNodeImpl> getPolicyNodesValid(int depth, String validOID) {
        HashSet<PolicyNodeImpl> set = new HashSet<PolicyNodeImpl>();
        if (this.mDepth < depth) {
            for (PolicyNodeImpl node : this.mChildren) {
                set.addAll(node.getPolicyNodesValid(depth, validOID));
            }
        } else if (this.mValidPolicy.equals(validOID)) {
            set.add(this);
        }
        return set;
    }

    private static String policyToString(String oid) {
        if (oid.equals(ANY_POLICY)) {
            return "anyPolicy";
        }
        return oid;
    }

    String asString() {
        if (this.mParent == null) {
            return "anyPolicy  ROOT\n";
        }
        StringBuilder sb = new StringBuilder();
        int n = this.getDepth();
        for (int i = 0; i < n; ++i) {
            sb.append("  ");
        }
        sb.append(PolicyNodeImpl.policyToString(this.getValidPolicy()));
        sb.append("  CRIT: ");
        sb.append(this.isCritical());
        sb.append("  EP: ");
        for (String policy : this.getExpectedPolicies()) {
            sb.append(PolicyNodeImpl.policyToString(policy));
            sb.append(" ");
        }
        sb.append(" (");
        sb.append(this.getDepth());
        sb.append(")\n");
        return sb.toString();
    }
}

