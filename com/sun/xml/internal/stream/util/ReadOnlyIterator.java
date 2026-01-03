/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.internal.stream.util;

import java.util.Iterator;

public class ReadOnlyIterator<T>
implements Iterator<T> {
    Iterator<T> iterator = null;

    public ReadOnlyIterator() {
    }

    public ReadOnlyIterator(Iterator<T> itr) {
        this.iterator = itr;
    }

    @Override
    public boolean hasNext() {
        if (this.iterator != null) {
            return this.iterator.hasNext();
        }
        return false;
    }

    @Override
    public T next() {
        if (this.iterator != null) {
            return this.iterator.next();
        }
        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove operation is not supported");
    }
}

