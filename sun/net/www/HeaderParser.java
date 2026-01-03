/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www;

import java.util.Iterator;
import java.util.Locale;
import java.util.OptionalInt;

public class HeaderParser {
    String raw;
    String[][] tab;
    int nkeys;
    int asize = 10;

    public HeaderParser(String raw) {
        this.raw = raw;
        this.tab = new String[this.asize][2];
        this.parse();
    }

    private HeaderParser() {
    }

    public HeaderParser subsequence(int start, int end) {
        if (start == 0 && end == this.nkeys) {
            return this;
        }
        if (start < 0 || start >= end || end > this.nkeys) {
            throw new IllegalArgumentException("invalid start or end");
        }
        HeaderParser n = new HeaderParser();
        n.tab = new String[this.asize][2];
        n.asize = this.asize;
        System.arraycopy(this.tab, start, n.tab, 0, end - start);
        n.nkeys = end - start;
        return n;
    }

    private void parse() {
        if (this.raw != null) {
            this.raw = this.raw.trim();
            char[] ca = this.raw.toCharArray();
            int beg = 0;
            int end = 0;
            int i = 0;
            boolean inKey = true;
            boolean inQuote = false;
            int len = ca.length;
            while (end < len) {
                char c = ca[end];
                if (c == '=' && !inQuote) {
                    this.tab[i][0] = new String(ca, beg, end - beg).toLowerCase(Locale.ROOT);
                    inKey = false;
                    beg = ++end;
                } else if (c == '\"') {
                    if (inQuote) {
                        this.tab[i++][1] = new String(ca, beg, end - beg);
                        inQuote = false;
                        while (++end < len && (ca[end] == ' ' || ca[end] == ',')) {
                        }
                        inKey = true;
                        beg = end;
                    } else {
                        inQuote = true;
                        beg = ++end;
                    }
                } else if (c == ' ' || c == ',') {
                    if (inQuote) {
                        ++end;
                        continue;
                    }
                    if (inKey) {
                        this.tab[i++][0] = new String(ca, beg, end - beg).toLowerCase(Locale.ROOT);
                    } else {
                        this.tab[i++][1] = new String(ca, beg, end - beg);
                    }
                    while (end < len && (ca[end] == ' ' || ca[end] == ',')) {
                        ++end;
                    }
                    inKey = true;
                    beg = end;
                } else {
                    ++end;
                }
                if (i != this.asize) continue;
                this.asize *= 2;
                String[][] ntab = new String[this.asize][2];
                System.arraycopy(this.tab, 0, ntab, 0, this.tab.length);
                this.tab = ntab;
            }
            if (--end > beg) {
                if (!inKey) {
                    this.tab[i++][1] = ca[end] == '\"' ? new String(ca, beg, end - beg) : new String(ca, beg, end - beg + 1);
                } else {
                    this.tab[i++][0] = new String(ca, beg, end - beg + 1).toLowerCase(Locale.ROOT);
                }
            } else if (end == beg) {
                if (!inKey) {
                    this.tab[i++][1] = ca[end] == '\"' ? String.valueOf(ca[end - 1]) : String.valueOf(ca[end]);
                } else {
                    this.tab[i++][0] = String.valueOf(ca[end]).toLowerCase(Locale.ROOT);
                }
            }
            this.nkeys = i;
        }
    }

    public String findKey(int i) {
        if (i < 0 || i > this.asize) {
            return null;
        }
        return this.tab[i][0];
    }

    public String findValue(int i) {
        if (i < 0 || i > this.asize) {
            return null;
        }
        return this.tab[i][1];
    }

    public String findValue(String key) {
        return this.findValue(key, null);
    }

    public String findValue(String k, String Default2) {
        if (k == null) {
            return Default2;
        }
        k = k.toLowerCase(Locale.ROOT);
        for (int i = 0; i < this.asize; ++i) {
            if (this.tab[i][0] == null) {
                return Default2;
            }
            if (!k.equals(this.tab[i][0])) continue;
            return this.tab[i][1];
        }
        return Default2;
    }

    public Iterator<String> keys() {
        return new ParserIterator(false);
    }

    public Iterator<String> values() {
        return new ParserIterator(true);
    }

    public String toString() {
        Iterator<String> k = this.keys();
        StringBuilder sb = new StringBuilder();
        sb.append("{size=").append(this.asize).append(" nkeys=").append(this.nkeys).append(' ');
        int i = 0;
        while (k.hasNext()) {
            String key = k.next();
            String val = this.findValue(i);
            if (val != null && val.isEmpty()) {
                val = null;
            }
            sb.append(" {").append(key).append(val == null ? "" : "," + val).append('}');
            if (k.hasNext()) {
                sb.append(',');
            }
            ++i;
        }
        sb.append(" }");
        return sb.toString();
    }

    public int findInt(String k, int Default2) {
        try {
            return Integer.parseInt(this.findValue(k, String.valueOf(Default2)));
        }
        catch (Throwable t) {
            return Default2;
        }
    }

    public OptionalInt findInt(String k) {
        try {
            String s = this.findValue(k);
            if (s == null) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(Integer.parseInt(s));
        }
        catch (Throwable t) {
            return OptionalInt.empty();
        }
    }

    class ParserIterator
    implements Iterator<String> {
        int index;
        boolean returnsValue;

        ParserIterator(boolean returnValue) {
            this.returnsValue = returnValue;
        }

        @Override
        public boolean hasNext() {
            return this.index < HeaderParser.this.nkeys;
        }

        @Override
        public String next() {
            return HeaderParser.this.tab[this.index++][this.returnsValue ? 1 : 0];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }
    }
}

