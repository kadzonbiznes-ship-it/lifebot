/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.icu.impl;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import jdk.internal.icu.impl.ICUBinary;

public final class StringPrepDataReader
implements ICUBinary.Authenticate {
    private DataInputStream dataInputStream;
    private byte[] unicodeVersion;
    private static final byte[] DATA_FORMAT_ID = new byte[]{83, 80, 82, 80};
    private static final byte[] DATA_FORMAT_VERSION = new byte[]{3, 2, 5, 2};

    public StringPrepDataReader(InputStream inputStream) throws IOException {
        this.unicodeVersion = ICUBinary.readHeader(inputStream, DATA_FORMAT_ID, (ICUBinary.Authenticate)this);
        this.dataInputStream = new DataInputStream(inputStream);
    }

    public void read(byte[] idnaBytes, char[] mappingTable) throws IOException {
        this.dataInputStream.read(idnaBytes);
        for (int i = 0; i < mappingTable.length; ++i) {
            mappingTable[i] = this.dataInputStream.readChar();
        }
    }

    public byte[] getDataFormatVersion() {
        return DATA_FORMAT_VERSION;
    }

    @Override
    public boolean isDataVersionAcceptable(byte[] version) {
        return version[0] == DATA_FORMAT_VERSION[0] && version[2] == DATA_FORMAT_VERSION[2] && version[3] == DATA_FORMAT_VERSION[3];
    }

    public int[] readIndexes(int length) throws IOException {
        int[] indexes = new int[length];
        for (int i = 0; i < length; ++i) {
            indexes[i] = this.dataInputStream.readInt();
        }
        return indexes;
    }

    public byte[] getUnicodeVersion() {
        return this.unicodeVersion;
    }
}

