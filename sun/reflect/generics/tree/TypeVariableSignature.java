/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.tree;

import sun.reflect.generics.tree.FieldTypeSignature;
import sun.reflect.generics.visitor.TypeTreeVisitor;

public class TypeVariableSignature
implements FieldTypeSignature {
    private final String identifier;

    private TypeVariableSignature(String id) {
        this.identifier = id;
    }

    public static TypeVariableSignature make(String id) {
        return new TypeVariableSignature(id);
    }

    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public void accept(TypeTreeVisitor<?> v) {
        v.visitTypeVariableSignature(this);
    }
}

