/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.misc;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;
import sun.reflect.misc.ReflectUtil;

public final class MethodUtil
extends SecureClassLoader {
    private static final String MISC_PKG = "sun.reflect.misc.";
    private static final String TRAMPOLINE = "sun.reflect.misc.Trampoline";
    private static final Method bounce = MethodUtil.getTrampoline();

    private MethodUtil() {
    }

    public static Method getMethod(Class<?> cls, String name, Class<?>[] args) throws NoSuchMethodException {
        ReflectUtil.checkPackageAccess(cls);
        return cls.getMethod(name, args);
    }

    public static Method[] getMethods(Class<?> cls) {
        ReflectUtil.checkPackageAccess(cls);
        return cls.getMethods();
    }

    public static Object invoke(Method m, Object obj, Object[] params) throws InvocationTargetException, IllegalAccessException {
        try {
            return bounce.invoke(null, m, obj, params);
        }
        catch (InvocationTargetException ie) {
            Throwable t = ie.getCause();
            if (t instanceof InvocationTargetException) {
                throw (InvocationTargetException)t;
            }
            if (t instanceof IllegalAccessException) {
                throw (IllegalAccessException)t;
            }
            if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            }
            if (t instanceof Error) {
                throw (Error)t;
            }
            throw new Error("Unexpected invocation error", t);
        }
        catch (IllegalAccessException iae) {
            throw new Error("Unexpected invocation error", iae);
        }
    }

    private static Method getTrampoline() {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Method>(){

                @Override
                public Method run() throws Exception {
                    Class<?> t = MethodUtil.getTrampolineClass();
                    Class[] types = new Class[]{Method.class, Object.class, Object[].class};
                    Method b = t.getDeclaredMethod("invoke", types);
                    b.setAccessible(true);
                    return b;
                }
            });
        }
        catch (Exception e) {
            throw new InternalError("bouncer cannot be found", e);
        }
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        ReflectUtil.checkPackageAccess(name);
        Class<?> c = this.findLoadedClass(name);
        if (c == null) {
            try {
                c = this.findClass(name);
            }
            catch (ClassNotFoundException classNotFoundException) {
                // empty catch block
            }
            if (c == null) {
                c = this.getParent().loadClass(name);
            }
        }
        if (resolve) {
            this.resolveClass(c);
        }
        return c;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        block9: {
            Class<?> clazz;
            block10: {
                if (!name.startsWith(MISC_PKG)) {
                    throw new ClassNotFoundException(name);
                }
                String path = name.replace('.', '/').concat(".class");
                InputStream in = Object.class.getModule().getResourceAsStream(path);
                if (in == null) break block9;
                InputStream inputStream = in;
                try {
                    byte[] b = in.readAllBytes();
                    clazz = this.defineClass(name, b);
                    if (inputStream == null) break block10;
                }
                catch (Throwable throwable) {
                    try {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
                inputStream.close();
            }
            return clazz;
        }
        throw new ClassNotFoundException(name);
    }

    private Class<?> defineClass(String name, byte[] b) throws IOException {
        CodeSource cs = new CodeSource(null, (Certificate[])null);
        if (!name.equals(TRAMPOLINE)) {
            throw new IOException("MethodUtil: bad name " + name);
        }
        return this.defineClass(name, b, 0, b.length, cs);
    }

    @Override
    protected PermissionCollection getPermissions(CodeSource codesource) {
        PermissionCollection perms = super.getPermissions(codesource);
        perms.add(new AllPermission());
        return perms;
    }

    private static Class<?> getTrampolineClass() {
        try {
            return Class.forName(TRAMPOLINE, true, new MethodUtil());
        }
        catch (ClassNotFoundException classNotFoundException) {
            return null;
        }
    }
}

