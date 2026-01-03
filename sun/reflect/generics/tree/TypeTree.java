/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.tree;

import sun.reflect.generics.tree.Tree;
import sun.reflect.generics.visitor.TypeTreeVisitor;

public interface TypeTree
extends Tree {
    public void accept(TypeTreeVisitor<?> var1);
}

