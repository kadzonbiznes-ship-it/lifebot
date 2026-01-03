/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.xni.parser;

import com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import java.io.IOException;
import java.util.Locale;

public interface XMLParserConfiguration
extends XMLComponentManager {
    public void parse(XMLInputSource var1) throws XNIException, IOException;

    public void addRecognizedFeatures(String[] var1);

    public void setFeature(String var1, boolean var2) throws XMLConfigurationException;

    @Override
    public boolean getFeature(String var1) throws XMLConfigurationException;

    public void addRecognizedProperties(String[] var1);

    public void setProperty(String var1, Object var2) throws XMLConfigurationException;

    @Override
    public Object getProperty(String var1) throws XMLConfigurationException;

    public void setErrorHandler(XMLErrorHandler var1);

    public XMLErrorHandler getErrorHandler();

    public void setDocumentHandler(XMLDocumentHandler var1);

    public XMLDocumentHandler getDocumentHandler();

    public void setDTDHandler(XMLDTDHandler var1);

    public XMLDTDHandler getDTDHandler();

    public void setDTDContentModelHandler(XMLDTDContentModelHandler var1);

    public XMLDTDContentModelHandler getDTDContentModelHandler();

    public void setEntityResolver(XMLEntityResolver var1);

    public XMLEntityResolver getEntityResolver();

    public void setLocale(Locale var1) throws XNIException;

    public Locale getLocale();
}

