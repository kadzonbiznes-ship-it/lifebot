/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.crypto.JceSecurity;
import javax.crypto.KeyAgreementSpi;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import sun.security.jca.GetInstance;
import sun.security.jca.JCAUtil;
import sun.security.util.Debug;

public class KeyAgreement {
    private static final Debug debug = Debug.getInstance("jca", "KeyAgreement");
    private static final Debug pdebug = Debug.getInstance("provider", "Provider");
    private static final boolean skipDebug = Debug.isOn("engine=") && !Debug.isOn("keyagreement");
    private Provider provider;
    private KeyAgreementSpi spi;
    private final String algorithm;
    private Provider.Service firstService;
    private Iterator<Provider.Service> serviceIterator;
    private final Object lock;
    private static int warnCount = 10;
    private static final int I_NO_PARAMS = 1;
    private static final int I_PARAMS = 2;

    protected KeyAgreement(KeyAgreementSpi keyAgreeSpi, Provider provider, String algorithm) {
        this.spi = keyAgreeSpi;
        this.provider = provider;
        this.algorithm = algorithm;
        this.lock = null;
    }

    private KeyAgreement(Provider.Service s, Iterator<Provider.Service> t, String algorithm) {
        this.firstService = s;
        this.serviceIterator = t;
        this.algorithm = algorithm;
        this.lock = new Object();
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public static final KeyAgreement getInstance(String algorithm) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        List<Provider.Service> services = GetInstance.getServices("KeyAgreement", algorithm);
        Iterator<Provider.Service> t = services.iterator();
        while (t.hasNext()) {
            Provider.Service s = t.next();
            if (!JceSecurity.canUseProvider(s.getProvider())) continue;
            return new KeyAgreement(s, t, algorithm);
        }
        throw new NoSuchAlgorithmException("Algorithm " + algorithm + " not available");
    }

    public static final KeyAgreement getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        GetInstance.Instance instance = JceSecurity.getInstance("KeyAgreement", KeyAgreementSpi.class, algorithm, provider);
        return new KeyAgreement((KeyAgreementSpi)instance.impl, instance.provider, algorithm);
    }

    public static final KeyAgreement getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        GetInstance.Instance instance = JceSecurity.getInstance("KeyAgreement", KeyAgreementSpi.class, algorithm, provider);
        return new KeyAgreement((KeyAgreementSpi)instance.impl, instance.provider, algorithm);
    }

    void chooseFirstProvider() {
        if (this.spi != null) {
            return;
        }
        Object object = this.lock;
        synchronized (object) {
            int w;
            if (this.spi != null) {
                return;
            }
            if (debug != null && (w = --warnCount) >= 0) {
                debug.println("KeyAgreement.init() not first method called, disabling delayed provider selection");
                if (w == 0) {
                    debug.println("Further warnings of this type will be suppressed");
                }
                new Exception("Call trace").printStackTrace();
            }
            Exception lastException = null;
            while (this.firstService != null || this.serviceIterator.hasNext()) {
                Provider.Service s;
                if (this.firstService != null) {
                    s = this.firstService;
                    this.firstService = null;
                } else {
                    s = this.serviceIterator.next();
                }
                if (!JceSecurity.canUseProvider(s.getProvider())) continue;
                try {
                    Object obj = s.newInstance(null);
                    if (!(obj instanceof KeyAgreementSpi)) continue;
                    this.spi = (KeyAgreementSpi)obj;
                    this.provider = s.getProvider();
                    this.firstService = null;
                    this.serviceIterator = null;
                    return;
                }
                catch (Exception e) {
                    lastException = e;
                }
            }
            ProviderException e = new ProviderException("Could not construct KeyAgreementSpi instance");
            if (lastException != null) {
                e.initCause(lastException);
            }
            throw e;
        }
    }

    private void implInit(KeyAgreementSpi spi, int type, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (type == 1) {
            spi.engineInit(key, random);
        } else {
            spi.engineInit(key, params, random);
        }
    }

    private void chooseProvider(int initType, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        Object object = this.lock;
        synchronized (object) {
            if (this.spi != null) {
                this.implInit(this.spi, initType, key, params, random);
                return;
            }
            Exception lastException = null;
            while (this.firstService != null || this.serviceIterator.hasNext()) {
                Provider.Service s;
                if (this.firstService != null) {
                    s = this.firstService;
                    this.firstService = null;
                } else {
                    s = this.serviceIterator.next();
                }
                if (!s.supportsParameter(key) || !JceSecurity.canUseProvider(s.getProvider())) continue;
                try {
                    KeyAgreementSpi spi = (KeyAgreementSpi)s.newInstance(null);
                    this.implInit(spi, initType, key, params, random);
                    this.provider = s.getProvider();
                    this.spi = spi;
                    this.firstService = null;
                    this.serviceIterator = null;
                    return;
                }
                catch (Exception e) {
                    if (lastException != null) continue;
                    lastException = e;
                }
            }
            if (lastException instanceof InvalidKeyException) {
                throw (InvalidKeyException)lastException;
            }
            if (lastException instanceof InvalidAlgorithmParameterException) {
                throw (InvalidAlgorithmParameterException)lastException;
            }
            if (lastException instanceof RuntimeException) {
                throw (RuntimeException)lastException;
            }
            String kName = key != null ? key.getClass().getName() : "(null)";
            throw new InvalidKeyException("No installed provider supports this key: " + kName, lastException);
        }
    }

    public final Provider getProvider() {
        this.chooseFirstProvider();
        return this.provider;
    }

    public final void init(Key key) throws InvalidKeyException {
        this.init(key, JCAUtil.getDefSecureRandom());
    }

    public final void init(Key key, SecureRandom random) throws InvalidKeyException {
        if (this.spi != null) {
            this.spi.engineInit(key, random);
        } else {
            try {
                this.chooseProvider(1, key, null, random);
            }
            catch (InvalidAlgorithmParameterException e) {
                throw new InvalidKeyException(e);
            }
        }
        if (!skipDebug && pdebug != null) {
            pdebug.println("KeyAgreement." + this.algorithm + " algorithm from: " + this.getProviderName());
        }
    }

    public final void init(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.init(key, params, JCAUtil.getDefSecureRandom());
    }

    private String getProviderName() {
        return this.provider == null ? "(no provider)" : this.provider.getName();
    }

    public final void init(Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (this.spi != null) {
            this.spi.engineInit(key, params, random);
        } else {
            this.chooseProvider(2, key, params, random);
        }
        if (!skipDebug && pdebug != null) {
            pdebug.println("KeyAgreement." + this.algorithm + " algorithm from: " + this.getProviderName());
        }
    }

    public final Key doPhase(Key key, boolean lastPhase) throws InvalidKeyException, IllegalStateException {
        this.chooseFirstProvider();
        return this.spi.engineDoPhase(key, lastPhase);
    }

    public final byte[] generateSecret() throws IllegalStateException {
        this.chooseFirstProvider();
        return this.spi.engineGenerateSecret();
    }

    public final int generateSecret(byte[] sharedSecret, int offset) throws IllegalStateException, ShortBufferException {
        this.chooseFirstProvider();
        return this.spi.engineGenerateSecret(sharedSecret, offset);
    }

    public final SecretKey generateSecret(String algorithm) throws IllegalStateException, NoSuchAlgorithmException, InvalidKeyException {
        this.chooseFirstProvider();
        return this.spi.engineGenerateSecret(algorithm);
    }
}

