/*
 * Decompiled with CFR 0.152.
 */
package java.security.spec;

import java.security.InvalidParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;
import java.util.Optional;

public class EdDSAParameterSpec
implements AlgorithmParameterSpec {
    private final boolean prehash;
    private final byte[] context;

    public EdDSAParameterSpec(boolean prehash) {
        this.prehash = prehash;
        this.context = null;
    }

    public EdDSAParameterSpec(boolean prehash, byte[] context) {
        Objects.requireNonNull(context, "context may not be null");
        if (context.length > 255) {
            throw new InvalidParameterException("context length cannot be greater than 255");
        }
        this.prehash = prehash;
        this.context = (byte[])context.clone();
    }

    public boolean isPrehash() {
        return this.prehash;
    }

    public Optional<byte[]> getContext() {
        if (this.context == null) {
            return Optional.empty();
        }
        return Optional.of((byte[])this.context.clone());
    }
}

