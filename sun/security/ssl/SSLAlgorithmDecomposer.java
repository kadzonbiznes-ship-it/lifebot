/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.util.HashSet;
import java.util.Set;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.CipherType;
import sun.security.ssl.SSLCipher;
import sun.security.util.AlgorithmDecomposer;

class SSLAlgorithmDecomposer
extends AlgorithmDecomposer {
    private final boolean onlyX509;

    SSLAlgorithmDecomposer(boolean onlyX509) {
        this.onlyX509 = onlyX509;
    }

    SSLAlgorithmDecomposer() {
        this(false);
    }

    private Set<String> decomposes(CipherSuite.KeyExchange keyExchange) {
        HashSet<String> components = new HashSet<String>();
        switch (keyExchange) {
            case K_NULL: {
                if (this.onlyX509) break;
                components.add("K_NULL");
                break;
            }
            case K_RSA: {
                components.add("RSA");
                break;
            }
            case K_RSA_EXPORT: {
                components.add("RSA");
                components.add("RSA_EXPORT");
                break;
            }
            case K_DH_RSA: {
                components.add("RSA");
                components.add("DH");
                components.add("DiffieHellman");
                components.add("DH_RSA");
                break;
            }
            case K_DH_DSS: {
                components.add("DSA");
                components.add("DSS");
                components.add("DH");
                components.add("DiffieHellman");
                components.add("DH_DSS");
                break;
            }
            case K_DHE_DSS: {
                components.add("DSA");
                components.add("DSS");
                components.add("DH");
                components.add("DHE");
                components.add("DiffieHellman");
                components.add("DHE_DSS");
                break;
            }
            case K_DHE_RSA: {
                components.add("RSA");
                components.add("DH");
                components.add("DHE");
                components.add("DiffieHellman");
                components.add("DHE_RSA");
                break;
            }
            case K_DH_ANON: {
                if (this.onlyX509) break;
                components.add("ANON");
                components.add("DH");
                components.add("DiffieHellman");
                components.add("DH_ANON");
                break;
            }
            case K_ECDH_ECDSA: {
                components.add("ECDH");
                components.add("ECDSA");
                components.add("ECDH_ECDSA");
                break;
            }
            case K_ECDH_RSA: {
                components.add("ECDH");
                components.add("RSA");
                components.add("ECDH_RSA");
                break;
            }
            case K_ECDHE_ECDSA: {
                components.add("ECDHE");
                components.add("ECDSA");
                components.add("ECDHE_ECDSA");
                break;
            }
            case K_ECDHE_RSA: {
                components.add("ECDHE");
                components.add("RSA");
                components.add("ECDHE_RSA");
                break;
            }
            case K_ECDH_ANON: {
                if (this.onlyX509) break;
                components.add("ECDH");
                components.add("ANON");
                components.add("ECDH_ANON");
                break;
            }
        }
        return components;
    }

    private Set<String> decomposes(SSLCipher bulkCipher) {
        HashSet<String> components = new HashSet<String>();
        if (bulkCipher.transformation != null) {
            components.addAll(super.decompose(bulkCipher.transformation));
        }
        switch (bulkCipher) {
            case B_NULL: {
                components.add("C_NULL");
                break;
            }
            case B_RC2_40: {
                components.add("RC2_CBC_40");
                break;
            }
            case B_RC4_40: {
                components.add("RC4_40");
                break;
            }
            case B_RC4_128: {
                components.add("RC4_128");
                break;
            }
            case B_DES_40: {
                components.add("DES40_CBC");
                components.add("DES_CBC_40");
                break;
            }
            case B_DES: {
                components.add("DES_CBC");
                break;
            }
            case B_3DES: {
                components.add("3DES_EDE_CBC");
                break;
            }
            case B_AES_128: {
                components.add("AES_128_CBC");
                break;
            }
            case B_AES_256: {
                components.add("AES_256_CBC");
                break;
            }
            case B_AES_128_GCM: {
                components.add("AES_128_GCM");
                break;
            }
            case B_AES_256_GCM: {
                components.add("AES_256_GCM");
            }
        }
        return components;
    }

    private Set<String> decomposes(CipherSuite.MacAlg macAlg, SSLCipher cipher) {
        HashSet<String> components = new HashSet<String>();
        if (macAlg == CipherSuite.MacAlg.M_NULL && cipher.cipherType != CipherType.AEAD_CIPHER) {
            components.add("M_NULL");
        } else if (macAlg == CipherSuite.MacAlg.M_MD5) {
            components.add("MD5");
            components.add("HmacMD5");
        } else if (macAlg == CipherSuite.MacAlg.M_SHA) {
            components.add("SHA1");
            components.add("SHA-1");
            components.add("HmacSHA1");
        } else if (macAlg == CipherSuite.MacAlg.M_SHA256) {
            components.add("SHA256");
            components.add("SHA-256");
            components.add("HmacSHA256");
        } else if (macAlg == CipherSuite.MacAlg.M_SHA384) {
            components.add("SHA384");
            components.add("SHA-384");
            components.add("HmacSHA384");
        }
        return components;
    }

    private Set<String> decomposes(CipherSuite.HashAlg hashAlg) {
        HashSet<String> components = new HashSet<String>();
        if (hashAlg == CipherSuite.HashAlg.H_SHA256) {
            components.add("SHA256");
            components.add("SHA-256");
            components.add("HmacSHA256");
        } else if (hashAlg == CipherSuite.HashAlg.H_SHA384) {
            components.add("SHA384");
            components.add("SHA-384");
            components.add("HmacSHA384");
        }
        return components;
    }

    private Set<String> decompose(CipherSuite.KeyExchange keyExchange, SSLCipher cipher, CipherSuite.MacAlg macAlg, CipherSuite.HashAlg hashAlg) {
        HashSet<String> components = new HashSet<String>();
        if (keyExchange != null) {
            components.addAll(this.decomposes(keyExchange));
        }
        if (this.onlyX509) {
            return components;
        }
        if (cipher != null) {
            components.addAll(this.decomposes(cipher));
        }
        if (macAlg != null) {
            components.addAll(this.decomposes(macAlg, cipher));
        }
        if (hashAlg != null) {
            components.addAll(this.decomposes(hashAlg));
        }
        return components;
    }

    @Override
    public Set<String> decompose(String algorithm) {
        if (algorithm.startsWith("SSL_") || algorithm.startsWith("TLS_")) {
            CipherSuite cipherSuite = null;
            try {
                cipherSuite = CipherSuite.nameOf(algorithm);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
            if (cipherSuite != null && cipherSuite != CipherSuite.TLS_EMPTY_RENEGOTIATION_INFO_SCSV) {
                return this.decompose(cipherSuite.keyExchange, cipherSuite.bulkCipher, cipherSuite.macAlg, cipherSuite.hashAlg);
            }
        }
        return super.decompose(algorithm);
    }
}

