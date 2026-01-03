/*
 * Decompiled with CFR 0.152.
 */
package java.nio;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import jdk.internal.misc.ScopedMemoryAccess;
import jdk.internal.util.ArraysSupport;

final class BufferMismatch {
    static final ScopedMemoryAccess SCOPED_MEMORY_ACCESS = ScopedMemoryAccess.getScopedMemoryAccess();

    BufferMismatch() {
    }

    static int mismatch(ByteBuffer a, int aOff, ByteBuffer b, int bOff, int length) {
        int i = 0;
        if (length > 7) {
            if (a.get(aOff) != b.get(bOff)) {
                return 0;
            }
            i = SCOPED_MEMORY_ACCESS.vectorizedMismatch(a.session(), b.session(), a.base(), a.address + (long)aOff, b.base(), b.address + (long)bOff, length, ArraysSupport.LOG2_ARRAY_BYTE_INDEX_SCALE);
            if (i >= 0) {
                return i;
            }
            i = length - ~i;
        }
        while (i < length) {
            if (a.get(aOff + i) != b.get(bOff + i)) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    static int mismatch(CharBuffer a, int aOff, CharBuffer b, int bOff, int length) {
        int i = 0;
        if (length > 3 && a.charRegionOrder() == b.charRegionOrder() && a.charRegionOrder() != null && b.charRegionOrder() != null) {
            if (a.get(aOff) != b.get(bOff)) {
                return 0;
            }
            i = SCOPED_MEMORY_ACCESS.vectorizedMismatch(a.session(), b.session(), a.base(), a.address + (long)(aOff << ArraysSupport.LOG2_ARRAY_CHAR_INDEX_SCALE), b.base(), b.address + (long)(bOff << ArraysSupport.LOG2_ARRAY_CHAR_INDEX_SCALE), length, ArraysSupport.LOG2_ARRAY_CHAR_INDEX_SCALE);
            if (i >= 0) {
                return i;
            }
            i = length - ~i;
        }
        while (i < length) {
            if (a.get(aOff + i) != b.get(bOff + i)) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    static int mismatch(ShortBuffer a, int aOff, ShortBuffer b, int bOff, int length) {
        int i = 0;
        if (length > 3 && a.order() == b.order()) {
            if (a.get(aOff) != b.get(bOff)) {
                return 0;
            }
            i = SCOPED_MEMORY_ACCESS.vectorizedMismatch(a.session(), b.session(), a.base(), a.address + (long)(aOff << ArraysSupport.LOG2_ARRAY_SHORT_INDEX_SCALE), b.base(), b.address + (long)(bOff << ArraysSupport.LOG2_ARRAY_SHORT_INDEX_SCALE), length, ArraysSupport.LOG2_ARRAY_SHORT_INDEX_SCALE);
            if (i >= 0) {
                return i;
            }
            i = length - ~i;
        }
        while (i < length) {
            if (a.get(aOff + i) != b.get(bOff + i)) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    static int mismatch(IntBuffer a, int aOff, IntBuffer b, int bOff, int length) {
        int i = 0;
        if (length > 1 && a.order() == b.order()) {
            if (a.get(aOff) != b.get(bOff)) {
                return 0;
            }
            i = SCOPED_MEMORY_ACCESS.vectorizedMismatch(a.session(), b.session(), a.base(), a.address + (long)(aOff << ArraysSupport.LOG2_ARRAY_INT_INDEX_SCALE), b.base(), b.address + (long)(bOff << ArraysSupport.LOG2_ARRAY_INT_INDEX_SCALE), length, ArraysSupport.LOG2_ARRAY_INT_INDEX_SCALE);
            if (i >= 0) {
                return i;
            }
            i = length - ~i;
        }
        while (i < length) {
            if (a.get(aOff + i) != b.get(bOff + i)) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    static int mismatch(FloatBuffer a, int aOff, FloatBuffer b, int bOff, int length) {
        float bv;
        float av;
        int i = 0;
        if (length > 1 && a.order() == b.order()) {
            if (Float.floatToRawIntBits(a.get(aOff)) == Float.floatToRawIntBits(b.get(bOff))) {
                i = SCOPED_MEMORY_ACCESS.vectorizedMismatch(a.session(), b.session(), a.base(), a.address + (long)(aOff << ArraysSupport.LOG2_ARRAY_FLOAT_INDEX_SCALE), b.base(), b.address + (long)(bOff << ArraysSupport.LOG2_ARRAY_FLOAT_INDEX_SCALE), length, ArraysSupport.LOG2_ARRAY_FLOAT_INDEX_SCALE);
            }
            if (i >= 0) {
                av = a.get(aOff + i);
                if (!(av == (bv = b.get(bOff + i)) || Float.isNaN(av) && Float.isNaN(bv))) {
                    return i;
                }
                ++i;
            } else {
                i = length - ~i;
            }
        }
        while (i < length) {
            av = a.get(aOff + i);
            if (!(av == (bv = b.get(bOff + i)) || Float.isNaN(av) && Float.isNaN(bv))) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    static int mismatch(LongBuffer a, int aOff, LongBuffer b, int bOff, int length) {
        int i;
        if (length > 0 && a.order() == b.order()) {
            if (a.get(aOff) != b.get(bOff)) {
                return 0;
            }
            i = SCOPED_MEMORY_ACCESS.vectorizedMismatch(a.session(), b.session(), a.base(), a.address + (long)(aOff << ArraysSupport.LOG2_ARRAY_LONG_INDEX_SCALE), b.base(), b.address + (long)(bOff << ArraysSupport.LOG2_ARRAY_LONG_INDEX_SCALE), length, ArraysSupport.LOG2_ARRAY_LONG_INDEX_SCALE);
            return i >= 0 ? i : -1;
        }
        for (i = 0; i < length; ++i) {
            if (a.get(aOff + i) == b.get(bOff + i)) continue;
            return i;
        }
        return -1;
    }

    static int mismatch(DoubleBuffer a, int aOff, DoubleBuffer b, int bOff, int length) {
        double bv;
        double av;
        int i = 0;
        if (length > 0 && a.order() == b.order()) {
            if (Double.doubleToRawLongBits(a.get(aOff)) == Double.doubleToRawLongBits(b.get(bOff))) {
                i = SCOPED_MEMORY_ACCESS.vectorizedMismatch(a.session(), b.session(), a.base(), a.address + (long)(aOff << ArraysSupport.LOG2_ARRAY_DOUBLE_INDEX_SCALE), b.base(), b.address + (long)(bOff << ArraysSupport.LOG2_ARRAY_DOUBLE_INDEX_SCALE), length, ArraysSupport.LOG2_ARRAY_DOUBLE_INDEX_SCALE);
            }
            if (i >= 0) {
                av = a.get(aOff + i);
                if (!(av == (bv = b.get(bOff + i)) || Double.isNaN(av) && Double.isNaN(bv))) {
                    return i;
                }
                ++i;
            } else {
                return -1;
            }
        }
        while (i < length) {
            av = a.get(aOff + i);
            if (!(av == (bv = b.get(bOff + i)) || Double.isNaN(av) && Double.isNaN(bv))) {
                return i;
            }
            ++i;
        }
        return -1;
    }
}

