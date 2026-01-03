/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.FileInputStream;
import java.security.AccessController;
import java.security.CryptoPrimitive;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.PrivilegedExceptionAction;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import sun.security.action.GetPropertyAction;
import sun.security.ssl.AbstractKeyManagerWrapper;
import sun.security.ssl.AbstractTrustManagerWrapper;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.DummyX509KeyManager;
import sun.security.ssl.DummyX509TrustManager;
import sun.security.ssl.EphemeralKeyManager;
import sun.security.ssl.HelloCookieManager;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.SSLAlgorithmConstraints;
import sun.security.ssl.SSLEngineImpl;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLServerSocketFactoryImpl;
import sun.security.ssl.SSLSessionContextImpl;
import sun.security.ssl.SSLSocketFactoryImpl;
import sun.security.ssl.StatusResponseManager;
import sun.security.ssl.TrustStoreManager;
import sun.security.ssl.Utilities;

public abstract class SSLContextImpl
extends SSLContextSpi {
    private final EphemeralKeyManager ephemeralKeyManager;
    private final SSLSessionContextImpl clientCache;
    private final SSLSessionContextImpl serverCache;
    private boolean isInitialized;
    private X509ExtendedKeyManager keyManager;
    private X509TrustManager trustManager;
    private SecureRandom secureRandom;
    private volatile HelloCookieManager.Builder helloCookieManagerBuilder;
    private final boolean clientEnableStapling = Utilities.getBooleanProperty("jdk.tls.client.enableStatusRequestExtension", true);
    private final boolean serverEnableStapling = Utilities.getBooleanProperty("jdk.tls.server.enableStatusRequestExtension", false);
    private static final Collection<CipherSuite> clientCustomizedCipherSuites = SSLContextImpl.getCustomizedCipherSuites("jdk.tls.client.cipherSuites");
    private static final Collection<CipherSuite> serverCustomizedCipherSuites = SSLContextImpl.getCustomizedCipherSuites("jdk.tls.server.cipherSuites");
    private volatile StatusResponseManager statusResponseManager;
    private final ReentrantLock contextLock = new ReentrantLock();

    SSLContextImpl() {
        this.ephemeralKeyManager = new EphemeralKeyManager();
        this.clientCache = new SSLSessionContextImpl(false);
        this.serverCache = new SSLSessionContextImpl(true);
    }

    @Override
    protected void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom sr) throws KeyManagementException {
        this.isInitialized = false;
        this.keyManager = this.chooseKeyManager(km);
        if (tm == null) {
            try {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init((KeyStore)null);
                tm = tmf.getTrustManagers();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        this.trustManager = this.chooseTrustManager(tm);
        this.secureRandom = Objects.requireNonNullElseGet(sr, SecureRandom::new);
        if (SSLLogger.isOn && SSLLogger.isOn("ssl,sslctx")) {
            SSLLogger.finest("trigger seeding of SecureRandom", new Object[0]);
        }
        this.secureRandom.nextInt();
        if (SSLLogger.isOn && SSLLogger.isOn("ssl,sslctx")) {
            SSLLogger.finest("done seeding of SecureRandom", new Object[0]);
        }
        this.isInitialized = true;
    }

    private X509TrustManager chooseTrustManager(TrustManager[] tm) {
        for (int i = 0; tm != null && i < tm.length; ++i) {
            if (!(tm[i] instanceof X509TrustManager)) continue;
            if (tm[i] instanceof X509ExtendedTrustManager) {
                return (X509TrustManager)tm[i];
            }
            return new AbstractTrustManagerWrapper((X509TrustManager)tm[i]);
        }
        return DummyX509TrustManager.INSTANCE;
    }

    private X509ExtendedKeyManager chooseKeyManager(KeyManager[] kms) {
        for (int i = 0; kms != null && i < kms.length; ++i) {
            KeyManager km = kms[i];
            if (!(km instanceof X509KeyManager)) continue;
            if (km instanceof X509ExtendedKeyManager) {
                return (X509ExtendedKeyManager)km;
            }
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,sslctx")) {
                SSLLogger.warning("X509KeyManager passed to SSLContext.init():  need an X509ExtendedKeyManager for SSLEngine use", new Object[0]);
            }
            return new AbstractKeyManagerWrapper((X509KeyManager)km);
        }
        return DummyX509KeyManager.INSTANCE;
    }

    abstract SSLEngine createSSLEngineImpl();

    abstract SSLEngine createSSLEngineImpl(String var1, int var2);

    @Override
    protected SSLEngine engineCreateSSLEngine() {
        if (!this.isInitialized) {
            throw new IllegalStateException("SSLContext is not initialized");
        }
        return this.createSSLEngineImpl();
    }

    @Override
    protected SSLEngine engineCreateSSLEngine(String host, int port) {
        if (!this.isInitialized) {
            throw new IllegalStateException("SSLContext is not initialized");
        }
        return this.createSSLEngineImpl(host, port);
    }

    @Override
    protected SSLSocketFactory engineGetSocketFactory() {
        if (!this.isInitialized) {
            throw new IllegalStateException("SSLContext is not initialized");
        }
        if (this.isDTLS()) {
            throw new UnsupportedOperationException("DTLS not supported with SSLSocket");
        }
        return new SSLSocketFactoryImpl(this);
    }

    @Override
    protected SSLServerSocketFactory engineGetServerSocketFactory() {
        if (!this.isInitialized) {
            throw new IllegalStateException("SSLContext is not initialized");
        }
        if (this.isDTLS()) {
            throw new UnsupportedOperationException("DTLS not supported with SSLServerSocket");
        }
        return new SSLServerSocketFactoryImpl(this);
    }

    @Override
    protected SSLSessionContext engineGetClientSessionContext() {
        return this.clientCache;
    }

    @Override
    protected SSLSessionContext engineGetServerSessionContext() {
        return this.serverCache;
    }

    SecureRandom getSecureRandom() {
        return this.secureRandom;
    }

    X509ExtendedKeyManager getX509KeyManager() {
        return this.keyManager;
    }

    X509TrustManager getX509TrustManager() {
        return this.trustManager;
    }

    EphemeralKeyManager getEphemeralKeyManager() {
        return this.ephemeralKeyManager;
    }

    HelloCookieManager getHelloCookieManager(ProtocolVersion protocolVersion) {
        if (this.helloCookieManagerBuilder == null) {
            this.contextLock.lock();
            try {
                if (this.helloCookieManagerBuilder == null) {
                    this.helloCookieManagerBuilder = new HelloCookieManager.Builder(this.secureRandom);
                }
            }
            finally {
                this.contextLock.unlock();
            }
        }
        return this.helloCookieManagerBuilder.valueOf(protocolVersion);
    }

    StatusResponseManager getStatusResponseManager() {
        if (this.serverEnableStapling && this.statusResponseManager == null) {
            this.contextLock.lock();
            try {
                if (this.statusResponseManager == null) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,sslctx")) {
                        SSLLogger.finest("Initializing StatusResponseManager", new Object[0]);
                    }
                    this.statusResponseManager = new StatusResponseManager();
                }
            }
            finally {
                this.contextLock.unlock();
            }
        }
        return this.statusResponseManager;
    }

    abstract List<ProtocolVersion> getSupportedProtocolVersions();

    abstract List<ProtocolVersion> getServerDefaultProtocolVersions();

    abstract List<ProtocolVersion> getClientDefaultProtocolVersions();

    abstract List<CipherSuite> getSupportedCipherSuites();

    abstract List<CipherSuite> getServerDefaultCipherSuites();

    abstract List<CipherSuite> getClientDefaultCipherSuites();

    abstract boolean isDTLS();

    List<ProtocolVersion> getDefaultProtocolVersions(boolean roleIsServer) {
        return roleIsServer ? this.getServerDefaultProtocolVersions() : this.getClientDefaultProtocolVersions();
    }

    List<CipherSuite> getDefaultCipherSuites(boolean roleIsServer) {
        return roleIsServer ? this.getServerDefaultCipherSuites() : this.getClientDefaultCipherSuites();
    }

    boolean isDefaultProtocolVesions(List<ProtocolVersion> protocols) {
        return protocols == this.getServerDefaultProtocolVersions() || protocols == this.getClientDefaultProtocolVersions();
    }

    boolean isDefaultCipherSuiteList(List<CipherSuite> cipherSuites) {
        return cipherSuites == this.getServerDefaultCipherSuites() || cipherSuites == this.getClientDefaultCipherSuites();
    }

    boolean isStaplingEnabled(boolean isClient) {
        return isClient ? this.clientEnableStapling : this.serverEnableStapling;
    }

    private static List<CipherSuite> getApplicableSupportedCipherSuites(List<ProtocolVersion> protocols) {
        return SSLContextImpl.getApplicableCipherSuites(CipherSuite.allowedCipherSuites(), protocols);
    }

    private static List<CipherSuite> getApplicableEnabledCipherSuites(List<ProtocolVersion> protocols, boolean isClient) {
        if (isClient) {
            if (!clientCustomizedCipherSuites.isEmpty()) {
                return SSLContextImpl.getApplicableCipherSuites(clientCustomizedCipherSuites, protocols);
            }
        } else if (!serverCustomizedCipherSuites.isEmpty()) {
            return SSLContextImpl.getApplicableCipherSuites(serverCustomizedCipherSuites, protocols);
        }
        return SSLContextImpl.getApplicableCipherSuites(CipherSuite.defaultCipherSuites(), protocols);
    }

    private static List<CipherSuite> getApplicableCipherSuites(Collection<CipherSuite> allowedCipherSuites, List<ProtocolVersion> protocols) {
        LinkedHashSet<CipherSuite> suites = new LinkedHashSet<CipherSuite>();
        if (protocols != null && !protocols.isEmpty()) {
            for (CipherSuite suite : allowedCipherSuites) {
                if (!suite.isAvailable()) continue;
                boolean isSupported = false;
                for (ProtocolVersion protocol : protocols) {
                    if (!suite.supports(protocol) || !suite.bulkCipher.isAvailable()) continue;
                    if (SSLAlgorithmConstraints.DEFAULT.permits(EnumSet.of(CryptoPrimitive.KEY_AGREEMENT), suite.name, null)) {
                        suites.add(suite);
                        isSupported = true;
                        break;
                    }
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,sslctx,verbose")) break;
                    SSLLogger.fine("Ignore disabled cipher suite: " + suite.name, new Object[0]);
                    break;
                }
                if (isSupported || !SSLLogger.isOn || !SSLLogger.isOn("ssl,sslctx,verbose")) continue;
                SSLLogger.finest("Ignore unsupported cipher suite: " + (Object)((Object)suite), new Object[0]);
            }
        }
        return new ArrayList<CipherSuite>(suites);
    }

    private static Collection<CipherSuite> getCustomizedCipherSuites(String propertyName) {
        String property = GetPropertyAction.privilegedGetProperty(propertyName);
        if (SSLLogger.isOn && SSLLogger.isOn("ssl,sslctx")) {
            SSLLogger.fine("System property " + propertyName + " is set to '" + property + "'", new Object[0]);
        }
        if (property != null && !property.isEmpty() && property.length() > 1 && property.charAt(0) == '\"' && property.charAt(property.length() - 1) == '\"') {
            property = property.substring(1, property.length() - 1);
        }
        if (property != null && !property.isEmpty()) {
            String[] cipherSuiteNames = property.split(",");
            ArrayList<CipherSuite> cipherSuites = new ArrayList<CipherSuite>(cipherSuiteNames.length);
            for (int i = 0; i < cipherSuiteNames.length; ++i) {
                CipherSuite suite;
                cipherSuiteNames[i] = cipherSuiteNames[i].trim();
                if (cipherSuiteNames[i].isEmpty()) continue;
                try {
                    suite = CipherSuite.nameOf(cipherSuiteNames[i]);
                }
                catch (IllegalArgumentException iae) {
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,sslctx")) continue;
                    SSLLogger.fine("Unknown or unsupported cipher suite name: " + cipherSuiteNames[i], new Object[0]);
                    continue;
                }
                if (suite != null && suite.isAvailable()) {
                    cipherSuites.add(suite);
                    continue;
                }
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,sslctx")) continue;
                SSLLogger.fine("The current installed providers do not support cipher suite: " + cipherSuiteNames[i], new Object[0]);
            }
            return cipherSuites;
        }
        return Collections.emptyList();
    }

    private static List<ProtocolVersion> getAvailableProtocols(ProtocolVersion[] protocolCandidates) {
        List<ProtocolVersion> availableProtocols = Collections.emptyList();
        if (protocolCandidates != null && protocolCandidates.length != 0) {
            availableProtocols = new ArrayList<ProtocolVersion>(protocolCandidates.length);
            for (ProtocolVersion p : protocolCandidates) {
                if (!p.isAvailable) continue;
                availableProtocols.add(p);
            }
        }
        return availableProtocols;
    }

    public static final class DTLSContext
    extends CustomizedDTLSContext {
    }

    private static class CustomizedDTLSContext
    extends AbstractDTLSContext {
        private static final List<ProtocolVersion> clientDefaultProtocols;
        private static final List<ProtocolVersion> serverDefaultProtocols;
        private static final List<CipherSuite> clientDefaultCipherSuites;
        private static final List<CipherSuite> serverDefaultCipherSuites;
        private static final IllegalArgumentException reservedException;

        private static List<ProtocolVersion> customizedProtocols(boolean client, List<ProtocolVersion> customized) {
            ProtocolVersion[] candidates;
            ArrayList<ProtocolVersion> refactored = new ArrayList<ProtocolVersion>();
            for (ProtocolVersion pv : customized) {
                if (!pv.isDTLS) continue;
                refactored.add(pv);
            }
            if (refactored.isEmpty()) {
                candidates = new ProtocolVersion[]{ProtocolVersion.DTLS12, ProtocolVersion.DTLS10};
            } else {
                candidates = new ProtocolVersion[customized.size()];
                candidates = refactored.toArray(candidates);
            }
            return SSLContextImpl.getAvailableProtocols(candidates);
        }

        protected CustomizedDTLSContext() {
            if (reservedException != null) {
                throw reservedException;
            }
        }

        @Override
        List<ProtocolVersion> getClientDefaultProtocolVersions() {
            return clientDefaultProtocols;
        }

        @Override
        List<ProtocolVersion> getServerDefaultProtocolVersions() {
            return serverDefaultProtocols;
        }

        @Override
        List<CipherSuite> getClientDefaultCipherSuites() {
            return clientDefaultCipherSuites;
        }

        @Override
        List<CipherSuite> getServerDefaultCipherSuites() {
            return serverDefaultCipherSuites;
        }

        static {
            reservedException = CustomizedSSLProtocols.reservedException;
            if (reservedException == null) {
                clientDefaultProtocols = CustomizedDTLSContext.customizedProtocols(true, CustomizedSSLProtocols.customizedClientProtocols);
                serverDefaultProtocols = CustomizedDTLSContext.customizedProtocols(false, CustomizedSSLProtocols.customizedServerProtocols);
                clientDefaultCipherSuites = SSLContextImpl.getApplicableEnabledCipherSuites(clientDefaultProtocols, true);
                serverDefaultCipherSuites = SSLContextImpl.getApplicableEnabledCipherSuites(serverDefaultProtocols, false);
            } else {
                clientDefaultProtocols = null;
                serverDefaultProtocols = null;
                clientDefaultCipherSuites = null;
                serverDefaultCipherSuites = null;
            }
        }
    }

    public static final class DTLS12Context
    extends AbstractDTLSContext {
        private static final List<ProtocolVersion> clientDefaultProtocols = SSLContextImpl.getAvailableProtocols(new ProtocolVersion[]{ProtocolVersion.DTLS12, ProtocolVersion.DTLS10});
        private static final List<CipherSuite> clientDefaultCipherSuites = SSLContextImpl.getApplicableEnabledCipherSuites(clientDefaultProtocols, true);

        @Override
        List<ProtocolVersion> getClientDefaultProtocolVersions() {
            return clientDefaultProtocols;
        }

        @Override
        List<CipherSuite> getClientDefaultCipherSuites() {
            return clientDefaultCipherSuites;
        }
    }

    public static final class DTLS10Context
    extends AbstractDTLSContext {
        private static final List<ProtocolVersion> clientDefaultProtocols = SSLContextImpl.getAvailableProtocols(new ProtocolVersion[]{ProtocolVersion.DTLS10});
        private static final List<CipherSuite> clientDefaultCipherSuites = SSLContextImpl.getApplicableEnabledCipherSuites(clientDefaultProtocols, true);

        @Override
        List<ProtocolVersion> getClientDefaultProtocolVersions() {
            return clientDefaultProtocols;
        }

        @Override
        List<CipherSuite> getClientDefaultCipherSuites() {
            return clientDefaultCipherSuites;
        }
    }

    private static abstract class AbstractDTLSContext
    extends SSLContextImpl {
        private static final List<ProtocolVersion> supportedProtocols = Arrays.asList(ProtocolVersion.DTLS12, ProtocolVersion.DTLS10);
        private static final List<ProtocolVersion> serverDefaultProtocols = SSLContextImpl.getAvailableProtocols(new ProtocolVersion[]{ProtocolVersion.DTLS12, ProtocolVersion.DTLS10});
        private static final List<CipherSuite> supportedCipherSuites = SSLContextImpl.getApplicableSupportedCipherSuites(supportedProtocols);
        private static final List<CipherSuite> serverDefaultCipherSuites = SSLContextImpl.getApplicableEnabledCipherSuites(serverDefaultProtocols, false);

        private AbstractDTLSContext() {
        }

        @Override
        protected SSLParameters engineGetDefaultSSLParameters() {
            SSLEngine engine = this.createSSLEngineImpl();
            engine.setUseClientMode(true);
            return engine.getSSLParameters();
        }

        @Override
        protected SSLParameters engineGetSupportedSSLParameters() {
            SSLEngine engine = this.createSSLEngineImpl();
            SSLParameters params = new SSLParameters();
            params.setCipherSuites(engine.getSupportedCipherSuites());
            params.setProtocols(engine.getSupportedProtocols());
            return params;
        }

        @Override
        List<ProtocolVersion> getSupportedProtocolVersions() {
            return supportedProtocols;
        }

        @Override
        List<CipherSuite> getSupportedCipherSuites() {
            return supportedCipherSuites;
        }

        @Override
        List<ProtocolVersion> getServerDefaultProtocolVersions() {
            return serverDefaultProtocols;
        }

        @Override
        List<CipherSuite> getServerDefaultCipherSuites() {
            return serverDefaultCipherSuites;
        }

        @Override
        SSLEngine createSSLEngineImpl() {
            return new SSLEngineImpl(this);
        }

        @Override
        SSLEngine createSSLEngineImpl(String host, int port) {
            return new SSLEngineImpl(this, host, port);
        }

        @Override
        boolean isDTLS() {
            return true;
        }
    }

    public static final class DefaultSSLContext
    extends CustomizedTLSContext {
        public DefaultSSLContext() throws Exception {
            if (DefaultManagersHolder.reservedException != null) {
                throw DefaultManagersHolder.reservedException;
            }
            try {
                super.engineInit(DefaultManagersHolder.keyManagers, DefaultManagersHolder.trustManagers, null);
            }
            catch (Exception e) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,defaultctx")) {
                    SSLLogger.fine("default context init failed: ", e);
                }
                throw e;
            }
        }

        @Override
        protected void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom sr) throws KeyManagementException {
            throw new KeyManagementException("Default SSLContext is initialized automatically");
        }

        static SSLContextImpl getDefaultImpl() throws Exception {
            if (DefaultSSLContextHolder.reservedException != null) {
                throw DefaultSSLContextHolder.reservedException;
            }
            return DefaultSSLContextHolder.sslContext;
        }
    }

    private static final class DefaultSSLContextHolder {
        private static final SSLContextImpl sslContext;
        private static final Exception reservedException;

        private DefaultSSLContextHolder() {
        }

        static {
            DefaultSSLContext mediator;
            Exception reserved;
            block4: {
                reserved = null;
                mediator = null;
                if (DefaultManagersHolder.reservedException != null) {
                    reserved = DefaultManagersHolder.reservedException;
                } else {
                    try {
                        mediator = new DefaultSSLContext();
                    }
                    catch (Exception e) {
                        reserved = new KeyManagementException(e.getMessage());
                        if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,defaultctx")) break block4;
                        SSLLogger.warning("Failed to load default SSLContext", e);
                    }
                }
            }
            sslContext = mediator;
            reservedException = reserved;
        }
    }

    private static final class DefaultManagersHolder {
        private static final String NONE = "NONE";
        private static final String P11KEYSTORE = "PKCS11";
        private static final TrustManager[] trustManagers;
        private static final KeyManager[] keyManagers;
        private static final Exception reservedException;

        private DefaultManagersHolder() {
        }

        private static TrustManager[] getTrustManagers() throws Exception {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            if ("SunJSSE".equals(tmf.getProvider().getName())) {
                tmf.init((KeyStore)null);
            } else {
                KeyStore ks = TrustStoreManager.getTrustedKeyStore();
                tmf.init(ks);
            }
            return tmf.getTrustManagers();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private static KeyManager[] getKeyManagers() throws Exception {
            final HashMap props = new HashMap();
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>(){

                @Override
                public Object run() {
                    props.put("keyStore", System.getProperty("javax.net.ssl.keyStore", ""));
                    props.put("keyStoreType", System.getProperty("javax.net.ssl.keyStoreType", KeyStore.getDefaultType()));
                    props.put("keyStoreProvider", System.getProperty("javax.net.ssl.keyStoreProvider", ""));
                    props.put("keyStorePasswd", System.getProperty("javax.net.ssl.keyStorePassword", ""));
                    return null;
                }
            });
            final String defaultKeyStore = (String)props.get("keyStore");
            String defaultKeyStoreType = (String)props.get("keyStoreType");
            String defaultKeyStoreProvider = (String)props.get("keyStoreProvider");
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,defaultctx")) {
                SSLLogger.fine("keyStore is : " + defaultKeyStore, new Object[0]);
                SSLLogger.fine("keyStore type is : " + defaultKeyStoreType, new Object[0]);
                SSLLogger.fine("keyStore provider is : " + defaultKeyStoreProvider, new Object[0]);
            }
            if (P11KEYSTORE.equals(defaultKeyStoreType) && !NONE.equals(defaultKeyStore)) {
                throw new IllegalArgumentException("if keyStoreType is PKCS11, then keyStore must be NONE");
            }
            FileInputStream fs = null;
            KeyStore ks = null;
            char[] passwd = null;
            try {
                String defaultKeyStorePassword;
                if (!defaultKeyStore.isEmpty() && !NONE.equals(defaultKeyStore)) {
                    fs = AccessController.doPrivileged(new PrivilegedExceptionAction<FileInputStream>(){

                        @Override
                        public FileInputStream run() throws Exception {
                            return new FileInputStream(defaultKeyStore);
                        }
                    });
                }
                if (!(defaultKeyStorePassword = (String)props.get("keyStorePasswd")).isEmpty()) {
                    passwd = defaultKeyStorePassword.toCharArray();
                }
                if (defaultKeyStoreType.length() != 0) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,defaultctx")) {
                        SSLLogger.finest("init keystore", new Object[0]);
                    }
                    ks = defaultKeyStoreProvider.isEmpty() ? KeyStore.getInstance(defaultKeyStoreType) : KeyStore.getInstance(defaultKeyStoreType, defaultKeyStoreProvider);
                    ks.load(fs, passwd);
                }
            }
            finally {
                if (fs != null) {
                    fs.close();
                    fs = null;
                }
            }
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,defaultctx")) {
                SSLLogger.fine("init keymanager of type " + KeyManagerFactory.getDefaultAlgorithm(), new Object[0]);
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            if (P11KEYSTORE.equals(defaultKeyStoreType)) {
                kmf.init(ks, null);
            } else {
                kmf.init(ks, passwd);
            }
            return kmf.getKeyManagers();
        }

        static {
            KeyManager[] kmMediator;
            TrustManager[] tmMediator;
            Exception reserved;
            block8: {
                block7: {
                    reserved = null;
                    tmMediator = null;
                    try {
                        tmMediator = DefaultManagersHolder.getTrustManagers();
                    }
                    catch (Exception e) {
                        reserved = e;
                        if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,defaultctx")) break block7;
                        SSLLogger.warning("Failed to load default trust managers", e);
                    }
                }
                kmMediator = null;
                if (reserved == null) {
                    try {
                        kmMediator = DefaultManagersHolder.getKeyManagers();
                    }
                    catch (Exception e) {
                        reserved = e;
                        if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,defaultctx")) break block8;
                        SSLLogger.warning("Failed to load default key managers", e);
                    }
                }
            }
            if (reserved != null) {
                trustManagers = new TrustManager[0];
                keyManagers = new KeyManager[0];
                reservedException = new KeyManagementException(reserved.getMessage());
            } else {
                trustManagers = tmMediator;
                keyManagers = kmMediator;
                reservedException = null;
            }
        }
    }

    public static final class TLSContext
    extends CustomizedTLSContext {
    }

    private static class CustomizedTLSContext
    extends AbstractTLSContext {
        private static final List<ProtocolVersion> clientDefaultProtocols;
        private static final List<ProtocolVersion> serverDefaultProtocols;
        private static final List<CipherSuite> clientDefaultCipherSuites;
        private static final List<CipherSuite> serverDefaultCipherSuites;
        private static final IllegalArgumentException reservedException;

        private static List<ProtocolVersion> customizedProtocols(boolean client, List<ProtocolVersion> customized) {
            ArrayList<ProtocolVersion> refactored = new ArrayList<ProtocolVersion>();
            for (ProtocolVersion pv : customized) {
                if (pv.isDTLS) continue;
                refactored.add(pv);
            }
            ProtocolVersion[] candidates = refactored.isEmpty() ? new ProtocolVersion[]{ProtocolVersion.TLS13, ProtocolVersion.TLS12, ProtocolVersion.TLS11, ProtocolVersion.TLS10} : refactored.toArray(new ProtocolVersion[0]);
            return SSLContextImpl.getAvailableProtocols(candidates);
        }

        protected CustomizedTLSContext() {
            if (reservedException != null) {
                throw reservedException;
            }
        }

        @Override
        List<ProtocolVersion> getClientDefaultProtocolVersions() {
            return clientDefaultProtocols;
        }

        @Override
        List<ProtocolVersion> getServerDefaultProtocolVersions() {
            return serverDefaultProtocols;
        }

        @Override
        List<CipherSuite> getClientDefaultCipherSuites() {
            return clientDefaultCipherSuites;
        }

        @Override
        List<CipherSuite> getServerDefaultCipherSuites() {
            return serverDefaultCipherSuites;
        }

        static {
            reservedException = CustomizedSSLProtocols.reservedException;
            if (reservedException == null) {
                clientDefaultProtocols = CustomizedTLSContext.customizedProtocols(true, CustomizedSSLProtocols.customizedClientProtocols);
                serverDefaultProtocols = CustomizedTLSContext.customizedProtocols(false, CustomizedSSLProtocols.customizedServerProtocols);
                clientDefaultCipherSuites = SSLContextImpl.getApplicableEnabledCipherSuites(clientDefaultProtocols, true);
                serverDefaultCipherSuites = SSLContextImpl.getApplicableEnabledCipherSuites(serverDefaultProtocols, false);
            } else {
                clientDefaultProtocols = null;
                serverDefaultProtocols = null;
                clientDefaultCipherSuites = null;
                serverDefaultCipherSuites = null;
            }
        }
    }

    private static class CustomizedSSLProtocols {
        private static final String JDK_TLS_CLIENT_PROTOCOLS = "jdk.tls.client.protocols";
        private static final String JDK_TLS_SERVER_PROTOCOLS = "jdk.tls.server.protocols";
        static IllegalArgumentException reservedException = null;
        static final ArrayList<ProtocolVersion> customizedClientProtocols = new ArrayList();
        static final ArrayList<ProtocolVersion> customizedServerProtocols = new ArrayList();

        private CustomizedSSLProtocols() {
        }

        private static void populate(String propname, ArrayList<ProtocolVersion> arrayList) {
            String property = GetPropertyAction.privilegedGetProperty(propname);
            if (property == null) {
                return;
            }
            if (!property.isEmpty() && property.length() > 1 && property.charAt(0) == '\"' && property.charAt(property.length() - 1) == '\"') {
                property = property.substring(1, property.length() - 1);
            }
            if (!property.isEmpty()) {
                String[] protocols = property.split(",");
                for (int i = 0; i < protocols.length; ++i) {
                    protocols[i] = protocols[i].trim();
                    ProtocolVersion pv = ProtocolVersion.nameOf(protocols[i]);
                    if (pv == null) {
                        reservedException = new IllegalArgumentException(propname + ": " + protocols[i] + " is not a supported SSL protocol name");
                    }
                    if (arrayList.contains((Object)pv)) continue;
                    arrayList.add(pv);
                }
            }
        }

        static {
            CustomizedSSLProtocols.populate(JDK_TLS_CLIENT_PROTOCOLS, customizedClientProtocols);
            CustomizedSSLProtocols.populate(JDK_TLS_SERVER_PROTOCOLS, customizedServerProtocols);
        }
    }

    public static final class TLS13Context
    extends AbstractTLSContext {
        private static final List<ProtocolVersion> clientDefaultProtocols = SSLContextImpl.getAvailableProtocols(new ProtocolVersion[]{ProtocolVersion.TLS13, ProtocolVersion.TLS12, ProtocolVersion.TLS11, ProtocolVersion.TLS10});
        private static final List<CipherSuite> clientDefaultCipherSuites = SSLContextImpl.getApplicableEnabledCipherSuites(clientDefaultProtocols, true);

        @Override
        List<ProtocolVersion> getClientDefaultProtocolVersions() {
            return clientDefaultProtocols;
        }

        @Override
        List<CipherSuite> getClientDefaultCipherSuites() {
            return clientDefaultCipherSuites;
        }
    }

    public static final class TLS12Context
    extends AbstractTLSContext {
        private static final List<ProtocolVersion> clientDefaultProtocols = SSLContextImpl.getAvailableProtocols(new ProtocolVersion[]{ProtocolVersion.TLS12, ProtocolVersion.TLS11, ProtocolVersion.TLS10});
        private static final List<CipherSuite> clientDefaultCipherSuites = SSLContextImpl.getApplicableEnabledCipherSuites(clientDefaultProtocols, true);

        @Override
        List<ProtocolVersion> getClientDefaultProtocolVersions() {
            return clientDefaultProtocols;
        }

        @Override
        List<CipherSuite> getClientDefaultCipherSuites() {
            return clientDefaultCipherSuites;
        }
    }

    public static final class TLS11Context
    extends AbstractTLSContext {
        private static final List<ProtocolVersion> clientDefaultProtocols = SSLContextImpl.getAvailableProtocols(new ProtocolVersion[]{ProtocolVersion.TLS11, ProtocolVersion.TLS10});
        private static final List<CipherSuite> clientDefaultCipherSuites = SSLContextImpl.getApplicableEnabledCipherSuites(clientDefaultProtocols, true);

        @Override
        List<ProtocolVersion> getClientDefaultProtocolVersions() {
            return clientDefaultProtocols;
        }

        @Override
        List<CipherSuite> getClientDefaultCipherSuites() {
            return clientDefaultCipherSuites;
        }
    }

    public static final class TLS10Context
    extends AbstractTLSContext {
        private static final List<ProtocolVersion> clientDefaultProtocols = SSLContextImpl.getAvailableProtocols(new ProtocolVersion[]{ProtocolVersion.TLS10});
        private static final List<CipherSuite> clientDefaultCipherSuites = SSLContextImpl.getApplicableEnabledCipherSuites(clientDefaultProtocols, true);

        @Override
        List<ProtocolVersion> getClientDefaultProtocolVersions() {
            return clientDefaultProtocols;
        }

        @Override
        List<CipherSuite> getClientDefaultCipherSuites() {
            return clientDefaultCipherSuites;
        }
    }

    private static abstract class AbstractTLSContext
    extends SSLContextImpl {
        private static final List<ProtocolVersion> supportedProtocols = Arrays.asList(ProtocolVersion.TLS13, ProtocolVersion.TLS12, ProtocolVersion.TLS11, ProtocolVersion.TLS10, ProtocolVersion.SSL30, ProtocolVersion.SSL20Hello);
        private static final List<ProtocolVersion> serverDefaultProtocols = SSLContextImpl.getAvailableProtocols(new ProtocolVersion[]{ProtocolVersion.TLS13, ProtocolVersion.TLS12, ProtocolVersion.TLS11, ProtocolVersion.TLS10});
        private static final List<CipherSuite> supportedCipherSuites = SSLContextImpl.getApplicableSupportedCipherSuites(supportedProtocols);
        private static final List<CipherSuite> serverDefaultCipherSuites = SSLContextImpl.getApplicableEnabledCipherSuites(serverDefaultProtocols, false);

        private AbstractTLSContext() {
        }

        @Override
        List<ProtocolVersion> getSupportedProtocolVersions() {
            return supportedProtocols;
        }

        @Override
        List<CipherSuite> getSupportedCipherSuites() {
            return supportedCipherSuites;
        }

        @Override
        List<ProtocolVersion> getServerDefaultProtocolVersions() {
            return serverDefaultProtocols;
        }

        @Override
        List<CipherSuite> getServerDefaultCipherSuites() {
            return serverDefaultCipherSuites;
        }

        @Override
        SSLEngine createSSLEngineImpl() {
            return new SSLEngineImpl(this);
        }

        @Override
        SSLEngine createSSLEngineImpl(String host, int port) {
            return new SSLEngineImpl(this, host, port);
        }

        @Override
        boolean isDTLS() {
            return false;
        }
    }
}

