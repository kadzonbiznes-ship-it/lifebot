/*
 * Decompiled with CFR 0.152.
 */
package sun.util.locale.provider;

import java.lang.reflect.InvocationTargetException;
import java.text.spi.BreakIteratorProvider;
import java.text.spi.CollatorProvider;
import java.text.spi.DateFormatProvider;
import java.text.spi.DateFormatSymbolsProvider;
import java.text.spi.DecimalFormatSymbolsProvider;
import java.text.spi.NumberFormatProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.spi.CalendarDataProvider;
import java.util.spi.CalendarNameProvider;
import java.util.spi.CurrencyNameProvider;
import java.util.spi.LocaleNameProvider;
import java.util.spi.LocaleServiceProvider;
import java.util.spi.TimeZoneNameProvider;
import sun.security.action.GetPropertyAction;
import sun.text.spi.JavaTimeDateTimePatternProvider;
import sun.util.locale.provider.JRELocaleConstants;
import sun.util.locale.provider.LocaleResources;
import sun.util.spi.CalendarProvider;

public abstract class LocaleProviderAdapter {
    private static final List<Type> adapterPreference;
    private static final Map<Type, LocaleProviderAdapter> adapterInstances;
    static volatile Type defaultLocaleProviderAdapter;
    private static final ConcurrentMap<Class<? extends LocaleServiceProvider>, ConcurrentMap<Locale, LocaleProviderAdapter>> adapterCache;

    public static LocaleProviderAdapter forType(Type type) {
        switch (type.ordinal()) {
            case 0: 
            case 1: 
            case 2: 
            case 3: 
            case 4: {
                LocaleProviderAdapter adapter = adapterInstances.get((Object)type);
                if (adapter == null) {
                    try {
                        adapter = (LocaleProviderAdapter)Class.forName(type.getAdapterClassName()).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                        LocaleProviderAdapter cached = adapterInstances.putIfAbsent(type, adapter);
                        if (cached != null) {
                            adapter = cached;
                        }
                    }
                    catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | UnsupportedOperationException | InvocationTargetException e) {
                        throw new ServiceConfigurationError("Locale provider adapter \"" + (Object)((Object)type) + "\"cannot be instantiated.", e);
                    }
                }
                return adapter;
            }
        }
        throw new InternalError("unknown locale data adapter type");
    }

    public static LocaleProviderAdapter forJRE() {
        return LocaleProviderAdapter.forType(Type.JRE);
    }

    public static LocaleProviderAdapter getResourceBundleBased() {
        for (Type type : LocaleProviderAdapter.getAdapterPreference()) {
            LocaleProviderAdapter adapter;
            if (type != Type.JRE && type != Type.CLDR && type != Type.FALLBACK || (adapter = LocaleProviderAdapter.forType(type)) == null) continue;
            return adapter;
        }
        throw new InternalError();
    }

    public static List<Type> getAdapterPreference() {
        return adapterPreference;
    }

    public static LocaleProviderAdapter getAdapter(Class<? extends LocaleServiceProvider> providerClass, Locale locale) {
        LocaleProviderAdapter adapter;
        ConcurrentHashMap<Locale, LocaleProviderAdapter> adapterMap = (ConcurrentHashMap<Locale, LocaleProviderAdapter>)adapterCache.get(providerClass);
        if (adapterMap != null) {
            adapter = (LocaleProviderAdapter)adapterMap.get(locale);
            if (adapter != null) {
                return adapter;
            }
        } else {
            adapterMap = new ConcurrentHashMap<Locale, LocaleProviderAdapter>();
            adapterCache.putIfAbsent(providerClass, adapterMap);
        }
        if ((adapter = LocaleProviderAdapter.findAdapter(providerClass, locale)) != null) {
            adapterMap.putIfAbsent(locale, adapter);
            return adapter;
        }
        List<Locale> lookupLocales = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT).getCandidateLocales("", locale);
        for (Locale loc : lookupLocales) {
            if (loc.equals(locale) || (adapter = LocaleProviderAdapter.findAdapter(providerClass, loc)) == null) continue;
            adapterMap.putIfAbsent(locale, adapter);
            return adapter;
        }
        adapterMap.putIfAbsent(locale, LocaleProviderAdapter.forType(Type.FALLBACK));
        return LocaleProviderAdapter.forType(Type.FALLBACK);
    }

    private static LocaleProviderAdapter findAdapter(Class<? extends LocaleServiceProvider> providerClass, Locale locale) {
        for (Type type : LocaleProviderAdapter.getAdapterPreference()) {
            LocaleServiceProvider provider;
            LocaleProviderAdapter adapter = LocaleProviderAdapter.forType(type);
            if (adapter == null || (provider = adapter.getLocaleServiceProvider(providerClass)) == null || !provider.isSupportedLocale(locale)) continue;
            return adapter;
        }
        return null;
    }

    public boolean isSupportedProviderLocale(Locale locale, Set<String> langtags) {
        Type type = this.getAdapterType();
        assert (type == Type.JRE || type == Type.CLDR || type == Type.FALLBACK);
        return false;
    }

    public static Locale[] toLocaleArray(Set<String> tags) {
        return (Locale[])tags.stream().map(t -> switch (t) {
            case "ja-JP-JP" -> JRELocaleConstants.JA_JP_JP;
            case "no-NO-NY" -> JRELocaleConstants.NO_NO_NY;
            case "th-TH-TH" -> JRELocaleConstants.TH_TH_TH;
            default -> Locale.forLanguageTag(t);
        }).toArray(Locale[]::new);
    }

    public abstract Type getAdapterType();

    public abstract <P extends LocaleServiceProvider> P getLocaleServiceProvider(Class<P> var1);

    public abstract BreakIteratorProvider getBreakIteratorProvider();

    public abstract CollatorProvider getCollatorProvider();

    public abstract DateFormatProvider getDateFormatProvider();

    public abstract DateFormatSymbolsProvider getDateFormatSymbolsProvider();

    public abstract DecimalFormatSymbolsProvider getDecimalFormatSymbolsProvider();

    public abstract NumberFormatProvider getNumberFormatProvider();

    public abstract CurrencyNameProvider getCurrencyNameProvider();

    public abstract LocaleNameProvider getLocaleNameProvider();

    public abstract TimeZoneNameProvider getTimeZoneNameProvider();

    public abstract CalendarDataProvider getCalendarDataProvider();

    public abstract CalendarNameProvider getCalendarNameProvider();

    public abstract CalendarProvider getCalendarProvider();

    public abstract JavaTimeDateTimePatternProvider getJavaTimeDateTimePatternProvider();

    public abstract LocaleResources getLocaleResources(Locale var1);

    public abstract Locale[] getAvailableLocales();

    static {
        adapterInstances = new ConcurrentHashMap<Type, LocaleProviderAdapter>();
        adapterCache = new ConcurrentHashMap<Class<? extends LocaleServiceProvider>, ConcurrentMap<Locale, LocaleProviderAdapter>>();
        String order = GetPropertyAction.privilegedGetProperty("java.locale.providers");
        ArrayList<Type> typeList = new ArrayList<Type>();
        String invalidTypeMessage = null;
        String compatWarningMessage = null;
        if (order != null && !order.isEmpty()) {
            String[] types;
            for (String type : types = order.split(",")) {
                if ((type = type.trim().toUpperCase(Locale.ROOT)).equals("COMPAT") || type.equals("JRE")) {
                    compatWarningMessage = "COMPAT locale provider will be removed in a future release";
                    type = "JRE";
                }
                try {
                    Type aType = Type.valueOf(type.trim().toUpperCase(Locale.ROOT));
                    if (typeList.contains((Object)aType)) continue;
                    typeList.add(aType);
                }
                catch (IllegalArgumentException e) {
                    invalidTypeMessage = "Invalid locale provider adapter \"" + type + "\" ignored.";
                }
            }
        }
        defaultLocaleProviderAdapter = Type.CLDR;
        if (!typeList.isEmpty()) {
            if (!typeList.contains((Object)Type.CLDR) && !typeList.contains((Object)Type.JRE)) {
                typeList.add(Type.FALLBACK);
                defaultLocaleProviderAdapter = Type.FALLBACK;
            }
        } else {
            typeList.add(Type.CLDR);
            typeList.add(Type.JRE);
        }
        adapterPreference = Collections.unmodifiableList(typeList);
        if (invalidTypeMessage != null) {
            System.getLogger(LocaleProviderAdapter.class.getCanonicalName()).log(System.Logger.Level.INFO, invalidTypeMessage);
        }
        if (compatWarningMessage != null) {
            System.getLogger(LocaleProviderAdapter.class.getCanonicalName()).log(System.Logger.Level.WARNING, compatWarningMessage);
        }
    }

    public static enum Type {
        JRE("sun.util.locale.provider.JRELocaleProviderAdapter", "sun.util.resources", "sun.text.resources"),
        CLDR("sun.util.cldr.CLDRLocaleProviderAdapter", "sun.util.resources.cldr", "sun.text.resources.cldr"),
        SPI("sun.util.locale.provider.SPILocaleProviderAdapter"),
        HOST("sun.util.locale.provider.HostLocaleProviderAdapter"),
        FALLBACK("sun.util.locale.provider.FallbackLocaleProviderAdapter", "sun.util.resources", "sun.text.resources");

        private final String CLASSNAME;
        private final String UTIL_RESOURCES_PACKAGE;
        private final String TEXT_RESOURCES_PACKAGE;

        private Type(String className) {
            this(className, null, null);
        }

        private Type(String className, String util, String text) {
            this.CLASSNAME = className;
            this.UTIL_RESOURCES_PACKAGE = util;
            this.TEXT_RESOURCES_PACKAGE = text;
        }

        public String getAdapterClassName() {
            return this.CLASSNAME;
        }

        public String getUtilResourcesPackage() {
            return this.UTIL_RESOURCES_PACKAGE;
        }

        public String getTextResourcesPackage() {
            return this.TEXT_RESOURCES_PACKAGE;
        }
    }
}

