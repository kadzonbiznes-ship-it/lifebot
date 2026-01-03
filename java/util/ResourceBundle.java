/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarEntry;
import java.util.spi.ResourceBundleControlProvider;
import java.util.spi.ResourceBundleProvider;
import java.util.stream.Stream;
import jdk.internal.access.JavaUtilResourceBundleAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.loader.BootLoader;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.reflect.Reflection;
import sun.security.action.GetPropertyAction;
import sun.security.util.SecurityConstants;
import sun.util.locale.BaseLocale;
import sun.util.locale.LocaleObjectCache;
import sun.util.resources.Bundles;

public abstract class ResourceBundle {
    private static final int INITIAL_CACHE_SIZE = 32;
    private static final ResourceBundle NONEXISTENT_BUNDLE;
    private static final ConcurrentMap<CacheKey, BundleReference> cacheList;
    private static final ReferenceQueue<Object> referenceQueue;
    protected ResourceBundle parent = null;
    private Locale locale = null;
    private String name;
    private volatile boolean expired;
    private volatile CacheKey cacheKey;
    private volatile Set<String> keySet;
    private static final String UNKNOWN_FORMAT = "";
    private static final boolean TRACE_ON;

    public String getBaseBundleName() {
        return this.name;
    }

    public final String getString(String key) {
        return (String)this.getObject(key);
    }

    public final String[] getStringArray(String key) {
        return (String[])this.getObject(key);
    }

    public final Object getObject(String key) {
        Object obj = this.handleGetObject(key);
        if (obj == null) {
            if (this.parent != null) {
                obj = this.parent.getObject(key);
            }
            if (obj == null) {
                throw new MissingResourceException("Can't find resource for bundle " + this.getClass().getName() + ", key " + key, this.getClass().getName(), key);
            }
        }
        return obj;
    }

    public Locale getLocale() {
        return this.locale;
    }

    private static ClassLoader getLoader(Module module) {
        PrivilegedAction<ClassLoader> pa = module::getClassLoader;
        return AccessController.doPrivileged(pa);
    }

    private static ClassLoader getLoaderForControl(Module module) {
        ClassLoader loader = ResourceBundle.getLoader(module);
        return loader == null ? ClassLoader.getPlatformClassLoader() : loader;
    }

    protected void setParent(ResourceBundle parent) {
        assert (parent != NONEXISTENT_BUNDLE);
        this.parent = parent;
    }

    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName) {
        Class<?> caller = Reflection.getCallerClass();
        return ResourceBundle.getBundleImpl(baseName, Locale.getDefault(), caller, ResourceBundle.getDefaultControl(caller, baseName));
    }

    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName, Control control) {
        Class<?> caller = Reflection.getCallerClass();
        Locale targetLocale = Locale.getDefault();
        ResourceBundle.checkNamedModule(caller);
        return ResourceBundle.getBundleImpl(baseName, targetLocale, caller, control);
    }

    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName, Locale locale) {
        Class<?> caller = Reflection.getCallerClass();
        return ResourceBundle.getBundleImpl(baseName, locale, caller, ResourceBundle.getDefaultControl(caller, baseName));
    }

    @CallerSensitive
    public static ResourceBundle getBundle(String baseName, Module module) {
        return ResourceBundle.getBundleFromModule(Reflection.getCallerClass(), module, baseName, Locale.getDefault(), ResourceBundle.getDefaultControl(module, baseName));
    }

    @CallerSensitive
    public static ResourceBundle getBundle(String baseName, Locale targetLocale, Module module) {
        return ResourceBundle.getBundleFromModule(Reflection.getCallerClass(), module, baseName, targetLocale, ResourceBundle.getDefaultControl(module, baseName));
    }

    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName, Locale targetLocale, Control control) {
        Class<?> caller = Reflection.getCallerClass();
        ResourceBundle.checkNamedModule(caller);
        return ResourceBundle.getBundleImpl(baseName, targetLocale, caller, control);
    }

    @CallerSensitive
    public static ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader) {
        if (loader == null) {
            throw new NullPointerException();
        }
        Class<?> caller = Reflection.getCallerClass();
        return ResourceBundle.getBundleImpl(baseName, locale, caller, loader, ResourceBundle.getDefaultControl(caller, baseName));
    }

    @CallerSensitive
    public static ResourceBundle getBundle(String baseName, Locale targetLocale, ClassLoader loader, Control control) {
        if (loader == null || control == null) {
            throw new NullPointerException();
        }
        Class<?> caller = Reflection.getCallerClass();
        ResourceBundle.checkNamedModule(caller);
        return ResourceBundle.getBundleImpl(baseName, targetLocale, caller, loader, control);
    }

    private static Control getDefaultControl(Class<?> caller, String baseName) {
        Module callerModule = ResourceBundle.getCallerModule(caller);
        return ResourceBundle.getDefaultControl(callerModule, baseName);
    }

    private static Control getDefaultControl(Module targetModule, String baseName) {
        return targetModule.isNamed() ? Control.INSTANCE : ResourceBundleControlProviderHolder.getControl(baseName);
    }

    private static void checkNamedModule(Class<?> caller) {
        Module callerModule = ResourceBundle.getCallerModule(caller);
        if (callerModule.isNamed()) {
            throw new UnsupportedOperationException("ResourceBundle.Control not supported in named modules");
        }
    }

    private static ResourceBundle getBundleImpl(String baseName, Locale locale, Class<?> caller, Control control) {
        ClassLoader loader = ResourceBundle.getLoader(ResourceBundle.getCallerModule(caller));
        return ResourceBundle.getBundleImpl(baseName, locale, caller, loader, control);
    }

    private static Module getCallerModule(Class<?> caller) {
        return caller != null ? caller.getModule() : ClassLoader.getSystemClassLoader().getUnnamedModule();
    }

    private static ResourceBundle getBundleImpl(String baseName, Locale locale, Class<?> caller, ClassLoader loader, Control control) {
        Module callerModule = ResourceBundle.getCallerModule(caller);
        if (callerModule.isNamed() && loader == ResourceBundle.getLoader(callerModule)) {
            return ResourceBundle.getBundleImpl(callerModule, callerModule, baseName, locale, control);
        }
        Module unnamedModule = loader != null ? loader.getUnnamedModule() : BootLoader.getUnnamedModule();
        return ResourceBundle.getBundleImpl(callerModule, unnamedModule, baseName, locale, control);
    }

    private static ResourceBundle getBundleFromModule(Class<?> caller, Module module, String baseName, Locale locale, Control control) {
        SecurityManager sm;
        Objects.requireNonNull(module);
        Module callerModule = ResourceBundle.getCallerModule(caller);
        if (callerModule != module && (sm = System.getSecurityManager()) != null) {
            sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
        }
        return ResourceBundle.getBundleImpl(callerModule, module, baseName, locale, control);
    }

    private static ResourceBundle getBundleImpl(Module callerModule, Module module, String baseName, Locale locale, Control control) {
        if (locale == null || control == null) {
            throw new NullPointerException();
        }
        CacheKey cacheKey = new CacheKey(baseName, locale, module, callerModule);
        ResourceBundle bundle = null;
        BundleReference bundleRef = (BundleReference)cacheList.get(cacheKey);
        if (bundleRef != null) {
            bundle = (ResourceBundle)bundleRef.get();
            bundleRef = null;
        }
        if (ResourceBundle.isValidBundle(bundle) && ResourceBundle.hasValidParentChain(bundle)) {
            return bundle;
        }
        boolean isKnownControl = control == Control.INSTANCE || control instanceof SingleFormatControl;
        List<String> formats = control.getFormats(baseName);
        if (!isKnownControl && !ResourceBundle.checkList(formats)) {
            throw new IllegalArgumentException("Invalid Control: getFormats");
        }
        ResourceBundle baseBundle = null;
        Locale targetLocale = locale;
        while (targetLocale != null) {
            List<Locale> candidateLocales = control.getCandidateLocales(baseName, targetLocale);
            if (!isKnownControl && !ResourceBundle.checkList(candidateLocales)) {
                throw new IllegalArgumentException("Invalid Control: getCandidateLocales");
            }
            bundle = ResourceBundle.findBundle(callerModule, module, cacheKey, candidateLocales, formats, 0, control, baseBundle);
            if (ResourceBundle.isValidBundle(bundle)) {
                boolean isBaseBundle = Locale.ROOT.equals(bundle.locale);
                if (!isBaseBundle || bundle.locale.equals(locale) || candidateLocales.size() == 1 && bundle.locale.equals(candidateLocales.get(0))) break;
                if (isBaseBundle && baseBundle == null) {
                    baseBundle = bundle;
                }
            }
            targetLocale = control.getFallbackLocale(baseName, targetLocale);
        }
        if (bundle == null) {
            if (baseBundle == null) {
                ResourceBundle.throwMissingResourceException(baseName, locale, cacheKey.getCause());
            }
            bundle = baseBundle;
        }
        Reference.reachabilityFence(callerModule);
        Reference.reachabilityFence(module);
        return bundle;
    }

    private static boolean checkList(List<?> a) {
        boolean valid;
        boolean bl = valid = a != null && !a.isEmpty();
        if (valid) {
            int size = a.size();
            for (int i = 0; valid && i < size; ++i) {
                valid = a.get(i) != null;
            }
        }
        return valid;
    }

    private static ResourceBundle findBundle(Module callerModule, Module module, CacheKey cacheKey, List<Locale> candidateLocales, List<String> formats, int index, Control control, ResourceBundle baseBundle) {
        Reference<Object> ref;
        Locale targetLocale = candidateLocales.get(index);
        ResourceBundle parent = null;
        if (index != candidateLocales.size() - 1) {
            parent = ResourceBundle.findBundle(callerModule, module, cacheKey, candidateLocales, formats, index + 1, control, baseBundle);
        } else if (baseBundle != null && Locale.ROOT.equals(targetLocale)) {
            return baseBundle;
        }
        while ((ref = referenceQueue.poll()) != null) {
            cacheList.remove(((CacheKeyReference)((Object)ref)).getCacheKey());
        }
        boolean expiredBundle = false;
        cacheKey.setLocale(targetLocale);
        ResourceBundle bundle = ResourceBundle.findBundleInCache(cacheKey, control);
        if (ResourceBundle.isValidBundle(bundle) && !(expiredBundle = bundle.expired)) {
            if (bundle.parent == parent) {
                return bundle;
            }
            BundleReference bundleRef = (BundleReference)cacheList.get(cacheKey);
            if (bundleRef != null && bundleRef.refersTo(bundle)) {
                cacheList.remove(cacheKey, bundleRef);
            }
        }
        if (bundle != NONEXISTENT_BUNDLE) {
            ResourceBundle.trace("findBundle: %d %s %s formats: %s%n", index, candidateLocales, cacheKey, formats);
            bundle = module.isNamed() ? ResourceBundle.loadBundle(cacheKey, formats, control, module, callerModule) : ResourceBundle.loadBundle(cacheKey, formats, control, expiredBundle);
            if (bundle != null) {
                if (bundle.parent == null) {
                    bundle.setParent(parent);
                }
                bundle.locale = targetLocale;
                bundle = ResourceBundle.putBundleInCache(cacheKey, bundle, control);
                return bundle;
            }
            ResourceBundle.putBundleInCache(cacheKey, NONEXISTENT_BUNDLE, control);
        }
        return parent;
    }

    private static ResourceBundle loadBundle(CacheKey cacheKey, List<String> formats, Control control, Module module, Module callerModule) {
        String baseName = cacheKey.getName();
        Locale targetLocale = cacheKey.getLocale();
        ResourceBundle bundle = null;
        if (cacheKey.hasProviders()) {
            if (callerModule == module) {
                bundle = ResourceBundle.loadBundleFromProviders(baseName, targetLocale, cacheKey.getProviders(), cacheKey);
            } else {
                ClassLoader loader = ResourceBundle.getLoader(module);
                Class<ResourceBundleProvider> svc = ResourceBundle.getResourceBundleProviderType(baseName, loader);
                if (svc != null && Reflection.verifyModuleAccess(callerModule, svc) && callerModule.canUse(svc)) {
                    bundle = ResourceBundle.loadBundleFromProviders(baseName, targetLocale, cacheKey.getProviders(), cacheKey);
                }
            }
            if (bundle != null) {
                cacheKey.setFormat(UNKNOWN_FORMAT);
            }
        }
        if (bundle == null && !cacheKey.callerHasProvider()) {
            for (String format : formats) {
                try {
                    if ((bundle = (switch (format) {
                        case "java.class" -> ResourceBundleProviderHelper.loadResourceBundle(callerModule, module, baseName, targetLocale);
                        case "java.properties" -> ResourceBundleProviderHelper.loadPropertyResourceBundle(callerModule, module, baseName, targetLocale);
                        default -> throw new InternalError("unexpected format: " + format);
                    })) == null) continue;
                    cacheKey.setFormat(format);
                    break;
                }
                catch (Exception | LinkageError e) {
                    cacheKey.setCause(e);
                }
            }
        }
        return bundle;
    }

    private static ServiceLoader<ResourceBundleProvider> getServiceLoader(Module module, String baseName) {
        if (!module.isNamed()) {
            return null;
        }
        ClassLoader loader = ResourceBundle.getLoader(module);
        Class<ResourceBundleProvider> service = ResourceBundle.getResourceBundleProviderType(baseName, loader);
        if (service != null && Reflection.verifyModuleAccess(module, service)) {
            try {
                return ServiceLoader.load(service, loader, module);
            }
            catch (ServiceConfigurationError e) {
                return null;
            }
        }
        return null;
    }

    private static Class<ResourceBundleProvider> getResourceBundleProviderType(String baseName, final ClassLoader loader) {
        int i = baseName.lastIndexOf(46);
        if (i <= 0) {
            return null;
        }
        String name = baseName.substring(i + 1, baseName.length()) + "Provider";
        final String providerName = baseName.substring(0, i) + ".spi." + name;
        return AccessController.doPrivileged(new PrivilegedAction<Class<ResourceBundleProvider>>(){

            @Override
            public Class<ResourceBundleProvider> run() {
                try {
                    Class<ResourceBundleProvider> c = Class.forName(providerName, false, loader);
                    if (ResourceBundleProvider.class.isAssignableFrom(c)) {
                        Class<ResourceBundleProvider> s = c;
                        return s;
                    }
                }
                catch (ClassNotFoundException classNotFoundException) {
                    // empty catch block
                }
                return null;
            }
        });
    }

    private static ResourceBundle loadBundleFromProviders(final String baseName, final Locale locale, final ServiceLoader<ResourceBundleProvider> providers, final CacheKey cacheKey) {
        if (providers == null) {
            return null;
        }
        return AccessController.doPrivileged(new PrivilegedAction<ResourceBundle>(){

            @Override
            public ResourceBundle run() {
                Iterator itr = providers.iterator();
                while (itr.hasNext()) {
                    try {
                        ResourceBundleProvider provider = (ResourceBundleProvider)itr.next();
                        if (cacheKey != null && cacheKey.callerHasProvider == null && cacheKey.getModule() == provider.getClass().getModule()) {
                            cacheKey.callerHasProvider = Boolean.TRUE;
                        }
                        ResourceBundle bundle = provider.getBundle(baseName, locale);
                        ResourceBundle.trace("provider %s %s locale: %s bundle: %s%n", provider, baseName, locale, bundle);
                        if (bundle == null) continue;
                        return bundle;
                    }
                    catch (SecurityException | ServiceConfigurationError e) {
                        if (cacheKey == null) continue;
                        cacheKey.setCause(e);
                    }
                }
                if (cacheKey != null && cacheKey.callerHasProvider == null) {
                    cacheKey.callerHasProvider = Boolean.FALSE;
                }
                return null;
            }
        });
    }

    private static ResourceBundle loadBundle(CacheKey cacheKey, List<String> formats, Control control, boolean reload) {
        Locale targetLocale = cacheKey.getLocale();
        Module module = cacheKey.getModule();
        if (module == null) {
            throw new InternalError("Module for cache key: " + cacheKey + " has been GCed.");
        }
        ClassLoader loader = ResourceBundle.getLoaderForControl(module);
        ResourceBundle bundle = null;
        for (String format : formats) {
            try {
                bundle = control.newBundle(cacheKey.getName(), targetLocale, format, loader, reload);
            }
            catch (Exception | LinkageError error) {
                cacheKey.setCause(error);
            }
            if (bundle == null) continue;
            cacheKey.setFormat(format);
            bundle.name = cacheKey.getName();
            bundle.locale = targetLocale;
            bundle.expired = false;
            break;
        }
        return bundle;
    }

    private static boolean isValidBundle(ResourceBundle bundle) {
        return bundle != null && bundle != NONEXISTENT_BUNDLE;
    }

    private static boolean hasValidParentChain(ResourceBundle bundle) {
        long now = System.currentTimeMillis();
        while (bundle != null) {
            long expirationTime;
            if (bundle.expired) {
                return false;
            }
            CacheKey key = bundle.cacheKey;
            if (key != null && (expirationTime = key.expirationTime) >= 0L && expirationTime <= now) {
                return false;
            }
            bundle = bundle.parent;
        }
        return true;
    }

    private static void throwMissingResourceException(String baseName, Locale locale, Throwable cause) {
        if (cause instanceof MissingResourceException) {
            cause = null;
        }
        throw new MissingResourceException("Can't find bundle for base name " + baseName + ", locale " + locale, baseName + "_" + locale, UNKNOWN_FORMAT, cause);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ResourceBundle findBundleInCache(CacheKey cacheKey, Control control) {
        BundleReference bundleRef = (BundleReference)cacheList.get(cacheKey);
        if (bundleRef == null) {
            return null;
        }
        ResourceBundle bundle = (ResourceBundle)bundleRef.get();
        if (bundle == null) {
            return null;
        }
        ResourceBundle p = bundle.parent;
        assert (p != NONEXISTENT_BUNDLE);
        if (p != null && p.expired) {
            assert (bundle != NONEXISTENT_BUNDLE);
            bundle.expired = true;
            bundle.cacheKey = null;
            cacheList.remove(cacheKey, bundleRef);
            bundle = null;
        } else {
            CacheKey key = bundleRef.getCacheKey();
            long expirationTime = key.expirationTime;
            if (!bundle.expired && expirationTime >= 0L && expirationTime <= System.currentTimeMillis()) {
                if (bundle != NONEXISTENT_BUNDLE) {
                    ResourceBundle resourceBundle = bundle;
                    synchronized (resourceBundle) {
                        expirationTime = key.expirationTime;
                        if (!bundle.expired && expirationTime >= 0L && expirationTime <= System.currentTimeMillis()) {
                            try {
                                Module module = cacheKey.getModule();
                                bundle.expired = module == null || control.needsReload(key.getName(), key.getLocale(), key.getFormat(), ResourceBundle.getLoaderForControl(module), bundle, key.loadTime);
                            }
                            catch (Exception e) {
                                cacheKey.setCause(e);
                            }
                            if (bundle.expired) {
                                bundle.cacheKey = null;
                                cacheList.remove(cacheKey, bundleRef);
                            } else {
                                ResourceBundle.setExpirationTime(key, control);
                            }
                        }
                    }
                }
                cacheList.remove(cacheKey, bundleRef);
                bundle = null;
            }
        }
        return bundle;
    }

    private static ResourceBundle putBundleInCache(CacheKey cacheKey, ResourceBundle bundle, Control control) {
        ResourceBundle.setExpirationTime(cacheKey, control);
        if (cacheKey.expirationTime != -1L) {
            CacheKey key = new CacheKey(cacheKey);
            BundleReference bundleRef = new BundleReference(bundle, referenceQueue, key);
            bundle.cacheKey = key;
            BundleReference result = cacheList.putIfAbsent(key, bundleRef);
            if (result != null) {
                ResourceBundle rb = (ResourceBundle)result.get();
                if (rb != null && !rb.expired) {
                    bundle.cacheKey = null;
                    bundle = rb;
                    bundleRef.clear();
                } else {
                    cacheList.put(key, bundleRef);
                }
            }
        }
        return bundle;
    }

    private static void setExpirationTime(CacheKey cacheKey, Control control) {
        long ttl = control.getTimeToLive(cacheKey.getName(), cacheKey.getLocale());
        if (ttl >= 0L) {
            long now;
            cacheKey.loadTime = now = System.currentTimeMillis();
            cacheKey.expirationTime = now + ttl;
        } else if (ttl >= -2L) {
            cacheKey.expirationTime = ttl;
        } else {
            throw new IllegalArgumentException("Invalid Control: TTL=" + ttl);
        }
    }

    @CallerSensitive
    public static final void clearCache() {
        Module callerModule = ResourceBundle.getCallerModule(Reflection.getCallerClass());
        cacheList.keySet().removeIf(key -> key.getCallerModule() == callerModule);
    }

    public static final void clearCache(ClassLoader loader) {
        Objects.requireNonNull(loader);
        cacheList.keySet().removeIf(key -> {
            Module m = key.getModule();
            return m != null && ResourceBundle.getLoader(m) == loader;
        });
    }

    protected abstract Object handleGetObject(String var1);

    public abstract Enumeration<String> getKeys();

    public boolean containsKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        ResourceBundle rb = this;
        while (rb != null) {
            if (rb.handleKeySet().contains(key)) {
                return true;
            }
            rb = rb.parent;
        }
        return false;
    }

    public Set<String> keySet() {
        HashSet<String> keys = new HashSet<String>();
        ResourceBundle rb = this;
        while (rb != null) {
            keys.addAll(rb.handleKeySet());
            rb = rb.parent;
        }
        return keys;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Set<String> handleKeySet() {
        if (this.keySet == null) {
            ResourceBundle resourceBundle = this;
            synchronized (resourceBundle) {
                if (this.keySet == null) {
                    HashSet<String> keys = new HashSet<String>();
                    Enumeration<String> enumKeys = this.getKeys();
                    while (enumKeys.hasMoreElements()) {
                        String key = enumKeys.nextElement();
                        if (this.handleGetObject(key) == null) continue;
                        keys.add(key);
                    }
                    this.keySet = keys;
                }
            }
        }
        return this.keySet;
    }

    private static <T extends Throwable> void uncheckedThrow(Throwable t) throws T {
        if (t != null) {
            throw t;
        }
        throw new Error("Unknown Exception");
    }

    private static void trace(String format, Object ... params) {
        if (TRACE_ON) {
            System.out.format(format, params);
        }
    }

    static {
        SharedSecrets.setJavaUtilResourceBundleAccess(new JavaUtilResourceBundleAccess(){

            @Override
            public void setParent(ResourceBundle bundle, ResourceBundle parent) {
                bundle.setParent(parent);
            }

            @Override
            public ResourceBundle getParent(ResourceBundle bundle) {
                return bundle.parent;
            }

            @Override
            public void setLocale(ResourceBundle bundle, Locale locale) {
                bundle.locale = locale;
            }

            @Override
            public void setName(ResourceBundle bundle, String name) {
                bundle.name = name;
            }

            @Override
            public ResourceBundle getBundle(String baseName, Locale locale, Module module) {
                return ResourceBundle.getBundleImpl(module, module, baseName, locale, ResourceBundle.getDefaultControl(module, baseName));
            }

            @Override
            public ResourceBundle newResourceBundle(Class<? extends ResourceBundle> bundleClass) {
                return ResourceBundleProviderHelper.newResourceBundle(bundleClass);
            }
        });
        NONEXISTENT_BUNDLE = new ResourceBundle(){

            @Override
            public Enumeration<String> getKeys() {
                return null;
            }

            @Override
            protected Object handleGetObject(String key) {
                return null;
            }

            public String toString() {
                return "NONEXISTENT_BUNDLE";
            }
        };
        cacheList = new ConcurrentHashMap<CacheKey, BundleReference>(32);
        referenceQueue = new ReferenceQueue();
        TRACE_ON = Boolean.parseBoolean(GetPropertyAction.privilegedGetProperty("resource.bundle.debug", "false"));
    }

    public static class Control {
        public static final List<String> FORMAT_DEFAULT = List.of("java.class", "java.properties");
        public static final List<String> FORMAT_CLASS = List.of("java.class");
        public static final List<String> FORMAT_PROPERTIES = List.of("java.properties");
        public static final long TTL_DONT_CACHE = -1L;
        public static final long TTL_NO_EXPIRATION_CONTROL = -2L;
        private static final Control INSTANCE = new Control();
        private static final CandidateListCache CANDIDATES_CACHE = new CandidateListCache();

        protected Control() {
        }

        public static final Control getControl(List<String> formats) {
            if (formats.equals(FORMAT_PROPERTIES)) {
                return SingleFormatControl.PROPERTIES_ONLY;
            }
            if (formats.equals(FORMAT_CLASS)) {
                return SingleFormatControl.CLASS_ONLY;
            }
            if (formats.equals(FORMAT_DEFAULT)) {
                return INSTANCE;
            }
            throw new IllegalArgumentException();
        }

        public static final Control getNoFallbackControl(List<String> formats) {
            if (formats.equals(FORMAT_DEFAULT)) {
                return NoFallbackControl.NO_FALLBACK;
            }
            if (formats.equals(FORMAT_PROPERTIES)) {
                return NoFallbackControl.PROPERTIES_ONLY_NO_FALLBACK;
            }
            if (formats.equals(FORMAT_CLASS)) {
                return NoFallbackControl.CLASS_ONLY_NO_FALLBACK;
            }
            throw new IllegalArgumentException();
        }

        public List<String> getFormats(String baseName) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            return FORMAT_DEFAULT;
        }

        public List<Locale> getCandidateLocales(String baseName, Locale locale) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            return new ArrayList<Locale>((Collection)CANDIDATES_CACHE.get(locale.getBaseLocale()));
        }

        public Locale getFallbackLocale(String baseName, Locale locale) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            Locale defaultLocale = Locale.getDefault();
            return locale.equals(defaultLocale) ? null : defaultLocale;
        }

        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            String otherBundleName;
            String bundleName = this.toBundleName(baseName, locale);
            ResourceBundle bundle = this.newBundle0(bundleName, format, loader, reload);
            if (bundle == null && !bundleName.equals(otherBundleName = Bundles.toOtherBundleName(baseName, bundleName, locale))) {
                bundle = this.newBundle0(otherBundleName, format, loader, reload);
            }
            return bundle;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private ResourceBundle newBundle0(String bundleName, String format, final ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            ResourceBundle bundle;
            block20: {
                bundle = null;
                if (format.equals("java.class")) {
                    try {
                        Class<?> c = loader.loadClass(bundleName);
                        if (ResourceBundle.class.isAssignableFrom(c)) {
                            final Class<?> bundleClass = c;
                            Module m = bundleClass.getModule();
                            if (m.isNamed() && !m.isOpen(bundleClass.getPackageName())) {
                                throw new IllegalAccessException("unnamed module can't load " + bundleClass.getName() + " in " + m.toString());
                            }
                            try {
                                Constructor<ResourceBundle> ctor = AccessController.doPrivileged(new PrivilegedExceptionAction<Constructor<ResourceBundle>>(this){

                                    @Override
                                    public Constructor<ResourceBundle> run() throws NoSuchMethodException {
                                        return bundleClass.getDeclaredConstructor(new Class[0]);
                                    }
                                });
                                if (!Modifier.isPublic(ctor.getModifiers())) {
                                    throw new IllegalAccessException("no-arg constructor in " + bundleClass.getName() + " is not publicly accessible.");
                                }
                                PrivilegedAction<Void> pa1 = () -> {
                                    ctor.setAccessible(true);
                                    return null;
                                };
                                AccessController.doPrivileged(pa1);
                                bundle = ctor.newInstance(null);
                                break block20;
                            }
                            catch (InvocationTargetException e) {
                                ResourceBundle.uncheckedThrow(e);
                                break block20;
                            }
                            catch (PrivilegedActionException e) {
                                assert (e.getCause() instanceof NoSuchMethodException);
                                throw new InstantiationException("public no-arg constructor does not exist in " + bundleClass.getName());
                            }
                        }
                        throw new ClassCastException(c.getName() + " cannot be cast to ResourceBundle");
                    }
                    catch (ClassNotFoundException c) {}
                } else if (format.equals("java.properties")) {
                    final String resourceName = this.toResourceName0(bundleName, "properties");
                    if (resourceName == null) {
                        return bundle;
                    }
                    final boolean reloadFlag = reload;
                    InputStream stream = null;
                    try {
                        stream = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>(this){

                            @Override
                            public InputStream run() throws IOException {
                                URL url = loader.getResource(resourceName);
                                if (url == null) {
                                    return null;
                                }
                                URLConnection connection = url.openConnection();
                                if (reloadFlag) {
                                    connection.setUseCaches(false);
                                }
                                return connection.getInputStream();
                            }
                        });
                    }
                    catch (PrivilegedActionException e) {
                        throw (IOException)e.getCause();
                    }
                    if (stream != null) {
                        try {
                            bundle = new PropertyResourceBundle(stream);
                        }
                        finally {
                            stream.close();
                        }
                    }
                } else {
                    throw new IllegalArgumentException("unknown format: " + format);
                }
            }
            return bundle;
        }

        public long getTimeToLive(String baseName, Locale locale) {
            if (baseName == null || locale == null) {
                throw new NullPointerException();
            }
            return -2L;
        }

        public boolean needsReload(String baseName, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime) {
            if (bundle == null) {
                throw new NullPointerException();
            }
            if (format.equals("java.class") || format.equals("java.properties")) {
                format = format.substring(5);
            }
            boolean result = false;
            try {
                String resourceName = this.toResourceName0(this.toBundleName(baseName, locale), format);
                if (resourceName == null) {
                    return result;
                }
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    long lastModified = 0L;
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        if (connection instanceof JarURLConnection) {
                            JarEntry ent = ((JarURLConnection)connection).getJarEntry();
                            if (ent != null && (lastModified = ent.getTime()) == -1L) {
                                lastModified = 0L;
                            }
                        } else {
                            lastModified = connection.getLastModified();
                        }
                    }
                    result = lastModified >= loadTime;
                }
            }
            catch (NullPointerException npe) {
                throw npe;
            }
            catch (Exception exception) {
                // empty catch block
            }
            return result;
        }

        public String toBundleName(String baseName, Locale locale) {
            if (locale == Locale.ROOT) {
                return baseName;
            }
            String language = locale.getLanguage();
            String script = locale.getScript();
            String country = locale.getCountry();
            String variant = locale.getVariant();
            if (language == ResourceBundle.UNKNOWN_FORMAT && country == ResourceBundle.UNKNOWN_FORMAT && variant == ResourceBundle.UNKNOWN_FORMAT) {
                return baseName;
            }
            StringBuilder sb = new StringBuilder(baseName);
            sb.append('_');
            if (script != ResourceBundle.UNKNOWN_FORMAT) {
                if (variant != ResourceBundle.UNKNOWN_FORMAT) {
                    sb.append(language).append('_').append(script).append('_').append(country).append('_').append(variant);
                } else if (country != ResourceBundle.UNKNOWN_FORMAT) {
                    sb.append(language).append('_').append(script).append('_').append(country);
                } else {
                    sb.append(language).append('_').append(script);
                }
            } else if (variant != ResourceBundle.UNKNOWN_FORMAT) {
                sb.append(language).append('_').append(country).append('_').append(variant);
            } else if (country != ResourceBundle.UNKNOWN_FORMAT) {
                sb.append(language).append('_').append(country);
            } else {
                sb.append(language);
            }
            return sb.toString();
        }

        public final String toResourceName(String bundleName, String suffix) {
            StringBuilder sb = new StringBuilder(bundleName.length() + 1 + suffix.length());
            sb.append(bundleName.replace('.', '/')).append('.').append(suffix);
            return sb.toString();
        }

        private String toResourceName0(String bundleName, String suffix) {
            if (bundleName.contains("://")) {
                return null;
            }
            return this.toResourceName(bundleName, suffix);
        }

        private static class CandidateListCache
        extends LocaleObjectCache<BaseLocale, List<Locale>> {
            private CandidateListCache() {
            }

            @Override
            protected List<Locale> createObject(BaseLocale base) {
                String language = base.getLanguage();
                String script = base.getScript();
                String region = base.getRegion();
                String variant = base.getVariant();
                boolean isNorwegianBokmal = false;
                boolean isNorwegianNynorsk = false;
                if (language.equals("no")) {
                    if (region.equals("NO") && variant.equals("NY")) {
                        variant = ResourceBundle.UNKNOWN_FORMAT;
                        isNorwegianNynorsk = true;
                    } else {
                        isNorwegianBokmal = true;
                    }
                }
                if (language.equals("nb") || isNorwegianBokmal) {
                    List<Locale> tmpList = CandidateListCache.getDefaultList("nb", script, region, variant);
                    ArrayList<Locale> bokmalList = new ArrayList<Locale>();
                    for (Locale l_nb : tmpList) {
                        boolean isRoot = l_nb.getLanguage().isEmpty();
                        Locale l_no = Locale.getInstance(isRoot ? ResourceBundle.UNKNOWN_FORMAT : "no", l_nb.getScript(), l_nb.getCountry(), l_nb.getVariant(), null);
                        bokmalList.add(isNorwegianBokmal ? l_no : l_nb);
                        if (isRoot) break;
                        bokmalList.add(isNorwegianBokmal ? l_nb : l_no);
                    }
                    return bokmalList;
                }
                if (language.equals("nn") || isNorwegianNynorsk) {
                    List<Locale> nynorskList = CandidateListCache.getDefaultList("nn", script, region, variant);
                    int idx = nynorskList.size() - 1;
                    nynorskList.add(idx++, Locale.getInstance("no", "NO", "NY"));
                    nynorskList.add(idx++, Locale.getInstance("no", "NO", ResourceBundle.UNKNOWN_FORMAT));
                    nynorskList.add(idx++, Locale.getInstance("no", ResourceBundle.UNKNOWN_FORMAT, ResourceBundle.UNKNOWN_FORMAT));
                    return nynorskList;
                }
                if (language.equals("zh") && script.isEmpty() && !region.isEmpty()) {
                    switch (region) {
                        case "TW": 
                        case "HK": 
                        case "MO": {
                            script = "Hant";
                            break;
                        }
                        case "CN": 
                        case "SG": {
                            script = "Hans";
                        }
                    }
                }
                return CandidateListCache.getDefaultList(language, script, region, variant);
            }

            private static List<Locale> getDefaultList(String language, String script, String region, String variant) {
                ArrayList<String> variants = null;
                if (!variant.isEmpty()) {
                    variants = new ArrayList<String>();
                    int idx = variant.length();
                    while (idx != -1) {
                        variants.add(variant.substring(0, idx));
                        --idx;
                        idx = variant.lastIndexOf(95, idx);
                    }
                }
                ArrayList<Locale> list = new ArrayList<Locale>();
                if (variants != null) {
                    for (String v : variants) {
                        list.add(Locale.getInstance(language, script, region, v, null));
                    }
                }
                if (!region.isEmpty()) {
                    list.add(Locale.getInstance(language, script, region, ResourceBundle.UNKNOWN_FORMAT, null));
                }
                if (!script.isEmpty()) {
                    list.add(Locale.getInstance(language, script, ResourceBundle.UNKNOWN_FORMAT, ResourceBundle.UNKNOWN_FORMAT, null));
                    if (language.equals("zh") && region.isEmpty()) {
                        switch (script) {
                            case "Hans": {
                                region = "CN";
                                break;
                            }
                            case "Hant": {
                                region = "TW";
                            }
                        }
                    }
                    if (variants != null) {
                        for (String v : variants) {
                            list.add(Locale.getInstance(language, ResourceBundle.UNKNOWN_FORMAT, region, v, null));
                        }
                    }
                    if (!region.isEmpty()) {
                        list.add(Locale.getInstance(language, ResourceBundle.UNKNOWN_FORMAT, region, ResourceBundle.UNKNOWN_FORMAT, null));
                    }
                }
                if (!language.isEmpty()) {
                    list.add(Locale.getInstance(language, ResourceBundle.UNKNOWN_FORMAT, ResourceBundle.UNKNOWN_FORMAT, ResourceBundle.UNKNOWN_FORMAT, null));
                }
                list.add(Locale.ROOT);
                return list;
            }
        }
    }

    private static class ResourceBundleControlProviderHolder {
        private static final PrivilegedAction<List<ResourceBundleControlProvider>> pa = () -> ServiceLoader.load(ResourceBundleControlProvider.class, ClassLoader.getSystemClassLoader()).stream().map(ServiceLoader.Provider::get).toList();
        private static final List<ResourceBundleControlProvider> CONTROL_PROVIDERS = AccessController.doPrivileged(pa);

        private ResourceBundleControlProviderHolder() {
        }

        private static Control getControl(String baseName) {
            return CONTROL_PROVIDERS.isEmpty() ? Control.INSTANCE : CONTROL_PROVIDERS.stream().flatMap(provider -> Stream.ofNullable(provider.getControl(baseName))).findFirst().orElse(Control.INSTANCE);
        }
    }

    private static final class CacheKey {
        private final String name;
        private volatile Locale locale;
        private final KeyElementReference<Module> moduleRef;
        private final KeyElementReference<Module> callerRef;
        private final int modulesHash;
        private volatile String format;
        private volatile long loadTime;
        private volatile long expirationTime;
        private volatile Throwable cause;
        private volatile ServiceLoader<ResourceBundleProvider> providers;
        private volatile boolean providersChecked;
        private volatile Boolean callerHasProvider;

        CacheKey(String baseName, Locale locale, Module module, Module caller) {
            Objects.requireNonNull(module);
            Objects.requireNonNull(caller);
            this.name = baseName;
            this.locale = locale;
            this.moduleRef = new KeyElementReference<Module>(module, referenceQueue, this);
            this.callerRef = new KeyElementReference<Module>(caller, referenceQueue, this);
            this.modulesHash = module.hashCode() ^ caller.hashCode();
        }

        CacheKey(CacheKey src) {
            this.moduleRef = new KeyElementReference<Module>(Objects.requireNonNull(src.getModule()), referenceQueue, this);
            this.callerRef = new KeyElementReference<Module>(Objects.requireNonNull(src.getCallerModule()), referenceQueue, this);
            this.name = src.name;
            this.locale = src.locale;
            this.modulesHash = src.modulesHash;
            this.format = src.format;
            this.loadTime = src.loadTime;
            this.expirationTime = src.expirationTime;
        }

        String getName() {
            return this.name;
        }

        Locale getLocale() {
            return this.locale;
        }

        CacheKey setLocale(Locale locale) {
            this.locale = locale;
            return this;
        }

        Module getModule() {
            return (Module)this.moduleRef.get();
        }

        Module getCallerModule() {
            return (Module)this.callerRef.get();
        }

        ServiceLoader<ResourceBundleProvider> getProviders() {
            if (!this.providersChecked) {
                this.providers = ResourceBundle.getServiceLoader(this.getModule(), this.name);
                this.providersChecked = true;
            }
            return this.providers;
        }

        boolean hasProviders() {
            return this.getProviders() != null;
        }

        boolean callerHasProvider() {
            return this.callerHasProvider == Boolean.TRUE;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            try {
                CacheKey otherEntry = (CacheKey)other;
                if (this.modulesHash != otherEntry.modulesHash) {
                    return false;
                }
                if (!this.name.equals(otherEntry.name)) {
                    return false;
                }
                if (!this.locale.equals(otherEntry.locale)) {
                    return false;
                }
                Module module = this.getModule();
                Module caller = this.getCallerModule();
                return module != null && module.equals(otherEntry.getModule()) && caller != null && caller.equals(otherEntry.getCallerModule());
            }
            catch (ClassCastException | NullPointerException runtimeException) {
                return false;
            }
        }

        public int hashCode() {
            return this.name.hashCode() << 3 ^ this.locale.hashCode() ^ this.modulesHash;
        }

        String getFormat() {
            return this.format;
        }

        void setFormat(String format) {
            this.format = format;
        }

        private void setCause(Throwable cause) {
            if (this.cause == null) {
                this.cause = cause;
            } else if (this.cause instanceof ClassNotFoundException) {
                this.cause = cause;
            }
        }

        private Throwable getCause() {
            return this.cause;
        }

        public String toString() {
            String l = this.locale.toString();
            if (l.isEmpty()) {
                l = !this.locale.getVariant().isEmpty() ? "__" + this.locale.getVariant() : "\"\"";
            }
            return "CacheKey[" + this.name + ", locale=" + l + ", module=" + this.getModule() + ", callerModule=" + this.getCallerModule() + ", format=" + this.format + "]";
        }
    }

    private static class BundleReference
    extends SoftReference<ResourceBundle>
    implements CacheKeyReference {
        private final CacheKey cacheKey;

        BundleReference(ResourceBundle referent, ReferenceQueue<Object> q, CacheKey key) {
            super(referent, q);
            this.cacheKey = key;
        }

        @Override
        public CacheKey getCacheKey() {
            return this.cacheKey;
        }
    }

    private static class SingleFormatControl
    extends Control {
        private static final Control PROPERTIES_ONLY = new SingleFormatControl(FORMAT_PROPERTIES);
        private static final Control CLASS_ONLY = new SingleFormatControl(FORMAT_CLASS);
        private final List<String> formats;

        protected SingleFormatControl(List<String> formats) {
            this.formats = formats;
        }

        @Override
        public List<String> getFormats(String baseName) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            return this.formats;
        }
    }

    private static interface CacheKeyReference {
        public CacheKey getCacheKey();
    }

    private static class ResourceBundleProviderHelper {
        private ResourceBundleProviderHelper() {
        }

        static ResourceBundle newResourceBundle(Class<? extends ResourceBundle> bundleClass) {
            try {
                Constructor<? extends ResourceBundle> ctor = bundleClass.getConstructor(new Class[0]);
                if (!Modifier.isPublic(ctor.getModifiers())) {
                    return null;
                }
                PrivilegedAction<Void> pa = () -> {
                    ctor.setAccessible(true);
                    return null;
                };
                AccessController.doPrivileged(pa);
                try {
                    return ctor.newInstance(null);
                }
                catch (InvocationTargetException e) {
                    ResourceBundle.uncheckedThrow(e);
                }
                catch (IllegalAccessException | InstantiationException e) {
                    throw new InternalError(e);
                }
            }
            catch (NoSuchMethodException e) {
                throw new InternalError(e);
            }
            return null;
        }

        static ResourceBundle loadResourceBundle(Module callerModule, Module module, String baseName, Locale locale) {
            String bundleName = Control.INSTANCE.toBundleName(baseName, locale);
            try {
                PrivilegedAction<Class> pa = () -> Class.forName(module, bundleName);
                Class<?> c = AccessController.doPrivileged(pa, null, SecurityConstants.GET_CLASSLOADER_PERMISSION);
                ResourceBundle.trace("local in %s %s caller %s: %s%n", module, bundleName, callerModule, c);
                if (c == null) {
                    ClassLoader loader = ResourceBundle.getLoader(module);
                    c = loader != null ? Class.forName(bundleName, false, loader) : BootLoader.loadClassOrNull(bundleName);
                    ResourceBundle.trace("loader for %s %s caller %s: %s%n", module, bundleName, callerModule, c);
                }
                if (c != null && ResourceBundle.class.isAssignableFrom(c)) {
                    Class<?> bundleClass = c;
                    Module m = bundleClass.getModule();
                    if (!ResourceBundleProviderHelper.isAccessible(callerModule, m, bundleClass.getPackageName())) {
                        ResourceBundle.trace("   %s does not have access to %s/%s%n", callerModule, m.getName(), bundleClass.getPackageName());
                        return null;
                    }
                    return ResourceBundleProviderHelper.newResourceBundle(bundleClass);
                }
            }
            catch (ClassNotFoundException classNotFoundException) {
                // empty catch block
            }
            return null;
        }

        static boolean isAccessible(Module callerModule, Module module, String pn) {
            if (!module.isNamed() || callerModule == module) {
                return true;
            }
            return module.isOpen(pn, callerModule);
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        static ResourceBundle loadPropertyResourceBundle(Module callerModule, Module module, String baseName, Locale locale) throws IOException {
            String bundleName = Control.INSTANCE.toBundleName(baseName, locale);
            PrivilegedAction<InputStream> pa = () -> {
                try {
                    InputStream in;
                    String resourceName = Control.INSTANCE.toResourceName0(bundleName, "properties");
                    if (resourceName == null) {
                        return null;
                    }
                    ResourceBundle.trace("local in %s %s caller %s%n", module, resourceName, callerModule);
                    String pn = ResourceBundleProviderHelper.toPackageName(bundleName);
                    ResourceBundle.trace("   %s/%s is accessible to %s : %s%n", module.getName(), pn, callerModule, ResourceBundleProviderHelper.isAccessible(callerModule, module, pn));
                    if (ResourceBundleProviderHelper.isAccessible(callerModule, module, pn) && (in = module.getResourceAsStream(resourceName)) != null) {
                        return in;
                    }
                    ClassLoader loader = module.getClassLoader();
                    ResourceBundle.trace("loader for %s %s caller %s%n", module, resourceName, callerModule);
                    try {
                        if (loader != null) {
                            return loader.getResourceAsStream(resourceName);
                        }
                        URL url = BootLoader.findResource(resourceName);
                        if (url != null) {
                            return url.openStream();
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    return null;
                }
                catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
            try (InputStream stream = AccessController.doPrivileged(pa);){
                if (stream != null) {
                    PropertyResourceBundle propertyResourceBundle = new PropertyResourceBundle(stream);
                    return propertyResourceBundle;
                }
                ResourceBundle resourceBundle = null;
                return resourceBundle;
            }
            catch (UncheckedIOException e) {
                throw e.getCause();
            }
        }

        private static String toPackageName(String bundleName) {
            int i = bundleName.lastIndexOf(46);
            return i != -1 ? bundleName.substring(0, i) : ResourceBundle.UNKNOWN_FORMAT;
        }
    }

    private static final class NoFallbackControl
    extends SingleFormatControl {
        private static final Control NO_FALLBACK = new NoFallbackControl(FORMAT_DEFAULT);
        private static final Control PROPERTIES_ONLY_NO_FALLBACK = new NoFallbackControl(FORMAT_PROPERTIES);
        private static final Control CLASS_ONLY_NO_FALLBACK = new NoFallbackControl(FORMAT_CLASS);

        protected NoFallbackControl(List<String> formats) {
            super(formats);
        }

        @Override
        public Locale getFallbackLocale(String baseName, Locale locale) {
            if (baseName == null || locale == null) {
                throw new NullPointerException();
            }
            return null;
        }
    }

    private static class KeyElementReference<T>
    extends WeakReference<T>
    implements CacheKeyReference {
        private final CacheKey cacheKey;

        KeyElementReference(T referent, ReferenceQueue<Object> q, CacheKey key) {
            super(referent, q);
            this.cacheKey = key;
        }

        @Override
        public CacheKey getCacheKey() {
            return this.cacheKey;
        }
    }
}

