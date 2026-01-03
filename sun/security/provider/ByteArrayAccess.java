/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

final class ByteArrayAccess {
    private ByteArrayAccess() {
    }

    static void i2bLittle(int[] in, int inOfs, byte[] out, int outOfs, int len) {
        len += outOfs;
        while (outOfs < len) {
            LE.INT_ARRAY.set(out, outOfs, in[inOfs++]);
            outOfs += 4;
        }
    }

    static void i2bLittle4(int val, byte[] out, int outOfs) {
        LE.INT_ARRAY.set(out, outOfs, val);
    }

    static void b2iBig(byte[] in, int inOfs, int[] out, int outOfs, int len) {
        len += inOfs;
        while (inOfs < len) {
            out[outOfs++] = BE.INT_ARRAY.get(in, inOfs);
            inOfs += 4;
        }
    }

    static void b2iBig64(byte[] in, int inOfs, int[] out) {
        out[0] = BE.INT_ARRAY.get(in, inOfs);
        out[1] = BE.INT_ARRAY.get(in, inOfs + 4);
        out[2] = BE.INT_ARRAY.get(in, inOfs + 8);
        out[3] = BE.INT_ARRAY.get(in, inOfs + 12);
        out[4] = BE.INT_ARRAY.get(in, inOfs + 16);
        out[5] = BE.INT_ARRAY.get(in, inOfs + 20);
        out[6] = BE.INT_ARRAY.get(in, inOfs + 24);
        out[7] = BE.INT_ARRAY.get(in, inOfs + 28);
        out[8] = BE.INT_ARRAY.get(in, inOfs + 32);
        out[9] = BE.INT_ARRAY.get(in, inOfs + 36);
        out[10] = BE.INT_ARRAY.get(in, inOfs + 40);
        out[11] = BE.INT_ARRAY.get(in, inOfs + 44);
        out[12] = BE.INT_ARRAY.get(in, inOfs + 48);
        out[13] = BE.INT_ARRAY.get(in, inOfs + 52);
        out[14] = BE.INT_ARRAY.get(in, inOfs + 56);
        out[15] = BE.INT_ARRAY.get(in, inOfs + 60);
    }

    static void i2bBig(int[] in, int inOfs, byte[] out, int outOfs, int len) {
        len += outOfs;
        while (outOfs < len) {
            BE.INT_ARRAY.set(out, outOfs, in[inOfs++]);
            outOfs += 4;
        }
    }

    static void i2bBig4(int val, byte[] out, int outOfs) {
        BE.INT_ARRAY.set(out, outOfs, val);
    }

    static void b2lBig(byte[] in, int inOfs, long[] out, int outOfs, int len) {
        len += inOfs;
        while (inOfs < len) {
            out[outOfs++] = BE.LONG_ARRAY.get(in, inOfs);
            inOfs += 8;
        }
    }

    static void b2lBig128(byte[] in, int inOfs, long[] out) {
        out[0] = BE.LONG_ARRAY.get(in, inOfs);
        out[1] = BE.LONG_ARRAY.get(in, inOfs + 8);
        out[2] = BE.LONG_ARRAY.get(in, inOfs + 16);
        out[3] = BE.LONG_ARRAY.get(in, inOfs + 24);
        out[4] = BE.LONG_ARRAY.get(in, inOfs + 32);
        out[5] = BE.LONG_ARRAY.get(in, inOfs + 40);
        out[6] = BE.LONG_ARRAY.get(in, inOfs + 48);
        out[7] = BE.LONG_ARRAY.get(in, inOfs + 56);
        out[8] = BE.LONG_ARRAY.get(in, inOfs + 64);
        out[9] = BE.LONG_ARRAY.get(in, inOfs + 72);
        out[10] = BE.LONG_ARRAY.get(in, inOfs + 80);
        out[11] = BE.LONG_ARRAY.get(in, inOfs + 88);
        out[12] = BE.LONG_ARRAY.get(in, inOfs + 96);
        out[13] = BE.LONG_ARRAY.get(in, inOfs + 104);
        out[14] = BE.LONG_ARRAY.get(in, inOfs + 112);
        out[15] = BE.LONG_ARRAY.get(in, inOfs + 120);
    }

    static void l2bBig(long[] in, int inOfs, byte[] out, int outOfs, int len) {
        len += outOfs;
        while (outOfs < len) {
            BE.LONG_ARRAY.set(out, outOfs, in[inOfs++]);
            outOfs += 8;
        }
    }

    static void b2lLittle(byte[] in, int inOfs, long[] out, int outOfs, int len) {
        len += inOfs;
        while (inOfs < len) {
            out[outOfs++] = LE.LONG_ARRAY.get(in, inOfs);
            inOfs += 8;
        }
    }

    static void l2bLittle(long[] in, int inOfs, byte[] out, int outOfs, int len) {
        len += outOfs;
        while (outOfs < len) {
            LE.LONG_ARRAY.set(out, outOfs, in[inOfs++]);
            outOfs += 8;
        }
    }

    static final class LE {
        static final VarHandle INT_ARRAY = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior();
        static final VarHandle LONG_ARRAY = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior();

        LE() {
        }
    }

    static final class BE {
        static final VarHandle INT_ARRAY = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN).withInvokeExactBehavior();
        static final VarHandle LONG_ARRAY = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN).withInvokeExactBehavior();

        BE() {
        }
    }
}

