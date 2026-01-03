/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.security.ProviderException;
import jdk.internal.util.Preconditions;

public final class ArrayUtil {
    public static void blockSizeCheck(int len, int blockSize) {
        if (len % blockSize != 0) {
            throw new ProviderException("Internal error in input buffering");
        }
    }

    public static void nullAndBoundsCheck(byte[] array, int offset, int len) {
        Preconditions.checkFromIndexSize(offset, len, array.length, Preconditions.AIOOBE_FORMATTER);
    }

    private static void swap(byte[] arr, int i, int j) {
        byte tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    public static void reverse(byte[] arr) {
        int i = 0;
        for (int j = arr.length - 1; i < j; ++i, --j) {
            ArrayUtil.swap(arr, i, j);
        }
    }
}

