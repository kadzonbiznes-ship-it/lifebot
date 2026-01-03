/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.misc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;

class Trampoline {
    Trampoline() {
    }

    private static void ensureInvocableMethod(Method m) throws InvocationTargetException {
        Class<?> clazz = m.getDeclaringClass();
        if (clazz.equals(AccessController.class) || clazz.equals(Method.class) || clazz.getName().startsWith("java.lang.invoke.")) {
            throw new InvocationTargetException(new UnsupportedOperationException("invocation not supported"));
        }
    }

    private static Object invoke(Method m, Object obj, Object[] params) throws InvocationTargetException, IllegalAccessException {
        Trampoline.ensureInvocableMethod(m);
        return m.invoke(obj, params);
    }

    static {
        if (Trampoline.class.getClassLoader() == null) {
            throw new Error("Trampoline must not be defined by the bootstrap classloader");
        }
    }
}

