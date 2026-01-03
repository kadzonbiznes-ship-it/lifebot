/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReflectionUtils {
    private static final Logger LOG = Logger.getLogger(ReflectionUtils.class.getName());
    private static final Method METHOD_IS_DEFAULT;
    private static final Method METHOD_HANDLES_LOOKUP;
    private static final Method METHOD_HANDLES_LOOKUP_IN;
    private static final Method METHOD_HANDLES_PRIVATE_LOOKUP_IN;
    private static final Method METHOD_HANDLES_LOOKUP_UNREFLECT_SPECIAL;
    private static final Method METHOD_HANDLES_LOOKUP_FIND_SPECIAL;
    private static final Method METHOD_HANDLES_BIND_TO;
    private static final Method METHOD_HANDLES_INVOKE_WITH_ARGUMENTS;
    private static final Method METHOD_TYPE;
    private static Constructor CONSTRUCTOR_LOOKUP_CLASS;

    private static Constructor getConstructorLookupClass() {
        if (CONSTRUCTOR_LOOKUP_CLASS == null) {
            Class lookup = ReflectionUtils.lookupClass("java.lang.invoke.MethodHandles$Lookup");
            CONSTRUCTOR_LOOKUP_CLASS = ReflectionUtils.lookupDeclaredConstructor(lookup, Class.class);
        }
        return CONSTRUCTOR_LOOKUP_CLASS;
    }

    private static Constructor lookupDeclaredConstructor(Class clazz, Class ... arguments) {
        if (clazz == null) {
            LOG.log(Level.FINE, "Failed to lookup method: <init>#{1}({2})", new Object[]{clazz, Arrays.toString(arguments)});
            return null;
        }
        try {
            Constructor init = clazz.getDeclaredConstructor(arguments);
            init.setAccessible(true);
            return init;
        }
        catch (Exception ex) {
            LOG.log(Level.FINE, "Failed to lookup method: <init>#{1}({2})", new Object[]{clazz, Arrays.toString(arguments)});
            return null;
        }
    }

    private static Method lookupMethod(Class clazz, String methodName, Class ... arguments) {
        if (clazz == null) {
            LOG.log(Level.FINE, "Failed to lookup method: {0}#{1}({2})", new Object[]{clazz, methodName, Arrays.toString(arguments)});
            return null;
        }
        try {
            return clazz.getMethod(methodName, arguments);
        }
        catch (Exception ex) {
            LOG.log(Level.FINE, "Failed to lookup method: {0}#{1}({2})", new Object[]{clazz, methodName, Arrays.toString(arguments)});
            return null;
        }
    }

    private static Class lookupClass(String name) {
        try {
            return Class.forName(name);
        }
        catch (ClassNotFoundException ex) {
            LOG.log(Level.FINE, "Failed to lookup class: " + name, ex);
            return null;
        }
    }

    public static boolean isDefault(Method method) {
        if (METHOD_IS_DEFAULT == null) {
            return false;
        }
        try {
            return (Boolean)METHOD_IS_DEFAULT.invoke((Object)method, new Object[0]);
        }
        catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        }
        catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            }
            if (cause instanceof Error) {
                throw (Error)cause;
            }
            throw new RuntimeException(cause);
        }
    }

    public static Object getMethodHandle(Method method) throws Exception {
        assert (ReflectionUtils.isDefault(method));
        Object baseLookup = ReflectionUtils.createLookup();
        try {
            Object lookup = ReflectionUtils.createPrivateLookupIn(method.getDeclaringClass(), baseLookup);
            Object mh = ReflectionUtils.mhViaFindSpecial(lookup, method);
            return mh;
        }
        catch (Exception ex) {
            Object lookup = ReflectionUtils.getConstructorLookupClass().newInstance(method.getDeclaringClass());
            Object mh = ReflectionUtils.mhViaUnreflectSpecial(lookup, method);
            return mh;
        }
    }

    private static Object mhViaFindSpecial(Object lookup, Method method) throws Exception {
        return METHOD_HANDLES_LOOKUP_FIND_SPECIAL.invoke(lookup, method.getDeclaringClass(), method.getName(), METHOD_TYPE.invoke(null, method.getReturnType(), method.getParameterTypes()), method.getDeclaringClass());
    }

    private static Object mhViaUnreflectSpecial(Object lookup, Method method) throws Exception {
        Object l2 = METHOD_HANDLES_LOOKUP_IN.invoke(lookup, method.getDeclaringClass());
        return METHOD_HANDLES_LOOKUP_UNREFLECT_SPECIAL.invoke(l2, method, method.getDeclaringClass());
    }

    private static Object createPrivateLookupIn(Class type, Object lookup) throws Exception {
        return METHOD_HANDLES_PRIVATE_LOOKUP_IN.invoke(null, type, lookup);
    }

    private static Object createLookup() throws Exception {
        return METHOD_HANDLES_LOOKUP.invoke(null, new Object[0]);
    }

    public static Object invokeDefaultMethod(Object target, Object methodHandle, Object ... args) throws Throwable {
        Object boundMethodHandle = METHOD_HANDLES_BIND_TO.invoke(methodHandle, target);
        return METHOD_HANDLES_INVOKE_WITH_ARGUMENTS.invoke(boundMethodHandle, new Object[]{args});
    }

    static {
        Class methodHandles = ReflectionUtils.lookupClass("java.lang.invoke.MethodHandles");
        Class methodHandle = ReflectionUtils.lookupClass("java.lang.invoke.MethodHandle");
        Class lookup = ReflectionUtils.lookupClass("java.lang.invoke.MethodHandles$Lookup");
        Class methodType = ReflectionUtils.lookupClass("java.lang.invoke.MethodType");
        METHOD_IS_DEFAULT = ReflectionUtils.lookupMethod(Method.class, "isDefault", new Class[0]);
        METHOD_HANDLES_LOOKUP = ReflectionUtils.lookupMethod(methodHandles, "lookup", new Class[0]);
        METHOD_HANDLES_LOOKUP_IN = ReflectionUtils.lookupMethod(lookup, "in", Class.class);
        METHOD_HANDLES_LOOKUP_UNREFLECT_SPECIAL = ReflectionUtils.lookupMethod(lookup, "unreflectSpecial", Method.class, Class.class);
        METHOD_HANDLES_LOOKUP_FIND_SPECIAL = ReflectionUtils.lookupMethod(lookup, "findSpecial", Class.class, String.class, methodType, Class.class);
        METHOD_HANDLES_BIND_TO = ReflectionUtils.lookupMethod(methodHandle, "bindTo", Object.class);
        METHOD_HANDLES_INVOKE_WITH_ARGUMENTS = ReflectionUtils.lookupMethod(methodHandle, "invokeWithArguments", Object[].class);
        METHOD_HANDLES_PRIVATE_LOOKUP_IN = ReflectionUtils.lookupMethod(methodHandles, "privateLookupIn", Class.class, lookup);
        METHOD_TYPE = ReflectionUtils.lookupMethod(methodType, "methodType", Class.class, Class[].class);
    }
}

