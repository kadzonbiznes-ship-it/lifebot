/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.access;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.spec.AlgorithmParameterSpec;

public interface JavaSecuritySignatureAccess {
    public void initVerify(Signature var1, PublicKey var2, AlgorithmParameterSpec var3) throws InvalidKeyException, InvalidAlgorithmParameterException;

    public void initVerify(Signature var1, Certificate var2, AlgorithmParameterSpec var3) throws InvalidKeyException, InvalidAlgorithmParameterException;

    public void initSign(Signature var1, PrivateKey var2, AlgorithmParameterSpec var3, SecureRandom var4) throws InvalidKeyException, InvalidAlgorithmParameterException;
}

