/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.AlgorithmConstraints;
import java.security.CryptoPrimitive;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import javax.crypto.SecretKey;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLHandshakeException;
import javax.security.auth.x500.X500Principal;
import sun.security.ssl.Alert;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.ContentType;
import sun.security.ssl.HandshakeHash;
import sun.security.ssl.HandshakeOutStream;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.NamedGroup;
import sun.security.ssl.Plaintext;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.RandomCookie;
import sun.security.ssl.Record;
import sun.security.ssl.SSLAlgorithmConstraints;
import sun.security.ssl.SSLConfiguration;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLContextImpl;
import sun.security.ssl.SSLCredentials;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLKeyDerivation;
import sun.security.ssl.SSLKeyExchange;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLPossession;
import sun.security.ssl.SSLSessionImpl;
import sun.security.ssl.SignatureScheme;
import sun.security.ssl.TransportContext;
import sun.security.ssl.Utilities;

abstract class HandshakeContext
implements ConnectionContext {
    static final boolean allowUnsafeRenegotiation = Utilities.getBooleanProperty("sun.security.ssl.allowUnsafeRenegotiation", false);
    static final boolean allowLegacyHelloMessages = Utilities.getBooleanProperty("sun.security.ssl.allowLegacyHelloMessages", true);
    LinkedHashMap<Byte, SSLConsumer> handshakeConsumers;
    final HashMap<Byte, HandshakeProducer> handshakeProducers;
    final SSLContextImpl sslContext;
    final TransportContext conContext;
    final SSLConfiguration sslConfig;
    final List<ProtocolVersion> activeProtocols;
    final List<CipherSuite> activeCipherSuites;
    final SSLAlgorithmConstraints algorithmConstraints;
    final ProtocolVersion maximumActiveProtocol;
    final HandshakeOutStream handshakeOutput;
    final HandshakeHash handshakeHash;
    SSLSessionImpl handshakeSession;
    boolean handshakeFinished;
    boolean kickstartMessageDelivered;
    boolean isResumption;
    SSLSessionImpl resumingSession;
    boolean statelessResumption;
    final Queue<Map.Entry<Byte, ByteBuffer>> delegatedActions;
    volatile boolean taskDelegated;
    volatile Exception delegatedThrown;
    ProtocolVersion negotiatedProtocol;
    CipherSuite negotiatedCipherSuite;
    final List<SSLPossession> handshakePossessions;
    final List<SSLCredentials> handshakeCredentials;
    SSLKeyDerivation handshakeKeyDerivation;
    SSLKeyExchange handshakeKeyExchange;
    SecretKey baseReadSecret;
    SecretKey baseWriteSecret;
    int clientHelloVersion;
    String applicationProtocol;
    RandomCookie clientHelloRandom;
    RandomCookie serverHelloRandom;
    byte[] certRequestContext;
    final Map<SSLExtension, SSLExtension.SSLExtensionSpec> handshakeExtensions;
    int maxFragmentLength;
    List<SignatureScheme> localSupportedSignAlgs;
    List<SignatureScheme> localSupportedCertSignAlgs;
    List<SignatureScheme> peerRequestedSignatureSchemes;
    List<SignatureScheme> peerRequestedCertSignSchemes;
    X500Principal[] peerSupportedAuthorities = null;
    List<NamedGroup> clientRequestedNamedGroups;
    NamedGroup serverSelectedNamedGroup;
    List<SNIServerName> requestedServerNames;
    SNIServerName negotiatedServerName;
    boolean staplingActive = false;

    protected HandshakeContext(SSLContextImpl sslContext, TransportContext conContext) throws IOException {
        this.sslContext = sslContext;
        this.conContext = conContext;
        this.sslConfig = (SSLConfiguration)conContext.sslConfig.clone();
        this.algorithmConstraints = SSLAlgorithmConstraints.wrap(this.sslConfig.userSpecifiedAlgorithmConstraints);
        this.activeProtocols = HandshakeContext.getActiveProtocols(this.sslConfig, this.algorithmConstraints);
        if (this.activeProtocols.isEmpty()) {
            throw new SSLHandshakeException("No appropriate protocol (protocol is disabled or cipher suites are inappropriate)");
        }
        ProtocolVersion maximumVersion = ProtocolVersion.NONE;
        for (ProtocolVersion pv : this.activeProtocols) {
            if (maximumVersion != ProtocolVersion.NONE && pv.compare(maximumVersion) <= 0) continue;
            maximumVersion = pv;
        }
        this.maximumActiveProtocol = maximumVersion;
        this.activeCipherSuites = HandshakeContext.getActiveCipherSuites(this.sslConfig, this.activeProtocols, this.algorithmConstraints);
        if (this.activeCipherSuites.isEmpty()) {
            throw new SSLHandshakeException("No appropriate cipher suite");
        }
        this.handshakeConsumers = new LinkedHashMap();
        this.handshakeProducers = new HashMap();
        this.handshakeHash = conContext.inputRecord.handshakeHash;
        this.handshakeOutput = new HandshakeOutStream(conContext.outputRecord);
        this.handshakeFinished = false;
        this.kickstartMessageDelivered = false;
        this.delegatedActions = new LinkedList<Map.Entry<Byte, ByteBuffer>>();
        this.handshakeExtensions = new HashMap<SSLExtension, SSLExtension.SSLExtensionSpec>();
        this.handshakePossessions = new LinkedList<SSLPossession>();
        this.handshakeCredentials = new LinkedList<SSLCredentials>();
        this.requestedServerNames = null;
        this.negotiatedServerName = null;
        this.negotiatedCipherSuite = conContext.cipherSuite;
        this.initialize();
    }

    protected HandshakeContext(TransportContext conContext) {
        this.sslContext = conContext.sslContext;
        this.conContext = conContext;
        this.sslConfig = conContext.sslConfig;
        this.negotiatedProtocol = conContext.protocolVersion;
        this.negotiatedCipherSuite = conContext.cipherSuite;
        this.handshakeOutput = new HandshakeOutStream(conContext.outputRecord);
        this.delegatedActions = new LinkedList<Map.Entry<Byte, ByteBuffer>>();
        this.handshakeConsumers = new LinkedHashMap();
        this.handshakeProducers = null;
        this.handshakeHash = null;
        this.activeProtocols = null;
        this.activeCipherSuites = null;
        this.algorithmConstraints = null;
        this.maximumActiveProtocol = null;
        this.handshakeExtensions = Collections.emptyMap();
        this.handshakePossessions = null;
        this.handshakeCredentials = null;
    }

    private void initialize() {
        ProtocolVersion outputHelloVersion;
        ProtocolVersion inputHelloVersion;
        if (this.conContext.isNegotiated) {
            inputHelloVersion = this.conContext.protocolVersion;
            outputHelloVersion = this.conContext.protocolVersion;
        } else if (this.activeProtocols.contains((Object)ProtocolVersion.SSL20Hello)) {
            inputHelloVersion = ProtocolVersion.SSL20Hello;
            outputHelloVersion = this.maximumActiveProtocol.useTLS13PlusSpec() ? this.maximumActiveProtocol : ProtocolVersion.SSL20Hello;
        } else {
            inputHelloVersion = this.maximumActiveProtocol;
            outputHelloVersion = this.maximumActiveProtocol;
        }
        this.conContext.inputRecord.setHelloVersion(inputHelloVersion);
        this.conContext.outputRecord.setHelloVersion(outputHelloVersion);
        if (!this.conContext.isNegotiated) {
            this.conContext.protocolVersion = this.maximumActiveProtocol;
        }
        this.conContext.outputRecord.setVersion(this.conContext.protocolVersion);
    }

    private static List<ProtocolVersion> getActiveProtocols(SSLConfiguration sslConfig, AlgorithmConstraints algorithmConstraints) {
        boolean enabledSSL20Hello = false;
        ArrayList<ProtocolVersion> protocols = new ArrayList<ProtocolVersion>(4);
        for (ProtocolVersion protocol : sslConfig.enabledProtocols) {
            if (!enabledSSL20Hello && protocol == ProtocolVersion.SSL20Hello) {
                enabledSSL20Hello = true;
                continue;
            }
            if (!algorithmConstraints.permits(EnumSet.of(CryptoPrimitive.KEY_AGREEMENT), protocol.name, null)) continue;
            boolean found = false;
            EnumMap<NamedGroup.NamedGroupSpec, Boolean> cachedStatus = new EnumMap<NamedGroup.NamedGroupSpec, Boolean>(NamedGroup.NamedGroupSpec.class);
            for (CipherSuite suite : sslConfig.enabledCipherSuites) {
                if (suite.isAvailable() && suite.supports(protocol)) {
                    if (!HandshakeContext.isActivatable(sslConfig, suite, algorithmConstraints, cachedStatus)) continue;
                    protocols.add(protocol);
                    found = true;
                    break;
                }
                if (!SSLLogger.isOn || !SSLLogger.isOn("verbose")) continue;
                SSLLogger.fine("Ignore unsupported cipher suite: " + (Object)((Object)suite) + " for " + protocol.name, new Object[0]);
            }
            if (found || !SSLLogger.isOn || !SSLLogger.isOn("handshake")) continue;
            SSLLogger.fine("No available cipher suite for " + protocol.name, new Object[0]);
        }
        if (!protocols.isEmpty()) {
            if (enabledSSL20Hello) {
                protocols.add(ProtocolVersion.SSL20Hello);
            }
            Collections.sort(protocols);
        }
        return Collections.unmodifiableList(protocols);
    }

    private static List<CipherSuite> getActiveCipherSuites(SSLConfiguration sslConfig, List<ProtocolVersion> enabledProtocols, AlgorithmConstraints algorithmConstraints) {
        LinkedList<CipherSuite> suites = new LinkedList<CipherSuite>();
        if (enabledProtocols != null && !enabledProtocols.isEmpty()) {
            EnumMap<NamedGroup.NamedGroupSpec, Boolean> cachedStatus = new EnumMap<NamedGroup.NamedGroupSpec, Boolean>(NamedGroup.NamedGroupSpec.class);
            for (CipherSuite suite : sslConfig.enabledCipherSuites) {
                if (!suite.isAvailable()) continue;
                boolean isSupported = false;
                for (ProtocolVersion protocol : enabledProtocols) {
                    if (!suite.supports(protocol) || !HandshakeContext.isActivatable(sslConfig, suite, algorithmConstraints, cachedStatus)) continue;
                    suites.add(suite);
                    isSupported = true;
                    break;
                }
                if (isSupported || !SSLLogger.isOn || !SSLLogger.isOn("verbose")) continue;
                SSLLogger.finest("Ignore unsupported cipher suite: " + (Object)((Object)suite), new Object[0]);
            }
        }
        return Collections.unmodifiableList(suites);
    }

    static byte getHandshakeType(TransportContext conContext, Plaintext plaintext) throws IOException {
        if (plaintext.contentType != ContentType.HANDSHAKE.id) {
            throw conContext.fatal(Alert.INTERNAL_ERROR, "Unexpected operation for record: " + plaintext.contentType);
        }
        if (plaintext.fragment == null || plaintext.fragment.remaining() < 4) {
            throw conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Invalid handshake message: insufficient data");
        }
        byte handshakeType = (byte)Record.getInt8(plaintext.fragment);
        int handshakeLen = Record.getInt24(plaintext.fragment);
        if (handshakeLen != plaintext.fragment.remaining()) {
            throw conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Invalid handshake message: insufficient handshake body");
        }
        return handshakeType;
    }

    void dispatch(byte handshakeType, Plaintext plaintext) throws IOException {
        if (this.conContext.transport.useDelegatedTask()) {
            boolean hasDelegated;
            boolean bl = hasDelegated = !this.delegatedActions.isEmpty();
            if (hasDelegated || handshakeType != SSLHandshake.FINISHED.id && handshakeType != SSLHandshake.KEY_UPDATE.id && handshakeType != SSLHandshake.NEW_SESSION_TICKET.id) {
                if (!hasDelegated) {
                    this.taskDelegated = false;
                    this.delegatedThrown = null;
                }
                ByteBuffer fragment = ByteBuffer.wrap(new byte[plaintext.fragment.remaining()]);
                fragment.put(plaintext.fragment);
                fragment = fragment.rewind();
                this.delegatedActions.add(new AbstractMap.SimpleImmutableEntry<Byte, ByteBuffer>(handshakeType, fragment));
                if (hasDelegated && !this.conContext.sslConfig.isClientMode && handshakeType == SSLHandshake.FINISHED.id) {
                    this.conContext.hasDelegatedFinished = true;
                }
            } else {
                this.dispatch(handshakeType, plaintext.fragment);
            }
        } else {
            this.dispatch(handshakeType, plaintext.fragment);
        }
    }

    void dispatch(byte handshakeType, ByteBuffer fragment) throws IOException {
        SSLConsumer consumer = handshakeType == SSLHandshake.HELLO_REQUEST.id ? (this.conContext.sslConfig.isClientMode ? SSLHandshake.HELLO_REQUEST : null) : this.handshakeConsumers.get(handshakeType);
        if (consumer == null) {
            throw this.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected handshake message: " + SSLHandshake.nameOf(handshakeType));
        }
        try {
            consumer.consume(this, fragment);
        }
        catch (UnsupportedOperationException unsoe) {
            throw this.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unsupported handshake message: " + SSLHandshake.nameOf(handshakeType), unsoe);
        }
        catch (BufferOverflowException | BufferUnderflowException be) {
            throw this.conContext.fatal(Alert.DECODE_ERROR, "Illegal handshake message: " + SSLHandshake.nameOf(handshakeType), be);
        }
        this.handshakeHash.consume();
    }

    abstract void kickstart() throws IOException;

    boolean isNegotiable(CipherSuite cs) {
        return HandshakeContext.isNegotiable(this.activeCipherSuites, cs);
    }

    static final boolean isNegotiable(List<CipherSuite> proposed, CipherSuite cs) {
        return proposed.contains((Object)cs) && cs.isNegotiable();
    }

    static final boolean isNegotiable(List<CipherSuite> proposed, ProtocolVersion protocolVersion, CipherSuite cs) {
        return proposed.contains((Object)cs) && cs.isNegotiable() && cs.supports(protocolVersion);
    }

    boolean isNegotiable(ProtocolVersion protocolVersion) {
        return this.activeProtocols.contains((Object)protocolVersion);
    }

    private static boolean isActivatable(SSLConfiguration sslConfig, CipherSuite suite, AlgorithmConstraints algorithmConstraints, Map<NamedGroup.NamedGroupSpec, Boolean> cachedStatus) {
        if (algorithmConstraints.permits(EnumSet.of(CryptoPrimitive.KEY_AGREEMENT), suite.name, null)) {
            NamedGroup.NamedGroupSpec[] groupTypes;
            if (suite.keyExchange == null) {
                return true;
            }
            boolean retval = false;
            for (NamedGroup.NamedGroupSpec groupType : groupTypes = suite.keyExchange.groupTypes) {
                if (groupType != NamedGroup.NamedGroupSpec.NAMED_GROUP_NONE) {
                    boolean groupAvailable;
                    Boolean checkedStatus = cachedStatus.get(groupType);
                    if (checkedStatus == null) {
                        groupAvailable = NamedGroup.isActivatable(sslConfig, algorithmConstraints, groupType);
                        cachedStatus.put(groupType, groupAvailable);
                        if (!groupAvailable && SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                            SSLLogger.fine("No activated named group in " + groupType, new Object[0]);
                        }
                    } else {
                        groupAvailable = checkedStatus;
                    }
                    retval |= groupAvailable;
                    continue;
                }
                retval = true;
            }
            if (!retval && SSLLogger.isOn && SSLLogger.isOn("verbose")) {
                SSLLogger.fine("No active named group(s), ignore " + (Object)((Object)suite), new Object[0]);
            }
            return retval;
        }
        if (SSLLogger.isOn && SSLLogger.isOn("verbose")) {
            SSLLogger.fine("Ignore disabled cipher suite: " + (Object)((Object)suite), new Object[0]);
        }
        return false;
    }

    List<SNIServerName> getRequestedServerNames() {
        return Objects.requireNonNullElse(this.requestedServerNames, Collections.emptyList());
    }
}

