/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.security.spec.AlgorithmParameterSpec;

public class ECKeySizeParameterSpec
implements AlgorithmParameterSpec {
    private final int keySize;

    public ECKeySizeParameterSpec(int keySize) {
        this.keySize = keySize;
    }

    public int getKeySize() {
        return this.keySize;
    }
}

