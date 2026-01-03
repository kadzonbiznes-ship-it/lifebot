/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.internal.stream;

import com.sun.org.apache.xerces.internal.impl.PropertyManager;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import com.sun.org.apache.xerces.internal.util.MessageFormatter;
import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;

public class StaxErrorReporter
extends XMLErrorReporter {
    protected XMLReporter fXMLReporter = null;

    public StaxErrorReporter(PropertyManager propertyManager) {
        this.putMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210", new XMLMessageFormatter());
        this.reset(propertyManager);
    }

    public StaxErrorReporter() {
        this.putMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210", new XMLMessageFormatter());
    }

    public void reset(PropertyManager propertyManager) {
        this.fXMLReporter = (XMLReporter)propertyManager.getProperty("javax.xml.stream.reporter");
    }

    @Override
    public String reportError(XMLLocator location, String domain, String key, Object[] arguments, short severity) throws XNIException {
        String message;
        MessageFormatter messageFormatter = this.getMessageFormatter(domain);
        if (messageFormatter != null) {
            message = messageFormatter.formatMessage(this.fLocale, key, arguments);
        } else {
            int argCount;
            StringBuilder str = new StringBuilder();
            str.append(domain);
            str.append('#');
            str.append(key);
            int n = argCount = arguments != null ? arguments.length : 0;
            if (argCount > 0) {
                str.append('?');
                for (int i = 0; i < argCount; ++i) {
                    str.append(arguments[i]);
                    if (i >= argCount - 1) continue;
                    str.append('&');
                }
            }
            message = str.toString();
        }
        switch (severity) {
            case 0: {
                try {
                    if (this.fXMLReporter == null) break;
                    this.fXMLReporter.report(message, "WARNING", null, this.convertToStaxLocation(location));
                    break;
                }
                catch (XMLStreamException ex) {
                    throw new XNIException(ex);
                }
            }
            case 1: {
                try {
                    if (this.fXMLReporter == null) break;
                    this.fXMLReporter.report(message, "ERROR", null, this.convertToStaxLocation(location));
                    break;
                }
                catch (XMLStreamException ex) {
                    throw new XNIException(ex);
                }
            }
            case 2: {
                if (this.fContinueAfterFatalError) break;
                throw new XNIException(message);
            }
        }
        return message;
    }

    Location convertToStaxLocation(final XMLLocator location) {
        return new Location(){

            @Override
            public int getColumnNumber() {
                return location.getColumnNumber();
            }

            @Override
            public int getLineNumber() {
                return location.getLineNumber();
            }

            @Override
            public String getPublicId() {
                return location.getPublicId();
            }

            @Override
            public String getSystemId() {
                return location.getLiteralSystemId();
            }

            @Override
            public int getCharacterOffset() {
                return location.getCharacterOffset();
            }

            public String getLocationURI() {
                return "";
            }
        };
    }
}

