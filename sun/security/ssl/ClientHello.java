/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLProtocolException;
import sun.security.ssl.Alert;
import sun.security.ssl.ChangeCipherSpec;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.ClientAuthType;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.ContentType;
import sun.security.ssl.HandshakeConsumer;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeOutStream;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.HelloCookieManager;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.RandomCookie;
import sun.security.ssl.Record;
import sun.security.ssl.SSLConfiguration;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLExtensions;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLProducer;
import sun.security.ssl.SSLSessionContextImpl;
import sun.security.ssl.SSLSessionImpl;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.SessionId;
import sun.security.ssl.SignatureScheme;
import sun.security.ssl.SupportedVersionsExtension;
import sun.security.ssl.TransportContext;
import sun.security.ssl.Utilities;

final class ClientHello {
    static final SSLProducer kickstartProducer = new ClientHelloKickstartProducer();
    static final SSLConsumer handshakeConsumer = new ClientHelloConsumer();
    static final HandshakeProducer handshakeProducer = new ClientHelloProducer();
    private static final HandshakeConsumer t12HandshakeConsumer = new T12ClientHelloConsumer();
    private static final HandshakeConsumer t13HandshakeConsumer = new T13ClientHelloConsumer();
    private static final HandshakeConsumer d12HandshakeConsumer = new D12ClientHelloConsumer();
    private static final HandshakeConsumer d13HandshakeConsumer = new D13ClientHelloConsumer();

    ClientHello() {
    }

    private static final class ClientHelloKickstartProducer
    implements SSLProducer {
        private ClientHelloKickstartProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context) throws IOException {
            String sessionIdentityAlg;
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            chc.handshakeProducers.remove(SSLHandshake.CLIENT_HELLO.id);
            SessionId sessionId = new SessionId(new byte[0]);
            List<CipherSuite> cipherSuites = chc.activeCipherSuites;
            SSLSessionContextImpl ssci = (SSLSessionContextImpl)chc.sslContext.engineGetClientSessionContext();
            SSLSessionImpl session = ssci.get(chc.conContext.transport.getPeerHost(), chc.conContext.transport.getPeerPort());
            if (session != null) {
                if (!ClientHandshakeContext.allowUnsafeServerCertChange && session.isSessionResumption()) {
                    try {
                        chc.reservedServerCerts = (X509Certificate[])session.getPeerCertificates();
                    }
                    catch (SSLPeerUnverifiedException sSLPeerUnverifiedException) {
                        // empty catch block
                    }
                }
                if (!session.isRejoinable()) {
                    session = null;
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                        SSLLogger.finest("Can't resume, the session is not rejoinable", new Object[0]);
                    }
                }
            }
            CipherSuite sessionSuite = null;
            if (session != null && !chc.isNegotiable(sessionSuite = session.getSuite())) {
                session = null;
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                    SSLLogger.finest("Can't resume, unavailable session cipher suite", new Object[0]);
                }
            }
            ProtocolVersion sessionVersion = null;
            if (session != null && !chc.isNegotiable(sessionVersion = session.getProtocolVersion())) {
                session = null;
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                    SSLLogger.finest("Can't resume, unavailable protocol version", new Object[0]);
                }
            }
            if (session != null && !sessionVersion.useTLS13PlusSpec() && SSLConfiguration.useExtendedMasterSecret) {
                String identityAlg;
                boolean isEmsAvailable = chc.sslConfig.isAvailable(SSLExtension.CH_EXTENDED_MASTER_SECRET, sessionVersion);
                if (isEmsAvailable && !session.useExtendedMasterSecret && !SSLConfiguration.allowLegacyResumption) {
                    session = null;
                }
                if (session != null && !ClientHandshakeContext.allowUnsafeServerCertChange && ((identityAlg = chc.sslConfig.identificationProtocol) == null || identityAlg.isEmpty())) {
                    if (isEmsAvailable) {
                        if (!session.useExtendedMasterSecret) {
                            session = null;
                        }
                    } else {
                        session = null;
                    }
                }
            }
            String identityAlg = chc.sslConfig.identificationProtocol;
            if (session != null && identityAlg != null && !identityAlg.equalsIgnoreCase(sessionIdentityAlg = session.getIdentificationProtocol())) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                    SSLLogger.finest("Can't resume, endpoint id algorithm does not match, requested: " + identityAlg + ", cached: " + sessionIdentityAlg, new Object[0]);
                }
                session = null;
            }
            if (session != null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                    SSLLogger.finest("Try resuming session", session);
                }
                if (!session.getProtocolVersion().useTLS13PlusSpec()) {
                    sessionId = session.getSessionId();
                }
                if (!chc.sslConfig.enableSessionCreation) {
                    cipherSuites = !chc.conContext.isNegotiated && !sessionVersion.useTLS13PlusSpec() && cipherSuites.contains((Object)CipherSuite.TLS_EMPTY_RENEGOTIATION_INFO_SCSV) ? Arrays.asList(sessionSuite, CipherSuite.TLS_EMPTY_RENEGOTIATION_INFO_SCSV) : List.of(sessionSuite);
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                        SSLLogger.finest("No new session is allowed, so try to resume the session cipher suite only", new Object[]{sessionSuite});
                    }
                }
                chc.isResumption = true;
                chc.resumingSession = session;
            }
            if (session == null && !chc.sslConfig.enableSessionCreation) {
                throw new SSLHandshakeException("No new session is allowed and no existing session can be resumed");
            }
            if (sessionId.length() == 0 && chc.maximumActiveProtocol.useTLS13PlusSpec() && SSLConfiguration.useCompatibilityMode) {
                sessionId = new SessionId(true, chc.sslContext.getSecureRandom());
            }
            ProtocolVersion minimumVersion = ProtocolVersion.NONE;
            for (Object pv : chc.activeProtocols) {
                if (minimumVersion != ProtocolVersion.NONE && ((ProtocolVersion)((Object)pv)).compare(minimumVersion) >= 0) continue;
                minimumVersion = pv;
            }
            if (!minimumVersion.useTLS13PlusSpec() && chc.conContext.secureRenegotiation && cipherSuites.contains((Object)CipherSuite.TLS_EMPTY_RENEGOTIATION_INFO_SCSV)) {
                cipherSuites = new LinkedList<CipherSuite>(cipherSuites);
                cipherSuites.remove((Object)CipherSuite.TLS_EMPTY_RENEGOTIATION_INFO_SCSV);
            }
            boolean negotiable = false;
            for (CipherSuite suite : cipherSuites) {
                if (!chc.isNegotiable(suite)) continue;
                negotiable = true;
                break;
            }
            if (!negotiable) {
                throw new SSLHandshakeException("No negotiable cipher suite");
            }
            ProtocolVersion clientHelloVersion = chc.maximumActiveProtocol;
            if (clientHelloVersion.useTLS13PlusSpec()) {
                clientHelloVersion = clientHelloVersion.isDTLS ? ProtocolVersion.DTLS12 : ProtocolVersion.TLS12;
            }
            ClientHelloMessage chm = new ClientHelloMessage(chc, clientHelloVersion.id, sessionId, cipherSuites, chc.sslContext.getSecureRandom());
            chc.clientHelloRandom = chm.clientRandom;
            chc.clientHelloVersion = clientHelloVersion.id;
            SSLExtension[] extTypes = chc.sslConfig.getEnabledExtensions(SSLHandshake.CLIENT_HELLO, chc.activeProtocols);
            chm.extensions.produce(chc, extTypes);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced ClientHello handshake message", chm);
            }
            chm.write(chc.handshakeOutput);
            chc.handshakeOutput.flush();
            chc.initialClientHelloMsg = chm;
            chc.handshakeConsumers.put(SSLHandshake.SERVER_HELLO.id, SSLHandshake.SERVER_HELLO);
            if (chc.sslContext.isDTLS() && !minimumVersion.useTLS13PlusSpec()) {
                chc.handshakeConsumers.put(SSLHandshake.HELLO_VERIFY_REQUEST.id, SSLHandshake.HELLO_VERIFY_REQUEST);
            }
            return null;
        }
    }

    private static final class ClientHelloConsumer
    implements SSLConsumer {
        private ClientHelloConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            shc.handshakeConsumers.remove(SSLHandshake.CLIENT_HELLO.id);
            if (!shc.handshakeConsumers.isEmpty()) {
                throw shc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "No more handshake message allowed in a ClientHello flight");
            }
            SSLExtension[] enabledExtensions = shc.sslConfig.getEnabledExtensions(SSLHandshake.CLIENT_HELLO);
            ClientHelloMessage chm = new ClientHelloMessage(shc, message, enabledExtensions);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Consuming ClientHello handshake message", chm);
            }
            shc.clientHelloVersion = chm.clientVersion;
            this.onClientHello(shc, chm);
        }

        private void onClientHello(ServerHandshakeContext context, ClientHelloMessage clientHello) throws IOException {
            SSLExtension[] extTypes = new SSLExtension[]{SSLExtension.CH_SUPPORTED_VERSIONS};
            clientHello.extensions.consumeOnLoad(context, extTypes);
            SupportedVersionsExtension.CHSupportedVersionsSpec svs = (SupportedVersionsExtension.CHSupportedVersionsSpec)context.handshakeExtensions.get(SSLExtension.CH_SUPPORTED_VERSIONS);
            ProtocolVersion negotiatedProtocol = svs != null ? this.negotiateProtocol(context, svs.requestedProtocols) : this.negotiateProtocol(context, clientHello.clientVersion);
            context.negotiatedProtocol = negotiatedProtocol;
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Negotiated protocol version: " + negotiatedProtocol.name, new Object[0]);
            }
            SignatureScheme.updateHandshakeLocalSupportedAlgs(context);
            if (negotiatedProtocol.isDTLS) {
                if (negotiatedProtocol.useTLS13PlusSpec()) {
                    d13HandshakeConsumer.consume(context, clientHello);
                } else {
                    d12HandshakeConsumer.consume(context, clientHello);
                }
            } else if (negotiatedProtocol.useTLS13PlusSpec()) {
                t13HandshakeConsumer.consume(context, clientHello);
            } else {
                t12HandshakeConsumer.consume(context, clientHello);
            }
        }

        private ProtocolVersion negotiateProtocol(ServerHandshakeContext context, int clientHelloVersion) throws SSLException {
            ProtocolVersion pv;
            int chv = clientHelloVersion;
            if (context.sslContext.isDTLS()) {
                if (chv < ProtocolVersion.DTLS12.id) {
                    chv = ProtocolVersion.DTLS12.id;
                }
            } else if (chv > ProtocolVersion.TLS12.id) {
                chv = ProtocolVersion.TLS12.id;
            }
            if ((pv = ProtocolVersion.selectedFrom(context.activeProtocols, chv)) == null || pv == ProtocolVersion.NONE || pv == ProtocolVersion.SSL20Hello) {
                throw context.conContext.fatal(Alert.PROTOCOL_VERSION, "Client requested protocol " + ProtocolVersion.nameOf(clientHelloVersion) + " is not enabled or supported in server context");
            }
            return pv;
        }

        private ProtocolVersion negotiateProtocol(ServerHandshakeContext context, int[] clientSupportedVersions) throws SSLException {
            for (ProtocolVersion spv : context.activeProtocols) {
                if (spv == ProtocolVersion.SSL20Hello) continue;
                for (int cpv : clientSupportedVersions) {
                    if (cpv == ProtocolVersion.SSL20Hello.id || spv.id != cpv) continue;
                    return spv;
                }
            }
            throw context.conContext.fatal(Alert.PROTOCOL_VERSION, "The client supported protocol versions " + Arrays.toString(ProtocolVersion.toStringArray(clientSupportedVersions)) + " are not accepted by server preferences " + Arrays.toString(ProtocolVersion.toStringArray(context.activeProtocols)));
        }
    }

    private static final class ClientHelloProducer
    implements HandshakeProducer {
        private ClientHelloProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            SSLHandshake ht = message.handshakeType();
            if (ht == null) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            switch (ht) {
                case HELLO_REQUEST: {
                    try {
                        chc.kickstart();
                    }
                    catch (IOException ioe) {
                        throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, ioe);
                    }
                    return null;
                }
                case HELLO_VERIFY_REQUEST: {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.fine("Produced ClientHello(cookie) handshake message", chc.initialClientHelloMsg);
                    }
                    chc.initialClientHelloMsg.write(chc.handshakeOutput);
                    chc.handshakeOutput.flush();
                    chc.handshakeConsumers.put(SSLHandshake.SERVER_HELLO.id, SSLHandshake.SERVER_HELLO);
                    ProtocolVersion minimumVersion = ProtocolVersion.NONE;
                    for (ProtocolVersion pv : chc.activeProtocols) {
                        if (minimumVersion != ProtocolVersion.NONE && pv.compare(minimumVersion) >= 0) continue;
                        minimumVersion = pv;
                    }
                    if (chc.sslContext.isDTLS() && !minimumVersion.useTLS13PlusSpec()) {
                        chc.handshakeConsumers.put(SSLHandshake.HELLO_VERIFY_REQUEST.id, SSLHandshake.HELLO_VERIFY_REQUEST);
                    }
                    return null;
                }
                case HELLO_RETRY_REQUEST: {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.fine("Produced ClientHello(HRR) handshake message", chc.initialClientHelloMsg);
                    }
                    chc.initialClientHelloMsg.write(chc.handshakeOutput);
                    chc.handshakeOutput.flush();
                    chc.conContext.consumers.putIfAbsent(ContentType.CHANGE_CIPHER_SPEC.id, ChangeCipherSpec.t13Consumer);
                    chc.handshakeConsumers.put(SSLHandshake.SERVER_HELLO.id, SSLHandshake.SERVER_HELLO);
                    return null;
                }
            }
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static final class T12ClientHelloConsumer
    implements HandshakeConsumer {
        private T12ClientHelloConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            SSLHandshake[] probableHandshakeMessages;
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            ClientHelloMessage clientHello = (ClientHelloMessage)message;
            if (shc.conContext.isNegotiated) {
                if (!shc.conContext.secureRenegotiation && !HandshakeContext.allowUnsafeRenegotiation) {
                    throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Unsafe renegotiation is not allowed");
                }
                if (ServerHandshakeContext.rejectClientInitiatedRenego && !shc.kickstartMessageDelivered) {
                    throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Client initiated renegotiation is not allowed");
                }
            }
            SSLExtension[] ext = new SSLExtension[]{SSLExtension.CH_SESSION_TICKET};
            clientHello.extensions.consumeOnLoad(shc, ext);
            if (clientHello.sessionId.length() != 0 || shc.statelessResumption) {
                String sessionIdentityAlg;
                CipherSuite suite;
                boolean resumingSession;
                SSLSessionImpl previous;
                block17: {
                    ProtocolVersion sessionProtocol;
                    SSLSessionContextImpl cache = (SSLSessionContextImpl)shc.sslContext.engineGetServerSessionContext();
                    previous = shc.statelessResumption ? shc.resumingSession : cache.get(clientHello.sessionId.getId());
                    boolean bl = resumingSession = previous != null && previous.isRejoinable();
                    if (!resumingSession && SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                        SSLLogger.finest("Can't resume, the existing session is not rejoinable", new Object[0]);
                    }
                    if (resumingSession && (sessionProtocol = previous.getProtocolVersion()) != shc.negotiatedProtocol) {
                        resumingSession = false;
                        if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                            SSLLogger.finest("Can't resume, not the same protocol version", new Object[0]);
                        }
                    }
                    if (resumingSession && shc.sslConfig.clientAuthType == ClientAuthType.CLIENT_AUTH_REQUIRED) {
                        try {
                            previous.getPeerPrincipal();
                        }
                        catch (SSLPeerUnverifiedException e) {
                            resumingSession = false;
                            if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake,verbose")) break block17;
                            SSLLogger.finest("Can't resume, client authentication is required", new Object[0]);
                        }
                    }
                }
                if (!(!resumingSession || shc.isNegotiable(suite = previous.getSuite()) && clientHello.cipherSuites.contains((Object)suite))) {
                    resumingSession = false;
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                        SSLLogger.finest("Can't resume, the session cipher suite is absent", new Object[0]);
                    }
                }
                String identityAlg = shc.sslConfig.identificationProtocol;
                if (resumingSession && identityAlg != null && !identityAlg.equalsIgnoreCase(sessionIdentityAlg = previous.getIdentificationProtocol())) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                        SSLLogger.finest("Can't resume, endpoint id algorithm does not match, requested: " + identityAlg + ", cached: " + sessionIdentityAlg, new Object[0]);
                    }
                    resumingSession = false;
                }
                shc.isResumption = resumingSession;
                SSLSessionImpl sSLSessionImpl = shc.resumingSession = resumingSession ? previous : null;
                if (!resumingSession && SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Session not resumed.", new Object[0]);
                }
            }
            shc.clientHelloRandom = clientHello.clientRandom;
            SSLExtension[] extTypes = shc.sslConfig.getExclusiveExtensions(SSLHandshake.CLIENT_HELLO, List.of(SSLExtension.CH_SESSION_TICKET));
            clientHello.extensions.consumeOnLoad(shc, extTypes);
            if (!shc.conContext.isNegotiated) {
                shc.conContext.protocolVersion = shc.negotiatedProtocol;
                shc.conContext.outputRecord.setVersion(shc.negotiatedProtocol);
            }
            shc.handshakeProducers.put(SSLHandshake.SERVER_HELLO.id, SSLHandshake.SERVER_HELLO);
            for (SSLHandshake hs : probableHandshakeMessages = new SSLHandshake[]{SSLHandshake.SERVER_HELLO, SSLHandshake.CERTIFICATE, SSLHandshake.CERTIFICATE_STATUS, SSLHandshake.SERVER_KEY_EXCHANGE, SSLHandshake.CERTIFICATE_REQUEST, SSLHandshake.SERVER_HELLO_DONE, SSLHandshake.FINISHED}) {
                HandshakeProducer handshakeProducer = (HandshakeProducer)shc.handshakeProducers.remove(hs.id);
                if (handshakeProducer == null) continue;
                handshakeProducer.produce(context, clientHello);
            }
        }
    }

    private static final class T13ClientHelloConsumer
    implements HandshakeConsumer {
        private T13ClientHelloConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            ClientHelloMessage clientHello = (ClientHelloMessage)message;
            if (shc.conContext.isNegotiated) {
                throw shc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Received unexpected renegotiation handshake message");
            }
            if (clientHello.clientVersion != ProtocolVersion.TLS12.id) {
                throw shc.conContext.fatal(Alert.PROTOCOL_VERSION, "The ClientHello.legacy_version field is not TLS 1.2");
            }
            shc.conContext.consumers.putIfAbsent(ContentType.CHANGE_CIPHER_SPEC.id, ChangeCipherSpec.t13Consumer);
            shc.isResumption = true;
            SSLExtension[] extTypes = new SSLExtension[]{SSLExtension.PSK_KEY_EXCHANGE_MODES, SSLExtension.CH_PRE_SHARED_KEY};
            clientHello.extensions.consumeOnLoad(shc, extTypes);
            extTypes = shc.sslConfig.getExclusiveExtensions(SSLHandshake.CLIENT_HELLO, Arrays.asList(SSLExtension.PSK_KEY_EXCHANGE_MODES, SSLExtension.CH_PRE_SHARED_KEY, SSLExtension.CH_SUPPORTED_VERSIONS));
            clientHello.extensions.consumeOnLoad(shc, extTypes);
            if (!shc.handshakeProducers.isEmpty()) {
                this.goHelloRetryRequest(shc, clientHello);
            } else {
                this.goServerHello(shc, clientHello);
            }
        }

        private void goHelloRetryRequest(ServerHandshakeContext shc, ClientHelloMessage clientHello) throws IOException {
            HandshakeProducer handshakeProducer = (HandshakeProducer)shc.handshakeProducers.remove(SSLHandshake.HELLO_RETRY_REQUEST.id);
            if (handshakeProducer == null) {
                throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No HelloRetryRequest producer: " + shc.handshakeProducers);
            }
            handshakeProducer.produce(shc, clientHello);
            if (!shc.handshakeProducers.isEmpty()) {
                throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "unknown handshake producers: " + shc.handshakeProducers);
            }
        }

        private void goServerHello(ServerHandshakeContext shc, ClientHelloMessage clientHello) throws IOException {
            SSLHandshake[] probableHandshakeMessages;
            shc.clientHelloRandom = clientHello.clientRandom;
            if (!shc.conContext.isNegotiated) {
                shc.conContext.protocolVersion = shc.negotiatedProtocol;
                shc.conContext.outputRecord.setVersion(shc.negotiatedProtocol);
            }
            shc.handshakeProducers.put(SSLHandshake.SERVER_HELLO.id, SSLHandshake.SERVER_HELLO);
            for (SSLHandshake hs : probableHandshakeMessages = new SSLHandshake[]{SSLHandshake.SERVER_HELLO, SSLHandshake.ENCRYPTED_EXTENSIONS, SSLHandshake.CERTIFICATE_REQUEST, SSLHandshake.CERTIFICATE, SSLHandshake.CERTIFICATE_VERIFY, SSLHandshake.FINISHED}) {
                HandshakeProducer handshakeProducer = (HandshakeProducer)shc.handshakeProducers.remove(hs.id);
                if (handshakeProducer == null) continue;
                handshakeProducer.produce(shc, clientHello);
            }
        }
    }

    private static final class D12ClientHelloConsumer
    implements HandshakeConsumer {
        private D12ClientHelloConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            SSLHandshake[] probableHandshakeMessages;
            HelloCookieManager hcm;
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            ClientHelloMessage clientHello = (ClientHelloMessage)message;
            if (shc.conContext.isNegotiated) {
                if (!shc.conContext.secureRenegotiation && !HandshakeContext.allowUnsafeRenegotiation) {
                    throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Unsafe renegotiation is not allowed");
                }
                if (ServerHandshakeContext.rejectClientInitiatedRenego && !shc.kickstartMessageDelivered) {
                    throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Client initiated renegotiation is not allowed");
                }
            }
            if (clientHello.sessionId.length() != 0) {
                CipherSuite suite;
                boolean resumingSession;
                SSLSessionImpl previous;
                block15: {
                    ProtocolVersion sessionProtocol;
                    SSLSessionContextImpl cache = (SSLSessionContextImpl)shc.sslContext.engineGetServerSessionContext();
                    SSLExtension[] ext = new SSLExtension[]{SSLExtension.CH_SESSION_TICKET};
                    clientHello.extensions.consumeOnLoad(shc, ext);
                    previous = shc.statelessResumption ? shc.resumingSession : cache.get(clientHello.sessionId.getId());
                    boolean bl = resumingSession = previous != null && previous.isRejoinable();
                    if (!resumingSession && SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                        SSLLogger.finest("Can't resume, the existing session is not rejoinable", new Object[0]);
                    }
                    if (resumingSession && (sessionProtocol = previous.getProtocolVersion()) != shc.negotiatedProtocol) {
                        resumingSession = false;
                        if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                            SSLLogger.finest("Can't resume, not the same protocol version", new Object[0]);
                        }
                    }
                    if (resumingSession && shc.sslConfig.clientAuthType == ClientAuthType.CLIENT_AUTH_REQUIRED) {
                        try {
                            previous.getPeerPrincipal();
                        }
                        catch (SSLPeerUnverifiedException e) {
                            resumingSession = false;
                            if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake,verbose")) break block15;
                            SSLLogger.finest("Can't resume, client authentication is required", new Object[0]);
                        }
                    }
                }
                if (!(!resumingSession || shc.isNegotiable(suite = previous.getSuite()) && clientHello.cipherSuites.contains((Object)suite))) {
                    resumingSession = false;
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                        SSLLogger.finest("Can't resume, the session cipher suite is absent", new Object[0]);
                    }
                }
                shc.isResumption = resumingSession;
                SSLSessionImpl sSLSessionImpl = shc.resumingSession = resumingSession ? previous : null;
            }
            if (!(shc.isResumption && !SSLConfiguration.enableDtlsResumeCookie || (hcm = shc.sslContext.getHelloCookieManager(ProtocolVersion.DTLS10)).isCookieValid(shc, clientHello, clientHello.cookie))) {
                shc.handshakeProducers.put(SSLHandshake.HELLO_VERIFY_REQUEST.id, SSLHandshake.HELLO_VERIFY_REQUEST);
                SSLHandshake.HELLO_VERIFY_REQUEST.produce(context, clientHello);
                return;
            }
            shc.clientHelloRandom = clientHello.clientRandom;
            SSLExtension[] extTypes = shc.sslConfig.getEnabledExtensions(SSLHandshake.CLIENT_HELLO);
            clientHello.extensions.consumeOnLoad(shc, extTypes);
            if (!shc.conContext.isNegotiated) {
                shc.conContext.protocolVersion = shc.negotiatedProtocol;
                shc.conContext.outputRecord.setVersion(shc.negotiatedProtocol);
            }
            shc.handshakeProducers.put(SSLHandshake.SERVER_HELLO.id, SSLHandshake.SERVER_HELLO);
            shc.acceptCliHelloFragments = false;
            for (SSLHandshake hs : probableHandshakeMessages = new SSLHandshake[]{SSLHandshake.SERVER_HELLO, SSLHandshake.CERTIFICATE, SSLHandshake.CERTIFICATE_STATUS, SSLHandshake.SERVER_KEY_EXCHANGE, SSLHandshake.CERTIFICATE_REQUEST, SSLHandshake.SERVER_HELLO_DONE, SSLHandshake.FINISHED}) {
                HandshakeProducer handshakeProducer = (HandshakeProducer)shc.handshakeProducers.remove(hs.id);
                if (handshakeProducer == null) continue;
                handshakeProducer.produce(context, clientHello);
            }
        }
    }

    private static final class D13ClientHelloConsumer
    implements HandshakeConsumer {
        private D13ClientHelloConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    static final class ClientHelloMessage
    extends SSLHandshake.HandshakeMessage {
        private final boolean isDTLS;
        final int clientVersion;
        final RandomCookie clientRandom;
        final SessionId sessionId;
        private byte[] cookie;
        final int[] cipherSuiteIds;
        final List<CipherSuite> cipherSuites;
        final byte[] compressionMethod;
        final SSLExtensions extensions;
        private static final byte[] NULL_COMPRESSION = new byte[]{0};

        ClientHelloMessage(HandshakeContext handshakeContext, int clientVersion, SessionId sessionId, List<CipherSuite> cipherSuites, SecureRandom generator) {
            super(handshakeContext);
            this.isDTLS = handshakeContext.sslContext.isDTLS();
            this.clientVersion = clientVersion;
            this.clientRandom = new RandomCookie(generator);
            this.sessionId = sessionId;
            this.cookie = (byte[])(this.isDTLS ? new byte[0] : null);
            this.cipherSuites = cipherSuites;
            this.cipherSuiteIds = ClientHelloMessage.getCipherSuiteIds(cipherSuites);
            this.extensions = new SSLExtensions(this);
            this.compressionMethod = NULL_COMPRESSION;
        }

        static void readPartial(TransportContext tc, ByteBuffer m) throws IOException {
            boolean isDTLS = tc.sslContext.isDTLS();
            Record.getInt16(m);
            new RandomCookie(m);
            Record.getBytes8(m);
            if (isDTLS) {
                Record.getBytes8(m);
            }
            Record.getBytes16(m);
            Record.getBytes8(m);
            if (m.remaining() >= 2) {
                int extLen;
                for (int remaining = Record.getInt16(m); remaining > 0; remaining -= extLen + 4) {
                    int id = Record.getInt16(m);
                    extLen = Record.getInt16(m);
                    if (id == SSLExtension.CH_PRE_SHARED_KEY.id) {
                        if (remaining > 0) {
                            throw tc.fatal(Alert.ILLEGAL_PARAMETER, "pre_shared_key extension is not last");
                        }
                        Record.getBytes16(m);
                        return;
                    }
                    m.position(m.position() + extLen);
                }
            }
        }

        ClientHelloMessage(HandshakeContext handshakeContext, ByteBuffer m, SSLExtension[] supportedExtensions) throws IOException {
            super(handshakeContext);
            this.isDTLS = handshakeContext.sslContext.isDTLS();
            this.clientVersion = (m.get() & 0xFF) << 8 | m.get() & 0xFF;
            this.clientRandom = new RandomCookie(m);
            this.sessionId = new SessionId(Record.getBytes8(m));
            try {
                this.sessionId.checkLength(this.clientVersion);
            }
            catch (SSLProtocolException ex) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, ex);
            }
            this.cookie = (byte[])(this.isDTLS ? Record.getBytes8(m) : null);
            byte[] encodedIds = Record.getBytes16(m);
            if (encodedIds.length == 0 || (encodedIds.length & 1) != 0) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Invalid ClientHello message");
            }
            this.cipherSuiteIds = new int[encodedIds.length >> 1];
            int i = 0;
            int j = 0;
            while (i < encodedIds.length) {
                this.cipherSuiteIds[j] = (encodedIds[i++] & 0xFF) << 8 | encodedIds[i] & 0xFF;
                ++i;
                ++j;
            }
            this.cipherSuites = ClientHelloMessage.getCipherSuites(this.cipherSuiteIds);
            this.compressionMethod = Record.getBytes8(m);
            this.extensions = m.hasRemaining() ? new SSLExtensions(this, m, supportedExtensions) : new SSLExtensions(this);
        }

        void setHelloCookie(byte[] cookie) {
            this.cookie = cookie;
        }

        byte[] getHelloCookieBytes() {
            HandshakeOutStream hos = new HandshakeOutStream(null);
            try {
                hos.putInt8((byte)(this.clientVersion >>> 8 & 0xFF));
                hos.putInt8((byte)(this.clientVersion & 0xFF));
                hos.write(this.clientRandom.randomBytes, 0, 32);
                hos.putBytes8(this.sessionId.getId());
                hos.putBytes16(this.getEncodedCipherSuites());
                hos.putBytes8(this.compressionMethod);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return hos.toByteArray();
        }

        byte[] getHeaderBytes() {
            HandshakeOutStream hos = new HandshakeOutStream(null);
            try {
                hos.putInt8((byte)(this.clientVersion >>> 8 & 0xFF));
                hos.putInt8((byte)(this.clientVersion & 0xFF));
                hos.write(this.clientRandom.randomBytes, 0, 32);
                hos.putBytes8(this.sessionId.getId());
                hos.putBytes16(this.getEncodedCipherSuites());
                hos.putBytes8(this.compressionMethod);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return hos.toByteArray();
        }

        private static int[] getCipherSuiteIds(List<CipherSuite> cipherSuites) {
            if (cipherSuites != null) {
                int[] ids = new int[cipherSuites.size()];
                int i = 0;
                for (CipherSuite cipherSuite : cipherSuites) {
                    ids[i++] = cipherSuite.id;
                }
                return ids;
            }
            return new int[0];
        }

        private static List<CipherSuite> getCipherSuites(int[] ids) {
            LinkedList<CipherSuite> cipherSuites = new LinkedList<CipherSuite>();
            for (int id : ids) {
                CipherSuite cipherSuite = CipherSuite.valueOf(id);
                if (cipherSuite == null) continue;
                cipherSuites.add(cipherSuite);
            }
            return Collections.unmodifiableList(cipherSuites);
        }

        private List<String> getCipherSuiteNames() {
            LinkedList<String> names = new LinkedList<String>();
            for (int id : this.cipherSuiteIds) {
                names.add(CipherSuite.nameOf(id) + "(" + Utilities.byte16HexString(id) + ")");
            }
            return names;
        }

        private byte[] getEncodedCipherSuites() {
            byte[] encoded = new byte[this.cipherSuiteIds.length << 1];
            int i = 0;
            for (int id : this.cipherSuiteIds) {
                encoded[i++] = (byte)(id >> 8);
                encoded[i++] = (byte)id;
            }
            return encoded;
        }

        @Override
        public SSLHandshake handshakeType() {
            return SSLHandshake.CLIENT_HELLO;
        }

        @Override
        public int messageLength() {
            return 38 + this.sessionId.length() + (this.isDTLS ? 1 + this.cookie.length : 0) + this.cipherSuiteIds.length * 2 + this.compressionMethod.length + this.extensions.length();
        }

        @Override
        public void send(HandshakeOutStream hos) throws IOException {
            this.sendCore(hos);
            this.extensions.send(hos);
        }

        void sendCore(HandshakeOutStream hos) throws IOException {
            hos.putInt8((byte)(this.clientVersion >>> 8));
            hos.putInt8((byte)this.clientVersion);
            hos.write(this.clientRandom.randomBytes, 0, 32);
            hos.putBytes8(this.sessionId.getId());
            if (this.isDTLS) {
                hos.putBytes8(this.cookie);
            }
            hos.putBytes16(this.getEncodedCipherSuites());
            hos.putBytes8(this.compressionMethod);
        }

        public String toString() {
            Object[] messageFields;
            MessageFormat messageFormat;
            if (this.isDTLS) {
                messageFormat = new MessageFormat("\"ClientHello\": '{'\n  \"client version\"      : \"{0}\",\n  \"random\"              : \"{1}\",\n  \"session id\"          : \"{2}\",\n  \"cookie\"              : \"{3}\",\n  \"cipher suites\"       : \"{4}\",\n  \"compression methods\" : \"{5}\",\n  \"extensions\"          : [\n{6}\n  ]\n'}'", Locale.ENGLISH);
                messageFields = new Object[]{ProtocolVersion.nameOf(this.clientVersion), Utilities.toHexString(this.clientRandom.randomBytes), this.sessionId.toString(), Utilities.toHexString(this.cookie), this.getCipherSuiteNames().toString(), Utilities.toHexString(this.compressionMethod), Utilities.indent(Utilities.indent(this.extensions.toString()))};
            } else {
                messageFormat = new MessageFormat("\"ClientHello\": '{'\n  \"client version\"      : \"{0}\",\n  \"random\"              : \"{1}\",\n  \"session id\"          : \"{2}\",\n  \"cipher suites\"       : \"{3}\",\n  \"compression methods\" : \"{4}\",\n  \"extensions\"          : [\n{5}\n  ]\n'}'", Locale.ENGLISH);
                messageFields = new Object[]{ProtocolVersion.nameOf(this.clientVersion), Utilities.toHexString(this.clientRandom.randomBytes), this.sessionId.toString(), this.getCipherSuiteNames().toString(), Utilities.toHexString(this.compressionMethod), Utilities.indent(Utilities.indent(this.extensions.toString()))};
            }
            return messageFormat.format(messageFields);
        }
    }
}

