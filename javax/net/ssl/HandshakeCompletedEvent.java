/*
 * Decompiled with CFR 0.152.
 */
package javax.net.ssl;

import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.EventObject;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public class HandshakeCompletedEvent
extends EventObject {
    private static final long serialVersionUID = 7914963744257769778L;
    private final transient SSLSession session;

    public HandshakeCompletedEvent(SSLSocket sock, SSLSession s) {
        super(sock);
        this.session = s;
    }

    public SSLSession getSession() {
        return this.session;
    }

    public String getCipherSuite() {
        return this.session.getCipherSuite();
    }

    public Certificate[] getLocalCertificates() {
        return this.session.getLocalCertificates();
    }

    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        return this.session.getPeerCertificates();
    }

    @Deprecated(since="9", forRemoval=true)
    public javax.security.cert.X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        return this.session.getPeerCertificateChain();
    }

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        Principal principal;
        try {
            principal = this.session.getPeerPrincipal();
        }
        catch (AbstractMethodError e) {
            Certificate[] certs = this.getPeerCertificates();
            principal = ((X509Certificate)certs[0]).getSubjectX500Principal();
        }
        return principal;
    }

    public Principal getLocalPrincipal() {
        Principal principal;
        block2: {
            try {
                principal = this.session.getLocalPrincipal();
            }
            catch (AbstractMethodError e) {
                principal = null;
                Certificate[] certs = this.getLocalCertificates();
                if (certs == null) break block2;
                principal = ((X509Certificate)certs[0]).getSubjectX500Principal();
            }
        }
        return principal;
    }

    public SSLSocket getSocket() {
        return (SSLSocket)this.getSource();
    }
}

