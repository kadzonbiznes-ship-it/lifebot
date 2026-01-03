/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import javax.crypto.SecretKey;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.SSLKeyDerivation;

interface SSLKeyDerivationGenerator {
    public SSLKeyDerivation createKeyDerivation(HandshakeContext var1, SecretKey var2) throws IOException;
}

