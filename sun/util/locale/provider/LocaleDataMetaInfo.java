/*
 * Decompiled with CFR 0.152.
 */
package sun.util.locale.provider;

import java.util.Map;
import sun.util.locale.provider.LocaleProviderAdapter;

public interface LocaleDataMetaInfo {
    public LocaleProviderAdapter.Type getType();

    public String availableLanguageTags(String var1);

    default public Map<String, String> tzCanonicalIDs() {
        return null;
    }

    default public Map<String, String> getLanguageAliasMap() {
        return null;
    }
}

