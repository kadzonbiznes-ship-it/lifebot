/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import sun.security.jca.GetInstance;
import sun.security.util.Debug;

public class KeyFactory {
    private static final Debug debug = Debug.getInstance("jca", "KeyFactory");
    private final String algorithm;
    private Provider provider;
    private volatile KeyFactorySpi spi;
    private final Object lock = new Object();
    private Iterator<Provider.Service> serviceIterator;

    protected KeyFactory(KeyFactorySpi keyFacSpi, Provider provider, String algorithm) {
        this.spi = keyFacSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    private KeyFactory(String algorithm) throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
        List<Provider.Service> list = GetInstance.getServices("KeyFactory", algorithm);
        this.serviceIterator = list.iterator();
        if (this.nextSpi(null) == null) {
            throw new NoSuchAlgorithmException(algorithm + " KeyFactory not available");
        }
    }

    public static KeyFactory getInstance(String algorithm) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        return new KeyFactory(algorithm);
    }

    public static KeyFactory getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        GetInstance.Instance instance = GetInstance.getInstance("KeyFactory", KeyFactorySpi.class, algorithm, provider);
        return new KeyFactory((KeyFactorySpi)instance.impl, instance.provider, algorithm);
    }

    public static KeyFactory getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        GetInstance.Instance instance = GetInstance.getInstance("KeyFactory", KeyFactorySpi.class, algorithm, provider);
        return new KeyFactory((KeyFactorySpi)instance.impl, instance.provider, algorithm);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final Provider getProvider() {
        Object object = this.lock;
        synchronized (object) {
            this.serviceIterator = null;
            return this.provider;
        }
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private KeyFactorySpi nextSpi(KeyFactorySpi oldSpi) {
        Object object = this.lock;
        synchronized (object) {
            if (oldSpi != null && oldSpi != this.spi) {
                return this.spi;
            }
            if (this.serviceIterator == null) {
                return null;
            }
            while (this.serviceIterator.hasNext()) {
                Provider.Service s = this.serviceIterator.next();
                try {
                    Object obj = s.newInstance(null);
                    if (!(obj instanceof KeyFactorySpi)) continue;
                    KeyFactorySpi spi = (KeyFactorySpi)obj;
                    this.provider = s.getProvider();
                    this.spi = spi;
                    return spi;
                }
                catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                }
            }
            this.serviceIterator = null;
            return null;
        }
    }

    public final PublicKey generatePublic(KeySpec keySpec) throws InvalidKeySpecException {
        if (this.serviceIterator == null) {
            return this.spi.engineGeneratePublic(keySpec);
        }
        Exception failure = null;
        KeyFactorySpi mySpi = this.spi;
        while (true) {
            try {
                return mySpi.engineGeneratePublic(keySpec);
            }
            catch (Exception e) {
                if (failure != null) continue;
                failure = e;
                if ((mySpi = this.nextSpi(mySpi)) != null) continue;
                if (failure instanceof RuntimeException) {
                    throw (RuntimeException)failure;
                }
                if (failure instanceof InvalidKeySpecException) {
                    throw (InvalidKeySpecException)failure;
                }
                throw new InvalidKeySpecException("Could not generate public key", failure);
            }
            break;
        }
    }

    public final PrivateKey generatePrivate(KeySpec keySpec) throws InvalidKeySpecException {
        if (this.serviceIterator == null) {
            return this.spi.engineGeneratePrivate(keySpec);
        }
        Exception failure = null;
        KeyFactorySpi mySpi = this.spi;
        while (true) {
            try {
                return mySpi.engineGeneratePrivate(keySpec);
            }
            catch (Exception e) {
                if (failure != null) continue;
                failure = e;
                if ((mySpi = this.nextSpi(mySpi)) != null) continue;
                if (failure instanceof RuntimeException) {
                    throw (RuntimeException)failure;
                }
                if (failure instanceof InvalidKeySpecException) {
                    throw (InvalidKeySpecException)failure;
                }
                throw new InvalidKeySpecException("Could not generate private key", failure);
            }
            break;
        }
    }

    public final <T extends KeySpec> T getKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {
        if (this.serviceIterator == null) {
            return this.spi.engineGetKeySpec(key, keySpec);
        }
        Exception failure = null;
        KeyFactorySpi mySpi = this.spi;
        while (true) {
            try {
                return mySpi.engineGetKeySpec(key, keySpec);
            }
            catch (Exception e) {
                if (failure != null) continue;
                failure = e;
                if ((mySpi = this.nextSpi(mySpi)) != null) continue;
                if (failure instanceof RuntimeException) {
                    throw (RuntimeException)failure;
                }
                if (failure instanceof InvalidKeySpecException) {
                    throw (InvalidKeySpecException)failure;
                }
                throw new InvalidKeySpecException("Could not get key spec", failure);
            }
            break;
        }
    }

    public final Key translateKey(Key key) throws InvalidKeyException {
        if (this.serviceIterator == null) {
            return this.spi.engineTranslateKey(key);
        }
        Exception failure = null;
        KeyFactorySpi mySpi = this.spi;
        while (true) {
            try {
                return mySpi.engineTranslateKey(key);
            }
            catch (Exception e) {
                if (failure != null) continue;
                failure = e;
                if ((mySpi = this.nextSpi(mySpi)) != null) continue;
                if (failure instanceof RuntimeException) {
                    throw (RuntimeException)failure;
                }
                if (failure instanceof InvalidKeyException) {
                    throw (InvalidKeyException)failure;
                }
                throw new InvalidKeyException("Could not translate key", failure);
            }
            break;
        }
    }
}

