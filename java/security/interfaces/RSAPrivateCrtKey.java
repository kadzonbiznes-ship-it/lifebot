/*
 * Decompiled with CFR 0.152.
 */
package java.security.interfaces;

import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;

public interface RSAPrivateCrtKey
extends RSAPrivateKey {
    @Deprecated
    public static final long serialVersionUID = -5682214253527700368L;

    public BigInteger getPublicExponent();

    public BigInteger getPrimeP();

    public BigInteger getPrimeQ();

    public BigInteger getPrimeExponentP();

    public BigInteger getPrimeExponentQ();

    public BigInteger getCrtCoefficient();
}

