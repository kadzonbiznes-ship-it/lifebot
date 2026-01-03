/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.SecretKey;

interface SSLKeyDerivation {
    public SecretKey deriveKey(String var1, AlgorithmParameterSpec var2) throws IOException;
}

