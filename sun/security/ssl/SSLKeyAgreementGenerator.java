/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.SSLKeyDerivation;

interface SSLKeyAgreementGenerator {
    public SSLKeyDerivation createKeyDerivation(HandshakeContext var1) throws IOException;
}

