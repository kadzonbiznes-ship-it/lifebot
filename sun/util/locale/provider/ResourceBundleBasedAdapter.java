/*
 * Decompiled with CFR 0.152.
 */
package sun.util.locale.provider;

import java.util.List;
import java.util.Locale;
import sun.util.resources.LocaleData;

public interface ResourceBundleBasedAdapter {
    public LocaleData getLocaleData();

    public List<Locale> getCandidateLocales(String var1, Locale var2);
}

