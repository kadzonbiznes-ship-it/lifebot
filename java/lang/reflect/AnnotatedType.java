/*
 * Decompiled with CFR 0.152.
 */
package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

public interface AnnotatedType
extends AnnotatedElement {
    default public AnnotatedType getAnnotatedOwnerType() {
        return null;
    }

    public Type getType();

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> var1);

    @Override
    public Annotation[] getAnnotations();

    @Override
    public Annotation[] getDeclaredAnnotations();
}

