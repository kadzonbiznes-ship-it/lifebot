/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.xni.parser;

import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import java.io.IOException;

public interface XMLDocumentScanner
extends XMLDocumentSource {
    public void setInputSource(XMLInputSource var1) throws IOException;

    public boolean scanDocument(boolean var1) throws IOException, XNIException;

    public int next() throws XNIException, IOException;
}

