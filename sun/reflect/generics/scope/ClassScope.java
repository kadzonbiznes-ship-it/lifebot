/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.scope;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import sun.reflect.generics.scope.AbstractScope;
import sun.reflect.generics.scope.ConstructorScope;
import sun.reflect.generics.scope.DummyScope;
import sun.reflect.generics.scope.MethodScope;
import sun.reflect.generics.scope.Scope;

public class ClassScope
extends AbstractScope<Class<?>>
implements Scope {
    private ClassScope(Class<?> c) {
        super(c);
    }

    @Override
    protected Scope computeEnclosingScope() {
        Class receiver = (Class)this.getRecvr();
        Method m = receiver.getEnclosingMethod();
        if (m != null) {
            return MethodScope.make(m);
        }
        Constructor<?> cnstr = receiver.getEnclosingConstructor();
        if (cnstr != null) {
            return ConstructorScope.make(cnstr);
        }
        Class<?> c = receiver.getEnclosingClass();
        if (c != null) {
            return ClassScope.make(c);
        }
        return DummyScope.make();
    }

    public static ClassScope make(Class<?> c) {
        return new ClassScope(c);
    }
}

