/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;

public class GCMParameterSpec
implements AlgorithmParameterSpec {
    private byte[] iv;
    private int tLen;

    public GCMParameterSpec(int tLen, byte[] src) {
        if (src == null) {
            throw new IllegalArgumentException("src array is null");
        }
        this.init(tLen, src, 0, src.length);
    }

    public GCMParameterSpec(int tLen, byte[] src, int offset, int len) {
        this.init(tLen, src, offset, len);
    }

    private void init(int tLen, byte[] src, int offset, int len) {
        if (tLen < 0) {
            throw new IllegalArgumentException("Length argument is negative");
        }
        this.tLen = tLen;
        if (src == null || len < 0 || offset < 0 || len > src.length - offset) {
            throw new IllegalArgumentException("Invalid buffer arguments");
        }
        this.iv = new byte[len];
        System.arraycopy(src, offset, this.iv, 0, len);
    }

    public int getTLen() {
        return this.tLen;
    }

    public byte[] getIV() {
        return (byte[])this.iv.clone();
    }
}

