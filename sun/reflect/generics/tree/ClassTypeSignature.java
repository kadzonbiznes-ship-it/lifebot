/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.tree;

import java.util.List;
import sun.reflect.generics.tree.FieldTypeSignature;
import sun.reflect.generics.tree.SimpleClassTypeSignature;
import sun.reflect.generics.visitor.TypeTreeVisitor;

public class ClassTypeSignature
implements FieldTypeSignature {
    private final List<SimpleClassTypeSignature> path;

    private ClassTypeSignature(List<SimpleClassTypeSignature> p) {
        this.path = p;
    }

    public static ClassTypeSignature make(List<SimpleClassTypeSignature> p) {
        return new ClassTypeSignature(p);
    }

    public List<SimpleClassTypeSignature> getPath() {
        return this.path;
    }

    @Override
    public void accept(TypeTreeVisitor<?> v) {
        v.visitClassTypeSignature(this);
    }
}

