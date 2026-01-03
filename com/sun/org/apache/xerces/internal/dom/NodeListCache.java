/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.dom.ChildNode;
import com.sun.org.apache.xerces.internal.dom.ParentNode;
import java.io.Serializable;

class NodeListCache
implements Serializable {
    private static final long serialVersionUID = -7927529254918631002L;
    int fLength = -1;
    int fChildIndex = -1;
    ChildNode fChild;
    ParentNode fOwner;
    NodeListCache next;

    NodeListCache(ParentNode owner) {
        this.fOwner = owner;
    }
}

