/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.util.PrimeNumberSequenceGenerator;

public class SymbolTable {
    protected static final int TABLE_SIZE = 101;
    protected static final int MAX_HASH_COLLISIONS = 40;
    protected static final int MULTIPLIERS_SIZE = 32;
    protected static final int MULTIPLIERS_MASK = 31;
    protected Entry[] fBuckets = null;
    protected int fTableSize;
    protected transient int fCount;
    protected int fThreshold;
    protected float fLoadFactor;
    protected final int fCollisionThreshold;
    protected int[] fHashMultipliers;

    public SymbolTable(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        if (loadFactor <= 0.0f || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal Load: " + loadFactor);
        }
        if (initialCapacity == 0) {
            initialCapacity = 1;
        }
        this.fLoadFactor = loadFactor;
        this.fTableSize = initialCapacity;
        this.fBuckets = new Entry[this.fTableSize];
        this.fThreshold = (int)((float)this.fTableSize * loadFactor);
        this.fCollisionThreshold = (int)(40.0f * loadFactor);
        this.fCount = 0;
    }

    public SymbolTable(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    public SymbolTable() {
        this(101, 0.75f);
    }

    public String addSymbol(String symbol) {
        int collisionCount = 0;
        int bucket = this.hash(symbol) % this.fTableSize;
        Entry entry = this.fBuckets[bucket];
        while (entry != null) {
            if (entry.symbol.equals(symbol)) {
                return entry.symbol;
            }
            ++collisionCount;
            entry = entry.next;
        }
        return this.addSymbol0(symbol, bucket, collisionCount);
    }

    private String addSymbol0(String symbol, int bucket, int collisionCount) {
        Entry entry;
        if (this.fCount >= this.fThreshold) {
            this.rehash();
            bucket = this.hash(symbol) % this.fTableSize;
        } else if (collisionCount >= this.fCollisionThreshold) {
            this.rebalance();
            bucket = this.hash(symbol) % this.fTableSize;
        }
        this.fBuckets[bucket] = entry = new Entry(symbol, this.fBuckets[bucket]);
        ++this.fCount;
        return entry.symbol;
    }

    public String addSymbol(char[] buffer, int offset, int length) {
        int collisionCount = 0;
        int bucket = this.hash(buffer, offset, length) % this.fTableSize;
        Entry entry = this.fBuckets[bucket];
        while (entry != null) {
            block3: {
                if (length == entry.characters.length) {
                    for (int i = 0; i < length; ++i) {
                        if (buffer[offset + i] == entry.characters[i]) continue;
                        ++collisionCount;
                        break block3;
                    }
                    return entry.symbol;
                }
                ++collisionCount;
            }
            entry = entry.next;
        }
        return this.addSymbol0(buffer, offset, length, bucket, collisionCount);
    }

    private String addSymbol0(char[] buffer, int offset, int length, int bucket, int collisionCount) {
        Entry entry;
        if (this.fCount >= this.fThreshold) {
            this.rehash();
            bucket = this.hash(buffer, offset, length) % this.fTableSize;
        } else if (collisionCount >= this.fCollisionThreshold) {
            this.rebalance();
            bucket = this.hash(buffer, offset, length) % this.fTableSize;
        }
        this.fBuckets[bucket] = entry = new Entry(buffer, offset, length, this.fBuckets[bucket]);
        ++this.fCount;
        return entry.symbol;
    }

    public int hash(String symbol) {
        if (this.fHashMultipliers == null) {
            return symbol.hashCode() & Integer.MAX_VALUE;
        }
        return this.hash0(symbol);
    }

    private int hash0(String symbol) {
        int code = 0;
        int length = symbol.length();
        int[] multipliers = this.fHashMultipliers;
        for (int i = 0; i < length; ++i) {
            code = code * multipliers[i & 0x1F] + symbol.charAt(i);
        }
        return code & Integer.MAX_VALUE;
    }

    public int hash(char[] buffer, int offset, int length) {
        if (this.fHashMultipliers == null) {
            int code = 0;
            for (int i = 0; i < length; ++i) {
                code = code * 31 + buffer[offset + i];
            }
            return code & Integer.MAX_VALUE;
        }
        return this.hash0(buffer, offset, length);
    }

    private int hash0(char[] buffer, int offset, int length) {
        int code = 0;
        int[] multipliers = this.fHashMultipliers;
        for (int i = 0; i < length; ++i) {
            code = code * multipliers[i & 0x1F] + buffer[offset + i];
        }
        return code & Integer.MAX_VALUE;
    }

    protected void rehash() {
        this.rehashCommon(this.fBuckets.length * 2 + 1);
    }

    protected void rebalance() {
        if (this.fHashMultipliers == null) {
            this.fHashMultipliers = new int[32];
        }
        PrimeNumberSequenceGenerator.generateSequence(this.fHashMultipliers);
        this.rehashCommon(this.fBuckets.length);
    }

    private void rehashCommon(int newCapacity) {
        int oldCapacity = this.fBuckets.length;
        Entry[] oldTable = this.fBuckets;
        Entry[] newTable = new Entry[newCapacity];
        this.fThreshold = (int)((float)newCapacity * this.fLoadFactor);
        this.fBuckets = newTable;
        this.fTableSize = this.fBuckets.length;
        int i = oldCapacity;
        while (i-- > 0) {
            Entry old = oldTable[i];
            while (old != null) {
                Entry e = old;
                old = old.next;
                int index = this.hash(e.symbol) % newCapacity;
                e.next = newTable[index];
                newTable[index] = e;
            }
        }
    }

    public boolean containsSymbol(String symbol) {
        int bucket = this.hash(symbol) % this.fTableSize;
        int length = symbol.length();
        Entry entry = this.fBuckets[bucket];
        while (entry != null) {
            block4: {
                if (length == entry.characters.length) {
                    for (int i = 0; i < length; ++i) {
                        if (symbol.charAt(i) == entry.characters[i]) {
                            continue;
                        }
                        break block4;
                    }
                    return true;
                }
            }
            entry = entry.next;
        }
        return false;
    }

    public boolean containsSymbol(char[] buffer, int offset, int length) {
        int bucket = this.hash(buffer, offset, length) % this.fTableSize;
        Entry entry = this.fBuckets[bucket];
        while (entry != null) {
            block4: {
                if (length == entry.characters.length) {
                    for (int i = 0; i < length; ++i) {
                        if (buffer[offset + i] == entry.characters[i]) {
                            continue;
                        }
                        break block4;
                    }
                    return true;
                }
            }
            entry = entry.next;
        }
        return false;
    }

    protected static final class Entry {
        public final String symbol;
        public final char[] characters;
        public Entry next;

        public Entry(String symbol, Entry next) {
            this.symbol = symbol.intern();
            this.characters = new char[symbol.length()];
            symbol.getChars(0, this.characters.length, this.characters, 0);
            this.next = next;
        }

        public Entry(char[] ch, int offset, int length, Entry next) {
            this.characters = new char[length];
            System.arraycopy(ch, offset, this.characters, 0, length);
            this.symbol = new String(this.characters).intern();
            this.next = next;
        }
    }
}

