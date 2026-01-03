/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;
import com.sun.org.apache.xerces.internal.dom.NodeImpl;
import org.w3c.dom.Node;

public abstract class ChildNode
extends NodeImpl {
    static final long serialVersionUID = -6112455738802414002L;
    protected ChildNode previousSibling;
    protected ChildNode nextSibling;

    protected ChildNode(CoreDocumentImpl ownerDocument) {
        super(ownerDocument);
    }

    public ChildNode() {
    }

    @Override
    public Node cloneNode(boolean deep) {
        ChildNode newnode = (ChildNode)super.cloneNode(deep);
        newnode.previousSibling = null;
        newnode.nextSibling = null;
        newnode.isFirstChild(false);
        return newnode;
    }

    @Override
    public Node getParentNode() {
        return this.isOwned() ? this.ownerNode : null;
    }

    @Override
    final NodeImpl parentNode() {
        return this.isOwned() ? this.ownerNode : null;
    }

    @Override
    public Node getNextSibling() {
        return this.nextSibling;
    }

    @Override
    public Node getPreviousSibling() {
        return this.isFirstChild() ? null : this.previousSibling;
    }

    @Override
    final ChildNode previousSibling() {
        return this.isFirstChild() ? null : this.previousSibling;
    }
}

