/*
 * Decompiled with CFR 0.152.
 */
package sun.util.resources;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.spi.ResourceBundleProvider;
import sun.util.locale.provider.JRELocaleProviderAdapter;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.ResourceBundleBasedAdapter;
import sun.util.resources.Bundles;
import sun.util.resources.OpenListResourceBundle;
import sun.util.resources.ParallelListResourceBundle;
import sun.util.resources.TimeZoneNamesBundle;

public class LocaleData {
    private static final ResourceBundle.Control defaultControl = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT);
    private static final String DOTCLDR = ".cldr";
    private static final Map<String, List<Locale>> CANDIDATES_MAP = new ConcurrentHashMap<String, List<Locale>>();
    private final LocaleProviderAdapter.Type type;

    public LocaleData(LocaleProviderAdapter.Type type) {
        this.type = type;
    }

    public ResourceBundle getCalendarData(Locale locale) {
        return LocaleData.getBundle(this.type.getUtilResourcesPackage() + ".CalendarData", locale);
    }

    public OpenListResourceBundle getCurrencyNames(Locale locale) {
        return (OpenListResourceBundle)LocaleData.getBundle(this.type.getUtilResourcesPackage() + ".CurrencyNames", locale);
    }

    public OpenListResourceBundle getLocaleNames(Locale locale) {
        return (OpenListResourceBundle)LocaleData.getBundle(this.type.getUtilResourcesPackage() + ".LocaleNames", locale);
    }

    public TimeZoneNamesBundle getTimeZoneNames(Locale locale) {
        return (TimeZoneNamesBundle)LocaleData.getBundle(this.type.getUtilResourcesPackage() + ".TimeZoneNames", locale);
    }

    public ResourceBundle getBreakIteratorInfo(Locale locale) {
        return LocaleData.getBundle(this.type.getTextResourcesPackage() + ".BreakIteratorInfo", locale);
    }

    public ResourceBundle getBreakIteratorResources(Locale locale) {
        return LocaleData.getBundle(this.type.getTextResourcesPackage() + ".BreakIteratorResources", locale);
    }

    public ResourceBundle getCollationData(Locale locale) {
        return LocaleData.getBundle(this.type.getTextResourcesPackage() + ".CollationData", locale);
    }

    public ResourceBundle getDateFormatData(Locale locale) {
        return LocaleData.getBundle(this.type.getTextResourcesPackage() + ".FormatData", locale);
    }

    public void setSupplementary(ParallelListResourceBundle formatData) {
        if (!formatData.areParallelContentsComplete()) {
            String suppName = this.type.getTextResourcesPackage() + ".JavaTimeSupplementary";
            this.setSupplementary(suppName, formatData);
        }
    }

    private boolean setSupplementary(String suppName, ParallelListResourceBundle formatData) {
        ParallelListResourceBundle parent = (ParallelListResourceBundle)formatData.getParent();
        boolean resetKeySet = false;
        if (parent != null) {
            resetKeySet = this.setSupplementary(suppName, parent);
        }
        OpenListResourceBundle supp = LocaleData.getSupplementary(suppName, formatData.getLocale());
        formatData.setParallelContents(supp);
        if (resetKeySet |= supp != null) {
            formatData.resetKeySet();
        }
        return resetKeySet;
    }

    public ResourceBundle getNumberFormatData(Locale locale) {
        return LocaleData.getBundle(this.type.getTextResourcesPackage() + ".FormatData", locale);
    }

    public static ResourceBundle getBundle(final String baseName, final Locale locale) {
        return AccessController.doPrivileged(new PrivilegedAction<ResourceBundle>(){

            @Override
            public ResourceBundle run() {
                return Bundles.of(baseName, locale, LocaleDataStrategy.INSTANCE);
            }
        });
    }

    private static OpenListResourceBundle getSupplementary(final String baseName, final Locale locale) {
        return AccessController.doPrivileged(new PrivilegedAction<OpenListResourceBundle>(){

            @Override
            public OpenListResourceBundle run() {
                OpenListResourceBundle rb = null;
                try {
                    rb = (OpenListResourceBundle)Bundles.of(baseName, locale, SupplementaryStrategy.INSTANCE);
                }
                catch (MissingResourceException missingResourceException) {
                    // empty catch block
                }
                return rb;
            }
        });
    }

    private static class SupplementaryStrategy
    extends LocaleDataStrategy {
        private static final SupplementaryStrategy INSTANCE = new SupplementaryStrategy();
        private static final Set<Locale> JAVA_BASE_LOCALES = Set.of(Locale.ROOT, Locale.ENGLISH, Locale.US);

        private SupplementaryStrategy() {
        }

        @Override
        public List<Locale> getCandidateLocales(String baseName, Locale locale) {
            return List.of(locale);
        }

        @Override
        public Class<? extends ResourceBundleProvider> getResourceBundleProviderType(String baseName, Locale locale) {
            return this.inJavaBaseModule(baseName, locale) ? null : SupplementaryResourceBundleProvider.class;
        }

        @Override
        boolean inJavaBaseModule(String baseName, Locale locale) {
            return JAVA_BASE_LOCALES.contains(locale);
        }
    }

    private static class LocaleDataStrategy
    implements Bundles.Strategy {
        private static final LocaleDataStrategy INSTANCE = new LocaleDataStrategy();
        private static final Set<Locale> JAVA_BASE_LOCALES = Set.of(Locale.ROOT, Locale.ENGLISH, Locale.US, Locale.of("en", "US", "POSIX"));

        private LocaleDataStrategy() {
        }

        @Override
        public List<Locale> getCandidateLocales(String baseName, Locale locale) {
            String key = baseName + '-' + locale.toLanguageTag();
            List<Locale> candidates = CANDIDATES_MAP.get(key);
            if (candidates == null) {
                LocaleProviderAdapter.Type type = baseName.contains(LocaleData.DOTCLDR) ? LocaleProviderAdapter.Type.CLDR : LocaleProviderAdapter.Type.JRE;
                LocaleProviderAdapter adapter = LocaleProviderAdapter.forType(type);
                candidates = adapter instanceof ResourceBundleBasedAdapter ? ((ResourceBundleBasedAdapter)((Object)adapter)).getCandidateLocales(baseName, locale) : defaultControl.getCandidateLocales(baseName, locale);
                int lastDot = baseName.lastIndexOf(46);
                String category = lastDot >= 0 ? baseName.substring(lastDot + 1) : baseName;
                Set<String> langtags = ((JRELocaleProviderAdapter)adapter).getLanguageTagSet(category);
                if (!langtags.isEmpty()) {
                    Iterator<Locale> itr = candidates.iterator();
                    while (itr.hasNext()) {
                        if (adapter.isSupportedProviderLocale(itr.next(), langtags)) continue;
                        itr.remove();
                    }
                }
                CANDIDATES_MAP.putIfAbsent(key, candidates);
            }
            return candidates;
        }

        boolean inJavaBaseModule(String baseName, Locale locale) {
            return JAVA_BASE_LOCALES.contains(locale);
        }

        @Override
        public String toBundleName(String baseName, Locale locale) {
            String newBaseName = baseName;
            if (!this.inJavaBaseModule(baseName, locale) && (baseName.startsWith(LocaleProviderAdapter.Type.JRE.getUtilResourcesPackage()) || baseName.startsWith(LocaleProviderAdapter.Type.JRE.getTextResourcesPackage()))) {
                assert (LocaleProviderAdapter.Type.JRE.getUtilResourcesPackage().length() == LocaleProviderAdapter.Type.JRE.getTextResourcesPackage().length());
                int index = LocaleProviderAdapter.Type.JRE.getUtilResourcesPackage().length();
                if (baseName.indexOf(LocaleData.DOTCLDR, index) > 0) {
                    index += LocaleData.DOTCLDR.length();
                }
                newBaseName = baseName.substring(0, index + 1) + "ext" + baseName.substring(index);
            }
            return defaultControl.toBundleName(newBaseName, locale);
        }

        @Override
        public Class<? extends ResourceBundleProvider> getResourceBundleProviderType(String baseName, Locale locale) {
            return this.inJavaBaseModule(baseName, locale) ? null : CommonResourceBundleProvider.class;
        }
    }

    public static abstract class SupplementaryResourceBundleProvider
    extends LocaleDataResourceBundleProvider {
    }

    public static abstract class CommonResourceBundleProvider
    extends LocaleDataResourceBundleProvider {
    }

    private static abstract class LocaleDataResourceBundleProvider
    implements ResourceBundleProvider {
        private LocaleDataResourceBundleProvider() {
        }

        protected String toBundleName(String baseName, Locale locale) {
            return LocaleDataStrategy.INSTANCE.toBundleName(baseName, locale);
        }

        protected String toOtherBundleName(String baseName, String bundleName, Locale locale) {
            return Bundles.toOtherBundleName(baseName, bundleName, locale);
        }
    }
}

