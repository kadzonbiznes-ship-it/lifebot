/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import sun.security.ssl.ConnectionContext;

interface SSLProducer {
    public byte[] produce(ConnectionContext var1) throws IOException;
}

