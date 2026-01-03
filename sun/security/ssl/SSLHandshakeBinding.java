/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.util.Map;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLHandshake;

interface SSLHandshakeBinding {
    default public SSLHandshake[] getRelatedHandshakers(HandshakeContext handshakeContext) {
        return new SSLHandshake[0];
    }

    default public Map.Entry<Byte, HandshakeProducer>[] getHandshakeProducers(HandshakeContext handshakeContext) {
        return new Map.Entry[0];
    }

    default public Map.Entry<Byte, SSLConsumer>[] getHandshakeConsumers(HandshakeContext handshakeContext) {
        return new Map.Entry[0];
    }
}

