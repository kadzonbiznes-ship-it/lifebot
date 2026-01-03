/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import sun.security.ssl.HandshakeContext;
import sun.security.ssl.SSLPossession;

interface SSLPossessionGenerator {
    public SSLPossession createPossession(HandshakeContext var1);
}

