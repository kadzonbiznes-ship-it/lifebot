/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.xni;

import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource;

public interface XMLDTDHandler {
    public static final short CONDITIONAL_INCLUDE = 0;
    public static final short CONDITIONAL_IGNORE = 1;

    public void startDTD(XMLLocator var1, Augmentations var2) throws XNIException;

    public void startParameterEntity(String var1, XMLResourceIdentifier var2, String var3, Augmentations var4) throws XNIException;

    public void textDecl(String var1, String var2, Augmentations var3) throws XNIException;

    public void endParameterEntity(String var1, Augmentations var2) throws XNIException;

    public void startExternalSubset(XMLResourceIdentifier var1, Augmentations var2) throws XNIException;

    public void endExternalSubset(Augmentations var1) throws XNIException;

    public void comment(XMLString var1, Augmentations var2) throws XNIException;

    public void processingInstruction(String var1, XMLString var2, Augmentations var3) throws XNIException;

    public void elementDecl(String var1, String var2, Augmentations var3) throws XNIException;

    public void startAttlist(String var1, Augmentations var2) throws XNIException;

    public void attributeDecl(String var1, String var2, String var3, String[] var4, String var5, XMLString var6, XMLString var7, Augmentations var8) throws XNIException;

    public void endAttlist(Augmentations var1) throws XNIException;

    public void internalEntityDecl(String var1, XMLString var2, XMLString var3, Augmentations var4) throws XNIException;

    public void externalEntityDecl(String var1, XMLResourceIdentifier var2, Augmentations var3) throws XNIException;

    public void unparsedEntityDecl(String var1, XMLResourceIdentifier var2, String var3, Augmentations var4) throws XNIException;

    public void notationDecl(String var1, XMLResourceIdentifier var2, Augmentations var3) throws XNIException;

    public void startConditional(short var1, Augmentations var2) throws XNIException;

    public void ignoredCharacters(XMLString var1, Augmentations var2) throws XNIException;

    public void endConditional(Augmentations var1) throws XNIException;

    public void endDTD(Augmentations var1) throws XNIException;

    public void setDTDSource(XMLDTDSource var1);

    public XMLDTDSource getDTDSource();
}

