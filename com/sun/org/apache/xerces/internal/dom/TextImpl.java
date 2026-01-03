/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.dom.CharacterDataImpl;
import com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;
import com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import com.sun.org.apache.xerces.internal.dom.NodeImpl;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class TextImpl
extends CharacterDataImpl
implements CharacterData,
Text {
    static final long serialVersionUID = -5294980852957403469L;

    public TextImpl() {
    }

    public TextImpl(CoreDocumentImpl ownerDoc, String data) {
        super(ownerDoc, data);
    }

    public void setValues(CoreDocumentImpl ownerDoc, String data) {
        this.flags = 0;
        this.nextSibling = null;
        this.previousSibling = null;
        this.setOwnerDocument(ownerDoc);
        this.data = data;
    }

    @Override
    public short getNodeType() {
        return 3;
    }

    @Override
    public String getNodeName() {
        return "#text";
    }

    public void setIgnorableWhitespace(boolean ignore) {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        this.isIgnorableWhitespace(ignore);
    }

    @Override
    public boolean isElementContentWhitespace() {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        return this.internalIsIgnorableWhitespace();
    }

    @Override
    public String getWholeText() {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        StringBuilder buffer = new StringBuilder();
        if (this.data != null && this.data.length() != 0) {
            buffer.append(this.data);
        }
        this.getWholeTextBackward(this.getPreviousSibling(), buffer, this.getParentNode());
        String temp = buffer.toString();
        buffer.setLength(0);
        this.getWholeTextForward(this.getNextSibling(), buffer, this.getParentNode());
        return temp + buffer.toString();
    }

    protected void insertTextContent(StringBuilder buf) throws DOMException {
        String content = this.getNodeValue();
        if (content != null) {
            buf.insert(0, content);
        }
    }

    private boolean getWholeTextForward(Node node, StringBuilder buffer, Node parent) {
        boolean inEntRef = false;
        if (parent != null) {
            boolean bl = inEntRef = parent.getNodeType() == 5;
        }
        while (node != null) {
            short type = node.getNodeType();
            if (type == 5) {
                if (this.getWholeTextForward(node.getFirstChild(), buffer, node)) {
                    return true;
                }
            } else if (type == 3 || type == 4) {
                ((NodeImpl)node).getTextContent(buffer);
            } else {
                return true;
            }
            node = node.getNextSibling();
        }
        if (inEntRef) {
            this.getWholeTextForward(parent.getNextSibling(), buffer, parent.getParentNode());
            return true;
        }
        return false;
    }

    private boolean getWholeTextBackward(Node node, StringBuilder buffer, Node parent) {
        boolean inEntRef = false;
        if (parent != null) {
            boolean bl = inEntRef = parent.getNodeType() == 5;
        }
        while (node != null) {
            short type = node.getNodeType();
            if (type == 5) {
                if (this.getWholeTextBackward(node.getLastChild(), buffer, node)) {
                    return true;
                }
            } else if (type == 3 || type == 4) {
                ((TextImpl)node).insertTextContent(buffer);
            } else {
                return true;
            }
            node = node.getPreviousSibling();
        }
        if (inEntRef) {
            this.getWholeTextBackward(parent.getPreviousSibling(), buffer, parent.getParentNode());
            return true;
        }
        return false;
    }

    /*
     * WARNING - void declaration
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public Text replaceWholeText(String content) throws DOMException {
        void var3_6;
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        Node parent = this.getParentNode();
        if (content == null || content.length() == 0) {
            if (parent == null) return null;
            parent.removeChild(this);
            return null;
        }
        if (this.ownerDocument().errorChecking) {
            if (!this.canModifyPrev(this)) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null));
            }
            if (!this.canModifyNext(this)) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null));
            }
        }
        Object var3_3 = null;
        if (this.isReadOnly()) {
            Text newNode = this.ownerDocument().createTextNode(content);
            if (parent == null) return newNode;
            parent.insertBefore(newNode, this);
            parent.removeChild(this);
            Text text = newNode;
        } else {
            this.setData(content);
            TextImpl textImpl = this;
        }
        for (Node prev = var3_6.getPreviousSibling(); prev != null && (prev.getNodeType() == 3 || prev.getNodeType() == 4 || prev.getNodeType() == 5 && this.hasTextOnlyChildren(prev)); prev = prev.getPreviousSibling()) {
            parent.removeChild(prev);
            prev = var3_6;
        }
        for (Node next = var3_6.getNextSibling(); next != null && (next.getNodeType() == 3 || next.getNodeType() == 4 || next.getNodeType() == 5 && this.hasTextOnlyChildren(next)); next = next.getNextSibling()) {
            parent.removeChild(next);
            next = var3_6;
        }
        return var3_6;
    }

    private boolean canModifyPrev(Node node) {
        boolean textLastChild = false;
        for (Node prev = node.getPreviousSibling(); prev != null; prev = prev.getPreviousSibling()) {
            short type = prev.getNodeType();
            if (type == 5) {
                Node lastChild = prev.getLastChild();
                if (lastChild == null) {
                    return false;
                }
                while (lastChild != null) {
                    short lType = lastChild.getNodeType();
                    if (lType == 3 || lType == 4) {
                        textLastChild = true;
                    } else if (lType == 5) {
                        if (!this.canModifyPrev(lastChild)) {
                            return false;
                        }
                        textLastChild = true;
                    } else {
                        return !textLastChild;
                    }
                    lastChild = lastChild.getPreviousSibling();
                }
                continue;
            }
            if (type == 3 || type == 4) continue;
            return true;
        }
        return true;
    }

    private boolean canModifyNext(Node node) {
        boolean textFirstChild = false;
        for (Node next = node.getNextSibling(); next != null; next = next.getNextSibling()) {
            short type = next.getNodeType();
            if (type == 5) {
                Node firstChild = next.getFirstChild();
                if (firstChild == null) {
                    return false;
                }
                while (firstChild != null) {
                    short lType = firstChild.getNodeType();
                    if (lType == 3 || lType == 4) {
                        textFirstChild = true;
                    } else if (lType == 5) {
                        if (!this.canModifyNext(firstChild)) {
                            return false;
                        }
                        textFirstChild = true;
                    } else {
                        return !textFirstChild;
                    }
                    firstChild = firstChild.getNextSibling();
                }
                continue;
            }
            if (type == 3 || type == 4) continue;
            return true;
        }
        return true;
    }

    private boolean hasTextOnlyChildren(Node node) {
        Node child = node;
        if (child == null) {
            return false;
        }
        for (child = child.getFirstChild(); child != null; child = child.getNextSibling()) {
            short type = child.getNodeType();
            if (type == 5) {
                return this.hasTextOnlyChildren(child);
            }
            if (type == 3 || type == 4 || type == 5) continue;
            return false;
        }
        return true;
    }

    public boolean isIgnorableWhitespace() {
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        return this.internalIsIgnorableWhitespace();
    }

    @Override
    public Text splitText(int offset) throws DOMException {
        if (this.isReadOnly()) {
            throw new DOMException(7, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "NO_MODIFICATION_ALLOWED_ERR", null));
        }
        if (this.needsSyncData()) {
            this.synchronizeData();
        }
        if (offset < 0 || offset > this.data.length()) {
            throw new DOMException(1, DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "INDEX_SIZE_ERR", null));
        }
        Text newText = this.getOwnerDocument().createTextNode(this.data.substring(offset));
        this.setNodeValue(this.data.substring(0, offset));
        Node parentNode = this.getParentNode();
        if (parentNode != null) {
            parentNode.insertBefore(newText, this.nextSibling);
        }
        return newText;
    }

    public void replaceData(String value) {
        this.data = value;
    }

    public String removeData() {
        String olddata = this.data;
        this.data = "";
        return olddata;
    }
}

