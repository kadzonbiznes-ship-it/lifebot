/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.security.AlgorithmConstraints;
import java.security.AlgorithmParameters;
import java.security.CryptoPrimitive;
import java.security.Key;
import java.util.Set;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.SSLAlgorithmDecomposer;
import sun.security.ssl.SSLEngineImpl;
import sun.security.ssl.SSLScope;
import sun.security.ssl.SSLSocketImpl;
import sun.security.util.DisabledAlgorithmConstraints;

final class SSLAlgorithmConstraints
implements AlgorithmConstraints {
    private static final DisabledAlgorithmConstraints tlsDisabledAlgConstraints = new DisabledAlgorithmConstraints("jdk.tls.disabledAlgorithms", new SSLAlgorithmDecomposer());
    private static final DisabledAlgorithmConstraints x509DisabledAlgConstraints = new DisabledAlgorithmConstraints("jdk.certpath.disabledAlgorithms", new SSLAlgorithmDecomposer(true));
    private final AlgorithmConstraints userSpecifiedConstraints;
    private final AlgorithmConstraints peerSpecifiedConstraints;
    private final boolean enabledX509DisabledAlgConstraints;
    static final SSLAlgorithmConstraints DEFAULT = new SSLAlgorithmConstraints(null, true);
    static final SSLAlgorithmConstraints DEFAULT_SSL_ONLY = new SSLAlgorithmConstraints(null, false);

    private SSLAlgorithmConstraints(AlgorithmConstraints userSpecifiedConstraints, boolean enabledX509DisabledAlgConstraints) {
        this(userSpecifiedConstraints, null, enabledX509DisabledAlgConstraints);
    }

    private SSLAlgorithmConstraints(AlgorithmConstraints userSpecifiedConstraints, SupportedSignatureAlgorithmConstraints peerSpecifiedConstraints, boolean withDefaultCertPathConstraints) {
        this.userSpecifiedConstraints = userSpecifiedConstraints;
        this.peerSpecifiedConstraints = peerSpecifiedConstraints;
        this.enabledX509DisabledAlgConstraints = withDefaultCertPathConstraints;
    }

    static SSLAlgorithmConstraints wrap(AlgorithmConstraints userSpecifiedConstraints) {
        return SSLAlgorithmConstraints.wrap(userSpecifiedConstraints, true);
    }

    private static SSLAlgorithmConstraints wrap(AlgorithmConstraints userSpecifiedConstraints, boolean withDefaultCertPathConstraints) {
        if (SSLAlgorithmConstraints.nullIfDefault(userSpecifiedConstraints) == null) {
            return withDefaultCertPathConstraints ? DEFAULT : DEFAULT_SSL_ONLY;
        }
        return new SSLAlgorithmConstraints(userSpecifiedConstraints, withDefaultCertPathConstraints);
    }

    static AlgorithmConstraints forSocket(SSLSocket socket, boolean withDefaultCertPathConstraints) {
        AlgorithmConstraints userSpecifiedConstraints = SSLAlgorithmConstraints.getUserSpecifiedConstraints(socket);
        return SSLAlgorithmConstraints.wrap(userSpecifiedConstraints, withDefaultCertPathConstraints);
    }

    static SSLAlgorithmConstraints forSocket(SSLSocket socket, String[] supportedAlgorithms, boolean withDefaultCertPathConstraints) {
        return new SSLAlgorithmConstraints(SSLAlgorithmConstraints.nullIfDefault(SSLAlgorithmConstraints.getUserSpecifiedConstraints(socket)), new SupportedSignatureAlgorithmConstraints(supportedAlgorithms), withDefaultCertPathConstraints);
    }

    static AlgorithmConstraints forEngine(SSLEngine engine, boolean withDefaultCertPathConstraints) {
        AlgorithmConstraints userSpecifiedConstraints = SSLAlgorithmConstraints.getUserSpecifiedConstraints(engine);
        return SSLAlgorithmConstraints.wrap(userSpecifiedConstraints, withDefaultCertPathConstraints);
    }

    static SSLAlgorithmConstraints forEngine(SSLEngine engine, String[] supportedAlgorithms, boolean withDefaultCertPathConstraints) {
        return new SSLAlgorithmConstraints(SSLAlgorithmConstraints.nullIfDefault(SSLAlgorithmConstraints.getUserSpecifiedConstraints(engine)), new SupportedSignatureAlgorithmConstraints(supportedAlgorithms), withDefaultCertPathConstraints);
    }

    private static AlgorithmConstraints nullIfDefault(AlgorithmConstraints constraints) {
        return constraints == DEFAULT ? null : constraints;
    }

    private static AlgorithmConstraints getUserSpecifiedConstraints(SSLEngine engine) {
        if (engine != null) {
            HandshakeContext hc;
            if (engine instanceof SSLEngineImpl && (hc = ((SSLEngineImpl)engine).conContext.handshakeContext) != null) {
                return hc.sslConfig.userSpecifiedAlgorithmConstraints;
            }
            return engine.getSSLParameters().getAlgorithmConstraints();
        }
        return null;
    }

    private static AlgorithmConstraints getUserSpecifiedConstraints(SSLSocket socket) {
        if (socket != null) {
            HandshakeContext hc;
            if (socket instanceof SSLSocketImpl && (hc = ((SSLSocketImpl)socket).conContext.handshakeContext) != null) {
                return hc.sslConfig.userSpecifiedAlgorithmConstraints;
            }
            return socket.getSSLParameters().getAlgorithmConstraints();
        }
        return null;
    }

    @Override
    public boolean permits(Set<CryptoPrimitive> primitives, String algorithm, AlgorithmParameters parameters) {
        boolean permitted = true;
        if (this.peerSpecifiedConstraints != null) {
            permitted = this.peerSpecifiedConstraints.permits(primitives, algorithm, parameters);
        }
        if (permitted && this.userSpecifiedConstraints != null) {
            permitted = this.userSpecifiedConstraints.permits(primitives, algorithm, parameters);
        }
        if (permitted) {
            permitted = tlsDisabledAlgConstraints.permits(primitives, algorithm, parameters);
        }
        if (permitted && this.enabledX509DisabledAlgConstraints) {
            permitted = x509DisabledAlgConstraints.permits(primitives, algorithm, parameters);
        }
        return permitted;
    }

    @Override
    public boolean permits(Set<CryptoPrimitive> primitives, Key key) {
        boolean permitted = true;
        if (this.peerSpecifiedConstraints != null) {
            permitted = this.peerSpecifiedConstraints.permits(primitives, key);
        }
        if (permitted && this.userSpecifiedConstraints != null) {
            permitted = this.userSpecifiedConstraints.permits(primitives, key);
        }
        if (permitted) {
            permitted = tlsDisabledAlgConstraints.permits(primitives, key);
        }
        if (permitted && this.enabledX509DisabledAlgConstraints) {
            permitted = x509DisabledAlgConstraints.permits(primitives, key);
        }
        return permitted;
    }

    @Override
    public boolean permits(Set<CryptoPrimitive> primitives, String algorithm, Key key, AlgorithmParameters parameters) {
        boolean permitted = true;
        if (this.peerSpecifiedConstraints != null) {
            permitted = this.peerSpecifiedConstraints.permits(primitives, algorithm, key, parameters);
        }
        if (permitted && this.userSpecifiedConstraints != null) {
            permitted = this.userSpecifiedConstraints.permits(primitives, algorithm, key, parameters);
        }
        if (permitted) {
            permitted = tlsDisabledAlgConstraints.permits(primitives, algorithm, key, parameters);
        }
        if (permitted && this.enabledX509DisabledAlgConstraints) {
            permitted = x509DisabledAlgConstraints.permits(primitives, algorithm, key, parameters);
        }
        return permitted;
    }

    boolean permits(String algorithm, Set<SSLScope> scopes) {
        return tlsDisabledAlgConstraints.permits(algorithm, scopes);
    }

    private static class SupportedSignatureAlgorithmConstraints
    implements AlgorithmConstraints {
        private final String[] supportedAlgorithms;

        SupportedSignatureAlgorithmConstraints(String[] supportedAlgorithms) {
            this.supportedAlgorithms = supportedAlgorithms != null ? (String[])supportedAlgorithms.clone() : null;
        }

        @Override
        public boolean permits(Set<CryptoPrimitive> primitives, String algorithm, AlgorithmParameters parameters) {
            if (algorithm == null || algorithm.isEmpty()) {
                throw new IllegalArgumentException("No algorithm name specified");
            }
            if (primitives == null || primitives.isEmpty()) {
                throw new IllegalArgumentException("No cryptographic primitive specified");
            }
            if (this.supportedAlgorithms == null || this.supportedAlgorithms.length == 0) {
                return false;
            }
            int position = algorithm.indexOf("and");
            if (position > 0) {
                algorithm = algorithm.substring(0, position);
            }
            for (String supportedAlgorithm : this.supportedAlgorithms) {
                if (!algorithm.equalsIgnoreCase(supportedAlgorithm)) continue;
                return true;
            }
            return false;
        }

        @Override
        public final boolean permits(Set<CryptoPrimitive> primitives, Key key) {
            return true;
        }

        @Override
        public final boolean permits(Set<CryptoPrimitive> primitives, String algorithm, Key key, AlgorithmParameters parameters) {
            if (algorithm == null || algorithm.isEmpty()) {
                throw new IllegalArgumentException("No algorithm name specified");
            }
            return this.permits(primitives, algorithm, parameters);
        }
    }
}

