/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.dom.AttrImpl;
import com.sun.org.apache.xerces.internal.dom.ChildNode;
import com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;
import com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import com.sun.org.apache.xerces.internal.dom.ElementImpl;
import com.sun.org.apache.xerces.internal.dom.NamedNodeMapImpl;
import com.sun.org.apache.xerces.internal.dom.ParentNode;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

public abstract class NodeImpl
implements Node,
NodeList,
EventTarget,
Cloneable,
Serializable {
    public static final short TREE_POSITION_PRECEDING = 1;
    public static final short TREE_POSITION_FOLLOWING = 2;
    public static final short TREE_POSITION_ANCESTOR = 4;
    public static final short TREE_POSITION_DESCENDANT = 8;
    public static final short TREE_POSITION_EQUIVALENT = 16;
    public static final short TREE_POSITION_SAME_NODE = 32;
    public static final short TREE_POSITION_DISCONNECTED = 0;
    static final long serialVersionUID = -6316591992167219696L;
    public static final short ELEMENT_DEFINITION_NODE = 21;
    protected NodeImpl ownerNode;
    protected short flags;
    protected static final short READONLY = 1;
    protected static final short SYNCDATA = 2;
    protected static final short SYNCCHILDREN = 4;
    protected static final short OWNED = 8;
    protected static final short FIRSTCHILD = 16;
    protected static final short SPECIFIED = 32;
    protected static final short IGNORABLEWS = 64;
    protected static final short HASSTRING = 128;
    protected static final short NORMALIZED = 256;
    protected static final short ID = 512;

    protected NodeImpl(CoreDocumentImpl ownerDocument) {
        this.ownerNode = ownerDocument;
    }

    public NodeImpl() {
    }

    @Override
    public abstract short getNodeType();

    @Override
    public abstract String getNodeName();

    @Override
    public String getNodeValue() throws DOMException {
        return null;
    }

    @Override
    public void setNodeValue(String x) throws DOMException {
    }

    @Override
    public Node appendChild(Node newChild) throws DOMException {
        return this.insertBefore(newChild, null);
    }

    @Override
    public Node cloneNode(boolean deep) {
        NodeImpl newnode;
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        try {
            newnode = (NodeImpl)this.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException("**Internal Error**" + String.valueOf(e));
        }
        newnode.ownerNode = this.ownerDocument();
        newnode.isOwned(false);
        newnode.isReadOnly(false);
        this.ownerDocument().callUserDataHandlers(this, newnode, (short)1);
        return newnode;
    }

    @Override
    public Document getOwnerDocument() {
        if (this.isOwned()) {
            return this.ownerNode.ownerDocument();
        }
        return (Document)((Object)this.ownerNode);
    }

    CoreDocumentImpl ownerDocument() {
        if (this.isOwned()) {
            return this.ownerNode.ownerDocument();
        }
        return (CoreDocumentImpl)this.ownerNode;
    }

    protected void setOwnerDocument(CoreDocumentImpl doc) {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (!this.isOwned()) {
            this.ownerNode = doc;
        }
    }

    protected int getNodeNumber() {
        CoreDocumentImpl cd = (CoreDocumentImpl)this.getOwnerDocument();
        int nodeNumber = cd.getNodeNumber(this);
        return nodeNumber;
    }

    @Override
    public Node getParentNode() {
        return null;
    }

    NodeImpl parentNode() {
        return null;
    }

    @Override
    public Node getNextSibling() {
        return null;
    }

    @Override
    public Node getPreviousSibling() {
        return null;
    }

    ChildNode previousSibling() {
        return null;
    }

    @Override
    public NamedNodeMap getAttributes() {
        return null;
    }

    @Override
    public boolean hasAttributes() {
        return false;
    }

    @Override
    public boolean hasChildNodes() {
        return false;
    }

    @Override
    public NodeList getChildNodes() {
        return this;
    }

    @Override
    public Node getFirstChild() {
        return null;
    }

    @Override
    public Node getLastChild() {
        return null;
    }

    @Override
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        throw new DOMException(3, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "HIERARCHY_REQUEST_ERR", null));
    }

    @Override
    public Node removeChild(Node oldChild) throws DOMException {
        throw new DOMException(8, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_FOUND_ERR", null));
    }

    @Override
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        throw new DOMException(3, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "HIERARCHY_REQUEST_ERR", null));
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public Node item(int index) {
        return null;
    }

    @Override
    public void normalize() {
    }

    @Override
    public boolean isSupported(String feature, String version) {
        return this.ownerDocument().getImplementation().hasFeature(feature, version);
    }

    @Override
    public String getNamespaceURI() {
        return null;
    }

    @Override
    public String getPrefix() {
        return null;
    }

    @Override
    public void setPrefix(String prefix) throws DOMException {
        throw new DOMException(14, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NAMESPACE_ERR", null));
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public void addEventListener(String type, EventListener listener, boolean useCapture) {
        this.ownerDocument().addEventListener(this, type, listener, useCapture);
    }

    @Override
    public void removeEventListener(String type, EventListener listener, boolean useCapture) {
        this.ownerDocument().removeEventListener(this, type, listener, useCapture);
    }

    @Override
    public boolean dispatchEvent(Event event) {
        return this.ownerDocument().dispatchEvent(this, event);
    }

    @Override
    public String getBaseURI() {
        return null;
    }

    @Deprecated
    public short compareTreePosition(Node other) {
        Node node;
        if (this == other) {
            return 48;
        }
        short thisType = this.getNodeType();
        short otherType = other.getNodeType();
        if (thisType == 6 || thisType == 12 || otherType == 6 || otherType == 12) {
            return 0;
        }
        Node thisAncestor = this;
        Node otherAncestor = other;
        int thisDepth = 0;
        int otherDepth = 0;
        for (node = this; node != null; node = node.getParentNode()) {
            ++thisDepth;
            if (node == other) {
                return 5;
            }
            thisAncestor = node;
        }
        for (node = other; node != null; node = node.getParentNode()) {
            ++otherDepth;
            if (node == this) {
                return 10;
            }
            otherAncestor = node;
        }
        Node thisNode = this;
        Node otherNode = other;
        short thisAncestorType = thisAncestor.getNodeType();
        short otherAncestorType = otherAncestor.getNodeType();
        if (thisAncestorType == 2) {
            thisNode = ((AttrImpl)thisAncestor).getOwnerElement();
        }
        if (otherAncestorType == 2) {
            otherNode = ((AttrImpl)otherAncestor).getOwnerElement();
        }
        if (thisAncestorType == 2 && otherAncestorType == 2 && thisNode == otherNode) {
            return 16;
        }
        if (thisAncestorType == 2) {
            thisDepth = 0;
            for (node = thisNode; node != null; node = node.getParentNode()) {
                ++thisDepth;
                if (node == otherNode) {
                    return 1;
                }
                thisAncestor = node;
            }
        }
        if (otherAncestorType == 2) {
            otherDepth = 0;
            for (node = otherNode; node != null; node = node.getParentNode()) {
                ++otherDepth;
                if (node == thisNode) {
                    return 2;
                }
                otherAncestor = node;
            }
        }
        if (thisAncestor != otherAncestor) {
            return 0;
        }
        if (thisDepth > otherDepth) {
            for (i = 0; i < thisDepth - otherDepth; ++i) {
                thisNode = thisNode.getParentNode();
            }
            if (thisNode == otherNode) {
                return 1;
            }
        } else {
            for (i = 0; i < otherDepth - thisDepth; ++i) {
                otherNode = otherNode.getParentNode();
            }
            if (otherNode == thisNode) {
                return 2;
            }
        }
        Node thisNodeP = thisNode.getParentNode();
        for (Node otherNodeP = otherNode.getParentNode(); thisNodeP != otherNodeP; thisNodeP = thisNodeP.getParentNode(), otherNodeP = otherNodeP.getParentNode()) {
            thisNode = thisNodeP;
            otherNode = otherNodeP;
        }
        for (Node current = thisNodeP.getFirstChild(); current != null; current = current.getNextSibling()) {
            if (current == otherNode) {
                return 1;
            }
            if (current != thisNode) continue;
            return 2;
        }
        return 0;
    }

    @Override
    public short compareDocumentPosition(Node other) throws DOMException {
        DocumentType container;
        Node node;
        Document otherOwnerDoc;
        if (this == other) {
            return 0;
        }
        if (other != null && !(other instanceof NodeImpl)) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_SUPPORTED_ERR", null);
            throw new DOMException(9, msg);
        }
        Document thisOwnerDoc = this.getNodeType() == 9 ? (Document)((Object)this) : this.getOwnerDocument();
        if (thisOwnerDoc != (otherOwnerDoc = other.getNodeType() == 9 ? (Document)other : other.getOwnerDocument()) && thisOwnerDoc != null && otherOwnerDoc != null) {
            int thisDocNum;
            int otherDocNum = ((CoreDocumentImpl)otherOwnerDoc).getNodeNumber();
            if (otherDocNum > (thisDocNum = ((CoreDocumentImpl)thisOwnerDoc).getNodeNumber())) {
                return 37;
            }
            return 35;
        }
        Node thisAncestor = this;
        Node otherAncestor = other;
        int thisDepth = 0;
        int otherDepth = 0;
        for (node = this; node != null; node = node.getParentNode()) {
            ++thisDepth;
            if (node == other) {
                return 10;
            }
            thisAncestor = node;
        }
        for (node = other; node != null; node = node.getParentNode()) {
            ++otherDepth;
            if (node == this) {
                return 20;
            }
            otherAncestor = node;
        }
        short thisAncestorType = thisAncestor.getNodeType();
        short otherAncestorType = otherAncestor.getNodeType();
        Node thisNode = this;
        Node otherNode = other;
        switch (thisAncestorType) {
            case 6: 
            case 12: {
                container = thisOwnerDoc.getDoctype();
                if (container == otherAncestor) {
                    return 10;
                }
                switch (otherAncestorType) {
                    case 6: 
                    case 12: {
                        if (thisAncestorType != otherAncestorType) {
                            return thisAncestorType > otherAncestorType ? (short)2 : 4;
                        }
                        if (thisAncestorType == 12) {
                            if (((NamedNodeMapImpl)container.getNotations()).precedes(otherAncestor, thisAncestor)) {
                                return 34;
                            }
                            return 36;
                        }
                        if (((NamedNodeMapImpl)container.getEntities()).precedes(otherAncestor, thisAncestor)) {
                            return 34;
                        }
                        return 36;
                    }
                }
                thisAncestor = thisOwnerDoc;
                thisNode = thisAncestor;
                break;
            }
            case 10: {
                if (otherNode == thisOwnerDoc) {
                    return 10;
                }
                if (thisOwnerDoc == null || thisOwnerDoc != otherOwnerDoc) break;
                return 4;
            }
            case 2: {
                thisNode = ((AttrImpl)thisAncestor).getOwnerElement();
                if (otherAncestorType == 2 && (otherNode = ((AttrImpl)otherAncestor).getOwnerElement()) == thisNode) {
                    if (((NamedNodeMapImpl)thisNode.getAttributes()).precedes(other, this)) {
                        return 34;
                    }
                    return 36;
                }
                thisDepth = 0;
                for (node = thisNode; node != null; node = node.getParentNode()) {
                    ++thisDepth;
                    if (node == otherNode) {
                        return 10;
                    }
                    thisAncestor = node;
                }
                break;
            }
        }
        switch (otherAncestorType) {
            case 6: 
            case 12: {
                container = thisOwnerDoc.getDoctype();
                if (container == this) {
                    return 20;
                }
                otherNode = otherAncestor = thisOwnerDoc;
                break;
            }
            case 10: {
                if (thisNode == otherOwnerDoc) {
                    return 20;
                }
                if (otherOwnerDoc == null || thisOwnerDoc != otherOwnerDoc) break;
                return 2;
            }
            case 2: {
                otherDepth = 0;
                for (node = otherNode = ((AttrImpl)otherAncestor).getOwnerElement(); node != null; node = node.getParentNode()) {
                    ++otherDepth;
                    if (node == thisNode) {
                        return 20;
                    }
                    otherAncestor = node;
                }
                break;
            }
        }
        if (thisAncestor != otherAncestor) {
            int otherAncestorNum;
            int thisAncestorNum = thisAncestor.getNodeNumber();
            if (thisAncestorNum > (otherAncestorNum = ((NodeImpl)otherAncestor).getNodeNumber())) {
                return 37;
            }
            return 35;
        }
        if (thisDepth > otherDepth) {
            for (i = 0; i < thisDepth - otherDepth; ++i) {
                thisNode = thisNode.getParentNode();
            }
            if (thisNode == otherNode) {
                return 2;
            }
        } else {
            for (i = 0; i < otherDepth - thisDepth; ++i) {
                otherNode = otherNode.getParentNode();
            }
            if (otherNode == thisNode) {
                return 4;
            }
        }
        Node thisNodeP = thisNode.getParentNode();
        for (Node otherNodeP = otherNode.getParentNode(); thisNodeP != otherNodeP; thisNodeP = thisNodeP.getParentNode(), otherNodeP = otherNodeP.getParentNode()) {
            thisNode = thisNodeP;
            otherNode = otherNodeP;
        }
        for (Node current = thisNodeP.getFirstChild(); current != null; current = current.getNextSibling()) {
            if (current == otherNode) {
                return 2;
            }
            if (current != thisNode) continue;
            return 4;
        }
        return 0;
    }

    @Override
    public String getTextContent() throws DOMException {
        return this.getNodeValue();
    }

    void getTextContent(StringBuilder buf) throws DOMException {
        String content = this.getNodeValue();
        if (content != null) {
            buf.append(content);
        }
    }

    @Override
    public void setTextContent(String textContent) throws DOMException {
        this.setNodeValue(textContent);
    }

    @Override
    public boolean isSameNode(Node other) {
        return this == other;
    }

    @Override
    public boolean isDefaultNamespace(String namespaceURI) {
        short type = this.getNodeType();
        switch (type) {
            case 1: {
                ElementImpl elem;
                NodeImpl attr;
                String namespace = this.getNamespaceURI();
                String prefix = this.getPrefix();
                if (prefix == null || prefix.length() == 0) {
                    if (namespaceURI == null) {
                        return namespace == namespaceURI;
                    }
                    return namespaceURI.equals(namespace);
                }
                if (this.hasAttributes() && (attr = (NodeImpl)((Object)(elem = (ElementImpl)this).getAttributeNodeNS("http://www.w3.org/2000/xmlns/", "xmlns"))) != null) {
                    String value = attr.getNodeValue();
                    if (namespaceURI == null) {
                        return namespace == value;
                    }
                    return namespaceURI.equals(value);
                }
                NodeImpl ancestor = (NodeImpl)this.getElementAncestor(this);
                if (ancestor != null) {
                    return ancestor.isDefaultNamespace(namespaceURI);
                }
                return false;
            }
            case 9: {
                Element docElement = ((Document)((Object)this)).getDocumentElement();
                if (docElement != null) {
                    return docElement.isDefaultNamespace(namespaceURI);
                }
                return false;
            }
            case 6: 
            case 10: 
            case 11: 
            case 12: {
                return false;
            }
            case 2: {
                if (this.ownerNode.getNodeType() == 1) {
                    return this.ownerNode.isDefaultNamespace(namespaceURI);
                }
                return false;
            }
        }
        NodeImpl ancestor = (NodeImpl)this.getElementAncestor(this);
        if (ancestor != null) {
            return ancestor.isDefaultNamespace(namespaceURI);
        }
        return false;
    }

    @Override
    public String lookupPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            return null;
        }
        short type = this.getNodeType();
        switch (type) {
            case 1: {
                this.getNamespaceURI();
                return this.lookupNamespacePrefix(namespaceURI, (ElementImpl)this);
            }
            case 9: {
                Element docElement = ((Document)((Object)this)).getDocumentElement();
                if (docElement != null) {
                    return docElement.lookupPrefix(namespaceURI);
                }
                return null;
            }
            case 6: 
            case 10: 
            case 11: 
            case 12: {
                return null;
            }
            case 2: {
                if (this.ownerNode.getNodeType() == 1) {
                    return this.ownerNode.lookupPrefix(namespaceURI);
                }
                return null;
            }
        }
        NodeImpl ancestor = (NodeImpl)this.getElementAncestor(this);
        if (ancestor != null) {
            return ancestor.lookupPrefix(namespaceURI);
        }
        return null;
    }

    @Override
    public String lookupNamespaceURI(String specifiedPrefix) {
        short type = this.getNodeType();
        switch (type) {
            case 1: {
                NodeImpl ancestor;
                String namespace = this.getNamespaceURI();
                String prefix = this.getPrefix();
                if (namespace != null) {
                    if (specifiedPrefix == null && prefix == specifiedPrefix) {
                        return namespace;
                    }
                    if (prefix != null && prefix.equals(specifiedPrefix)) {
                        return namespace;
                    }
                }
                if (this.hasAttributes()) {
                    NamedNodeMap map = this.getAttributes();
                    int length = map.getLength();
                    for (int i = 0; i < length; ++i) {
                        Node attr = map.item(i);
                        namespace = attr.getNamespaceURI();
                        if (namespace == null || !namespace.equals("http://www.w3.org/2000/xmlns/")) continue;
                        String attrPrefix = attr.getPrefix();
                        String value = attr.getNodeValue();
                        if (specifiedPrefix == null && attr.getNodeName().equals("xmlns")) {
                            return value.length() > 0 ? value : null;
                        }
                        if (attrPrefix == null || !attrPrefix.equals("xmlns") || !attr.getLocalName().equals(specifiedPrefix)) continue;
                        return value.length() > 0 ? value : null;
                    }
                }
                if ((ancestor = (NodeImpl)this.getElementAncestor(this)) != null) {
                    return ancestor.lookupNamespaceURI(specifiedPrefix);
                }
                return null;
            }
            case 9: {
                Element docElement = ((Document)((Object)this)).getDocumentElement();
                if (docElement != null) {
                    return docElement.lookupNamespaceURI(specifiedPrefix);
                }
                return null;
            }
            case 6: 
            case 10: 
            case 11: 
            case 12: {
                return null;
            }
            case 2: {
                if (this.ownerNode.getNodeType() == 1) {
                    return this.ownerNode.lookupNamespaceURI(specifiedPrefix);
                }
                return null;
            }
        }
        NodeImpl ancestor = (NodeImpl)this.getElementAncestor(this);
        if (ancestor != null) {
            return ancestor.lookupNamespaceURI(specifiedPrefix);
        }
        return null;
    }

    Node getElementAncestor(Node currentNode) {
        for (Node parent = currentNode.getParentNode(); parent != null; parent = parent.getParentNode()) {
            short type = parent.getNodeType();
            if (type != 1) continue;
            return parent;
        }
        return null;
    }

    String lookupNamespacePrefix(String namespaceURI, ElementImpl el) {
        NodeImpl ancestor;
        String foundNamespace;
        String namespace = this.getNamespaceURI();
        String prefix = this.getPrefix();
        if (namespace != null && namespace.equals(namespaceURI) && prefix != null && (foundNamespace = el.lookupNamespaceURI(prefix)) != null && foundNamespace.equals(namespaceURI)) {
            return prefix;
        }
        if (this.hasAttributes()) {
            NamedNodeMap map = this.getAttributes();
            int length = map.getLength();
            for (int i = 0; i < length; ++i) {
                String localname;
                String foundNamespace2;
                Node attr = map.item(i);
                namespace = attr.getNamespaceURI();
                if (namespace == null || !namespace.equals("http://www.w3.org/2000/xmlns/")) continue;
                String attrPrefix = attr.getPrefix();
                String value = attr.getNodeValue();
                if (!attr.getNodeName().equals("xmlns") && (attrPrefix == null || !attrPrefix.equals("xmlns") || !value.equals(namespaceURI)) || (foundNamespace2 = el.lookupNamespaceURI(localname = attr.getLocalName())) == null || !foundNamespace2.equals(namespaceURI)) continue;
                return localname;
            }
        }
        if ((ancestor = (NodeImpl)this.getElementAncestor(this)) != null) {
            return ancestor.lookupNamespacePrefix(namespaceURI, el);
        }
        return null;
    }

    @Override
    public boolean isEqualNode(Node arg) {
        if (arg == this) {
            return true;
        }
        if (arg.getNodeType() != this.getNodeType()) {
            return false;
        }
        if (this.getNodeName() == null ? arg.getNodeName() != null : !this.getNodeName().equals(arg.getNodeName())) {
            return false;
        }
        if (this.getLocalName() == null ? arg.getLocalName() != null : !this.getLocalName().equals(arg.getLocalName())) {
            return false;
        }
        if (this.getNamespaceURI() == null ? arg.getNamespaceURI() != null : !this.getNamespaceURI().equals(arg.getNamespaceURI())) {
            return false;
        }
        if (this.getPrefix() == null ? arg.getPrefix() != null : !this.getPrefix().equals(arg.getPrefix())) {
            return false;
        }
        return !(this.getNodeValue() == null ? arg.getNodeValue() != null : !this.getNodeValue().equals(arg.getNodeValue()));
    }

    @Override
    public Object getFeature(String feature, String version) {
        return this.isSupported(feature, version) ? this : null;
    }

    @Override
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        return this.ownerDocument().setUserData(this, key, data, handler);
    }

    @Override
    public Object getUserData(String key) {
        return this.ownerDocument().getUserData(this, key);
    }

    protected Map<String, ParentNode.UserDataRecord> getUserDataRecord() {
        return this.ownerDocument().getUserDataRecord(this);
    }

    public void setReadOnly(boolean readOnly, boolean deep) {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        this.isReadOnly(readOnly);
    }

    public boolean getReadOnly() {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        return this.isReadOnly();
    }

    public void setUserData(Object data) {
        this.ownerDocument().setUserData(this, data);
    }

    public Object getUserData() {
        return this.ownerDocument().getUserData(this);
    }

    protected void changed() {
        this.ownerDocument().changed();
    }

    protected int changes() {
        return this.ownerDocument().changes();
    }

    protected void synchronizeData() {
        this.needsSyncData(false);
    }

    protected Node getContainer() {
        return null;
    }

    final boolean isReadOnly() {
        return (this.flags & 1) != 0;
    }

    final void isReadOnly(boolean value) {
        this.flags = (short)(value ? this.flags | 1 : this.flags & 0xFFFFFFFE);
    }

    final boolean needsSyncData() {
        return (this.flags & 2) != 0;
    }

    final void needsSyncData(boolean value) {
        this.flags = (short)(value ? this.flags | 2 : this.flags & 0xFFFFFFFD);
    }

    final boolean needsSyncChildren() {
        return (this.flags & 4) != 0;
    }

    public final void needsSyncChildren(boolean value) {
        this.flags = (short)(value ? this.flags | 4 : this.flags & 0xFFFFFFFB);
    }

    final boolean isOwned() {
        return (this.flags & 8) != 0;
    }

    final void isOwned(boolean value) {
        this.flags = (short)(value ? this.flags | 8 : this.flags & 0xFFFFFFF7);
    }

    final boolean isFirstChild() {
        return (this.flags & 0x10) != 0;
    }

    final void isFirstChild(boolean value) {
        this.flags = (short)(value ? this.flags | 0x10 : this.flags & 0xFFFFFFEF);
    }

    final boolean isSpecified() {
        return (this.flags & 0x20) != 0;
    }

    final void isSpecified(boolean value) {
        this.flags = (short)(value ? this.flags | 0x20 : this.flags & 0xFFFFFFDF);
    }

    final boolean internalIsIgnorableWhitespace() {
        return (this.flags & 0x40) != 0;
    }

    final void isIgnorableWhitespace(boolean value) {
        this.flags = (short)(value ? this.flags | 0x40 : this.flags & 0xFFFFFFBF);
    }

    final boolean hasStringValue() {
        return (this.flags & 0x80) != 0;
    }

    final void hasStringValue(boolean value) {
        this.flags = (short)(value ? this.flags | 0x80 : this.flags & 0xFFFFFF7F);
    }

    final boolean isNormalized() {
        return (this.flags & 0x100) != 0;
    }

    final void isNormalized(boolean value) {
        if (!value && this.isNormalized() && this.ownerNode != null) {
            this.ownerNode.isNormalized(false);
        }
        this.flags = (short)(value ? this.flags | 0x100 : this.flags & 0xFFFFFEFF);
    }

    final boolean isIdAttribute() {
        return (this.flags & 0x200) != 0;
    }

    final void isIdAttribute(boolean value) {
        this.flags = (short)(value ? this.flags | 0x200 : this.flags & 0xFFFFFDFF);
    }

    public String toString() {
        return "[" + this.getNodeName() + ": " + this.getNodeValue() + "]";
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        out.defaultWriteObject();
    }
}

