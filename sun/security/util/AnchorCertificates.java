/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivilegedAction;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.util.FilePaths;
import sun.security.x509.X509CertImpl;

public class AnchorCertificates {
    private static final Debug debug = Debug.getInstance("certpath");
    private static final String HASH = "SHA-256";
    private static Set<String> certs = Collections.emptySet();
    private static Set<X500Principal> certIssuers = Collections.emptySet();

    public static boolean contains(X509Certificate cert) {
        boolean result;
        String key = X509CertImpl.getFingerprint(HASH, cert, debug);
        boolean bl = result = key != null && certs.contains(key);
        if (result && debug != null) {
            debug.println("AnchorCertificate.contains: matched " + cert.getSubjectX500Principal());
        }
        return result;
    }

    public static boolean issuerOf(X509Certificate cert) {
        return certIssuers.contains(cert.getIssuerX500Principal());
    }

    private AnchorCertificates() {
    }

    static {
        Object object = AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Void run() {
                block8: {
                    File f = new File(FilePaths.cacerts());
                    try {
                        KeyStore cacerts = KeyStore.getInstance("JKS");
                        try (FileInputStream fis = new FileInputStream(f);){
                            cacerts.load(fis, null);
                            certs = new HashSet<String>();
                            certIssuers = new HashSet<X500Principal>();
                            Enumeration<String> list = cacerts.aliases();
                            while (list.hasMoreElements()) {
                                X509Certificate cert;
                                String fp;
                                String alias = list.nextElement();
                                if (!alias.contains(" [jdk") || (fp = X509CertImpl.getFingerprint(AnchorCertificates.HASH, cert = (X509Certificate)cacerts.getCertificate(alias), debug)) == null) continue;
                                certs.add(fp);
                                certIssuers.add(cert.getSubjectX500Principal());
                            }
                        }
                    }
                    catch (Exception e) {
                        if (debug == null) break block8;
                        debug.println("Error parsing cacerts");
                        e.printStackTrace();
                    }
                }
                return null;
            }
        });
    }
}

