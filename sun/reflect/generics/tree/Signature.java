/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.tree;

import sun.reflect.generics.tree.FormalTypeParameter;
import sun.reflect.generics.tree.Tree;

public interface Signature
extends Tree {
    public FormalTypeParameter[] getFormalTypeParameters();
}

