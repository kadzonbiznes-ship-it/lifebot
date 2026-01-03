/*
 * Decompiled with CFR 0.152.
 */
package sun.util.resources;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.spi.ResourceBundleProvider;
import jdk.internal.access.JavaUtilResourceBundleAccess;
import jdk.internal.access.SharedSecrets;

public abstract class Bundles {
    private static final int INITIAL_CACHE_SIZE = 32;
    private static final ResourceBundle NONEXISTENT_BUNDLE = new ResourceBundle(){

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
    private static final JavaUtilResourceBundleAccess bundleAccess = SharedSecrets.getJavaUtilResourceBundleAccess();
    private static final ConcurrentMap<CacheKey, BundleReference> cacheList = new ConcurrentHashMap<CacheKey, BundleReference>(32);
    private static final ReferenceQueue<Object> referenceQueue = new ReferenceQueue();

    private Bundles() {
    }

    public static ResourceBundle of(String baseName, Locale locale, Strategy strategy) {
        return Bundles.loadBundleOf(baseName, locale, strategy);
    }

    private static ResourceBundle loadBundleOf(String baseName, Locale targetLocale, Strategy strategy) {
        List<Locale> candidateLocales;
        Objects.requireNonNull(baseName);
        Objects.requireNonNull(targetLocale);
        Objects.requireNonNull(strategy);
        CacheKey cacheKey = new CacheKey(baseName, targetLocale);
        ResourceBundle bundle = null;
        BundleReference bundleRef = (BundleReference)cacheList.get(cacheKey);
        if (bundleRef != null) {
            bundle = (ResourceBundle)bundleRef.get();
        }
        if (Bundles.isValidBundle(bundle)) {
            return bundle;
        }
        Class<? extends ResourceBundleProvider> type = strategy.getResourceBundleProviderType(baseName, targetLocale);
        if (type != null) {
            ServiceLoader<ResourceBundleProvider> providers = ServiceLoader.loadInstalled(type);
            cacheKey.setProviders(providers);
        }
        if ((bundle = Bundles.findBundleOf(cacheKey, strategy, baseName, candidateLocales = strategy.getCandidateLocales(baseName, targetLocale), 0)) == null) {
            Bundles.throwMissingResourceException(baseName, targetLocale, cacheKey.getCause());
        }
        return bundle;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ResourceBundle findBundleOf(CacheKey cacheKey, Strategy strategy, String baseName, List<Locale> candidateLocales, int index) {
        ResourceBundle bundle;
        Locale targetLocale;
        ResourceBundle parent;
        block17: {
            ServiceLoader<ResourceBundleProvider> providers;
            parent = null;
            targetLocale = candidateLocales.get(index);
            if (index != candidateLocales.size() - 1) {
                parent = Bundles.findBundleOf(cacheKey, strategy, baseName, candidateLocales, index + 1);
            }
            Bundles.cleanupCache();
            cacheKey.setLocale(targetLocale);
            bundle = Bundles.findBundleInCache(cacheKey);
            if (bundle != null) {
                if (bundle == NONEXISTENT_BUNDLE) {
                    return parent;
                }
                if (bundleAccess.getParent(bundle) == parent) {
                    return bundle;
                }
                BundleReference bundleRef = (BundleReference)cacheList.get(cacheKey);
                if (bundleRef != null && bundleRef.get() == bundle) {
                    cacheList.remove(cacheKey, bundleRef);
                }
            }
            if ((providers = cacheKey.getProviders()) != null && strategy.getResourceBundleProviderType(baseName, targetLocale) == null) {
                providers = null;
            }
            CacheKey constKey = (CacheKey)cacheKey.clone();
            try {
                if (providers != null) {
                    bundle = Bundles.loadBundleFromProviders(baseName, targetLocale, providers, cacheKey);
                    break block17;
                }
                try {
                    String otherBundleName;
                    String bundleName = strategy.toBundleName(baseName, targetLocale);
                    Class<?> c = Class.forName(Bundles.class.getModule(), bundleName);
                    if (c != null && ResourceBundle.class.isAssignableFrom(c)) {
                        Class<?> bundleClass = c;
                        bundle = bundleAccess.newResourceBundle(bundleClass);
                    }
                    if (bundle == null && !bundleName.equals(otherBundleName = Bundles.toOtherBundleName(baseName, bundleName, targetLocale)) && (c = Class.forName(Bundles.class.getModule(), otherBundleName)) != null && ResourceBundle.class.isAssignableFrom(c)) {
                        Class<?> bundleClass = c;
                        bundle = bundleAccess.newResourceBundle(bundleClass);
                    }
                }
                catch (Exception e) {
                    cacheKey.setCause(e);
                }
            }
            finally {
                if (constKey.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        if (bundle == null) {
            Bundles.putBundleInCache(cacheKey, NONEXISTENT_BUNDLE);
            return parent;
        }
        if (parent != null && bundleAccess.getParent(bundle) == null) {
            bundleAccess.setParent(bundle, parent);
        }
        bundleAccess.setLocale(bundle, targetLocale);
        bundleAccess.setName(bundle, baseName);
        bundle = Bundles.putBundleInCache(cacheKey, bundle);
        return bundle;
    }

    private static void cleanupCache() {
        Reference<Object> ref;
        while ((ref = referenceQueue.poll()) != null) {
            cacheList.remove(((CacheKeyReference)((Object)ref)).getCacheKey());
        }
    }

    private static ResourceBundle loadBundleFromProviders(final String baseName, final Locale locale, final ServiceLoader<ResourceBundleProvider> providers, final CacheKey cacheKey) {
        return AccessController.doPrivileged(new PrivilegedAction<ResourceBundle>(){

            @Override
            public ResourceBundle run() {
                Iterator itr = providers.iterator();
                while (itr.hasNext()) {
                    try {
                        ResourceBundleProvider provider = (ResourceBundleProvider)itr.next();
                        ResourceBundle bundle = provider.getBundle(baseName, locale);
                        if (bundle == null) continue;
                        return bundle;
                    }
                    catch (SecurityException | ServiceConfigurationError e) {
                        if (cacheKey == null) continue;
                        cacheKey.setCause(e);
                    }
                }
                return null;
            }
        });
    }

    private static boolean isValidBundle(ResourceBundle bundle) {
        return bundle != null && bundle != NONEXISTENT_BUNDLE;
    }

    private static void throwMissingResourceException(String baseName, Locale locale, Throwable cause) {
        if (cause instanceof MissingResourceException) {
            cause = null;
        }
        MissingResourceException e = new MissingResourceException("Can't find bundle for base name " + baseName + ", locale " + locale, baseName + "_" + locale, "");
        e.initCause(cause);
        throw e;
    }

    private static ResourceBundle findBundleInCache(CacheKey cacheKey) {
        BundleReference bundleRef = (BundleReference)cacheList.get(cacheKey);
        if (bundleRef == null) {
            return null;
        }
        return (ResourceBundle)bundleRef.get();
    }

    private static ResourceBundle putBundleInCache(CacheKey cacheKey, ResourceBundle bundle) {
        BundleReference bundleRef;
        CacheKey key = (CacheKey)cacheKey.clone();
        BundleReference result = cacheList.putIfAbsent(key, bundleRef = new BundleReference(bundle, referenceQueue, key));
        if (result != null) {
            ResourceBundle rb = (ResourceBundle)result.get();
            if (rb != null) {
                bundle = rb;
                bundleRef.clear();
            } else {
                cacheList.put(key, bundleRef);
            }
        }
        return bundle;
    }

    public static String toOtherBundleName(String baseName, String bundleName, Locale locale) {
        String otherSuffix;
        String simpleName = baseName.substring(baseName.lastIndexOf(46) + 1);
        String suffix = bundleName.substring(bundleName.lastIndexOf(simpleName) + simpleName.length());
        switch (locale.getLanguage()) {
            case "he": {
                String string = suffix.replaceFirst("^_he(_.*)?$", "_iw$1");
                break;
            }
            case "id": {
                String string = suffix.replaceFirst("^_id(_.*)?$", "_in$1");
                break;
            }
            case "yi": {
                String string = suffix.replaceFirst("^_yi(_.*)?$", "_ji$1");
                break;
            }
            case "iw": {
                String string = suffix.replaceFirst("^_iw(_.*)?$", "_he$1");
                break;
            }
            case "in": {
                String string = suffix.replaceFirst("^_in(_.*)?$", "_id$1");
                break;
            }
            case "ji": {
                String string = suffix.replaceFirst("^_ji(_.*)?$", "_yi$1");
                break;
            }
            default: {
                String string = otherSuffix = suffix;
            }
        }
        if (suffix.equals(otherSuffix)) {
            return bundleName;
        }
        return bundleName.substring(0, bundleName.lastIndexOf(suffix)) + otherSuffix;
    }

    public static interface Strategy {
        public List<Locale> getCandidateLocales(String var1, Locale var2);

        public String toBundleName(String var1, Locale var2);

        public Class<? extends ResourceBundleProvider> getResourceBundleProviderType(String var1, Locale var2);
    }

    private static class CacheKey
    implements Cloneable {
        private String name;
        private Locale locale;
        private Throwable cause;
        private int hashCodeCache;
        private ServiceLoader<ResourceBundleProvider> providers;

        CacheKey(String baseName, Locale locale) {
            this.name = baseName;
            this.locale = locale;
            this.calculateHashCode();
        }

        String getName() {
            return this.name;
        }

        CacheKey setName(String baseName) {
            if (!this.name.equals(baseName)) {
                this.name = baseName;
                this.calculateHashCode();
            }
            return this;
        }

        Locale getLocale() {
            return this.locale;
        }

        CacheKey setLocale(Locale locale) {
            if (!this.locale.equals(locale)) {
                this.locale = locale;
                this.calculateHashCode();
            }
            return this;
        }

        ServiceLoader<ResourceBundleProvider> getProviders() {
            return this.providers;
        }

        void setProviders(ServiceLoader<ResourceBundleProvider> providers) {
            this.providers = providers;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            try {
                CacheKey otherEntry = (CacheKey)other;
                if (this.hashCodeCache != otherEntry.hashCodeCache) {
                    return false;
                }
                return this.locale.equals(otherEntry.locale) && this.name.equals(otherEntry.name);
            }
            catch (ClassCastException | NullPointerException runtimeException) {
                return false;
            }
        }

        public int hashCode() {
            return this.hashCodeCache;
        }

        private void calculateHashCode() {
            this.hashCodeCache = this.name.hashCode() << 3;
            this.hashCodeCache ^= this.locale.hashCode();
        }

        public Object clone() {
            try {
                CacheKey clone = (CacheKey)super.clone();
                clone.cause = null;
                clone.providers = null;
                return clone;
            }
            catch (CloneNotSupportedException e) {
                throw new InternalError(e);
            }
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
            return "CacheKey[" + this.name + ", lc=" + l + ")]";
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

    private static interface CacheKeyReference {
        public CacheKey getCacheKey();
    }
}

