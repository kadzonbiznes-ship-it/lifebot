/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl.dtd;

import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter;

public interface XMLDTDValidatorFilter
extends XMLDocumentFilter {
    public boolean hasGrammar();

    public boolean validate();
}

