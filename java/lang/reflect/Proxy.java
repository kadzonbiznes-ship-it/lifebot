/*
 * Decompiled with CFR 0.152.
 */
package java.lang.reflect;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.module.ModuleDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ProxyGenerator;
import java.lang.reflect.ReflectPermission;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import jdk.internal.access.JavaLangAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.loader.ClassLoaderValue;
import jdk.internal.misc.VM;
import jdk.internal.module.Modules;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.reflect.Reflection;
import jdk.internal.vm.annotation.Stable;
import sun.reflect.misc.ReflectUtil;
import sun.security.action.GetPropertyAction;
import sun.security.util.SecurityConstants;

public class Proxy
implements Serializable {
    private static final long serialVersionUID = -2222568056686623797L;
    private static final Class<?>[] constructorParams = new Class[]{InvocationHandler.class};
    private static final ClassLoaderValue<Constructor<?>> proxyCache = new ClassLoaderValue();
    protected InvocationHandler h;
    private static final String PROXY_PACKAGE_PREFIX = "com.sun.proxy";
    private static final ClassValue<ConcurrentHashMap<Method, MethodHandle>> DEFAULT_METHODS_MAP = new ClassValue<ConcurrentHashMap<Method, MethodHandle>>(){

        @Override
        protected ConcurrentHashMap<Method, MethodHandle> computeValue(Class<?> type) {
            return new ConcurrentHashMap<Method, MethodHandle>(4);
        }
    };
    static final Object[] EMPTY_ARGS = new Object[0];

    private Proxy() {
    }

    protected Proxy(InvocationHandler h) {
        Objects.requireNonNull(h);
        this.h = h;
    }

    @Deprecated
    @CallerSensitive
    public static Class<?> getProxyClass(ClassLoader loader, Class<?> ... interfaces) throws IllegalArgumentException {
        Class<?> caller = System.getSecurityManager() == null ? null : Reflection.getCallerClass();
        return Proxy.getProxyConstructor(caller, loader, interfaces).getDeclaringClass();
    }

    private static Constructor<?> getProxyConstructor(Class<?> caller, ClassLoader loader, Class<?> ... interfaces) {
        if (interfaces.length == 1) {
            Class<?> intf = interfaces[0];
            if (caller != null) {
                Proxy.checkProxyAccess(caller, loader, intf);
            }
            return proxyCache.sub(intf).computeIfAbsent(loader, (ld, clv) -> new ProxyBuilder((ClassLoader)ld, (Class)clv.key()).build());
        }
        Class[] intfsArray = (Class[])interfaces.clone();
        if (caller != null) {
            Proxy.checkProxyAccess(caller, loader, intfsArray);
        }
        List<Class> intfs = Arrays.asList(intfsArray);
        return proxyCache.sub(intfs).computeIfAbsent(loader, (ld, clv) -> new ProxyBuilder((ClassLoader)ld, (List)clv.key()).build());
    }

    private static void checkProxyAccess(Class<?> caller, ClassLoader loader, Class<?> ... interfaces) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader ccl = caller.getClassLoader();
            if (loader == null && ccl != null) {
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
            }
            ReflectUtil.checkProxyPackageAccess(ccl, interfaces);
        }
    }

    @CallerSensitive
    public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h) {
        Objects.requireNonNull(h);
        Class<?> caller = System.getSecurityManager() == null ? null : Reflection.getCallerClass();
        Constructor<?> cons = Proxy.getProxyConstructor(caller, loader, interfaces);
        return Proxy.newProxyInstance(caller, cons, h);
    }

    private static Object newProxyInstance(Class<?> caller, Constructor<?> cons, InvocationHandler h) {
        try {
            if (caller != null) {
                Proxy.checkNewProxyPermission(caller, cons.getDeclaringClass());
            }
            return cons.newInstance(h);
        }
        catch (IllegalAccessException | InstantiationException e) {
            throw new InternalError(e.toString(), e);
        }
        catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            }
            throw new InternalError(t.toString(), t);
        }
    }

    private static void checkNewProxyPermission(Class<?> caller, Class<?> proxyClass) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null && ReflectUtil.isNonPublicProxyClass(proxyClass)) {
            ClassLoader ccl = caller.getClassLoader();
            ClassLoader pcl = proxyClass.getClassLoader();
            String pkg = proxyClass.getPackageName();
            String callerPkg = caller.getPackageName();
            if (pcl != ccl || !pkg.equals(callerPkg)) {
                sm.checkPermission(new ReflectPermission("newProxyInPackage." + pkg));
            }
        }
    }

    private static ClassLoader getLoader(Module m) {
        PrivilegedAction<ClassLoader> pa = m::getClassLoader;
        return AccessController.doPrivileged(pa);
    }

    public static boolean isProxyClass(Class<?> cl) {
        return Proxy.class.isAssignableFrom(cl) && ProxyBuilder.isProxyClass(cl);
    }

    @CallerSensitive
    public static InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException {
        if (!Proxy.isProxyClass(proxy.getClass())) {
            throw new IllegalArgumentException("not a proxy instance");
        }
        Proxy p = (Proxy)proxy;
        InvocationHandler ih = p.h;
        if (System.getSecurityManager() != null) {
            Class<?> ihClass = ih.getClass();
            Class<?> caller = Reflection.getCallerClass();
            if (ReflectUtil.needsPackageAccessCheck(caller.getClassLoader(), ihClass.getClassLoader())) {
                ReflectUtil.checkPackageAccess(ihClass);
            }
        }
        return ih;
    }

    private static ConcurrentHashMap<Method, MethodHandle> defaultMethodMap(Class<?> proxyClass) {
        assert (Proxy.isProxyClass(proxyClass));
        return DEFAULT_METHODS_MAP.get(proxyClass);
    }

    static MethodHandle defaultMethodHandle(Class<? extends Proxy> proxyClass, Method method) {
        ConcurrentHashMap<Method, MethodHandle> methods = Proxy.defaultMethodMap(proxyClass);
        MethodHandle superMH = methods.get(method);
        if (superMH == null) {
            MethodHandle dmh;
            MethodType type = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> proxyInterface = Proxy.findProxyInterfaceOrElseThrow(proxyClass, method);
            try {
                dmh = Proxy.proxyClassLookup(lookup, proxyClass).findSpecial(proxyInterface, method.getName(), type, proxyClass).withVarargs(false);
            }
            catch (IllegalAccessException | NoSuchMethodException e) {
                throw new InternalError(e);
            }
            assert (((BooleanSupplier)() -> {
                try {
                    dmh.asType(type.insertParameterTypes(0, proxyClass));
                    return true;
                }
                catch (WrongMethodTypeException e) {
                    return false;
                }
            }).getAsBoolean()) : "Wrong method type";
            MethodHandle mh = dmh.asType(dmh.type().changeReturnType(Object.class));
            mh = MethodHandles.catchException(mh, Throwable.class, InvocationException.wrapMH());
            mh = mh.asSpreader(1, Object[].class, type.parameterCount());
            MethodHandle cached = methods.putIfAbsent(method, mh = mh.asType(MethodType.methodType(Object.class, Object.class, Object[].class)));
            superMH = cached != null ? cached : mh;
        }
        return superMH;
    }

    private static Class<?> findProxyInterfaceOrElseThrow(Class<?> proxyClass, Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        if (!declaringClass.isInterface()) {
            throw new IllegalArgumentException("\"" + method + "\" is not a method declared in the proxy class");
        }
        List<Class<?>> proxyInterfaces = Arrays.asList(proxyClass.getInterfaces());
        if (proxyInterfaces.contains(declaringClass)) {
            return declaringClass;
        }
        ArrayDeque deque = new ArrayDeque();
        HashSet<Class> visited = new HashSet<Class>();
        boolean indirectMethodRef = false;
        for (Class<?> proxyIntf : proxyInterfaces) {
            Class c;
            assert (proxyIntf != declaringClass);
            visited.add(proxyIntf);
            deque.add(proxyIntf);
            while ((c = (Class)deque.poll()) != null) {
                if (c == declaringClass) {
                    try {
                        Method m = proxyIntf.getMethod(method.getName(), method.getSharedParameterTypes());
                        if (m.getDeclaringClass() == declaringClass) {
                            return proxyIntf;
                        }
                        indirectMethodRef = true;
                    }
                    catch (NoSuchMethodException noSuchMethodException) {}
                    continue;
                }
                visited.add(c);
                for (Class<?> superIntf : c.getInterfaces()) {
                    if (visited.contains(superIntf) || deque.contains(superIntf)) continue;
                    if (superIntf == declaringClass) {
                        deque.addFirst(superIntf);
                        continue;
                    }
                    deque.add(superIntf);
                }
            }
        }
        throw new IllegalArgumentException("\"" + method + (indirectMethodRef ? "\" is overridden directly or indirectly by the proxy interfaces" : "\" is not a method declared in the proxy class"));
    }

    private static MethodHandles.Lookup proxyClassLookup(final MethodHandles.Lookup caller, final Class<?> proxyClass) {
        return AccessController.doPrivileged(new PrivilegedAction<MethodHandles.Lookup>(){

            @Override
            public MethodHandles.Lookup run() {
                try {
                    Method m = proxyClass.getDeclaredMethod("proxyClassLookup", MethodHandles.Lookup.class);
                    m.setAccessible(true);
                    return (MethodHandles.Lookup)m.invoke(null, caller);
                }
                catch (ReflectiveOperationException e) {
                    throw new InternalError(e);
                }
            }
        });
    }

    static Object invokeDefault(Object proxy, Method method, Object[] args, Class<?> caller) throws Throwable {
        if (!Proxy.isProxyClass(proxy.getClass())) {
            throw new IllegalArgumentException("'proxy' is not a proxy instance");
        }
        if (!method.isDefault()) {
            throw new IllegalArgumentException("\"" + method + "\" is not a default method");
        }
        Class<?> proxyClass = proxy.getClass();
        if (caller != null) {
            Class<?> intf = method.getDeclaringClass();
            method.checkAccess(caller, intf, proxyClass, method.getModifiers());
        }
        MethodHandle mh = Proxy.defaultMethodHandle(proxyClass, method);
        try {
            Object[] params = args != null ? args : EMPTY_ARGS;
            return mh.invokeExact(proxy, params);
        }
        catch (ClassCastException | NullPointerException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        catch (InvocationException e) {
            throw e.getCause();
        }
    }

    private static final class ProxyBuilder {
        private static final JavaLangAccess JLA = SharedSecrets.getJavaLangAccess();
        private static final String proxyClassNamePrefix = "$Proxy";
        private static final AtomicLong nextUniqueNumber = new AtomicLong();
        private static final ClassLoaderValue<Boolean> reverseProxyCache = new ClassLoaderValue();
        private static final String DEBUG = GetPropertyAction.privilegedGetProperty("jdk.proxy.debug", "");
        private final List<Class<?>> interfaces;
        private final ProxyClassContext context;
        private static final ClassLoaderValue<Module> dynProxyModules = new ClassLoaderValue();
        private static final AtomicInteger counter = new AtomicInteger();

        private static Class<?> defineProxyClass(ProxyClassContext context, List<Class<?>> interfaces) {
            long num = nextUniqueNumber.getAndIncrement();
            String proxyName = context.packageName().isEmpty() ? proxyClassNamePrefix + num : context.packageName() + "." + proxyClassNamePrefix + num;
            ClassLoader loader = Proxy.getLoader(context.module());
            ProxyBuilder.trace(proxyName, context.module(), loader, interfaces);
            byte[] proxyClassFile = ProxyGenerator.generateProxyClass(loader, proxyName, interfaces, context.accessFlags() | 0x10);
            try {
                Class<?> pc = JLA.defineClass(loader, proxyName, proxyClassFile, null, "__dynamic_proxy__");
                reverseProxyCache.sub(pc).putIfAbsent(loader, Boolean.TRUE);
                return pc;
            }
            catch (ClassFormatError e) {
                throw new IllegalArgumentException(e.toString());
            }
        }

        static boolean isProxyClass(Class<?> c) {
            return Objects.equals(reverseProxyCache.sub(c).get(c.getClassLoader()), Boolean.TRUE);
        }

        private static boolean isExportedType(Class<?> c) {
            String pn = c.getPackageName();
            return Modifier.isPublic(c.getModifiers()) && c.getModule().isExported(pn);
        }

        private static boolean isPackagePrivateType(Class<?> c) {
            return !Modifier.isPublic(c.getModifiers());
        }

        private static String toDetails(Class<?> c) {
            String access = "unknown";
            access = ProxyBuilder.isExportedType(c) ? "exported" : (ProxyBuilder.isPackagePrivateType(c) ? "package-private" : "module-private");
            ClassLoader ld = c.getClassLoader();
            return String.format("   %s/%s %s loader %s", c.getModule().getName(), c.getName(), access, ld);
        }

        static void trace(String cn, Module module, ClassLoader loader, List<Class<?>> interfaces) {
            if (ProxyBuilder.isDebug()) {
                System.err.format("PROXY: %s/%s defined by %s%n", module.getName(), cn, loader);
            }
            if (ProxyBuilder.isDebug("debug")) {
                interfaces.forEach(c -> System.out.println(ProxyBuilder.toDetails(c)));
            }
        }

        private static boolean isDebug() {
            return !DEBUG.isEmpty();
        }

        private static boolean isDebug(String flag) {
            return DEBUG.equals(flag);
        }

        ProxyBuilder(ClassLoader loader, List<Class<?>> interfaces) {
            if (!VM.isModuleSystemInited()) {
                throw new InternalError("Proxy is not supported until module system is fully initialized");
            }
            if (interfaces.size() > 65535) {
                throw new IllegalArgumentException("interface limit exceeded: " + interfaces.size());
            }
            Set<Class<?>> refTypes = ProxyBuilder.referencedTypes(loader, interfaces);
            ProxyBuilder.validateProxyInterfaces(loader, interfaces, refTypes);
            this.interfaces = interfaces;
            this.context = ProxyBuilder.proxyClassContext(loader, interfaces, refTypes);
            assert (Proxy.getLoader(this.context.module()) == loader);
        }

        ProxyBuilder(ClassLoader loader, Class<?> intf) {
            this(loader, Collections.singletonList(intf));
        }

        Constructor<?> build() {
            Constructor<?> cons;
            Class<?> proxyClass = ProxyBuilder.defineProxyClass(this.context, this.interfaces);
            try {
                cons = proxyClass.getConstructor(constructorParams);
            }
            catch (NoSuchMethodException e) {
                throw new InternalError(e.toString(), e);
            }
            AccessController.doPrivileged(new PrivilegedAction<Void>(){

                @Override
                public Void run() {
                    cons.setAccessible(true);
                    return null;
                }
            });
            return cons;
        }

        private static void validateProxyInterfaces(ClassLoader loader, List<Class<?>> interfaces, Set<Class<?>> refTypes) {
            IdentityHashMap interfaceSet = new IdentityHashMap(interfaces.size());
            for (Class<?> intf : interfaces) {
                if (!intf.isInterface()) {
                    throw new IllegalArgumentException(intf.getName() + " is not an interface");
                }
                if (intf.isHidden()) {
                    throw new IllegalArgumentException(intf.getName() + " is a hidden interface");
                }
                if (intf.isSealed()) {
                    throw new IllegalArgumentException(intf.getName() + " is a sealed interface");
                }
                ProxyBuilder.ensureVisible(loader, intf);
                if (interfaceSet.put(intf, Boolean.TRUE) == null) continue;
                throw new IllegalArgumentException("repeated interface: " + intf.getName());
            }
            for (Class<?> type : refTypes) {
                ProxyBuilder.ensureVisible(loader, type);
            }
        }

        private static Set<Class<?>> referencedTypes(ClassLoader loader, List<Class<?>> interfaces) {
            HashSet types = new HashSet();
            for (Class<?> intf : interfaces) {
                for (Method m : intf.getMethods()) {
                    if (Modifier.isStatic(m.getModifiers())) continue;
                    ProxyBuilder.addElementType(types, m.getReturnType());
                    ProxyBuilder.addElementTypes(types, m.getSharedParameterTypes());
                    ProxyBuilder.addElementTypes(types, m.getSharedExceptionTypes());
                }
            }
            return types;
        }

        private static void addElementTypes(HashSet<Class<?>> types, Class<?> ... classes) {
            for (Class<?> cls : classes) {
                ProxyBuilder.addElementType(types, cls);
            }
        }

        private static void addElementType(HashSet<Class<?>> types, Class<?> cls) {
            Class<?> type = ProxyBuilder.getElementType(cls);
            if (!type.isPrimitive()) {
                types.add(type);
            }
        }

        private static ProxyClassContext proxyClassContext(ClassLoader loader, List<Class<?>> interfaces, Set<Class<?>> refTypes) {
            Module targetModule;
            HashMap packagePrivateTypes = new HashMap();
            boolean nonExported = false;
            for (Class<?> intf : interfaces) {
                Iterator<Class<Object>> m = intf.getModule();
                if (!Modifier.isPublic(intf.getModifiers())) {
                    packagePrivateTypes.put(intf, m);
                    continue;
                }
                if (intf.getModule().isExported(intf.getPackageName())) continue;
                nonExported = true;
            }
            if (packagePrivateTypes.size() > 0) {
                targetModule = null;
                String targetPackageName = null;
                for (Map.Entry entry : packagePrivateTypes.entrySet()) {
                    Class intf = (Class)entry.getKey();
                    Module m = (Module)entry.getValue();
                    if (targetModule != null && targetModule != m || targetPackageName != null && targetPackageName != intf.getPackageName()) {
                        throw new IllegalArgumentException("cannot have non-public interfaces in different packages");
                    }
                    if (Proxy.getLoader(m) != loader) {
                        throw new IllegalArgumentException("non-public interface is not defined by the given loader");
                    }
                    targetModule = m;
                    targetPackageName = ((Class)entry.getKey()).getPackageName();
                }
                for (Class<Object> clazz : interfaces) {
                    Module m = clazz.getModule();
                    if (m == targetModule || targetModule.canRead(m) && m.isExported(clazz.getPackageName(), targetModule)) continue;
                    throw new IllegalArgumentException(targetModule + " can't access " + clazz.getName());
                }
                if (targetModule.isNamed()) {
                    Modules.addOpens(targetModule, targetPackageName, Proxy.class.getModule());
                }
                return new ProxyClassContext(targetModule, targetPackageName, 0);
            }
            targetModule = ProxyBuilder.getDynamicModule(loader);
            HashSet types = new HashSet(interfaces);
            types.addAll(refTypes);
            for (Class<Object> clazz : types) {
                ProxyBuilder.ensureAccess(targetModule, clazz);
            }
            String pkgName = nonExported ? "com.sun.proxy." + targetModule.getName() : targetModule.getName();
            return new ProxyClassContext(targetModule, pkgName, 1);
        }

        private static void ensureAccess(Module target, Class<?> c) {
            String pn;
            Module m = c.getModule();
            if (!target.canRead(m)) {
                Modules.addReads(target, m);
            }
            if (!m.isExported(pn = c.getPackageName(), target)) {
                Modules.addExports(m, pn, target);
            }
        }

        private static void ensureVisible(ClassLoader ld, Class<?> c) {
            Class<?> type = null;
            try {
                type = Class.forName(c.getName(), false, ld);
            }
            catch (ClassNotFoundException classNotFoundException) {
                // empty catch block
            }
            if (type != c) {
                throw new IllegalArgumentException(c.getName() + " referenced from a method is not visible from class loader: " + JLA.getLoaderNameID(ld));
            }
        }

        private static Class<?> getElementType(Class<?> type) {
            Class<?> e = type;
            while (e.isArray()) {
                e = e.getComponentType();
            }
            return e;
        }

        private static Module getDynamicModule(ClassLoader loader) {
            return dynProxyModules.computeIfAbsent(loader, (ld, clv) -> {
                String mn = "jdk.proxy" + counter.incrementAndGet();
                String pn = "com.sun.proxy." + mn;
                ModuleDescriptor descriptor = ModuleDescriptor.newModule(mn, Set.of(ModuleDescriptor.Modifier.SYNTHETIC)).packages(Set.of(pn, mn)).exports(mn).build();
                Module m = Modules.defineModule(ld, descriptor, null);
                Modules.addReads(m, Proxy.class.getModule());
                Modules.addExports(m, mn);
                Modules.addOpens(m, pn, Proxy.class.getModule());
                Modules.addOpens(m, mn, Proxy.class.getModule());
                return m;
            });
        }

        private record ProxyClassContext(Module module, String packageName, int accessFlags) {
            private ProxyClassContext {
                if (module.isNamed()) {
                    if (packageName.isEmpty()) {
                        throw new InternalError("Unnamed package cannot be added to " + module);
                    }
                    if (!module.getDescriptor().packages().contains(packageName)) {
                        throw new InternalError(packageName + " not exist in " + module.getName());
                    }
                    if (!module.isOpen(packageName, Proxy.class.getModule())) {
                        throw new InternalError(packageName + " not open to " + Proxy.class.getModule());
                    }
                } else if (Modifier.isPublic(accessFlags)) {
                    throw new InternalError("public proxy in unnamed module: " + module);
                }
                if ((accessFlags & 0xFFFFFFFE) != 0) {
                    throw new InternalError("proxy access flags must be Modifier.PUBLIC or 0");
                }
            }
        }
    }

    static class InvocationException
    extends ReflectiveOperationException {
        private static final long serialVersionUID = 0L;
        @Stable
        static MethodHandle wrapMethodHandle;

        InvocationException(Throwable cause) {
            super(cause);
        }

        static Object wrap(Throwable cause) throws InvocationException {
            throw new InvocationException(cause);
        }

        static MethodHandle wrapMH() {
            MethodHandle mh = wrapMethodHandle;
            if (mh == null) {
                try {
                    wrapMethodHandle = mh = MethodHandles.lookup().findStatic(InvocationException.class, "wrap", MethodType.methodType(Object.class, Throwable.class));
                }
                catch (IllegalAccessException | NoSuchMethodException e) {
                    throw new InternalError(e);
                }
            }
            return mh;
        }
    }
}

