/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.xni;

import java.util.Enumeration;

public interface Augmentations {
    public Object putItem(String var1, Object var2);

    public Object getItem(String var1);

    public Object removeItem(String var1);

    public Enumeration<Object> keys();

    public void removeAllItems();
}

