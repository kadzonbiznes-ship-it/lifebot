/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.io.Serializable;
import java.security.SecureRandomParameters;

public abstract class SecureRandomSpi
implements Serializable {
    private static final long serialVersionUID = -2991854161009191830L;

    public SecureRandomSpi() {
    }

    protected SecureRandomSpi(SecureRandomParameters params) {
    }

    protected abstract void engineSetSeed(byte[] var1);

    protected abstract void engineNextBytes(byte[] var1);

    protected void engineNextBytes(byte[] bytes, SecureRandomParameters params) {
        throw new UnsupportedOperationException();
    }

    protected abstract byte[] engineGenerateSeed(int var1);

    protected void engineReseed(SecureRandomParameters params) {
        throw new UnsupportedOperationException();
    }

    protected SecureRandomParameters engineGetParameters() {
        return null;
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }
}

