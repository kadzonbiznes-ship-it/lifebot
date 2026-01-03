/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.access;

import java.util.Locale;
import java.util.ResourceBundle;

public interface JavaUtilResourceBundleAccess {
    public void setParent(ResourceBundle var1, ResourceBundle var2);

    public ResourceBundle getParent(ResourceBundle var1);

    public void setLocale(ResourceBundle var1, Locale var2);

    public void setName(ResourceBundle var1, String var2);

    public ResourceBundle getBundle(String var1, Locale var2, Module var3);

    public ResourceBundle newResourceBundle(Class<? extends ResourceBundle> var1);
}

