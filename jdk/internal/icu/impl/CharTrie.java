/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.icu.impl;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import jdk.internal.icu.impl.Trie;

public class CharTrie
extends Trie {
    private char m_initialValue_;
    private char[] m_data_;

    public CharTrie(InputStream inputStream, Trie.DataManipulate dataManipulate) throws IOException {
        super(inputStream, dataManipulate);
        if (!this.isCharTrie()) {
            throw new IllegalArgumentException("Data given does not belong to a char trie.");
        }
    }

    public final char getCodePointValue(int ch) {
        if (0 <= ch && ch < 55296) {
            int offset = (this.m_index_[ch >> 5] << 2) + (ch & 0x1F);
            return this.m_data_[offset];
        }
        int offset = this.getCodePointOffset(ch);
        return offset >= 0 ? this.m_data_[offset] : this.m_initialValue_;
    }

    public final char getLeadValue(char ch) {
        return this.m_data_[this.getLeadOffset(ch)];
    }

    @Override
    protected final void unserialize(InputStream inputStream) throws IOException {
        DataInputStream input = new DataInputStream(inputStream);
        int indexDataLength = this.m_dataOffset_ + this.m_dataLength_;
        this.m_index_ = new char[indexDataLength];
        for (int i = 0; i < indexDataLength; ++i) {
            this.m_index_[i] = input.readChar();
        }
        this.m_data_ = this.m_index_;
        this.m_initialValue_ = this.m_data_[this.m_dataOffset_];
    }

    @Override
    protected final int getSurrogateOffset(char lead, char trail) {
        if (this.m_dataManipulate_ == null) {
            throw new NullPointerException("The field DataManipulate in this Trie is null");
        }
        int offset = this.m_dataManipulate_.getFoldingOffset(this.getLeadValue(lead));
        if (offset > 0) {
            return this.getRawOffset(offset, (char)(trail & 0x3FF));
        }
        return -1;
    }
}

