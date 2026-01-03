/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyFactorySpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import sun.security.ec.ECPrivateKeyImpl;
import sun.security.ec.ECPublicKeyImpl;

public final class ECKeyFactory
extends KeyFactorySpi {
    private static KeyFactory instance;

    private static KeyFactory getInstance() {
        if (instance == null) {
            try {
                instance = KeyFactory.getInstance("EC", "SunEC");
            }
            catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public static ECKey toECKey(Key key) throws InvalidKeyException {
        if (key instanceof ECKey) {
            ECKey ecKey = (ECKey)((Object)key);
            ECKeyFactory.checkKey(ecKey);
            return ecKey;
        }
        return (ECKey)((Object)ECKeyFactory.getInstance().translateKey(key));
    }

    private static void checkKey(ECKey key) throws InvalidKeyException {
        String keyAlg;
        if (key instanceof ECPublicKey) {
            if (key instanceof ECPublicKeyImpl) {
                return;
            }
        } else if (key instanceof ECPrivateKey) {
            if (key instanceof ECPrivateKeyImpl) {
                return;
            }
        } else {
            throw new InvalidKeyException("Neither a public nor a private key");
        }
        if (!(keyAlg = ((Key)((Object)key)).getAlgorithm()).equals("EC")) {
            throw new InvalidKeyException("Not an EC key: " + keyAlg);
        }
    }

    @Override
    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("Key must not be null");
        }
        String keyAlg = key.getAlgorithm();
        if (!keyAlg.equals("EC")) {
            throw new InvalidKeyException("Not an EC key: " + keyAlg);
        }
        if (key instanceof PublicKey) {
            return this.implTranslatePublicKey((PublicKey)key);
        }
        if (key instanceof PrivateKey) {
            return this.implTranslatePrivateKey((PrivateKey)key);
        }
        throw new InvalidKeyException("Neither a public nor a private key");
    }

    @Override
    protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
        try {
            return this.implGeneratePublic(keySpec);
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
            return this.implGeneratePrivate(keySpec);
        }
        catch (InvalidKeySpecException e) {
            throw e;
        }
        catch (GeneralSecurityException e) {
            throw new InvalidKeySpecException(e);
        }
    }

    private PublicKey implTranslatePublicKey(PublicKey key) throws InvalidKeyException {
        if (key instanceof ECPublicKey) {
            if (key instanceof ECPublicKeyImpl) {
                return key;
            }
            ECPublicKey ecKey = (ECPublicKey)key;
            return new ECPublicKeyImpl(ecKey.getW(), ecKey.getParams());
        }
        if ("X.509".equals(key.getFormat())) {
            byte[] encoded = key.getEncoded();
            return new ECPublicKeyImpl(encoded);
        }
        throw new InvalidKeyException("Public keys must be instance of ECPublicKey or have X.509 encoding");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private PrivateKey implTranslatePrivateKey(PrivateKey key) throws InvalidKeyException {
        if (key instanceof ECPrivateKey) {
            if (key instanceof ECPrivateKeyImpl) {
                return key;
            }
            ECPrivateKey ecKey = (ECPrivateKey)key;
            return new ECPrivateKeyImpl(ecKey.getS(), ecKey.getParams());
        }
        if ("PKCS#8".equals(key.getFormat())) {
            byte[] encoded = key.getEncoded();
            try {
                ECPrivateKeyImpl eCPrivateKeyImpl = new ECPrivateKeyImpl(encoded);
                return eCPrivateKeyImpl;
            }
            finally {
                Arrays.fill(encoded, (byte)0);
            }
        }
        throw new InvalidKeyException("Private keys must be instance of ECPrivateKey or have PKCS#8 encoding");
    }

    private PublicKey implGeneratePublic(KeySpec keySpec) throws GeneralSecurityException {
        if (keySpec instanceof X509EncodedKeySpec) {
            X509EncodedKeySpec x509Spec = (X509EncodedKeySpec)keySpec;
            return new ECPublicKeyImpl(x509Spec.getEncoded());
        }
        if (keySpec instanceof ECPublicKeySpec) {
            ECPublicKeySpec ecSpec = (ECPublicKeySpec)keySpec;
            return new ECPublicKeyImpl(ecSpec.getW(), ecSpec.getParams());
        }
        throw new InvalidKeySpecException("Only ECPublicKeySpec and X509EncodedKeySpec supported for EC public keys");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private PrivateKey implGeneratePrivate(KeySpec keySpec) throws GeneralSecurityException {
        if (keySpec instanceof PKCS8EncodedKeySpec) {
            PKCS8EncodedKeySpec pkcsSpec = (PKCS8EncodedKeySpec)keySpec;
            byte[] encoded = pkcsSpec.getEncoded();
            try {
                ECPrivateKeyImpl eCPrivateKeyImpl = new ECPrivateKeyImpl(encoded);
                return eCPrivateKeyImpl;
            }
            finally {
                Arrays.fill(encoded, (byte)0);
            }
        }
        if (keySpec instanceof ECPrivateKeySpec) {
            ECPrivateKeySpec ecSpec = (ECPrivateKeySpec)keySpec;
            return new ECPrivateKeyImpl(ecSpec.getS(), ecSpec.getParams());
        }
        throw new InvalidKeySpecException("Only ECPrivateKeySpec and PKCS8EncodedKeySpec supported for EC private keys");
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
        if (key instanceof ECPublicKey) {
            ECPublicKey ecKey = (ECPublicKey)key;
            if (keySpec.isAssignableFrom(ECPublicKeySpec.class)) {
                return (T)((KeySpec)keySpec.cast(new ECPublicKeySpec(ecKey.getW(), ecKey.getParams())));
            }
            if (keySpec.isAssignableFrom(X509EncodedKeySpec.class)) {
                return (T)((KeySpec)keySpec.cast(new X509EncodedKeySpec(key.getEncoded())));
            }
            throw new InvalidKeySpecException("KeySpec must be ECPublicKeySpec or X509EncodedKeySpec for EC public keys");
        }
        if (key instanceof ECPrivateKey) {
            if (keySpec.isAssignableFrom(PKCS8EncodedKeySpec.class)) {
                byte[] encoded = key.getEncoded();
                try {
                    KeySpec keySpec2 = (KeySpec)keySpec.cast(new PKCS8EncodedKeySpec(encoded));
                    return (T)keySpec2;
                }
                finally {
                    Arrays.fill(encoded, (byte)0);
                }
            }
            if (keySpec.isAssignableFrom(ECPrivateKeySpec.class)) {
                ECPrivateKey ecKey = (ECPrivateKey)key;
                return (T)((KeySpec)keySpec.cast(new ECPrivateKeySpec(ecKey.getS(), ecKey.getParams())));
            }
            throw new InvalidKeySpecException("KeySpec must be ECPrivateKeySpec or PKCS8EncodedKeySpec for EC private keys");
        }
        throw new InvalidKeySpecException("Neither public nor private key");
    }
}

