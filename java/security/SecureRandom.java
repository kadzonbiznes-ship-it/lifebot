/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandomParameters;
import java.security.SecureRandomSpi;
import java.security.Security;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.internal.util.random.RandomSupport;
import sun.security.jca.GetInstance;
import sun.security.jca.Providers;
import sun.security.provider.SunEntries;
import sun.security.util.Debug;

@RandomSupport.RandomGeneratorProperties(name="SecureRandom", isStochastic=true)
public class SecureRandom
extends Random {
    private static final Debug pdebug = Debug.getInstance("provider", "Provider");
    private static final boolean skipDebug = Debug.isOn("engine=") && !Debug.isOn("securerandom");
    private Provider provider = null;
    private SecureRandomSpi secureRandomSpi = null;
    private final boolean threadSafe;
    private String algorithm;
    private static volatile SecureRandom seedGenerator;
    static final long serialVersionUID = 4940670005562187L;
    private byte[] state;
    private MessageDigest digest = null;
    private byte[] randomBytes;
    private int randomBytesUsed;
    private long counter;

    public SecureRandom() {
        super(0L);
        this.getDefaultPRNG(false, null);
        this.threadSafe = this.getThreadSafe();
    }

    private boolean getThreadSafe() {
        if (this.provider == null || this.algorithm == null) {
            return false;
        }
        return Boolean.parseBoolean(this.provider.getProperty("SecureRandom." + this.algorithm + " ThreadSafe", "false"));
    }

    public SecureRandom(byte[] seed) {
        super(0L);
        Objects.requireNonNull(seed);
        this.getDefaultPRNG(true, seed);
        this.threadSafe = this.getThreadSafe();
    }

    private void getDefaultPRNG(boolean setSeed, byte[] seed) {
        Provider.Service prngService = null;
        String prngAlgorithm = null;
        for (Provider p : Providers.getProviderList().providers()) {
            if (p.getName().equals("SUN")) {
                prngAlgorithm = SunEntries.DEF_SECURE_RANDOM_ALGO;
                prngService = p.getService("SecureRandom", prngAlgorithm);
                break;
            }
            prngService = p.getDefaultSecureRandomService();
            if (prngService == null) continue;
            prngAlgorithm = prngService.getAlgorithm();
            break;
        }
        if (prngService == null) {
            prngAlgorithm = "SHA1PRNG";
            this.secureRandomSpi = new sun.security.provider.SecureRandom();
            this.provider = Providers.getSunProvider();
        } else {
            try {
                this.secureRandomSpi = (SecureRandomSpi)prngService.newInstance(null);
                this.provider = prngService.getProvider();
            }
            catch (NoSuchAlgorithmException nsae) {
                throw new RuntimeException(nsae);
            }
        }
        if (setSeed) {
            this.secureRandomSpi.engineSetSeed(seed);
        }
        if (this.getClass() == SecureRandom.class) {
            this.algorithm = prngAlgorithm;
        }
    }

    protected SecureRandom(SecureRandomSpi secureRandomSpi, Provider provider) {
        this(secureRandomSpi, provider, null);
    }

    private SecureRandom(SecureRandomSpi secureRandomSpi, Provider provider, String algorithm) {
        super(0L);
        this.secureRandomSpi = secureRandomSpi;
        this.provider = provider;
        this.algorithm = algorithm;
        this.threadSafe = this.getThreadSafe();
        if (!skipDebug && pdebug != null) {
            pdebug.println("SecureRandom." + algorithm + " algorithm from: " + this.getProviderName());
        }
    }

    private String getProviderName() {
        return this.provider == null ? "(no provider)" : this.provider.getName();
    }

    public static SecureRandom getInstance(String algorithm) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        GetInstance.Instance instance = GetInstance.getInstance("SecureRandom", SecureRandomSpi.class, algorithm);
        return new SecureRandom((SecureRandomSpi)instance.impl, instance.provider, algorithm);
    }

    public static SecureRandom getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        GetInstance.Instance instance = GetInstance.getInstance("SecureRandom", SecureRandomSpi.class, algorithm, provider);
        return new SecureRandom((SecureRandomSpi)instance.impl, instance.provider, algorithm);
    }

    public static SecureRandom getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        GetInstance.Instance instance = GetInstance.getInstance("SecureRandom", SecureRandomSpi.class, algorithm, provider);
        return new SecureRandom((SecureRandomSpi)instance.impl, instance.provider, algorithm);
    }

    public static SecureRandom getInstance(String algorithm, SecureRandomParameters params) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        if (params == null) {
            throw new IllegalArgumentException("params cannot be null");
        }
        GetInstance.Instance instance = GetInstance.getInstance("SecureRandom", SecureRandomSpi.class, algorithm, params);
        return new SecureRandom((SecureRandomSpi)instance.impl, instance.provider, algorithm);
    }

    public static SecureRandom getInstance(String algorithm, SecureRandomParameters params, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        if (params == null) {
            throw new IllegalArgumentException("params cannot be null");
        }
        GetInstance.Instance instance = GetInstance.getInstance("SecureRandom", SecureRandomSpi.class, algorithm, (Object)params, provider);
        return new SecureRandom((SecureRandomSpi)instance.impl, instance.provider, algorithm);
    }

    public static SecureRandom getInstance(String algorithm, SecureRandomParameters params, Provider provider) throws NoSuchAlgorithmException {
        Objects.requireNonNull(algorithm, "null algorithm name");
        if (params == null) {
            throw new IllegalArgumentException("params cannot be null");
        }
        GetInstance.Instance instance = GetInstance.getInstance("SecureRandom", SecureRandomSpi.class, algorithm, (Object)params, provider);
        return new SecureRandom((SecureRandomSpi)instance.impl, instance.provider, algorithm);
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public String getAlgorithm() {
        return Objects.toString(this.algorithm, "unknown");
    }

    public String toString() {
        return this.secureRandomSpi.toString();
    }

    public SecureRandomParameters getParameters() {
        return this.secureRandomSpi.engineGetParameters();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setSeed(byte[] seed) {
        Objects.requireNonNull(seed);
        if (this.threadSafe) {
            this.secureRandomSpi.engineSetSeed(seed);
        } else {
            SecureRandom secureRandom = this;
            synchronized (secureRandom) {
                this.secureRandomSpi.engineSetSeed(seed);
            }
        }
    }

    @Override
    public void setSeed(long seed) {
        if (seed != 0L) {
            this.setSeed(SecureRandom.longToByteArray(seed));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void nextBytes(byte[] bytes) {
        Objects.requireNonNull(bytes);
        if (this.threadSafe) {
            this.secureRandomSpi.engineNextBytes(bytes);
        } else {
            SecureRandom secureRandom = this;
            synchronized (secureRandom) {
                this.secureRandomSpi.engineNextBytes(bytes);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void nextBytes(byte[] bytes, SecureRandomParameters params) {
        if (params == null) {
            throw new IllegalArgumentException("params cannot be null");
        }
        Objects.requireNonNull(bytes);
        if (this.threadSafe) {
            this.secureRandomSpi.engineNextBytes(bytes, params);
        } else {
            SecureRandom secureRandom = this;
            synchronized (secureRandom) {
                this.secureRandomSpi.engineNextBytes(bytes, params);
            }
        }
    }

    @Override
    protected final int next(int numBits) {
        int numBytes = (numBits + 7) / 8;
        byte[] b = new byte[numBytes];
        int next = 0;
        this.nextBytes(b);
        for (int i = 0; i < numBytes; ++i) {
            next = (next << 8) + (b[i] & 0xFF);
        }
        return next >>> numBytes * 8 - numBits;
    }

    public static byte[] getSeed(int numBytes) {
        SecureRandom seedGen = seedGenerator;
        if (seedGen == null) {
            seedGenerator = seedGen = new SecureRandom();
        }
        return seedGen.generateSeed(numBytes);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public byte[] generateSeed(int numBytes) {
        if (numBytes < 0) {
            throw new IllegalArgumentException("numBytes cannot be negative");
        }
        if (this.threadSafe) {
            return this.secureRandomSpi.engineGenerateSeed(numBytes);
        }
        SecureRandom secureRandom = this;
        synchronized (secureRandom) {
            return this.secureRandomSpi.engineGenerateSeed(numBytes);
        }
    }

    private static byte[] longToByteArray(long l) {
        byte[] retVal = new byte[8];
        for (int i = 0; i < 8; ++i) {
            retVal[i] = (byte)l;
            l >>= 8;
        }
        return retVal;
    }

    public static SecureRandom getInstanceStrong() throws NoSuchAlgorithmException {
        String property = AccessController.doPrivileged(() -> Security.getProperty("securerandom.strongAlgorithms"));
        if (property == null || property.isEmpty()) {
            throw new NoSuchAlgorithmException("Null/empty securerandom.strongAlgorithms Security Property");
        }
        String remainder = property;
        while (remainder != null) {
            Matcher m = StrongPatternHolder.pattern.matcher(remainder);
            if (m.matches()) {
                String alg = m.group(1);
                String prov = m.group(3);
                try {
                    if (prov == null) {
                        return SecureRandom.getInstance(alg);
                    }
                    return SecureRandom.getInstance(alg, prov);
                }
                catch (NoSuchAlgorithmException | NoSuchProviderException generalSecurityException) {
                    remainder = m.group(5);
                    continue;
                }
            }
            remainder = null;
        }
        throw new NoSuchAlgorithmException("No strong SecureRandom impls available: " + property);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void reseed() {
        if (this.threadSafe) {
            this.secureRandomSpi.engineReseed(null);
        } else {
            SecureRandom secureRandom = this;
            synchronized (secureRandom) {
                this.secureRandomSpi.engineReseed(null);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void reseed(SecureRandomParameters params) {
        if (params == null) {
            throw new IllegalArgumentException("params cannot be null");
        }
        if (this.threadSafe) {
            this.secureRandomSpi.engineReseed(params);
        } else {
            SecureRandom secureRandom = this;
            synchronized (secureRandom) {
                this.secureRandomSpi.engineReseed(params);
            }
        }
    }

    private static final class StrongPatternHolder {
        private static final Pattern pattern = Pattern.compile("\\s*([\\S&&[^:,]]*)(:([\\S&&[^,]]*))?\\s*(,(.*))?");

        private StrongPatternHolder() {
        }
    }
}

