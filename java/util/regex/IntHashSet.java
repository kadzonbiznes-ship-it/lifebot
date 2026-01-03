/*
 * Decompiled with CFR 0.152.
 */
package java.util.regex;

import java.util.Arrays;

class IntHashSet {
    private int[] entries = new int[32];
    private int[] hashes = new int[9];
    private int pos = 0;

    public IntHashSet() {
        Arrays.fill(this.entries, -1);
        Arrays.fill(this.hashes, -1);
    }

    public boolean contains(int i) {
        int h = this.hashes[i % this.hashes.length];
        while (h != -1) {
            if (this.entries[h] == i) {
                return true;
            }
            h = this.entries[h + 1];
        }
        return false;
    }

    public void add(int i) {
        int next;
        int h0 = i % this.hashes.length;
        int next0 = next = this.hashes[h0];
        while (next0 != -1) {
            if (this.entries[next0] == i) {
                return;
            }
            next0 = this.entries[next0 + 1];
        }
        this.hashes[h0] = this.pos;
        this.entries[this.pos++] = i;
        this.entries[this.pos++] = next;
        if (this.pos == this.entries.length) {
            this.expand();
        }
    }

    public void clear() {
        Arrays.fill(this.entries, -1);
        Arrays.fill(this.hashes, -1);
        this.pos = 0;
    }

    private void expand() {
        int[] old = this.entries;
        int[] es = new int[old.length << 1];
        int hlen = old.length / 2 | 1;
        int[] hs = new int[hlen];
        Arrays.fill(es, -1);
        Arrays.fill(hs, -1);
        int n = 0;
        while (n < this.pos) {
            int i = old[n];
            int hsh = i % hlen;
            int next = hs[hsh];
            hs[hsh] = n;
            es[n++] = i;
            es[n++] = next;
        }
        this.entries = es;
        this.hashes = hs;
    }
}

