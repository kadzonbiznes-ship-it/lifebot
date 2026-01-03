/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.dom.AttrImpl;
import com.sun.org.apache.xerces.internal.dom.AttrNSImpl;
import com.sun.org.apache.xerces.internal.dom.CDATASectionImpl;
import com.sun.org.apache.xerces.internal.dom.ChildNode;
import com.sun.org.apache.xerces.internal.dom.CommentImpl;
import com.sun.org.apache.xerces.internal.dom.CoreDOMImplementationImpl;
import com.sun.org.apache.xerces.internal.dom.DOMConfigurationImpl;
import com.sun.org.apache.xerces.internal.dom.DOMImplementationImpl;
import com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import com.sun.org.apache.xerces.internal.dom.DOMNormalizer;
import com.sun.org.apache.xerces.internal.dom.DeepNodeListImpl;
import com.sun.org.apache.xerces.internal.dom.DeferredDOMImplementationImpl;
import com.sun.org.apache.xerces.internal.dom.DocumentFragmentImpl;
import com.sun.org.apache.xerces.internal.dom.DocumentTypeImpl;
import com.sun.org.apache.xerces.internal.dom.ElementDefinitionImpl;
import com.sun.org.apache.xerces.internal.dom.ElementImpl;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import com.sun.org.apache.xerces.internal.dom.EntityImpl;
import com.sun.org.apache.xerces.internal.dom.EntityReferenceImpl;
import com.sun.org.apache.xerces.internal.dom.NodeImpl;
import com.sun.org.apache.xerces.internal.dom.NodeListCache;
import com.sun.org.apache.xerces.internal.dom.NotationImpl;
import com.sun.org.apache.xerces.internal.dom.ParentNode;
import com.sun.org.apache.xerces.internal.dom.ProcessingInstructionImpl;
import com.sun.org.apache.xerces.internal.dom.TextImpl;
import com.sun.org.apache.xerces.internal.util.URI;
import com.sun.org.apache.xerces.internal.util.XML11Char;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import jdk.xml.internal.SecuritySupport;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Notation;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class CoreDocumentImpl
extends ParentNode
implements Document {
    static final long serialVersionUID = 0L;
    protected DocumentTypeImpl docType;
    protected ElementImpl docElement;
    transient NodeListCache fFreeNLCache;
    protected String encoding;
    protected String actualEncoding;
    protected String version;
    protected boolean standalone;
    protected String fDocumentURI;
    private Map<Node, Map<String, ParentNode.UserDataRecord>> nodeUserData;
    protected Map<String, Node> identifiers;
    transient DOMNormalizer domNormalizer = null;
    transient DOMConfigurationImpl fConfiguration = null;
    transient Object fXPathEvaluator = null;
    private static final int[] kidOK = new int[13];
    protected int changes = 0;
    protected boolean allowGrammarAccess;
    protected boolean errorChecking = true;
    protected boolean ancestorChecking = true;
    protected boolean xmlVersionChanged = false;
    private int documentNumber = 0;
    private int nodeCounter = 0;
    private Map<Node, Integer> nodeTable;
    private boolean xml11Version = false;
    private static final ObjectStreamField[] serialPersistentFields;

    public CoreDocumentImpl() {
        this(false);
    }

    public CoreDocumentImpl(boolean grammarAccess) {
        super(null);
        this.ownerDocument = this;
        this.allowGrammarAccess = grammarAccess;
        String systemProp = SecuritySupport.getSystemProperty("http://java.sun.com/xml/dom/properties/ancestor-check");
        if (systemProp != null && systemProp.equalsIgnoreCase("false")) {
            this.ancestorChecking = false;
        }
    }

    public CoreDocumentImpl(DocumentType doctype) {
        this(doctype, false);
    }

    public CoreDocumentImpl(DocumentType doctype, boolean grammarAccess) {
        this(grammarAccess);
        if (doctype != null) {
            try {
                DocumentTypeImpl doctypeImpl = (DocumentTypeImpl)doctype;
            }
            catch (ClassCastException e) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "WRONG_DOCUMENT_ERR", null);
                throw new DOMException(4, msg);
            }
            doctypeImpl.ownerDocument = this;
            this.appendChild(doctype);
        }
    }

    @Override
    public final Document getOwnerDocument() {
        return null;
    }

    @Override
    public short getNodeType() {
        return 9;
    }

    @Override
    public String getNodeName() {
        return "#document";
    }

    @Override
    public Node cloneNode(boolean deep) {
        CoreDocumentImpl newdoc = new CoreDocumentImpl();
        this.callUserDataHandlers(this, newdoc, (short)1);
        this.cloneNode(newdoc, deep);
        return newdoc;
    }

    protected void cloneNode(CoreDocumentImpl newdoc, boolean deep) {
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        if (deep) {
            HashMap<Node, String> reversedIdentifiers = null;
            if (this.identifiers != null) {
                reversedIdentifiers = HashMap.newHashMap(this.identifiers.size());
                for (String elementId : this.identifiers.keySet()) {
                    reversedIdentifiers.put(this.identifiers.get(elementId), elementId);
                }
            }
            ChildNode kid = this.firstChild;
            while (kid != null) {
                newdoc.appendChild(newdoc.importNode(kid, true, true, reversedIdentifiers));
                kid = kid.nextSibling;
            }
        }
        newdoc.allowGrammarAccess = this.allowGrammarAccess;
        newdoc.errorChecking = this.errorChecking;
    }

    @Override
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        short type = newChild.getNodeType();
        if (this.errorChecking && (type == 1 && this.docElement != null || type == 10 && this.docType != null)) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "HIERARCHY_REQUEST_ERR", null);
            throw new DOMException(3, msg);
        }
        if (newChild.getOwnerDocument() == null && newChild instanceof DocumentTypeImpl) {
            ((DocumentTypeImpl)newChild).ownerDocument = this;
        }
        super.insertBefore(newChild, refChild);
        if (type == 1) {
            this.docElement = (ElementImpl)newChild;
        } else if (type == 10) {
            this.docType = (DocumentTypeImpl)newChild;
        }
        return newChild;
    }

    @Override
    public Node removeChild(Node oldChild) throws DOMException {
        super.removeChild(oldChild);
        short type = oldChild.getNodeType();
        if (type == 1) {
            this.docElement = null;
        } else if (type == 10) {
            this.docType = null;
        }
        return oldChild;
    }

    @Override
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        if (newChild.getOwnerDocument() == null && newChild instanceof DocumentTypeImpl) {
            ((DocumentTypeImpl)newChild).ownerDocument = this;
        }
        if (this.errorChecking && (this.docType != null && oldChild.getNodeType() != 10 && newChild.getNodeType() == 10 || this.docElement != null && oldChild.getNodeType() != 1 && newChild.getNodeType() == 1)) {
            throw new DOMException(3, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "HIERARCHY_REQUEST_ERR", null));
        }
        super.replaceChild(newChild, oldChild);
        short type = oldChild.getNodeType();
        if (type == 1) {
            this.docElement = (ElementImpl)newChild;
        } else if (type == 10) {
            this.docType = (DocumentTypeImpl)newChild;
        }
        return oldChild;
    }

    @Override
    public String getTextContent() throws DOMException {
        return null;
    }

    @Override
    public void setTextContent(String textContent) throws DOMException {
    }

    @Override
    public Object getFeature(String feature, String version) {
        return super.getFeature(feature, version);
    }

    @Override
    public Attr createAttribute(String name) throws DOMException {
        if (this.errorChecking && !CoreDocumentImpl.isXMLName(name, this.xml11Version)) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "INVALID_CHARACTER_ERR", null);
            throw new DOMException(5, msg);
        }
        return new AttrImpl(this, name);
    }

    @Override
    public CDATASection createCDATASection(String data) throws DOMException {
        return new CDATASectionImpl(this, data);
    }

    @Override
    public Comment createComment(String data) {
        return new CommentImpl(this, data);
    }

    @Override
    public DocumentFragment createDocumentFragment() {
        return new DocumentFragmentImpl(this);
    }

    @Override
    public Element createElement(String tagName) throws DOMException {
        if (this.errorChecking && !CoreDocumentImpl.isXMLName(tagName, this.xml11Version)) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "INVALID_CHARACTER_ERR", null);
            throw new DOMException(5, msg);
        }
        return new ElementImpl(this, tagName);
    }

    @Override
    public EntityReference createEntityReference(String name) throws DOMException {
        if (this.errorChecking && !CoreDocumentImpl.isXMLName(name, this.xml11Version)) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "INVALID_CHARACTER_ERR", null);
            throw new DOMException(5, msg);
        }
        return new EntityReferenceImpl(this, name);
    }

    @Override
    public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
        if (this.errorChecking && !CoreDocumentImpl.isXMLName(target, this.xml11Version)) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "INVALID_CHARACTER_ERR", null);
            throw new DOMException(5, msg);
        }
        return new ProcessingInstructionImpl(this, target, data);
    }

    @Override
    public Text createTextNode(String data) {
        return new TextImpl(this, data);
    }

    @Override
    public DocumentType getDoctype() {
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        return this.docType;
    }

    @Override
    public Element getDocumentElement() {
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        return this.docElement;
    }

    @Override
    public NodeList getElementsByTagName(String tagname) {
        return new DeepNodeListImpl(this, tagname);
    }

    @Override
    public DOMImplementation getImplementation() {
        return CoreDOMImplementationImpl.getDOMImplementation();
    }

    public void setErrorChecking(boolean check) {
        this.errorChecking = check;
    }

    @Override
    public void setStrictErrorChecking(boolean check) {
        this.errorChecking = check;
    }

    public boolean getErrorChecking() {
        return this.errorChecking;
    }

    @Override
    public boolean getStrictErrorChecking() {
        return this.errorChecking;
    }

    @Override
    public String getInputEncoding() {
        return this.actualEncoding;
    }

    public void setInputEncoding(String value) {
        this.actualEncoding = value;
    }

    public void setXmlEncoding(String value) {
        this.encoding = value;
    }

    @Deprecated
    public void setEncoding(String value) {
        this.setXmlEncoding(value);
    }

    @Override
    public String getXmlEncoding() {
        return this.encoding;
    }

    @Deprecated
    public String getEncoding() {
        return this.getXmlEncoding();
    }

    @Override
    public void setXmlVersion(String value) {
        if (value == null) {
            return;
        }
        if (value.equals("1.0") || value.equals("1.1")) {
            if (!this.getXmlVersion().equals(value)) {
                this.xmlVersionChanged = true;
                this.isNormalized(false);
                this.version = value;
            }
        } else {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_SUPPORTED_ERR", null);
            throw new DOMException(9, msg);
        }
        this.xml11Version = this.getXmlVersion().equals("1.1");
    }

    @Deprecated
    public void setVersion(String value) {
        this.setXmlVersion(value);
    }

    @Override
    public String getXmlVersion() {
        return this.version == null ? "1.0" : this.version;
    }

    @Deprecated
    public String getVersion() {
        return this.getXmlVersion();
    }

    @Override
    public void setXmlStandalone(boolean value) throws DOMException {
        this.standalone = value;
    }

    @Deprecated
    public void setStandalone(boolean value) {
        this.setXmlStandalone(value);
    }

    @Override
    public boolean getXmlStandalone() {
        return this.standalone;
    }

    @Deprecated
    public boolean getStandalone() {
        return this.getXmlStandalone();
    }

    @Override
    public String getDocumentURI() {
        return this.fDocumentURI;
    }

    @Override
    public Node renameNode(Node n, String namespaceURI, String name) throws DOMException {
        if (this.errorChecking && n.getOwnerDocument() != this && n != this) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "WRONG_DOCUMENT_ERR", null);
            throw new DOMException(4, msg);
        }
        switch (n.getNodeType()) {
            case 1: {
                ElementImpl el = (ElementImpl)n;
                if (el instanceof ElementNSImpl) {
                    ((ElementNSImpl)el).rename(namespaceURI, name);
                    this.callUserDataHandlers(el, null, (short)4);
                } else if (namespaceURI == null) {
                    if (this.errorChecking) {
                        int colon1 = name.indexOf(58);
                        if (colon1 != -1) {
                            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NAMESPACE_ERR", null);
                            throw new DOMException(14, msg);
                        }
                        if (!CoreDocumentImpl.isXMLName(name, this.xml11Version)) {
                            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "INVALID_CHARACTER_ERR", null);
                            throw new DOMException(5, msg);
                        }
                    }
                    el.rename(name);
                    this.callUserDataHandlers(el, null, (short)4);
                } else {
                    ElementNSImpl nel = new ElementNSImpl(this, namespaceURI, name);
                    this.copyEventListeners(el, nel);
                    Map<String, ParentNode.UserDataRecord> data = this.removeUserDataTable(el);
                    Node parent = el.getParentNode();
                    Node nextSib = el.getNextSibling();
                    if (parent != null) {
                        parent.removeChild(el);
                    }
                    Node child = el.getFirstChild();
                    while (child != null) {
                        el.removeChild(child);
                        nel.appendChild(child);
                        child = el.getFirstChild();
                    }
                    nel.moveSpecifiedAttributes(el);
                    this.setUserDataTable(nel, data);
                    this.callUserDataHandlers(el, nel, (short)4);
                    if (parent != null) {
                        parent.insertBefore(nel, nextSib);
                    }
                    el = nel;
                }
                this.renamedElement((Element)n, el);
                return el;
            }
            case 2: {
                AttrImpl at = (AttrImpl)n;
                Element el = at.getOwnerElement();
                if (el != null) {
                    el.removeAttributeNode(at);
                }
                if (n instanceof AttrNSImpl) {
                    ((AttrNSImpl)at).rename(namespaceURI, name);
                    if (el != null) {
                        el.setAttributeNodeNS(at);
                    }
                    this.callUserDataHandlers(at, null, (short)4);
                } else if (namespaceURI == null) {
                    at.rename(name);
                    if (el != null) {
                        el.setAttributeNode(at);
                    }
                    this.callUserDataHandlers(at, null, (short)4);
                } else {
                    AttrNSImpl nat = new AttrNSImpl(this, namespaceURI, name);
                    this.copyEventListeners(at, nat);
                    Map<String, ParentNode.UserDataRecord> data = this.removeUserDataTable(at);
                    Node child = at.getFirstChild();
                    while (child != null) {
                        at.removeChild(child);
                        nat.appendChild(child);
                        child = at.getFirstChild();
                    }
                    this.setUserDataTable(nat, data);
                    this.callUserDataHandlers(at, nat, (short)4);
                    if (el != null) {
                        el.setAttributeNode(nat);
                    }
                    at = nat;
                }
                this.renamedAttrNode((Attr)n, at);
                return at;
            }
        }
        String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_SUPPORTED_ERR", null);
        throw new DOMException(9, msg);
    }

    @Override
    public void normalizeDocument() {
        if (this.isNormalized() && !this.isNormalizeDocRequired()) {
            return;
        }
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        if (this.domNormalizer == null) {
            this.domNormalizer = new DOMNormalizer();
        }
        if (this.fConfiguration == null) {
            this.fConfiguration = new DOMConfigurationImpl();
        } else {
            this.fConfiguration.reset();
        }
        this.domNormalizer.normalizeDocument(this, this.fConfiguration);
        this.isNormalized(true);
        this.xmlVersionChanged = false;
    }

    @Override
    public DOMConfiguration getDomConfig() {
        if (this.fConfiguration == null) {
            this.fConfiguration = new DOMConfigurationImpl();
        }
        return this.fConfiguration;
    }

    @Override
    public String getBaseURI() {
        if (this.fDocumentURI != null && this.fDocumentURI.length() != 0) {
            try {
                return new URI(this.fDocumentURI).toString();
            }
            catch (URI.MalformedURIException e) {
                return null;
            }
        }
        return this.fDocumentURI;
    }

    @Override
    public void setDocumentURI(String documentURI) {
        this.fDocumentURI = documentURI;
    }

    public boolean getAsync() {
        return false;
    }

    public void setAsync(boolean async) {
        if (async) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_SUPPORTED_ERR", null);
            throw new DOMException(9, msg);
        }
    }

    public void abort() {
    }

    public boolean load(String uri) {
        return false;
    }

    public boolean loadXML(String source) {
        return false;
    }

    public String saveXML(Node node) throws DOMException {
        if (this.errorChecking && node != null && this != node.getOwnerDocument()) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "WRONG_DOCUMENT_ERR", null);
            throw new DOMException(4, msg);
        }
        DOMImplementationLS domImplLS = (DOMImplementationLS)((Object)DOMImplementationImpl.getDOMImplementation());
        LSSerializer xmlWriter = domImplLS.createLSSerializer();
        if (node == null) {
            node = this;
        }
        return xmlWriter.writeToString(node);
    }

    void setMutationEvents(boolean set) {
    }

    boolean getMutationEvents() {
        return false;
    }

    public DocumentType createDocumentType(String qualifiedName, String publicID, String systemID) throws DOMException {
        return new DocumentTypeImpl(this, qualifiedName, publicID, systemID);
    }

    public Entity createEntity(String name) throws DOMException {
        if (this.errorChecking && !CoreDocumentImpl.isXMLName(name, this.xml11Version)) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "INVALID_CHARACTER_ERR", null);
            throw new DOMException(5, msg);
        }
        return new EntityImpl(this, name);
    }

    public Notation createNotation(String name) throws DOMException {
        if (this.errorChecking && !CoreDocumentImpl.isXMLName(name, this.xml11Version)) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "INVALID_CHARACTER_ERR", null);
            throw new DOMException(5, msg);
        }
        return new NotationImpl(this, name);
    }

    public ElementDefinitionImpl createElementDefinition(String name) throws DOMException {
        if (this.errorChecking && !CoreDocumentImpl.isXMLName(name, this.xml11Version)) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "INVALID_CHARACTER_ERR", null);
            throw new DOMException(5, msg);
        }
        return new ElementDefinitionImpl(this, name);
    }

    @Override
    protected int getNodeNumber() {
        if (this.documentNumber == 0) {
            CoreDOMImplementationImpl cd = (CoreDOMImplementationImpl)CoreDOMImplementationImpl.getDOMImplementation();
            this.documentNumber = cd.assignDocumentNumber();
        }
        return this.documentNumber;
    }

    protected int getNodeNumber(Node node) {
        int num;
        if (this.nodeTable == null) {
            this.nodeTable = new HashMap<Node, Integer>();
            num = --this.nodeCounter;
            this.nodeTable.put(node, num);
        } else {
            Integer n = this.nodeTable.get(node);
            if (n == null) {
                num = --this.nodeCounter;
                this.nodeTable.put(node, num);
            } else {
                num = n;
            }
        }
        return num;
    }

    @Override
    public Node importNode(Node source, boolean deep) throws DOMException {
        return this.importNode(source, deep, false, null);
    }

    private Node importNode(Node source, boolean deep, boolean cloningDoc, Map<Node, String> reversedIdentifiers) throws DOMException {
        Node newnode = null;
        Map<String, ParentNode.UserDataRecord> userData = null;
        if (source instanceof NodeImpl) {
            userData = ((NodeImpl)source).getUserDataRecord();
        }
        short type = source.getNodeType();
        switch (type) {
            case 1: {
                String elementId;
                boolean domLevel20 = source.getOwnerDocument().getImplementation().hasFeature("XML", "2.0");
                Element newElement = !domLevel20 || source.getLocalName() == null ? this.createElement(source.getNodeName()) : this.createElementNS(source.getNamespaceURI(), source.getNodeName());
                NamedNodeMap sourceAttrs = source.getAttributes();
                if (sourceAttrs != null) {
                    int length = sourceAttrs.getLength();
                    for (int index = 0; index < length; ++index) {
                        Attr attr = (Attr)sourceAttrs.item(index);
                        if (!attr.getSpecified() && !cloningDoc) continue;
                        Attr newAttr = (Attr)this.importNode(attr, true, cloningDoc, reversedIdentifiers);
                        if (!domLevel20 || attr.getLocalName() == null) {
                            newElement.setAttributeNode(newAttr);
                            continue;
                        }
                        newElement.setAttributeNodeNS(newAttr);
                    }
                }
                if (reversedIdentifiers != null && (elementId = reversedIdentifiers.get(source)) != null) {
                    if (this.identifiers == null) {
                        this.identifiers = new HashMap<String, Node>();
                    }
                    this.identifiers.put(elementId, newElement);
                }
                newnode = newElement;
                break;
            }
            case 2: {
                newnode = source.getOwnerDocument().getImplementation().hasFeature("XML", "2.0") ? (source.getLocalName() == null ? this.createAttribute(source.getNodeName()) : this.createAttributeNS(source.getNamespaceURI(), source.getNodeName())) : this.createAttribute(source.getNodeName());
                if (source instanceof AttrImpl) {
                    AttrImpl attr = (AttrImpl)source;
                    if (attr.hasStringValue()) {
                        AttrImpl newattr = (AttrImpl)newnode;
                        newattr.setValue(attr.getValue());
                        deep = false;
                        break;
                    }
                    deep = true;
                    break;
                }
                if (source.getFirstChild() == null) {
                    newnode.setNodeValue(source.getNodeValue());
                    deep = false;
                    break;
                }
                deep = true;
                break;
            }
            case 3: {
                newnode = this.createTextNode(source.getNodeValue());
                break;
            }
            case 4: {
                newnode = this.createCDATASection(source.getNodeValue());
                break;
            }
            case 5: {
                newnode = this.createEntityReference(source.getNodeName());
                deep = false;
                break;
            }
            case 6: {
                Entity srcentity = (Entity)source;
                EntityImpl newentity = (EntityImpl)this.createEntity(source.getNodeName());
                newentity.setPublicId(srcentity.getPublicId());
                newentity.setSystemId(srcentity.getSystemId());
                newentity.setNotationName(srcentity.getNotationName());
                newentity.isReadOnly(false);
                newnode = newentity;
                break;
            }
            case 7: {
                newnode = this.createProcessingInstruction(source.getNodeName(), source.getNodeValue());
                break;
            }
            case 8: {
                newnode = this.createComment(source.getNodeValue());
                break;
            }
            case 10: {
                int i;
                if (!cloningDoc) {
                    String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_SUPPORTED_ERR", null);
                    throw new DOMException(9, msg);
                }
                DocumentType srcdoctype = (DocumentType)source;
                DocumentTypeImpl newdoctype = (DocumentTypeImpl)this.createDocumentType(srcdoctype.getNodeName(), srcdoctype.getPublicId(), srcdoctype.getSystemId());
                NamedNodeMap smap = srcdoctype.getEntities();
                NamedNodeMap tmap = newdoctype.getEntities();
                if (smap != null) {
                    for (i = 0; i < smap.getLength(); ++i) {
                        tmap.setNamedItem(this.importNode(smap.item(i), true, true, reversedIdentifiers));
                    }
                }
                smap = srcdoctype.getNotations();
                tmap = newdoctype.getNotations();
                if (smap != null) {
                    for (i = 0; i < smap.getLength(); ++i) {
                        tmap.setNamedItem(this.importNode(smap.item(i), true, true, reversedIdentifiers));
                    }
                }
                newnode = newdoctype;
                break;
            }
            case 11: {
                newnode = this.createDocumentFragment();
                break;
            }
            case 12: {
                Notation srcnotation = (Notation)source;
                NotationImpl newnotation = (NotationImpl)this.createNotation(source.getNodeName());
                newnotation.setPublicId(srcnotation.getPublicId());
                newnotation.setSystemId(srcnotation.getSystemId());
                newnode = newnotation;
                break;
            }
            default: {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_SUPPORTED_ERR", null);
                throw new DOMException(9, msg);
            }
        }
        if (userData != null) {
            this.callUserDataHandlers(source, newnode, (short)2, userData);
        }
        if (deep) {
            for (Node srckid = source.getFirstChild(); srckid != null; srckid = srckid.getNextSibling()) {
                newnode.appendChild(this.importNode(srckid, true, cloningDoc, reversedIdentifiers));
            }
        }
        if (newnode.getNodeType() == 6) {
            ((NodeImpl)newnode).setReadOnly(true, true);
        }
        return newnode;
    }

    @Override
    public Node adoptNode(Node source) {
        Map<String, ParentNode.UserDataRecord> userData;
        NodeImpl node;
        try {
            node = (NodeImpl)source;
        }
        catch (ClassCastException e) {
            return null;
        }
        if (source == null) {
            return null;
        }
        if (source.getOwnerDocument() != null) {
            DOMImplementation otherImpl;
            DOMImplementation thisImpl = this.getImplementation();
            if (thisImpl != (otherImpl = source.getOwnerDocument().getImplementation())) {
                if (thisImpl instanceof DOMImplementationImpl && otherImpl instanceof DeferredDOMImplementationImpl) {
                    this.undeferChildren(node);
                } else if (!(thisImpl instanceof DeferredDOMImplementationImpl) || !(otherImpl instanceof DOMImplementationImpl)) {
                    return null;
                }
            } else if (otherImpl instanceof DeferredDOMImplementationImpl) {
                this.undeferChildren(node);
            }
        }
        switch (node.getNodeType()) {
            case 2: {
                AttrImpl attr = (AttrImpl)node;
                if (attr.getOwnerElement() != null) {
                    attr.getOwnerElement().removeAttributeNode(attr);
                }
                attr.isSpecified(true);
                userData = node.getUserDataRecord();
                attr.setOwnerDocument(this);
                if (userData == null) break;
                this.setUserDataTable(node, userData);
                break;
            }
            case 6: 
            case 12: {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null);
                throw new DOMException(7, msg);
            }
            case 9: 
            case 10: {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_SUPPORTED_ERR", null);
                throw new DOMException(9, msg);
            }
            case 5: {
                NamedNodeMap entities;
                Node entityNode;
                Node child;
                userData = node.getUserDataRecord();
                Node parent = node.getParentNode();
                if (parent != null) {
                    parent.removeChild(source);
                }
                while ((child = node.getFirstChild()) != null) {
                    node.removeChild(child);
                }
                node.setOwnerDocument(this);
                if (userData != null) {
                    this.setUserDataTable(node, userData);
                }
                if (this.docType == null || (entityNode = (entities = this.docType.getEntities()).getNamedItem(node.getNodeName())) == null) break;
                for (child = entityNode.getFirstChild(); child != null; child = child.getNextSibling()) {
                    Node childClone = child.cloneNode(true);
                    node.appendChild(childClone);
                }
                break;
            }
            case 1: {
                userData = node.getUserDataRecord();
                Node parent = node.getParentNode();
                if (parent != null) {
                    parent.removeChild(source);
                }
                node.setOwnerDocument(this);
                if (userData != null) {
                    this.setUserDataTable(node, userData);
                }
                ((ElementImpl)node).reconcileDefaultAttributes();
                break;
            }
            default: {
                userData = node.getUserDataRecord();
                Node parent = node.getParentNode();
                if (parent != null) {
                    parent.removeChild(source);
                }
                node.setOwnerDocument(this);
                if (userData == null) break;
                this.setUserDataTable(node, userData);
            }
        }
        if (userData != null) {
            this.callUserDataHandlers(source, null, (short)5, userData);
        }
        return node;
    }

    protected void undeferChildren(Node node) {
        Node top = node;
        while (null != node) {
            NamedNodeMap attributes;
            if (((NodeImpl)node).needsSyncData()) {
                ((NodeImpl)node).synchronizeData();
            }
            if ((attributes = node.getAttributes()) != null) {
                int length = attributes.getLength();
                for (int i = 0; i < length; ++i) {
                    this.undeferChildren(attributes.item(i));
                }
            }
            Node nextNode = null;
            nextNode = node.getFirstChild();
            while (null == nextNode && !top.equals(node)) {
                nextNode = node.getNextSibling();
                if (null != nextNode || null != (node = node.getParentNode()) && !top.equals(node)) continue;
                nextNode = null;
                break;
            }
            node = nextNode;
        }
    }

    @Override
    public Element getElementById(String elementId) {
        return this.getIdentifier(elementId);
    }

    protected final void clearIdentifiers() {
        if (this.identifiers != null) {
            this.identifiers.clear();
        }
    }

    public void putIdentifier(String idName, Element element) {
        if (element == null) {
            this.removeIdentifier(idName);
            return;
        }
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.identifiers == null) {
            this.identifiers = new HashMap<String, Node>();
        }
        this.identifiers.put(idName, element);
    }

    public Element getIdentifier(String idName) {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.identifiers == null) {
            return null;
        }
        Element elem = (Element)this.identifiers.get(idName);
        if (elem != null) {
            for (Node parent = elem.getParentNode(); parent != null; parent = parent.getParentNode()) {
                if (parent != this) continue;
                return elem;
            }
        }
        return null;
    }

    public void removeIdentifier(String idName) {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (this.identifiers == null) {
            return;
        }
        this.identifiers.remove(idName);
    }

    @Override
    public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
        return new ElementNSImpl(this, namespaceURI, qualifiedName);
    }

    public Element createElementNS(String namespaceURI, String qualifiedName, String localpart) throws DOMException {
        return new ElementNSImpl(this, namespaceURI, qualifiedName, localpart);
    }

    @Override
    public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
        return new AttrNSImpl(this, namespaceURI, qualifiedName);
    }

    public Attr createAttributeNS(String namespaceURI, String qualifiedName, String localpart) throws DOMException {
        return new AttrNSImpl(this, namespaceURI, qualifiedName, localpart);
    }

    @Override
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return new DeepNodeListImpl(this, namespaceURI, localName);
    }

    public Object clone() throws CloneNotSupportedException {
        CoreDocumentImpl newdoc = (CoreDocumentImpl)super.clone();
        newdoc.docType = null;
        newdoc.docElement = null;
        return newdoc;
    }

    public static final boolean isXMLName(String s, boolean xml11Version) {
        if (s == null) {
            return false;
        }
        if (!xml11Version) {
            return XMLChar.isValidName(s);
        }
        return XML11Char.isXML11ValidName(s);
    }

    public static final boolean isValidQName(String prefix, String local, boolean xml11Version) {
        if (local == null) {
            return false;
        }
        boolean validNCName = false;
        validNCName = !xml11Version ? (prefix == null || XMLChar.isValidNCName(prefix)) && XMLChar.isValidNCName(local) : (prefix == null || XML11Char.isXML11ValidNCName(prefix)) && XML11Char.isXML11ValidNCName(local);
        return validNCName;
    }

    protected boolean isKidOK(Node parent, Node child) {
        if (this.allowGrammarAccess && parent.getNodeType() == 10) {
            return child.getNodeType() == 1;
        }
        return 0 != (kidOK[parent.getNodeType()] & 1 << child.getNodeType());
    }

    @Override
    protected void changed() {
        ++this.changes;
    }

    @Override
    protected int changes() {
        return this.changes;
    }

    NodeListCache getNodeListCache(ParentNode owner) {
        if (this.fFreeNLCache == null) {
            return new NodeListCache(owner);
        }
        NodeListCache c = this.fFreeNLCache;
        this.fFreeNLCache = this.fFreeNLCache.next;
        c.fChild = null;
        c.fChildIndex = -1;
        c.fLength = -1;
        if (c.fOwner != null) {
            c.fOwner.fNodeListCache = null;
        }
        c.fOwner = owner;
        return c;
    }

    void freeNodeListCache(NodeListCache c) {
        c.next = this.fFreeNLCache;
        this.fFreeNLCache = c;
    }

    public Object setUserData(Node n, String key, Object data, UserDataHandler handler) {
        Map<String, ParentNode.UserDataRecord> t;
        if (data == null) {
            ParentNode.UserDataRecord r;
            Map<String, ParentNode.UserDataRecord> t2;
            if (this.nodeUserData != null && (t2 = this.nodeUserData.get(n)) != null && (r = t2.remove(key)) != null) {
                return r.fData;
            }
            return null;
        }
        if (this.nodeUserData == null) {
            this.nodeUserData = new HashMap<Node, Map<String, ParentNode.UserDataRecord>>();
            t = new HashMap<String, ParentNode.UserDataRecord>();
            this.nodeUserData.put(n, t);
        } else {
            t = this.nodeUserData.get(n);
            if (t == null) {
                t = new HashMap<String, ParentNode.UserDataRecord>();
                this.nodeUserData.put(n, t);
            }
        }
        ParentNode.UserDataRecord r = t.put(key, new ParentNode.UserDataRecord(this, data, handler));
        if (r != null) {
            return r.fData;
        }
        return null;
    }

    public Object getUserData(Node n, String key) {
        if (this.nodeUserData == null) {
            return null;
        }
        Map<String, ParentNode.UserDataRecord> t = this.nodeUserData.get(n);
        if (t == null) {
            return null;
        }
        ParentNode.UserDataRecord r = t.get(key);
        if (r != null) {
            return r.fData;
        }
        return null;
    }

    protected Map<String, ParentNode.UserDataRecord> getUserDataRecord(Node n) {
        if (this.nodeUserData == null) {
            return null;
        }
        Map<String, ParentNode.UserDataRecord> t = this.nodeUserData.get(n);
        if (t == null) {
            return null;
        }
        return t;
    }

    Map<String, ParentNode.UserDataRecord> removeUserDataTable(Node n) {
        if (this.nodeUserData == null) {
            return null;
        }
        return this.nodeUserData.get(n);
    }

    void setUserDataTable(Node n, Map<String, ParentNode.UserDataRecord> data) {
        if (this.nodeUserData == null) {
            this.nodeUserData = new HashMap<Node, Map<String, ParentNode.UserDataRecord>>();
        }
        if (data != null) {
            this.nodeUserData.put(n, data);
        }
    }

    void callUserDataHandlers(Node n, Node c, short operation) {
        if (this.nodeUserData == null) {
            return;
        }
        if (n instanceof NodeImpl) {
            Map<String, ParentNode.UserDataRecord> t = ((NodeImpl)n).getUserDataRecord();
            if (t == null || t.isEmpty()) {
                return;
            }
            this.callUserDataHandlers(n, c, operation, t);
        }
    }

    void callUserDataHandlers(Node n, Node c, short operation, Map<String, ParentNode.UserDataRecord> userData) {
        if (userData == null || userData.isEmpty()) {
            return;
        }
        userData.keySet().stream().forEach(key -> {
            ParentNode.UserDataRecord r = (ParentNode.UserDataRecord)userData.get(key);
            if (r.fHandler != null) {
                r.fHandler.handle(operation, (String)key, r.fData, n, c);
            }
        });
    }

    protected final void checkNamespaceWF(String qname, int colon1, int colon2) {
        if (!this.errorChecking) {
            return;
        }
        if (colon1 == 0 || colon1 == qname.length() - 1 || colon2 != colon1) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NAMESPACE_ERR", null);
            throw new DOMException(14, msg);
        }
    }

    protected final void checkDOMNSErr(String prefix, String namespace) {
        if (this.errorChecking) {
            if (namespace == null) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NAMESPACE_ERR", null);
                throw new DOMException(14, msg);
            }
            if (prefix.equals("xml") && !namespace.equals(NamespaceContext.XML_URI)) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NAMESPACE_ERR", null);
                throw new DOMException(14, msg);
            }
            if (prefix.equals("xmlns") && !namespace.equals(NamespaceContext.XMLNS_URI) || !prefix.equals("xmlns") && namespace.equals(NamespaceContext.XMLNS_URI)) {
                String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NAMESPACE_ERR", null);
                throw new DOMException(14, msg);
            }
        }
    }

    protected final void checkQName(String prefix, String local) {
        if (!this.errorChecking) {
            return;
        }
        boolean validNCName = false;
        if (!this.xml11Version) {
            validNCName = (prefix == null || XMLChar.isValidNCName(prefix)) && XMLChar.isValidNCName(local);
        } else {
            boolean bl = validNCName = (prefix == null || XML11Char.isXML11ValidNCName(prefix)) && XML11Char.isXML11ValidNCName(local);
        }
        if (!validNCName) {
            String msg = DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "INVALID_CHARACTER_ERR", null);
            throw new DOMException(5, msg);
        }
    }

    boolean isXML11Version() {
        return this.xml11Version;
    }

    boolean isNormalizeDocRequired() {
        return true;
    }

    boolean isXMLVersionChanged() {
        return this.xmlVersionChanged;
    }

    protected void setUserData(NodeImpl n, Object data) {
        this.setUserData(n, "XERCES1DOMUSERDATA", data, null);
    }

    protected Object getUserData(NodeImpl n) {
        return this.getUserData(n, "XERCES1DOMUSERDATA");
    }

    protected void addEventListener(NodeImpl node, String type, EventListener listener, boolean useCapture) {
    }

    protected void removeEventListener(NodeImpl node, String type, EventListener listener, boolean useCapture) {
    }

    protected void copyEventListeners(NodeImpl src, NodeImpl tgt) {
    }

    protected boolean dispatchEvent(NodeImpl node, Event event) {
        return false;
    }

    void replacedText(NodeImpl node) {
    }

    void deletedText(NodeImpl node, int offset, int count) {
    }

    void insertedText(NodeImpl node, int offset, int count) {
    }

    void modifyingCharacterData(NodeImpl node, boolean replace) {
    }

    void modifiedCharacterData(NodeImpl node, String oldvalue, String value, boolean replace) {
    }

    void insertingNode(NodeImpl node, boolean replace) {
    }

    void insertedNode(NodeImpl node, NodeImpl newInternal, boolean replace) {
    }

    void removingNode(NodeImpl node, NodeImpl oldChild, boolean replace) {
    }

    void removedNode(NodeImpl node, boolean replace) {
    }

    void replacingNode(NodeImpl node) {
    }

    void replacedNode(NodeImpl node) {
    }

    void replacingData(NodeImpl node) {
    }

    void replacedCharacterData(NodeImpl node, String oldvalue, String value) {
    }

    void modifiedAttrValue(AttrImpl attr, String oldvalue) {
    }

    void setAttrNode(AttrImpl attr, AttrImpl previous) {
    }

    void removedAttrNode(AttrImpl attr, NodeImpl oldOwner, String name) {
    }

    void renamedAttrNode(Attr oldAt, Attr newAt) {
    }

    void renamedElement(Element oldEl, Element newEl) {
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        Hashtable<Node, Hashtable<String, ParentNode.UserDataRecord>> nud = null;
        if (this.nodeUserData != null) {
            nud = new Hashtable<Node, Hashtable<String, ParentNode.UserDataRecord>>();
            for (Map.Entry<Node, Map<String, ParentNode.UserDataRecord>> e : this.nodeUserData.entrySet()) {
                nud.put(e.getKey(), new Hashtable<String, ParentNode.UserDataRecord>(e.getValue()));
            }
        }
        Hashtable<String, Node> ids = this.identifiers == null ? null : new Hashtable<String, Node>(this.identifiers);
        Hashtable<Node, Integer> nt = this.nodeTable == null ? null : new Hashtable<Node, Integer>(this.nodeTable);
        ObjectOutputStream.PutField pf = out.putFields();
        pf.put("docType", this.docType);
        pf.put("docElement", this.docElement);
        pf.put("fFreeNLCache", this.fFreeNLCache);
        pf.put("encoding", this.encoding);
        pf.put("actualEncoding", this.actualEncoding);
        pf.put("version", this.version);
        pf.put("standalone", this.standalone);
        pf.put("fDocumentURI", this.fDocumentURI);
        pf.put("userData", nud);
        pf.put("identifiers", ids);
        pf.put("changes", this.changes);
        pf.put("allowGrammarAccess", this.allowGrammarAccess);
        pf.put("errorChecking", this.errorChecking);
        pf.put("ancestorChecking", this.ancestorChecking);
        pf.put("xmlVersionChanged", this.xmlVersionChanged);
        pf.put("documentNumber", this.documentNumber);
        pf.put("nodeCounter", this.nodeCounter);
        pf.put("nodeTable", nt);
        pf.put("xml11Version", this.xml11Version);
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField gf = in.readFields();
        this.docType = (DocumentTypeImpl)gf.get("docType", null);
        this.docElement = (ElementImpl)gf.get("docElement", null);
        this.fFreeNLCache = (NodeListCache)gf.get("fFreeNLCache", null);
        this.encoding = (String)gf.get("encoding", null);
        this.actualEncoding = (String)gf.get("actualEncoding", null);
        this.version = (String)gf.get("version", null);
        this.standalone = gf.get("standalone", false);
        this.fDocumentURI = (String)gf.get("fDocumentURI", null);
        Hashtable nud = (Hashtable)gf.get("userData", null);
        Hashtable ids = (Hashtable)gf.get("identifiers", null);
        this.changes = gf.get("changes", 0);
        this.allowGrammarAccess = gf.get("allowGrammarAccess", false);
        this.errorChecking = gf.get("errorChecking", true);
        this.ancestorChecking = gf.get("ancestorChecking", true);
        this.xmlVersionChanged = gf.get("xmlVersionChanged", false);
        this.documentNumber = gf.get("documentNumber", 0);
        this.nodeCounter = gf.get("nodeCounter", 0);
        Hashtable nt = (Hashtable)gf.get("nodeTable", null);
        this.xml11Version = gf.get("xml11Version", false);
        if (nud != null) {
            this.nodeUserData = new HashMap<Node, Map<String, ParentNode.UserDataRecord>>();
            for (Map.Entry e : nud.entrySet()) {
                this.nodeUserData.put((Node)e.getKey(), new HashMap((Map)e.getValue()));
            }
        }
        if (ids != null) {
            this.identifiers = new HashMap<String, Node>(ids);
        }
        if (nt != null) {
            this.nodeTable = new HashMap<Node, Integer>(nt);
        }
    }

    static {
        CoreDocumentImpl.kidOK[9] = 1410;
        CoreDocumentImpl.kidOK[1] = 442;
        CoreDocumentImpl.kidOK[5] = 442;
        CoreDocumentImpl.kidOK[6] = 442;
        CoreDocumentImpl.kidOK[11] = 442;
        CoreDocumentImpl.kidOK[2] = 40;
        CoreDocumentImpl.kidOK[12] = 0;
        CoreDocumentImpl.kidOK[4] = 0;
        CoreDocumentImpl.kidOK[3] = 0;
        CoreDocumentImpl.kidOK[8] = 0;
        CoreDocumentImpl.kidOK[7] = 0;
        CoreDocumentImpl.kidOK[10] = 0;
        serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("docType", DocumentTypeImpl.class), new ObjectStreamField("docElement", ElementImpl.class), new ObjectStreamField("fFreeNLCache", NodeListCache.class), new ObjectStreamField("encoding", String.class), new ObjectStreamField("actualEncoding", String.class), new ObjectStreamField("version", String.class), new ObjectStreamField("standalone", Boolean.TYPE), new ObjectStreamField("fDocumentURI", String.class), new ObjectStreamField("userData", Hashtable.class), new ObjectStreamField("identifiers", Hashtable.class), new ObjectStreamField("changes", Integer.TYPE), new ObjectStreamField("allowGrammarAccess", Boolean.TYPE), new ObjectStreamField("errorChecking", Boolean.TYPE), new ObjectStreamField("ancestorChecking", Boolean.TYPE), new ObjectStreamField("xmlVersionChanged", Boolean.TYPE), new ObjectStreamField("documentNumber", Integer.TYPE), new ObjectStreamField("nodeCounter", Integer.TYPE), new ObjectStreamField("nodeTable", Hashtable.class), new ObjectStreamField("xml11Version", Boolean.TYPE)};
    }
}

