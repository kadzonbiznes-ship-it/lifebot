/*
 * Decompiled with CFR 0.152.
 */
package sun.util.locale.provider;

import java.util.HashMap;
import java.util.Map;
import sun.util.locale.provider.LocaleDataMetaInfo;
import sun.util.locale.provider.LocaleProviderAdapter;

public class BaseLocaleDataMetaInfo
implements LocaleDataMetaInfo {
    private static final Map<String, String> resourceNameToLocales = new HashMap<String, String>(9);

    public static String getSupportedLocaleString(String resourceName) {
        return resourceNameToLocales.getOrDefault(resourceName, "");
    }

    @Override
    public LocaleProviderAdapter.Type getType() {
        return LocaleProviderAdapter.Type.JRE;
    }

    @Override
    public String availableLanguageTags(String category) {
        return BaseLocaleDataMetaInfo.getSupportedLocaleString(category);
    }

    static {
        resourceNameToLocales.put("FormatData", "  en en-US ");
        resourceNameToLocales.put("CollationData", "  ");
        resourceNameToLocales.put("BreakIteratorInfo", "  ");
        resourceNameToLocales.put("BreakIteratorRules", "  ");
        resourceNameToLocales.put("TimeZoneNames", "  en ");
        resourceNameToLocales.put("LocaleNames", "  en ");
        resourceNameToLocales.put("CurrencyNames", "  en-US ");
        resourceNameToLocales.put("CalendarData", "  en ");
        resourceNameToLocales.put("AvailableLocales", " en en-US ");
    }
}

