/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.security.cert.X509Certificate;
import sun.security.ssl.ClientHello;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.SSLContextImpl;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.TransportContext;
import sun.security.ssl.Utilities;

class ClientHandshakeContext
extends HandshakeContext {
    static final boolean allowUnsafeServerCertChange = Utilities.getBooleanProperty("jdk.tls.allowUnsafeServerCertChange", false);
    X509Certificate[] reservedServerCerts = null;
    X509Certificate[] deferredCerts;
    ClientHello.ClientHelloMessage initialClientHelloMsg = null;
    boolean receivedCertReq = false;
    byte[] pskIdentity;

    ClientHandshakeContext(SSLContextImpl sslContext, TransportContext conContext) throws IOException {
        super(sslContext, conContext);
    }

    @Override
    void kickstart() throws IOException {
        if (this.kickstartMessageDelivered) {
            return;
        }
        SSLHandshake.kickstart(this);
        this.kickstartMessageDelivered = true;
    }
}

