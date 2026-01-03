/*
 * Decompiled with CFR 0.152.
 */
package sun.security.validator;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public final class TrustStoreUtil {
    private TrustStoreUtil() {
    }

    public static Set<X509Certificate> getTrustedCerts(KeyStore ks) {
        HashSet<X509Certificate> set = new HashSet<X509Certificate>();
        try {
            Enumeration<String> e = ks.aliases();
            while (e.hasMoreElements()) {
                Certificate[] certs;
                String alias = e.nextElement();
                if (ks.isCertificateEntry(alias)) {
                    Certificate cert = ks.getCertificate(alias);
                    if (!(cert instanceof X509Certificate)) continue;
                    set.add((X509Certificate)cert);
                    continue;
                }
                if (!ks.isKeyEntry(alias) || (certs = ks.getCertificateChain(alias)) == null || certs.length <= 0 || !(certs[0] instanceof X509Certificate)) continue;
                set.add((X509Certificate)certs[0]);
            }
        }
        catch (KeyStoreException keyStoreException) {
            // empty catch block
        }
        return Collections.unmodifiableSet(set);
    }
}

