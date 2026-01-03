/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.xni;

import java.util.Enumeration;

public interface NamespaceContext {
    public static final String XML_URI = "http://www.w3.org/XML/1998/namespace".intern();
    public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/".intern();

    public void pushContext();

    public void popContext();

    public boolean declarePrefix(String var1, String var2);

    public String getURI(String var1);

    public String getPrefix(String var1);

    public int getDeclaredPrefixCount();

    public String getDeclaredPrefixAt(int var1);

    public Enumeration<String> getAllPrefixes();

    public void reset();
}

