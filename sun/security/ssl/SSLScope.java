/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

public enum SSLScope {
    HANDSHAKE_SIGNATURE("HandshakeSignature"),
    CERTIFICATE_SIGNATURE("CertificateSignature");

    private final String name;

    private SSLScope(String name) {
        this.name = name;
    }

    public static SSLScope nameOf(String scopeName) {
        for (SSLScope scope : SSLScope.values()) {
            if (!scope.name.equalsIgnoreCase(scopeName)) continue;
            return scope;
        }
        return null;
    }
}

