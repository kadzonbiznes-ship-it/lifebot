/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.repository;

import java.lang.reflect.TypeVariable;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.repository.AbstractRepository;
import sun.reflect.generics.tree.FormalTypeParameter;
import sun.reflect.generics.tree.Signature;
import sun.reflect.generics.visitor.Reifier;

public abstract class GenericDeclRepository<S extends Signature>
extends AbstractRepository<S> {
    public static final TypeVariable<?>[] EMPTY_TYPE_VARS = new TypeVariable[0];
    private volatile TypeVariable<?>[] typeParameters;

    protected GenericDeclRepository(String rawSig, GenericsFactory f) {
        super(rawSig, f);
    }

    public TypeVariable<?>[] getTypeParameters() {
        TypeVariable<?>[] value = this.typeParameters;
        if (value == null) {
            this.typeParameters = value = this.computeTypeParameters();
        }
        return (TypeVariable[])value.clone();
    }

    private TypeVariable<?>[] computeTypeParameters() {
        FormalTypeParameter[] ftps = ((Signature)this.getTree()).getFormalTypeParameters();
        int length = ftps.length;
        if (length == 0) {
            return EMPTY_TYPE_VARS;
        }
        TypeVariable[] typeParameters = new TypeVariable[length];
        for (int i = 0; i < length; ++i) {
            Reifier r = this.getReifier();
            ftps[i].accept(r);
            typeParameters[i] = (TypeVariable)r.getResult();
        }
        return typeParameters;
    }
}

