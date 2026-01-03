/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl;
import com.sun.org.apache.xerces.internal.dom.DeferredNode;
import com.sun.org.apache.xerces.internal.dom.ElementImpl;
import com.sun.org.apache.xerces.internal.dom.NodeImpl;
import org.w3c.dom.NamedNodeMap;

public class DeferredElementImpl
extends ElementImpl
implements DeferredNode {
    static final long serialVersionUID = -7670981133940934842L;
    protected transient int fNodeIndex;

    DeferredElementImpl(DeferredDocumentImpl ownerDoc, int nodeIndex) {
        super(ownerDoc, null);
        this.fNodeIndex = nodeIndex;
        this.needsSyncChildren(true);
    }

    @Override
    public final int getNodeIndex() {
        return this.fNodeIndex;
    }

    @Override
    protected final void synchronizeData() {
        this.needsSyncData(false);
        DeferredDocumentImpl ownerDocument = (DeferredDocumentImpl)this.ownerDocument;
        boolean orig = ownerDocument.mutationEvents;
        ownerDocument.mutationEvents = false;
        this.name = ownerDocument.getNodeName(this.fNodeIndex);
        this.setupDefaultAttributes();
        int index = ownerDocument.getNodeExtra(this.fNodeIndex);
        if (index != -1) {
            NamedNodeMap attrs = this.getAttributes();
            do {
                NodeImpl attr = (NodeImpl)((Object)ownerDocument.getNodeObject(index));
                attrs.setNamedItem(attr);
            } while ((index = ownerDocument.getPrevSibling(index)) != -1);
        }
        ownerDocument.mutationEvents = orig;
    }

    @Override
    protected final void synchronizeChildren() {
        DeferredDocumentImpl ownerDocument = (DeferredDocumentImpl)this.ownerDocument();
        ownerDocument.synchronizeChildren(this, this.fNodeIndex);
    }
}

