/*
 * Decompiled with CFR 0.152.
 */
package sun.util.locale.provider;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.text.spi.BreakIteratorProvider;
import java.text.spi.CollatorProvider;
import java.text.spi.DateFormatProvider;
import java.text.spi.DateFormatSymbolsProvider;
import java.text.spi.DecimalFormatSymbolsProvider;
import java.text.spi.NumberFormatProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.spi.CalendarDataProvider;
import java.util.spi.CalendarNameProvider;
import java.util.spi.CurrencyNameProvider;
import java.util.spi.LocaleNameProvider;
import java.util.spi.LocaleServiceProvider;
import java.util.spi.TimeZoneNameProvider;
import sun.text.spi.JavaTimeDateTimePatternProvider;
import sun.util.locale.provider.BaseLocaleDataMetaInfo;
import sun.util.locale.provider.BreakIteratorProviderImpl;
import sun.util.locale.provider.CalendarDataProviderImpl;
import sun.util.locale.provider.CalendarNameProviderImpl;
import sun.util.locale.provider.CalendarProviderImpl;
import sun.util.locale.provider.CollatorProviderImpl;
import sun.util.locale.provider.CurrencyNameProviderImpl;
import sun.util.locale.provider.DateFormatProviderImpl;
import sun.util.locale.provider.DateFormatSymbolsProviderImpl;
import sun.util.locale.provider.DecimalFormatSymbolsProviderImpl;
import sun.util.locale.provider.JRELocaleConstants;
import sun.util.locale.provider.JavaTimeDateTimePatternImpl;
import sun.util.locale.provider.LocaleDataMetaInfo;
import sun.util.locale.provider.LocaleNameProviderImpl;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;
import sun.util.locale.provider.NumberFormatProviderImpl;
import sun.util.locale.provider.ResourceBundleBasedAdapter;
import sun.util.locale.provider.TimeZoneNameProviderImpl;
import sun.util.resources.LocaleData;
import sun.util.spi.CalendarProvider;

public class JRELocaleProviderAdapter
extends LocaleProviderAdapter
implements ResourceBundleBasedAdapter {
    private final ConcurrentMap<String, Set<String>> langtagSets = new ConcurrentHashMap<String, Set<String>>();
    private final ConcurrentMap<Locale, LocaleResources> localeResourcesMap = new ConcurrentHashMap<Locale, LocaleResources>();
    private volatile LocaleData localeData;
    private volatile BreakIteratorProvider breakIteratorProvider;
    private volatile CollatorProvider collatorProvider;
    private volatile DateFormatProvider dateFormatProvider;
    private volatile DateFormatSymbolsProvider dateFormatSymbolsProvider;
    private volatile DecimalFormatSymbolsProvider decimalFormatSymbolsProvider;
    private volatile NumberFormatProvider numberFormatProvider;
    private volatile CurrencyNameProvider currencyNameProvider;
    private volatile LocaleNameProvider localeNameProvider;
    protected volatile TimeZoneNameProvider timeZoneNameProvider;
    protected volatile CalendarDataProvider calendarDataProvider;
    protected volatile CalendarNameProvider calendarNameProvider;
    private volatile CalendarProvider calendarProvider;
    private volatile JavaTimeDateTimePatternProvider javaTimeDateTimePatternProvider;

    @Override
    public LocaleProviderAdapter.Type getAdapterType() {
        return LocaleProviderAdapter.Type.JRE;
    }

    @Override
    public <P extends LocaleServiceProvider> P getLocaleServiceProvider(Class<P> c) {
        switch (c.getSimpleName()) {
            case "BreakIteratorProvider": {
                return (P)this.getBreakIteratorProvider();
            }
            case "CollatorProvider": {
                return (P)this.getCollatorProvider();
            }
            case "DateFormatProvider": {
                return (P)this.getDateFormatProvider();
            }
            case "DateFormatSymbolsProvider": {
                return (P)this.getDateFormatSymbolsProvider();
            }
            case "DecimalFormatSymbolsProvider": {
                return (P)this.getDecimalFormatSymbolsProvider();
            }
            case "NumberFormatProvider": {
                return (P)this.getNumberFormatProvider();
            }
            case "CurrencyNameProvider": {
                return (P)this.getCurrencyNameProvider();
            }
            case "LocaleNameProvider": {
                return (P)this.getLocaleNameProvider();
            }
            case "TimeZoneNameProvider": {
                return (P)this.getTimeZoneNameProvider();
            }
            case "CalendarDataProvider": {
                return (P)this.getCalendarDataProvider();
            }
            case "CalendarNameProvider": {
                return (P)this.getCalendarNameProvider();
            }
            case "CalendarProvider": {
                return (P)this.getCalendarProvider();
            }
            case "JavaTimeDateTimePatternProvider": {
                return (P)this.getJavaTimeDateTimePatternProvider();
            }
        }
        throw new InternalError("should not come down here");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public BreakIteratorProvider getBreakIteratorProvider() {
        if (this.breakIteratorProvider == null) {
            BreakIteratorProvider provider = AccessController.doPrivileged(() -> new BreakIteratorProviderImpl(this.getAdapterType(), this.getLanguageTagSet("FormatData")));
            JRELocaleProviderAdapter jRELocaleProviderAdapter = this;
            synchronized (jRELocaleProviderAdapter) {
                if (this.breakIteratorProvider == null) {
                    this.breakIteratorProvider = provider;
                }
            }
        }
        return this.breakIteratorProvider;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public CollatorProvider getCollatorProvider() {
        if (this.collatorProvider == null) {
            CollatorProvider provider = AccessController.doPrivileged(() -> new CollatorProviderImpl(this.getAdapterType(), this.getLanguageTagSet("CollationData")));
            JRELocaleProviderAdapter jRELocaleProviderAdapter = this;
            synchronized (jRELocaleProviderAdapter) {
                if (this.collatorProvider == null) {
                    this.collatorProvider = provider;
                }
            }
        }
        return this.collatorProvider;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public DateFormatProvider getDateFormatProvider() {
        if (this.dateFormatProvider == null) {
            DateFormatProvider provider = AccessController.doPrivileged(() -> new DateFormatProviderImpl(this.getAdapterType(), this.getLanguageTagSet("FormatData")));
            JRELocaleProviderAdapter jRELocaleProviderAdapter = this;
            synchronized (jRELocaleProviderAdapter) {
                if (this.dateFormatProvider == null) {
                    this.dateFormatProvider = provider;
                }
            }
        }
        return this.dateFormatProvider;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public DateFormatSymbolsProvider getDateFormatSymbolsProvider() {
        if (this.dateFormatSymbolsProvider == null) {
            DateFormatSymbolsProvider provider = AccessController.doPrivileged(() -> new DateFormatSymbolsProviderImpl(this.getAdapterType(), this.getLanguageTagSet("FormatData")));
            JRELocaleProviderAdapter jRELocaleProviderAdapter = this;
            synchronized (jRELocaleProviderAdapter) {
                if (this.dateFormatSymbolsProvider == null) {
                    this.dateFormatSymbolsProvider = provider;
                }
            }
        }
        return this.dateFormatSymbolsProvider;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public DecimalFormatSymbolsProvider getDecimalFormatSymbolsProvider() {
        if (this.decimalFormatSymbolsProvider == null) {
            DecimalFormatSymbolsProvider provider = AccessController.doPrivileged(() -> new DecimalFormatSymbolsProviderImpl(this.getAdapterType(), this.getLanguageTagSet("FormatData")));
            JRELocaleProviderAdapter jRELocaleProviderAdapter = this;
            synchronized (jRELocaleProviderAdapter) {
                if (this.decimalFormatSymbolsProvider == null) {
                    this.decimalFormatSymbolsProvider = provider;
                }
            }
        }
        return this.decimalFormatSymbolsProvider;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public NumberFormatProvider getNumberFormatProvider() {
        if (this.numberFormatProvider == null) {
            NumberFormatProvider provider = AccessController.doPrivileged(() -> new NumberFormatProviderImpl(this.getAdapterType(), this.getLanguageTagSet("FormatData")));
            JRELocaleProviderAdapter jRELocaleProviderAdapter = this;
            synchronized (jRELocaleProviderAdapter) {
                if (this.numberFormatProvider == null) {
                    this.numberFormatProvider = provider;
                }
            }
        }
        return this.numberFormatProvider;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public CurrencyNameProvider getCurrencyNameProvider() {
        if (this.currencyNameProvider == null) {
            CurrencyNameProvider provider = AccessController.doPrivileged(() -> new CurrencyNameProviderImpl(this.getAdapterType(), this.getLanguageTagSet("CurrencyNames")));
            JRELocaleProviderAdapter jRELocaleProviderAdapter = this;
            synchronized (jRELocaleProviderAdapter) {
                if (this.currencyNameProvider == null) {
                    this.currencyNameProvider = provider;
                }
            }
        }
        return this.currencyNameProvider;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public LocaleNameProvider getLocaleNameProvider() {
        if (this.localeNameProvider == null) {
            LocaleNameProvider provider = AccessController.doPrivileged(() -> new LocaleNameProviderImpl(this.getAdapterType(), this.getLanguageTagSet("LocaleNames")));
            JRELocaleProviderAdapter jRELocaleProviderAdapter = this;
            synchronized (jRELocaleProviderAdapter) {
                if (this.localeNameProvider == null) {
                    this.localeNameProvider = provider;
                }
            }
        }
        return this.localeNameProvider;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public TimeZoneNameProvider getTimeZoneNameProvider() {
        if (this.timeZoneNameProvider == null) {
            TimeZoneNameProvider provider = AccessController.doPrivileged(() -> new TimeZoneNameProviderImpl(this.getAdapterType(), this.getLanguageTagSet("TimeZoneNames")));
            JRELocaleProviderAdapter jRELocaleProviderAdapter = this;
            synchronized (jRELocaleProviderAdapter) {
                if (this.timeZoneNameProvider == null) {
                    this.timeZoneNameProvider = provider;
                }
            }
        }
        return this.timeZoneNameProvider;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public CalendarDataProvider getCalendarDataProvider() {
        if (this.calendarDataProvider == null) {
            CalendarDataProvider provider = AccessController.doPrivileged(() -> new CalendarDataProviderImpl(this.getAdapterType(), this.getLanguageTagSet("CalendarData")));
            JRELocaleProviderAdapter jRELocaleProviderAdapter = this;
            synchronized (jRELocaleProviderAdapter) {
                if (this.calendarDataProvider == null) {
                    this.calendarDataProvider = provider;
                }
            }
        }
        return this.calendarDataProvider;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public CalendarNameProvider getCalendarNameProvider() {
        if (this.calendarNameProvider == null) {
            CalendarNameProvider provider = AccessController.doPrivileged(() -> new CalendarNameProviderImpl(this.getAdapterType(), this.getLanguageTagSet("FormatData")));
            JRELocaleProviderAdapter jRELocaleProviderAdapter = this;
            synchronized (jRELocaleProviderAdapter) {
                if (this.calendarNameProvider == null) {
                    this.calendarNameProvider = provider;
                }
            }
        }
        return this.calendarNameProvider;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public CalendarProvider getCalendarProvider() {
        if (this.calendarProvider == null) {
            CalendarProvider provider = AccessController.doPrivileged(() -> new CalendarProviderImpl(this.getAdapterType(), this.getLanguageTagSet("CalendarData")));
            JRELocaleProviderAdapter jRELocaleProviderAdapter = this;
            synchronized (jRELocaleProviderAdapter) {
                if (this.calendarProvider == null) {
                    this.calendarProvider = provider;
                }
            }
        }
        return this.calendarProvider;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public JavaTimeDateTimePatternProvider getJavaTimeDateTimePatternProvider() {
        if (this.javaTimeDateTimePatternProvider == null) {
            JavaTimeDateTimePatternProvider provider = AccessController.doPrivileged(() -> new JavaTimeDateTimePatternImpl(this.getAdapterType(), this.getLanguageTagSet("FormatData")));
            JRELocaleProviderAdapter jRELocaleProviderAdapter = this;
            synchronized (jRELocaleProviderAdapter) {
                if (this.javaTimeDateTimePatternProvider == null) {
                    this.javaTimeDateTimePatternProvider = provider;
                }
            }
        }
        return this.javaTimeDateTimePatternProvider;
    }

    @Override
    public LocaleResources getLocaleResources(Locale locale) {
        LocaleResources lrc;
        LocaleResources lr = (LocaleResources)this.localeResourcesMap.get(locale);
        if (lr == null && (lrc = this.localeResourcesMap.putIfAbsent(locale, lr = new LocaleResources(this, locale))) != null) {
            lr = lrc;
        }
        return lr;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public LocaleData getLocaleData() {
        if (this.localeData == null) {
            JRELocaleProviderAdapter jRELocaleProviderAdapter = this;
            synchronized (jRELocaleProviderAdapter) {
                if (this.localeData == null) {
                    this.localeData = new LocaleData(this.getAdapterType());
                }
            }
        }
        return this.localeData;
    }

    @Override
    public List<Locale> getCandidateLocales(String baseName, Locale locale) {
        return ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT).getCandidateLocales(baseName, locale);
    }

    @Override
    public Locale[] getAvailableLocales() {
        return AvailableJRELocales.localeList;
    }

    public Set<String> getLanguageTagSet(String category) {
        Set<String> ts;
        Set<String> tagset = (Set<String>)this.langtagSets.get(category);
        if (tagset == null && (ts = this.langtagSets.putIfAbsent(category, tagset = this.createLanguageTagSet(category))) != null) {
            tagset = ts;
        }
        return tagset;
    }

    protected Set<String> createLanguageTagSet(String category) {
        String supportedLocaleString = JRELocaleProviderAdapter.createSupportedLocaleString(category);
        return supportedLocaleString != null ? Set.of(supportedLocaleString.split(" +")) : Collections.emptySet();
    }

    private static String createSupportedLocaleString(String category) {
        String supportedLocaleString = BaseLocaleDataMetaInfo.getSupportedLocaleString(category);
        try {
            String nonBaseTags = AccessController.doPrivileged(() -> {
                StringBuilder tags = new StringBuilder();
                for (LocaleDataMetaInfo ldmi : ServiceLoader.loadInstalled(LocaleDataMetaInfo.class)) {
                    String t;
                    if (ldmi.getType() != LocaleProviderAdapter.Type.JRE || (t = ldmi.availableLanguageTags(category)) == null) continue;
                    if (tags.length() > 0) {
                        tags.append(' ');
                    }
                    tags.append(t);
                }
                return tags.toString();
            });
            if (nonBaseTags != null) {
                supportedLocaleString = supportedLocaleString + " " + nonBaseTags;
            }
        }
        catch (PrivilegedActionException pae) {
            throw new InternalError(pae.getCause());
        }
        return supportedLocaleString;
    }

    private static Locale[] createAvailableLocales() {
        String supportedLocaleString = JRELocaleProviderAdapter.createSupportedLocaleString("AvailableLocales");
        if (supportedLocaleString.isEmpty()) {
            throw new InternalError("No available locales for JRE");
        }
        return (Locale[])Arrays.stream(supportedLocaleString.split(" +")).map(t -> switch (t) {
            case "ja-JP-JP" -> JRELocaleConstants.JA_JP_JP;
            case "no-NO-NY" -> JRELocaleConstants.NO_NO_NY;
            case "th-TH-TH" -> JRELocaleConstants.TH_TH_TH;
            default -> Locale.forLanguageTag(t);
        }).distinct().toArray(Locale[]::new);
    }

    @Override
    public boolean isSupportedProviderLocale(Locale locale, Set<String> langtags) {
        if (Locale.ROOT.equals(locale)) {
            return true;
        }
        if (langtags.contains((locale = locale.stripExtensions()).toLanguageTag())) {
            return true;
        }
        String oldname = locale.toString().replace('_', '-');
        return langtags.contains(oldname) || "ja-JP-JP".equals(oldname) || "th-TH-TH".equals(oldname) || "no-NO-NY".equals(oldname);
    }

    private static class AvailableJRELocales {
        private static final Locale[] localeList = JRELocaleProviderAdapter.createAvailableLocales();

        private AvailableJRELocales() {
        }
    }
}

