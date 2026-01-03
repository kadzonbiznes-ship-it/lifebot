/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.spi;

import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.imageio.spi.ServiceRegistry;

class FilterIterator<T>
implements Iterator<T> {
    private Iterator<? extends T> iter;
    private ServiceRegistry.Filter filter;
    private T next = null;

    public FilterIterator(Iterator<? extends T> iter, ServiceRegistry.Filter filter) {
        this.iter = iter;
        this.filter = filter;
        this.advance();
    }

    private void advance() {
        while (this.iter.hasNext()) {
            T elt = this.iter.next();
            if (!this.filter.filter(elt)) continue;
            this.next = elt;
            return;
        }
        this.next = null;
    }

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    @Override
    public T next() {
        if (this.next == null) {
            throw new NoSuchElementException();
        }
        T o = this.next;
        this.advance();
        return o;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

