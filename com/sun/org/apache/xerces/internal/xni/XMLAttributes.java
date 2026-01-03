/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.xni;

import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLString;

public interface XMLAttributes {
    public int addAttribute(QName var1, String var2, String var3);

    public void removeAllAttributes();

    public void removeAttributeAt(int var1);

    public int getLength();

    public int getIndex(String var1);

    public int getIndex(String var1, String var2);

    public void setName(int var1, QName var2);

    public void getName(int var1, QName var2);

    public String getPrefix(int var1);

    public String getURI(int var1);

    public String getLocalName(int var1);

    public String getQName(int var1);

    public QName getQualifiedName(int var1);

    public void setType(int var1, String var2);

    public String getType(int var1);

    public String getType(String var1);

    public String getType(String var1, String var2);

    public void setValue(int var1, String var2);

    public void setValue(int var1, String var2, XMLString var3);

    public String getValue(int var1);

    public String getValue(String var1);

    public String getValue(String var1, String var2);

    public void setNonNormalizedValue(int var1, String var2);

    public String getNonNormalizedValue(int var1);

    public void setSpecified(int var1, boolean var2);

    public boolean isSpecified(int var1);

    public Augmentations getAugmentations(int var1);

    public Augmentations getAugmentations(String var1, String var2);

    public Augmentations getAugmentations(String var1);

    public void setAugmentations(int var1, Augmentations var2);
}

