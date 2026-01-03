/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.util.Comparator;

public class ByteArrayLexOrder
implements Comparator<byte[]> {
    @Override
    public final int compare(byte[] bytes1, byte[] bytes2) {
        for (int i = 0; i < bytes1.length && i < bytes2.length; ++i) {
            int diff = (bytes1[i] & 0xFF) - (bytes2[i] & 0xFF);
            if (diff == 0) continue;
            return diff;
        }
        return bytes1.length - bytes2.length;
    }
}

