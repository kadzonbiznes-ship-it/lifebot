/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.xni;

public interface XMLResourceIdentifier {
    public void setPublicId(String var1);

    public String getPublicId();

    public void setExpandedSystemId(String var1);

    public String getExpandedSystemId();

    public void setLiteralSystemId(String var1);

    public String getLiteralSystemId();

    public void setBaseSystemId(String var1);

    public String getBaseSystemId();

    public void setNamespace(String var1);

    public String getNamespace();
}

