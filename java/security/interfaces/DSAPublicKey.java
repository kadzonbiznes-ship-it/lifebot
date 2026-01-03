/*
 * Decompiled with CFR 0.152.
 */
package java.security.interfaces;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.DSAKey;

public interface DSAPublicKey
extends DSAKey,
PublicKey {
    @Deprecated
    public static final long serialVersionUID = 1234526332779022332L;

    public BigInteger getY();
}

