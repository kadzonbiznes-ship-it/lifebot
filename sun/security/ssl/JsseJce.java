/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.KeyAgreement;
import sun.security.ssl.Utilities;

final class JsseJce {
    static final boolean ALLOW_ECC = Utilities.getBooleanProperty("com.sun.net.ssl.enableECC", true);
    static final String CIPHER_RSA_PKCS1 = "RSA/ECB/PKCS1Padding";
    static final String CIPHER_RC4 = "RC4";
    static final String CIPHER_DES = "DES/CBC/NoPadding";
    static final String CIPHER_3DES = "DESede/CBC/NoPadding";
    static final String CIPHER_AES = "AES/CBC/NoPadding";
    static final String CIPHER_AES_GCM = "AES/GCM/NoPadding";
    static final String CIPHER_CHACHA20_POLY1305 = "ChaCha20-Poly1305";
    static final String SIGNATURE_DSA = "DSA";
    static final String SIGNATURE_ECDSA = "SHA1withECDSA";
    static final String SIGNATURE_EDDSA = "EdDSA";
    static final String SIGNATURE_RAWDSA = "RawDSA";
    static final String SIGNATURE_RAWECDSA = "NONEwithECDSA";
    static final String SIGNATURE_RAWRSA = "NONEwithRSA";
    static final String SIGNATURE_SSLRSA = "MD5andSHA1withRSA";

    private JsseJce() {
    }

    static boolean isEcAvailable() {
        return EcAvailability.isAvailable;
    }

    static int getRSAKeyLength(PublicKey key) {
        BigInteger modulus;
        if (key instanceof RSAPublicKey) {
            modulus = ((RSAPublicKey)key).getModulus();
        } else {
            RSAPublicKeySpec spec = JsseJce.getRSAPublicKeySpec(key);
            modulus = spec.getModulus();
        }
        return modulus.bitLength();
    }

    static RSAPublicKeySpec getRSAPublicKeySpec(PublicKey key) {
        if (key instanceof RSAPublicKey) {
            RSAPublicKey rsaKey = (RSAPublicKey)key;
            return new RSAPublicKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent());
        }
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.getKeySpec(key, RSAPublicKeySpec.class);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class EcAvailability {
        private static final boolean isAvailable;

        private EcAvailability() {
        }

        static {
            boolean mediator = true;
            try {
                Signature.getInstance(JsseJce.SIGNATURE_ECDSA);
                Signature.getInstance(JsseJce.SIGNATURE_RAWECDSA);
                KeyAgreement.getInstance("ECDH");
                KeyFactory.getInstance("EC");
                KeyPairGenerator.getInstance("EC");
                AlgorithmParameters.getInstance("EC");
            }
            catch (Exception e) {
                mediator = false;
            }
            isAvailable = mediator;
        }
    }
}

