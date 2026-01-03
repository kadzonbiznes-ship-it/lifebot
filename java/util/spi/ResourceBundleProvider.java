/*
 * Decompiled with CFR 0.152.
 */
package java.util.spi;

import java.util.Locale;
import java.util.ResourceBundle;

public interface ResourceBundleProvider {
    public ResourceBundle getBundle(String var1, Locale var2);
}

