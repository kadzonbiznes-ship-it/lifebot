/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.icu.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import jdk.internal.icu.impl.Trie2;

public final class Trie2_16
extends Trie2 {
    Trie2_16() {
    }

    public static Trie2_16 createFromSerialized(ByteBuffer bytes) throws IOException {
        return (Trie2_16)Trie2.createFromSerialized(bytes);
    }

    @Override
    public final int get(int codePoint) {
        if (codePoint >= 0) {
            if (codePoint < 55296 || codePoint > 56319 && codePoint <= 65535) {
                int ix = this.index[codePoint >> 5];
                ix = (ix << 2) + (codePoint & 0x1F);
                char value = this.index[ix];
                return value;
            }
            if (codePoint <= 65535) {
                int ix = this.index[2048 + (codePoint - 55296 >> 5)];
                ix = (ix << 2) + (codePoint & 0x1F);
                char value = this.index[ix];
                return value;
            }
            if (codePoint < this.highStart) {
                int ix = 2080 + (codePoint >> 11);
                ix = this.index[ix];
                ix += codePoint >> 5 & 0x3F;
                ix = this.index[ix];
                ix = (ix << 2) + (codePoint & 0x1F);
                char value = this.index[ix];
                return value;
            }
            if (codePoint <= 0x10FFFF) {
                char value = this.index[this.highValueIndex];
                return value;
            }
        }
        return this.errorValue;
    }

    @Override
    public int getFromU16SingleLead(char codeUnit) {
        int ix = this.index[codeUnit >> 5];
        ix = (ix << 2) + (codeUnit & 0x1F);
        char value = this.index[ix];
        return value;
    }

    public int getSerializedLength() {
        return 16 + (this.header.indexLength + this.dataLength) * 2;
    }
}

