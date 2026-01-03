/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringJoiner;

public class MessageHeader {
    private String[] keys;
    private String[] values;
    private int nkeys;
    private final int maxHeaderSize;
    private long size;

    public MessageHeader() {
        this(0);
    }

    public MessageHeader(int maxHeaderSize) {
        this.maxHeaderSize = maxHeaderSize;
        this.grow();
    }

    public MessageHeader(InputStream is) throws IOException {
        this.maxHeaderSize = 0;
        this.parseHeader(is);
    }

    public synchronized String getHeaderNamesInList() {
        StringJoiner joiner = new StringJoiner(",");
        for (int i = 0; i < this.nkeys; ++i) {
            joiner.add(this.keys[i]);
        }
        return joiner.toString();
    }

    public synchronized void reset() {
        this.keys = null;
        this.values = null;
        this.nkeys = 0;
        this.grow();
    }

    public synchronized String findValue(String k) {
        if (k == null) {
            int i = this.nkeys;
            while (--i >= 0) {
                if (this.keys[i] != null) continue;
                return this.values[i];
            }
        } else {
            int i = this.nkeys;
            while (--i >= 0) {
                if (!k.equalsIgnoreCase(this.keys[i])) continue;
                return this.values[i];
            }
        }
        return null;
    }

    public synchronized int getKey(String k) {
        int i = this.nkeys;
        while (--i >= 0) {
            if (this.keys[i] != k && (k == null || !k.equalsIgnoreCase(this.keys[i]))) continue;
            return i;
        }
        return -1;
    }

    public synchronized String getKey(int n) {
        if (n < 0 || n >= this.nkeys) {
            return null;
        }
        return this.keys[n];
    }

    public synchronized String getValue(int n) {
        if (n < 0 || n >= this.nkeys) {
            return null;
        }
        return this.values[n];
    }

    public synchronized String findNextValue(String k, String v) {
        boolean foundV = false;
        if (k == null) {
            int i = this.nkeys;
            while (--i >= 0) {
                if (this.keys[i] != null) continue;
                if (foundV) {
                    return this.values[i];
                }
                if (this.values[i] != v) continue;
                foundV = true;
            }
        } else {
            int i = this.nkeys;
            while (--i >= 0) {
                if (!k.equalsIgnoreCase(this.keys[i])) continue;
                if (foundV) {
                    return this.values[i];
                }
                if (this.values[i] != v) continue;
                foundV = true;
            }
        }
        return null;
    }

    public boolean filterNTLMResponses(String k) {
        boolean found = false;
        for (int i = 0; i < this.nkeys; ++i) {
            if (!k.equalsIgnoreCase(this.keys[i]) || this.values[i] == null || this.values[i].length() <= 5 || !this.values[i].substring(0, 5).equalsIgnoreCase("NTLM ")) continue;
            found = true;
            break;
        }
        if (found) {
            int j = 0;
            for (int i = 0; i < this.nkeys; ++i) {
                if (k.equalsIgnoreCase(this.keys[i]) && ("Negotiate".equalsIgnoreCase(this.values[i]) || "Kerberos".equalsIgnoreCase(this.values[i]))) continue;
                if (i != j) {
                    this.keys[j] = this.keys[i];
                    this.values[j] = this.values[i];
                }
                ++j;
            }
            if (j != this.nkeys) {
                this.nkeys = j;
                return true;
            }
        }
        return false;
    }

    public Iterator<String> multiValueIterator(String k) {
        return new HeaderIterator(k, this);
    }

    public synchronized Map<String, List<String>> getHeaders() {
        return this.getHeaders(null);
    }

    public synchronized Map<String, List<String>> getHeaders(String[] excludeList) {
        return this.filterAndAddHeaders(excludeList, null);
    }

    public synchronized Map<String, List<String>> filterAndAddHeaders(String[] excludeList, Map<String, List<String>> include) {
        boolean skipIt = false;
        HashMap<String, List<Object>> m = new HashMap<String, List<Object>>();
        for (int i = 0; i < this.nkeys; ++i) {
            if (excludeList != null) {
                for (int j = 0; j < excludeList.length; ++j) {
                    if (excludeList[j] == null || !excludeList[j].equalsIgnoreCase(this.keys[i])) continue;
                    skipIt = true;
                    break;
                }
            }
            if (!skipIt) {
                ArrayList<String> l = (ArrayList<String>)m.get(this.keys[i]);
                if (l == null) {
                    l = new ArrayList<String>();
                    m.put(this.keys[i], l);
                }
                l.add(this.values[i]);
                continue;
            }
            skipIt = false;
        }
        if (include != null) {
            for (Map.Entry<String, List<String>> entry : include.entrySet()) {
                ArrayList l = (ArrayList)m.get(entry.getKey());
                if (l == null) {
                    l = new ArrayList();
                    m.put(entry.getKey(), l);
                }
                l.addAll(entry.getValue());
            }
        }
        for (String key : m.keySet()) {
            m.put(key, Collections.unmodifiableList((List)m.get(key)));
        }
        return Collections.unmodifiableMap(m);
    }

    private static boolean isRequestline(String line) {
        String k = line.trim();
        int i = k.lastIndexOf(32);
        if (i <= 0) {
            return false;
        }
        int len = k.length();
        if (len - i < 9) {
            return false;
        }
        char c1 = k.charAt(len - 3);
        char c2 = k.charAt(len - 2);
        char c3 = k.charAt(len - 1);
        if (c1 < '1' || c1 > '9') {
            return false;
        }
        if (c2 != '.') {
            return false;
        }
        if (c3 < '0' || c3 > '9') {
            return false;
        }
        return k.substring(i + 1, len - 3).equalsIgnoreCase("HTTP/");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void print(PrintStream p) {
        String[] v;
        String[] k;
        int n;
        MessageHeader messageHeader = this;
        synchronized (messageHeader) {
            n = this.nkeys;
            k = (String[])this.keys.clone();
            v = (String[])this.values.clone();
        }
        MessageHeader.print(n, k, v, p);
    }

    private static void print(int nkeys, String[] keys, String[] values, PrintStream p) {
        for (int i = 0; i < nkeys; ++i) {
            if (keys[i] == null) continue;
            StringBuilder sb = new StringBuilder(keys[i]);
            if (values[i] != null) {
                sb.append(": " + values[i]);
            } else if (i != 0 || !MessageHeader.isRequestline(keys[i])) {
                sb.append(":");
            }
            p.print(sb.append("\r\n"));
        }
        p.print("\r\n");
        p.flush();
    }

    public synchronized void add(String k, String v) {
        this.grow();
        this.keys[this.nkeys] = k;
        this.values[this.nkeys] = v;
        ++this.nkeys;
    }

    public synchronized void prepend(String k, String v) {
        this.grow();
        for (int i = this.nkeys; i > 0; --i) {
            this.keys[i] = this.keys[i - 1];
            this.values[i] = this.values[i - 1];
        }
        this.keys[0] = k;
        this.values[0] = v;
        ++this.nkeys;
    }

    public synchronized void set(int i, String k, String v) {
        this.grow();
        if (i < 0) {
            return;
        }
        if (i >= this.nkeys) {
            this.add(k, v);
        } else {
            this.keys[i] = k;
            this.values[i] = v;
        }
    }

    private void grow() {
        if (this.keys == null || this.nkeys >= this.keys.length) {
            String[] nk = new String[this.nkeys + 4];
            String[] nv = new String[this.nkeys + 4];
            if (this.keys != null) {
                System.arraycopy(this.keys, 0, nk, 0, this.nkeys);
            }
            if (this.values != null) {
                System.arraycopy(this.values, 0, nv, 0, this.nkeys);
            }
            this.keys = nk;
            this.values = nv;
        }
    }

    public synchronized void remove(String k) {
        if (k == null) {
            for (int i = 0; i < this.nkeys; ++i) {
                while (this.keys[i] == null && i < this.nkeys) {
                    for (int j = i; j < this.nkeys - 1; ++j) {
                        this.keys[j] = this.keys[j + 1];
                        this.values[j] = this.values[j + 1];
                    }
                    --this.nkeys;
                }
            }
        } else {
            for (int i = 0; i < this.nkeys; ++i) {
                while (k.equalsIgnoreCase(this.keys[i]) && i < this.nkeys) {
                    for (int j = i; j < this.nkeys - 1; ++j) {
                        this.keys[j] = this.keys[j + 1];
                        this.values[j] = this.values[j + 1];
                    }
                    --this.nkeys;
                }
            }
        }
    }

    public synchronized void set(String k, String v) {
        int i = this.nkeys;
        while (--i >= 0) {
            if (!k.equalsIgnoreCase(this.keys[i])) continue;
            this.values[i] = v;
            return;
        }
        this.add(k, v);
    }

    public synchronized void setIfNotSet(String k, String v) {
        if (this.findValue(k) == null) {
            this.add(k, v);
        }
    }

    public static String canonicalID(String id) {
        char c;
        int st;
        if (id == null) {
            return "";
        }
        int len = id.length();
        boolean substr = false;
        for (st = 0; st < len && ((c = id.charAt(st)) == '<' || c <= ' '); ++st) {
            substr = true;
        }
        while (st < len && ((c = id.charAt(len - 1)) == '>' || c <= ' ')) {
            --len;
            substr = true;
        }
        return substr ? id.substring(st, len) : id;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void parseHeader(InputStream is) throws IOException {
        MessageHeader messageHeader = this;
        synchronized (messageHeader) {
            this.nkeys = 0;
            this.size = 0L;
        }
        this.mergeHeader(is);
    }

    private void checkMaxHeaderSize(int sz) throws ProtocolException {
        if (this.maxHeaderSize > 0) {
            this.checkNewSize(this.size, sz, 0);
        }
    }

    private long checkNewSize(long size, int name, int value) throws ProtocolException {
        long newSize = size + (long)name + (long)value + 32L;
        if (this.maxHeaderSize > 0 && newSize > (long)this.maxHeaderSize) {
            Arrays.fill(this.keys, 0, this.nkeys, null);
            Arrays.fill(this.values, 0, this.nkeys, null);
            this.nkeys = 0;
            throw new ProtocolException(String.format("Header size too big: %s > %s", newSize, this.maxHeaderSize));
        }
        return newSize;
    }

    /*
     * Enabled aggressive block sorting
     */
    public void mergeHeader(InputStream is) throws IOException {
        if (is == null) {
            return;
        }
        char[] s = new char[10];
        int firstc = is.read();
        while (true) {
            String k;
            int keyend;
            int len;
            block20: {
                int c;
                long maxRemaining;
                boolean inKey;
                if (firstc != 10 && firstc != 13 && firstc >= 0) {
                    len = 0;
                    keyend = -1;
                    inKey = firstc > 32;
                    s[len++] = (char)firstc;
                    this.checkMaxHeaderSize(len);
                    maxRemaining = this.maxHeaderSize > 0 ? (long)this.maxHeaderSize - this.size - 32L : Long.MAX_VALUE;
                } else {
                    return;
                }
                while ((c = is.read()) >= 0) {
                    switch (c) {
                        case 58: {
                            if (inKey && len > 0) {
                                keyend = len;
                            }
                            inKey = false;
                            break;
                        }
                        case 9: {
                            c = 32;
                        }
                        case 32: {
                            inKey = false;
                            break;
                        }
                        case 10: 
                        case 13: {
                            firstc = is.read();
                            if (c == 13 && firstc == 10 && (firstc = is.read()) == 13) {
                                firstc = is.read();
                            }
                            if (firstc == 10 || firstc == 13 || firstc > 32) break block20;
                            c = 32;
                        }
                    }
                    if (len >= s.length) {
                        char[] ns = new char[s.length * 2];
                        System.arraycopy(s, 0, ns, 0, len);
                        s = ns;
                    }
                    s[len++] = (char)c;
                    if (this.maxHeaderSize <= 0 || (long)len <= maxRemaining) continue;
                    this.checkMaxHeaderSize(len);
                }
                firstc = -1;
            }
            while (len > 0 && s[len - 1] <= ' ') {
                --len;
            }
            if (keyend <= 0) {
                k = null;
                keyend = 0;
            } else {
                k = String.copyValueOf(s, 0, keyend);
                if (keyend < len && s[keyend] == ':') {
                    ++keyend;
                }
                while (keyend < len && s[keyend] <= ' ') {
                    ++keyend;
                }
            }
            String v = keyend >= len ? new String() : String.copyValueOf(s, keyend, len - keyend);
            int klen = k == null ? 0 : k.length();
            this.size = this.checkNewSize(this.size, klen, v.length());
            this.add(k, v);
        }
    }

    public synchronized String toString() {
        String result = super.toString() + this.nkeys + " pairs: ";
        for (int i = 0; i < this.keys.length && i < this.nkeys; ++i) {
            result = result + "{" + this.keys[i] + ": " + this.values[i] + "}";
        }
        return result;
    }

    class HeaderIterator
    implements Iterator<String> {
        int index = 0;
        int next = -1;
        String key;
        boolean haveNext = false;
        Object lock;

        public HeaderIterator(String k, Object lock) {
            this.key = k;
            this.lock = lock;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean hasNext() {
            Object object = this.lock;
            synchronized (object) {
                if (this.haveNext) {
                    return true;
                }
                while (this.index < MessageHeader.this.nkeys) {
                    if (this.key.equalsIgnoreCase(MessageHeader.this.keys[this.index])) {
                        this.haveNext = true;
                        this.next = this.index++;
                        return true;
                    }
                    ++this.index;
                }
                return false;
            }
        }

        @Override
        public String next() {
            Object object = this.lock;
            synchronized (object) {
                if (this.haveNext) {
                    this.haveNext = false;
                    return MessageHeader.this.values[this.next];
                }
                if (this.hasNext()) {
                    return this.next();
                }
                throw new NoSuchElementException("No more elements");
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not allowed");
        }
    }
}

