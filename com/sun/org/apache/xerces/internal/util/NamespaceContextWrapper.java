/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import java.util.Iterator;
import java.util.List;

public class NamespaceContextWrapper
implements javax.xml.namespace.NamespaceContext {
    private NamespaceContext fNamespaceContext;

    public NamespaceContextWrapper(NamespaceSupport namespaceContext) {
        this.fNamespaceContext = namespaceContext;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix can't be null");
        }
        return this.fNamespaceContext.getURI(prefix.intern());
    }

    @Override
    public String getPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("URI can't be null.");
        }
        return this.fNamespaceContext.getPrefix(namespaceURI.intern());
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("URI can't be null.");
        }
        List<String> vector = ((NamespaceSupport)this.fNamespaceContext).getPrefixes(namespaceURI.intern());
        return vector.iterator();
    }

    public NamespaceContext getNamespaceContext() {
        return this.fNamespaceContext;
    }
}

