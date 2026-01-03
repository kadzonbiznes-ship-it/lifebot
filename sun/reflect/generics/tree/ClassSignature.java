/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.tree;

import sun.reflect.generics.tree.ClassTypeSignature;
import sun.reflect.generics.tree.FormalTypeParameter;
import sun.reflect.generics.tree.Signature;
import sun.reflect.generics.visitor.Visitor;

public class ClassSignature
implements Signature {
    private final FormalTypeParameter[] formalTypeParams;
    private final ClassTypeSignature superclass;
    private final ClassTypeSignature[] superInterfaces;

    private ClassSignature(FormalTypeParameter[] ftps, ClassTypeSignature sc, ClassTypeSignature[] sis) {
        this.formalTypeParams = ftps;
        this.superclass = sc;
        this.superInterfaces = sis;
    }

    public static ClassSignature make(FormalTypeParameter[] ftps, ClassTypeSignature sc, ClassTypeSignature[] sis) {
        return new ClassSignature(ftps, sc, sis);
    }

    @Override
    public FormalTypeParameter[] getFormalTypeParameters() {
        return this.formalTypeParams;
    }

    public ClassTypeSignature getSuperclass() {
        return this.superclass;
    }

    public ClassTypeSignature[] getSuperInterfaces() {
        return this.superInterfaces;
    }

    public void accept(Visitor<?> v) {
        v.visitClassSignature(this);
    }
}

