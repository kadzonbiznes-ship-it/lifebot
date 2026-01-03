/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.dom.AttrImpl;
import com.sun.org.apache.xerces.internal.dom.AttrNSImpl;
import com.sun.org.apache.xerces.internal.dom.AttributeMap;
import com.sun.org.apache.xerces.internal.dom.ChildNode;
import com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;
import com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import com.sun.org.apache.xerces.internal.dom.DeepNodeListImpl;
import com.sun.org.apache.xerces.internal.dom.DocumentTypeImpl;
import com.sun.org.apache.xerces.internal.dom.ElementDefinitionImpl;
import com.sun.org.apache.xerces.internal.dom.NamedNodeMapImpl;
import com.sun.org.apache.xerces.internal.dom.NodeImpl;
import com.sun.org.apache.xerces.internal.dom.ParentNode;
import com.sun.org.apache.xerces.internal.util.URI;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;

public class ElementImpl
extends ParentNode
implements Element,
ElementTraversal,
TypeInfo {
    static final long serialVersionUID = 3717253516652722278L;
    protected String name;
    protected AttributeMap attributes;

    public ElementImpl(CoreDocumentImpl ownerDoc, String name) {
        super(ownerDoc);
        this.name = name;
        this.needsSyncData(true);
    }

    protected ElementImpl() {
    }

    void rename(String name) {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.ownerDocument.errorChecking) {
            int colon1 = name.indexOf(58);
            if (colon1 != -1) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NAMESPACE_ERR", null);
                throw new DOMException(14, msg);
            }
            if (!CoreDocumentImpl.isXMLName(name, this.ownerDocument.isXML11Version())) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "INVALID_CHARACTER_ERR", null);
                throw new DOMException(5, msg);
            }
        }
        this.name = name;
        this.reconcileDefaultAttributes();
    }

    @Override
    public short getNodeType() {
        return 1;
    }

    @Override
    public String getNodeName() {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        return this.name;
    }

    @Override
    public NamedNodeMap getAttributes() {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.attributes == null) {
            this.attributes = new AttributeMap(this, null);
        }
        return this.attributes;
    }

    @Override
    public Node cloneNode(boolean deep) {
        ElementImpl newnode = (ElementImpl)super.cloneNode(deep);
        if (this.attributes != null) {
            newnode.attributes = (AttributeMap)this.attributes.cloneMap(newnode);
        }
        return newnode;
    }

    @Override
    public String getBaseURI() {
        String uri;
        Attr attrNode;
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.attributes != null && (attrNode = this.getXMLBaseAttribute()) != null && (uri = attrNode.getNodeValue()).length() != 0) {
            try {
                String parentBaseURI;
                URI _uri = new URI(uri, true);
                if (_uri.isAbsoluteURI()) {
                    return _uri.toString();
                }
                String string = parentBaseURI = this.ownerNode != null ? this.ownerNode.getBaseURI() : null;
                if (parentBaseURI != null) {
                    try {
                        URI _parentBaseURI = new URI(parentBaseURI);
                        _uri.absolutize(_parentBaseURI);
                        return _uri.toString();
                    }
                    catch (URI.MalformedURIException ex) {
                        return null;
                    }
                }
                return null;
            }
            catch (URI.MalformedURIException ex) {
                return null;
            }
        }
        return this.ownerNode != null ? this.ownerNode.getBaseURI() : null;
    }

    protected Attr getXMLBaseAttribute() {
        return (Attr)this.attributes.getNamedItem("xml:base");
    }

    @Override
    protected void setOwnerDocument(CoreDocumentImpl doc) {
        super.setOwnerDocument(doc);
        if (this.attributes != null) {
            this.attributes.setOwnerDocument(doc);
        }
    }

    @Override
    public String getAttribute(String name) {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.attributes == null) {
            return "";
        }
        Attr attr = (Attr)this.attributes.getNamedItem(name);
        return attr == null ? "" : attr.getValue();
    }

    @Override
    public Attr getAttributeNode(String name) {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.attributes == null) {
            return null;
        }
        return (Attr)this.attributes.getNamedItem(name);
    }

    @Override
    public NodeList getElementsByTagName(String tagname) {
        return new DeepNodeListImpl(this, tagname);
    }

    @Override
    public String getTagName() {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        return this.name;
    }

    @Override
    public void normalize() {
        if (this.isNormalized()) {
            return;
        }
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        ChildNode kid = this.firstChild;
        while (kid != null) {
            ChildNode next = kid.nextSibling;
            if (kid.getNodeType() == 3) {
                if (next != null && next.getNodeType() == 3) {
                    ((Text)((Object)kid)).appendData(next.getNodeValue());
                    this.removeChild(next);
                    next = kid;
                } else if (kid.getNodeValue() == null || kid.getNodeValue().length() == 0) {
                    this.removeChild(kid);
                }
            } else if (kid.getNodeType() == 1) {
                kid.normalize();
            }
            kid = next;
        }
        if (this.attributes != null) {
            for (int i = 0; i < this.attributes.getLength(); ++i) {
                Node attr = this.attributes.item(i);
                attr.normalize();
            }
        }
        this.isNormalized(true);
    }

    @Override
    public void removeAttribute(String name) {
        if (this.ownerDocument.errorChecking && this.isReadOnly()) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(7, msg);
        }
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.attributes == null) {
            return;
        }
        this.attributes.safeRemoveNamedItem(name);
    }

    @Override
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        if (this.ownerDocument.errorChecking && this.isReadOnly()) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(7, msg);
        }
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.attributes == null) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_FOUND_ERR", null);
            throw new DOMException(8, msg);
        }
        return (Attr)this.attributes.removeItem(oldAttr, true);
    }

    @Override
    public void setAttribute(String name, String value) {
        Attr newAttr;
        if (this.ownerDocument.errorChecking && this.isReadOnly()) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(7, msg);
        }
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if ((newAttr = this.getAttributeNode(name)) == null) {
            newAttr = this.getOwnerDocument().createAttribute(name);
            if (this.attributes == null) {
                this.attributes = new AttributeMap(this, null);
            }
            newAttr.setNodeValue(value);
            this.attributes.setNamedItem(newAttr);
        } else {
            newAttr.setNodeValue(value);
        }
    }

    @Override
    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.ownerDocument.errorChecking) {
            if (this.isReadOnly()) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null);
                throw new DOMException(7, msg);
            }
            if (newAttr.getOwnerDocument() != this.ownerDocument) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "WRONG_DOCUMENT_ERR", null);
                throw new DOMException(4, msg);
            }
        }
        if (this.attributes == null) {
            this.attributes = new AttributeMap(this, null);
        }
        return (Attr)this.attributes.setNamedItem(newAttr);
    }

    @Override
    public String getAttributeNS(String namespaceURI, String localName) {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.attributes == null) {
            return "";
        }
        Attr attr = (Attr)this.attributes.getNamedItemNS(namespaceURI, localName);
        return attr == null ? "" : attr.getValue();
    }

    @Override
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) {
        String localName;
        String prefix;
        int index;
        if (this.ownerDocument.errorChecking && this.isReadOnly()) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(7, msg);
        }
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if ((index = qualifiedName.indexOf(58)) < 0) {
            prefix = null;
            localName = qualifiedName;
        } else {
            prefix = qualifiedName.substring(0, index);
            localName = qualifiedName.substring(index + 1);
        }
        Attr newAttr = this.getAttributeNodeNS(namespaceURI, localName);
        if (newAttr == null) {
            newAttr = this.getOwnerDocument().createAttributeNS(namespaceURI, qualifiedName);
            if (this.attributes == null) {
                this.attributes = new AttributeMap(this, null);
            }
            newAttr.setNodeValue(value);
            this.attributes.setNamedItemNS(newAttr);
        } else {
            if (newAttr instanceof AttrNSImpl) {
                String newName;
                String origNodeName = ((AttrNSImpl)newAttr).name;
                ((AttrNSImpl)newAttr).name = newName = prefix != null ? prefix + ":" + localName : localName;
                if (!newName.equals(origNodeName)) {
                    newAttr = (Attr)this.attributes.removeItem(newAttr, false);
                    this.attributes.addItem(newAttr);
                }
            } else {
                newAttr = ((CoreDocumentImpl)this.getOwnerDocument()).createAttributeNS(namespaceURI, qualifiedName, localName);
                this.attributes.setNamedItemNS(newAttr);
            }
            newAttr.setNodeValue(value);
        }
    }

    @Override
    public void removeAttributeNS(String namespaceURI, String localName) {
        if (this.ownerDocument.errorChecking && this.isReadOnly()) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(7, msg);
        }
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.attributes == null) {
            return;
        }
        this.attributes.safeRemoveNamedItemNS(namespaceURI, localName);
    }

    @Override
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.attributes == null) {
            return null;
        }
        return (Attr)this.attributes.getNamedItemNS(namespaceURI, localName);
    }

    @Override
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.ownerDocument.errorChecking) {
            if (this.isReadOnly()) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null);
                throw new DOMException(7, msg);
            }
            if (newAttr.getOwnerDocument() != this.ownerDocument) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "WRONG_DOCUMENT_ERR", null);
                throw new DOMException(4, msg);
            }
        }
        if (this.attributes == null) {
            this.attributes = new AttributeMap(this, null);
        }
        return (Attr)this.attributes.setNamedItemNS(newAttr);
    }

    protected int setXercesAttributeNode(Attr attr) {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.attributes == null) {
            this.attributes = new AttributeMap(this, null);
        }
        return this.attributes.addItem(attr);
    }

    protected int getXercesAttribute(String namespaceURI, String localName) {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.attributes == null) {
            return -1;
        }
        return this.attributes.getNamedItemIndex(namespaceURI, localName);
    }

    @Override
    public boolean hasAttributes() {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        return this.attributes != null && this.attributes.getLength() != 0;
    }

    @Override
    public boolean hasAttribute(String name) {
        return this.getAttributeNode(name) != null;
    }

    @Override
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return this.getAttributeNodeNS(namespaceURI, localName) != null;
    }

    @Override
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return new DeepNodeListImpl(this, namespaceURI, localName);
    }

    @Override
    public boolean isEqualNode(Node arg) {
        if (!super.isEqualNode(arg)) {
            return false;
        }
        boolean hasAttrs = this.hasAttributes();
        if (hasAttrs != ((Element)arg).hasAttributes()) {
            return false;
        }
        if (hasAttrs) {
            NamedNodeMap map1 = this.getAttributes();
            NamedNodeMap map2 = ((Element)arg).getAttributes();
            int len = map1.getLength();
            if (len != map2.getLength()) {
                return false;
            }
            for (int i = 0; i < len; ++i) {
                Node n2;
                Node n1 = map1.item(i);
                if (!(n1.getLocalName() == null ? (n2 = map2.getNamedItem(n1.getNodeName())) == null || !((NodeImpl)n1).isEqualNode(n2) : (n2 = map2.getNamedItemNS(n1.getNamespaceURI(), n1.getLocalName())) == null || !((NodeImpl)n1).isEqualNode(n2))) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public void setIdAttributeNode(Attr at, boolean makeId) {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.ownerDocument.errorChecking) {
            if (this.isReadOnly()) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null);
                throw new DOMException(7, msg);
            }
            if (at.getOwnerElement() != this) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_FOUND_ERR", null);
                throw new DOMException(8, msg);
            }
        }
        ((AttrImpl)at).isIdAttribute(makeId);
        if (!makeId) {
            this.ownerDocument.removeIdentifier(at.getValue());
        } else {
            this.ownerDocument.putIdentifier(at.getValue(), this);
        }
    }

    @Override
    public void setIdAttribute(String name, boolean makeId) {
        Attr at;
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if ((at = this.getAttributeNode(name)) == null) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_FOUND_ERR", null);
            throw new DOMException(8, msg);
        }
        if (this.ownerDocument.errorChecking) {
            if (this.isReadOnly()) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null);
                throw new DOMException(7, msg);
            }
            if (at.getOwnerElement() != this) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_FOUND_ERR", null);
                throw new DOMException(8, msg);
            }
        }
        ((AttrImpl)at).isIdAttribute(makeId);
        if (!makeId) {
            this.ownerDocument.removeIdentifier(at.getValue());
        } else {
            this.ownerDocument.putIdentifier(at.getValue(), this);
        }
    }

    @Override
    public void setIdAttributeNS(String namespaceURI, String localName, boolean makeId) {
        Attr at;
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (namespaceURI != null) {
            String string = namespaceURI = namespaceURI.length() == 0 ? null : namespaceURI;
        }
        if ((at = this.getAttributeNodeNS(namespaceURI, localName)) == null) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_FOUND_ERR", null);
            throw new DOMException(8, msg);
        }
        if (this.ownerDocument.errorChecking) {
            if (this.isReadOnly()) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null);
                throw new DOMException(7, msg);
            }
            if (at.getOwnerElement() != this) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_FOUND_ERR", null);
                throw new DOMException(8, msg);
            }
        }
        ((AttrImpl)at).isIdAttribute(makeId);
        if (!makeId) {
            this.ownerDocument.removeIdentifier(at.getValue());
        } else {
            this.ownerDocument.putIdentifier(at.getValue(), this);
        }
    }

    @Override
    public String getTypeName() {
        return null;
    }

    @Override
    public String getTypeNamespace() {
        return null;
    }

    @Override
    public boolean isDerivedFrom(String typeNamespaceArg, String typeNameArg, int derivationMethod) {
        return false;
    }

    @Override
    public TypeInfo getSchemaTypeInfo() {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        return this;
    }

    @Override
    public void setReadOnly(boolean readOnly, boolean deep) {
        super.setReadOnly(readOnly, deep);
        if (this.attributes != null) {
            this.attributes.setReadOnly(readOnly, true);
        }
    }

    @Override
    protected void synchronizeData() {
        this.needsSyncData(false);
        boolean orig = this.ownerDocument.getMutationEvents();
        this.ownerDocument.setMutationEvents(false);
        this.setupDefaultAttributes();
        this.ownerDocument.setMutationEvents(orig);
    }

    void moveSpecifiedAttributes(ElementImpl el) {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (el.hasAttributes()) {
            if (this.attributes == null) {
                this.attributes = new AttributeMap(this, null);
            }
            this.attributes.moveSpecifiedAttributes(el.attributes);
        }
    }

    protected void setupDefaultAttributes() {
        NamedNodeMapImpl defaults = this.getDefaultAttributes();
        if (defaults != null) {
            this.attributes = new AttributeMap(this, defaults);
        }
    }

    protected void reconcileDefaultAttributes() {
        if (this.attributes != null) {
            NamedNodeMapImpl defaults = this.getDefaultAttributes();
            this.attributes.reconcileDefaults(defaults);
        }
    }

    protected NamedNodeMapImpl getDefaultAttributes() {
        DocumentTypeImpl doctype = (DocumentTypeImpl)this.ownerDocument.getDoctype();
        if (doctype == null) {
            return null;
        }
        ElementDefinitionImpl eldef = (ElementDefinitionImpl)doctype.getElements().getNamedItem(this.getNodeName());
        if (eldef == null) {
            return null;
        }
        return (NamedNodeMapImpl)eldef.getAttributes();
    }

    @Override
    public final int getChildElementCount() {
        int count = 0;
        Element child = this.getFirstElementChild();
        while (child != null) {
            ++count;
            child = ((ElementImpl)child).getNextElementSibling();
        }
        return count;
    }

    @Override
    public final Element getFirstElementChild() {
        block4: for (Node n = this.getFirstChild(); n != null; n = n.getNextSibling()) {
            switch (n.getNodeType()) {
                case 1: {
                    return (Element)n;
                }
                case 5: {
                    Element e = this.getFirstElementChild(n);
                    if (e == null) continue block4;
                    return e;
                }
            }
        }
        return null;
    }

    @Override
    public final Element getLastElementChild() {
        block4: for (Node n = this.getLastChild(); n != null; n = n.getPreviousSibling()) {
            switch (n.getNodeType()) {
                case 1: {
                    return (Element)n;
                }
                case 5: {
                    Element e = this.getLastElementChild(n);
                    if (e == null) continue block4;
                    return e;
                }
            }
        }
        return null;
    }

    @Override
    public final Element getNextElementSibling() {
        Node n = this.getNextLogicalSibling(this);
        while (n != null) {
            switch (n.getNodeType()) {
                case 1: {
                    return (Element)n;
                }
                case 5: {
                    Element e = this.getFirstElementChild(n);
                    if (e == null) break;
                    return e;
                }
            }
            n = this.getNextLogicalSibling(n);
        }
        return null;
    }

    @Override
    public final Element getPreviousElementSibling() {
        Node n = this.getPreviousLogicalSibling(this);
        while (n != null) {
            switch (n.getNodeType()) {
                case 1: {
                    return (Element)n;
                }
                case 5: {
                    Element e = this.getLastElementChild(n);
                    if (e == null) break;
                    return e;
                }
            }
            n = this.getPreviousLogicalSibling(n);
        }
        return null;
    }

    private Element getFirstElementChild(Node n) {
        Node top = n;
        while (n != null) {
            if (n.getNodeType() == 1) {
                return (Element)n;
            }
            Node next = n.getFirstChild();
            while (next == null && top != n) {
                next = n.getNextSibling();
                if (next != null || (n = n.getParentNode()) != null && top != n) continue;
                return null;
            }
            n = next;
        }
        return null;
    }

    private Element getLastElementChild(Node n) {
        Node top = n;
        while (n != null) {
            if (n.getNodeType() == 1) {
                return (Element)n;
            }
            Node next = n.getLastChild();
            while (next == null && top != n) {
                next = n.getPreviousSibling();
                if (next != null || (n = n.getParentNode()) != null && top != n) continue;
                return null;
            }
            n = next;
        }
        return null;
    }

    private Node getNextLogicalSibling(Node n) {
        Node next = n.getNextSibling();
        if (next == null) {
            for (Node parent = n.getParentNode(); parent != null && parent.getNodeType() == 5 && (next = parent.getNextSibling()) == null; parent = parent.getParentNode()) {
            }
        }
        return next;
    }

    private Node getPreviousLogicalSibling(Node n) {
        Node prev = n.getPreviousSibling();
        if (prev == null) {
            for (Node parent = n.getParentNode(); parent != null && parent.getNodeType() == 5 && (prev = parent.getPreviousSibling()) == null; parent = parent.getParentNode()) {
            }
        }
        return prev;
    }
}

