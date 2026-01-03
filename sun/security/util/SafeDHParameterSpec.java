/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.math.BigInteger;
import javax.crypto.spec.DHParameterSpec;

public final class SafeDHParameterSpec
extends DHParameterSpec {
    public SafeDHParameterSpec(BigInteger p, BigInteger g) {
        super(p, g);
    }

    public SafeDHParameterSpec(BigInteger p, BigInteger g, int l) {
        super(p, g, l);
    }
}

