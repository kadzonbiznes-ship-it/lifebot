/*
 * Decompiled with CFR 0.152.
 */
package java.lang.reflect;

import java.lang.reflect.Type;

public interface ParameterizedType
extends Type {
    public Type[] getActualTypeArguments();

    public Type getRawType();

    public Type getOwnerType();
}

