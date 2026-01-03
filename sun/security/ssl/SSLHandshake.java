/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.Map;
import javax.net.ssl.SSLException;
import sun.security.ssl.CertificateMessage;
import sun.security.ssl.CertificateRequest;
import sun.security.ssl.CertificateStatus;
import sun.security.ssl.CertificateVerify;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ClientHello;
import sun.security.ssl.ClientKeyExchange;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.EncryptedExtensions;
import sun.security.ssl.Finished;
import sun.security.ssl.HandshakeAbsence;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeOutStream;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.HelloRequest;
import sun.security.ssl.HelloVerifyRequest;
import sun.security.ssl.KeyUpdate;
import sun.security.ssl.NewSessionTicket;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.ServerHello;
import sun.security.ssl.ServerHelloDone;
import sun.security.ssl.ServerKeyExchange;

enum SSLHandshake implements SSLConsumer,
HandshakeProducer
{
    HELLO_REQUEST(0, "hello_request", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(HelloRequest.handshakeConsumer, ProtocolVersion.PROTOCOLS_TO_12)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(HelloRequest.handshakeProducer, ProtocolVersion.PROTOCOLS_TO_12)}),
    CLIENT_HELLO(1, "client_hello", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(ClientHello.handshakeConsumer, ProtocolVersion.PROTOCOLS_TO_13)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(ClientHello.handshakeProducer, ProtocolVersion.PROTOCOLS_TO_13)}),
    SERVER_HELLO(2, "server_hello", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(ServerHello.handshakeConsumer, ProtocolVersion.PROTOCOLS_TO_13)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(ServerHello.t12HandshakeProducer, ProtocolVersion.PROTOCOLS_TO_12), new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(ServerHello.t13HandshakeProducer, ProtocolVersion.PROTOCOLS_OF_13)}),
    HELLO_RETRY_REQUEST(2, "hello_retry_request", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(ServerHello.handshakeConsumer, ProtocolVersion.PROTOCOLS_TO_13)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(ServerHello.hrrHandshakeProducer, ProtocolVersion.PROTOCOLS_OF_13)}),
    HELLO_VERIFY_REQUEST(3, "hello_verify_request", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(HelloVerifyRequest.handshakeConsumer, ProtocolVersion.PROTOCOLS_TO_12)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(HelloVerifyRequest.handshakeProducer, ProtocolVersion.PROTOCOLS_TO_12)}),
    NEW_SESSION_TICKET(4, "new_session_ticket", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(NewSessionTicket.handshake12Consumer, ProtocolVersion.PROTOCOLS_TO_12), new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(NewSessionTicket.handshakeConsumer, ProtocolVersion.PROTOCOLS_OF_13)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(NewSessionTicket.handshake12Producer, ProtocolVersion.PROTOCOLS_TO_12)}),
    END_OF_EARLY_DATA(5, "end_of_early_data"),
    ENCRYPTED_EXTENSIONS(8, "encrypted_extensions", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(EncryptedExtensions.handshakeConsumer, ProtocolVersion.PROTOCOLS_OF_13)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(EncryptedExtensions.handshakeProducer, ProtocolVersion.PROTOCOLS_OF_13)}),
    CERTIFICATE(11, "certificate", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(CertificateMessage.t12HandshakeConsumer, ProtocolVersion.PROTOCOLS_TO_12), new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(CertificateMessage.t13HandshakeConsumer, ProtocolVersion.PROTOCOLS_OF_13)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(CertificateMessage.t12HandshakeProducer, ProtocolVersion.PROTOCOLS_TO_12), new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(CertificateMessage.t13HandshakeProducer, ProtocolVersion.PROTOCOLS_OF_13)}),
    SERVER_KEY_EXCHANGE(12, "server_key_exchange", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(ServerKeyExchange.handshakeConsumer, ProtocolVersion.PROTOCOLS_TO_12)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(ServerKeyExchange.handshakeProducer, ProtocolVersion.PROTOCOLS_TO_12)}),
    CERTIFICATE_REQUEST(13, "certificate_request", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(CertificateRequest.t10HandshakeConsumer, ProtocolVersion.PROTOCOLS_TO_11), new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(CertificateRequest.t12HandshakeConsumer, ProtocolVersion.PROTOCOLS_OF_12), new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(CertificateRequest.t13HandshakeConsumer, ProtocolVersion.PROTOCOLS_OF_13)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(CertificateRequest.t10HandshakeProducer, ProtocolVersion.PROTOCOLS_TO_11), new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(CertificateRequest.t12HandshakeProducer, ProtocolVersion.PROTOCOLS_OF_12), new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(CertificateRequest.t13HandshakeProducer, ProtocolVersion.PROTOCOLS_OF_13)}),
    SERVER_HELLO_DONE(14, "server_hello_done", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(ServerHelloDone.handshakeConsumer, ProtocolVersion.PROTOCOLS_TO_12)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(ServerHelloDone.handshakeProducer, ProtocolVersion.PROTOCOLS_TO_12)}),
    CERTIFICATE_VERIFY(15, "certificate_verify", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(CertificateVerify.s30HandshakeConsumer, ProtocolVersion.PROTOCOLS_OF_30), new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(CertificateVerify.t10HandshakeConsumer, ProtocolVersion.PROTOCOLS_10_11), new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(CertificateVerify.t12HandshakeConsumer, ProtocolVersion.PROTOCOLS_OF_12), new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(CertificateVerify.t13HandshakeConsumer, ProtocolVersion.PROTOCOLS_OF_13)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(CertificateVerify.s30HandshakeProducer, ProtocolVersion.PROTOCOLS_OF_30), new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(CertificateVerify.t10HandshakeProducer, ProtocolVersion.PROTOCOLS_10_11), new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(CertificateVerify.t12HandshakeProducer, ProtocolVersion.PROTOCOLS_OF_12), new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(CertificateVerify.t13HandshakeProducer, ProtocolVersion.PROTOCOLS_OF_13)}),
    CLIENT_KEY_EXCHANGE(16, "client_key_exchange", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(ClientKeyExchange.handshakeConsumer, ProtocolVersion.PROTOCOLS_TO_12)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(ClientKeyExchange.handshakeProducer, ProtocolVersion.PROTOCOLS_TO_12)}),
    FINISHED(20, "finished", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(Finished.t12HandshakeConsumer, ProtocolVersion.PROTOCOLS_TO_12), new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(Finished.t13HandshakeConsumer, ProtocolVersion.PROTOCOLS_OF_13)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(Finished.t12HandshakeProducer, ProtocolVersion.PROTOCOLS_TO_12), new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(Finished.t13HandshakeProducer, ProtocolVersion.PROTOCOLS_OF_13)}),
    CERTIFICATE_URL(21, "certificate_url"),
    CERTIFICATE_STATUS(22, "certificate_status", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(CertificateStatus.handshakeConsumer, ProtocolVersion.PROTOCOLS_TO_12)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(CertificateStatus.handshakeProducer, ProtocolVersion.PROTOCOLS_TO_12)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeAbsence, ProtocolVersion[]>(CertificateStatus.handshakeAbsence, ProtocolVersion.PROTOCOLS_TO_12)}),
    SUPPLEMENTAL_DATA(23, "supplemental_data"),
    KEY_UPDATE(24, "key_update", new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<SSLConsumer, ProtocolVersion[]>(KeyUpdate.handshakeConsumer, ProtocolVersion.PROTOCOLS_OF_13)}, new Map.Entry[]{new AbstractMap.SimpleImmutableEntry<HandshakeProducer, ProtocolVersion[]>(KeyUpdate.handshakeProducer, ProtocolVersion.PROTOCOLS_OF_13)}),
    MESSAGE_HASH(-2, "message_hash"),
    NOT_APPLICABLE(-1, "not_applicable");

    final byte id;
    final String name;
    final Map.Entry<SSLConsumer, ProtocolVersion[]>[] handshakeConsumers;
    final Map.Entry<HandshakeProducer, ProtocolVersion[]>[] handshakeProducers;
    final Map.Entry<HandshakeAbsence, ProtocolVersion[]>[] handshakeAbsences;

    private SSLHandshake(byte id, String name) {
        this(id, name, new Map.Entry[0], new Map.Entry[0], new Map.Entry[0]);
    }

    private SSLHandshake(byte id, String name, Map.Entry<SSLConsumer, ProtocolVersion[]>[] handshakeConsumers, Map.Entry<HandshakeProducer, ProtocolVersion[]>[] handshakeProducers) {
        this(id, name, handshakeConsumers, handshakeProducers, new Map.Entry[0]);
    }

    private SSLHandshake(byte id, String name, Map.Entry<SSLConsumer, ProtocolVersion[]>[] handshakeConsumers, Map.Entry<HandshakeProducer, ProtocolVersion[]>[] handshakeProducers, Map.Entry<HandshakeAbsence, ProtocolVersion[]>[] handshakeAbsence) {
        this.id = id;
        this.name = name;
        this.handshakeConsumers = handshakeConsumers;
        this.handshakeProducers = handshakeProducers;
        this.handshakeAbsences = handshakeAbsence;
    }

    @Override
    public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
        SSLConsumer hc = this.getHandshakeConsumer(context);
        if (hc == null) {
            throw new UnsupportedOperationException("Unsupported handshake consumer: " + this.name);
        }
        hc.consume(context, message);
    }

    private SSLConsumer getHandshakeConsumer(ConnectionContext context) {
        if (this.handshakeConsumers.length == 0) {
            return null;
        }
        HandshakeContext hc = (HandshakeContext)context;
        ProtocolVersion protocolVersion = hc.negotiatedProtocol == null || hc.negotiatedProtocol == ProtocolVersion.NONE ? (hc.conContext.isNegotiated && hc.conContext.protocolVersion != ProtocolVersion.NONE ? hc.conContext.protocolVersion : hc.maximumActiveProtocol) : hc.negotiatedProtocol;
        for (Map.Entry<SSLConsumer, ProtocolVersion[]> phe : this.handshakeConsumers) {
            for (ProtocolVersion pv : phe.getValue()) {
                if (protocolVersion != pv) continue;
                return phe.getKey();
            }
        }
        return null;
    }

    @Override
    public byte[] produce(ConnectionContext context, HandshakeMessage message) throws IOException {
        HandshakeProducer hp = this.getHandshakeProducer(context);
        if (hp != null) {
            return hp.produce(context, message);
        }
        throw new UnsupportedOperationException("Unsupported handshake producer: " + this.name);
    }

    private HandshakeProducer getHandshakeProducer(ConnectionContext context) {
        if (this.handshakeProducers.length == 0) {
            return null;
        }
        HandshakeContext hc = (HandshakeContext)context;
        ProtocolVersion protocolVersion = hc.negotiatedProtocol == null || hc.negotiatedProtocol == ProtocolVersion.NONE ? (hc.conContext.isNegotiated && hc.conContext.protocolVersion != ProtocolVersion.NONE ? hc.conContext.protocolVersion : hc.maximumActiveProtocol) : hc.negotiatedProtocol;
        for (Map.Entry<HandshakeProducer, ProtocolVersion[]> phe : this.handshakeProducers) {
            for (ProtocolVersion pv : phe.getValue()) {
                if (protocolVersion != pv) continue;
                return phe.getKey();
            }
        }
        return null;
    }

    public String toString() {
        return this.name;
    }

    static String nameOf(byte id) {
        for (SSLHandshake hs : SSLHandshake.values()) {
            if (hs.id != id) continue;
            return hs.name;
        }
        return "UNKNOWN-HANDSHAKE-MESSAGE(" + id + ")";
    }

    static boolean isKnown(byte id) {
        for (SSLHandshake hs : SSLHandshake.values()) {
            if (hs.id != id || id == SSLHandshake.NOT_APPLICABLE.id) continue;
            return true;
        }
        return false;
    }

    static final void kickstart(HandshakeContext context) throws IOException {
        if (context instanceof ClientHandshakeContext) {
            if (context.conContext.isNegotiated && context.conContext.protocolVersion.useTLS13PlusSpec()) {
                KeyUpdate.kickstartProducer.produce(context);
            } else {
                ClientHello.kickstartProducer.produce(context);
            }
        } else if (context.conContext.protocolVersion.useTLS13PlusSpec()) {
            KeyUpdate.kickstartProducer.produce(context);
        } else {
            HelloRequest.kickstartProducer.produce(context);
        }
    }

    static abstract class HandshakeMessage {
        final HandshakeContext handshakeContext;

        HandshakeMessage(HandshakeContext handshakeContext) {
            this.handshakeContext = handshakeContext;
        }

        abstract SSLHandshake handshakeType();

        abstract int messageLength();

        abstract void send(HandshakeOutStream var1) throws IOException;

        void write(HandshakeOutStream hos) throws IOException {
            int len = this.messageLength();
            if (len >= 0x1000000) {
                throw new SSLException("Handshake message is overflow, type = " + this.handshakeType() + ", len = " + len);
            }
            hos.write(this.handshakeType().id);
            hos.putInt24(len);
            this.send(hos);
            hos.complete();
        }
    }
}

