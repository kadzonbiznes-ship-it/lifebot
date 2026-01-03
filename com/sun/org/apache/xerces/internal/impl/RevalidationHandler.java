/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter;

public interface RevalidationHandler
extends XMLDocumentFilter {
    public boolean characterData(String var1, Augmentations var2);
}

