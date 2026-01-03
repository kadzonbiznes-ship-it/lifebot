/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.SSLHandshake;

interface HandshakeConsumer {
    public void consume(ConnectionContext var1, SSLHandshake.HandshakeMessage var2) throws IOException;
}

