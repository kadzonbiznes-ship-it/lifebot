/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.CipherSpi;

public abstract class MacSpi {
    protected abstract int engineGetMacLength();

    protected abstract void engineInit(Key var1, AlgorithmParameterSpec var2) throws InvalidKeyException, InvalidAlgorithmParameterException;

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
            int chunk;
            int len;
            byte[] b = new byte[CipherSpi.getTempArraySize(len)];
            for (len = input.remaining(); len > 0; len -= chunk) {
                chunk = Math.min(len, b.length);
                input.get(b, 0, chunk);
                this.engineUpdate(b, 0, chunk);
            }
        }
    }

    protected abstract byte[] engineDoFinal();

    protected abstract void engineReset();

    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        }
        throw new CloneNotSupportedException();
    }
}

