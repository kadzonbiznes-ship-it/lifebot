/*
 * Decompiled with CFR 0.152.
 */
package java.security.interfaces;

import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;

public interface RSAKey {
    public BigInteger getModulus();

    default public AlgorithmParameterSpec getParams() {
        return null;
    }
}

