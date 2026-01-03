/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl;
import com.sun.org.apache.xerces.internal.dom.DeferredNode;
import com.sun.org.apache.xerces.internal.dom.TextImpl;

public class DeferredTextImpl
extends TextImpl
implements DeferredNode {
    static final long serialVersionUID = 2310613872100393425L;
    protected transient int fNodeIndex;

    DeferredTextImpl(DeferredDocumentImpl ownerDocument, int nodeIndex) {
        super(ownerDocument, null);
        this.fNodeIndex = nodeIndex;
        this.needsSyncData(true);
    }

    @Override
    public int getNodeIndex() {
        return this.fNodeIndex;
    }

    @Override
    protected void synchronizeData() {
        this.needsSyncData(false);
        DeferredDocumentImpl ownerDocument = (DeferredDocumentImpl)this.ownerDocument();
        this.data = ownerDocument.getNodeValueString(this.fNodeIndex);
        this.isIgnorableWhitespace(ownerDocument.getNodeExtra(this.fNodeIndex) == 1);
    }
}

