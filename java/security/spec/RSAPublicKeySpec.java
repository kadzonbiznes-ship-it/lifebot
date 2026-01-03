/*
 * Decompiled with CFR 0.152.
 */
package java.security.spec;

import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

public class RSAPublicKeySpec
implements KeySpec {
    private final BigInteger modulus;
    private final BigInteger publicExponent;
    private final AlgorithmParameterSpec params;

    public RSAPublicKeySpec(BigInteger modulus, BigInteger publicExponent) {
        this(modulus, publicExponent, null);
    }

    public RSAPublicKeySpec(BigInteger modulus, BigInteger publicExponent, AlgorithmParameterSpec params) {
        this.modulus = modulus;
        this.publicExponent = publicExponent;
        this.params = params;
    }

    public BigInteger getModulus() {
        return this.modulus;
    }

    public BigInteger getPublicExponent() {
        return this.publicExponent;
    }

    public AlgorithmParameterSpec getParams() {
        return this.params;
    }
}

