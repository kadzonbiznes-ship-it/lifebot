/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.tree;

import sun.reflect.generics.tree.FieldTypeSignature;
import sun.reflect.generics.tree.TypeTree;
import sun.reflect.generics.visitor.TypeTreeVisitor;

public class FormalTypeParameter
implements TypeTree {
    private final String name;
    private final FieldTypeSignature[] bounds;

    private FormalTypeParameter(String n, FieldTypeSignature[] bs) {
        this.name = n;
        this.bounds = bs;
    }

    public static FormalTypeParameter make(String n, FieldTypeSignature[] bs) {
        return new FormalTypeParameter(n, bs);
    }

    public FieldTypeSignature[] getBounds() {
        return this.bounds;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void accept(TypeTreeVisitor<?> v) {
        v.visitFormalTypeParameter(this);
    }
}

