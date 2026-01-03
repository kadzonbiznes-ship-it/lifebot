/*
 * Decompiled with CFR 0.152.
 */
package javax.net.ssl;

import java.security.AlgorithmConstraints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SNIServerName;

public class SSLParameters {
    private String[] cipherSuites;
    private String[] protocols;
    private boolean wantClientAuth;
    private boolean needClientAuth;
    private String identificationAlgorithm;
    private AlgorithmConstraints algorithmConstraints;
    private List<SNIServerName> sniNames = null;
    private Collection<SNIMatcher> sniMatchers = null;
    private boolean preferLocalCipherSuites;
    private boolean enableRetransmissions = true;
    private int maximumPacketSize = 0;
    private String[] applicationProtocols = new String[0];
    private String[] signatureSchemes = null;
    private String[] namedGroups = null;

    public SSLParameters() {
    }

    public SSLParameters(String[] cipherSuites) {
        this.setCipherSuites(cipherSuites);
    }

    public SSLParameters(String[] cipherSuites, String[] protocols) {
        this.setCipherSuites(cipherSuites);
        this.setProtocols(protocols);
    }

    private static String[] clone(String[] s) {
        return s == null ? null : (String[])s.clone();
    }

    public String[] getCipherSuites() {
        return SSLParameters.clone(this.cipherSuites);
    }

    public void setCipherSuites(String[] cipherSuites) {
        this.cipherSuites = SSLParameters.clone(cipherSuites);
    }

    public String[] getProtocols() {
        return SSLParameters.clone(this.protocols);
    }

    public void setProtocols(String[] protocols) {
        this.protocols = SSLParameters.clone(protocols);
    }

    public boolean getWantClientAuth() {
        return this.wantClientAuth;
    }

    public void setWantClientAuth(boolean wantClientAuth) {
        this.wantClientAuth = wantClientAuth;
        this.needClientAuth = false;
    }

    public boolean getNeedClientAuth() {
        return this.needClientAuth;
    }

    public void setNeedClientAuth(boolean needClientAuth) {
        this.wantClientAuth = false;
        this.needClientAuth = needClientAuth;
    }

    public AlgorithmConstraints getAlgorithmConstraints() {
        return this.algorithmConstraints;
    }

    public void setAlgorithmConstraints(AlgorithmConstraints constraints) {
        this.algorithmConstraints = constraints;
    }

    public String getEndpointIdentificationAlgorithm() {
        return this.identificationAlgorithm;
    }

    public void setEndpointIdentificationAlgorithm(String algorithm) {
        this.identificationAlgorithm = algorithm;
    }

    public final void setServerNames(List<SNIServerName> serverNames) {
        if (this.sniNames == serverNames) {
            return;
        }
        if (serverNames == null) {
            this.sniNames = null;
        } else if (serverNames.isEmpty()) {
            this.sniNames = Collections.emptyList();
        } else {
            ArrayList<Integer> sniTypes = new ArrayList<Integer>(serverNames.size());
            ArrayList<SNIServerName> sniValues = new ArrayList<SNIServerName>(serverNames.size());
            for (SNIServerName serverName : serverNames) {
                if (sniTypes.contains(serverName.getType())) {
                    throw new IllegalArgumentException("Duplicated server name of type " + serverName.getType());
                }
                sniTypes.add(serverName.getType());
                sniValues.add(serverName);
            }
            this.sniNames = Collections.unmodifiableList(sniValues);
        }
    }

    public final List<SNIServerName> getServerNames() {
        return this.sniNames;
    }

    public final void setSNIMatchers(Collection<SNIMatcher> matchers) {
        if (this.sniMatchers == matchers) {
            return;
        }
        if (matchers == null) {
            this.sniMatchers = null;
        } else if (matchers.isEmpty()) {
            this.sniMatchers = Collections.emptyList();
        } else {
            ArrayList<Integer> matcherTypes = new ArrayList<Integer>(matchers.size());
            ArrayList<SNIMatcher> matcherValues = new ArrayList<SNIMatcher>(matchers.size());
            for (SNIMatcher matcher : matchers) {
                if (matcherTypes.contains(matcher.getType())) {
                    throw new IllegalArgumentException("Duplicated server name of type " + matcher.getType());
                }
                matcherTypes.add(matcher.getType());
                matcherValues.add(matcher);
            }
            this.sniMatchers = Collections.unmodifiableList(matcherValues);
        }
    }

    public final Collection<SNIMatcher> getSNIMatchers() {
        return this.sniMatchers;
    }

    public final void setUseCipherSuitesOrder(boolean honorOrder) {
        this.preferLocalCipherSuites = honorOrder;
    }

    public final boolean getUseCipherSuitesOrder() {
        return this.preferLocalCipherSuites;
    }

    public void setEnableRetransmissions(boolean enableRetransmissions) {
        this.enableRetransmissions = enableRetransmissions;
    }

    public boolean getEnableRetransmissions() {
        return this.enableRetransmissions;
    }

    public void setMaximumPacketSize(int maximumPacketSize) {
        if (maximumPacketSize < 0) {
            throw new IllegalArgumentException("The maximum packet size cannot be negative");
        }
        this.maximumPacketSize = maximumPacketSize;
    }

    public int getMaximumPacketSize() {
        return this.maximumPacketSize;
    }

    public String[] getApplicationProtocols() {
        return (String[])this.applicationProtocols.clone();
    }

    public void setApplicationProtocols(String[] protocols) {
        String[] tempProtocols;
        if (protocols == null) {
            throw new IllegalArgumentException("protocols was null");
        }
        for (String p : tempProtocols = (String[])protocols.clone()) {
            if (p != null && !p.isEmpty()) continue;
            throw new IllegalArgumentException("An element of protocols was null/empty");
        }
        this.applicationProtocols = tempProtocols;
    }

    public String[] getSignatureSchemes() {
        return SSLParameters.clone(this.signatureSchemes);
    }

    public void setSignatureSchemes(String[] signatureSchemes) {
        String[] tempSchemes = null;
        if (signatureSchemes != null) {
            for (String scheme : tempSchemes = (String[])signatureSchemes.clone()) {
                if (scheme != null && !scheme.isBlank()) continue;
                throw new IllegalArgumentException("An element of signatureSchemes is null or blank");
            }
        }
        this.signatureSchemes = tempSchemes;
    }

    public String[] getNamedGroups() {
        return SSLParameters.clone(this.namedGroups);
    }

    public void setNamedGroups(String[] namedGroups) {
        String[] tempGroups = null;
        if (namedGroups != null) {
            tempGroups = (String[])namedGroups.clone();
            HashSet<String> groupsSet = new HashSet<String>();
            for (String namedGroup : tempGroups) {
                if (namedGroup == null || namedGroup.isBlank()) {
                    throw new IllegalArgumentException("An element of namedGroups is null or blank");
                }
                if (groupsSet.contains(namedGroup)) {
                    throw new IllegalArgumentException("Duplicate element of namedGroups: " + namedGroup);
                }
                groupsSet.add(namedGroup);
            }
        }
        this.namedGroups = tempGroups;
    }
}

