/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.reflectiveObjects;

import java.lang.reflect.Type;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.tree.FieldTypeSignature;
import sun.reflect.generics.visitor.Reifier;

public abstract class LazyReflectiveObjectGenerator {
    private final GenericsFactory factory;

    protected LazyReflectiveObjectGenerator(GenericsFactory f) {
        this.factory = f;
    }

    private GenericsFactory getFactory() {
        return this.factory;
    }

    protected Reifier getReifier() {
        return Reifier.make(this.getFactory());
    }

    Type[] reifyBounds(FieldTypeSignature[] boundASTs) {
        int length = boundASTs.length;
        Type[] bounds = new Type[length];
        for (int i = 0; i < length; ++i) {
            Reifier r = this.getReifier();
            boundASTs[i].accept(r);
            bounds[i] = r.getResult();
        }
        return bounds;
    }
}

