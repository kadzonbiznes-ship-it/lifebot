/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.crypto.JceSecurity;
import javax.crypto.MacSpi;
import javax.crypto.ShortBufferException;
import sun.security.jca.GetInstance;
import sun.security.util.Debug;

public class Mac
implements Cloneable {
    private static final Debug debug = Debug.getInstance("jca", "Mac");
    private static final Debug pdebug = Debug.getInstance("provider", "Provider");
    private static final boolean skipDebug = Debug.isOn("engine=") && !Debug.isOn("mac");
    private Provider provider;
    private MacSpi spi;
    private final String algorithm;
    private boolean initialized = false;
    private Provider.Service firstService;
    private Iterator<Provider.Service> serviceIterator;
    private final Object lock;
    private static int warnCount = 10;

    protected Mac(MacSpi macSpi, Provider provider, String algorithm) {
        this.spi = macSpi;
        this.provider = provider;
        this.algorithm = algorithm;
        this.serviceIterator = null;
        this.lock = null;
    }

    private Mac(Provider.Service s, Iterator<Provider.Service> t, String algorithm) {
        this.firstService = s;
        this.serviceIterator = t;
        this.algorithm = algorithm;
        this.lock = new Object();
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public static final Mac getInstance(String algorithm) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        List<Provider.Service> services = GetInstance.getServices("Mac", algorithm);
        Iterator<Provider.Service> t = services.iterator();
        while (t.hasNext()) {
            Provider.Service s = t.next();
            if (!JceSecurity.canUseProvider(s.getProvider())) continue;
            return new Mac(s, t, algorithm);
        }
        throw new NoSuchAlgorithmException("Algorithm " + algorithm + " not available");
    }

    public static final Mac getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        GetInstance.Instance instance = JceSecurity.getInstance("Mac", MacSpi.class, algorithm, provider);
        return new Mac((MacSpi)instance.impl, instance.provider, algorithm);
    }

    public static final Mac getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        GetInstance.Instance instance = JceSecurity.getInstance("Mac", MacSpi.class, algorithm, provider);
        return new Mac((MacSpi)instance.impl, instance.provider, algorithm);
    }

    void chooseFirstProvider() {
        if (this.spi != null || this.serviceIterator == null) {
            return;
        }
        Object object = this.lock;
        synchronized (object) {
            int w;
            if (this.spi != null) {
                return;
            }
            if (debug != null && (w = --warnCount) >= 0) {
                debug.println("Mac.init() not first method called, disabling delayed provider selection");
                if (w == 0) {
                    debug.println("Further warnings of this type will be suppressed");
                }
                new Exception("Call trace").printStackTrace();
            }
            NoSuchAlgorithmException lastException = null;
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
                    if (!(obj instanceof MacSpi)) continue;
                    this.spi = (MacSpi)obj;
                    this.provider = s.getProvider();
                    this.firstService = null;
                    this.serviceIterator = null;
                    return;
                }
                catch (NoSuchAlgorithmException e) {
                    lastException = e;
                }
            }
            ProviderException e = new ProviderException("Could not construct MacSpi instance");
            if (lastException != null) {
                e.initCause(lastException);
            }
            throw e;
        }
    }

    private void chooseProvider(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        Object object = this.lock;
        synchronized (object) {
            if (this.spi != null) {
                this.spi.engineInit(key, params);
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
                    MacSpi spi = (MacSpi)s.newInstance(null);
                    spi.engineInit(key, params);
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

    public final int getMacLength() {
        this.chooseFirstProvider();
        return this.spi.engineGetMacLength();
    }

    private String getProviderName() {
        return this.provider == null ? "(no provider)" : this.provider.getName();
    }

    public final void init(Key key) throws InvalidKeyException {
        try {
            if (this.spi != null) {
                this.spi.engineInit(key, null);
            } else {
                this.chooseProvider(key, null);
            }
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException("init() failed", e);
        }
        this.initialized = true;
        if (!skipDebug && pdebug != null) {
            pdebug.println("Mac." + this.algorithm + " algorithm from: " + this.getProviderName());
        }
    }

    public final void init(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (this.spi != null) {
            this.spi.engineInit(key, params);
        } else {
            this.chooseProvider(key, params);
        }
        this.initialized = true;
        if (!skipDebug && pdebug != null) {
            pdebug.println("Mac." + this.algorithm + " algorithm from: " + this.getProviderName());
        }
    }

    public final void update(byte input) throws IllegalStateException {
        this.chooseFirstProvider();
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        }
        this.spi.engineUpdate(input);
    }

    public final void update(byte[] input) throws IllegalStateException {
        this.chooseFirstProvider();
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        }
        if (input != null) {
            this.spi.engineUpdate(input, 0, input.length);
        }
    }

    public final void update(byte[] input, int offset, int len) throws IllegalStateException {
        this.chooseFirstProvider();
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        }
        if (input != null) {
            if (offset < 0 || len > input.length - offset || len < 0) {
                throw new IllegalArgumentException("Bad arguments");
            }
            this.spi.engineUpdate(input, offset, len);
        }
    }

    public final void update(ByteBuffer input) {
        this.chooseFirstProvider();
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        }
        if (input == null) {
            throw new IllegalArgumentException("Buffer must not be null");
        }
        this.spi.engineUpdate(input);
    }

    public final byte[] doFinal() throws IllegalStateException {
        this.chooseFirstProvider();
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        }
        byte[] mac = this.spi.engineDoFinal();
        this.spi.engineReset();
        return mac;
    }

    public final void doFinal(byte[] output, int outOffset) throws ShortBufferException, IllegalStateException {
        this.chooseFirstProvider();
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        }
        int macLen = this.getMacLength();
        if (output == null || output.length - outOffset < macLen) {
            throw new ShortBufferException("Cannot store MAC in output buffer");
        }
        byte[] mac = this.doFinal();
        System.arraycopy(mac, 0, output, outOffset, macLen);
    }

    public final byte[] doFinal(byte[] input) throws IllegalStateException {
        this.chooseFirstProvider();
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        }
        this.update(input);
        return this.doFinal();
    }

    public final void reset() {
        this.chooseFirstProvider();
        this.spi.engineReset();
    }

    public final Object clone() throws CloneNotSupportedException {
        this.chooseFirstProvider();
        Mac that = (Mac)super.clone();
        that.spi = (MacSpi)this.spi.clone();
        return that;
    }
}

