/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.AlgorithmConstraints;
import java.security.CryptoPrimitive;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLProtocolException;
import sun.security.ssl.Alert;
import sun.security.ssl.Authenticator;
import sun.security.ssl.ChangeCipherSpec;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.ClientAuthType;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ClientHello;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.ContentType;
import sun.security.ssl.HKDF;
import sun.security.ssl.HandshakeConsumer;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeOutStream;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.RandomCookie;
import sun.security.ssl.Record;
import sun.security.ssl.SSLCipher;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLExtensions;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLKeyDerivation;
import sun.security.ssl.SSLKeyExchange;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLPossession;
import sun.security.ssl.SSLSecretDerivation;
import sun.security.ssl.SSLSessionContextImpl;
import sun.security.ssl.SSLSessionImpl;
import sun.security.ssl.SSLTrafficKeyDerivation;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.SessionId;
import sun.security.ssl.SignatureScheme;
import sun.security.ssl.StatusResponseManager;
import sun.security.ssl.SupportedVersionsExtension;
import sun.security.ssl.Utilities;

final class ServerHello {
    static final SSLConsumer handshakeConsumer = new ServerHelloConsumer();
    static final HandshakeProducer t12HandshakeProducer = new T12ServerHelloProducer();
    static final HandshakeProducer t13HandshakeProducer = new T13ServerHelloProducer();
    static final HandshakeProducer hrrHandshakeProducer = new T13HelloRetryRequestProducer();
    static final HandshakeProducer hrrReproducer = new T13HelloRetryRequestReproducer();
    private static final HandshakeConsumer t12HandshakeConsumer = new T12ServerHelloConsumer();
    private static final HandshakeConsumer t13HandshakeConsumer = new T13ServerHelloConsumer();
    private static final HandshakeConsumer d12HandshakeConsumer = new T12ServerHelloConsumer();
    private static final HandshakeConsumer d13HandshakeConsumer = new T13ServerHelloConsumer();
    private static final HandshakeConsumer t13HrrHandshakeConsumer = new T13HelloRetryRequestConsumer();
    private static final HandshakeConsumer d13HrrHandshakeConsumer = new T13HelloRetryRequestConsumer();

    ServerHello() {
    }

    private static void setUpPskKD(HandshakeContext hc, SecretKey psk) throws SSLHandshakeException {
        if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
            SSLLogger.fine("Using PSK to derive early secret", new Object[0]);
        }
        try {
            CipherSuite.HashAlg hashAlg = hc.negotiatedCipherSuite.hashAlg;
            HKDF hkdf = new HKDF(hashAlg.name);
            byte[] zeros = new byte[hashAlg.hashLength];
            SecretKey earlySecret = hkdf.extract(zeros, psk, "TlsEarlySecret");
            hc.handshakeKeyDerivation = new SSLSecretDerivation(hc, earlySecret);
        }
        catch (GeneralSecurityException gse) {
            throw new SSLHandshakeException("Could not generate secret", gse);
        }
    }

    private static final class ServerHelloConsumer
    implements SSLConsumer {
        private ServerHelloConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            chc.handshakeConsumers.remove(SSLHandshake.SERVER_HELLO.id);
            if (!chc.handshakeConsumers.isEmpty()) {
                chc.handshakeConsumers.remove(SSLHandshake.HELLO_VERIFY_REQUEST.id);
            }
            if (!chc.handshakeConsumers.isEmpty()) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "No more message expected before ServerHello is processed");
            }
            ServerHelloMessage shm = new ServerHelloMessage(chc, message);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Consuming ServerHello handshake message", shm);
            }
            if (shm.serverRandom.isHelloRetryRequest()) {
                this.onHelloRetryRequest(chc, shm);
            } else {
                this.onServerHello(chc, shm);
            }
        }

        private void onHelloRetryRequest(ClientHandshakeContext chc, ServerHelloMessage helloRetryRequest) throws IOException {
            SSLExtension[] extTypes = new SSLExtension[]{SSLExtension.HRR_SUPPORTED_VERSIONS};
            helloRetryRequest.extensions.consumeOnLoad(chc, extTypes);
            SupportedVersionsExtension.SHSupportedVersionsSpec svs = (SupportedVersionsExtension.SHSupportedVersionsSpec)chc.handshakeExtensions.get(SSLExtension.HRR_SUPPORTED_VERSIONS);
            ProtocolVersion serverVersion = svs != null ? ProtocolVersion.valueOf(svs.selectedVersion) : helloRetryRequest.serverVersion;
            if (!chc.activeProtocols.contains((Object)serverVersion)) {
                throw chc.conContext.fatal(Alert.PROTOCOL_VERSION, "The server selected protocol version " + (Object)((Object)serverVersion) + " is not accepted by client preferences " + chc.activeProtocols);
            }
            if (!serverVersion.useTLS13PlusSpec()) {
                throw chc.conContext.fatal(Alert.PROTOCOL_VERSION, "Unexpected HelloRetryRequest for " + serverVersion.name);
            }
            chc.negotiatedProtocol = serverVersion;
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Negotiated protocol version: " + serverVersion.name, new Object[0]);
            }
            SignatureScheme.updateHandshakeLocalSupportedAlgs(chc);
            chc.handshakePossessions.clear();
            if (serverVersion.isDTLS) {
                d13HrrHandshakeConsumer.consume(chc, helloRetryRequest);
            } else {
                t13HrrHandshakeConsumer.consume(chc, helloRetryRequest);
            }
        }

        private void onServerHello(ClientHandshakeContext chc, ServerHelloMessage serverHello) throws IOException {
            SSLExtension[] extTypes = new SSLExtension[]{SSLExtension.SH_SUPPORTED_VERSIONS};
            serverHello.extensions.consumeOnLoad(chc, extTypes);
            SupportedVersionsExtension.SHSupportedVersionsSpec svs = (SupportedVersionsExtension.SHSupportedVersionsSpec)chc.handshakeExtensions.get(SSLExtension.SH_SUPPORTED_VERSIONS);
            ProtocolVersion serverVersion = svs != null ? ProtocolVersion.valueOf(svs.selectedVersion) : serverHello.serverVersion;
            if (!chc.activeProtocols.contains((Object)serverVersion)) {
                throw chc.conContext.fatal(Alert.PROTOCOL_VERSION, "The server selected protocol version " + (Object)((Object)serverVersion) + " is not accepted by client preferences " + chc.activeProtocols);
            }
            chc.negotiatedProtocol = serverVersion;
            if (!chc.conContext.isNegotiated) {
                chc.conContext.protocolVersion = chc.negotiatedProtocol;
                chc.conContext.outputRecord.setVersion(chc.negotiatedProtocol);
            }
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Negotiated protocol version: " + serverVersion.name, new Object[0]);
            }
            SignatureScheme.updateHandshakeLocalSupportedAlgs(chc);
            if (serverHello.serverRandom.isVersionDowngrade(chc)) {
                throw chc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "A potential protocol version downgrade attack");
            }
            if (serverVersion.isDTLS) {
                if (serverVersion.useTLS13PlusSpec()) {
                    d13HandshakeConsumer.consume(chc, serverHello);
                } else {
                    chc.handshakePossessions.clear();
                    d12HandshakeConsumer.consume(chc, serverHello);
                }
            } else if (serverVersion.useTLS13PlusSpec()) {
                t13HandshakeConsumer.consume(chc, serverHello);
            } else {
                chc.handshakePossessions.clear();
                t12HandshakeConsumer.consume(chc, serverHello);
            }
        }
    }

    private static final class T12ServerHelloProducer
    implements HandshakeProducer {
        private T12ServerHelloProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            ClientHello.ClientHelloMessage clientHello = (ClientHello.ClientHelloMessage)message;
            if (!shc.isResumption || shc.resumingSession == null) {
                if (!shc.sslConfig.enableSessionCreation) {
                    throw new SSLException("Not resumption, and no new session is allowed");
                }
                SSLSessionImpl session = new SSLSessionImpl((HandshakeContext)shc, CipherSuite.C_NULL);
                session.setMaximumPacketSize(shc.sslConfig.maximumPacketSize);
                shc.handshakeSession = session;
                SSLExtension[] enabledExtensions = shc.sslConfig.getEnabledExtensions(SSLHandshake.CLIENT_HELLO, shc.negotiatedProtocol);
                clientHello.extensions.consumeOnTrade(shc, enabledExtensions);
                KeyExchangeProperties credentials = T12ServerHelloProducer.chooseCipherSuite(shc, clientHello);
                if (credentials == null) {
                    throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "no cipher suites in common");
                }
                shc.negotiatedCipherSuite = credentials.cipherSuite;
                shc.handshakeKeyExchange = credentials.keyExchange;
                shc.handshakeSession.setSuite(credentials.cipherSuite);
                shc.handshakePossessions.addAll(Arrays.asList(credentials.possessions));
                shc.handshakeHash.determine(shc.negotiatedProtocol, shc.negotiatedCipherSuite);
                shc.stapleParams = StatusResponseManager.processStapling(shc);
                shc.staplingActive = shc.stapleParams != null;
                SSLKeyExchange ke = credentials.keyExchange;
                if (ke != null) {
                    for (Map.Entry<Byte, HandshakeProducer> me : ke.getHandshakeProducers(shc)) {
                        shc.handshakeProducers.put(me.getKey(), me.getValue());
                    }
                }
                if (ke != null && shc.sslConfig.clientAuthType != ClientAuthType.CLIENT_AUTH_NONE && !shc.negotiatedCipherSuite.isAnonymous()) {
                    for (SSLHandshake hs : ke.getRelatedHandshakers(shc)) {
                        if (hs != SSLHandshake.CERTIFICATE) continue;
                        shc.handshakeProducers.put(SSLHandshake.CERTIFICATE_REQUEST.id, SSLHandshake.CERTIFICATE_REQUEST);
                        break;
                    }
                }
                shc.handshakeProducers.put(SSLHandshake.SERVER_HELLO_DONE.id, SSLHandshake.SERVER_HELLO_DONE);
            } else {
                if (shc.statelessResumption) {
                    shc.resumingSession = new SSLSessionImpl(shc.resumingSession, clientHello.sessionId.length() == 0 ? new SessionId(true, shc.sslContext.getSecureRandom()) : new SessionId(clientHello.sessionId.getId()));
                }
                shc.handshakeSession = shc.resumingSession;
                shc.negotiatedProtocol = shc.resumingSession.getProtocolVersion();
                shc.negotiatedCipherSuite = shc.resumingSession.getSuite();
                shc.handshakeHash.determine(shc.negotiatedProtocol, shc.negotiatedCipherSuite);
            }
            ServerHelloMessage shm = new ServerHelloMessage(shc, shc.negotiatedProtocol, shc.handshakeSession.getSessionId(), shc.negotiatedCipherSuite, new RandomCookie(shc), clientHello);
            shc.serverHelloRandom = shm.serverRandom;
            SSLExtension[] serverHelloExtensions = shc.sslConfig.getEnabledExtensions(SSLHandshake.SERVER_HELLO, shc.negotiatedProtocol);
            shm.extensions.produce(shc, serverHelloExtensions);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced ServerHello handshake message", shm);
            }
            shm.write(shc.handshakeOutput);
            shc.handshakeOutput.flush();
            if (shc.isResumption && shc.resumingSession != null) {
                SSLTrafficKeyDerivation kdg = SSLTrafficKeyDerivation.valueOf(shc.negotiatedProtocol);
                if (kdg == null) {
                    throw shc.conContext.fatal(Alert.INTERNAL_ERROR, "Not supported key derivation: " + (Object)((Object)shc.negotiatedProtocol));
                }
                shc.handshakeKeyDerivation = kdg.createKeyDerivation(shc, shc.resumingSession.getMasterSecret());
                shc.handshakeProducers.put(SSLHandshake.FINISHED.id, SSLHandshake.FINISHED);
            }
            return null;
        }

        private static KeyExchangeProperties chooseCipherSuite(ServerHandshakeContext shc, ClientHello.ClientHelloMessage clientHello) throws IOException {
            SSLPossession[] hcds;
            SSLKeyExchange ke;
            List proposed;
            List<CipherSuite> preferred;
            if (shc.sslConfig.preferLocalCipherSuites) {
                preferred = shc.activeCipherSuites;
                proposed = clientHello.cipherSuites;
            } else {
                preferred = clientHello.cipherSuites;
                proposed = shc.activeCipherSuites;
            }
            LinkedList<CipherSuite> legacySuites = new LinkedList<CipherSuite>();
            for (CipherSuite cs : preferred) {
                if (!HandshakeContext.isNegotiable(proposed, shc.negotiatedProtocol, cs) || shc.sslConfig.clientAuthType == ClientAuthType.CLIENT_AUTH_REQUIRED && (cs.keyExchange == CipherSuite.KeyExchange.K_DH_ANON || cs.keyExchange == CipherSuite.KeyExchange.K_ECDH_ANON) || (ke = SSLKeyExchange.valueOf(cs.keyExchange, shc.negotiatedProtocol)) == null) continue;
                if (!ServerHandshakeContext.legacyAlgorithmConstraints.permits(EnumSet.of(CryptoPrimitive.KEY_AGREEMENT), cs.name, null)) {
                    legacySuites.add(cs);
                    continue;
                }
                hcds = ke.createPossessions(shc);
                if (hcds == null || hcds.length == 0) continue;
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("use cipher suite " + cs.name, new Object[0]);
                }
                return new KeyExchangeProperties(cs, ke, hcds);
            }
            for (CipherSuite cs : legacySuites) {
                ke = SSLKeyExchange.valueOf(cs.keyExchange, shc.negotiatedProtocol);
                if (ke == null || (hcds = ke.createPossessions(shc)) == null || hcds.length == 0) continue;
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("use legacy cipher suite " + cs.name, new Object[0]);
                }
                return new KeyExchangeProperties(cs, ke, hcds);
            }
            throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "no cipher suites in common");
        }

        private static final class KeyExchangeProperties {
            final CipherSuite cipherSuite;
            final SSLKeyExchange keyExchange;
            final SSLPossession[] possessions;

            private KeyExchangeProperties(CipherSuite cipherSuite, SSLKeyExchange keyExchange, SSLPossession[] possessions) {
                this.cipherSuite = cipherSuite;
                this.keyExchange = keyExchange;
                this.possessions = possessions;
            }
        }
    }

    private static final class T13ServerHelloProducer
    implements HandshakeProducer {
        private T13ServerHelloProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            SSLCipher.SSLWriteCipher writeCipher;
            SSLCipher.SSLReadCipher readCipher;
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            ClientHello.ClientHelloMessage clientHello = (ClientHello.ClientHelloMessage)message;
            SSLSessionContextImpl sessionCache = (SSLSessionContextImpl)shc.sslContext.engineGetServerSessionContext();
            if (!shc.isResumption || shc.resumingSession == null) {
                if (!shc.sslConfig.enableSessionCreation) {
                    throw new SSLException("Not resumption, and no new session is allowed");
                }
                SSLSessionImpl session = new SSLSessionImpl((HandshakeContext)shc, CipherSuite.C_NULL);
                session.setMaximumPacketSize(shc.sslConfig.maximumPacketSize);
                shc.handshakeSession = session;
                SSLExtension[] enabledExtensions = shc.sslConfig.getEnabledExtensions(SSLHandshake.CLIENT_HELLO, shc.negotiatedProtocol);
                clientHello.extensions.consumeOnTrade(shc, enabledExtensions);
                CipherSuite cipherSuite = T13ServerHelloProducer.chooseCipherSuite(shc, clientHello);
                if (cipherSuite == null) {
                    throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "no cipher suites in common");
                }
                shc.negotiatedCipherSuite = cipherSuite;
                shc.handshakeSession.setSuite(cipherSuite);
                shc.handshakeHash.determine(shc.negotiatedProtocol, shc.negotiatedCipherSuite);
            } else {
                shc.handshakeSession = shc.resumingSession;
                SSLExtension[] enabledExtensions = shc.sslConfig.getEnabledExtensions(SSLHandshake.CLIENT_HELLO, shc.negotiatedProtocol);
                clientHello.extensions.consumeOnTrade(shc, enabledExtensions);
                shc.negotiatedProtocol = shc.resumingSession.getProtocolVersion();
                shc.negotiatedCipherSuite = shc.resumingSession.getSuite();
                shc.handshakeHash.determine(shc.negotiatedProtocol, shc.negotiatedCipherSuite);
                ServerHello.setUpPskKD(shc, shc.resumingSession.consumePreSharedKey());
            }
            shc.handshakeProducers.put(SSLHandshake.ENCRYPTED_EXTENSIONS.id, SSLHandshake.ENCRYPTED_EXTENSIONS);
            shc.handshakeProducers.put(SSLHandshake.FINISHED.id, SSLHandshake.FINISHED);
            ServerHelloMessage shm = new ServerHelloMessage(shc, ProtocolVersion.TLS12, clientHello.sessionId, shc.negotiatedCipherSuite, new RandomCookie(shc), clientHello);
            shc.serverHelloRandom = shm.serverRandom;
            SSLExtension[] serverHelloExtensions = shc.sslConfig.getEnabledExtensions(SSLHandshake.SERVER_HELLO, shc.negotiatedProtocol);
            shm.extensions.produce(shc, serverHelloExtensions);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced ServerHello handshake message", shm);
            }
            shm.write(shc.handshakeOutput);
            shc.handshakeOutput.flush();
            shc.handshakeHash.update();
            SSLKeyExchange ke = shc.handshakeKeyExchange;
            if (ke == null) {
                throw shc.conContext.fatal(Alert.INTERNAL_ERROR, "Not negotiated key shares");
            }
            SSLKeyDerivation handshakeKD = ke.createKeyDerivation(shc);
            SecretKey handshakeSecret = handshakeKD.deriveKey("TlsHandshakeSecret", null);
            SSLTrafficKeyDerivation kdg = SSLTrafficKeyDerivation.valueOf(shc.negotiatedProtocol);
            if (kdg == null) {
                throw shc.conContext.fatal(Alert.INTERNAL_ERROR, "Not supported key derivation: " + (Object)((Object)shc.negotiatedProtocol));
            }
            SSLSecretDerivation kd = new SSLSecretDerivation(shc, handshakeSecret);
            SecretKey readSecret = kd.deriveKey("TlsClientHandshakeTrafficSecret", null);
            SSLKeyDerivation readKD = kdg.createKeyDerivation(shc, readSecret);
            SecretKey readKey = readKD.deriveKey("TlsKey", null);
            SecretKey readIvSecret = readKD.deriveKey("TlsIv", null);
            IvParameterSpec readIv = new IvParameterSpec(readIvSecret.getEncoded());
            try {
                readCipher = shc.negotiatedCipherSuite.bulkCipher.createReadCipher(Authenticator.valueOf(shc.negotiatedProtocol), shc.negotiatedProtocol, readKey, readIv, shc.sslContext.getSecureRandom());
            }
            catch (GeneralSecurityException gse) {
                throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Missing cipher algorithm", gse);
            }
            if (readCipher == null) {
                throw shc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Illegal cipher suite (" + (Object)((Object)shc.negotiatedCipherSuite) + ") and protocol version (" + (Object)((Object)shc.negotiatedProtocol) + ")");
            }
            shc.baseReadSecret = readSecret;
            shc.conContext.inputRecord.changeReadCiphers(readCipher);
            SecretKey writeSecret = kd.deriveKey("TlsServerHandshakeTrafficSecret", null);
            SSLKeyDerivation writeKD = kdg.createKeyDerivation(shc, writeSecret);
            SecretKey writeKey = writeKD.deriveKey("TlsKey", null);
            SecretKey writeIvSecret = writeKD.deriveKey("TlsIv", null);
            IvParameterSpec writeIv = new IvParameterSpec(writeIvSecret.getEncoded());
            try {
                writeCipher = shc.negotiatedCipherSuite.bulkCipher.createWriteCipher(Authenticator.valueOf(shc.negotiatedProtocol), shc.negotiatedProtocol, writeKey, writeIv, shc.sslContext.getSecureRandom());
            }
            catch (GeneralSecurityException gse) {
                throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Missing cipher algorithm", gse);
            }
            if (writeCipher == null) {
                throw shc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Illegal cipher suite (" + (Object)((Object)shc.negotiatedCipherSuite) + ") and protocol version (" + (Object)((Object)shc.negotiatedProtocol) + ")");
            }
            shc.baseWriteSecret = writeSecret;
            shc.conContext.outputRecord.changeWriteCiphers(writeCipher, clientHello.sessionId.length() != 0);
            shc.handshakeKeyDerivation = kd;
            if (sessionCache.statelessEnabled()) {
                shc.statelessResumption = true;
            }
            return null;
        }

        private static CipherSuite chooseCipherSuite(ServerHandshakeContext shc, ClientHello.ClientHelloMessage clientHello) {
            List proposed;
            List<CipherSuite> preferred;
            if (shc.sslConfig.preferLocalCipherSuites) {
                preferred = shc.activeCipherSuites;
                proposed = clientHello.cipherSuites;
            } else {
                preferred = clientHello.cipherSuites;
                proposed = shc.activeCipherSuites;
            }
            CipherSuite legacySuite = null;
            AlgorithmConstraints legacyConstraints = ServerHandshakeContext.legacyAlgorithmConstraints;
            for (CipherSuite cs : preferred) {
                if (!HandshakeContext.isNegotiable(proposed, shc.negotiatedProtocol, cs)) continue;
                if (legacySuite == null && !legacyConstraints.permits(EnumSet.of(CryptoPrimitive.KEY_AGREEMENT), cs.name, null)) {
                    legacySuite = cs;
                    continue;
                }
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("use cipher suite " + cs.name, new Object[0]);
                }
                return cs;
            }
            if (legacySuite != null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("use legacy cipher suite " + legacySuite.name, new Object[0]);
                }
                return legacySuite;
            }
            return null;
        }
    }

    private static final class T13HelloRetryRequestProducer
    implements HandshakeProducer {
        private T13HelloRetryRequestProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            ClientHello.ClientHelloMessage clientHello = (ClientHello.ClientHelloMessage)message;
            CipherSuite cipherSuite = T13ServerHelloProducer.chooseCipherSuite(shc, clientHello);
            if (cipherSuite == null) {
                throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "no cipher suites in common for hello retry request");
            }
            ServerHelloMessage hhrm = new ServerHelloMessage(shc, ProtocolVersion.TLS12, clientHello.sessionId, cipherSuite, RandomCookie.hrrRandom, clientHello);
            shc.negotiatedCipherSuite = cipherSuite;
            shc.handshakeHash.determine(shc.negotiatedProtocol, shc.negotiatedCipherSuite);
            SSLExtension[] serverHelloExtensions = shc.sslConfig.getEnabledExtensions(SSLHandshake.HELLO_RETRY_REQUEST, shc.negotiatedProtocol);
            hhrm.extensions.produce(shc, serverHelloExtensions);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced HelloRetryRequest handshake message", hhrm);
            }
            hhrm.write(shc.handshakeOutput);
            shc.handshakeOutput.flush();
            shc.conContext.outputRecord.changeWriteCiphers(SSLCipher.SSLWriteCipher.nullTlsWriteCipher(), clientHello.sessionId.length() != 0);
            shc.handshakeHash.finish();
            shc.handshakeExtensions.clear();
            shc.handshakeConsumers.put(SSLHandshake.CLIENT_HELLO.id, SSLHandshake.CLIENT_HELLO);
            return null;
        }
    }

    private static final class T13HelloRetryRequestReproducer
    implements HandshakeProducer {
        private T13HelloRetryRequestReproducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            ClientHello.ClientHelloMessage clientHello = (ClientHello.ClientHelloMessage)message;
            CipherSuite cipherSuite = shc.negotiatedCipherSuite;
            ServerHelloMessage hhrm = new ServerHelloMessage(shc, ProtocolVersion.TLS12, clientHello.sessionId, cipherSuite, RandomCookie.hrrRandom, clientHello);
            SSLExtension[] serverHelloExtensions = shc.sslConfig.getEnabledExtensions(SSLHandshake.MESSAGE_HASH, shc.negotiatedProtocol);
            hhrm.extensions.produce(shc, serverHelloExtensions);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Reproduced HelloRetryRequest handshake message", hhrm);
            }
            HandshakeOutStream hos = new HandshakeOutStream(null);
            hhrm.write(hos);
            return hos.toByteArray();
        }
    }

    private static final class T12ServerHelloConsumer
    implements HandshakeConsumer {
        private T12ServerHelloConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            ServerHelloMessage serverHello = (ServerHelloMessage)message;
            if (!chc.isNegotiable(serverHello.serverVersion)) {
                throw chc.conContext.fatal(Alert.PROTOCOL_VERSION, "Server chose " + (Object)((Object)serverHello.serverVersion) + ", but that protocol version is not enabled or not supported by the client.");
            }
            chc.negotiatedCipherSuite = serverHello.cipherSuite;
            chc.handshakeHash.determine(chc.negotiatedProtocol, chc.negotiatedCipherSuite);
            chc.serverHelloRandom = serverHello.serverRandom;
            if (chc.negotiatedCipherSuite.keyExchange == null) {
                throw chc.conContext.fatal(Alert.PROTOCOL_VERSION, "TLS 1.2 or prior version does not support the server cipher suite: " + chc.negotiatedCipherSuite.name);
            }
            SSLExtension[] extTypes = new SSLExtension[]{SSLExtension.SH_RENEGOTIATION_INFO};
            serverHello.extensions.consumeOnLoad(chc, extTypes);
            if (chc.resumingSession != null) {
                if (serverHello.sessionId.equals(chc.resumingSession.getSessionId())) {
                    CipherSuite sessionSuite = chc.resumingSession.getSuite();
                    if (chc.negotiatedCipherSuite != sessionSuite) {
                        throw chc.conContext.fatal(Alert.PROTOCOL_VERSION, "Server returned wrong cipher suite for session");
                    }
                    ProtocolVersion sessionVersion = chc.resumingSession.getProtocolVersion();
                    if (chc.negotiatedProtocol != sessionVersion) {
                        throw chc.conContext.fatal(Alert.PROTOCOL_VERSION, "Server resumed with wrong protocol version");
                    }
                    chc.isResumption = true;
                    chc.resumingSession.setAsSessionResumption(true);
                    chc.handshakeSession = chc.resumingSession;
                } else {
                    chc.resumingSession.invalidate();
                    chc.resumingSession = null;
                    chc.isResumption = false;
                    if (!chc.sslConfig.enableSessionCreation) {
                        throw chc.conContext.fatal(Alert.PROTOCOL_VERSION, "New session creation is disabled");
                    }
                }
            }
            extTypes = chc.sslConfig.getEnabledExtensions(SSLHandshake.SERVER_HELLO);
            serverHello.extensions.consumeOnLoad(chc, extTypes);
            if (!chc.isResumption) {
                if (chc.resumingSession != null) {
                    chc.resumingSession.invalidate();
                    chc.resumingSession = null;
                }
                if (!chc.sslConfig.enableSessionCreation) {
                    throw chc.conContext.fatal(Alert.PROTOCOL_VERSION, "New session creation is disabled");
                }
                if (serverHello.sessionId.length() == 0 && chc.statelessResumption) {
                    SessionId newId = new SessionId(true, chc.sslContext.getSecureRandom());
                    chc.handshakeSession = new SSLSessionImpl(chc, chc.negotiatedCipherSuite, newId);
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.fine("Locally assigned Session Id: " + newId.toString(), new Object[0]);
                    }
                } else {
                    chc.handshakeSession = new SSLSessionImpl(chc, chc.negotiatedCipherSuite, serverHello.sessionId);
                }
                chc.handshakeSession.setMaximumPacketSize(chc.sslConfig.maximumPacketSize);
            }
            serverHello.extensions.consumeOnTrade(chc, extTypes);
            if (chc.isResumption) {
                SSLTrafficKeyDerivation kdg = SSLTrafficKeyDerivation.valueOf(chc.negotiatedProtocol);
                if (kdg == null) {
                    throw chc.conContext.fatal(Alert.INTERNAL_ERROR, "Not supported key derivation: " + (Object)((Object)chc.negotiatedProtocol));
                }
                chc.handshakeKeyDerivation = kdg.createKeyDerivation(chc, chc.resumingSession.getMasterSecret());
                if (chc.statelessResumption) {
                    chc.handshakeConsumers.putIfAbsent(SSLHandshake.NEW_SESSION_TICKET.id, SSLHandshake.NEW_SESSION_TICKET);
                }
                chc.conContext.consumers.putIfAbsent(ContentType.CHANGE_CIPHER_SPEC.id, ChangeCipherSpec.t10Consumer);
                chc.handshakeConsumers.put(SSLHandshake.FINISHED.id, SSLHandshake.FINISHED);
            } else {
                SSLKeyExchange ke;
                chc.handshakeKeyExchange = ke = SSLKeyExchange.valueOf(chc.negotiatedCipherSuite.keyExchange, chc.negotiatedProtocol);
                if (ke != null) {
                    for (SSLHandshake handshake : ke.getRelatedHandshakers(chc)) {
                        chc.handshakeConsumers.put(handshake.id, handshake);
                    }
                }
                chc.handshakeConsumers.put(SSLHandshake.SERVER_HELLO_DONE.id, SSLHandshake.SERVER_HELLO_DONE);
            }
        }
    }

    private static final class T13ServerHelloConsumer
    implements HandshakeConsumer {
        private T13ServerHelloConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            SSLCipher.SSLWriteCipher writeCipher;
            SSLCipher.SSLReadCipher readCipher;
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            ServerHelloMessage serverHello = (ServerHelloMessage)message;
            if (serverHello.serverVersion != ProtocolVersion.TLS12) {
                throw chc.conContext.fatal(Alert.PROTOCOL_VERSION, "The ServerHello.legacy_version field is not TLS 1.2");
            }
            chc.negotiatedCipherSuite = serverHello.cipherSuite;
            chc.handshakeHash.determine(chc.negotiatedProtocol, chc.negotiatedCipherSuite);
            chc.serverHelloRandom = serverHello.serverRandom;
            SSLExtension[] extTypes = chc.sslConfig.getEnabledExtensions(SSLHandshake.SERVER_HELLO);
            serverHello.extensions.consumeOnLoad(chc, extTypes);
            if (!chc.isResumption) {
                if (chc.resumingSession != null) {
                    chc.resumingSession.invalidate();
                    chc.resumingSession = null;
                }
                if (!chc.sslConfig.enableSessionCreation) {
                    throw chc.conContext.fatal(Alert.PROTOCOL_VERSION, "New session creation is disabled");
                }
                chc.handshakeSession = new SSLSessionImpl(chc, chc.negotiatedCipherSuite, serverHello.sessionId);
                chc.handshakeSession.setMaximumPacketSize(chc.sslConfig.maximumPacketSize);
            } else {
                SecretKey psk = chc.resumingSession.consumePreSharedKey();
                if (psk == null) {
                    throw chc.conContext.fatal(Alert.INTERNAL_ERROR, "No PSK available. Unable to resume.");
                }
                chc.handshakeSession = chc.resumingSession;
                ServerHello.setUpPskKD(chc, psk);
            }
            serverHello.extensions.consumeOnTrade(chc, extTypes);
            chc.handshakeHash.update();
            SSLKeyExchange ke = chc.handshakeKeyExchange;
            if (ke == null) {
                throw chc.conContext.fatal(Alert.INTERNAL_ERROR, "Not negotiated key shares");
            }
            SSLKeyDerivation handshakeKD = ke.createKeyDerivation(chc);
            SecretKey handshakeSecret = handshakeKD.deriveKey("TlsHandshakeSecret", null);
            SSLTrafficKeyDerivation kdg = SSLTrafficKeyDerivation.valueOf(chc.negotiatedProtocol);
            if (kdg == null) {
                throw chc.conContext.fatal(Alert.INTERNAL_ERROR, "Not supported key derivation: " + (Object)((Object)chc.negotiatedProtocol));
            }
            SSLSecretDerivation secretKD = new SSLSecretDerivation(chc, handshakeSecret);
            SecretKey readSecret = secretKD.deriveKey("TlsServerHandshakeTrafficSecret", null);
            SSLKeyDerivation readKD = kdg.createKeyDerivation(chc, readSecret);
            SecretKey readKey = readKD.deriveKey("TlsKey", null);
            SecretKey readIvSecret = readKD.deriveKey("TlsIv", null);
            IvParameterSpec readIv = new IvParameterSpec(readIvSecret.getEncoded());
            try {
                readCipher = chc.negotiatedCipherSuite.bulkCipher.createReadCipher(Authenticator.valueOf(chc.negotiatedProtocol), chc.negotiatedProtocol, readKey, readIv, chc.sslContext.getSecureRandom());
            }
            catch (GeneralSecurityException gse) {
                throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Missing cipher algorithm", gse);
            }
            if (readCipher == null) {
                throw chc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Illegal cipher suite (" + (Object)((Object)chc.negotiatedCipherSuite) + ") and protocol version (" + (Object)((Object)chc.negotiatedProtocol) + ")");
            }
            chc.baseReadSecret = readSecret;
            chc.conContext.inputRecord.changeReadCiphers(readCipher);
            SecretKey writeSecret = secretKD.deriveKey("TlsClientHandshakeTrafficSecret", null);
            SSLKeyDerivation writeKD = kdg.createKeyDerivation(chc, writeSecret);
            SecretKey writeKey = writeKD.deriveKey("TlsKey", null);
            SecretKey writeIvSecret = writeKD.deriveKey("TlsIv", null);
            IvParameterSpec writeIv = new IvParameterSpec(writeIvSecret.getEncoded());
            try {
                writeCipher = chc.negotiatedCipherSuite.bulkCipher.createWriteCipher(Authenticator.valueOf(chc.negotiatedProtocol), chc.negotiatedProtocol, writeKey, writeIv, chc.sslContext.getSecureRandom());
            }
            catch (GeneralSecurityException gse) {
                throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Missing cipher algorithm", gse);
            }
            if (writeCipher == null) {
                throw chc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Illegal cipher suite (" + (Object)((Object)chc.negotiatedCipherSuite) + ") and protocol version (" + (Object)((Object)chc.negotiatedProtocol) + ")");
            }
            chc.baseWriteSecret = writeSecret;
            chc.conContext.outputRecord.changeWriteCiphers(writeCipher, serverHello.sessionId.length() != 0);
            chc.handshakeKeyDerivation = secretKD;
            chc.conContext.consumers.putIfAbsent(ContentType.CHANGE_CIPHER_SPEC.id, ChangeCipherSpec.t13Consumer);
            chc.handshakeConsumers.put(SSLHandshake.ENCRYPTED_EXTENSIONS.id, SSLHandshake.ENCRYPTED_EXTENSIONS);
            chc.handshakeConsumers.put(SSLHandshake.CERTIFICATE_REQUEST.id, SSLHandshake.CERTIFICATE_REQUEST);
            chc.handshakeConsumers.put(SSLHandshake.CERTIFICATE.id, SSLHandshake.CERTIFICATE);
            chc.handshakeConsumers.put(SSLHandshake.CERTIFICATE_VERIFY.id, SSLHandshake.CERTIFICATE_VERIFY);
            chc.handshakeConsumers.put(SSLHandshake.FINISHED.id, SSLHandshake.FINISHED);
        }
    }

    private static final class T13HelloRetryRequestConsumer
    implements HandshakeConsumer {
        private T13HelloRetryRequestConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            ServerHelloMessage helloRetryRequest = (ServerHelloMessage)message;
            if (helloRetryRequest.serverVersion != ProtocolVersion.TLS12) {
                throw chc.conContext.fatal(Alert.PROTOCOL_VERSION, "The HelloRetryRequest.legacy_version is not TLS 1.2");
            }
            chc.negotiatedCipherSuite = helloRetryRequest.cipherSuite;
            SSLExtension[] extTypes = chc.sslConfig.getEnabledExtensions(SSLHandshake.HELLO_RETRY_REQUEST);
            helloRetryRequest.extensions.consumeOnLoad(chc, extTypes);
            helloRetryRequest.extensions.consumeOnTrade(chc, extTypes);
            chc.handshakeHash.finish();
            HandshakeOutStream hos = new HandshakeOutStream(null);
            try {
                chc.initialClientHelloMsg.write(hos);
            }
            catch (IOException ioe) {
                throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Failed to construct message hash", ioe);
            }
            chc.handshakeHash.deliver(hos.toByteArray());
            chc.handshakeHash.determine(chc.negotiatedProtocol, chc.negotiatedCipherSuite);
            byte[] clientHelloHash = chc.handshakeHash.digest();
            int hashLen = chc.negotiatedCipherSuite.hashAlg.hashLength;
            byte[] hashedClientHello = new byte[4 + hashLen];
            hashedClientHello[0] = SSLHandshake.MESSAGE_HASH.id;
            hashedClientHello[1] = 0;
            hashedClientHello[2] = 0;
            hashedClientHello[3] = (byte)(hashLen & 0xFF);
            System.arraycopy(clientHelloHash, 0, hashedClientHello, 4, hashLen);
            chc.handshakeHash.finish();
            chc.handshakeHash.deliver(hashedClientHello);
            int hrrBodyLen = helloRetryRequest.handshakeRecord.remaining();
            byte[] hrrMessage = new byte[4 + hrrBodyLen];
            hrrMessage[0] = SSLHandshake.HELLO_RETRY_REQUEST.id;
            hrrMessage[1] = (byte)(hrrBodyLen >> 16 & 0xFF);
            hrrMessage[2] = (byte)(hrrBodyLen >> 8 & 0xFF);
            hrrMessage[3] = (byte)(hrrBodyLen & 0xFF);
            ByteBuffer hrrBody = helloRetryRequest.handshakeRecord.duplicate();
            hrrBody.get(hrrMessage, 4, hrrBodyLen);
            chc.handshakeHash.receive(hrrMessage);
            chc.initialClientHelloMsg.extensions.reproduce(chc, new SSLExtension[]{SSLExtension.CH_COOKIE, SSLExtension.CH_KEY_SHARE, SSLExtension.CH_PRE_SHARED_KEY});
            SSLHandshake.CLIENT_HELLO.produce(context, helloRetryRequest);
        }
    }

    static final class ServerHelloMessage
    extends SSLHandshake.HandshakeMessage {
        final ProtocolVersion serverVersion;
        final RandomCookie serverRandom;
        final SessionId sessionId;
        final CipherSuite cipherSuite;
        final byte compressionMethod;
        final SSLExtensions extensions;
        final ClientHello.ClientHelloMessage clientHello;
        final ByteBuffer handshakeRecord;

        ServerHelloMessage(HandshakeContext context, ProtocolVersion serverVersion, SessionId sessionId, CipherSuite cipherSuite, RandomCookie serverRandom, ClientHello.ClientHelloMessage clientHello) {
            super(context);
            this.serverVersion = serverVersion;
            this.serverRandom = serverRandom;
            this.sessionId = sessionId;
            this.cipherSuite = cipherSuite;
            this.compressionMethod = 0;
            this.extensions = new SSLExtensions(this);
            this.clientHello = clientHello;
            this.handshakeRecord = null;
        }

        ServerHelloMessage(HandshakeContext context, ByteBuffer m) throws IOException {
            super(context);
            this.handshakeRecord = m.duplicate();
            byte major = m.get();
            byte minor = m.get();
            this.serverVersion = ProtocolVersion.valueOf(major, minor);
            if (this.serverVersion == null) {
                throw context.conContext.fatal(Alert.PROTOCOL_VERSION, "Unsupported protocol version: " + ProtocolVersion.nameOf(major, minor));
            }
            this.serverRandom = new RandomCookie(m);
            this.sessionId = new SessionId(Record.getBytes8(m));
            try {
                this.sessionId.checkLength(this.serverVersion.id);
            }
            catch (SSLProtocolException ex) {
                throw this.handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, ex);
            }
            int cipherSuiteId = Record.getInt16(m);
            this.cipherSuite = CipherSuite.valueOf(cipherSuiteId);
            if (this.cipherSuite == null || !context.isNegotiable(this.cipherSuite)) {
                throw context.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Server selected improper ciphersuite " + CipherSuite.nameOf(cipherSuiteId));
            }
            this.compressionMethod = m.get();
            if (this.compressionMethod != 0) {
                throw context.conContext.fatal(Alert.ILLEGAL_PARAMETER, "compression type not supported, " + this.compressionMethod);
            }
            SSLExtension[] supportedExtensions = this.serverRandom.isHelloRetryRequest() ? context.sslConfig.getEnabledExtensions(SSLHandshake.HELLO_RETRY_REQUEST) : context.sslConfig.getEnabledExtensions(SSLHandshake.SERVER_HELLO);
            this.extensions = m.hasRemaining() ? new SSLExtensions(this, m, supportedExtensions) : new SSLExtensions(this);
            this.clientHello = null;
        }

        @Override
        public SSLHandshake handshakeType() {
            return this.serverRandom.isHelloRetryRequest() ? SSLHandshake.HELLO_RETRY_REQUEST : SSLHandshake.SERVER_HELLO;
        }

        @Override
        public int messageLength() {
            return 38 + this.sessionId.length() + this.extensions.length();
        }

        @Override
        public void send(HandshakeOutStream hos) throws IOException {
            hos.putInt8(this.serverVersion.major);
            hos.putInt8(this.serverVersion.minor);
            hos.write(this.serverRandom.randomBytes);
            hos.putBytes8(this.sessionId.getId());
            hos.putInt8(this.cipherSuite.id >> 8 & 0xFF);
            hos.putInt8(this.cipherSuite.id & 0xFF);
            hos.putInt8(this.compressionMethod);
            this.extensions.send(hos);
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"{0}\": '{'\n  \"server version\"      : \"{1}\",\n  \"random\"              : \"{2}\",\n  \"session id\"          : \"{3}\",\n  \"cipher suite\"        : \"{4}\",\n  \"compression methods\" : \"{5}\",\n  \"extensions\"          : [\n{6}\n  ]\n'}'", Locale.ENGLISH);
            Object[] messageFields = new Object[]{this.serverRandom.isHelloRetryRequest() ? "HelloRetryRequest" : "ServerHello", this.serverVersion.name, Utilities.toHexString(this.serverRandom.randomBytes), this.sessionId.toString(), this.cipherSuite.name + "(" + Utilities.byte16HexString(this.cipherSuite.id) + ")", HexFormat.of().toHexDigits(this.compressionMethod), Utilities.indent(this.extensions.toString(), "    ")};
            return messageFormat.format(messageFields);
        }
    }
}

