/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.dom.ElementImpl;
import com.sun.org.apache.xerces.internal.dom.NodeImpl;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DeepNodeListImpl
implements NodeList {
    protected NodeImpl rootNode;
    protected String tagName;
    protected int changes = 0;
    protected List<Node> nodes;
    protected String nsName;
    protected boolean enableNS = false;

    public DeepNodeListImpl(NodeImpl rootNode, String tagName) {
        this.rootNode = rootNode;
        this.tagName = tagName;
        this.nodes = new ArrayList<Node>();
    }

    public DeepNodeListImpl(NodeImpl rootNode, String nsName, String tagName) {
        this(rootNode, tagName);
        this.nsName = nsName != null && nsName.length() != 0 ? nsName : null;
        this.enableNS = true;
    }

    @Override
    public int getLength() {
        this.item(Integer.MAX_VALUE);
        return this.nodes.size();
    }

    @Override
    public Node item(int index) {
        int currentSize;
        if (this.rootNode.changes() != this.changes) {
            this.nodes = new ArrayList<Node>();
            this.changes = this.rootNode.changes();
        }
        if (index < (currentSize = this.nodes.size())) {
            return this.nodes.get(index);
        }
        Node thisNode = currentSize == 0 ? this.rootNode : (NodeImpl)this.nodes.get(currentSize - 1);
        while (thisNode != null && index >= this.nodes.size()) {
            if ((thisNode = this.nextMatchingElementAfter(thisNode)) == null) continue;
            this.nodes.add(thisNode);
        }
        return thisNode;
    }

    protected Node nextMatchingElementAfter(Node current) {
        while (current != null) {
            ElementImpl el;
            Node next;
            if (current.hasChildNodes()) {
                current = current.getFirstChild();
            } else if (current != this.rootNode && null != (next = current.getNextSibling())) {
                current = next;
            } else {
                next = null;
                while (current != this.rootNode && (next = current.getNextSibling()) == null) {
                    current = current.getParentNode();
                }
                current = next;
            }
            if (current == this.rootNode || current == null || current.getNodeType() != 1) continue;
            if (!this.enableNS) {
                if (!this.tagName.equals("*") && !((ElementImpl)current).getTagName().equals(this.tagName)) continue;
                return current;
            }
            if (this.tagName.equals("*")) {
                if (this.nsName != null && this.nsName.equals("*")) {
                    return current;
                }
                el = (ElementImpl)current;
                if ((this.nsName != null || el.getNamespaceURI() != null) && (this.nsName == null || !this.nsName.equals(el.getNamespaceURI()))) continue;
                return current;
            }
            el = (ElementImpl)current;
            if (el.getLocalName() == null || !el.getLocalName().equals(this.tagName)) continue;
            if (this.nsName != null && this.nsName.equals("*")) {
                return current;
            }
            if ((this.nsName != null || el.getNamespaceURI() != null) && (this.nsName == null || !this.nsName.equals(el.getNamespaceURI()))) continue;
            return current;
        }
        return null;
    }
}

