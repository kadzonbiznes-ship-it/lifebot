/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AlgorithmConstraints;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import javax.crypto.KeyGenerator;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import sun.security.action.GetIntegerAction;
import sun.security.action.GetPropertyAction;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.ClientAuthType;
import sun.security.ssl.NamedGroup;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.SSLAlgorithmConstraints;
import sun.security.ssl.SSLContextImpl;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SignatureScheme;
import sun.security.ssl.Utilities;

final class SSLConfiguration
implements Cloneable {
    AlgorithmConstraints userSpecifiedAlgorithmConstraints = SSLAlgorithmConstraints.DEFAULT;
    List<ProtocolVersion> enabledProtocols;
    List<CipherSuite> enabledCipherSuites;
    ClientAuthType clientAuthType;
    String identificationProtocol;
    List<SNIServerName> serverNames;
    Collection<SNIMatcher> sniMatchers;
    String[] applicationProtocols;
    boolean preferLocalCipherSuites;
    boolean enableRetransmissions;
    int maximumPacketSize;
    String[] signatureSchemes;
    String[] namedGroups;
    ProtocolVersion maximumProtocolVersion;
    boolean isClientMode;
    boolean enableSessionCreation;
    BiFunction<SSLSocket, List<String>, String> socketAPSelector;
    BiFunction<SSLEngine, List<String>, String> engineAPSelector;
    HashMap<HandshakeCompletedListener, AccessControlContext> handshakeListeners;
    boolean noSniExtension;
    boolean noSniMatcher;
    static final boolean useExtendedMasterSecret;
    static final boolean allowLegacyResumption;
    static final boolean allowLegacyMasterSecret;
    static final boolean useCompatibilityMode;
    static final boolean acknowledgeCloseNotify;
    static final int maxHandshakeMessageSize;
    static final int maxCertificateChainLength;
    static final boolean enableFFDHE;
    static final boolean enableDtlsResumeCookie;

    SSLConfiguration(SSLContextImpl sslContext, boolean isClientMode) {
        this.enabledProtocols = sslContext.getDefaultProtocolVersions(!isClientMode);
        this.enabledCipherSuites = sslContext.getDefaultCipherSuites(!isClientMode);
        this.clientAuthType = ClientAuthType.CLIENT_AUTH_NONE;
        this.identificationProtocol = null;
        this.serverNames = Collections.emptyList();
        this.sniMatchers = Collections.emptyList();
        this.preferLocalCipherSuites = true;
        this.applicationProtocols = new String[0];
        this.enableRetransmissions = sslContext.isDTLS();
        this.maximumPacketSize = 0;
        this.signatureSchemes = isClientMode ? CustomizedClientSignatureSchemes.signatureSchemes : CustomizedServerSignatureSchemes.signatureSchemes;
        this.namedGroups = NamedGroup.SupportedGroups.namedGroups;
        this.maximumProtocolVersion = ProtocolVersion.NONE;
        for (ProtocolVersion pv : this.enabledProtocols) {
            if (pv.compareTo(this.maximumProtocolVersion) <= 0) continue;
            this.maximumProtocolVersion = pv;
        }
        this.isClientMode = isClientMode;
        this.enableSessionCreation = true;
        this.socketAPSelector = null;
        this.engineAPSelector = null;
        this.handshakeListeners = null;
        this.noSniExtension = false;
        this.noSniMatcher = false;
    }

    SSLParameters getSSLParameters() {
        SSLParameters params = new SSLParameters();
        params.setAlgorithmConstraints(this.userSpecifiedAlgorithmConstraints);
        params.setProtocols(ProtocolVersion.toStringArray(this.enabledProtocols));
        params.setCipherSuites(CipherSuite.namesOf(this.enabledCipherSuites));
        switch (this.clientAuthType) {
            case CLIENT_AUTH_REQUIRED: {
                params.setNeedClientAuth(true);
                break;
            }
            case CLIENT_AUTH_REQUESTED: {
                params.setWantClientAuth(true);
                break;
            }
            default: {
                params.setWantClientAuth(false);
            }
        }
        params.setEndpointIdentificationAlgorithm(this.identificationProtocol);
        if (this.serverNames.isEmpty() && !this.noSniExtension) {
            params.setServerNames(null);
        } else {
            params.setServerNames(this.serverNames);
        }
        if (this.sniMatchers.isEmpty() && !this.noSniMatcher) {
            params.setSNIMatchers(null);
        } else {
            params.setSNIMatchers(this.sniMatchers);
        }
        params.setApplicationProtocols(this.applicationProtocols);
        params.setUseCipherSuitesOrder(this.preferLocalCipherSuites);
        params.setEnableRetransmissions(this.enableRetransmissions);
        params.setMaximumPacketSize(this.maximumPacketSize);
        params.setSignatureSchemes(this.signatureSchemes);
        params.setNamedGroups(this.namedGroups);
        return params;
    }

    void setSSLParameters(SSLParameters params) {
        String[] ngs;
        String[] ss;
        Collection<SNIMatcher> matchers;
        List<SNIServerName> sniNames;
        String[] sa;
        AlgorithmConstraints ac = params.getAlgorithmConstraints();
        if (ac != null) {
            this.userSpecifiedAlgorithmConstraints = ac;
        }
        if ((sa = params.getCipherSuites()) != null) {
            this.enabledCipherSuites = CipherSuite.validValuesOf(sa);
        }
        if ((sa = params.getProtocols()) != null) {
            this.enabledProtocols = ProtocolVersion.namesOf(sa);
            this.maximumProtocolVersion = ProtocolVersion.NONE;
            for (ProtocolVersion pv : this.enabledProtocols) {
                if (pv.compareTo(this.maximumProtocolVersion) <= 0) continue;
                this.maximumProtocolVersion = pv;
            }
        }
        this.clientAuthType = params.getNeedClientAuth() ? ClientAuthType.CLIENT_AUTH_REQUIRED : (params.getWantClientAuth() ? ClientAuthType.CLIENT_AUTH_REQUESTED : ClientAuthType.CLIENT_AUTH_NONE);
        String s = params.getEndpointIdentificationAlgorithm();
        if (s != null) {
            this.identificationProtocol = s;
        }
        if ((sniNames = params.getServerNames()) != null) {
            this.noSniExtension = sniNames.isEmpty();
            this.serverNames = sniNames;
        }
        if ((matchers = params.getSNIMatchers()) != null) {
            this.noSniMatcher = matchers.isEmpty();
            this.sniMatchers = matchers;
        }
        if ((sa = params.getApplicationProtocols()) != null) {
            this.applicationProtocols = sa;
        }
        if ((ss = params.getSignatureSchemes()) != null) {
            this.signatureSchemes = ss;
        }
        this.namedGroups = (ngs = params.getNamedGroups()) != null ? ngs : NamedGroup.SupportedGroups.namedGroups;
        this.preferLocalCipherSuites = params.getUseCipherSuitesOrder();
        this.enableRetransmissions = params.getEnableRetransmissions();
        this.maximumPacketSize = params.getMaximumPacketSize();
    }

    void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
        if (this.handshakeListeners == null) {
            this.handshakeListeners = new HashMap(4);
        }
        this.handshakeListeners.put(listener, AccessController.getContext());
    }

    void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
        if (this.handshakeListeners == null) {
            throw new IllegalArgumentException("no listeners");
        }
        if (this.handshakeListeners.remove(listener) == null) {
            throw new IllegalArgumentException("listener not registered");
        }
        if (this.handshakeListeners.isEmpty()) {
            this.handshakeListeners = null;
        }
    }

    boolean isAvailable(SSLExtension extension) {
        for (ProtocolVersion protocolVersion : this.enabledProtocols) {
            if (!extension.isAvailable(protocolVersion) || !(this.isClientMode ? SSLExtension.ClientExtensions.defaults.contains(extension) : SSLExtension.ServerExtensions.defaults.contains(extension))) continue;
            return true;
        }
        return false;
    }

    boolean isAvailable(SSLExtension extension, ProtocolVersion protocolVersion) {
        return extension.isAvailable(protocolVersion) && (this.isClientMode ? SSLExtension.ClientExtensions.defaults.contains(extension) : SSLExtension.ServerExtensions.defaults.contains(extension));
    }

    SSLExtension[] getEnabledExtensions(SSLHandshake handshakeType) {
        ArrayList<SSLExtension> extensions = new ArrayList<SSLExtension>();
        for (SSLExtension extension : SSLExtension.values()) {
            if (extension.handshakeType != handshakeType || !this.isAvailable(extension)) continue;
            extensions.add(extension);
        }
        return extensions.toArray(new SSLExtension[0]);
    }

    SSLExtension[] getExclusiveExtensions(SSLHandshake handshakeType, List<SSLExtension> excluded) {
        ArrayList<SSLExtension> extensions = new ArrayList<SSLExtension>();
        for (SSLExtension extension : SSLExtension.values()) {
            if (extension.handshakeType != handshakeType || !this.isAvailable(extension) || excluded.contains(extension)) continue;
            extensions.add(extension);
        }
        return extensions.toArray(new SSLExtension[0]);
    }

    SSLExtension[] getEnabledExtensions(SSLHandshake handshakeType, ProtocolVersion protocolVersion) {
        return this.getEnabledExtensions(handshakeType, List.of(protocolVersion));
    }

    SSLExtension[] getEnabledExtensions(SSLHandshake handshakeType, List<ProtocolVersion> activeProtocols) {
        ArrayList<SSLExtension> extensions = new ArrayList<SSLExtension>();
        block0: for (SSLExtension extension : SSLExtension.values()) {
            if (extension.handshakeType != handshakeType || !this.isAvailable(extension)) continue;
            for (ProtocolVersion protocolVersion : activeProtocols) {
                if (!extension.isAvailable(protocolVersion)) continue;
                extensions.add(extension);
                continue block0;
            }
        }
        return extensions.toArray(new SSLExtension[0]);
    }

    void toggleClientMode() {
        this.isClientMode ^= true;
        if (Arrays.equals(this.signatureSchemes, CustomizedClientSignatureSchemes.signatureSchemes) || Arrays.equals(this.signatureSchemes, CustomizedServerSignatureSchemes.signatureSchemes)) {
            this.signatureSchemes = this.isClientMode ? CustomizedClientSignatureSchemes.signatureSchemes : CustomizedServerSignatureSchemes.signatureSchemes;
        }
    }

    public Object clone() {
        try {
            SSLConfiguration config = (SSLConfiguration)super.clone();
            if (this.handshakeListeners != null) {
                config.handshakeListeners = (HashMap)this.handshakeListeners.clone();
            }
            return config;
        }
        catch (CloneNotSupportedException cloneNotSupportedException) {
            return null;
        }
    }

    private static String[] getCustomizedSignatureScheme(String propertyName) {
        String property = GetPropertyAction.privilegedGetProperty(propertyName);
        if (SSLLogger.isOn && SSLLogger.isOn("ssl,sslctx")) {
            SSLLogger.fine("System property " + propertyName + " is set to '" + property + "'", new Object[0]);
        }
        if (property != null && !property.isEmpty() && property.length() > 1 && property.charAt(0) == '\"' && property.charAt(property.length() - 1) == '\"') {
            property = property.substring(1, property.length() - 1);
        }
        if (property != null && !property.isEmpty()) {
            String[] signatureSchemeNames = property.split(",");
            ArrayList<String> signatureSchemes = new ArrayList<String>(signatureSchemeNames.length);
            for (String schemeName : signatureSchemeNames) {
                if ((schemeName = schemeName.trim()).isEmpty()) continue;
                SignatureScheme scheme = SignatureScheme.nameOf(schemeName);
                if (scheme != null && scheme.isAvailable) {
                    signatureSchemes.add(schemeName);
                    continue;
                }
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,sslctx")) continue;
                SSLLogger.fine("The current installed providers do not support signature scheme: " + schemeName, new Object[0]);
            }
            if (!signatureSchemes.isEmpty()) {
                return signatureSchemes.toArray(new String[0]);
            }
        }
        return null;
    }

    static {
        allowLegacyResumption = Utilities.getBooleanProperty("jdk.tls.allowLegacyResumption", true);
        allowLegacyMasterSecret = Utilities.getBooleanProperty("jdk.tls.allowLegacyMasterSecret", true);
        useCompatibilityMode = Utilities.getBooleanProperty("jdk.tls.client.useCompatibilityMode", true);
        acknowledgeCloseNotify = Utilities.getBooleanProperty("jdk.tls.acknowledgeCloseNotify", false);
        maxHandshakeMessageSize = GetIntegerAction.privilegedGetProperty("jdk.tls.maxHandshakeMessageSize", 32768);
        maxCertificateChainLength = GetIntegerAction.privilegedGetProperty("jdk.tls.maxCertificateChainLength", 10);
        enableFFDHE = Utilities.getBooleanProperty("jsse.enableFFDHE", true);
        enableDtlsResumeCookie = Utilities.getBooleanProperty("jdk.tls.enableDtlsResumeCookie", true);
        boolean supportExtendedMasterSecret = Utilities.getBooleanProperty("jdk.tls.useExtendedMasterSecret", true);
        if (supportExtendedMasterSecret) {
            try {
                KeyGenerator.getInstance("SunTlsExtendedMasterSecret");
            }
            catch (NoSuchAlgorithmException nae) {
                supportExtendedMasterSecret = false;
            }
        }
        useExtendedMasterSecret = supportExtendedMasterSecret;
    }

    private static final class CustomizedClientSignatureSchemes {
        private static final String[] signatureSchemes = SSLConfiguration.getCustomizedSignatureScheme("jdk.tls.client.SignatureSchemes");

        private CustomizedClientSignatureSchemes() {
        }
    }

    private static final class CustomizedServerSignatureSchemes {
        private static final String[] signatureSchemes = SSLConfiguration.getCustomizedSignatureScheme("jdk.tls.server.SignatureSchemes");

        private CustomizedServerSignatureSchemes() {
        }
    }
}

