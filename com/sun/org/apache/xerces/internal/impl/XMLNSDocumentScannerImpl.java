/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.impl.PropertyManager;
import com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl;
import com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl;
import com.sun.org.apache.xerces.internal.impl.XMLScanner;
import com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidatorFilter;
import com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import com.sun.org.apache.xerces.internal.util.XMLSymbols;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import java.io.IOException;

public class XMLNSDocumentScannerImpl
extends XMLDocumentScannerImpl {
    protected boolean fBindNamespaces;
    protected boolean fPerformValidation;
    protected boolean fNotAddNSDeclAsAttribute = false;
    private XMLDTDValidatorFilter fDTDValidator;
    private boolean fXmlnsDeclared = false;

    @Override
    public void reset(PropertyManager propertyManager) {
        this.setPropertyManager(propertyManager);
        super.reset(propertyManager);
        this.fBindNamespaces = false;
        this.fNotAddNSDeclAsAttribute = (Boolean)propertyManager.getProperty("add-namespacedecl-as-attrbiute") == false;
    }

    @Override
    public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
        super.reset(componentManager);
        this.fNotAddNSDeclAsAttribute = false;
        this.fPerformValidation = false;
        this.fBindNamespaces = false;
    }

    @Override
    public int next() throws IOException, XNIException {
        if (this.fScannerLastState == 2 && this.fBindNamespaces) {
            this.fScannerLastState = -1;
            this.fNamespaceContext.popContext();
        }
        this.fScannerLastState = super.next();
        return this.fScannerLastState;
    }

    public void setDTDValidator(XMLDTDValidatorFilter dtd) {
        this.fDTDValidator = dtd;
    }

    @Override
    protected boolean scanStartElement() throws IOException, XNIException {
        Object name;
        if (this.fSkip && !this.fAdd) {
            name = this.fElementStack.getNext();
            this.fSkip = this.fEntityScanner.skipString(((QName)name).rawname);
            if (this.fSkip) {
                this.fElementStack.push();
                this.fElementQName = name;
            } else {
                this.fElementStack.reposition();
            }
        }
        if (!this.fSkip || this.fAdd) {
            this.fElementQName = this.fElementStack.nextElement();
            if (this.fNamespaces) {
                this.fEntityScanner.scanQName(this.fElementQName, XMLScanner.NameType.ELEMENTSTART);
            } else {
                name = this.fEntityScanner.scanName(XMLScanner.NameType.ELEMENTSTART);
                this.fElementQName.setValues(null, (String)name, (String)name, null);
            }
        }
        if (this.fAdd) {
            this.fElementStack.matchElement(this.fElementQName);
        }
        this.fCurrentElement = this.fElementQName;
        String rawname = this.fElementQName.rawname;
        this.checkDepth(rawname);
        if (this.fBindNamespaces) {
            this.fNamespaceContext.pushContext();
            if (this.fScannerState == 26 && this.fPerformValidation) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_GRAMMAR_NOT_FOUND", new Object[]{rawname}, (short)1);
                if (this.fDoctypeName == null || !this.fDoctypeName.equals(rawname)) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "RootElementTypeMustMatchDoctypedecl", new Object[]{this.fDoctypeName, rawname}, (short)1);
                }
            }
        }
        this.fEmptyElement = false;
        this.fAttributes.removeAllAttributes();
        if (!this.seekCloseOfStartTag()) {
            this.fReadingAttributes = true;
            this.fAttributeCacheUsedCount = 0;
            this.fStringBufferIndex = 0;
            this.fAddDefaultAttr = true;
            this.fXmlnsDeclared = false;
            do {
                this.scanAttribute(this.fAttributes);
                if (this.fSecurityManager == null || this.fSecurityManager.isNoLimit(this.fElementAttributeLimit) || this.fAttributes.getLength() <= this.fElementAttributeLimit) continue;
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "ElementAttributeLimit", new Object[]{rawname, this.fElementAttributeLimit}, (short)2);
            } while (!this.seekCloseOfStartTag());
            this.fReadingAttributes = false;
        }
        if (this.fBindNamespaces) {
            QName name2;
            if (this.fElementQName.prefix == XMLSymbols.PREFIX_XMLNS) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "ElementXMLNSPrefix", new Object[]{this.fElementQName.rawname}, (short)2);
            }
            String prefix = this.fElementQName.prefix != null ? this.fElementQName.prefix : XMLSymbols.EMPTY_STRING;
            this.fCurrentElement.uri = this.fElementQName.uri = this.fNamespaceContext.getURI(prefix);
            if (this.fElementQName.prefix == null && this.fElementQName.uri != null) {
                this.fElementQName.prefix = XMLSymbols.EMPTY_STRING;
            }
            if (this.fElementQName.prefix != null && this.fElementQName.uri == null) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "ElementPrefixUnbound", new Object[]{this.fElementQName.prefix, this.fElementQName.rawname}, (short)2);
            }
            int length = this.fAttributes.getLength();
            for (int i = 0; i < length; ++i) {
                this.fAttributes.getName(i, this.fAttributeQName);
                String aprefix = this.fAttributeQName.prefix != null ? this.fAttributeQName.prefix : XMLSymbols.EMPTY_STRING;
                String uri = this.fNamespaceContext.getURI(aprefix);
                if (this.fAttributeQName.uri != null && this.fAttributeQName.uri == uri || aprefix == XMLSymbols.EMPTY_STRING) continue;
                this.fAttributeQName.uri = uri;
                if (uri == null) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "AttributePrefixUnbound", new Object[]{this.fElementQName.rawname, this.fAttributeQName.rawname, aprefix}, (short)2);
                }
                this.fAttributes.setURI(i, uri);
            }
            if (length > 1 && (name2 = this.fAttributes.checkDuplicatesNS()) != null) {
                if (name2.uri != null) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "AttributeNSNotUnique", new Object[]{this.fElementQName.rawname, name2.localpart, name2.uri}, (short)2);
                } else {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "AttributeNotUnique", new Object[]{this.fElementQName.rawname, name2.rawname}, (short)2);
                }
            }
        }
        if (this.fEmptyElement) {
            --this.fMarkupDepth;
            if (this.fMarkupDepth < this.fEntityStack[this.fEntityDepth - 1]) {
                this.reportFatalError("ElementEntityMismatch", new Object[]{this.fCurrentElement.rawname});
            }
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.emptyElement(this.fElementQName, this.fAttributes, null);
            }
            this.fScanEndElement = true;
            this.fElementStack.popElement();
        } else {
            if (this.dtdGrammarUtil != null) {
                this.dtdGrammarUtil.startElement(this.fElementQName, this.fAttributes);
            }
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.startElement(this.fElementQName, this.fAttributes, null);
            }
        }
        return this.fEmptyElement;
    }

    protected void scanAttribute(XMLAttributesImpl attributes) throws IOException, XNIException {
        this.fEntityScanner.scanQName(this.fAttributeQName, XMLScanner.NameType.ATTRIBUTENAME);
        this.fEntityScanner.skipSpaces();
        if (!this.fEntityScanner.skipChar(61, XMLScanner.NameType.ATTRIBUTE)) {
            this.reportFatalError("EqRequiredInAttribute", new Object[]{this.fCurrentElement.rawname, this.fAttributeQName.rawname});
        }
        this.fEntityScanner.skipSpaces();
        int attrIndex = 0;
        boolean isVC = this.fHasExternalDTD && !this.fStandalone;
        XMLString tmpStr = this.getString();
        String localpart = this.fAttributeQName.localpart;
        String prefix = this.fAttributeQName.prefix != null ? this.fAttributeQName.prefix : XMLSymbols.EMPTY_STRING;
        boolean isNSDecl = this.fBindNamespaces & (prefix == XMLSymbols.PREFIX_XMLNS || prefix == XMLSymbols.EMPTY_STRING && localpart == XMLSymbols.PREFIX_XMLNS);
        this.scanAttributeValue(tmpStr, this.fTempString2, this.fAttributeQName.rawname, attributes, attrIndex, isVC, this.fCurrentElement.rawname, isNSDecl);
        String value = null;
        if (this.fBindNamespaces && isNSDecl) {
            boolean declared;
            String uri;
            if (this.fXMLNameLimit > 0 && tmpStr.length > this.fXMLNameLimit) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MaxXMLNameLimit", new Object[]{new String(tmpStr.ch, tmpStr.offset, tmpStr.length), tmpStr.length, this.fXMLNameLimit, this.fSecurityManager.getStateLiteral(XMLSecurityManager.Limit.MAX_NAME_LIMIT)}, (short)2);
            }
            value = uri = this.fSymbolTable.addSymbol(tmpStr.ch, tmpStr.offset, tmpStr.length);
            if (prefix == XMLSymbols.PREFIX_XMLNS && localpart == XMLSymbols.PREFIX_XMLNS) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXMLNS", new Object[]{this.fAttributeQName}, (short)2);
            }
            if (uri == NamespaceContext.XMLNS_URI) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXMLNS", new Object[]{this.fAttributeQName}, (short)2);
            }
            if (localpart == XMLSymbols.PREFIX_XML) {
                if (uri != NamespaceContext.XML_URI) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXML", new Object[]{this.fAttributeQName}, (short)2);
                }
            } else if (uri == NamespaceContext.XML_URI) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXML", new Object[]{this.fAttributeQName}, (short)2);
            }
            String string = prefix = localpart != XMLSymbols.PREFIX_XMLNS ? localpart : XMLSymbols.EMPTY_STRING;
            if (prefix == XMLSymbols.EMPTY_STRING && localpart == XMLSymbols.PREFIX_XMLNS) {
                this.fAttributeQName.prefix = XMLSymbols.PREFIX_XMLNS;
            }
            if (uri == XMLSymbols.EMPTY_STRING && localpart != XMLSymbols.PREFIX_XMLNS) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "EmptyPrefixedAttName", new Object[]{this.fAttributeQName}, (short)2);
            }
            if (((NamespaceSupport)this.fNamespaceContext).containsPrefixInCurrentContext(prefix)) {
                this.reportFatalError("AttributeNotUnique", new Object[]{this.fCurrentElement.rawname, this.fAttributeQName.rawname});
            }
            if (!(declared = this.fNamespaceContext.declarePrefix(prefix, uri.length() != 0 ? uri : null))) {
                if (this.fXmlnsDeclared) {
                    this.reportFatalError("AttributeNotUnique", new Object[]{this.fCurrentElement.rawname, this.fAttributeQName.rawname});
                }
                this.fXmlnsDeclared = true;
            }
            if (this.fNotAddNSDeclAsAttribute) {
                return;
            }
        }
        if (this.fBindNamespaces) {
            attrIndex = attributes.getLength();
            attributes.addAttributeNS(this.fAttributeQName, XMLSymbols.fCDATASymbol, null);
        } else {
            int oldLen = attributes.getLength();
            attrIndex = attributes.addAttribute(this.fAttributeQName, XMLSymbols.fCDATASymbol, null);
            if (oldLen == attributes.getLength()) {
                this.reportFatalError("AttributeNotUnique", new Object[]{this.fCurrentElement.rawname, this.fAttributeQName.rawname});
            }
        }
        attributes.setValue(attrIndex, value, tmpStr);
        attributes.setSpecified(attrIndex, true);
        if (this.fAttributeQName.prefix != null) {
            attributes.setURI(attrIndex, this.fNamespaceContext.getURI(this.fAttributeQName.prefix));
        }
    }

    @Override
    protected XMLDocumentFragmentScannerImpl.Driver createContentDriver() {
        return new NSContentDriver();
    }

    protected final class NSContentDriver
    extends XMLDocumentScannerImpl.ContentDriver {
        protected NSContentDriver() {
        }

        @Override
        protected boolean scanRootElementHook() throws IOException, XNIException {
            this.reconfigurePipeline();
            if (XMLNSDocumentScannerImpl.this.scanStartElement()) {
                XMLNSDocumentScannerImpl.this.setScannerState(44);
                XMLNSDocumentScannerImpl.this.setDriver(XMLNSDocumentScannerImpl.this.fTrailingMiscDriver);
                return true;
            }
            return false;
        }

        private void reconfigurePipeline() {
            if (XMLNSDocumentScannerImpl.this.fNamespaces && XMLNSDocumentScannerImpl.this.fDTDValidator == null) {
                XMLNSDocumentScannerImpl.this.fBindNamespaces = true;
            } else if (XMLNSDocumentScannerImpl.this.fNamespaces && !XMLNSDocumentScannerImpl.this.fDTDValidator.hasGrammar()) {
                XMLNSDocumentScannerImpl.this.fBindNamespaces = true;
                XMLNSDocumentScannerImpl.this.fPerformValidation = XMLNSDocumentScannerImpl.this.fDTDValidator.validate();
                XMLDocumentSource source = XMLNSDocumentScannerImpl.this.fDTDValidator.getDocumentSource();
                XMLDocumentHandler handler = XMLNSDocumentScannerImpl.this.fDTDValidator.getDocumentHandler();
                source.setDocumentHandler(handler);
                if (handler != null) {
                    handler.setDocumentSource(source);
                }
                XMLNSDocumentScannerImpl.this.fDTDValidator.setDocumentSource(null);
                XMLNSDocumentScannerImpl.this.fDTDValidator.setDocumentHandler(null);
            }
        }
    }
}

