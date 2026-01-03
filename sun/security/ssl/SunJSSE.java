/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.security.AccessController;
import java.security.Provider;
import java.util.HashMap;
import java.util.List;
import sun.security.util.SecurityConstants;

public class SunJSSE
extends Provider {
    private static final long serialVersionUID = 3231825739635378733L;
    private static final String info = "Sun JSSE provider(PKCS12, SunX509/PKIX key/trust factories, SSLv3/TLSv1/TLSv1.1/TLSv1.2/TLSv1.3/DTLSv1.0/DTLSv1.2)";

    public SunJSSE() {
        super("SunJSSE", SecurityConstants.PROVIDER_VER, info);
        this.registerAlgorithms();
    }

    private void registerAlgorithms() {
        AccessController.doPrivileged(() -> {
            this.doRegister();
            return null;
        });
    }

    private void ps(String type, String algo, String cn, List<String> a, HashMap<String, String> attrs) {
        this.putService(new Provider.Service(this, type, algo, cn, a, attrs));
    }

    private void doRegister() {
        this.ps("Signature", "MD5andSHA1withRSA", "sun.security.ssl.RSASignature", null, null);
        this.ps("KeyManagerFactory", "SunX509", "sun.security.ssl.KeyManagerFactoryImpl$SunX509", null, null);
        this.ps("KeyManagerFactory", "NewSunX509", "sun.security.ssl.KeyManagerFactoryImpl$X509", List.of("PKIX"), null);
        this.ps("TrustManagerFactory", "SunX509", "sun.security.ssl.TrustManagerFactoryImpl$SimpleFactory", null, null);
        this.ps("TrustManagerFactory", "PKIX", "sun.security.ssl.TrustManagerFactoryImpl$PKIXFactory", List.of("SunPKIX", "X509", "X.509"), null);
        this.ps("SSLContext", "TLSv1", "sun.security.ssl.SSLContextImpl$TLS10Context", List.of("SSLv3"), null);
        this.ps("SSLContext", "TLSv1.1", "sun.security.ssl.SSLContextImpl$TLS11Context", null, null);
        this.ps("SSLContext", "TLSv1.2", "sun.security.ssl.SSLContextImpl$TLS12Context", null, null);
        this.ps("SSLContext", "TLSv1.3", "sun.security.ssl.SSLContextImpl$TLS13Context", null, null);
        this.ps("SSLContext", "TLS", "sun.security.ssl.SSLContextImpl$TLSContext", List.of("SSL"), null);
        this.ps("SSLContext", "DTLSv1.0", "sun.security.ssl.SSLContextImpl$DTLS10Context", null, null);
        this.ps("SSLContext", "DTLSv1.2", "sun.security.ssl.SSLContextImpl$DTLS12Context", null, null);
        this.ps("SSLContext", "DTLS", "sun.security.ssl.SSLContextImpl$DTLSContext", null, null);
        this.ps("SSLContext", "Default", "sun.security.ssl.SSLContextImpl$DefaultSSLContext", null, null);
        this.ps("KeyStore", "PKCS12", "sun.security.pkcs12.PKCS12KeyStore", null, null);
    }
}

