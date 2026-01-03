/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto;

import javax.crypto.Cipher;
import javax.crypto.NullCipherSpi;

public class NullCipher
extends Cipher {
    public NullCipher() {
        super(new NullCipherSpi(), null);
    }
}

