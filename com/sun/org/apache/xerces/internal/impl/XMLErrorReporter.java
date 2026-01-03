/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.util.DefaultErrorHandler;
import com.sun.org.apache.xerces.internal.util.ErrorHandlerProxy;
import com.sun.org.apache.xerces.internal.util.MessageFormatter;
import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.xml.sax.ErrorHandler;

public class XMLErrorReporter
implements XMLComponent {
    public static final short SEVERITY_WARNING = 0;
    public static final short SEVERITY_ERROR = 1;
    public static final short SEVERITY_FATAL_ERROR = 2;
    protected static final String CONTINUE_AFTER_FATAL_ERROR = "http://apache.org/xml/features/continue-after-fatal-error";
    protected static final String ERROR_HANDLER = "http://apache.org/xml/properties/internal/error-handler";
    private static final String[] RECOGNIZED_FEATURES = new String[]{"http://apache.org/xml/features/continue-after-fatal-error"};
    private static final Boolean[] FEATURE_DEFAULTS = new Boolean[]{null};
    private static final String[] RECOGNIZED_PROPERTIES = new String[]{"http://apache.org/xml/properties/internal/error-handler"};
    private static final Object[] PROPERTY_DEFAULTS = new Object[]{null};
    protected Locale fLocale;
    protected Map<String, MessageFormatter> fMessageFormatters = new HashMap<String, MessageFormatter>();
    protected XMLErrorHandler fErrorHandler;
    protected XMLLocator fLocator;
    protected boolean fContinueAfterFatalError;
    protected XMLErrorHandler fDefaultErrorHandler;
    private ErrorHandler fSaxProxy = null;

    public void setLocale(Locale locale) {
        this.fLocale = locale;
    }

    public Locale getLocale() {
        return this.fLocale;
    }

    public void setDocumentLocator(XMLLocator locator) {
        this.fLocator = locator;
    }

    public void putMessageFormatter(String domain, MessageFormatter messageFormatter) {
        this.fMessageFormatters.put(domain, messageFormatter);
    }

    public MessageFormatter getMessageFormatter(String domain) {
        return this.fMessageFormatters.get(domain);
    }

    public MessageFormatter removeMessageFormatter(String domain) {
        return this.fMessageFormatters.remove(domain);
    }

    public String reportError(String domain, String key, Object[] arguments, short severity) throws XNIException {
        return this.reportError(this.fLocator, domain, key, arguments, severity);
    }

    public String reportError(String domain, String key, Object[] arguments, short severity, Exception exception) throws XNIException {
        return this.reportError(this.fLocator, domain, key, arguments, severity, exception);
    }

    public String reportError(XMLLocator location, String domain, String key, Object[] arguments, short severity) throws XNIException {
        return this.reportError(location, domain, key, arguments, severity, null);
    }

    public String reportError(XMLLocator location, String domain, String key, Object[] arguments, short severity, Exception exception) throws XNIException {
        String message;
        MessageFormatter messageFormatter = this.getMessageFormatter(domain);
        if (messageFormatter != null) {
            message = messageFormatter.formatMessage(this.fLocale, key, arguments);
        } else {
            int argCount;
            StringBuffer str = new StringBuffer();
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
        XMLParseException parseException = exception != null ? new XMLParseException(location, message, exception) : new XMLParseException(location, message);
        XMLErrorHandler errorHandler = this.fErrorHandler;
        if (errorHandler == null) {
            if (this.fDefaultErrorHandler == null) {
                this.fDefaultErrorHandler = new DefaultErrorHandler();
            }
            errorHandler = this.fDefaultErrorHandler;
        }
        switch (severity) {
            case 0: {
                errorHandler.warning(domain, key, parseException);
                break;
            }
            case 1: {
                errorHandler.error(domain, key, parseException);
                break;
            }
            case 2: {
                errorHandler.fatalError(domain, key, parseException);
                if (this.fContinueAfterFatalError) break;
                throw parseException;
            }
        }
        return message;
    }

    @Override
    public void reset(XMLComponentManager componentManager) throws XNIException {
        this.fContinueAfterFatalError = componentManager.getFeature(CONTINUE_AFTER_FATAL_ERROR, false);
        this.fErrorHandler = (XMLErrorHandler)componentManager.getProperty(ERROR_HANDLER);
    }

    @Override
    public String[] getRecognizedFeatures() {
        return (String[])RECOGNIZED_FEATURES.clone();
    }

    @Override
    public void setFeature(String featureId, boolean state) throws XMLConfigurationException {
        int suffixLength;
        if (featureId.startsWith("http://apache.org/xml/features/") && (suffixLength = featureId.length() - "http://apache.org/xml/features/".length()) == "continue-after-fatal-error".length() && featureId.endsWith("continue-after-fatal-error")) {
            this.fContinueAfterFatalError = state;
        }
    }

    public boolean getFeature(String featureId) throws XMLConfigurationException {
        int suffixLength;
        if (featureId.startsWith("http://apache.org/xml/features/") && (suffixLength = featureId.length() - "http://apache.org/xml/features/".length()) == "continue-after-fatal-error".length() && featureId.endsWith("continue-after-fatal-error")) {
            return this.fContinueAfterFatalError;
        }
        return false;
    }

    @Override
    public String[] getRecognizedProperties() {
        return (String[])RECOGNIZED_PROPERTIES.clone();
    }

    @Override
    public void setProperty(String propertyId, Object value) throws XMLConfigurationException {
        int suffixLength;
        if (propertyId.startsWith("http://apache.org/xml/properties/") && (suffixLength = propertyId.length() - "http://apache.org/xml/properties/".length()) == "internal/error-handler".length() && propertyId.endsWith("internal/error-handler")) {
            this.fErrorHandler = (XMLErrorHandler)value;
        }
    }

    @Override
    public Boolean getFeatureDefault(String featureId) {
        for (int i = 0; i < RECOGNIZED_FEATURES.length; ++i) {
            if (!RECOGNIZED_FEATURES[i].equals(featureId)) continue;
            return FEATURE_DEFAULTS[i];
        }
        return null;
    }

    @Override
    public Object getPropertyDefault(String propertyId) {
        for (int i = 0; i < RECOGNIZED_PROPERTIES.length; ++i) {
            if (!RECOGNIZED_PROPERTIES[i].equals(propertyId)) continue;
            return PROPERTY_DEFAULTS[i];
        }
        return null;
    }

    public XMLErrorHandler getErrorHandler() {
        return this.fErrorHandler;
    }

    public ErrorHandler getSAXErrorHandler() {
        if (this.fSaxProxy == null) {
            this.fSaxProxy = new ErrorHandlerProxy(){

                @Override
                protected XMLErrorHandler getErrorHandler() {
                    return XMLErrorReporter.this.fErrorHandler;
                }
            };
        }
        return this.fSaxProxy;
    }
}

