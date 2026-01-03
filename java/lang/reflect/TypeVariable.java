/*
 * Decompiled with CFR 0.152.
 */
package java.lang.reflect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;

public interface TypeVariable<D extends GenericDeclaration>
extends Type,
AnnotatedElement {
    public Type[] getBounds();

    public D getGenericDeclaration();

    public String getName();

    public AnnotatedType[] getAnnotatedBounds();
}

