/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.access;

import java.util.List;

public interface JavaUtilCollectionAccess {
    public <E> List<E> listFromTrustedArray(Object[] var1);

    public <E> List<E> listFromTrustedArrayNullsAllowed(Object[] var1);
}

