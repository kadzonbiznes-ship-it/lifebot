/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.crypto.JceSecurity;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;
import sun.security.jca.GetInstance;
import sun.security.jca.JCAUtil;
import sun.security.util.Debug;

public class KeyGenerator {
    private static final Debug pdebug = Debug.getInstance("provider", "Provider");
    private static final boolean skipDebug = Debug.isOn("engine=") && !Debug.isOn("keygenerator");
    private static final int I_NONE = 1;
    private static final int I_RANDOM = 2;
    private static final int I_PARAMS = 3;
    private static final int I_SIZE = 4;
    private Provider provider;
    private volatile KeyGeneratorSpi spi;
    private final String algorithm;
    private final Object lock = new Object();
    private Iterator<Provider.Service> serviceIterator;
    private int initType;
    private int initKeySize;
    private AlgorithmParameterSpec initParams;
    private SecureRandom initRandom;

    protected KeyGenerator(KeyGeneratorSpi keyGenSpi, Provider provider, String algorithm) {
        this.spi = keyGenSpi;
        this.provider = provider;
        this.algorithm = algorithm;
        if (!skipDebug && pdebug != null) {
            pdebug.println("KeyGenerator." + algorithm + " algorithm from: " + this.getProviderName());
        }
    }

    private KeyGenerator(String algorithm) throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
        List<Provider.Service> list = GetInstance.getServices("KeyGenerator", algorithm);
        this.serviceIterator = list.iterator();
        this.initType = 1;
        if (this.nextSpi(null, false) == null) {
            throw new NoSuchAlgorithmException(algorithm + " KeyGenerator not available");
        }
        if (!skipDebug && pdebug != null) {
            pdebug.println("KeyGenerator." + algorithm + " algorithm from: " + this.getProviderName());
        }
    }

    private String getProviderName() {
        return this.provider == null ? "(no provider)" : this.provider.getName();
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public static final KeyGenerator getInstance(String algorithm) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        return new KeyGenerator(algorithm);
    }

    public static final KeyGenerator getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        GetInstance.Instance instance = JceSecurity.getInstance("KeyGenerator", KeyGeneratorSpi.class, algorithm, provider);
        return new KeyGenerator((KeyGeneratorSpi)instance.impl, instance.provider, algorithm);
    }

    public static final KeyGenerator getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        GetInstance.Instance instance = JceSecurity.getInstance("KeyGenerator", KeyGeneratorSpi.class, algorithm, provider);
        return new KeyGenerator((KeyGeneratorSpi)instance.impl, instance.provider, algorithm);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final Provider getProvider() {
        Object object = this.lock;
        synchronized (object) {
            this.disableFailover();
            return this.provider;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private KeyGeneratorSpi nextSpi(KeyGeneratorSpi oldSpi, boolean reinit) {
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
                if (!JceSecurity.canUseProvider(s.getProvider())) continue;
                try {
                    Object inst = s.newInstance(null);
                    if (!(inst instanceof KeyGeneratorSpi)) continue;
                    KeyGeneratorSpi spi = (KeyGeneratorSpi)inst;
                    if (reinit) {
                        if (this.initType == 4) {
                            spi.engineInit(this.initKeySize, this.initRandom);
                        } else if (this.initType == 3) {
                            spi.engineInit(this.initParams, this.initRandom);
                        } else if (this.initType == 2) {
                            spi.engineInit(this.initRandom);
                        } else if (this.initType != 1) {
                            throw new AssertionError((Object)("KeyGenerator initType: " + this.initType));
                        }
                    }
                    this.provider = s.getProvider();
                    this.spi = spi;
                    return spi;
                }
                catch (Exception exception) {
                }
            }
            this.disableFailover();
            return null;
        }
    }

    void disableFailover() {
        this.serviceIterator = null;
        this.initType = 0;
        this.initParams = null;
        this.initRandom = null;
    }

    public final void init(SecureRandom random) {
        if (this.serviceIterator == null) {
            this.spi.engineInit(random);
            return;
        }
        RuntimeException failure = null;
        KeyGeneratorSpi mySpi = this.spi;
        while (true) {
            try {
                mySpi.engineInit(random);
                this.initType = 2;
                this.initKeySize = 0;
                this.initParams = null;
                this.initRandom = random;
                return;
            }
            catch (RuntimeException e) {
                if (failure != null) continue;
                failure = e;
                if ((mySpi = this.nextSpi(mySpi, false)) != null) continue;
                throw failure;
            }
            break;
        }
    }

    public final void init(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        this.init(params, JCAUtil.getDefSecureRandom());
    }

    public final void init(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        if (this.serviceIterator == null) {
            this.spi.engineInit(params, random);
            return;
        }
        Exception failure = null;
        KeyGeneratorSpi mySpi = this.spi;
        while (true) {
            try {
                mySpi.engineInit(params, random);
                this.initType = 3;
                this.initKeySize = 0;
                this.initParams = params;
                this.initRandom = random;
                return;
            }
            catch (Exception e) {
                if (failure != null) continue;
                failure = e;
                if ((mySpi = this.nextSpi(mySpi, false)) != null) continue;
                if (failure instanceof InvalidAlgorithmParameterException) {
                    throw (InvalidAlgorithmParameterException)failure;
                }
                throw (RuntimeException)failure;
            }
            break;
        }
    }

    public final void init(int keysize) {
        this.init(keysize, JCAUtil.getDefSecureRandom());
    }

    public final void init(int keysize, SecureRandom random) {
        if (this.serviceIterator == null) {
            this.spi.engineInit(keysize, random);
            return;
        }
        RuntimeException failure = null;
        KeyGeneratorSpi mySpi = this.spi;
        while (true) {
            try {
                mySpi.engineInit(keysize, random);
                this.initType = 4;
                this.initKeySize = keysize;
                this.initParams = null;
                this.initRandom = random;
                return;
            }
            catch (RuntimeException e) {
                if (failure != null) continue;
                failure = e;
                if ((mySpi = this.nextSpi(mySpi, false)) != null) continue;
                throw failure;
            }
            break;
        }
    }

    public final SecretKey generateKey() {
        if (this.serviceIterator == null) {
            return this.spi.engineGenerateKey();
        }
        RuntimeException failure = null;
        KeyGeneratorSpi mySpi = this.spi;
        while (true) {
            try {
                return mySpi.engineGenerateKey();
            }
            catch (RuntimeException e) {
                if (failure != null) continue;
                failure = e;
                if ((mySpi = this.nextSpi(mySpi, true)) != null) continue;
                throw failure;
            }
            break;
        }
    }
}

