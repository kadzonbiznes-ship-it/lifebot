/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.security.ProtectionDomain;

@Deprecated(since="17", forRemoval=true)
public interface DomainCombiner {
    public ProtectionDomain[] combine(ProtectionDomain[] var1, ProtectionDomain[] var2);
}

