/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import sun.security.ssl.ClientAuthType;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeConsumer;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.Record;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLStringizer;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.SignatureAlgorithmsExtension;
import sun.security.ssl.SignatureScheme;

final class CertSignAlgsExtension {
    static final HandshakeProducer chNetworkProducer = new CHCertSignatureSchemesProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new CHCertSignatureSchemesConsumer();
    static final HandshakeConsumer chOnTradeConsumer = new CHCertSignatureSchemesUpdate();
    static final HandshakeProducer crNetworkProducer = new CRCertSignatureSchemesProducer();
    static final SSLExtension.ExtensionConsumer crOnLoadConsumer = new CRCertSignatureSchemesConsumer();
    static final HandshakeConsumer crOnTradeConsumer = new CRCertSignatureSchemesUpdate();
    static final SSLStringizer ssStringizer = new CertSignatureSchemesStringizer();

    CertSignAlgsExtension() {
    }

    private static final class CHCertSignatureSchemesProducer
    implements HandshakeProducer {
        private CHCertSignatureSchemesProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.CH_SIGNATURE_ALGORITHMS_CERT)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable signature_algorithms_cert extension", new Object[0]);
                }
                return null;
            }
            SignatureScheme.updateHandshakeLocalSupportedAlgs(chc);
            int vectorLen = SignatureScheme.sizeInRecord() * chc.localSupportedCertSignAlgs.size();
            byte[] extData = new byte[vectorLen + 2];
            ByteBuffer m = ByteBuffer.wrap(extData);
            Record.putInt16(m, vectorLen);
            for (SignatureScheme ss : chc.localSupportedCertSignAlgs) {
                Record.putInt16(m, ss.id);
            }
            chc.handshakeExtensions.put(SSLExtension.CH_SIGNATURE_ALGORITHMS_CERT, new SignatureAlgorithmsExtension.SignatureSchemesSpec(chc.localSupportedCertSignAlgs));
            return extData;
        }
    }

    private static final class CHCertSignatureSchemesConsumer
    implements SSLExtension.ExtensionConsumer {
        private CHCertSignatureSchemesConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_SIGNATURE_ALGORITHMS_CERT)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable signature_algorithms_cert extension", new Object[0]);
                }
                return;
            }
            SignatureAlgorithmsExtension.SignatureSchemesSpec spec = new SignatureAlgorithmsExtension.SignatureSchemesSpec(shc, buffer);
            shc.handshakeExtensions.put(SSLExtension.CH_SIGNATURE_ALGORITHMS_CERT, spec);
        }
    }

    private static final class CHCertSignatureSchemesUpdate
    implements HandshakeConsumer {
        private CHCertSignatureSchemesUpdate() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            List<SignatureScheme> schemes;
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            SignatureAlgorithmsExtension.SignatureSchemesSpec spec = (SignatureAlgorithmsExtension.SignatureSchemesSpec)shc.handshakeExtensions.get(SSLExtension.CH_SIGNATURE_ALGORITHMS_CERT);
            if (spec == null) {
                return;
            }
            shc.peerRequestedCertSignSchemes = schemes = SignatureScheme.getSupportedAlgorithms(shc.sslConfig, shc.algorithmConstraints, shc.negotiatedProtocol, spec.signatureSchemes, SignatureScheme.CERTIFICATE_SCOPE);
            shc.handshakeSession.setPeerSupportedSignatureAlgorithms(schemes);
            if (!shc.isResumption && shc.negotiatedProtocol.useTLS13PlusSpec()) {
                if (shc.sslConfig.clientAuthType != ClientAuthType.CLIENT_AUTH_NONE) {
                    shc.handshakeProducers.putIfAbsent(SSLHandshake.CERTIFICATE_REQUEST.id, SSLHandshake.CERTIFICATE_REQUEST);
                }
                shc.handshakeProducers.put(SSLHandshake.CERTIFICATE.id, SSLHandshake.CERTIFICATE);
                shc.handshakeProducers.putIfAbsent(SSLHandshake.CERTIFICATE_VERIFY.id, SSLHandshake.CERTIFICATE_VERIFY);
            }
        }
    }

    private static final class CRCertSignatureSchemesProducer
    implements HandshakeProducer {
        private CRCertSignatureSchemesProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_SIGNATURE_ALGORITHMS_CERT)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable signature_algorithms_cert extension", new Object[0]);
                }
                return null;
            }
            int vectorLen = SignatureScheme.sizeInRecord() * shc.localSupportedCertSignAlgs.size();
            byte[] extData = new byte[vectorLen + 2];
            ByteBuffer m = ByteBuffer.wrap(extData);
            Record.putInt16(m, vectorLen);
            for (SignatureScheme ss : shc.localSupportedCertSignAlgs) {
                Record.putInt16(m, ss.id);
            }
            shc.handshakeExtensions.put(SSLExtension.CR_SIGNATURE_ALGORITHMS_CERT, new SignatureAlgorithmsExtension.SignatureSchemesSpec(shc.localSupportedCertSignAlgs));
            return extData;
        }
    }

    private static final class CRCertSignatureSchemesConsumer
    implements SSLExtension.ExtensionConsumer {
        private CRCertSignatureSchemesConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.CH_SIGNATURE_ALGORITHMS_CERT)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable signature_algorithms_cert extension", new Object[0]);
                }
                return;
            }
            SignatureAlgorithmsExtension.SignatureSchemesSpec spec = new SignatureAlgorithmsExtension.SignatureSchemesSpec(chc, buffer);
            chc.handshakeExtensions.put(SSLExtension.CR_SIGNATURE_ALGORITHMS_CERT, spec);
        }
    }

    private static final class CRCertSignatureSchemesUpdate
    implements HandshakeConsumer {
        private CRCertSignatureSchemesUpdate() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            List<SignatureScheme> schemes;
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            SignatureAlgorithmsExtension.SignatureSchemesSpec spec = (SignatureAlgorithmsExtension.SignatureSchemesSpec)chc.handshakeExtensions.get(SSLExtension.CR_SIGNATURE_ALGORITHMS_CERT);
            if (spec == null) {
                return;
            }
            chc.peerRequestedCertSignSchemes = schemes = SignatureScheme.getSupportedAlgorithms(chc.sslConfig, chc.algorithmConstraints, chc.negotiatedProtocol, spec.signatureSchemes, SignatureScheme.CERTIFICATE_SCOPE);
            chc.handshakeSession.setPeerSupportedSignatureAlgorithms(schemes);
        }
    }

    private static final class CertSignatureSchemesStringizer
    implements SSLStringizer {
        private CertSignatureSchemesStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new SignatureAlgorithmsExtension.SignatureSchemesSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }
}

