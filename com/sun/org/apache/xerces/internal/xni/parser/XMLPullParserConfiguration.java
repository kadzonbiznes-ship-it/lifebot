/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.xni.parser;

import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import java.io.IOException;

public interface XMLPullParserConfiguration
extends XMLParserConfiguration {
    public void setInputSource(XMLInputSource var1) throws XMLConfigurationException, IOException;

    public boolean parse(boolean var1) throws XNIException, IOException;

    public void cleanup();
}

