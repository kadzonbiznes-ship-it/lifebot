/*
 * Decompiled with CFR 0.152.
 */
package sun.util.locale.provider;

import java.text.DecimalFormatSymbols;
import java.text.spi.DecimalFormatSymbolsProvider;
import java.util.Locale;
import java.util.Set;
import sun.util.locale.provider.AvailableLanguageTags;
import sun.util.locale.provider.LocaleProviderAdapter;

public class DecimalFormatSymbolsProviderImpl
extends DecimalFormatSymbolsProvider
implements AvailableLanguageTags {
    private final LocaleProviderAdapter.Type type;
    private final Set<String> langtags;

    public DecimalFormatSymbolsProviderImpl(LocaleProviderAdapter.Type type, Set<String> langtags) {
        this.type = type;
        this.langtags = langtags;
    }

    @Override
    public Locale[] getAvailableLocales() {
        return LocaleProviderAdapter.toLocaleArray(this.langtags);
    }

    @Override
    public boolean isSupportedLocale(Locale locale) {
        return LocaleProviderAdapter.forType(this.type).isSupportedProviderLocale(locale, this.langtags);
    }

    @Override
    public DecimalFormatSymbols getInstance(Locale locale) {
        if (locale == null) {
            throw new NullPointerException();
        }
        return new DecimalFormatSymbols(locale);
    }

    @Override
    public Set<String> getAvailableLanguageTags() {
        return this.langtags;
    }
}

