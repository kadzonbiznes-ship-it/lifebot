/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.dom.CoreDOMImplementationImpl;
import com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;
import com.sun.org.apache.xerces.internal.dom.NamedNodeMapImpl;
import com.sun.org.apache.xerces.internal.dom.NodeImpl;
import com.sun.org.apache.xerces.internal.dom.ParentNode;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;

public class DocumentTypeImpl
extends ParentNode
implements DocumentType {
    static final long serialVersionUID = 7751299192316526485L;
    protected String name;
    protected NamedNodeMapImpl entities;
    protected NamedNodeMapImpl notations;
    protected NamedNodeMapImpl elements;
    protected String publicID;
    protected String systemID;
    protected String internalSubset;
    private int doctypeNumber = 0;
    private Map<String, ParentNode.UserDataRecord> userData = null;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("name", String.class), new ObjectStreamField("entities", NamedNodeMapImpl.class), new ObjectStreamField("notations", NamedNodeMapImpl.class), new ObjectStreamField("elements", NamedNodeMapImpl.class), new ObjectStreamField("publicID", String.class), new ObjectStreamField("systemID", String.class), new ObjectStreamField("internalSubset", String.class), new ObjectStreamField("doctypeNumber", Integer.TYPE), new ObjectStreamField("userData", Hashtable.class)};

    public DocumentTypeImpl(CoreDocumentImpl ownerDocument, String name) {
        super(ownerDocument);
        this.name = name;
        this.entities = new NamedNodeMapImpl(this);
        this.notations = new NamedNodeMapImpl(this);
        this.elements = new NamedNodeMapImpl(this);
    }

    public DocumentTypeImpl(CoreDocumentImpl ownerDocument, String qualifiedName, String publicID, String systemID) {
        this(ownerDocument, qualifiedName);
        this.publicID = publicID;
        this.systemID = systemID;
    }

    @Override
    public String getPublicId() {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        return this.publicID;
    }

    @Override
    public String getSystemId() {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        return this.systemID;
    }

    public void setInternalSubset(String internalSubset) {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        this.internalSubset = internalSubset;
    }

    @Override
    public String getInternalSubset() {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        return this.internalSubset;
    }

    @Override
    public short getNodeType() {
        return 10;
    }

    @Override
    public String getNodeName() {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        return this.name;
    }

    @Override
    public Node cloneNode(boolean deep) {
        DocumentTypeImpl newnode = (DocumentTypeImpl)super.cloneNode(deep);
        newnode.entities = this.entities.cloneMap(newnode);
        newnode.notations = this.notations.cloneMap(newnode);
        newnode.elements = this.elements.cloneMap(newnode);
        return newnode;
    }

    @Override
    public String getTextContent() throws DOMException {
        return null;
    }

    @Override
    public void setTextContent(String textContent) throws DOMException {
    }

    @Override
    public boolean isEqualNode(Node arg) {
        if (!super.isEqualNode(arg)) {
            return false;
        }
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        DocumentTypeImpl argDocType = (DocumentTypeImpl)arg;
        if (this.getPublicId() == null && argDocType.getPublicId() != null || this.getPublicId() != null && argDocType.getPublicId() == null || this.getSystemId() == null && argDocType.getSystemId() != null || this.getSystemId() != null && argDocType.getSystemId() == null || this.getInternalSubset() == null && argDocType.getInternalSubset() != null || this.getInternalSubset() != null && argDocType.getInternalSubset() == null) {
            return false;
        }
        if (this.getPublicId() != null && !this.getPublicId().equals(argDocType.getPublicId())) {
            return false;
        }
        if (this.getSystemId() != null && !this.getSystemId().equals(argDocType.getSystemId())) {
            return false;
        }
        if (this.getInternalSubset() != null && !this.getInternalSubset().equals(argDocType.getInternalSubset())) {
            return false;
        }
        NamedNodeMapImpl argEntities = argDocType.entities;
        if (this.entities == null && argEntities != null || this.entities != null && argEntities == null) {
            return false;
        }
        if (this.entities != null && argEntities != null) {
            if (this.entities.getLength() != argEntities.getLength()) {
                return false;
            }
            int index = 0;
            while (this.entities.item(index) != null) {
                Node entNode2;
                Node entNode1 = this.entities.item(index);
                if (!((NodeImpl)entNode1).isEqualNode(entNode2 = argEntities.getNamedItem(entNode1.getNodeName()))) {
                    return false;
                }
                ++index;
            }
        }
        NamedNodeMapImpl argNotations = argDocType.notations;
        if (this.notations == null && argNotations != null || this.notations != null && argNotations == null) {
            return false;
        }
        if (this.notations != null && argNotations != null) {
            if (this.notations.getLength() != argNotations.getLength()) {
                return false;
            }
            int index = 0;
            while (this.notations.item(index) != null) {
                Node noteNode2;
                Node noteNode1 = this.notations.item(index);
                if (!((NodeImpl)noteNode1).isEqualNode(noteNode2 = argNotations.getNamedItem(noteNode1.getNodeName()))) {
                    return false;
                }
                ++index;
            }
        }
        return true;
    }

    @Override
    protected void setOwnerDocument(CoreDocumentImpl doc) {
        super.setOwnerDocument(doc);
        this.entities.setOwnerDocument(doc);
        this.notations.setOwnerDocument(doc);
        this.elements.setOwnerDocument(doc);
    }

    @Override
    protected int getNodeNumber() {
        if (this.getOwnerDocument() != null) {
            return super.getNodeNumber();
        }
        if (this.doctypeNumber == 0) {
            CoreDOMImplementationImpl cd = (CoreDOMImplementationImpl)CoreDOMImplementationImpl.getDOMImplementation();
            this.doctypeNumber = cd.assignDocTypeNumber();
        }
        return this.doctypeNumber;
    }

    @Override
    public String getName() {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        return this.name;
    }

    @Override
    public NamedNodeMap getEntities() {
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        return this.entities;
    }

    @Override
    public NamedNodeMap getNotations() {
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        return this.notations;
    }

    @Override
    public void setReadOnly(boolean readOnly, boolean deep) {
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        super.setReadOnly(readOnly, deep);
        this.elements.setReadOnly(readOnly, true);
        this.entities.setReadOnly(readOnly, true);
        this.notations.setReadOnly(readOnly, true);
    }

    public NamedNodeMap getElements() {
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        return this.elements;
    }

    @Override
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        if (this.userData == null) {
            this.userData = new HashMap<String, ParentNode.UserDataRecord>();
        }
        if (data == null) {
            ParentNode.UserDataRecord udr;
            if (this.userData != null && (udr = this.userData.remove(key)) != null) {
                return udr.fData;
            }
            return null;
        }
        ParentNode.UserDataRecord udr = this.userData.put(key, new ParentNode.UserDataRecord(this, data, handler));
        if (udr != null) {
            return udr.fData;
        }
        return null;
    }

    @Override
    public Object getUserData(String key) {
        if (this.userData == null) {
            return null;
        }
        ParentNode.UserDataRecord udr = this.userData.get(key);
        if (udr != null) {
            return udr.fData;
        }
        return null;
    }

    @Override
    protected Map<String, ParentNode.UserDataRecord> getUserDataRecord() {
        return this.userData;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        Hashtable<String, ParentNode.UserDataRecord> ud = this.userData == null ? null : new Hashtable<String, ParentNode.UserDataRecord>(this.userData);
        ObjectOutputStream.PutField pf = out.putFields();
        pf.put("name", this.name);
        pf.put("entities", this.entities);
        pf.put("notations", this.notations);
        pf.put("elements", this.elements);
        pf.put("publicID", this.publicID);
        pf.put("systemID", this.systemID);
        pf.put("internalSubset", this.internalSubset);
        pf.put("doctypeNumber", this.doctypeNumber);
        pf.put("userData", ud);
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField gf = in.readFields();
        this.name = (String)gf.get("name", null);
        this.entities = (NamedNodeMapImpl)gf.get("entities", null);
        this.notations = (NamedNodeMapImpl)gf.get("notations", null);
        this.elements = (NamedNodeMapImpl)gf.get("elements", null);
        this.publicID = (String)gf.get("publicID", null);
        this.systemID = (String)gf.get("systemID", null);
        this.internalSubset = (String)gf.get("internalSubset", null);
        this.doctypeNumber = gf.get("doctypeNumber", 0);
        Hashtable ud = (Hashtable)gf.get("userData", null);
        if (ud != null) {
            this.userData = new HashMap<String, ParentNode.UserDataRecord>(ud);
        }
    }
}

