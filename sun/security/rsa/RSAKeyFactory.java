/*
 * Decompiled with CFR 0.152.
 */
package sun.security.rsa;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import sun.security.action.GetPropertyAction;
import sun.security.rsa.RSAPrivateCrtKeyImpl;
import sun.security.rsa.RSAPrivateKeyImpl;
import sun.security.rsa.RSAPublicKeyImpl;
import sun.security.rsa.RSAUtil;

public class RSAKeyFactory
extends KeyFactorySpi {
    private static final Class<?> RSA_PUB_KEYSPEC_CLS = RSAPublicKeySpec.class;
    private static final Class<?> RSA_PRIV_KEYSPEC_CLS = RSAPrivateKeySpec.class;
    private static final Class<?> RSA_PRIVCRT_KEYSPEC_CLS = RSAPrivateCrtKeySpec.class;
    private static final Class<?> X509_KEYSPEC_CLS = X509EncodedKeySpec.class;
    private static final Class<?> PKCS8_KEYSPEC_CLS = PKCS8EncodedKeySpec.class;
    public static final int MIN_MODLEN = 512;
    public static final int MAX_MODLEN = 16384;
    private final RSAUtil.KeyType type;
    public static final int MAX_MODLEN_RESTRICT_EXP = 3072;
    public static final int MAX_RESTRICTED_EXPLEN = 64;
    private static final boolean restrictExpLen = "true".equalsIgnoreCase(GetPropertyAction.privilegedGetProperty("sun.security.rsa.restrictRSAExponent", "true"));

    static RSAKeyFactory getInstance(RSAUtil.KeyType type) {
        return new RSAKeyFactory(type);
    }

    static void checkKeyAlgo(Key key, String expectedAlg) throws InvalidKeyException {
        String keyAlg = key.getAlgorithm();
        if (keyAlg == null || !keyAlg.equalsIgnoreCase(expectedAlg)) {
            throw new InvalidKeyException("Expected a " + expectedAlg + " key, but got " + keyAlg);
        }
    }

    public static RSAKey toRSAKey(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("Key must not be null");
        }
        if (key instanceof RSAPrivateKeyImpl || key instanceof RSAPrivateCrtKeyImpl || key instanceof RSAPublicKeyImpl) {
            return (RSAKey)((Object)key);
        }
        try {
            RSAUtil.KeyType type = RSAUtil.KeyType.lookup(key.getAlgorithm());
            RSAKeyFactory kf = RSAKeyFactory.getInstance(type);
            return (RSAKey)((Object)kf.engineTranslateKey(key));
        }
        catch (ProviderException e) {
            throw new InvalidKeyException(e);
        }
    }

    static void checkRSAProviderKeyLengths(int modulusLen, BigInteger exponent) throws InvalidKeyException {
        RSAKeyFactory.checkKeyLengths(modulusLen + 7 & 0xFFFFFFF8, exponent, 512, Integer.MAX_VALUE);
    }

    public static void checkKeyLengths(int modulusLen, BigInteger exponent, int minModulusLen, int maxModulusLen) throws InvalidKeyException {
        if (minModulusLen > 0 && modulusLen < minModulusLen) {
            throw new InvalidKeyException("RSA keys must be at least " + minModulusLen + " bits long");
        }
        int maxLen = Math.min(maxModulusLen, 16384);
        if (modulusLen > maxLen) {
            throw new InvalidKeyException("RSA keys must be no longer than " + maxLen + " bits");
        }
        if (restrictExpLen && exponent != null && modulusLen > 3072 && exponent.bitLength() > 64) {
            throw new InvalidKeyException("RSA exponents can be no longer than 64 bits  if modulus is greater than 3072 bits");
        }
    }

    private RSAKeyFactory() {
        this.type = RSAUtil.KeyType.RSA;
    }

    public RSAKeyFactory(RSAUtil.KeyType type) {
        this.type = type;
    }

    @Override
    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("Key must not be null");
        }
        RSAKeyFactory.checkKeyAlgo(key, this.type.keyAlgo);
        if (key instanceof RSAPrivateKeyImpl || key instanceof RSAPrivateCrtKeyImpl || key instanceof RSAPublicKeyImpl) {
            return key;
        }
        if (key instanceof PublicKey) {
            return this.translatePublicKey((PublicKey)key);
        }
        if (key instanceof PrivateKey) {
            return this.translatePrivateKey((PrivateKey)key);
        }
        throw new InvalidKeyException("Neither a public nor a private key");
    }

    @Override
    protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
        try {
            return this.generatePublic(keySpec);
        }
        catch (InvalidKeySpecException e) {
            throw e;
        }
        catch (GeneralSecurityException e) {
            throw new InvalidKeySpecException(e);
        }
    }

    @Override
    protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
        try {
            return this.generatePrivate(keySpec);
        }
        catch (InvalidKeySpecException e) {
            throw e;
        }
        catch (GeneralSecurityException e) {
            throw new InvalidKeySpecException(e);
        }
    }

    private PublicKey translatePublicKey(PublicKey key) throws InvalidKeyException {
        if (key instanceof RSAPublicKey) {
            RSAPublicKey rsaKey = (RSAPublicKey)key;
            try {
                return new RSAPublicKeyImpl(this.type, rsaKey.getParams(), rsaKey.getModulus(), rsaKey.getPublicExponent());
            }
            catch (ProviderException e) {
                throw new InvalidKeyException("Invalid key", e);
            }
        }
        return RSAPublicKeyImpl.newKey(this.type, key.getFormat(), key.getEncoded());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private PrivateKey translatePrivateKey(PrivateKey key) throws InvalidKeyException {
        if (key instanceof RSAPrivateCrtKey) {
            RSAPrivateCrtKey rsaKey = (RSAPrivateCrtKey)key;
            try {
                return new RSAPrivateCrtKeyImpl(this.type, rsaKey.getParams(), rsaKey.getModulus(), rsaKey.getPublicExponent(), rsaKey.getPrivateExponent(), rsaKey.getPrimeP(), rsaKey.getPrimeQ(), rsaKey.getPrimeExponentP(), rsaKey.getPrimeExponentQ(), rsaKey.getCrtCoefficient());
            }
            catch (ProviderException e) {
                throw new InvalidKeyException("Invalid key", e);
            }
        }
        if (key instanceof RSAPrivateKey) {
            RSAPrivateKey rsaKey = (RSAPrivateKey)key;
            try {
                return new RSAPrivateKeyImpl(this.type, rsaKey.getParams(), rsaKey.getModulus(), rsaKey.getPrivateExponent());
            }
            catch (ProviderException e) {
                throw new InvalidKeyException("Invalid key", e);
            }
        }
        byte[] encoded = key.getEncoded();
        try {
            RSAPrivateKey rSAPrivateKey = RSAPrivateCrtKeyImpl.newKey(this.type, key.getFormat(), encoded);
            return rSAPrivateKey;
        }
        finally {
            if (encoded != null) {
                Arrays.fill(encoded, (byte)0);
            }
        }
    }

    private PublicKey generatePublic(KeySpec keySpec) throws GeneralSecurityException {
        if (keySpec instanceof X509EncodedKeySpec) {
            return RSAPublicKeyImpl.newKey(this.type, "X.509", ((X509EncodedKeySpec)keySpec).getEncoded());
        }
        if (keySpec instanceof RSAPublicKeySpec) {
            RSAPublicKeySpec rsaSpec = (RSAPublicKeySpec)keySpec;
            try {
                return new RSAPublicKeyImpl(this.type, rsaSpec.getParams(), rsaSpec.getModulus(), rsaSpec.getPublicExponent());
            }
            catch (ProviderException e) {
                throw new InvalidKeySpecException(e);
            }
        }
        throw new InvalidKeySpecException("Only RSAPublicKeySpec and X509EncodedKeySpec supported for RSA public keys");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private PrivateKey generatePrivate(KeySpec keySpec) throws GeneralSecurityException {
        if (keySpec instanceof PKCS8EncodedKeySpec) {
            byte[] encoded = ((PKCS8EncodedKeySpec)keySpec).getEncoded();
            try {
                RSAPrivateKey rSAPrivateKey = RSAPrivateCrtKeyImpl.newKey(this.type, "PKCS#8", encoded);
                return rSAPrivateKey;
            }
            finally {
                Arrays.fill(encoded, (byte)0);
            }
        }
        if (keySpec instanceof RSAPrivateCrtKeySpec) {
            RSAPrivateCrtKeySpec rsaSpec = (RSAPrivateCrtKeySpec)keySpec;
            try {
                return new RSAPrivateCrtKeyImpl(this.type, rsaSpec.getParams(), rsaSpec.getModulus(), rsaSpec.getPublicExponent(), rsaSpec.getPrivateExponent(), rsaSpec.getPrimeP(), rsaSpec.getPrimeQ(), rsaSpec.getPrimeExponentP(), rsaSpec.getPrimeExponentQ(), rsaSpec.getCrtCoefficient());
            }
            catch (ProviderException e) {
                throw new InvalidKeySpecException(e);
            }
        }
        if (keySpec instanceof RSAPrivateKeySpec) {
            RSAPrivateKeySpec rsaSpec = (RSAPrivateKeySpec)keySpec;
            try {
                return new RSAPrivateKeyImpl(this.type, rsaSpec.getParams(), rsaSpec.getModulus(), rsaSpec.getPrivateExponent());
            }
            catch (ProviderException e) {
                throw new InvalidKeySpecException(e);
            }
        }
        throw new InvalidKeySpecException("Only RSAPrivate(Crt)KeySpec and PKCS8EncodedKeySpec supported for RSA private keys");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {
        try {
            key = this.engineTranslateKey(key);
        }
        catch (InvalidKeyException e) {
            throw new InvalidKeySpecException(e);
        }
        if (key instanceof RSAPublicKey) {
            RSAPublicKey rsaKey = (RSAPublicKey)key;
            if (keySpec.isAssignableFrom(RSA_PUB_KEYSPEC_CLS)) {
                return (T)((KeySpec)keySpec.cast(new RSAPublicKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent(), rsaKey.getParams())));
            }
            if (keySpec.isAssignableFrom(X509_KEYSPEC_CLS)) {
                return (T)((KeySpec)keySpec.cast(new X509EncodedKeySpec(key.getEncoded())));
            }
            throw new InvalidKeySpecException("KeySpec must be RSAPublicKeySpec or X509EncodedKeySpec for RSA public keys");
        }
        if (key instanceof RSAPrivateKey) {
            if (keySpec.isAssignableFrom(PKCS8_KEYSPEC_CLS)) {
                byte[] encoded = key.getEncoded();
                try {
                    KeySpec keySpec2 = (KeySpec)keySpec.cast(new PKCS8EncodedKeySpec(encoded));
                    return (T)keySpec2;
                }
                finally {
                    Arrays.fill(encoded, (byte)0);
                }
            }
            if (keySpec.isAssignableFrom(RSA_PRIVCRT_KEYSPEC_CLS)) {
                if (key instanceof RSAPrivateCrtKey) {
                    RSAPrivateCrtKey crtKey = (RSAPrivateCrtKey)key;
                    return (T)((KeySpec)keySpec.cast(new RSAPrivateCrtKeySpec(crtKey.getModulus(), crtKey.getPublicExponent(), crtKey.getPrivateExponent(), crtKey.getPrimeP(), crtKey.getPrimeQ(), crtKey.getPrimeExponentP(), crtKey.getPrimeExponentQ(), crtKey.getCrtCoefficient(), crtKey.getParams())));
                }
                if (!keySpec.isAssignableFrom(RSA_PRIV_KEYSPEC_CLS)) {
                    throw new InvalidKeySpecException("RSAPrivateCrtKeySpec can only be used with CRT keys");
                }
                RSAPrivateKey rsaKey = (RSAPrivateKey)key;
                return (T)((KeySpec)keySpec.cast(new RSAPrivateKeySpec(rsaKey.getModulus(), rsaKey.getPrivateExponent(), rsaKey.getParams())));
            }
            throw new InvalidKeySpecException("KeySpec must be RSAPrivate(Crt)KeySpec or PKCS8EncodedKeySpec for RSA private keys");
        }
        throw new InvalidKeySpecException("Neither public nor private key");
    }

    public static final class PSS
    extends RSAKeyFactory {
        public PSS() {
            super(RSAUtil.KeyType.PSS);
        }
    }

    public static final class Legacy
    extends RSAKeyFactory {
        public Legacy() {
            super(RSAUtil.KeyType.RSA);
        }
    }
}

