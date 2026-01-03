/*
 * Decompiled with CFR 0.152.
 */
package sun.util.resources.provider;

import java.util.Locale;
import java.util.ResourceBundle;
import sun.util.resources.LocaleData;

public class LocaleDataProvider
extends LocaleData.CommonResourceBundleProvider {
    @Override
    public ResourceBundle getBundle(String baseName, Locale locale) {
        String otherBundleName;
        String bundleName = this.toBundleName(baseName, locale);
        ResourceBundle rb = LocaleDataProvider.loadResourceBundle(bundleName);
        if (rb == null && !bundleName.equals(otherBundleName = this.toOtherBundleName(baseName, bundleName, locale))) {
            rb = LocaleDataProvider.loadResourceBundle(otherBundleName);
        }
        return rb;
    }

    static ResourceBundle loadResourceBundle(String bundleName) {
        Class<?> c = Class.forName(LocaleDataProvider.class.getModule(), bundleName);
        if (c != null && ResourceBundle.class.isAssignableFrom(c)) {
            try {
                ResourceBundle rb = (ResourceBundle)c.newInstance();
                return rb;
            }
            catch (IllegalAccessException | InstantiationException e) {
                throw new InternalError(e);
            }
        }
        return null;
    }
}

