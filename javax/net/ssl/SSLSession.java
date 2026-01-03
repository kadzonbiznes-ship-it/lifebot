/*
 * Decompiled with CFR 0.152.
 */
package javax.net.ssl;

import java.security.Principal;
import java.security.cert.Certificate;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSessionContext;
import javax.security.cert.X509Certificate;

public interface SSLSession {
    public byte[] getId();

    public SSLSessionContext getSessionContext();

    public long getCreationTime();

    public long getLastAccessedTime();

    public void invalidate();

    public boolean isValid();

    public void putValue(String var1, Object var2);

    public Object getValue(String var1);

    public void removeValue(String var1);

    public String[] getValueNames();

    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException;

    public Certificate[] getLocalCertificates();

    @Deprecated(since="9", forRemoval=true)
    default public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        throw new UnsupportedOperationException("This method is deprecated and marked for removal. Use the getPeerCertificates() method instead.");
    }

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException;

    public Principal getLocalPrincipal();

    public String getCipherSuite();

    public String getProtocol();

    public String getPeerHost();

    public int getPeerPort();

    public int getPacketBufferSize();

    public int getApplicationBufferSize();
}

