/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLPermission;
import javax.net.ssl.SSLSessionBindingEvent;
import javax.net.ssl.SSLSessionBindingListener;
import javax.net.ssl.SSLSessionContext;
import sun.security.provider.X509Factory;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeOutStream;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.Record;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLSessionContextImpl;
import sun.security.ssl.SecureKey;
import sun.security.ssl.SessionId;
import sun.security.ssl.SignatureScheme;
import sun.security.ssl.Utilities;

final class SSLSessionImpl
extends ExtendedSSLSession {
    private final ProtocolVersion protocolVersion;
    private final SessionId sessionId;
    private X509Certificate[] peerCerts;
    private CipherSuite cipherSuite;
    private SecretKey masterSecret;
    final boolean useExtendedMasterSecret;
    private final long creationTime;
    private long lastUsedTime = 0L;
    private final String host;
    private final int port;
    private SSLSessionContextImpl context;
    private boolean invalidated;
    private X509Certificate[] localCerts;
    private PrivateKey localPrivateKey;
    private final Collection<SignatureScheme> localSupportedSignAlgs;
    private Collection<SignatureScheme> peerSupportedSignAlgs;
    private boolean useDefaultPeerSignAlgs = false;
    private List<byte[]> statusResponses;
    private SecretKey resumptionMasterSecret;
    private SecretKey preSharedKey;
    private byte[] pskIdentity;
    private final long ticketCreationTime = System.currentTimeMillis();
    private int ticketAgeAdd;
    private int negotiatedMaxFragLen = -1;
    private int maximumPacketSize;
    private final Queue<SSLSessionImpl> childSessions = new ConcurrentLinkedQueue<SSLSessionImpl>();
    private boolean isSessionResumption = false;
    private static final boolean defaultRejoinable = true;
    final SNIServerName serverNameIndication;
    private final List<SNIServerName> requestedServerNames;
    private BigInteger ticketNonceCounter = BigInteger.ONE;
    private final String identificationProtocol;
    private final ReentrantLock sessionLock = new ReentrantLock();
    private static final ArrayList<SignatureScheme> defaultPeerSupportedSignAlgs = new ArrayList<SignatureScheme>(Arrays.asList(SignatureScheme.RSA_PKCS1_SHA1, SignatureScheme.DSA_SHA1, SignatureScheme.ECDSA_SHA1));
    private final ConcurrentHashMap<SecureKey, Object> boundValues;
    boolean updateNST;
    private boolean acceptLargeFragments = Utilities.getBooleanProperty("jsse.SSLEngine.acceptLargeFragments", false);

    SSLSessionImpl() {
        this.protocolVersion = ProtocolVersion.NONE;
        this.cipherSuite = CipherSuite.C_NULL;
        this.sessionId = new SessionId(false, null);
        this.host = null;
        this.port = -1;
        this.localSupportedSignAlgs = Collections.emptySet();
        this.serverNameIndication = null;
        this.requestedServerNames = Collections.emptyList();
        this.useExtendedMasterSecret = false;
        this.creationTime = System.currentTimeMillis();
        this.identificationProtocol = null;
        this.boundValues = new ConcurrentHashMap();
    }

    SSLSessionImpl(HandshakeContext hc, CipherSuite cipherSuite) {
        this(hc, cipherSuite, new SessionId(true, hc.sslContext.getSecureRandom()));
    }

    SSLSessionImpl(HandshakeContext hc, CipherSuite cipherSuite, SessionId id) {
        this(hc, cipherSuite, id, System.currentTimeMillis());
    }

    SSLSessionImpl(HandshakeContext hc, CipherSuite cipherSuite, SessionId id, long creationTime) {
        this.protocolVersion = hc.negotiatedProtocol;
        this.cipherSuite = cipherSuite;
        this.sessionId = id;
        this.host = hc.conContext.transport.getPeerHost();
        this.port = hc.conContext.transport.getPeerPort();
        this.localSupportedSignAlgs = hc.localSupportedCertSignAlgs == null ? Collections.emptySet() : Collections.unmodifiableCollection(new ArrayList<SignatureScheme>(hc.localSupportedCertSignAlgs));
        this.serverNameIndication = hc.negotiatedServerName;
        this.requestedServerNames = List.copyOf(hc.getRequestedServerNames());
        this.useExtendedMasterSecret = hc.sslConfig.isClientMode ? hc.handshakeExtensions.get(SSLExtension.CH_EXTENDED_MASTER_SECRET) != null && hc.handshakeExtensions.get(SSLExtension.SH_EXTENDED_MASTER_SECRET) != null : hc.handshakeExtensions.get(SSLExtension.CH_EXTENDED_MASTER_SECRET) != null && !hc.negotiatedProtocol.useTLS13PlusSpec();
        this.creationTime = creationTime;
        this.identificationProtocol = hc.sslConfig.identificationProtocol;
        this.boundValues = new ConcurrentHashMap();
        if (SSLLogger.isOn && SSLLogger.isOn("session")) {
            SSLLogger.finest("Session initialized:  " + this, new Object[0]);
        }
    }

    SSLSessionImpl(SSLSessionImpl baseSession, SessionId newId) {
        this.protocolVersion = baseSession.getProtocolVersion();
        this.cipherSuite = baseSession.cipherSuite;
        this.sessionId = newId;
        this.host = baseSession.getPeerHost();
        this.port = baseSession.getPeerPort();
        this.localSupportedSignAlgs = baseSession.localSupportedSignAlgs == null ? Collections.emptySet() : baseSession.localSupportedSignAlgs;
        this.peerSupportedSignAlgs = baseSession.peerSupportedSignAlgs == null ? Collections.emptySet() : baseSession.peerSupportedSignAlgs;
        this.serverNameIndication = baseSession.serverNameIndication;
        this.requestedServerNames = baseSession.getRequestedServerNames();
        this.masterSecret = baseSession.getMasterSecret();
        this.useExtendedMasterSecret = baseSession.useExtendedMasterSecret;
        this.creationTime = baseSession.getCreationTime();
        this.lastUsedTime = System.currentTimeMillis();
        this.identificationProtocol = baseSession.getIdentificationProtocol();
        this.localCerts = baseSession.localCerts;
        this.peerCerts = baseSession.peerCerts;
        this.statusResponses = baseSession.statusResponses;
        this.resumptionMasterSecret = baseSession.resumptionMasterSecret;
        this.context = baseSession.context;
        this.negotiatedMaxFragLen = baseSession.negotiatedMaxFragLen;
        this.maximumPacketSize = baseSession.maximumPacketSize;
        this.boundValues = baseSession.boundValues;
        if (SSLLogger.isOn && SSLLogger.isOn("session")) {
            SSLLogger.finest("Session initialized:  " + this, new Object[0]);
        }
    }

    SSLSessionImpl(HandshakeContext hc, ByteBuffer buf) throws IOException {
        int len;
        this.boundValues = new ConcurrentHashMap();
        this.protocolVersion = ProtocolVersion.valueOf(Record.getInt16(buf));
        this.sessionId = new SessionId(true, hc.sslContext.getSecureRandom());
        this.cipherSuite = CipherSuite.valueOf(Record.getInt16(buf));
        ArrayList<SignatureScheme> list = new ArrayList<SignatureScheme>();
        int i = Record.getInt8(buf);
        while (i-- > 0) {
            list.add(SignatureScheme.valueOf(Record.getInt16(buf)));
        }
        this.localSupportedSignAlgs = Collections.unmodifiableCollection(list);
        i = Record.getInt8(buf);
        list.clear();
        while (i-- > 0) {
            list.add(SignatureScheme.valueOf(Record.getInt16(buf)));
        }
        this.peerSupportedSignAlgs = Collections.unmodifiableCollection(list);
        byte[] b = Record.getBytes16(buf);
        if (b.length > 0) {
            b = Record.getBytes16(buf);
            this.preSharedKey = new SecretKeySpec(b, "TlsMasterSecret");
        } else {
            this.preSharedKey = null;
        }
        b = Record.getBytes8(buf);
        this.pskIdentity = (byte[])(b.length > 0 ? b : null);
        b = Record.getBytes8(buf);
        if (b.length > 0) {
            b = Record.getBytes16(buf);
            this.masterSecret = new SecretKeySpec(b, "TlsMasterSecret");
        } else {
            this.masterSecret = null;
        }
        this.useExtendedMasterSecret = Record.getInt8(buf) != 0;
        b = Record.getBytes8(buf);
        this.identificationProtocol = b.length == 0 ? null : new String(b);
        b = Record.getBytes8(buf);
        this.serverNameIndication = b.length == 0 ? null : new SNIHostName(b);
        if (len == 0) {
            this.requestedServerNames = Collections.emptyList();
        } else {
            this.requestedServerNames = new ArrayList<SNIServerName>();
            for (len = Record.getInt16(buf); len > 0; --len) {
                b = Record.getBytes8(buf);
                this.requestedServerNames.add(new SNIHostName(new String(b)));
            }
        }
        this.maximumPacketSize = buf.getInt();
        this.negotiatedMaxFragLen = buf.getInt();
        this.creationTime = buf.getLong();
        len = Record.getInt16(buf);
        this.statusResponses = len == 0 ? Collections.emptyList() : new ArrayList<byte[]>();
        while (len-- > 0) {
            b = Record.getBytes16(buf);
            this.statusResponses.add(b);
        }
        b = Record.getBytes8(buf);
        this.host = b.length == 0 ? "" : new String(b);
        this.port = Record.getInt16(buf);
        i = Record.getInt8(buf);
        if (i == 0) {
            this.peerCerts = null;
        } else {
            this.peerCerts = new X509Certificate[i];
            for (int j = 0; i > j; ++j) {
                b = new byte[buf.getInt()];
                buf.get(b);
                try {
                    this.peerCerts[j] = X509Factory.cachedGetX509Cert(b);
                    continue;
                }
                catch (Exception e) {
                    throw new IOException(e);
                }
            }
        }
        switch (Record.getInt8(buf)) {
            case 0: {
                break;
            }
            case 1: {
                len = buf.get();
                this.localCerts = new X509Certificate[len];
                for (i = 0; len > i; ++i) {
                    b = new byte[buf.getInt()];
                    buf.get(b);
                    try {
                        this.localCerts[i] = X509Factory.cachedGetX509Cert(b);
                        continue;
                    }
                    catch (Exception e) {
                        throw new IOException(e);
                    }
                }
                break;
            }
            case 2: {
                b = Record.getBytes8(buf);
                String alg = new String(b);
                b = Record.getBytes16(buf);
                this.preSharedKey = new SecretKeySpec(b, alg);
                this.pskIdentity = Record.getBytes8(buf);
                break;
            }
            default: {
                throw new SSLException("Failed local certs of session.");
            }
        }
        this.context = (SSLSessionContextImpl)hc.sslContext.engineGetServerSessionContext();
        this.lastUsedTime = System.currentTimeMillis();
    }

    boolean isStatelessable() {
        if (!this.protocolVersion.useTLS13PlusSpec() && this.getMasterSecret().getEncoded() == null) {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.finest("No MasterSecret, cannot make stateless ticket", new Object[0]);
            }
            return false;
        }
        if (this.boundValues != null && this.boundValues.size() > 0) {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.finest("There are boundValues, cannot make stateless ticket", new Object[0]);
            }
            return false;
        }
        return true;
    }

    byte[] write() throws Exception {
        byte[] b;
        HandshakeOutStream hos = new HandshakeOutStream(null);
        hos.putInt16(this.protocolVersion.id);
        hos.putInt16(this.cipherSuite.id);
        hos.putInt8(this.localSupportedSignAlgs.size());
        for (SignatureScheme s : this.localSupportedSignAlgs) {
            hos.putInt16(s.id);
        }
        hos.putInt8(this.peerSupportedSignAlgs.size());
        for (SignatureScheme s : this.peerSupportedSignAlgs) {
            hos.putInt16(s.id);
        }
        if (this.preSharedKey == null || this.preSharedKey.getAlgorithm() == null) {
            hos.putInt16(0);
        } else {
            hos.putInt16(this.preSharedKey.getAlgorithm().length());
            if (this.preSharedKey.getAlgorithm().length() != 0) {
                hos.write(this.preSharedKey.getAlgorithm().getBytes());
            }
            b = this.preSharedKey.getEncoded();
            hos.putInt16(b.length);
            hos.write(b, 0, b.length);
        }
        if (this.pskIdentity == null) {
            hos.putInt8(0);
        } else {
            hos.putInt8(this.pskIdentity.length);
            hos.write(this.pskIdentity, 0, this.pskIdentity.length);
        }
        if (this.getMasterSecret() == null || this.getMasterSecret().getAlgorithm() == null) {
            hos.putInt8(0);
        } else {
            hos.putInt8(this.getMasterSecret().getAlgorithm().length());
            if (this.getMasterSecret().getAlgorithm().length() != 0) {
                hos.write(this.getMasterSecret().getAlgorithm().getBytes());
            }
            b = this.getMasterSecret().getEncoded();
            hos.putInt16(b.length);
            hos.write(b, 0, b.length);
        }
        hos.putInt8(this.useExtendedMasterSecret ? 1 : 0);
        if (this.identificationProtocol == null) {
            hos.putInt8(0);
        } else {
            hos.putInt8(this.identificationProtocol.length());
            hos.write(this.identificationProtocol.getBytes(), 0, this.identificationProtocol.length());
        }
        if (this.serverNameIndication == null) {
            hos.putInt8(0);
        } else {
            b = this.serverNameIndication.getEncoded();
            hos.putInt8(b.length);
            hos.write(b, 0, b.length);
        }
        hos.putInt16(this.requestedServerNames.size());
        if (this.requestedServerNames.size() > 0) {
            for (SNIServerName sn : this.requestedServerNames) {
                b = sn.getEncoded();
                hos.putInt8(b.length);
                hos.write(b, 0, b.length);
            }
        }
        hos.putInt32(this.maximumPacketSize);
        hos.putInt32(this.negotiatedMaxFragLen);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        hos.writeBytes(buffer.putLong(this.creationTime).array());
        List<byte[]> list = this.getStatusResponses();
        int l = list.size();
        hos.putInt16(l);
        for (byte[] e : list) {
            hos.putInt16(e.length);
            hos.write(e);
        }
        if (this.host == null || this.host.length() == 0) {
            hos.putInt8(0);
        } else {
            hos.putInt8(this.host.length());
            hos.writeBytes(this.host.getBytes());
        }
        hos.putInt16(this.port);
        if (this.peerCerts == null || this.peerCerts.length == 0) {
            hos.putInt8(0);
        } else {
            hos.putInt8(this.peerCerts.length);
            for (X509Certificate c : this.peerCerts) {
                b = c.getEncoded();
                hos.putInt32(b.length);
                hos.writeBytes(b);
            }
        }
        if (this.localCerts != null && this.localCerts.length > 0) {
            hos.putInt8(1);
            hos.putInt8(this.localCerts.length);
            for (X509Certificate c : this.localCerts) {
                b = c.getEncoded();
                hos.putInt32(b.length);
                hos.writeBytes(b);
            }
        } else if (this.preSharedKey != null) {
            hos.putInt8(2);
            hos.putInt8(this.preSharedKey.getAlgorithm().length());
            hos.write(this.preSharedKey.getAlgorithm().getBytes());
            b = this.preSharedKey.getEncoded();
            hos.putInt32(b.length);
            hos.writeBytes(b);
            hos.putInt32(this.pskIdentity.length);
            hos.writeBytes(this.pskIdentity);
        } else {
            hos.putInt8(0);
        }
        return hos.toByteArray();
    }

    void setMasterSecret(SecretKey secret) {
        this.masterSecret = secret;
    }

    void setResumptionMasterSecret(SecretKey secret) {
        this.resumptionMasterSecret = secret;
    }

    void setPreSharedKey(SecretKey key) {
        this.preSharedKey = key;
    }

    void addChild(SSLSessionImpl session) {
        this.childSessions.add(session);
    }

    void setTicketAgeAdd(int ticketAgeAdd) {
        this.ticketAgeAdd = ticketAgeAdd;
    }

    void setPskIdentity(byte[] pskIdentity) {
        this.pskIdentity = pskIdentity;
    }

    BigInteger incrTicketNonceCounter() {
        BigInteger result = this.ticketNonceCounter;
        this.ticketNonceCounter = this.ticketNonceCounter.add(BigInteger.ONE);
        return result;
    }

    boolean isPSKable() {
        return this.ticketNonceCounter.compareTo(BigInteger.ZERO) > 0;
    }

    SecretKey getMasterSecret() {
        return this.masterSecret;
    }

    SecretKey getResumptionMasterSecret() {
        return this.resumptionMasterSecret;
    }

    SecretKey getPreSharedKey() {
        this.sessionLock.lock();
        try {
            SecretKey secretKey = this.preSharedKey;
            return secretKey;
        }
        finally {
            this.sessionLock.unlock();
        }
    }

    SecretKey consumePreSharedKey() {
        this.sessionLock.lock();
        try {
            SecretKey secretKey = this.preSharedKey;
            return secretKey;
        }
        finally {
            this.preSharedKey = null;
            this.sessionLock.unlock();
        }
    }

    int getTicketAgeAdd() {
        return this.ticketAgeAdd;
    }

    String getIdentificationProtocol() {
        return this.identificationProtocol;
    }

    byte[] consumePskIdentity() {
        this.sessionLock.lock();
        try {
            byte[] byArray = this.pskIdentity;
            return byArray;
        }
        finally {
            this.pskIdentity = null;
            this.sessionLock.unlock();
        }
    }

    byte[] getPskIdentity() {
        return this.pskIdentity;
    }

    void setPeerCertificates(X509Certificate[] peer) {
        if (this.peerCerts == null) {
            this.peerCerts = peer;
        }
    }

    void setLocalCertificates(X509Certificate[] local) {
        this.localCerts = local;
    }

    void setLocalPrivateKey(PrivateKey privateKey) {
        this.localPrivateKey = privateKey;
    }

    void setPeerSupportedSignatureAlgorithms(Collection<SignatureScheme> signatureSchemes) {
        this.peerSupportedSignAlgs = signatureSchemes;
    }

    void setUseDefaultPeerSignAlgs() {
        this.useDefaultPeerSignAlgs = true;
        this.peerSupportedSignAlgs = defaultPeerSupportedSignAlgs;
    }

    SSLSessionImpl finish() {
        if (this.useDefaultPeerSignAlgs) {
            this.peerSupportedSignAlgs = Collections.emptySet();
        }
        return this;
    }

    void setStatusResponses(List<byte[]> responses) {
        this.statusResponses = responses != null && !responses.isEmpty() ? responses : Collections.emptyList();
    }

    boolean isRejoinable() {
        if (this.protocolVersion.useTLS13PlusSpec()) {
            return !this.invalidated && this.isLocalAuthenticationValid();
        }
        return this.sessionId != null && this.sessionId.length() != 0 && !this.invalidated && this.isLocalAuthenticationValid();
    }

    @Override
    public boolean isValid() {
        this.sessionLock.lock();
        try {
            boolean bl = this.isRejoinable();
            return bl;
        }
        finally {
            this.sessionLock.unlock();
        }
    }

    private boolean isLocalAuthenticationValid() {
        if (this.localPrivateKey != null) {
            try {
                this.localPrivateKey.getAlgorithm();
            }
            catch (Exception e) {
                this.invalidate();
                return false;
            }
        }
        return true;
    }

    @Override
    public byte[] getId() {
        return this.sessionId.getId();
    }

    @Override
    public SSLSessionContext getSessionContext() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SSLPermission("getSSLSessionContext"));
        }
        return this.context;
    }

    SessionId getSessionId() {
        return this.sessionId;
    }

    CipherSuite getSuite() {
        return this.cipherSuite;
    }

    void setSuite(CipherSuite suite) {
        this.cipherSuite = suite;
        if (SSLLogger.isOn && SSLLogger.isOn("session")) {
            SSLLogger.finest("Negotiating session:  " + this, new Object[0]);
        }
    }

    boolean isSessionResumption() {
        return this.isSessionResumption;
    }

    void setAsSessionResumption(boolean flag) {
        this.isSessionResumption = flag;
    }

    @Override
    public String getCipherSuite() {
        return this.getSuite().name;
    }

    ProtocolVersion getProtocolVersion() {
        return this.protocolVersion;
    }

    @Override
    public String getProtocol() {
        return this.getProtocolVersion().name;
    }

    public int hashCode() {
        return this.sessionId.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof SSLSessionImpl) {
            SSLSessionImpl sess = (SSLSessionImpl)obj;
            return this.sessionId != null && this.sessionId.equals(sess.getSessionId());
        }
        return false;
    }

    @Override
    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        if (this.peerCerts == null) {
            throw new SSLPeerUnverifiedException("peer not authenticated");
        }
        return (Certificate[])this.peerCerts.clone();
    }

    @Override
    public Certificate[] getLocalCertificates() {
        return this.localCerts == null ? null : (Certificate[])this.localCerts.clone();
    }

    public X509Certificate[] getCertificateChain() throws SSLPeerUnverifiedException {
        if (this.peerCerts != null) {
            return (X509Certificate[])this.peerCerts.clone();
        }
        throw new SSLPeerUnverifiedException("peer not authenticated");
    }

    @Override
    public List<byte[]> getStatusResponses() {
        if (this.statusResponses == null || this.statusResponses.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<byte[]> responses = new ArrayList<byte[]>(this.statusResponses.size());
        for (byte[] respBytes : this.statusResponses) {
            responses.add((byte[])respBytes.clone());
        }
        return Collections.unmodifiableList(responses);
    }

    @Override
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        if (this.peerCerts == null) {
            throw new SSLPeerUnverifiedException("peer not authenticated");
        }
        return this.peerCerts[0].getSubjectX500Principal();
    }

    @Override
    public Principal getLocalPrincipal() {
        return this.localCerts == null || this.localCerts.length == 0 ? null : this.localCerts[0].getSubjectX500Principal();
    }

    public long getTicketCreationTime() {
        return this.ticketCreationTime;
    }

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    @Override
    public long getLastAccessedTime() {
        return this.lastUsedTime != 0L ? this.lastUsedTime : this.creationTime;
    }

    void setLastAccessedTime(long time) {
        this.lastUsedTime = time;
    }

    public InetAddress getPeerAddress() {
        try {
            return InetAddress.getByName(this.host);
        }
        catch (UnknownHostException e) {
            return null;
        }
    }

    @Override
    public String getPeerHost() {
        return this.host;
    }

    @Override
    public int getPeerPort() {
        return this.port;
    }

    void setContext(SSLSessionContextImpl ctx) {
        if (this.context == null) {
            this.context = ctx;
        }
    }

    @Override
    public void invalidate() {
        this.sessionLock.lock();
        try {
            if (this.context != null) {
                this.context.remove(this.sessionId);
                this.context = null;
            }
            if (this.invalidated) {
                return;
            }
            this.invalidated = true;
            if (SSLLogger.isOn && SSLLogger.isOn("session")) {
                SSLLogger.finest("Invalidated session:  " + this, new Object[0]);
            }
            for (SSLSessionImpl child : this.childSessions) {
                child.invalidate();
            }
        }
        finally {
            this.sessionLock.unlock();
        }
    }

    @Override
    public void putValue(String key, Object value) {
        SSLSessionBindingEvent e;
        if (key == null || value == null) {
            throw new IllegalArgumentException("arguments can not be null");
        }
        SecureKey secureKey = new SecureKey(key);
        Object oldValue = this.boundValues.put(secureKey, value);
        if (oldValue instanceof SSLSessionBindingListener) {
            e = new SSLSessionBindingEvent(this, key);
            ((SSLSessionBindingListener)oldValue).valueUnbound(e);
        }
        if (value instanceof SSLSessionBindingListener) {
            e = new SSLSessionBindingEvent(this, key);
            ((SSLSessionBindingListener)value).valueBound(e);
        }
        if (this.protocolVersion.useTLS13PlusSpec()) {
            this.updateNST = true;
        }
    }

    @Override
    public Object getValue(String key) {
        if (key == null) {
            throw new IllegalArgumentException("argument can not be null");
        }
        SecureKey secureKey = new SecureKey(key);
        return this.boundValues.get(secureKey);
    }

    @Override
    public void removeValue(String key) {
        if (key == null) {
            throw new IllegalArgumentException("argument can not be null");
        }
        SecureKey secureKey = new SecureKey(key);
        Object value = this.boundValues.remove(secureKey);
        if (value instanceof SSLSessionBindingListener) {
            SSLSessionBindingEvent e = new SSLSessionBindingEvent(this, key);
            ((SSLSessionBindingListener)value).valueUnbound(e);
        }
        if (this.protocolVersion.useTLS13PlusSpec()) {
            this.updateNST = true;
        }
    }

    @Override
    public String[] getValueNames() {
        ArrayList<Object> v = new ArrayList<Object>();
        Object securityCtx = SecureKey.getCurrentSecurityContext();
        for (SecureKey key : this.boundValues.keySet()) {
            if (!securityCtx.equals(key.getSecurityContext())) continue;
            v.add(key.getAppKey());
        }
        return v.toArray(new String[0]);
    }

    protected void expandBufferSizes() {
        this.sessionLock.lock();
        try {
            this.acceptLargeFragments = true;
        }
        finally {
            this.sessionLock.unlock();
        }
    }

    @Override
    public int getPacketBufferSize() {
        this.sessionLock.lock();
        try {
            int packetSize = 0;
            if (this.negotiatedMaxFragLen > 0) {
                packetSize = this.cipherSuite.calculatePacketSize(this.negotiatedMaxFragLen, this.protocolVersion, this.protocolVersion.isDTLS);
            }
            if (this.maximumPacketSize > 0) {
                int n = Math.max(this.maximumPacketSize, packetSize);
                return n;
            }
            if (packetSize != 0) {
                int n = packetSize;
                return n;
            }
            if (this.protocolVersion.isDTLS) {
                int n = 16717;
                return n;
            }
            int n = this.acceptLargeFragments ? 33093 : 16709;
            return n;
        }
        finally {
            this.sessionLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getApplicationBufferSize() {
        this.sessionLock.lock();
        try {
            int fragmentSize = 0;
            if (this.maximumPacketSize > 0) {
                fragmentSize = this.cipherSuite.calculateFragSize(this.maximumPacketSize, this.protocolVersion, this.protocolVersion.isDTLS);
            }
            if (this.negotiatedMaxFragLen > 0) {
                int n = Math.max(this.negotiatedMaxFragLen, fragmentSize);
                return n;
            }
            if (fragmentSize != 0) {
                int n = fragmentSize;
                return n;
            }
            if (this.protocolVersion.isDTLS) {
                int n = 16384;
                return n;
            }
            int maxPacketSize = this.acceptLargeFragments ? 33093 : 16709;
            int n = maxPacketSize - 5;
            return n;
        }
        finally {
            this.sessionLock.unlock();
        }
    }

    void setNegotiatedMaxFragSize(int negotiatedMaxFragLen) {
        this.sessionLock.lock();
        try {
            this.negotiatedMaxFragLen = negotiatedMaxFragLen;
        }
        finally {
            this.sessionLock.unlock();
        }
    }

    int getNegotiatedMaxFragSize() {
        this.sessionLock.lock();
        try {
            int n = this.negotiatedMaxFragLen;
            return n;
        }
        finally {
            this.sessionLock.unlock();
        }
    }

    void setMaximumPacketSize(int maximumPacketSize) {
        this.sessionLock.lock();
        try {
            this.maximumPacketSize = maximumPacketSize;
        }
        finally {
            this.sessionLock.unlock();
        }
    }

    int getMaximumPacketSize() {
        this.sessionLock.lock();
        try {
            int n = this.maximumPacketSize;
            return n;
        }
        finally {
            this.sessionLock.unlock();
        }
    }

    @Override
    public String[] getLocalSupportedSignatureAlgorithms() {
        return SignatureScheme.getAlgorithmNames(this.localSupportedSignAlgs);
    }

    public Collection<SignatureScheme> getLocalSupportedSignatureSchemes() {
        return this.localSupportedSignAlgs;
    }

    @Override
    public String[] getPeerSupportedSignatureAlgorithms() {
        return SignatureScheme.getAlgorithmNames(this.peerSupportedSignAlgs);
    }

    @Override
    public List<SNIServerName> getRequestedServerNames() {
        return this.requestedServerNames;
    }

    public String toString() {
        return "Session(" + this.creationTime + "|" + this.getCipherSuite() + ")";
    }
}

