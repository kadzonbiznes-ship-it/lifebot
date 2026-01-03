/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.dom.ChildNode;
import com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;
import com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import com.sun.org.apache.xerces.internal.dom.NodeImpl;
import com.sun.org.apache.xerces.internal.dom.NodeListCache;
import com.sun.org.apache.xerces.internal.dom.TextImpl;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

public abstract class ParentNode
extends ChildNode {
    static final long serialVersionUID = 2815829867152120872L;
    protected CoreDocumentImpl ownerDocument;
    protected ChildNode firstChild = null;
    protected transient NodeListCache fNodeListCache = null;

    protected ParentNode(CoreDocumentImpl ownerDocument) {
        super(ownerDocument);
        this.ownerDocument = ownerDocument;
    }

    public ParentNode() {
    }

    @Override
    public Node cloneNode(boolean deep) {
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        ParentNode newnode = (ParentNode)super.cloneNode(deep);
        newnode.ownerDocument = this.ownerDocument;
        newnode.firstChild = null;
        newnode.fNodeListCache = null;
        if (deep) {
            ChildNode child = this.firstChild;
            while (child != null) {
                newnode.appendChild(child.cloneNode(true));
                child = child.nextSibling;
            }
        }
        return newnode;
    }

    @Override
    public Document getOwnerDocument() {
        return this.ownerDocument;
    }

    @Override
    CoreDocumentImpl ownerDocument() {
        return this.ownerDocument;
    }

    @Override
    protected void setOwnerDocument(CoreDocumentImpl doc) {
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        super.setOwnerDocument(doc);
        this.ownerDocument = doc;
        ChildNode child = this.firstChild;
        while (child != null) {
            child.setOwnerDocument(doc);
            child = child.nextSibling;
        }
    }

    @Override
    public boolean hasChildNodes() {
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        return this.firstChild != null;
    }

    @Override
    public NodeList getChildNodes() {
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        return this;
    }

    @Override
    public Node getFirstChild() {
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        return this.firstChild;
    }

    @Override
    public Node getLastChild() {
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        return this.lastChild();
    }

    final ChildNode lastChild() {
        return this.firstChild != null ? this.firstChild.previousSibling : null;
    }

    final void lastChild(ChildNode node) {
        if (this.firstChild != null) {
            this.firstChild.previousSibling = node;
        }
    }

    @Override
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return this.internalInsertBefore(newChild, refChild, false);
    }

    Node internalInsertBefore(Node newChild, Node refChild, boolean replace) throws DOMException {
        boolean errorChecking = this.ownerDocument.errorChecking;
        if (newChild.getNodeType() == 11) {
            if (errorChecking) {
                for (Node kid = newChild.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
                    if (this.ownerDocument.isKidOK(this, kid)) continue;
                    throw new DOMException(3, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "HIERARCHY_REQUEST_ERR", null));
                }
            }
            while (newChild.hasChildNodes()) {
                this.insertBefore(newChild.getFirstChild(), refChild);
            }
            return newChild;
        }
        if (newChild == refChild) {
            refChild = refChild.getNextSibling();
            this.removeChild(newChild);
            this.insertBefore(newChild, refChild);
            return newChild;
        }
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        if (errorChecking) {
            if (this.isReadOnly()) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null));
            }
            if (newChild.getOwnerDocument() != this.ownerDocument && newChild != this.ownerDocument) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "WRONG_DOCUMENT_ERR", null));
            }
            if (!this.ownerDocument.isKidOK(this, newChild)) {
                throw new DOMException(3, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "HIERARCHY_REQUEST_ERR", null));
            }
            if (refChild != null && refChild.getParentNode() != this) {
                throw new DOMException(8, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_FOUND_ERR", null));
            }
            boolean treeSafe = true;
            for (NodeImpl a = this; treeSafe && a != null; a = ((NodeImpl)a).parentNode()) {
                treeSafe = newChild != a;
            }
            if (!treeSafe) {
                throw new DOMException(3, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "HIERARCHY_REQUEST_ERR", null));
            }
        }
        this.ownerDocument.insertingNode(this, replace);
        ChildNode newInternal = (ChildNode)newChild;
        NodeImpl oldparent = newInternal.parentNode();
        if (oldparent != null) {
            oldparent.removeChild(newInternal);
        }
        ChildNode refInternal = (ChildNode)refChild;
        newInternal.ownerNode = this;
        newInternal.isOwned(true);
        if (this.firstChild == null) {
            this.firstChild = newInternal;
            newInternal.isFirstChild(true);
            newInternal.previousSibling = newInternal;
        } else if (refInternal == null) {
            ChildNode lastChild = this.firstChild.previousSibling;
            lastChild.nextSibling = newInternal;
            newInternal.previousSibling = lastChild;
            this.firstChild.previousSibling = newInternal;
        } else if (refChild == this.firstChild) {
            this.firstChild.isFirstChild(false);
            newInternal.nextSibling = this.firstChild;
            newInternal.previousSibling = this.firstChild.previousSibling;
            this.firstChild.previousSibling = newInternal;
            this.firstChild = newInternal;
            newInternal.isFirstChild(true);
        } else {
            ChildNode prev = refInternal.previousSibling;
            newInternal.nextSibling = refInternal;
            prev.nextSibling = newInternal;
            refInternal.previousSibling = newInternal;
            newInternal.previousSibling = prev;
        }
        this.changed();
        if (this.fNodeListCache != null) {
            if (this.fNodeListCache.fLength != -1) {
                ++this.fNodeListCache.fLength;
            }
            if (this.fNodeListCache.fChildIndex != -1) {
                if (this.fNodeListCache.fChild == refInternal) {
                    this.fNodeListCache.fChild = newInternal;
                } else {
                    this.fNodeListCache.fChildIndex = -1;
                }
            }
        }
        this.ownerDocument.insertedNode(this, newInternal, replace);
        this.checkNormalizationAfterInsert(newInternal);
        return newChild;
    }

    @Override
    public Node removeChild(Node oldChild) throws DOMException {
        return this.internalRemoveChild(oldChild, false);
    }

    Node internalRemoveChild(Node oldChild, boolean replace) throws DOMException {
        CoreDocumentImpl ownerDocument = this.ownerDocument();
        if (ownerDocument.errorChecking) {
            if (this.isReadOnly()) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null));
            }
            if (oldChild != null && oldChild.getParentNode() != this) {
                throw new DOMException(8, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NOT_FOUND_ERR", null));
            }
        }
        ChildNode oldInternal = (ChildNode)oldChild;
        ownerDocument.removingNode(this, oldInternal, replace);
        ChildNode oldPreviousSibling = oldInternal.previousSibling();
        if (this.fNodeListCache != null) {
            if (this.fNodeListCache.fLength != -1) {
                --this.fNodeListCache.fLength;
            }
            if (this.fNodeListCache.fChildIndex != -1) {
                if (this.fNodeListCache.fChild == oldInternal) {
                    --this.fNodeListCache.fChildIndex;
                    this.fNodeListCache.fChild = oldPreviousSibling;
                } else {
                    this.fNodeListCache.fChildIndex = -1;
                }
            }
        }
        if (oldInternal == this.firstChild) {
            oldInternal.isFirstChild(false);
            this.firstChild = oldInternal.nextSibling;
            if (this.firstChild != null) {
                this.firstChild.isFirstChild(true);
                this.firstChild.previousSibling = oldInternal.previousSibling;
            }
        } else {
            ChildNode next;
            ChildNode prev = oldInternal.previousSibling;
            prev.nextSibling = next = oldInternal.nextSibling;
            if (next == null) {
                this.firstChild.previousSibling = prev;
            } else {
                next.previousSibling = prev;
            }
        }
        oldInternal.ownerNode = ownerDocument;
        oldInternal.isOwned(false);
        oldInternal.nextSibling = null;
        oldInternal.previousSibling = null;
        this.changed();
        ownerDocument.removedNode(this, replace);
        this.checkNormalizationAfterRemove(oldPreviousSibling);
        return oldInternal;
    }

    @Override
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        this.ownerDocument.replacingNode(this);
        this.internalInsertBefore(newChild, oldChild, true);
        if (newChild != oldChild) {
            this.internalRemoveChild(oldChild, true);
        }
        this.ownerDocument.replacedNode(this);
        return oldChild;
    }

    @Override
    public String getTextContent() throws DOMException {
        Node child = this.getFirstChild();
        if (child != null) {
            Node next = child.getNextSibling();
            if (next == null) {
                return this.hasTextContent(child) ? ((NodeImpl)child).getTextContent() : "";
            }
            StringBuilder buf = new StringBuilder();
            this.getTextContent(buf);
            return buf.toString();
        }
        return "";
    }

    @Override
    void getTextContent(StringBuilder buf) throws DOMException {
        for (Node child = this.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (!this.hasTextContent(child)) continue;
            ((NodeImpl)child).getTextContent(buf);
        }
    }

    final boolean hasTextContent(Node child) {
        return child.getNodeType() != 8 && child.getNodeType() != 7 && (child.getNodeType() != 3 || !((TextImpl)child).isIgnorableWhitespace());
    }

    @Override
    public void setTextContent(String textContent) throws DOMException {
        Node child;
        while ((child = this.getFirstChild()) != null) {
            this.removeChild(child);
        }
        if (textContent != null && textContent.length() != 0) {
            this.appendChild(this.ownerDocument().createTextNode(textContent));
        }
    }

    private int nodeListGetLength() {
        if (this.fNodeListCache == null) {
            if (this.needsSyncChildren()) {
                this.synchronizeChildren();
            }
            if (this.firstChild == null) {
                return 0;
            }
            if (this.firstChild == this.lastChild()) {
                return 1;
            }
            this.fNodeListCache = this.ownerDocument.getNodeListCache(this);
        }
        if (this.fNodeListCache.fLength == -1) {
            ChildNode n;
            int l;
            if (this.fNodeListCache.fChildIndex != -1 && this.fNodeListCache.fChild != null) {
                l = this.fNodeListCache.fChildIndex;
                n = this.fNodeListCache.fChild;
            } else {
                n = this.firstChild;
                l = 0;
            }
            while (n != null) {
                ++l;
                n = n.nextSibling;
            }
            this.fNodeListCache.fLength = l;
        }
        return this.fNodeListCache.fLength;
    }

    @Override
    public int getLength() {
        return this.nodeListGetLength();
    }

    private Node nodeListItem(int index) {
        ChildNode n;
        int i;
        if (this.fNodeListCache == null) {
            if (this.needsSyncChildren()) {
                this.synchronizeChildren();
            }
            if (this.firstChild == this.lastChild()) {
                return index == 0 ? this.firstChild : null;
            }
            this.fNodeListCache = this.ownerDocument.getNodeListCache(this);
        }
        boolean firstAccess = true;
        if (i != -1 && n != null) {
            firstAccess = false;
            if (i < index) {
                for (i = this.fNodeListCache.fChildIndex; i < index && n != null; ++i) {
                    n = n.nextSibling;
                }
            } else if (i > index) {
                for (n = this.fNodeListCache.fChild; i > index && n != null; --i, n = n.previousSibling()) {
                }
            }
        } else {
            if (index < 0) {
                return null;
            }
            n = this.firstChild;
            for (i = 0; i < index && n != null; ++i) {
                n = n.nextSibling;
            }
        }
        if (!(firstAccess || n != this.firstChild && n != this.lastChild())) {
            this.fNodeListCache.fChildIndex = -1;
            this.fNodeListCache.fChild = null;
            this.ownerDocument.freeNodeListCache(this.fNodeListCache);
        } else {
            this.fNodeListCache.fChildIndex = i;
            this.fNodeListCache.fChild = n;
        }
        return n;
    }

    @Override
    public Node item(int index) {
        return this.nodeListItem(index);
    }

    protected final NodeList getChildNodesUnoptimized() {
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        return new NodeList(){

            @Override
            public int getLength() {
                return ParentNode.this.nodeListGetLength();
            }

            @Override
            public Node item(int index) {
                return ParentNode.this.nodeListItem(index);
            }
        };
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
            kid.normalize();
            kid = kid.nextSibling;
        }
        this.isNormalized(true);
    }

    @Override
    public boolean isEqualNode(Node arg) {
        Node child2;
        if (!super.isEqualNode(arg)) {
            return false;
        }
        Node child1 = this.getFirstChild();
        for (child2 = arg.getFirstChild(); child1 != null && child2 != null; child1 = child1.getNextSibling(), child2 = child2.getNextSibling()) {
            if (child1.isEqualNode(child2)) continue;
            return false;
        }
        return child1 == child2;
    }

    @Override
    public void setReadOnly(boolean readOnly, boolean deep) {
        super.setReadOnly(readOnly, deep);
        if (deep) {
            if (this.needsSyncChildren()) {
                this.synchronizeChildren();
            }
            ChildNode mykid = this.firstChild;
            while (mykid != null) {
                if (mykid.getNodeType() != 5) {
                    mykid.setReadOnly(readOnly, true);
                }
                mykid = mykid.nextSibling;
            }
        }
    }

    protected void synchronizeChildren() {
        this.needsSyncChildren(false);
    }

    void checkNormalizationAfterInsert(ChildNode insertedChild) {
        if (insertedChild.getNodeType() == 3) {
            ChildNode prev = insertedChild.previousSibling();
            ChildNode next = insertedChild.nextSibling;
            if (prev != null && prev.getNodeType() == 3 || next != null && next.getNodeType() == 3) {
                this.isNormalized(false);
            }
        } else if (!insertedChild.isNormalized()) {
            this.isNormalized(false);
        }
    }

    void checkNormalizationAfterRemove(ChildNode previousSibling) {
        ChildNode next;
        if (previousSibling != null && previousSibling.getNodeType() == 3 && (next = previousSibling.nextSibling) != null && next.getNodeType() == 3) {
            this.isNormalized(false);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (this.needsSyncChildren()) {
            this.synchronizeChildren();
        }
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.needsSyncChildren(false);
    }

    class UserDataRecord
    implements Serializable {
        private static final long serialVersionUID = 3258126977134310455L;
        Object fData;
        UserDataHandler fHandler;

        UserDataRecord(Object data, UserDataHandler handler) {
            this.fData = data;
            this.fHandler = handler;
        }
    }
}

