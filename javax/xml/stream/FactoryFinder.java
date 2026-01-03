/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.stream;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import javax.xml.stream.FactoryConfigurationError;
import jdk.xml.internal.SecuritySupport;

class FactoryFinder {
    private static final String DEFAULT_PACKAGE = "com.sun.xml.internal.";
    private static boolean debug = false;

    FactoryFinder() {
    }

    private static void dPrint(Supplier<String> msgGen) {
        if (debug) {
            System.err.println("JAXP: " + msgGen.get());
        }
    }

    private static Class<?> getProviderClass(String className, ClassLoader cl, boolean doFallback, boolean useBSClsLoader) throws ClassNotFoundException {
        try {
            if (cl == null) {
                if (useBSClsLoader) {
                    return Class.forName(className, false, FactoryFinder.class.getClassLoader());
                }
                cl = SecuritySupport.getContextClassLoader();
                if (cl == null) {
                    throw new ClassNotFoundException();
                }
                return Class.forName(className, false, cl);
            }
            return Class.forName(className, false, cl);
        }
        catch (ClassNotFoundException e1) {
            if (doFallback) {
                return Class.forName(className, false, FactoryFinder.class.getClassLoader());
            }
            throw e1;
        }
    }

    static <T> T newInstance(Class<T> type, String className, ClassLoader cl, boolean doFallback) throws FactoryConfigurationError {
        return FactoryFinder.newInstance(type, className, cl, doFallback, false);
    }

    static <T> T newInstance(Class<T> type, String className, ClassLoader cl, boolean doFallback, boolean useBSClsLoader) throws FactoryConfigurationError {
        assert (type != null);
        if (System.getSecurityManager() != null && className != null && className.startsWith(DEFAULT_PACKAGE)) {
            cl = null;
            useBSClsLoader = true;
        }
        try {
            Class<?> providerClass = FactoryFinder.getProviderClass(className, cl, doFallback, useBSClsLoader);
            if (!type.isAssignableFrom(providerClass)) {
                throw new ClassCastException(className + " cannot be cast to " + type.getName());
            }
            Object instance = providerClass.getConstructor(new Class[0]).newInstance(new Object[0]);
            ClassLoader clD = cl;
            FactoryFinder.dPrint(() -> "created new instance of " + String.valueOf(providerClass) + " using ClassLoader: " + String.valueOf(clD));
            return type.cast(instance);
        }
        catch (ClassNotFoundException x) {
            throw new FactoryConfigurationError("Provider " + className + " not found", x);
        }
        catch (Exception x) {
            throw new FactoryConfigurationError("Provider " + className + " could not be instantiated: " + String.valueOf(x), x);
        }
    }

    static <T> T find(Class<T> type, String fallbackClassName) throws FactoryConfigurationError {
        return FactoryFinder.find(type, type.getName(), null, fallbackClassName);
    }

    static <T> T find(Class<T> type, String factoryId, ClassLoader cl, String fallbackClassName) throws FactoryConfigurationError {
        FactoryFinder.dPrint(() -> "find factoryId =" + factoryId);
        try {
            String systemProp = type.getName().equals(factoryId) ? SecuritySupport.getSystemProperty(factoryId) : System.getProperty(factoryId);
            if (systemProp != null) {
                FactoryFinder.dPrint(() -> "found system property, value=" + systemProp);
                return FactoryFinder.newInstance(type, systemProp, cl, true);
            }
        }
        catch (SecurityException se) {
            throw new FactoryConfigurationError("Failed to read factoryId '" + factoryId + "'", se);
        }
        String factoryClassName = SecuritySupport.readConfig(factoryId, true);
        if (factoryClassName != null) {
            return FactoryFinder.newInstance(type, factoryClassName, cl, true);
        }
        if (type.getName().equals(factoryId)) {
            T provider = FactoryFinder.findServiceProvider(type, cl);
            if (provider != null) {
                return provider;
            }
        } else assert (fallbackClassName == null);
        if (fallbackClassName == null) {
            throw new FactoryConfigurationError("Provider for " + factoryId + " cannot be found", null);
        }
        FactoryFinder.dPrint(() -> "loaded from fallback value: " + fallbackClassName);
        return FactoryFinder.newInstance(type, fallbackClassName, cl, true);
    }

    private static <T> T findServiceProvider(final Class<T> type, final ClassLoader cl) {
        try {
            return AccessController.doPrivileged(new PrivilegedAction<T>(){

                @Override
                public T run() {
                    ServiceLoader serviceLoader = cl == null ? ServiceLoader.load(type) : ServiceLoader.load(type, cl);
                    Iterator iterator = serviceLoader.iterator();
                    if (iterator.hasNext()) {
                        return iterator.next();
                    }
                    return null;
                }
            });
        }
        catch (ServiceConfigurationError e) {
            RuntimeException x = new RuntimeException("Provider for " + String.valueOf(type) + " cannot be created", e);
            FactoryConfigurationError error = new FactoryConfigurationError(x, x.getMessage());
            throw error;
        }
    }

    static {
        try {
            String val = SecuritySupport.getSystemProperty("jaxp.debug");
            debug = val != null && !"false".equals(val);
        }
        catch (SecurityException se) {
            debug = false;
        }
    }
}

