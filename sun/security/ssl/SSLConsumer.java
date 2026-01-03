/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import sun.security.ssl.ConnectionContext;

interface SSLConsumer {
    public void consume(ConnectionContext var1, ByteBuffer var2) throws IOException;
}

