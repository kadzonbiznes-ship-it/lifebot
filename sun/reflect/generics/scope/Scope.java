/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.scope;

import java.lang.reflect.TypeVariable;

public interface Scope {
    public TypeVariable<?> lookup(String var1);
}

