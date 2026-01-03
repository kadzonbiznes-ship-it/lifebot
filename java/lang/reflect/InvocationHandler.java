/*
 * Decompiled with CFR 0.152.
 */
package java.lang.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.reflect.Reflection;

public interface InvocationHandler {
    public Object invoke(Object var1, Method var2, Object[] var3) throws Throwable;

    @CallerSensitive
    public static Object invokeDefault(Object proxy, Method method, Object ... args) throws Throwable {
        Objects.requireNonNull(proxy);
        Objects.requireNonNull(method);
        return Proxy.invokeDefault(proxy, method, args, Reflection.getCallerClass());
    }
}

