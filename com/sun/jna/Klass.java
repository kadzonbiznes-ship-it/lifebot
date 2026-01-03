/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna;

import java.lang.reflect.InvocationTargetException;

abstract class Klass {
    private Klass() {
    }

    public static <T> T newInstance(Class<T> klass) {
        try {
            return klass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException e) {
            String msg = "Can't create an instance of " + klass + ", requires a public no-arg constructor: " + e;
            throw new IllegalArgumentException(msg, e);
        }
        catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException)e.getCause();
            }
            String msg = "Can't create an instance of " + klass + ", requires a public no-arg constructor: " + e;
            throw new IllegalArgumentException(msg, e);
        }
    }
}

