/*
 * Decompiled with CFR 0.152.
 */
package java.security.spec;

import java.security.spec.KeySpec;
import java.util.Arrays;
import jdk.internal.access.SharedSecrets;

public abstract class EncodedKeySpec
implements KeySpec {
    private final byte[] encodedKey;
    private String algorithmName;

    public EncodedKeySpec(byte[] encodedKey) {
        this.encodedKey = (byte[])encodedKey.clone();
    }

    protected EncodedKeySpec(byte[] encodedKey, String algorithm) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm name may not be null");
        }
        if (algorithm.isEmpty()) {
            throw new IllegalArgumentException("algorithm name may not be empty");
        }
        this.encodedKey = (byte[])encodedKey.clone();
        this.algorithmName = algorithm;
    }

    public String getAlgorithm() {
        return this.algorithmName;
    }

    public byte[] getEncoded() {
        return (byte[])this.encodedKey.clone();
    }

    public abstract String getFormat();

    void clear() {
        Arrays.fill(this.encodedKey, (byte)0);
    }

    static {
        SharedSecrets.setJavaSecuritySpecAccess(EncodedKeySpec::clear);
    }
}

