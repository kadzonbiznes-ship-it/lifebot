/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl.dtd;

import com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator;
import com.sun.org.apache.xerces.internal.util.XMLSymbols;
import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import com.sun.org.apache.xerces.internal.xni.XNIException;

public class XMLNSDTDValidator
extends XMLDTDValidator {
    private QName fAttributeQName = new QName();

    @Override
    protected final void startNamespaceScope(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
        this.fNamespaceContext.pushContext();
        if (element.prefix == XMLSymbols.PREFIX_XMLNS) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "ElementXMLNSPrefix", new Object[]{element.rawname}, (short)2);
        }
        int length = attributes.getLength();
        for (int i = 0; i < length; ++i) {
            String localpart = attributes.getLocalName(i);
            String prefix = attributes.getPrefix(i);
            if (prefix != XMLSymbols.PREFIX_XMLNS && (prefix != XMLSymbols.EMPTY_STRING || localpart != XMLSymbols.PREFIX_XMLNS)) continue;
            String uri = this.fSymbolTable.addSymbol(attributes.getValue(i));
            if (prefix == XMLSymbols.PREFIX_XMLNS && localpart == XMLSymbols.PREFIX_XMLNS) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXMLNS", new Object[]{attributes.getQName(i)}, (short)2);
            }
            if (uri == NamespaceContext.XMLNS_URI) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXMLNS", new Object[]{attributes.getQName(i)}, (short)2);
            }
            if (localpart == XMLSymbols.PREFIX_XML) {
                if (uri != NamespaceContext.XML_URI) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXML", new Object[]{attributes.getQName(i)}, (short)2);
                }
            } else if (uri == NamespaceContext.XML_URI) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXML", new Object[]{attributes.getQName(i)}, (short)2);
            }
            String string = prefix = localpart != XMLSymbols.PREFIX_XMLNS ? localpart : XMLSymbols.EMPTY_STRING;
            if (uri == XMLSymbols.EMPTY_STRING && localpart != XMLSymbols.PREFIX_XMLNS) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "EmptyPrefixedAttName", new Object[]{attributes.getQName(i)}, (short)2);
                continue;
            }
            this.fNamespaceContext.declarePrefix(prefix, uri.length() != 0 ? uri : null);
        }
        String prefix = element.prefix != null ? element.prefix : XMLSymbols.EMPTY_STRING;
        element.uri = this.fNamespaceContext.getURI(prefix);
        if (element.prefix == null && element.uri != null) {
            element.prefix = XMLSymbols.EMPTY_STRING;
        }
        if (element.prefix != null && element.uri == null) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "ElementPrefixUnbound", new Object[]{element.prefix, element.rawname}, (short)2);
        }
        for (int i = 0; i < length; ++i) {
            attributes.getName(i, this.fAttributeQName);
            String aprefix = this.fAttributeQName.prefix != null ? this.fAttributeQName.prefix : XMLSymbols.EMPTY_STRING;
            String arawname = this.fAttributeQName.rawname;
            if (arawname == XMLSymbols.PREFIX_XMLNS) {
                this.fAttributeQName.uri = this.fNamespaceContext.getURI(XMLSymbols.PREFIX_XMLNS);
                attributes.setName(i, this.fAttributeQName);
                continue;
            }
            if (aprefix == XMLSymbols.EMPTY_STRING) continue;
            this.fAttributeQName.uri = this.fNamespaceContext.getURI(aprefix);
            if (this.fAttributeQName.uri == null) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "AttributePrefixUnbound", new Object[]{element.rawname, arawname, aprefix}, (short)2);
            }
            attributes.setName(i, this.fAttributeQName);
        }
        int attrCount = attributes.getLength();
        for (int i = 0; i < attrCount - 1; ++i) {
            String auri = attributes.getURI(i);
            if (auri == null || auri == NamespaceContext.XMLNS_URI) continue;
            String alocalpart = attributes.getLocalName(i);
            for (int j = i + 1; j < attrCount; ++j) {
                String blocalpart = attributes.getLocalName(j);
                String buri = attributes.getURI(j);
                if (alocalpart != blocalpart || auri != buri) continue;
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "AttributeNSNotUnique", new Object[]{element.rawname, alocalpart, auri}, (short)2);
            }
        }
    }

    @Override
    protected void endNamespaceScope(QName element, Augmentations augs, boolean isEmpty) throws XNIException {
        String eprefix = element.prefix != null ? element.prefix : XMLSymbols.EMPTY_STRING;
        element.uri = this.fNamespaceContext.getURI(eprefix);
        if (element.uri != null) {
            element.prefix = eprefix;
        }
        if (this.fDocumentHandler != null && !isEmpty) {
            this.fDocumentHandler.endElement(element, augs);
        }
        this.fNamespaceContext.popContext();
    }
}

