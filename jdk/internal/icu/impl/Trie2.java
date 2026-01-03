/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.icu.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.NoSuchElementException;
import jdk.internal.icu.impl.Trie2_16;

abstract class Trie2
implements Iterable<Range> {
    private static ValueMapper defaultValueMapper = new ValueMapper(){

        @Override
        public int map(int in) {
            return in;
        }
    };
    UTrie2Header header;
    char[] index;
    int data16;
    int[] data32;
    int indexLength;
    int dataLength;
    int index2NullOffset;
    int initialValue;
    int errorValue;
    int highStart;
    int highValueIndex;
    int dataNullOffset;
    static final int UTRIE2_OPTIONS_VALUE_BITS_MASK = 15;
    static final int UTRIE2_SHIFT_1 = 11;
    static final int UTRIE2_SHIFT_2 = 5;
    static final int UTRIE2_SHIFT_1_2 = 6;
    static final int UTRIE2_OMITTED_BMP_INDEX_1_LENGTH = 32;
    static final int UTRIE2_INDEX_2_BLOCK_LENGTH = 64;
    static final int UTRIE2_INDEX_2_MASK = 63;
    static final int UTRIE2_DATA_BLOCK_LENGTH = 32;
    static final int UTRIE2_DATA_MASK = 31;
    static final int UTRIE2_INDEX_SHIFT = 2;
    static final int UTRIE2_DATA_GRANULARITY = 4;
    static final int UTRIE2_LSCP_INDEX_2_OFFSET = 2048;
    static final int UTRIE2_LSCP_INDEX_2_LENGTH = 32;
    static final int UTRIE2_INDEX_2_BMP_LENGTH = 2080;
    static final int UTRIE2_UTF8_2B_INDEX_2_OFFSET = 2080;
    static final int UTRIE2_UTF8_2B_INDEX_2_LENGTH = 32;
    static final int UTRIE2_INDEX_1_OFFSET = 2112;
    static final int UTRIE2_BAD_UTF8_DATA_OFFSET = 128;

    Trie2() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Trie2 createFromSerialized(ByteBuffer bytes) throws IOException {
        ByteOrder outerByteOrder = bytes.order();
        try {
            int i;
            UTrie2Header header = new UTrie2Header();
            header.signature = bytes.getInt();
            switch (header.signature) {
                case 1416784178: {
                    break;
                }
                case 845771348: {
                    boolean isBigEndian = outerByteOrder == ByteOrder.BIG_ENDIAN;
                    bytes.order(isBigEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
                    header.signature = 1416784178;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Buffer does not contain a serialized UTrie2");
                }
            }
            header.options = bytes.getChar();
            header.indexLength = bytes.getChar();
            header.shiftedDataLength = bytes.getChar();
            header.index2NullOffset = bytes.getChar();
            header.dataNullOffset = bytes.getChar();
            header.shiftedHighStart = bytes.getChar();
            if ((header.options & 0xF) != 0) {
                throw new IllegalArgumentException("UTrie2 serialized format error.");
            }
            Trie2_16 This = new Trie2_16();
            This.header = header;
            This.indexLength = header.indexLength;
            This.dataLength = header.shiftedDataLength << 2;
            This.index2NullOffset = header.index2NullOffset;
            This.dataNullOffset = header.dataNullOffset;
            This.highStart = header.shiftedHighStart << 11;
            This.highValueIndex = This.dataLength - 4;
            This.highValueIndex += This.indexLength;
            int indexArraySize = This.indexLength;
            This.index = new char[indexArraySize += This.dataLength];
            for (i = 0; i < This.indexLength; ++i) {
                This.index[i] = bytes.getChar();
            }
            This.data16 = This.indexLength;
            for (i = 0; i < This.dataLength; ++i) {
                This.index[This.data16 + i] = bytes.getChar();
            }
            This.data32 = null;
            This.initialValue = This.index[This.dataNullOffset];
            This.errorValue = This.index[This.data16 + 128];
            Trie2_16 trie2_16 = This;
            return trie2_16;
        }
        finally {
            bytes.order(outerByteOrder);
        }
    }

    public abstract int get(int var1);

    public abstract int getFromU16SingleLead(char var1);

    @Override
    public Iterator<Range> iterator() {
        return this.iterator(defaultValueMapper);
    }

    public Iterator<Range> iterator(ValueMapper mapper) {
        return new Trie2Iterator(mapper);
    }

    int rangeEnd(int start, int limitp, int val) {
        int c;
        int limit = Math.min(this.highStart, limitp);
        for (c = start + 1; c < limit && this.get(c) == val; ++c) {
        }
        if (c >= this.highStart) {
            c = limitp;
        }
        return c - 1;
    }

    private static int initHash() {
        return -2128831035;
    }

    private static int hashByte(int h, int b) {
        h *= 16777619;
        return h ^= b;
    }

    private static int hashUChar32(int h, int c) {
        h = Trie2.hashByte(h, c & 0xFF);
        h = Trie2.hashByte(h, c >> 8 & 0xFF);
        h = Trie2.hashByte(h, c >> 16);
        return h;
    }

    private static int hashInt(int h, int i) {
        h = Trie2.hashByte(h, i & 0xFF);
        h = Trie2.hashByte(h, i >> 8 & 0xFF);
        h = Trie2.hashByte(h, i >> 16 & 0xFF);
        h = Trie2.hashByte(h, i >> 24 & 0xFF);
        return h;
    }

    static class UTrie2Header {
        int signature;
        int options;
        int indexLength;
        int shiftedDataLength;
        int index2NullOffset;
        int dataNullOffset;
        int shiftedHighStart;

        UTrie2Header() {
        }
    }

    public static interface ValueMapper {
        public int map(int var1);
    }

    class Trie2Iterator
    implements Iterator<Range> {
        private ValueMapper mapper;
        private Range returnValue = new Range();
        private int nextStart;
        private int limitCP;
        private boolean doingCodePoints = true;
        private boolean doLeadSurrogates = true;

        Trie2Iterator(ValueMapper vm) {
            this.mapper = vm;
            this.nextStart = 0;
            this.limitCP = 0x110000;
            this.doLeadSurrogates = true;
        }

        @Override
        public Range next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            if (this.nextStart >= this.limitCP) {
                this.doingCodePoints = false;
                this.nextStart = 55296;
            }
            int endOfRange = 0;
            int val = 0;
            int mappedVal = 0;
            if (this.doingCodePoints) {
                val = Trie2.this.get(this.nextStart);
                mappedVal = this.mapper.map(val);
                endOfRange = Trie2.this.rangeEnd(this.nextStart, this.limitCP, val);
                while (endOfRange < this.limitCP - 1 && this.mapper.map(val = Trie2.this.get(endOfRange + 1)) == mappedVal) {
                    endOfRange = Trie2.this.rangeEnd(endOfRange + 1, this.limitCP, val);
                }
            } else {
                val = Trie2.this.getFromU16SingleLead((char)this.nextStart);
                mappedVal = this.mapper.map(val);
                endOfRange = this.rangeEndLS((char)this.nextStart);
                while (endOfRange < 56319 && this.mapper.map(val = Trie2.this.getFromU16SingleLead((char)(endOfRange + 1))) == mappedVal) {
                    endOfRange = this.rangeEndLS((char)(endOfRange + 1));
                }
            }
            this.returnValue.startCodePoint = this.nextStart;
            this.returnValue.endCodePoint = endOfRange;
            this.returnValue.value = mappedVal;
            this.returnValue.leadSurrogate = !this.doingCodePoints;
            this.nextStart = endOfRange + 1;
            return this.returnValue;
        }

        @Override
        public boolean hasNext() {
            return this.doingCodePoints && (this.doLeadSurrogates || this.nextStart < this.limitCP) || this.nextStart < 56320;
        }

        private int rangeEndLS(char startingLS) {
            int c;
            if (startingLS >= '\udbff') {
                return 56319;
            }
            int val = Trie2.this.getFromU16SingleLead(startingLS);
            for (c = startingLS + '\u0001'; c <= 56319 && Trie2.this.getFromU16SingleLead((char)c) == val; ++c) {
            }
            return c - 1;
        }
    }

    public static class Range {
        public int startCodePoint;
        public int endCodePoint;
        public int value;
        public boolean leadSurrogate;

        public boolean equals(Object other) {
            if (other == null || !other.getClass().equals(this.getClass())) {
                return false;
            }
            Range tother = (Range)other;
            return this.startCodePoint == tother.startCodePoint && this.endCodePoint == tother.endCodePoint && this.value == tother.value && this.leadSurrogate == tother.leadSurrogate;
        }

        public int hashCode() {
            int h = Trie2.initHash();
            h = Trie2.hashUChar32(h, this.startCodePoint);
            h = Trie2.hashUChar32(h, this.endCodePoint);
            h = Trie2.hashInt(h, this.value);
            h = Trie2.hashByte(h, this.leadSurrogate ? 1 : 0);
            return h;
        }
    }
}

