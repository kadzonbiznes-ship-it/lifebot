/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import sun.security.ssl.Record;

interface SSLRecord
extends Record {
    public static final int headerSize = 5;
    public static final int handshakeHeaderSize = 4;
    public static final int headerPlusMaxIVSize = 21;
    public static final int maxPlaintextPlusSize = 325;
    public static final int maxRecordSize = 16709;
    public static final int maxLargeRecordSize = 33093;
    public static final byte[] v2NoCipher = new byte[]{-128, 3, 0, 0, 1};
}

