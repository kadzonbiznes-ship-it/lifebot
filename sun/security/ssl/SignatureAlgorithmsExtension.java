/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLProtocolException;
import sun.security.ssl.Alert;
import sun.security.ssl.ClientAuthType;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeAbsence;
import sun.security.ssl.HandshakeConsumer;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.Record;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLStringizer;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.SignatureScheme;

final class SignatureAlgorithmsExtension {
    static final HandshakeProducer chNetworkProducer = new CHSignatureSchemesProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new CHSignatureSchemesConsumer();
    static final HandshakeAbsence chOnLoadAbsence = new CHSignatureSchemesOnLoadAbsence();
    static final HandshakeConsumer chOnTradeConsumer = new CHSignatureSchemesUpdate();
    static final HandshakeAbsence chOnTradeAbsence = new CHSignatureSchemesOnTradeAbsence();
    static final HandshakeProducer crNetworkProducer = new CRSignatureSchemesProducer();
    static final SSLExtension.ExtensionConsumer crOnLoadConsumer = new CRSignatureSchemesConsumer();
    static final HandshakeAbsence crOnLoadAbsence = new CRSignatureSchemesAbsence();
    static final HandshakeConsumer crOnTradeConsumer = new CRSignatureSchemesUpdate();
    static final SSLStringizer ssStringizer = new SignatureSchemesStringizer();

    SignatureAlgorithmsExtension() {
    }

    private static void updateHandshakeContext(HandshakeContext hc, int[] signatureSchemes, SSLExtension signatureAlgorithmsCertExt) throws SSLException {
        List<SignatureScheme> handshakeSS = SignatureScheme.getSupportedAlgorithms(hc.sslConfig, hc.algorithmConstraints, hc.negotiatedProtocol, signatureSchemes, SignatureScheme.HANDSHAKE_SCOPE);
        if (handshakeSS.isEmpty()) {
            throw hc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No supported signature algorithm");
        }
        hc.peerRequestedSignatureSchemes = handshakeSS;
        SignatureSchemesSpec certSpec = (SignatureSchemesSpec)hc.handshakeExtensions.get(signatureAlgorithmsCertExt);
        if (certSpec == null) {
            List<SignatureScheme> certSS = SignatureScheme.getSupportedAlgorithms(hc.sslConfig, hc.algorithmConstraints, hc.negotiatedProtocol, signatureSchemes, SignatureScheme.CERTIFICATE_SCOPE);
            if (certSS.isEmpty()) {
                throw hc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No supported signature algorithm");
            }
            hc.peerRequestedCertSignSchemes = certSS;
            hc.handshakeSession.setPeerSupportedSignatureAlgorithms(certSS);
        }
    }

    static final class SignatureSchemesSpec
    implements SSLExtension.SSLExtensionSpec {
        final int[] signatureSchemes;

        SignatureSchemesSpec(List<SignatureScheme> schemes) {
            if (schemes != null) {
                this.signatureSchemes = new int[schemes.size()];
                int i = 0;
                for (SignatureScheme scheme : schemes) {
                    this.signatureSchemes[i++] = scheme.id;
                }
            } else {
                this.signatureSchemes = new int[0];
            }
        }

        SignatureSchemesSpec(HandshakeContext hc, ByteBuffer buffer) throws IOException {
            if (buffer.remaining() < 2) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid signature_algorithms: insufficient data"));
            }
            byte[] algs = Record.getBytes16(buffer);
            if (buffer.hasRemaining()) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid signature_algorithms: unknown extra data"));
            }
            if (algs.length == 0 || (algs.length & 1) != 0) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid signature_algorithms: incomplete data"));
            }
            int[] schemes = new int[algs.length / 2];
            int i = 0;
            int j = 0;
            while (i < algs.length) {
                byte hash = algs[i++];
                byte sign = algs[i++];
                schemes[j++] = (hash & 0xFF) << 8 | sign & 0xFF;
            }
            this.signatureSchemes = schemes;
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"signature schemes\": '['{0}']'", Locale.ENGLISH);
            if (this.signatureSchemes == null || this.signatureSchemes.length == 0) {
                Object[] messageFields = new Object[]{"<no supported signature schemes specified>"};
                return messageFormat.format(messageFields);
            }
            StringBuilder builder = new StringBuilder(512);
            boolean isFirst = true;
            for (int pv : this.signatureSchemes) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(", ");
                }
                builder.append(SignatureScheme.nameOf(pv));
            }
            Object[] messageFields = new Object[]{builder.toString()};
            return messageFormat.format(messageFields);
        }
    }

    private static final class CHSignatureSchemesProducer
    implements HandshakeProducer {
        private CHSignatureSchemesProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.CH_SIGNATURE_ALGORITHMS)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable signature_algorithms extension", new Object[0]);
                }
                return null;
            }
            SignatureScheme.updateHandshakeLocalSupportedAlgs(chc);
            int vectorLen = SignatureScheme.sizeInRecord() * chc.localSupportedSignAlgs.size();
            byte[] extData = new byte[vectorLen + 2];
            ByteBuffer m = ByteBuffer.wrap(extData);
            Record.putInt16(m, vectorLen);
            for (SignatureScheme ss : chc.localSupportedSignAlgs) {
                Record.putInt16(m, ss.id);
            }
            chc.handshakeExtensions.put(SSLExtension.CH_SIGNATURE_ALGORITHMS, new SignatureSchemesSpec(chc.localSupportedSignAlgs));
            return extData;
        }
    }

    private static final class CHSignatureSchemesConsumer
    implements SSLExtension.ExtensionConsumer {
        private CHSignatureSchemesConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_SIGNATURE_ALGORITHMS)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable signature_algorithms extension", new Object[0]);
                }
                return;
            }
            SignatureSchemesSpec spec = new SignatureSchemesSpec(shc, buffer);
            shc.handshakeExtensions.put(SSLExtension.CH_SIGNATURE_ALGORITHMS, spec);
        }
    }

    private static final class CHSignatureSchemesOnLoadAbsence
    implements HandshakeAbsence {
        private CHSignatureSchemesOnLoadAbsence() {
        }

        @Override
        public void absent(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (shc.negotiatedProtocol.useTLS13PlusSpec()) {
                throw shc.conContext.fatal(Alert.MISSING_EXTENSION, "No mandatory signature_algorithms extension in the received ClientHello handshake message");
            }
        }
    }

    private static final class CHSignatureSchemesUpdate
    implements HandshakeConsumer {
        private CHSignatureSchemesUpdate() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            SignatureSchemesSpec spec = (SignatureSchemesSpec)shc.handshakeExtensions.get(SSLExtension.CH_SIGNATURE_ALGORITHMS);
            if (spec == null) {
                return;
            }
            SignatureAlgorithmsExtension.updateHandshakeContext(shc, spec.signatureSchemes, SSLExtension.CH_SIGNATURE_ALGORITHMS_CERT);
            if (!shc.isResumption && shc.negotiatedProtocol.useTLS13PlusSpec()) {
                if (shc.sslConfig.clientAuthType != ClientAuthType.CLIENT_AUTH_NONE) {
                    shc.handshakeProducers.putIfAbsent(SSLHandshake.CERTIFICATE_REQUEST.id, SSLHandshake.CERTIFICATE_REQUEST);
                }
                shc.handshakeProducers.put(SSLHandshake.CERTIFICATE.id, SSLHandshake.CERTIFICATE);
                shc.handshakeProducers.putIfAbsent(SSLHandshake.CERTIFICATE_VERIFY.id, SSLHandshake.CERTIFICATE_VERIFY);
            }
        }
    }

    private static final class CHSignatureSchemesOnTradeAbsence
    implements HandshakeAbsence {
        private CHSignatureSchemesOnTradeAbsence() {
        }

        @Override
        public void absent(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (shc.negotiatedProtocol.useTLS12PlusSpec()) {
                List<SignatureScheme> schemes;
                shc.peerRequestedSignatureSchemes = schemes = Arrays.asList(SignatureScheme.RSA_PKCS1_SHA1, SignatureScheme.DSA_SHA1, SignatureScheme.ECDSA_SHA1);
                if (shc.peerRequestedCertSignSchemes == null || shc.peerRequestedCertSignSchemes.isEmpty()) {
                    shc.peerRequestedCertSignSchemes = schemes;
                }
                shc.handshakeSession.setUseDefaultPeerSignAlgs();
            }
        }
    }

    private static final class CRSignatureSchemesProducer
    implements HandshakeProducer {
        private CRSignatureSchemesProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CR_SIGNATURE_ALGORITHMS)) {
                throw shc.conContext.fatal(Alert.MISSING_EXTENSION, "No available signature_algorithms extension for client certificate authentication");
            }
            int vectorLen = SignatureScheme.sizeInRecord() * shc.localSupportedSignAlgs.size();
            byte[] extData = new byte[vectorLen + 2];
            ByteBuffer m = ByteBuffer.wrap(extData);
            Record.putInt16(m, vectorLen);
            for (SignatureScheme ss : shc.localSupportedSignAlgs) {
                Record.putInt16(m, ss.id);
            }
            shc.handshakeExtensions.put(SSLExtension.CR_SIGNATURE_ALGORITHMS, new SignatureSchemesSpec(shc.localSupportedSignAlgs));
            return extData;
        }
    }

    private static final class CRSignatureSchemesConsumer
    implements SSLExtension.ExtensionConsumer {
        private CRSignatureSchemesConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.CR_SIGNATURE_ALGORITHMS)) {
                throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No available signature_algorithms extension for client certificate authentication");
            }
            SignatureSchemesSpec spec = new SignatureSchemesSpec(chc, buffer);
            chc.handshakeExtensions.put(SSLExtension.CR_SIGNATURE_ALGORITHMS, spec);
        }
    }

    private static final class CRSignatureSchemesAbsence
    implements HandshakeAbsence {
        private CRSignatureSchemesAbsence() {
        }

        @Override
        public void absent(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            throw chc.conContext.fatal(Alert.MISSING_EXTENSION, "No mandatory signature_algorithms extension in the received CertificateRequest handshake message");
        }
    }

    private static final class CRSignatureSchemesUpdate
    implements HandshakeConsumer {
        private CRSignatureSchemesUpdate() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            SignatureSchemesSpec spec = (SignatureSchemesSpec)chc.handshakeExtensions.get(SSLExtension.CR_SIGNATURE_ALGORITHMS);
            if (spec == null) {
                return;
            }
            SignatureAlgorithmsExtension.updateHandshakeContext(chc, spec.signatureSchemes, SSLExtension.CR_SIGNATURE_ALGORITHMS_CERT);
        }
    }

    private static final class SignatureSchemesStringizer
    implements SSLStringizer {
        private SignatureSchemesStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new SignatureSchemesSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }
}

