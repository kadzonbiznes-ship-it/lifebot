/*
 * Decompiled with CFR 0.152.
 */
package sun.util.cldr;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.text.spi.BreakIteratorProvider;
import java.text.spi.CollatorProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.spi.CalendarDataProvider;
import java.util.spi.CalendarNameProvider;
import java.util.spi.TimeZoneNameProvider;
import sun.util.cldr.CLDRBaseLocaleDataMetaInfo;
import sun.util.cldr.CLDRCalendarDataProviderImpl;
import sun.util.cldr.CLDRCalendarNameProviderImpl;
import sun.util.cldr.CLDRTimeZoneNameProviderImpl;
import sun.util.locale.provider.JRELocaleProviderAdapter;
import sun.util.locale.provider.LocaleDataMetaInfo;
import sun.util.locale.provider.LocaleProviderAdapter;

public class CLDRLocaleProviderAdapter
extends JRELocaleProviderAdapter {
    private static final CLDRBaseLocaleDataMetaInfo baseMetaInfo = new CLDRBaseLocaleDataMetaInfo();
    private final LocaleDataMetaInfo nonBaseMetaInfo;
    private static volatile Map<Locale, Locale> parentLocalesMap = new ConcurrentHashMap<Locale, Locale>();
    private static final Map<Locale, Locale> langAliasesCache = new ConcurrentHashMap<Locale, Locale>();
    private static volatile Locale[] AVAILABLE_LOCALES;
    private static final Locale NB;
    private static final Locale NO;

    public CLDRLocaleProviderAdapter() {
        LocaleDataMetaInfo nbmi;
        try {
            nbmi = AccessController.doPrivileged(() -> {
                for (LocaleDataMetaInfo ldmi : ServiceLoader.loadInstalled(LocaleDataMetaInfo.class)) {
                    if (ldmi.getType() != LocaleProviderAdapter.Type.CLDR) continue;
                    return ldmi;
                }
                return null;
            });
        }
        catch (PrivilegedActionException pae) {
            throw new InternalError(pae.getCause());
        }
        this.nonBaseMetaInfo = nbmi;
    }

    @Override
    public LocaleProviderAdapter.Type getAdapterType() {
        return LocaleProviderAdapter.Type.CLDR;
    }

    @Override
    public BreakIteratorProvider getBreakIteratorProvider() {
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public CalendarDataProvider getCalendarDataProvider() {
        if (this.calendarDataProvider == null) {
            CalendarDataProvider provider = AccessController.doPrivileged(() -> new CLDRCalendarDataProviderImpl(this.getAdapterType(), this.getLanguageTagSet("CalendarData")));
            CLDRLocaleProviderAdapter cLDRLocaleProviderAdapter = this;
            synchronized (cLDRLocaleProviderAdapter) {
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
            CalendarNameProvider provider = AccessController.doPrivileged(() -> new CLDRCalendarNameProviderImpl(this.getAdapterType(), this.getLanguageTagSet("FormatData")));
            CLDRLocaleProviderAdapter cLDRLocaleProviderAdapter = this;
            synchronized (cLDRLocaleProviderAdapter) {
                if (this.calendarNameProvider == null) {
                    this.calendarNameProvider = provider;
                }
            }
        }
        return this.calendarNameProvider;
    }

    @Override
    public CollatorProvider getCollatorProvider() {
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public TimeZoneNameProvider getTimeZoneNameProvider() {
        if (this.timeZoneNameProvider == null) {
            TimeZoneNameProvider provider = AccessController.doPrivileged(() -> new CLDRTimeZoneNameProviderImpl(this.getAdapterType(), this.getLanguageTagSet("TimeZoneNames")));
            CLDRLocaleProviderAdapter cLDRLocaleProviderAdapter = this;
            synchronized (cLDRLocaleProviderAdapter) {
                if (this.timeZoneNameProvider == null) {
                    this.timeZoneNameProvider = provider;
                }
            }
        }
        return this.timeZoneNameProvider;
    }

    @Override
    public Locale[] getAvailableLocales() {
        if (AVAILABLE_LOCALES == null) {
            AVAILABLE_LOCALES = (Locale[])this.createLanguageTagSet("AvailableLocales").stream().map(Locale::forLanguageTag).toArray(Locale[]::new);
        }
        return AVAILABLE_LOCALES;
    }

    private static Locale applyAliases(Locale loc) {
        return langAliasesCache.computeIfAbsent(loc, l -> {
            String alias = baseMetaInfo.getLanguageAliasMap().get(l.toLanguageTag());
            return alias != null ? Locale.forLanguageTag(alias) : l;
        });
    }

    @Override
    protected Set<String> createLanguageTagSet(String category) {
        category = "AvailableLocales";
        String supportedLocaleString = baseMetaInfo.availableLanguageTags(category);
        String nonBaseTags = null;
        if (this.nonBaseMetaInfo != null) {
            nonBaseTags = this.nonBaseMetaInfo.availableLanguageTags(category);
        }
        if (nonBaseTags != null) {
            supportedLocaleString = supportedLocaleString != null ? supportedLocaleString + " " + nonBaseTags : nonBaseTags;
        }
        return supportedLocaleString != null ? Set.of(supportedLocaleString.split(" +")) : Collections.emptySet();
    }

    @Override
    public List<Locale> getCandidateLocales(String baseName, Locale locale) {
        List<Locale> candidates = super.getCandidateLocales(baseName, CLDRLocaleProviderAdapter.applyAliases(locale));
        return this.applyParentLocales(baseName, candidates);
    }

    private List<Locale> applyParentLocales(String baseName, List<Locale> candidates) {
        for (int i = 0; i < candidates.size(); ++i) {
            Locale p;
            Locale l = candidates.get(i);
            if (l.equals(Locale.ROOT) || (p = CLDRLocaleProviderAdapter.getParentLocale(l)) == null || candidates.get(i + 1).equals(p)) continue;
            List<Locale> applied = candidates.subList(0, i + 1);
            if (p.equals(NB) || p.equals(NO)) {
                applied.add(NO);
                applied.add(Locale.ROOT);
            } else {
                applied.addAll(this.applyParentLocales(baseName, super.getCandidateLocales(baseName, p)));
            }
            return applied;
        }
        return candidates;
    }

    private static Locale getParentLocale(Locale locale) {
        Locale parent = parentLocalesMap.get(locale);
        if (parent == null) {
            String tag = locale.toLanguageTag();
            for (Map.Entry<Locale, String[]> entry : baseMetaInfo.parentLocales().entrySet()) {
                if (Arrays.binarySearch(entry.getValue(), tag) < 0) continue;
                parent = entry.getKey();
                break;
            }
            if (parent == null) {
                parent = locale;
            }
            parentLocalesMap.putIfAbsent(locale, parent);
        }
        if (locale.equals(parent)) {
            parent = null;
        }
        return parent;
    }

    private static Locale getEquivalentLoc(Locale locale) {
        return switch (locale.toString()) {
            case "no", "no_NO" -> Locale.forLanguageTag("nb");
            default -> CLDRLocaleProviderAdapter.applyAliases(locale);
        };
    }

    @Override
    public boolean isSupportedProviderLocale(Locale locale, Set<String> langtags) {
        return Locale.ROOT.equals(locale) || langtags.contains(locale.stripExtensions().toLanguageTag()) || langtags.contains(CLDRLocaleProviderAdapter.getEquivalentLoc(locale).toLanguageTag());
    }

    public Optional<String> canonicalTZID(String id) {
        return Optional.ofNullable(baseMetaInfo.tzCanonicalIDs().get(id));
    }

    static {
        parentLocalesMap.put(Locale.ROOT, Locale.ROOT);
        parentLocalesMap.put(Locale.ENGLISH, Locale.ENGLISH);
        parentLocalesMap.put(Locale.US, Locale.US);
        NB = Locale.forLanguageTag("nb");
        NO = Locale.forLanguageTag("no");
    }
}

