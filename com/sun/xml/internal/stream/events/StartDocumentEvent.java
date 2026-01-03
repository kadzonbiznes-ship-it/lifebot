/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.internal.stream.events;

import com.sun.xml.internal.stream.events.DummyEvent;
import java.io.IOException;
import java.io.Writer;
import javax.xml.stream.Location;
import javax.xml.stream.events.StartDocument;

public class StartDocumentEvent
extends DummyEvent
implements StartDocument {
    protected String fSystemId;
    protected String fEncodingScheam;
    protected boolean fStandalone;
    protected String fVersion;
    private boolean fEncodingSchemeSet = false;
    private boolean fStandaloneSet = false;
    private boolean nestedCall = false;

    public StartDocumentEvent() {
        this.init("UTF-8", "1.0", true, null);
    }

    public StartDocumentEvent(String encoding) {
        this.init(encoding, "1.0", true, null);
    }

    public StartDocumentEvent(String encoding, String version) {
        this.init(encoding, version, true, null);
    }

    public StartDocumentEvent(String encoding, String version, boolean standalone) {
        this.fStandaloneSet = true;
        this.init(encoding, version, standalone, null);
    }

    public StartDocumentEvent(String encoding, String version, boolean standalone, Location loc) {
        this.fStandaloneSet = true;
        this.init(encoding, version, standalone, loc);
    }

    protected void init(String encoding, String version, boolean standalone, Location loc) {
        this.setEventType(7);
        this.fEncodingScheam = encoding;
        this.fVersion = version;
        this.fStandalone = standalone;
        if (encoding != null && !encoding.isEmpty()) {
            this.fEncodingSchemeSet = true;
        } else {
            this.fEncodingSchemeSet = false;
            this.fEncodingScheam = "UTF-8";
        }
        this.fLocation = loc;
    }

    @Override
    public String getSystemId() {
        if (this.fLocation == null) {
            return "";
        }
        return this.fLocation.getSystemId();
    }

    @Override
    public String getCharacterEncodingScheme() {
        return this.fEncodingScheam;
    }

    @Override
    public boolean isStandalone() {
        return this.fStandalone;
    }

    @Override
    public String getVersion() {
        return this.fVersion;
    }

    public void setStandalone(boolean isStandalone, boolean standaloneSet) {
        this.fStandaloneSet = standaloneSet;
        this.fStandalone = isStandalone;
    }

    public void setStandalone(String s) {
        this.fStandaloneSet = true;
        if (s == null) {
            this.fStandalone = true;
            return;
        }
        this.fStandalone = s.equals("yes");
    }

    @Override
    public boolean encodingSet() {
        return this.fEncodingSchemeSet;
    }

    @Override
    public boolean standaloneSet() {
        return this.fStandaloneSet;
    }

    public void setEncoding(String encoding) {
        this.fEncodingScheam = encoding;
    }

    void setDeclaredEncoding(boolean value) {
        this.fEncodingSchemeSet = value;
    }

    public void setVersion(String s) {
        this.fVersion = s;
    }

    void clear() {
        this.fEncodingScheam = "UTF-8";
        this.fStandalone = true;
        this.fVersion = "1.0";
        this.fEncodingSchemeSet = false;
        this.fStandaloneSet = false;
    }

    public String toString() {
        String s = "<?xml version=\"" + this.fVersion + "\"";
        s = s + " encoding='" + this.fEncodingScheam + "'";
        s = this.fStandaloneSet ? (this.fStandalone ? s + " standalone='yes'?>" : s + " standalone='no'?>") : s + "?>";
        return s;
    }

    @Override
    public boolean isStartDocument() {
        return true;
    }

    @Override
    protected void writeAsEncodedUnicodeEx(Writer writer) throws IOException {
        writer.write(this.toString());
    }
}

