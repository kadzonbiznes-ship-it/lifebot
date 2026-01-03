/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import sun.security.jca.GetInstance;
import sun.security.jca.JCAUtil;
import sun.security.util.Debug;

public abstract class KeyPairGenerator
extends KeyPairGeneratorSpi {
    private static final Debug pdebug = Debug.getInstance("provider", "Provider");
    private static final boolean skipDebug = Debug.isOn("engine=") && !Debug.isOn("keypairgenerator");
    private final String algorithm;
    Provider provider;

    protected KeyPairGenerator(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    private static KeyPairGenerator getInstance(GetInstance.Instance instance, String algorithm) {
        KeyPairGenerator kpg;
        if (instance.impl instanceof KeyPairGenerator) {
            kpg = (KeyPairGenerator)instance.impl;
        } else {
            KeyPairGeneratorSpi spi = (KeyPairGeneratorSpi)instance.impl;
            kpg = new Delegate(spi, algorithm);
        }
        kpg.provider = instance.provider;
        if (!skipDebug && pdebug != null) {
            pdebug.println("KeyPairGenerator." + algorithm + " algorithm from: " + kpg.provider.getName());
        }
        return kpg;
    }

    public static KeyPairGenerator getInstance(String algorithm) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        List<Provider.Service> list = GetInstance.getServices("KeyPairGenerator", algorithm);
        Iterator<Provider.Service> t = list.iterator();
        if (!t.hasNext()) {
            throw new NoSuchAlgorithmException(algorithm + " KeyPairGenerator not available");
        }
        NoSuchAlgorithmException failure = null;
        while (true) {
            Provider.Service s = t.next();
            try {
                GetInstance.Instance instance = GetInstance.getInstance(s, KeyPairGeneratorSpi.class);
                if (instance.impl instanceof KeyPairGenerator) {
                    return KeyPairGenerator.getInstance(instance, algorithm);
                }
                return new Delegate(instance, t, algorithm);
            }
            catch (NoSuchAlgorithmException e) {
                if (failure != null) continue;
                failure = e;
                if (t.hasNext()) continue;
                throw failure;
            }
            break;
        }
    }

    public static KeyPairGenerator getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        GetInstance.Instance instance = GetInstance.getInstance("KeyPairGenerator", KeyPairGeneratorSpi.class, algorithm, provider);
        return KeyPairGenerator.getInstance(instance, algorithm);
    }

    public static KeyPairGenerator getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        GetInstance.Instance instance = GetInstance.getInstance("KeyPairGenerator", KeyPairGeneratorSpi.class, algorithm, provider);
        return KeyPairGenerator.getInstance(instance, algorithm);
    }

    public final Provider getProvider() {
        this.disableFailover();
        return this.provider;
    }

    void disableFailover() {
    }

    public void initialize(int keysize) {
        this.initialize(keysize, JCAUtil.getDefSecureRandom());
    }

    @Override
    public void initialize(int keysize, SecureRandom random) {
    }

    public void initialize(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        this.initialize(params, JCAUtil.getDefSecureRandom());
    }

    @Override
    public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
    }

    public final KeyPair genKeyPair() {
        return this.generateKeyPair();
    }

    @Override
    public KeyPair generateKeyPair() {
        return null;
    }

    private static final class Delegate
    extends KeyPairGenerator {
        private volatile KeyPairGeneratorSpi spi;
        private final Object lock = new Object();
        private Iterator<Provider.Service> serviceIterator;
        private static final int I_NONE = 1;
        private static final int I_SIZE = 2;
        private static final int I_PARAMS = 3;
        private int initType;
        private int initKeySize;
        private AlgorithmParameterSpec initParams;
        private SecureRandom initRandom;

        Delegate(KeyPairGeneratorSpi spi, String algorithm) {
            super(algorithm);
            this.spi = spi;
        }

        Delegate(GetInstance.Instance instance, Iterator<Provider.Service> serviceIterator, String algorithm) {
            super(algorithm);
            this.spi = (KeyPairGeneratorSpi)instance.impl;
            this.provider = instance.provider;
            this.serviceIterator = serviceIterator;
            this.initType = 1;
            if (!skipDebug && pdebug != null) {
                pdebug.println("KeyPairGenerator." + algorithm + " algorithm from: " + this.provider.getName());
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private KeyPairGeneratorSpi nextSpi(KeyPairGeneratorSpi oldSpi, boolean reinit) {
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
                        Object inst = s.newInstance(null);
                        if (!(inst instanceof KeyPairGeneratorSpi)) continue;
                        KeyPairGeneratorSpi spi = (KeyPairGeneratorSpi)inst;
                        if (inst instanceof KeyPairGenerator) continue;
                        if (reinit) {
                            if (this.initType == 2) {
                                spi.initialize(this.initKeySize, this.initRandom);
                            } else if (this.initType == 3) {
                                spi.initialize(this.initParams, this.initRandom);
                            } else if (this.initType != 1) {
                                throw new AssertionError((Object)("KeyPairGenerator initType: " + this.initType));
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

        @Override
        void disableFailover() {
            this.serviceIterator = null;
            this.initType = 0;
            this.initParams = null;
            this.initRandom = null;
        }

        @Override
        public void initialize(int keysize, SecureRandom random) {
            if (this.serviceIterator == null) {
                this.spi.initialize(keysize, random);
                return;
            }
            RuntimeException failure = null;
            KeyPairGeneratorSpi mySpi = this.spi;
            while (true) {
                try {
                    mySpi.initialize(keysize, random);
                    this.initType = 2;
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

        @Override
        public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
            if (this.serviceIterator == null) {
                this.spi.initialize(params, random);
                return;
            }
            Exception failure = null;
            KeyPairGeneratorSpi mySpi = this.spi;
            while (true) {
                try {
                    mySpi.initialize(params, random);
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
                    if (failure instanceof RuntimeException) {
                        throw (RuntimeException)failure;
                    }
                    throw (InvalidAlgorithmParameterException)failure;
                }
                break;
            }
        }

        @Override
        public KeyPair generateKeyPair() {
            if (this.serviceIterator == null) {
                return this.spi.generateKeyPair();
            }
            RuntimeException failure = null;
            KeyPairGeneratorSpi mySpi = this.spi;
            while (true) {
                try {
                    return mySpi.generateKeyPair();
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
}

