/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.scope;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import sun.reflect.generics.scope.Scope;

public abstract class AbstractScope<D extends GenericDeclaration>
implements Scope {
    private final D recvr;
    private volatile Scope enclosingScope;

    protected AbstractScope(D decl) {
        this.recvr = decl;
    }

    protected D getRecvr() {
        return this.recvr;
    }

    protected abstract Scope computeEnclosingScope();

    protected Scope getEnclosingScope() {
        Scope value = this.enclosingScope;
        if (value == null) {
            this.enclosingScope = value = this.computeEnclosingScope();
        }
        return value;
    }

    @Override
    public TypeVariable<?> lookup(String name) {
        TypeVariable<?>[] tas;
        for (TypeVariable<?> tv : tas = this.getRecvr().getTypeParameters()) {
            if (!tv.getName().equals(name)) continue;
            return tv;
        }
        return this.getEnclosingScope().lookup(name);
    }
}

