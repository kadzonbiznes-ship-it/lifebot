/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.xni;

import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelSource;

public interface XMLDTDContentModelHandler {
    public static final short SEPARATOR_CHOICE = 0;
    public static final short SEPARATOR_SEQUENCE = 1;
    public static final short OCCURS_ZERO_OR_ONE = 2;
    public static final short OCCURS_ZERO_OR_MORE = 3;
    public static final short OCCURS_ONE_OR_MORE = 4;

    public void startContentModel(String var1, Augmentations var2) throws XNIException;

    public void any(Augmentations var1) throws XNIException;

    public void empty(Augmentations var1) throws XNIException;

    public void startGroup(Augmentations var1) throws XNIException;

    public void pcdata(Augmentations var1) throws XNIException;

    public void element(String var1, Augmentations var2) throws XNIException;

    public void separator(short var1, Augmentations var2) throws XNIException;

    public void occurrence(short var1, Augmentations var2) throws XNIException;

    public void endGroup(Augmentations var1) throws XNIException;

    public void endContentModel(Augmentations var1) throws XNIException;

    public void setDTDContentModelSource(XMLDTDContentModelSource var1);

    public XMLDTDContentModelSource getDTDContentModelSource();
}

