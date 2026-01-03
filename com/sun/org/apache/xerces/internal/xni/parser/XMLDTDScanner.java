/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.xni.parser;

import com.sun.org.apache.xerces.internal.utils.XMLLimitAnalyzer;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import java.io.IOException;

public interface XMLDTDScanner
extends XMLDTDSource,
XMLDTDContentModelSource {
    public void setInputSource(XMLInputSource var1) throws IOException;

    public boolean scanDTDInternalSubset(boolean var1, boolean var2, boolean var3) throws IOException, XNIException;

    public boolean scanDTDExternalSubset(boolean var1) throws IOException, XNIException;

    public boolean skipDTD(boolean var1) throws IOException;

    public void setLimitAnalyzer(XMLLimitAnalyzer var1);
}

