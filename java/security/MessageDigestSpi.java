/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.nio.ByteBuffer;
import java.security.DigestException;
import sun.security.jca.JCAUtil;

public abstract class MessageDigestSpi {
    private byte[] tempArray;

    protected int engineGetDigestLength() {
        return 0;
    }

    protected abstract void engineUpdate(byte var1);

    protected abstract void engineUpdate(byte[] var1, int var2, int var3);

    protected void engineUpdate(ByteBuffer input) {
        if (!input.hasRemaining()) {
            return;
        }
        if (input.hasArray()) {
            byte[] b = input.array();
            int ofs = input.arrayOffset();
            int pos = input.position();
            int lim = input.limit();
            this.engineUpdate(b, ofs + pos, lim - pos);
            input.position(lim);
        } else {
            int len = input.remaining();
            int n = JCAUtil.getTempArraySize(len);
            if (this.tempArray == null || n > this.tempArray.length) {
                this.tempArray = new byte[n];
            }
            while (len > 0) {
                int chunk = Math.min(len, this.tempArray.length);
                input.get(this.tempArray, 0, chunk);
                this.engineUpdate(this.tempArray, 0, chunk);
                len -= chunk;
            }
        }
    }

    protected abstract byte[] engineDigest();

    protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
        byte[] digest = this.engineDigest();
        if (len < digest.length) {
            throw new DigestException("partial digests not returned");
        }
        if (buf.length - offset < digest.length) {
            throw new DigestException("insufficient space in the output buffer to store the digest");
        }
        System.arraycopy(digest, 0, buf, offset, digest.length);
        return digest.length;
    }

    protected abstract void engineReset();

    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            MessageDigestSpi o = (MessageDigestSpi)super.clone();
            if (o.tempArray != null) {
                o.tempArray = (byte[])this.tempArray.clone();
            }
            return o;
        }
        throw new CloneNotSupportedException();
    }
}

