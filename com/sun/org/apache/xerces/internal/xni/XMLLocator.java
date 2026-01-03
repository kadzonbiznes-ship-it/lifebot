/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.xni;

public interface XMLLocator {
    public String getPublicId();

    public String getLiteralSystemId();

    public String getBaseSystemId();

    public String getExpandedSystemId();

    public int getLineNumber();

    public int getColumnNumber();

    public int getCharacterOffset();

    public String getEncoding();

    public String getXMLVersion();
}

