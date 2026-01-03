/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.xni.parser;

import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import java.io.InputStream;
import java.io.Reader;
import org.xml.sax.InputSource;

public class XMLInputSource {
    protected String fPublicId;
    protected String fSystemId;
    protected String fBaseSystemId;
    protected InputStream fByteStream;
    protected Reader fCharStream;
    protected String fEncoding;
    boolean fIsCreatedByResolver = false;

    public XMLInputSource(String publicId, String systemId, String baseSystemId, boolean isCreatedByResolver) {
        this.fPublicId = publicId;
        this.fSystemId = systemId;
        this.fBaseSystemId = baseSystemId;
        this.fIsCreatedByResolver = isCreatedByResolver;
    }

    public XMLInputSource(XMLResourceIdentifier resourceIdentifier) {
        this.fPublicId = resourceIdentifier.getPublicId();
        this.fSystemId = resourceIdentifier.getLiteralSystemId();
        this.fBaseSystemId = resourceIdentifier.getBaseSystemId();
    }

    public XMLInputSource(InputSource inputSource, boolean isCreatedByResolver) {
        this.fPublicId = inputSource.getPublicId();
        this.fSystemId = inputSource.getSystemId();
        this.fByteStream = inputSource.getByteStream();
        this.fCharStream = inputSource.getCharacterStream();
        this.fEncoding = inputSource.getEncoding();
        this.fIsCreatedByResolver = isCreatedByResolver;
    }

    public XMLInputSource(String publicId, String systemId, String baseSystemId, InputStream byteStream, String encoding) {
        this.fPublicId = publicId;
        this.fSystemId = systemId;
        this.fBaseSystemId = baseSystemId;
        this.fByteStream = byteStream;
        this.fEncoding = encoding;
    }

    public XMLInputSource(String publicId, String systemId, String baseSystemId, Reader charStream, String encoding) {
        this.fPublicId = publicId;
        this.fSystemId = systemId;
        this.fBaseSystemId = baseSystemId;
        this.fCharStream = charStream;
        this.fEncoding = encoding;
    }

    public void setPublicId(String publicId) {
        this.fPublicId = publicId;
    }

    public String getPublicId() {
        return this.fPublicId;
    }

    public void setSystemId(String systemId) {
        this.fSystemId = systemId;
    }

    public String getSystemId() {
        return this.fSystemId;
    }

    public void setBaseSystemId(String baseSystemId) {
        this.fBaseSystemId = baseSystemId;
    }

    public String getBaseSystemId() {
        return this.fBaseSystemId;
    }

    public void setByteStream(InputStream byteStream) {
        this.fByteStream = byteStream;
    }

    public InputStream getByteStream() {
        return this.fByteStream;
    }

    public void setCharacterStream(Reader charStream) {
        this.fCharStream = charStream;
    }

    public Reader getCharacterStream() {
        return this.fCharStream;
    }

    public void setEncoding(String encoding) {
        this.fEncoding = encoding;
    }

    public String getEncoding() {
        return this.fEncoding;
    }

    public void setCreatedByResolver(boolean createdByResolver) {
        this.fIsCreatedByResolver = createdByResolver;
    }

    public boolean isCreatedByResolver() {
        return this.fIsCreatedByResolver;
    }
}

