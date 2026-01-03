/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.net.Socket;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.security.auth.x500.X500Principal;
import sun.security.ssl.SSLLogger;

final class SunX509KeyManagerImpl
extends X509ExtendedKeyManager {
    private static final String[] STRING0 = new String[0];
    private final Map<String, X509Credentials> credentialsMap = new HashMap<String, X509Credentials>();
    private final Map<String, String[]> serverAliasCache = Collections.synchronizedMap(new HashMap());

    SunX509KeyManagerImpl(KeyStore ks, char[] password) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        if (ks == null) {
            return;
        }
        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            Certificate[] certs;
            Key key;
            String alias = aliases.nextElement();
            if (!ks.isKeyEntry(alias) || !((key = ks.getKey(alias, password)) instanceof PrivateKey) || (certs = ks.getCertificateChain(alias)) == null || certs.length == 0 || !(certs[0] instanceof X509Certificate)) continue;
            if (!(certs instanceof X509Certificate[])) {
                X509Certificate[] tmp = new X509Certificate[certs.length];
                System.arraycopy(certs, 0, tmp, 0, certs.length);
                certs = tmp;
            }
            X509Credentials cred = new X509Credentials((PrivateKey)key, (X509Certificate[])certs);
            this.credentialsMap.put(alias, cred);
            if (!SSLLogger.isOn || !SSLLogger.isOn("keymanager")) continue;
            SSLLogger.fine("found key for : " + alias, certs);
        }
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        if (alias == null) {
            return null;
        }
        X509Credentials cred = this.credentialsMap.get(alias);
        if (cred == null) {
            return null;
        }
        return (X509Certificate[])cred.certificates.clone();
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        if (alias == null) {
            return null;
        }
        X509Credentials cred = this.credentialsMap.get(alias);
        if (cred == null) {
            return null;
        }
        return cred.privateKey;
    }

    @Override
    public String chooseClientAlias(String[] keyTypes, Principal[] issuers, Socket socket) {
        if (keyTypes == null) {
            return null;
        }
        for (int i = 0; i < keyTypes.length; ++i) {
            String[] aliases = this.getClientAliases(keyTypes[i], issuers);
            if (aliases == null || aliases.length <= 0) continue;
            return aliases[0];
        }
        return null;
    }

    @Override
    public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
        return this.chooseClientAlias(keyType, issuers, null);
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        String[] aliases;
        if (keyType == null) {
            return null;
        }
        if (issuers == null || issuers.length == 0) {
            aliases = this.serverAliasCache.get(keyType);
            if (aliases == null) {
                aliases = this.getServerAliases(keyType, issuers);
                if (aliases == null) {
                    aliases = STRING0;
                }
                this.serverAliasCache.put(keyType, aliases);
            }
        } else {
            aliases = this.getServerAliases(keyType, issuers);
        }
        if (aliases != null && aliases.length > 0) {
            return aliases[0];
        }
        return null;
    }

    @Override
    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        return this.chooseServerAlias(keyType, issuers, null);
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return this.getAliases(keyType, issuers);
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return this.getAliases(keyType, issuers);
    }

    private String[] getAliases(String keyType, Principal[] issuers) {
        String sigType;
        if (keyType == null) {
            return null;
        }
        if (issuers == null) {
            issuers = new X500Principal[]{};
        }
        if (!(issuers instanceof X500Principal[])) {
            issuers = SunX509KeyManagerImpl.convertPrincipals(issuers);
        }
        if (keyType.contains("_")) {
            int k = keyType.indexOf(95);
            sigType = keyType.substring(k + 1);
            keyType = keyType.substring(0, k);
        } else {
            sigType = null;
        }
        X500Principal[] x500Issuers = (X500Principal[])issuers;
        ArrayList<String> aliases = new ArrayList<String>();
        block0: for (Map.Entry<String, X509Credentials> entry : this.credentialsMap.entrySet()) {
            String pattern;
            String sigAlgName;
            String alias = entry.getKey();
            X509Credentials credentials = entry.getValue();
            X509Certificate[] certs = credentials.certificates;
            if (!keyType.equals(certs[0].getPublicKey().getAlgorithm()) || sigType != null && (certs.length <= 1 ? !(sigAlgName = certs[0].getSigAlgName().toUpperCase(Locale.ENGLISH)).contains(pattern = "WITH" + sigType.toUpperCase(Locale.ENGLISH)) : !sigType.equals(certs[1].getPublicKey().getAlgorithm()))) continue;
            if (issuers.length == 0) {
                aliases.add(alias);
                if (!SSLLogger.isOn || !SSLLogger.isOn("keymanager")) continue;
                SSLLogger.fine("matching alias: " + alias, new Object[0]);
                continue;
            }
            Set<X500Principal> certIssuers = credentials.getIssuerX500Principals();
            for (int i = 0; i < x500Issuers.length; ++i) {
                if (!certIssuers.contains(issuers[i])) continue;
                aliases.add(alias);
                if (!SSLLogger.isOn || !SSLLogger.isOn("keymanager")) continue block0;
                SSLLogger.fine("matching alias: " + alias, new Object[0]);
                continue block0;
            }
        }
        String[] aliasStrings = aliases.toArray(STRING0);
        return aliasStrings.length == 0 ? null : aliasStrings;
    }

    private static X500Principal[] convertPrincipals(Principal[] principals) {
        ArrayList<X500Principal> list = new ArrayList<X500Principal>(principals.length);
        for (int i = 0; i < principals.length; ++i) {
            Principal p = principals[i];
            if (p instanceof X500Principal) {
                list.add((X500Principal)p);
                continue;
            }
            try {
                list.add(new X500Principal(p.getName()));
                continue;
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        return list.toArray(new X500Principal[0]);
    }

    private static class X509Credentials {
        final PrivateKey privateKey;
        final X509Certificate[] certificates;
        private final Set<X500Principal> issuerX500Principals;

        X509Credentials(PrivateKey privateKey, X509Certificate[] certificates) {
            this.privateKey = privateKey;
            this.certificates = certificates;
            this.issuerX500Principals = HashSet.newHashSet(certificates.length);
            for (X509Certificate certificate : certificates) {
                this.issuerX500Principals.add(certificate.getIssuerX500Principal());
            }
        }

        Set<X500Principal> getIssuerX500Principals() {
            return this.issuerX500Principals;
        }
    }
}

