/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class IOUtils {
    public static byte[] readExactlyNBytes(InputStream is, int length) throws IOException {
        if (length < 0) {
            throw new IOException("length cannot be negative: " + length);
        }
        byte[] data = is.readNBytes(length);
        if (data.length < length) {
            throw new EOFException();
        }
        return data;
    }
}

