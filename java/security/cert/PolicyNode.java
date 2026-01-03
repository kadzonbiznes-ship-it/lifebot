/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.security.cert.PolicyQualifierInfo;
import java.util.Iterator;
import java.util.Set;

public interface PolicyNode {
    public PolicyNode getParent();

    public Iterator<? extends PolicyNode> getChildren();

    public int getDepth();

    public String getValidPolicy();

    public Set<? extends PolicyQualifierInfo> getPolicyQualifiers();

    public Set<String> getExpectedPolicies();

    public boolean isCritical();
}

