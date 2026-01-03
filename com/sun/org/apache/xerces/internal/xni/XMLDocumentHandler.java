/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.xni;

import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;

public interface XMLDocumentHandler {
    public void startDocument(XMLLocator var1, String var2, NamespaceContext var3, Augmentations var4) throws XNIException;

    public void xmlDecl(String var1, String var2, String var3, Augmentations var4) throws XNIException;

    public void doctypeDecl(String var1, String var2, String var3, Augmentations var4) throws XNIException;

    public void comment(XMLString var1, Augmentations var2) throws XNIException;

    public void processingInstruction(String var1, XMLString var2, Augmentations var3) throws XNIException;

    public void startElement(QName var1, XMLAttributes var2, Augmentations var3) throws XNIException;

    public void emptyElement(QName var1, XMLAttributes var2, Augmentations var3) throws XNIException;

    public void startGeneralEntity(String var1, XMLResourceIdentifier var2, String var3, Augmentations var4) throws XNIException;

    public void textDecl(String var1, String var2, Augmentations var3) throws XNIException;

    public void endGeneralEntity(String var1, Augmentations var2) throws XNIException;

    public void characters(XMLString var1, Augmentations var2) throws XNIException;

    public void ignorableWhitespace(XMLString var1, Augmentations var2) throws XNIException;

    public void endElement(QName var1, Augmentations var2) throws XNIException;

    public void startCDATA(Augmentations var1) throws XNIException;

    public void endCDATA(Augmentations var1) throws XNIException;

    public void endDocument(Augmentations var1) throws XNIException;

    public void setDocumentSource(XMLDocumentSource var1);

    public XMLDocumentSource getDocumentSource();
}

