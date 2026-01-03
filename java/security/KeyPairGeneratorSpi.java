/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

public abstract class KeyPairGeneratorSpi {
    public abstract void initialize(int var1, SecureRandom var2);

    public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        throw new UnsupportedOperationException();
    }

    public abstract KeyPair generateKeyPair();
}

