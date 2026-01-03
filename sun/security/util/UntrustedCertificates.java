/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.cert.X509Certificate;
import java.util.Properties;
import jdk.internal.util.StaticProperty;
import sun.security.util.Debug;
import sun.security.x509.X509CertImpl;

public final class UntrustedCertificates {
    private static final Debug debug = Debug.getInstance("certpath");
    private static final String ALGORITHM_KEY = "Algorithm";
    private static final Properties props = new Properties();
    private static final String algorithm;

    public static boolean isUntrusted(X509Certificate cert) {
        if (algorithm == null) {
            return false;
        }
        String key = X509CertImpl.getFingerprint(algorithm, cert, debug);
        return key == null || props.containsKey(key);
    }

    private UntrustedCertificates() {
    }

    static {
        Void dummy = AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                block7: {
                    File f = new File(StaticProperty.javaHome(), "lib/security/blocked.certs");
                    try (FileInputStream fin = new FileInputStream(f);){
                        props.load(fin);
                    }
                    catch (IOException fnfe) {
                        if (debug == null) break block7;
                        debug.println("Error parsing blocked.certs");
                    }
                }
                return null;
            }
        });
        algorithm = props.getProperty(ALGORITHM_KEY);
    }
}

