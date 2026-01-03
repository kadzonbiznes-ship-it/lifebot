/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.nio.ByteBuffer;
import sun.security.ssl.HandshakeContext;

interface SSLStringizer {
    public String toString(HandshakeContext var1, ByteBuffer var2);
}

