/*
 * Decompiled with CFR 0.152.
 */
package java.lang;

public class InheritableThreadLocal<T>
extends ThreadLocal<T> {
    @Override
    protected T childValue(T parentValue) {
        return parentValue;
    }

    @Override
    ThreadLocal.ThreadLocalMap getMap(Thread t) {
        return t.inheritableThreadLocals;
    }

    @Override
    void createMap(Thread t, T firstValue) {
        t.inheritableThreadLocals = new ThreadLocal.ThreadLocalMap(this, firstValue);
    }
}

