/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.util.XMLSymbols;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class NamespaceSupport
implements NamespaceContext {
    protected String[] fNamespace = new String[32];
    protected int fNamespaceSize;
    protected int[] fContext = new int[8];
    protected int fCurrentContext;
    protected String[] fPrefixes = new String[16];

    public NamespaceSupport() {
    }

    public NamespaceSupport(NamespaceContext context) {
        this.pushContext();
        Enumeration<String> prefixes = context.getAllPrefixes();
        while (prefixes.hasMoreElements()) {
            String prefix = prefixes.nextElement();
            String uri = context.getURI(prefix);
            this.declarePrefix(prefix, uri);
        }
    }

    @Override
    public void reset() {
        this.fNamespaceSize = 0;
        this.fCurrentContext = 0;
        this.fNamespace[this.fNamespaceSize++] = XMLSymbols.PREFIX_XML;
        this.fNamespace[this.fNamespaceSize++] = NamespaceContext.XML_URI;
        this.fNamespace[this.fNamespaceSize++] = XMLSymbols.PREFIX_XMLNS;
        this.fNamespace[this.fNamespaceSize++] = NamespaceContext.XMLNS_URI;
        this.fContext[this.fCurrentContext] = this.fNamespaceSize;
    }

    @Override
    public void pushContext() {
        if (this.fCurrentContext + 1 == this.fContext.length) {
            int[] contextarray = new int[this.fContext.length * 2];
            System.arraycopy(this.fContext, 0, contextarray, 0, this.fContext.length);
            this.fContext = contextarray;
        }
        this.fContext[++this.fCurrentContext] = this.fNamespaceSize;
    }

    @Override
    public void popContext() {
        this.fNamespaceSize = this.fContext[this.fCurrentContext--];
    }

    @Override
    public boolean declarePrefix(String prefix, String uri) {
        if (prefix == XMLSymbols.PREFIX_XML || prefix == XMLSymbols.PREFIX_XMLNS) {
            return false;
        }
        for (int i = this.fNamespaceSize; i > this.fContext[this.fCurrentContext]; i -= 2) {
            if (this.fNamespace[i - 2] != prefix) continue;
            this.fNamespace[i - 1] = uri;
            return true;
        }
        if (this.fNamespaceSize == this.fNamespace.length) {
            String[] namespacearray = new String[this.fNamespaceSize * 2];
            System.arraycopy(this.fNamespace, 0, namespacearray, 0, this.fNamespaceSize);
            this.fNamespace = namespacearray;
        }
        this.fNamespace[this.fNamespaceSize++] = prefix;
        this.fNamespace[this.fNamespaceSize++] = uri;
        return true;
    }

    @Override
    public String getURI(String prefix) {
        for (int i = this.fNamespaceSize; i > 0; i -= 2) {
            if (this.fNamespace[i - 2] != prefix) continue;
            return this.fNamespace[i - 1];
        }
        return null;
    }

    @Override
    public String getPrefix(String uri) {
        for (int i = this.fNamespaceSize; i > 0; i -= 2) {
            if (this.fNamespace[i - 1] != uri || this.getURI(this.fNamespace[i - 2]) != uri) continue;
            return this.fNamespace[i - 2];
        }
        return null;
    }

    @Override
    public int getDeclaredPrefixCount() {
        return (this.fNamespaceSize - this.fContext[this.fCurrentContext]) / 2;
    }

    @Override
    public String getDeclaredPrefixAt(int index) {
        return this.fNamespace[this.fContext[this.fCurrentContext] + index * 2];
    }

    public Iterator<String> getPrefixes() {
        int count = 0;
        if (this.fPrefixes.length < this.fNamespace.length / 2) {
            String[] prefixes = new String[this.fNamespaceSize];
            this.fPrefixes = prefixes;
        }
        String prefix = null;
        boolean unique = true;
        for (int i = 2; i < this.fNamespaceSize - 2; i += 2) {
            prefix = this.fNamespace[i + 2];
            for (int k = 0; k < count; ++k) {
                if (this.fPrefixes[k] != prefix) continue;
                unique = false;
                break;
            }
            if (unique) {
                this.fPrefixes[count++] = prefix;
            }
            unique = true;
        }
        return new IteratorPrefixes(this.fPrefixes, count);
    }

    @Override
    public Enumeration<String> getAllPrefixes() {
        int count = 0;
        if (this.fPrefixes.length < this.fNamespace.length / 2) {
            String[] prefixes = new String[this.fNamespaceSize];
            this.fPrefixes = prefixes;
        }
        String prefix = null;
        boolean unique = true;
        for (int i = 2; i < this.fNamespaceSize - 2; i += 2) {
            prefix = this.fNamespace[i + 2];
            for (int k = 0; k < count; ++k) {
                if (this.fPrefixes[k] != prefix) continue;
                unique = false;
                break;
            }
            if (unique) {
                this.fPrefixes[count++] = prefix;
            }
            unique = true;
        }
        return new Prefixes(this.fPrefixes, count);
    }

    public List<String> getPrefixes(String uri) {
        boolean count = false;
        Object prefix = null;
        boolean unique = true;
        ArrayList<String> prefixList = new ArrayList<String>();
        for (int i = this.fNamespaceSize; i > 0; i -= 2) {
            if (this.fNamespace[i - 1] != uri || prefixList.contains(this.fNamespace[i - 2])) continue;
            prefixList.add(this.fNamespace[i - 2]);
        }
        return prefixList;
    }

    public boolean containsPrefix(String prefix) {
        for (int i = this.fNamespaceSize; i > 0; i -= 2) {
            if (this.fNamespace[i - 2] != prefix) continue;
            return true;
        }
        return false;
    }

    public boolean containsPrefixInCurrentContext(String prefix) {
        for (int i = this.fContext[this.fCurrentContext]; i < this.fNamespaceSize; i += 2) {
            if (this.fNamespace[i] != prefix) continue;
            return true;
        }
        return false;
    }

    protected final class IteratorPrefixes
    implements Iterator<String> {
        private String[] prefixes;
        private int counter = 0;
        private int size = 0;

        public IteratorPrefixes(String[] prefixes, int size) {
            this.prefixes = prefixes;
            this.size = size;
        }

        @Override
        public boolean hasNext() {
            return this.counter < this.size;
        }

        @Override
        public String next() {
            if (this.counter < this.size) {
                return NamespaceSupport.this.fPrefixes[this.counter++];
            }
            throw new NoSuchElementException("Illegal access to Namespace prefixes enumeration.");
        }

        public String toString() {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < this.size; ++i) {
                buf.append(this.prefixes[i]);
                buf.append(" ");
            }
            return buf.toString();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    protected final class Prefixes
    implements Enumeration<String> {
        private String[] prefixes;
        private int counter = 0;
        private int size = 0;

        public Prefixes(String[] prefixes, int size) {
            this.prefixes = prefixes;
            this.size = size;
        }

        @Override
        public boolean hasMoreElements() {
            return this.counter < this.size;
        }

        @Override
        public String nextElement() {
            if (this.counter < this.size) {
                return NamespaceSupport.this.fPrefixes[this.counter++];
            }
            throw new NoSuchElementException("Illegal access to Namespace prefixes enumeration.");
        }

        public String toString() {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < this.size; ++i) {
                buf.append(this.prefixes[i]);
                buf.append(" ");
            }
            return buf.toString();
        }
    }
}

