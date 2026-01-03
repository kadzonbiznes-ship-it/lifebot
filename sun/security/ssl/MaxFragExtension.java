/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLProtocolException;
import sun.security.ssl.Alert;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeConsumer;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLStringizer;
import sun.security.ssl.ServerHandshakeContext;

final class MaxFragExtension {
    static final HandshakeProducer chNetworkProducer = new CHMaxFragmentLengthProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new CHMaxFragmentLengthConsumer();
    static final HandshakeProducer shNetworkProducer = new SHMaxFragmentLengthProducer();
    static final SSLExtension.ExtensionConsumer shOnLoadConsumer = new SHMaxFragmentLengthConsumer();
    static final HandshakeConsumer shOnTradeConsumer = new SHMaxFragmentLengthUpdate();
    static final HandshakeProducer eeNetworkProducer = new EEMaxFragmentLengthProducer();
    static final SSLExtension.ExtensionConsumer eeOnLoadConsumer = new EEMaxFragmentLengthConsumer();
    static final HandshakeConsumer eeOnTradeConsumer = new EEMaxFragmentLengthUpdate();
    static final SSLStringizer maxFragLenStringizer = new MaxFragLenStringizer();

    MaxFragExtension() {
    }

    private static final class CHMaxFragmentLengthProducer
    implements HandshakeProducer {
        private CHMaxFragmentLengthProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            int requestedMFLength;
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.CH_MAX_FRAGMENT_LENGTH)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable max_fragment_length extension", new Object[0]);
                }
                return null;
            }
            if (chc.isResumption && chc.resumingSession != null) {
                requestedMFLength = chc.resumingSession.getNegotiatedMaxFragSize();
            } else if (chc.sslConfig.maximumPacketSize != 0) {
                requestedMFLength = chc.sslConfig.maximumPacketSize;
                requestedMFLength = chc.sslContext.isDTLS() ? (requestedMFLength -= 333) : (requestedMFLength -= 325);
            } else {
                requestedMFLength = -1;
            }
            MaxFragLenEnum mfl = MaxFragLenEnum.valueOf(requestedMFLength);
            if (mfl != null) {
                chc.handshakeExtensions.put(SSLExtension.CH_MAX_FRAGMENT_LENGTH, new MaxFragLenSpec(mfl.id));
                return new byte[]{mfl.id};
            }
            chc.maxFragmentLength = -1;
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("No available max_fragment_length extension can be used for fragment size of " + requestedMFLength + "bytes", new Object[0]);
            }
            return null;
        }
    }

    private static final class CHMaxFragmentLengthConsumer
    implements SSLExtension.ExtensionConsumer {
        private CHMaxFragmentLengthConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_MAX_FRAGMENT_LENGTH)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable max_fragment_length extension", new Object[0]);
                }
                return;
            }
            MaxFragLenSpec spec = new MaxFragLenSpec(shc, buffer);
            MaxFragLenEnum mfle = MaxFragLenEnum.valueOf(spec.id);
            if (mfle == null) {
                throw shc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "the requested maximum fragment length is other than the allowed values");
            }
            shc.maxFragmentLength = mfle.fragmentSize;
            shc.handshakeExtensions.put(SSLExtension.CH_MAX_FRAGMENT_LENGTH, spec);
        }
    }

    private static final class SHMaxFragmentLengthProducer
    implements HandshakeProducer {
        private SHMaxFragmentLengthProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            int estimatedMaxFragSize;
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            MaxFragLenSpec spec = (MaxFragLenSpec)shc.handshakeExtensions.get(SSLExtension.CH_MAX_FRAGMENT_LENGTH);
            if (spec == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("Ignore unavailable max_fragment_length extension", new Object[0]);
                }
                return null;
            }
            if (shc.maxFragmentLength > 0 && shc.sslConfig.maximumPacketSize != 0 && (estimatedMaxFragSize = shc.negotiatedCipherSuite.calculatePacketSize(shc.maxFragmentLength, shc.negotiatedProtocol, shc.sslContext.isDTLS())) > shc.sslConfig.maximumPacketSize) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Abort the maximum fragment length negotiation, may overflow the maximum packet size limit.", new Object[0]);
                }
                shc.maxFragmentLength = -1;
            }
            if (shc.maxFragmentLength > 0) {
                shc.handshakeSession.setNegotiatedMaxFragSize(shc.maxFragmentLength);
                shc.conContext.inputRecord.changeFragmentSize(shc.maxFragmentLength);
                shc.conContext.outputRecord.changeFragmentSize(shc.maxFragmentLength);
                shc.handshakeExtensions.put(SSLExtension.SH_MAX_FRAGMENT_LENGTH, spec);
                return new byte[]{spec.id};
            }
            return null;
        }
    }

    private static final class SHMaxFragmentLengthConsumer
    implements SSLExtension.ExtensionConsumer {
        private SHMaxFragmentLengthConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            MaxFragLenSpec requestedSpec = (MaxFragLenSpec)chc.handshakeExtensions.get(SSLExtension.CH_MAX_FRAGMENT_LENGTH);
            if (requestedSpec == null) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected max_fragment_length extension in ServerHello");
            }
            MaxFragLenSpec spec = new MaxFragLenSpec(chc, buffer);
            if (spec.id != requestedSpec.id) {
                throw chc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "The maximum fragment length response is not requested");
            }
            MaxFragLenEnum mfle = MaxFragLenEnum.valueOf(spec.id);
            if (mfle == null) {
                throw chc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "the requested maximum fragment length is other than the allowed values");
            }
            chc.maxFragmentLength = mfle.fragmentSize;
            chc.handshakeExtensions.put(SSLExtension.SH_MAX_FRAGMENT_LENGTH, spec);
        }
    }

    private static final class SHMaxFragmentLengthUpdate
    implements HandshakeConsumer {
        private SHMaxFragmentLengthUpdate() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            int estimatedMaxFragSize;
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            MaxFragLenSpec spec = (MaxFragLenSpec)chc.handshakeExtensions.get(SSLExtension.SH_MAX_FRAGMENT_LENGTH);
            if (spec == null) {
                return;
            }
            if (chc.maxFragmentLength > 0 && chc.sslConfig.maximumPacketSize != 0 && (estimatedMaxFragSize = chc.negotiatedCipherSuite.calculatePacketSize(chc.maxFragmentLength, chc.negotiatedProtocol, chc.sslContext.isDTLS())) > chc.sslConfig.maximumPacketSize) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Abort the maximum fragment length negotiation, may overflow the maximum packet size limit.", new Object[0]);
                }
                chc.maxFragmentLength = -1;
            }
            if (chc.maxFragmentLength > 0) {
                chc.handshakeSession.setNegotiatedMaxFragSize(chc.maxFragmentLength);
                chc.conContext.inputRecord.changeFragmentSize(chc.maxFragmentLength);
                chc.conContext.outputRecord.changeFragmentSize(chc.maxFragmentLength);
            }
        }
    }

    private static final class EEMaxFragmentLengthProducer
    implements HandshakeProducer {
        private EEMaxFragmentLengthProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            int estimatedMaxFragSize;
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            MaxFragLenSpec spec = (MaxFragLenSpec)shc.handshakeExtensions.get(SSLExtension.CH_MAX_FRAGMENT_LENGTH);
            if (spec == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("Ignore unavailable max_fragment_length extension", new Object[0]);
                }
                return null;
            }
            if (shc.maxFragmentLength > 0 && shc.sslConfig.maximumPacketSize != 0 && (estimatedMaxFragSize = shc.negotiatedCipherSuite.calculatePacketSize(shc.maxFragmentLength, shc.negotiatedProtocol, shc.sslContext.isDTLS())) > shc.sslConfig.maximumPacketSize) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Abort the maximum fragment length negotiation, may overflow the maximum packet size limit.", new Object[0]);
                }
                shc.maxFragmentLength = -1;
            }
            if (shc.maxFragmentLength > 0) {
                shc.handshakeSession.setNegotiatedMaxFragSize(shc.maxFragmentLength);
                shc.conContext.inputRecord.changeFragmentSize(shc.maxFragmentLength);
                shc.conContext.outputRecord.changeFragmentSize(shc.maxFragmentLength);
                shc.handshakeExtensions.put(SSLExtension.EE_MAX_FRAGMENT_LENGTH, spec);
                return new byte[]{spec.id};
            }
            return null;
        }
    }

    private static final class EEMaxFragmentLengthConsumer
    implements SSLExtension.ExtensionConsumer {
        private EEMaxFragmentLengthConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            MaxFragLenSpec requestedSpec = (MaxFragLenSpec)chc.handshakeExtensions.get(SSLExtension.CH_MAX_FRAGMENT_LENGTH);
            if (requestedSpec == null) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected max_fragment_length extension in ServerHello");
            }
            MaxFragLenSpec spec = new MaxFragLenSpec(chc, buffer);
            if (spec.id != requestedSpec.id) {
                throw chc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "The maximum fragment length response is not requested");
            }
            MaxFragLenEnum mfle = MaxFragLenEnum.valueOf(spec.id);
            if (mfle == null) {
                throw chc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "the requested maximum fragment length is other than the allowed values");
            }
            chc.maxFragmentLength = mfle.fragmentSize;
            chc.handshakeExtensions.put(SSLExtension.EE_MAX_FRAGMENT_LENGTH, spec);
        }
    }

    private static final class EEMaxFragmentLengthUpdate
    implements HandshakeConsumer {
        private EEMaxFragmentLengthUpdate() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            int estimatedMaxFragSize;
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            MaxFragLenSpec spec = (MaxFragLenSpec)chc.handshakeExtensions.get(SSLExtension.EE_MAX_FRAGMENT_LENGTH);
            if (spec == null) {
                return;
            }
            if (chc.maxFragmentLength > 0 && chc.sslConfig.maximumPacketSize != 0 && (estimatedMaxFragSize = chc.negotiatedCipherSuite.calculatePacketSize(chc.maxFragmentLength, chc.negotiatedProtocol, chc.sslContext.isDTLS())) > chc.sslConfig.maximumPacketSize) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Abort the maximum fragment length negotiation, may overflow the maximum packet size limit.", new Object[0]);
                }
                chc.maxFragmentLength = -1;
            }
            if (chc.maxFragmentLength > 0) {
                chc.handshakeSession.setNegotiatedMaxFragSize(chc.maxFragmentLength);
                chc.conContext.inputRecord.changeFragmentSize(chc.maxFragmentLength);
                chc.conContext.outputRecord.changeFragmentSize(chc.maxFragmentLength);
            }
        }
    }

    private static final class MaxFragLenStringizer
    implements SSLStringizer {
        private MaxFragLenStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new MaxFragLenSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    static enum MaxFragLenEnum {
        MFL_512(1, 512, "2^9"),
        MFL_1024(2, 1024, "2^10"),
        MFL_2048(3, 2048, "2^11"),
        MFL_4096(4, 4096, "2^12");

        final byte id;
        final int fragmentSize;
        final String description;

        private MaxFragLenEnum(byte id, int fragmentSize, String description) {
            this.id = id;
            this.fragmentSize = fragmentSize;
            this.description = description;
        }

        private static MaxFragLenEnum valueOf(byte id) {
            for (MaxFragLenEnum mfl : MaxFragLenEnum.values()) {
                if (mfl.id != id) continue;
                return mfl;
            }
            return null;
        }

        private static String nameOf(byte id) {
            for (MaxFragLenEnum mfl : MaxFragLenEnum.values()) {
                if (mfl.id != id) continue;
                return mfl.description;
            }
            return "UNDEFINED-MAX-FRAGMENT-LENGTH(" + id + ")";
        }

        static MaxFragLenEnum valueOf(int fragmentSize) {
            if (fragmentSize <= 0) {
                return null;
            }
            if (fragmentSize < 1024) {
                return MFL_512;
            }
            if (fragmentSize < 2048) {
                return MFL_1024;
            }
            if (fragmentSize < 4096) {
                return MFL_2048;
            }
            if (fragmentSize == 4096) {
                return MFL_4096;
            }
            return null;
        }
    }

    static final class MaxFragLenSpec
    implements SSLExtension.SSLExtensionSpec {
        byte id;

        private MaxFragLenSpec(byte id) {
            this.id = id;
        }

        private MaxFragLenSpec(HandshakeContext hc, ByteBuffer buffer) throws IOException {
            if (buffer.remaining() != 1) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid max_fragment_length extension data"));
            }
            this.id = buffer.get();
        }

        public String toString() {
            return MaxFragLenEnum.nameOf(this.id);
        }
    }
}

