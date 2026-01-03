/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl.dv;

import java.util.Locale;

public interface ValidationContext {
    public boolean needFacetChecking();

    public boolean needExtraChecking();

    public boolean needToNormalize();

    public boolean useNamespaces();

    public boolean isEntityDeclared(String var1);

    public boolean isEntityUnparsed(String var1);

    public boolean isIdDeclared(String var1);

    public void addId(String var1);

    public void addIdRef(String var1);

    public String getSymbol(String var1);

    public String getURI(String var1);

    public Locale getLocale();
}

