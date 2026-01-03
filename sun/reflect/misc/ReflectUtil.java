/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.misc;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import jdk.internal.reflect.Reflection;
import sun.security.util.SecurityConstants;

public final class ReflectUtil {
    public static final String PROXY_PACKAGE = "com.sun.proxy";

    private ReflectUtil() {
    }

    public static Class<?> forName(String name) throws ClassNotFoundException {
        ReflectUtil.checkPackageAccess(name);
        return Class.forName(name);
    }

    public static void ensureMemberAccess(Class<?> currentClass, Class<?> memberClass, Object target, int modifiers) throws IllegalAccessException {
        Reflection.ensureMemberAccess(currentClass, memberClass, target == null ? null : target.getClass(), modifiers);
    }

    public static void conservativeCheckMemberAccess(Member m) throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return;
        }
        Class<?> declaringClass = m.getDeclaringClass();
        ReflectUtil.privateCheckPackageAccess(sm, declaringClass);
        if (Modifier.isPublic(m.getModifiers()) && Modifier.isPublic(declaringClass.getModifiers())) {
            return;
        }
        sm.checkPermission(SecurityConstants.CHECK_MEMBER_ACCESS_PERMISSION);
    }

    public static void checkPackageAccess(Class<?> clazz) {
        SecurityManager s = System.getSecurityManager();
        if (s != null) {
            ReflectUtil.privateCheckPackageAccess(s, clazz);
        }
    }

    private static void privateCheckPackageAccess(SecurityManager s, Class<?> clazz) {
        String pkg = clazz.getPackageName();
        if (!pkg.isEmpty()) {
            s.checkPackageAccess(pkg);
        }
        if (ReflectUtil.isNonPublicProxyClass(clazz)) {
            ReflectUtil.privateCheckProxyPackageAccess(s, clazz);
        }
    }

    public static void checkPackageAccess(String name) {
        SecurityManager s = System.getSecurityManager();
        if (s != null) {
            int i;
            int b;
            String cname = name.replace('/', '.');
            if (cname.startsWith("[") && (b = cname.lastIndexOf(91) + 2) > 1 && b < cname.length()) {
                cname = cname.substring(b);
            }
            if ((i = cname.lastIndexOf(46)) != -1) {
                s.checkPackageAccess(cname.substring(0, i));
            }
        }
    }

    public static boolean isPackageAccessible(Class<?> clazz) {
        try {
            ReflectUtil.checkPackageAccess(clazz);
        }
        catch (SecurityException e) {
            return false;
        }
        return true;
    }

    private static boolean isAncestor(ClassLoader p, ClassLoader cl) {
        ClassLoader acl = cl;
        do {
            if (p != (acl = acl.getParent())) continue;
            return true;
        } while (acl != null);
        return false;
    }

    public static boolean needsPackageAccessCheck(ClassLoader from, ClassLoader to) {
        if (from == null || from == to) {
            return false;
        }
        if (to == null) {
            return true;
        }
        return !ReflectUtil.isAncestor(from, to);
    }

    public static void checkProxyPackageAccess(Class<?> clazz) {
        SecurityManager s = System.getSecurityManager();
        if (s != null) {
            ReflectUtil.privateCheckProxyPackageAccess(s, clazz);
        }
    }

    private static void privateCheckProxyPackageAccess(SecurityManager s, Class<?> clazz) {
        if (Proxy.isProxyClass(clazz)) {
            for (Class<?> intf : clazz.getInterfaces()) {
                ReflectUtil.privateCheckPackageAccess(s, intf);
            }
        }
    }

    public static void checkProxyPackageAccess(ClassLoader ccl, Class<?> ... interfaces) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            for (Class<?> intf : interfaces) {
                ClassLoader cl = intf.getClassLoader();
                if (!ReflectUtil.needsPackageAccessCheck(ccl, cl)) continue;
                ReflectUtil.privateCheckPackageAccess(sm, intf);
            }
        }
    }

    public static boolean isNonPublicProxyClass(Class<?> cls) {
        if (!Proxy.isProxyClass(cls)) {
            return false;
        }
        return !Modifier.isPublic(cls.getModifiers());
    }

    public static void checkProxyMethod(Object proxy, Method method) {
        String name;
        if (proxy == null || !Proxy.isProxyClass(proxy.getClass())) {
            throw new IllegalArgumentException("Not a Proxy instance");
        }
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Can't handle static method");
        }
        Class<?> c = method.getDeclaringClass();
        if (c == Object.class && ((name = method.getName()).equals("hashCode") || name.equals("equals") || name.equals("toString"))) {
            return;
        }
        if (ReflectUtil.isSuperInterface(proxy.getClass(), c)) {
            return;
        }
        throw new IllegalArgumentException("Can't handle: " + method);
    }

    private static boolean isSuperInterface(Class<?> c, Class<?> intf) {
        for (Class<?> i : c.getInterfaces()) {
            if (i == intf) {
                return true;
            }
            if (!ReflectUtil.isSuperInterface(i, intf)) continue;
            return true;
        }
        return false;
    }
}

