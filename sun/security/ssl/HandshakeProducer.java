/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.SSLHandshake;

interface HandshakeProducer {
    public byte[] produce(ConnectionContext var1, SSLHandshake.HandshakeMessage var2) throws IOException;
}

