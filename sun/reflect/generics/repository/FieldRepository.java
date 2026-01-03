/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.repository;

import java.lang.reflect.Type;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.parser.SignatureParser;
import sun.reflect.generics.repository.AbstractRepository;
import sun.reflect.generics.tree.TypeSignature;
import sun.reflect.generics.visitor.Reifier;

public class FieldRepository
extends AbstractRepository<TypeSignature> {
    private volatile Type genericType;

    protected FieldRepository(String rawSig, GenericsFactory f) {
        super(rawSig, f);
    }

    @Override
    protected TypeSignature parse(String s) {
        return SignatureParser.make().parseTypeSig(s);
    }

    public static FieldRepository make(String rawSig, GenericsFactory f) {
        return new FieldRepository(rawSig, f);
    }

    public Type getGenericType() {
        Type value = this.genericType;
        if (value == null) {
            this.genericType = value = this.computeGenericType();
        }
        return value;
    }

    private Type computeGenericType() {
        Reifier r = this.getReifier();
        ((TypeSignature)this.getTree()).accept(r);
        return r.getResult();
    }
}

