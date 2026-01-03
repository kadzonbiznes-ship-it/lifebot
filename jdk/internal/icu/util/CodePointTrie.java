/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.icu.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import jdk.internal.icu.impl.ICUBinary;
import jdk.internal.icu.impl.NormalizerImpl;
import jdk.internal.icu.util.CodePointMap;

public abstract class CodePointTrie
extends CodePointMap {
    private static final int MAX_UNICODE = 0x10FFFF;
    private static final int ASCII_LIMIT = 128;
    static final int FAST_SHIFT = 6;
    static final int FAST_DATA_BLOCK_LENGTH = 64;
    private static final int FAST_DATA_MASK = 63;
    private static final int SMALL_MAX = 4095;
    private static final int ERROR_VALUE_NEG_DATA_OFFSET = 1;
    private static final int HIGH_VALUE_NEG_DATA_OFFSET = 2;
    private static final int BMP_INDEX_LENGTH = 1024;
    static final int SMALL_LIMIT = 4096;
    private static final int SMALL_INDEX_LENGTH = 64;
    static final int SHIFT_3 = 4;
    private static final int SHIFT_2 = 9;
    private static final int SHIFT_1 = 14;
    static final int SHIFT_2_3 = 5;
    static final int SHIFT_1_2 = 5;
    private static final int OMITTED_BMP_INDEX_1_LENGTH = 4;
    static final int INDEX_2_BLOCK_LENGTH = 32;
    static final int INDEX_2_MASK = 31;
    static final int CP_PER_INDEX_2_ENTRY = 512;
    static final int INDEX_3_BLOCK_LENGTH = 32;
    private static final int INDEX_3_MASK = 31;
    static final int SMALL_DATA_BLOCK_LENGTH = 16;
    static final int SMALL_DATA_MASK = 15;
    private static final int OPTIONS_DATA_LENGTH_MASK = 61440;
    private static final int OPTIONS_DATA_NULL_OFFSET_MASK = 3840;
    private static final int OPTIONS_RESERVED_MASK = 56;
    private static final int OPTIONS_VALUE_BITS_MASK = 7;
    static final int NO_INDEX3_NULL_OFFSET = Short.MAX_VALUE;
    static final int NO_DATA_NULL_OFFSET = 1048575;
    private final int[] ascii = new int[128];
    private final char[] index;
    @Deprecated
    protected final Data data;
    @Deprecated
    protected final int dataLength;
    @Deprecated
    protected final int highStart;
    private final int index3NullOffset;
    private final int dataNullOffset;
    private final int nullValue;

    private CodePointTrie(char[] index, Data data, int highStart, int index3NullOffset, int dataNullOffset) {
        this.index = index;
        this.data = data;
        this.dataLength = data.getDataLength();
        this.highStart = highStart;
        this.index3NullOffset = index3NullOffset;
        this.dataNullOffset = dataNullOffset;
        for (int c = 0; c < 128; ++c) {
            this.ascii[c] = data.getFromIndex(c);
        }
        int nullValueOffset = dataNullOffset;
        if (nullValueOffset >= this.dataLength) {
            nullValueOffset = this.dataLength - 2;
        }
        this.nullValue = data.getFromIndex(nullValueOffset);
    }

    public static CodePointTrie fromBinary(Type type, ValueWidth valueWidth, ByteBuffer bytes) {
        ByteOrder outerByteOrder = bytes.order();
        try {
            if (bytes.remaining() < 16) {
                throw new InternalError("Buffer too short for a CodePointTrie header");
            }
            int signature = bytes.getInt();
            switch (signature) {
                case 1416784179: {
                    break;
                }
                case 862548564: {
                    boolean isBigEndian = outerByteOrder == ByteOrder.BIG_ENDIAN;
                    bytes.order(isBigEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
                    signature = 1416784179;
                    break;
                }
                default: {
                    throw new InternalError("Buffer does not contain a serialized CodePointTrie");
                }
            }
            char options = bytes.getChar();
            char indexLength = bytes.getChar();
            int dataLength = bytes.getChar();
            char index3NullOffset = bytes.getChar();
            int dataNullOffset = bytes.getChar();
            char shiftedHighStart = bytes.getChar();
            int typeInt = options >> 6 & 3;
            Type actualType = switch (typeInt) {
                case 0 -> Type.FAST;
                case 1 -> Type.SMALL;
                default -> throw new InternalError("CodePointTrie data header has an unsupported type");
            };
            int valueWidthInt = options & 7;
            ValueWidth actualValueWidth = switch (valueWidthInt) {
                case 0 -> ValueWidth.BITS_16;
                case 1 -> ValueWidth.BITS_32;
                case 2 -> ValueWidth.BITS_8;
                default -> throw new InternalError("CodePointTrie data header has an unsupported value width");
            };
            if ((options & 0x38) != 0) {
                throw new InternalError("CodePointTrie data header has unsupported options");
            }
            if (type == null) {
                type = actualType;
            }
            if (valueWidth == null) {
                valueWidth = actualValueWidth;
            }
            if (type != actualType || valueWidth != actualValueWidth) {
                throw new InternalError("CodePointTrie data header has a different type or value width than required");
            }
            dataNullOffset |= (options & 0xF00) << 8;
            int highStart = shiftedHighStart << 9;
            int actualLength = indexLength * 2;
            actualLength = valueWidth == ValueWidth.BITS_16 ? (actualLength += dataLength * 2) : (valueWidth == ValueWidth.BITS_32 ? (actualLength += dataLength * 4) : (actualLength += (dataLength |= (options & 0xF000) << 4)));
            if (bytes.remaining() < actualLength) {
                throw new InternalError("Buffer too short for the CodePointTrie data");
            }
            char[] index = ICUBinary.getChars(bytes, indexLength, 0);
            switch (valueWidth.ordinal()) {
                case 0: {
                    char[] data16 = ICUBinary.getChars(bytes, dataLength, 0);
                    CodePointTrie codePointTrie = type == Type.FAST ? new Fast16(index, data16, highStart, (int)index3NullOffset, dataNullOffset) : new Small16(index, data16, highStart, (int)index3NullOffset, dataNullOffset);
                    return codePointTrie;
                }
                case 1: {
                    int[] data32 = ICUBinary.getInts(bytes, dataLength, 0);
                    CodePointTrie codePointTrie = type == Type.FAST ? new Fast32(index, data32, highStart, (int)index3NullOffset, dataNullOffset) : new Small32(index, data32, highStart, (int)index3NullOffset, dataNullOffset);
                    return codePointTrie;
                }
                case 2: {
                    byte[] data8 = ICUBinary.getBytes(bytes, dataLength, 0);
                    CodePointTrie codePointTrie = type == Type.FAST ? new Fast8(index, data8, highStart, (int)index3NullOffset, dataNullOffset) : new Small8(index, data8, highStart, (int)index3NullOffset, dataNullOffset);
                    return codePointTrie;
                }
            }
            throw new AssertionError((Object)"should be unreachable");
        }
        finally {
            bytes.order(outerByteOrder);
        }
    }

    public abstract Type getType();

    public final ValueWidth getValueWidth() {
        return this.data.getValueWidth();
    }

    @Override
    public int get(int c) {
        return this.data.getFromIndex(this.cpIndex(c));
    }

    public final int asciiGet(int c) {
        return this.ascii[c];
    }

    private static final int maybeFilterValue(int value, int trieNullValue, int nullValue, CodePointMap.ValueFilter filter) {
        if (value == trieNullValue) {
            value = nullValue;
        } else if (filter != null) {
            value = filter.apply(value);
        }
        return value;
    }

    @Override
    public final boolean getRange(int start, CodePointMap.ValueFilter filter, CodePointMap.Range range) {
        if (start < 0 || 0x10FFFF < start) {
            return false;
        }
        if (start >= this.highStart) {
            int di = this.dataLength - 2;
            int value = this.data.getFromIndex(di);
            if (filter != null) {
                value = filter.apply(value);
            }
            range.set(start, 0x10FFFF, value);
            return true;
        }
        int nullValue = this.nullValue;
        if (filter != null) {
            nullValue = filter.apply(nullValue);
        }
        Type type = this.getType();
        int prevI3Block = -1;
        int prevBlock = -1;
        int c = start;
        int trieValue = 0;
        int value = 0;
        boolean haveValue = false;
        do {
            int dataBlockLength;
            int i3BlockLength;
            int i3;
            int i3Block;
            if (c <= 65535 && (type == Type.FAST || c <= 4095)) {
                i3Block = 0;
                i3 = c >> 6;
                i3BlockLength = type == Type.FAST ? 1024 : 64;
                dataBlockLength = 64;
            } else {
                int i1 = c >> 14;
                if (type == Type.FAST) {
                    assert (65535 < c && c < this.highStart);
                    i1 += 1020;
                } else {
                    assert (c < this.highStart && this.highStart > 4096);
                    i1 += 64;
                }
                i3Block = this.index[this.index[i1] + (c >> 9 & 0x1F)];
                if (i3Block == prevI3Block && c - start >= 512) {
                    assert ((c & 0x1FF) == 0);
                    c += 512;
                    continue;
                }
                prevI3Block = i3Block;
                if (i3Block == this.index3NullOffset) {
                    if (haveValue) {
                        if (nullValue != value) {
                            range.set(start, c - 1, value);
                            return true;
                        }
                    } else {
                        trieValue = this.nullValue;
                        value = nullValue;
                        haveValue = true;
                    }
                    prevBlock = this.dataNullOffset;
                    c = c + 512 & 0xFFFFFE00;
                    continue;
                }
                i3 = c >> 4 & 0x1F;
                i3BlockLength = 32;
                dataBlockLength = 16;
            }
            do {
                int block;
                if ((i3Block & 0x8000) == 0) {
                    block = this.index[i3Block + i3];
                } else {
                    int group = (i3Block & Short.MAX_VALUE) + (i3 & 0xFFFFFFF8) + (i3 >> 3);
                    int gi = i3 & 7;
                    block = this.index[group++] << 2 + 2 * gi & 0x30000;
                    block |= this.index[group + gi];
                }
                if (block == prevBlock && c - start >= dataBlockLength) {
                    assert ((c & dataBlockLength - 1) == 0);
                    c += dataBlockLength;
                    continue;
                }
                int dataMask = dataBlockLength - 1;
                prevBlock = block;
                if (block == this.dataNullOffset) {
                    if (haveValue) {
                        if (nullValue != value) {
                            range.set(start, c - 1, value);
                            return true;
                        }
                    } else {
                        trieValue = this.nullValue;
                        value = nullValue;
                        haveValue = true;
                    }
                    c = c + dataBlockLength & ~dataMask;
                    continue;
                }
                int di = block + (c & dataMask);
                int trieValue2 = this.data.getFromIndex(di);
                if (haveValue) {
                    if (trieValue2 != trieValue) {
                        if (filter == null || CodePointTrie.maybeFilterValue(trieValue2, this.nullValue, nullValue, filter) != value) {
                            range.set(start, c - 1, value);
                            return true;
                        }
                        trieValue = trieValue2;
                    }
                } else {
                    trieValue = trieValue2;
                    value = CodePointTrie.maybeFilterValue(trieValue2, this.nullValue, nullValue, filter);
                    haveValue = true;
                }
                while ((++c & dataMask) != 0) {
                    if ((trieValue2 = this.data.getFromIndex(++di)) == trieValue) continue;
                    if (filter == null || CodePointTrie.maybeFilterValue(trieValue2, this.nullValue, nullValue, filter) != value) {
                        range.set(start, c - 1, value);
                        return true;
                    }
                    trieValue = trieValue2;
                }
            } while (++i3 < i3BlockLength);
        } while (c < this.highStart);
        assert (haveValue);
        int di = this.dataLength - 2;
        int highValue = this.data.getFromIndex(di);
        c = CodePointTrie.maybeFilterValue(highValue, this.nullValue, nullValue, filter) != value ? --c : 0x10FFFF;
        range.set(start, c, value);
        return true;
    }

    public final int toBinary(OutputStream os) {
        try {
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeInt(1416784179);
            dos.writeChar((this.dataLength & 0xF0000) >> 4 | (this.dataNullOffset & 0xF0000) >> 8 | this.getType().ordinal() << 6 | this.getValueWidth().ordinal());
            dos.writeChar(this.index.length);
            dos.writeChar(this.dataLength);
            dos.writeChar(this.index3NullOffset);
            dos.writeChar(this.dataNullOffset);
            dos.writeChar(this.highStart >> 9);
            int length = 16;
            for (char i : this.index) {
                dos.writeChar(i);
            }
            length += this.index.length * 2;
            return length += this.data.write(dos);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Deprecated
    protected final int fastIndex(int c) {
        return this.index[c >> 6] + (c & 0x3F);
    }

    @Deprecated
    protected final int smallIndex(Type type, int c) {
        if (c >= this.highStart) {
            return this.dataLength - 2;
        }
        return this.internalSmallIndex(type, c);
    }

    private final int internalSmallIndex(Type type, int c) {
        int dataBlock;
        int i1 = c >> 14;
        if (type == Type.FAST) {
            assert (65535 < c && c < this.highStart);
            i1 += 1020;
        } else {
            assert (0 <= c && c < this.highStart && this.highStart > 4096);
            i1 += 64;
        }
        int i3Block = this.index[this.index[i1] + (c >> 9 & 0x1F)];
        int i3 = c >> 4 & 0x1F;
        if ((i3Block & 0x8000) == 0) {
            dataBlock = this.index[i3Block + i3];
        } else {
            i3Block = (i3Block & Short.MAX_VALUE) + (i3 & 0xFFFFFFF8) + (i3 >> 3);
            dataBlock = this.index[i3Block++] << 2 + 2 * (i3 &= 7) & 0x30000;
            dataBlock |= this.index[i3Block + i3];
        }
        return dataBlock + (c & 0xF);
    }

    @Deprecated
    protected abstract int cpIndex(int var1);

    private static abstract class Data {
        private Data() {
        }

        abstract ValueWidth getValueWidth();

        abstract int getDataLength();

        abstract int getFromIndex(int var1);

        abstract int write(DataOutputStream var1) throws IOException;
    }

    public static enum Type {
        FAST,
        SMALL;

    }

    public static enum ValueWidth {
        BITS_16,
        BITS_32,
        BITS_8;

    }

    public static final class Fast16
    extends Fast {
        private final char[] dataArray;

        Fast16(char[] index, char[] data16, int highStart, int index3NullOffset, int dataNullOffset) {
            super(index, new Data16(data16), highStart, index3NullOffset, dataNullOffset);
            this.dataArray = data16;
        }

        public static Fast16 fromBinary(ByteBuffer bytes) {
            return (Fast16)CodePointTrie.fromBinary(Type.FAST, ValueWidth.BITS_16, bytes);
        }

        @Override
        public final int get(int c) {
            return this.dataArray[this.cpIndex(c)];
        }

        @Override
        public final int bmpGet(int c) {
            assert (0 <= c && c <= 65535);
            return this.dataArray[this.fastIndex(c)];
        }

        @Override
        public final int suppGet(int c) {
            assert (65536 <= c && c <= 0x10FFFF);
            return this.dataArray[this.smallIndex(Type.FAST, c)];
        }
    }

    public static final class Small16
    extends Small {
        Small16(char[] index, char[] data16, int highStart, int index3NullOffset, int dataNullOffset) {
            super(index, new Data16(data16), highStart, index3NullOffset, dataNullOffset);
        }

        public static Small16 fromBinary(ByteBuffer bytes) {
            return (Small16)CodePointTrie.fromBinary(Type.SMALL, ValueWidth.BITS_16, bytes);
        }
    }

    public static final class Fast32
    extends Fast {
        private final int[] dataArray;

        Fast32(char[] index, int[] data32, int highStart, int index3NullOffset, int dataNullOffset) {
            super(index, new Data32(data32), highStart, index3NullOffset, dataNullOffset);
            this.dataArray = data32;
        }

        public static Fast32 fromBinary(ByteBuffer bytes) {
            return (Fast32)CodePointTrie.fromBinary(Type.FAST, ValueWidth.BITS_32, bytes);
        }

        @Override
        public final int get(int c) {
            return this.dataArray[this.cpIndex(c)];
        }

        @Override
        public final int bmpGet(int c) {
            assert (0 <= c && c <= 65535);
            return this.dataArray[this.fastIndex(c)];
        }

        @Override
        public final int suppGet(int c) {
            assert (65536 <= c && c <= 0x10FFFF);
            return this.dataArray[this.smallIndex(Type.FAST, c)];
        }
    }

    public static final class Small32
    extends Small {
        Small32(char[] index, int[] data32, int highStart, int index3NullOffset, int dataNullOffset) {
            super(index, new Data32(data32), highStart, index3NullOffset, dataNullOffset);
        }

        public static Small32 fromBinary(ByteBuffer bytes) {
            return (Small32)CodePointTrie.fromBinary(Type.SMALL, ValueWidth.BITS_32, bytes);
        }
    }

    public static final class Fast8
    extends Fast {
        private final byte[] dataArray;

        Fast8(char[] index, byte[] data8, int highStart, int index3NullOffset, int dataNullOffset) {
            super(index, new Data8(data8), highStart, index3NullOffset, dataNullOffset);
            this.dataArray = data8;
        }

        public static Fast8 fromBinary(ByteBuffer bytes) {
            return (Fast8)CodePointTrie.fromBinary(Type.FAST, ValueWidth.BITS_8, bytes);
        }

        @Override
        public final int get(int c) {
            return this.dataArray[this.cpIndex(c)] & 0xFF;
        }

        @Override
        public final int bmpGet(int c) {
            assert (0 <= c && c <= 65535);
            return this.dataArray[this.fastIndex(c)] & 0xFF;
        }

        @Override
        public final int suppGet(int c) {
            assert (65536 <= c && c <= 0x10FFFF);
            return this.dataArray[this.smallIndex(Type.FAST, c)] & 0xFF;
        }
    }

    public static final class Small8
    extends Small {
        Small8(char[] index, byte[] data8, int highStart, int index3NullOffset, int dataNullOffset) {
            super(index, new Data8(data8), highStart, index3NullOffset, dataNullOffset);
        }

        public static Small8 fromBinary(ByteBuffer bytes) {
            return (Small8)CodePointTrie.fromBinary(Type.SMALL, ValueWidth.BITS_8, bytes);
        }
    }

    public static abstract class Small
    extends CodePointTrie {
        private Small(char[] index, Data data, int highStart, int index3NullOffset, int dataNullOffset) {
            super(index, data, highStart, index3NullOffset, dataNullOffset);
        }

        public static Small fromBinary(ValueWidth valueWidth, ByteBuffer bytes) {
            return (Small)CodePointTrie.fromBinary(Type.SMALL, valueWidth, bytes);
        }

        @Override
        public final Type getType() {
            return Type.SMALL;
        }

        @Override
        @Deprecated
        protected final int cpIndex(int c) {
            if (c >= 0) {
                if (c <= 4095) {
                    return this.fastIndex(c);
                }
                if (c <= 0x10FFFF) {
                    return this.smallIndex(Type.SMALL, c);
                }
            }
            return this.dataLength - 1;
        }

        @Override
        public final CodePointMap.StringIterator stringIterator(CharSequence s, int sIndex) {
            return new SmallStringIterator(s, sIndex);
        }

        private final class SmallStringIterator
        extends CodePointMap.StringIterator {
            private SmallStringIterator(CharSequence s, int sIndex) {
                super(s, sIndex);
            }

            @Override
            public boolean next() {
                char trail;
                int dataIndex;
                if (this.sIndex >= this.s.length()) {
                    return false;
                }
                char lead = this.s.charAt(this.sIndex++);
                this.c = lead;
                if (!Character.isSurrogate(lead)) {
                    dataIndex = Small.this.cpIndex(this.c);
                } else if (NormalizerImpl.UTF16Plus.isSurrogateLead(lead) && this.sIndex < this.s.length() && Character.isLowSurrogate(trail = this.s.charAt(this.sIndex))) {
                    ++this.sIndex;
                    this.c = Character.toCodePoint(lead, trail);
                    dataIndex = Small.this.smallIndex(Type.SMALL, this.c);
                } else {
                    dataIndex = Small.this.dataLength - 1;
                }
                this.value = Small.this.data.getFromIndex(dataIndex);
                return true;
            }

            @Override
            public boolean previous() {
                char lead;
                int dataIndex;
                if (this.sIndex <= 0) {
                    return false;
                }
                char trail = this.s.charAt(--this.sIndex);
                this.c = trail;
                if (!Character.isSurrogate(trail)) {
                    dataIndex = Small.this.cpIndex(this.c);
                } else if (!NormalizerImpl.UTF16Plus.isSurrogateLead(trail) && this.sIndex > 0 && Character.isHighSurrogate(lead = this.s.charAt(this.sIndex - 1))) {
                    --this.sIndex;
                    this.c = Character.toCodePoint(lead, trail);
                    dataIndex = Small.this.smallIndex(Type.SMALL, this.c);
                } else {
                    dataIndex = Small.this.dataLength - 1;
                }
                this.value = Small.this.data.getFromIndex(dataIndex);
                return true;
            }
        }
    }

    public static abstract class Fast
    extends CodePointTrie {
        private Fast(char[] index, Data data, int highStart, int index3NullOffset, int dataNullOffset) {
            super(index, data, highStart, index3NullOffset, dataNullOffset);
        }

        public static Fast fromBinary(ValueWidth valueWidth, ByteBuffer bytes) {
            return (Fast)CodePointTrie.fromBinary(Type.FAST, valueWidth, bytes);
        }

        @Override
        public final Type getType() {
            return Type.FAST;
        }

        public abstract int bmpGet(int var1);

        public abstract int suppGet(int var1);

        @Override
        @Deprecated
        protected final int cpIndex(int c) {
            if (c >= 0) {
                if (c <= 65535) {
                    return this.fastIndex(c);
                }
                if (c <= 0x10FFFF) {
                    return this.smallIndex(Type.FAST, c);
                }
            }
            return this.dataLength - 1;
        }

        @Override
        public final CodePointMap.StringIterator stringIterator(CharSequence s, int sIndex) {
            return new FastStringIterator(s, sIndex);
        }

        private final class FastStringIterator
        extends CodePointMap.StringIterator {
            private FastStringIterator(CharSequence s, int sIndex) {
                super(s, sIndex);
            }

            @Override
            public boolean next() {
                char trail;
                int dataIndex;
                if (this.sIndex >= this.s.length()) {
                    return false;
                }
                char lead = this.s.charAt(this.sIndex++);
                this.c = lead;
                if (!Character.isSurrogate(lead)) {
                    dataIndex = Fast.this.fastIndex(this.c);
                } else if (NormalizerImpl.UTF16Plus.isSurrogateLead(lead) && this.sIndex < this.s.length() && Character.isLowSurrogate(trail = this.s.charAt(this.sIndex))) {
                    ++this.sIndex;
                    this.c = Character.toCodePoint(lead, trail);
                    dataIndex = Fast.this.smallIndex(Type.FAST, this.c);
                } else {
                    dataIndex = Fast.this.dataLength - 1;
                }
                this.value = Fast.this.data.getFromIndex(dataIndex);
                return true;
            }

            @Override
            public boolean previous() {
                char lead;
                int dataIndex;
                if (this.sIndex <= 0) {
                    return false;
                }
                char trail = this.s.charAt(--this.sIndex);
                this.c = trail;
                if (!Character.isSurrogate(trail)) {
                    dataIndex = Fast.this.fastIndex(this.c);
                } else if (!NormalizerImpl.UTF16Plus.isSurrogateLead(trail) && this.sIndex > 0 && Character.isHighSurrogate(lead = this.s.charAt(this.sIndex - 1))) {
                    --this.sIndex;
                    this.c = Character.toCodePoint(lead, trail);
                    dataIndex = Fast.this.smallIndex(Type.FAST, this.c);
                } else {
                    dataIndex = Fast.this.dataLength - 1;
                }
                this.value = Fast.this.data.getFromIndex(dataIndex);
                return true;
            }
        }
    }

    private static final class Data8
    extends Data {
        byte[] array;

        Data8(byte[] a) {
            this.array = a;
        }

        @Override
        ValueWidth getValueWidth() {
            return ValueWidth.BITS_8;
        }

        @Override
        int getDataLength() {
            return this.array.length;
        }

        @Override
        int getFromIndex(int index) {
            return this.array[index] & 0xFF;
        }

        @Override
        int write(DataOutputStream dos) throws IOException {
            for (byte v : this.array) {
                dos.writeByte(v);
            }
            return this.array.length;
        }
    }

    private static final class Data32
    extends Data {
        int[] array;

        Data32(int[] a) {
            this.array = a;
        }

        @Override
        ValueWidth getValueWidth() {
            return ValueWidth.BITS_32;
        }

        @Override
        int getDataLength() {
            return this.array.length;
        }

        @Override
        int getFromIndex(int index) {
            return this.array[index];
        }

        @Override
        int write(DataOutputStream dos) throws IOException {
            for (int v : this.array) {
                dos.writeInt(v);
            }
            return this.array.length * 4;
        }
    }

    private static final class Data16
    extends Data {
        char[] array;

        Data16(char[] a) {
            this.array = a;
        }

        @Override
        ValueWidth getValueWidth() {
            return ValueWidth.BITS_16;
        }

        @Override
        int getDataLength() {
            return this.array.length;
        }

        @Override
        int getFromIndex(int index) {
            return this.array[index];
        }

        @Override
        int write(DataOutputStream dos) throws IOException {
            for (char v : this.array) {
                dos.writeChar(v);
            }
            return this.array.length * 2;
        }
    }
}

