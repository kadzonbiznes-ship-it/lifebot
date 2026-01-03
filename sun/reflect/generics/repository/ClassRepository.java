/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.repository;

import java.lang.reflect.Type;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.parser.SignatureParser;
import sun.reflect.generics.repository.GenericDeclRepository;
import sun.reflect.generics.tree.ClassSignature;
import sun.reflect.generics.tree.ClassTypeSignature;
import sun.reflect.generics.visitor.Reifier;

public class ClassRepository
extends GenericDeclRepository<ClassSignature> {
    public static final ClassRepository NONE = ClassRepository.make("Ljava/lang/Object;", null);
    private volatile Type superclass;
    private volatile Type[] superInterfaces;

    private ClassRepository(String rawSig, GenericsFactory f) {
        super(rawSig, f);
    }

    @Override
    protected ClassSignature parse(String s) {
        return SignatureParser.make().parseClassSig(s);
    }

    public static ClassRepository make(String rawSig, GenericsFactory f) {
        return new ClassRepository(rawSig, f);
    }

    public Type getSuperclass() {
        Type value = this.superclass;
        if (value == null) {
            this.superclass = value = this.computeSuperclass();
        }
        return value;
    }

    public Type[] getSuperInterfaces() {
        Type[] value = this.superInterfaces;
        if (value == null) {
            this.superInterfaces = value = this.computeSuperInterfaces();
        }
        return (Type[])value.clone();
    }

    private Type computeSuperclass() {
        Reifier r = this.getReifier();
        ((ClassSignature)this.getTree()).getSuperclass().accept(r);
        return r.getResult();
    }

    private Type[] computeSuperInterfaces() {
        ClassTypeSignature[] ts = ((ClassSignature)this.getTree()).getSuperInterfaces();
        int length = ts.length;
        Type[] superInterfaces = new Type[length];
        for (int i = 0; i < length; ++i) {
            Reifier r = this.getReifier();
            ts[i].accept(r);
            superInterfaces[i] = r.getResult();
        }
        return superInterfaces;
    }
}

