/*
 * Decompiled with CFR 0.152.
 */
package java.lang.reflect;

import java.lang.reflect.Type;

public interface WildcardType
extends Type {
    public Type[] getUpperBounds();

    public Type[] getLowerBounds();
}

