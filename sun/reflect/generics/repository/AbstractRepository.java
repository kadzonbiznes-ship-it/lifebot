/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.repository;

import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.tree.Tree;
import sun.reflect.generics.visitor.Reifier;

public abstract class AbstractRepository<T extends Tree> {
    private final GenericsFactory factory;
    private final T tree;

    private GenericsFactory getFactory() {
        return this.factory;
    }

    protected T getTree() {
        return this.tree;
    }

    protected Reifier getReifier() {
        return Reifier.make(this.getFactory());
    }

    protected AbstractRepository(String rawSig, GenericsFactory f) {
        this.tree = this.parse(rawSig);
        this.factory = f;
    }

    protected abstract T parse(String var1);
}

