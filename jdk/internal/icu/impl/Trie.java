/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.icu.impl;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import jdk.internal.icu.text.UTF16;

public abstract class Trie {
    protected static final int LEAD_INDEX_OFFSET_ = 320;
    protected static final int INDEX_STAGE_1_SHIFT_ = 5;
    protected static final int INDEX_STAGE_2_SHIFT_ = 2;
    protected static final int DATA_BLOCK_LENGTH = 32;
    protected static final int INDEX_STAGE_3_MASK_ = 31;
    protected static final int SURROGATE_MASK_ = 1023;
    protected char[] m_index_;
    protected DataManipulate m_dataManipulate_;
    protected int m_dataOffset_;
    protected int m_dataLength_;
    protected static final int HEADER_OPTIONS_LATIN1_IS_LINEAR_MASK_ = 512;
    protected static final int HEADER_SIGNATURE_ = 1416784229;
    private static final int HEADER_OPTIONS_SHIFT_MASK_ = 15;
    protected static final int HEADER_OPTIONS_INDEX_SHIFT_ = 4;
    protected static final int HEADER_OPTIONS_DATA_IS_32_BIT_ = 256;
    private boolean m_isLatin1Linear_;
    private int m_options_;

    protected Trie(InputStream inputStream, DataManipulate dataManipulate) throws IOException {
        DataInputStream input = new DataInputStream(inputStream);
        int signature = input.readInt();
        this.m_options_ = input.readInt();
        if (!this.checkHeader(signature)) {
            throw new IllegalArgumentException("ICU data file error: Trie header authentication failed, please check if you have the most updated ICU data file");
        }
        this.m_dataManipulate_ = dataManipulate != null ? dataManipulate : new DefaultGetFoldingOffset();
        this.m_isLatin1Linear_ = (this.m_options_ & 0x200) != 0;
        this.m_dataOffset_ = input.readInt();
        this.m_dataLength_ = input.readInt();
        this.unserialize(inputStream);
    }

    protected abstract int getSurrogateOffset(char var1, char var2);

    protected final int getRawOffset(int offset, char ch) {
        return (this.m_index_[offset + (ch >> 5)] << 2) + (ch & 0x1F);
    }

    protected final int getBMPOffset(char ch) {
        return ch >= '\ud800' && ch <= '\udbff' ? this.getRawOffset(320, ch) : this.getRawOffset(0, ch);
    }

    protected final int getLeadOffset(char ch) {
        return this.getRawOffset(0, ch);
    }

    protected final int getCodePointOffset(int ch) {
        if (ch < 0) {
            return -1;
        }
        if (ch < 55296) {
            return this.getRawOffset(0, (char)ch);
        }
        if (ch < 65536) {
            return this.getBMPOffset((char)ch);
        }
        if (ch <= 0x10FFFF) {
            return this.getSurrogateOffset(UTF16.getLeadSurrogate(ch), (char)(ch & 0x3FF));
        }
        return -1;
    }

    protected void unserialize(InputStream inputStream) throws IOException {
        this.m_index_ = new char[this.m_dataOffset_];
        DataInputStream input = new DataInputStream(inputStream);
        for (int i = 0; i < this.m_dataOffset_; ++i) {
            this.m_index_[i] = input.readChar();
        }
    }

    protected final boolean isCharTrie() {
        return (this.m_options_ & 0x100) == 0;
    }

    private final boolean checkHeader(int signature) {
        if (signature != 1416784229) {
            return false;
        }
        return (this.m_options_ & 0xF) == 5 && (this.m_options_ >> 4 & 0xF) == 2;
    }

    public static interface DataManipulate {
        public int getFoldingOffset(int var1);
    }

    private static class DefaultGetFoldingOffset
    implements DataManipulate {
        private DefaultGetFoldingOffset() {
        }

        @Override
        public int getFoldingOffset(int value) {
            return value;
        }
    }
}

