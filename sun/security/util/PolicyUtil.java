/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import sun.net.www.ParseUtil;
import sun.security.util.Debug;
import sun.security.util.Password;

public class PolicyUtil {
    private static final String P11KEYSTORE = "PKCS11";
    private static final String NONE = "NONE";

    public static InputStream getInputStream(URL url) throws IOException {
        if ("file".equals(url.getProtocol())) {
            String path = url.getFile().replace('/', File.separatorChar);
            path = ParseUtil.decode(path);
            return new FileInputStream(path);
        }
        return url.openStream();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static KeyStore getKeyStore(URL policyUrl, String keyStoreName, String keyStoreType, String keyStoreProvider, String storePassURL, Debug debug) throws KeyStoreException, IOException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException {
        if (keyStoreName == null) {
            throw new IllegalArgumentException("null KeyStore name");
        }
        char[] keyStorePassword = null;
        try {
            Object in;
            URL uRL;
            if (keyStoreType == null) {
                keyStoreType = KeyStore.getDefaultType();
            }
            if (P11KEYSTORE.equalsIgnoreCase(keyStoreType) && !NONE.equals(keyStoreName)) {
                throw new IllegalArgumentException("Invalid value (" + keyStoreName + ") for keystore URL.  If the keystore type is \"" + P11KEYSTORE + "\", the keystore url must be \"" + NONE + "\"");
            }
            KeyStore ks = keyStoreProvider != null ? KeyStore.getInstance(keyStoreType, keyStoreProvider) : KeyStore.getInstance(keyStoreType);
            if (storePassURL != null) {
                URL passURL;
                try {
                    URL uRL2 = passURL = new URL(storePassURL);
                }
                catch (MalformedURLException e) {
                    if (policyUrl == null) {
                        throw e;
                    }
                    uRL = passURL = new URL(policyUrl, storePassURL);
                }
                if (debug != null) {
                    debug.println("reading password" + passURL);
                }
                in = passURL.openStream();
                try {
                    keyStorePassword = Password.readPassword((InputStream)in);
                }
                finally {
                    if (in != null) {
                        ((InputStream)in).close();
                    }
                }
            }
            if (NONE.equals(keyStoreName)) {
                ks.load(null, keyStorePassword);
            } else {
                URL keyStoreUrl;
                try {
                    keyStoreUrl = new URL(keyStoreName);
                    in = keyStoreUrl;
                }
                catch (MalformedURLException e) {
                    if (policyUrl == null) {
                        throw e;
                    }
                    uRL = keyStoreUrl = new URL(policyUrl, keyStoreName);
                }
                if (debug != null) {
                    debug.println("reading keystore" + keyStoreUrl);
                }
                try (BufferedInputStream inStream = new BufferedInputStream(PolicyUtil.getInputStream(keyStoreUrl));){
                    ks.load(inStream, keyStorePassword);
                }
            }
            KeyStore keyStore = ks;
            return keyStore;
        }
        finally {
            if (keyStorePassword != null) {
                Arrays.fill(keyStorePassword, ' ');
            }
        }
    }
}

