/*
 * Decompiled with CFR 0.152.
 */
package java.util.spi;

import java.util.Locale;

public abstract class LocaleServiceProvider {
    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("localeServiceProvider"));
        }
        return null;
    }

    private LocaleServiceProvider(Void ignore) {
    }

    protected LocaleServiceProvider() {
        this(LocaleServiceProvider.checkPermission());
    }

    public abstract Locale[] getAvailableLocales();

    public boolean isSupportedLocale(Locale locale) {
        locale = locale.stripExtensions();
        for (Locale available : this.getAvailableLocales()) {
            if (!locale.equals(available.stripExtensions())) continue;
            return true;
        }
        return false;
    }
}

