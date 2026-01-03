/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider;

import java.security.DrbgParameters;
import java.security.SecureRandomParameters;
import java.util.Arrays;
import sun.security.provider.EntropySource;
import sun.security.provider.HashDrbg;
import sun.security.provider.MoreDrbgParameters;
import sun.security.provider.SeedGenerator;
import sun.security.util.Debug;

public abstract class AbstractDrbg {
    protected static final Debug debug = Debug.getInstance("securerandom", "drbg");
    private boolean instantiated;
    protected volatile int reseedCounter;
    protected static final int DEFAULT_STRENGTH = 128;
    protected String mechName = "DRBG";
    protected int highestSupportedSecurityStrength = 256;
    protected boolean supportPredictionResistance = true;
    protected boolean supportReseeding = true;
    protected int minLength;
    protected int maxLength = Integer.MAX_VALUE;
    protected int maxPersonalizationStringLength = Integer.MAX_VALUE;
    protected int maxAdditionalInputLength = Integer.MAX_VALUE;
    protected int maxNumberOfBytesPerRequest = Integer.MAX_VALUE;
    protected int reseedInterval = Integer.MAX_VALUE;
    protected String algorithm;
    protected int securityStrength;
    protected int requestedInstantiationSecurityStrength = -1;
    protected byte[] personalizationString;
    private boolean predictionResistanceFlag;
    protected boolean usedf;
    protected byte[] nonce;
    private byte[] requestedNonce;
    protected String requestedAlgorithm;
    private EntropySource es;
    private static final EntropySource defaultES = (minE, minLen, maxLen, pr) -> {
        byte[] result = new byte[minLen];
        SeedGenerator.generateSeed(result);
        return result;
    };

    protected abstract void chooseAlgorithmAndStrength();

    protected abstract void initEngine();

    protected abstract void instantiateAlgorithm(byte[] var1);

    protected abstract void generateAlgorithm(byte[] var1, byte[] var2);

    protected void reseedAlgorithm(byte[] ei, byte[] additionalInput) {
        throw new UnsupportedOperationException("No reseed function");
    }

    protected final void engineNextBytes(byte[] result) {
        this.engineNextBytes(result, DrbgParameters.nextBytes(-1, this.predictionResistanceFlag, null));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void engineNextBytes(byte[] result, SecureRandomParameters params) {
        byte[] ai;
        if (debug != null) {
            debug.println(this, "nextBytes");
        }
        if (params instanceof DrbgParameters.NextBytes) {
            DrbgParameters.NextBytes dp = (DrbgParameters.NextBytes)params;
            if (result.length > this.maxNumberOfBytesPerRequest) {
                // empty if block
            }
            if (dp.getStrength() > this.securityStrength) {
                throw new IllegalArgumentException("strength too high: " + dp.getStrength());
            }
            ai = dp.getAdditionalInput();
            if (ai != null && ai.length > this.maxAdditionalInputLength) {
                throw new IllegalArgumentException("ai too long: " + ai.length);
            }
            boolean pr = dp.getPredictionResistance();
            if (!this.predictionResistanceFlag && pr) {
                throw new IllegalArgumentException("pr not available");
            }
            this.instantiateIfNecessary(null);
            if (this.reseedCounter < 0 || this.reseedCounter > this.reseedInterval || pr) {
                AbstractDrbg abstractDrbg = this;
                synchronized (abstractDrbg) {
                    if (this.reseedCounter < 0 || this.reseedCounter > this.reseedInterval || pr) {
                        this.reseedAlgorithm(this.getEntropyInput(pr), ai);
                        ai = null;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("unknown params type:" + params.getClass());
        }
        this.generateAlgorithm(result, ai);
    }

    public final void engineReseed(SecureRandomParameters params) {
        byte[] ai;
        boolean pr;
        if (debug != null) {
            debug.println(this, "reseed with params");
        }
        if (!this.supportReseeding) {
            throw new UnsupportedOperationException("Reseed not supported");
        }
        if (params == null) {
            params = DrbgParameters.reseed(this.predictionResistanceFlag, null);
        }
        if (params instanceof DrbgParameters.Reseed) {
            DrbgParameters.Reseed dp = (DrbgParameters.Reseed)params;
            pr = dp.getPredictionResistance();
            if (!this.predictionResistanceFlag && pr) {
                throw new IllegalArgumentException("pr not available");
            }
            ai = dp.getAdditionalInput();
            if (ai != null && ai.length > this.maxAdditionalInputLength) {
                throw new IllegalArgumentException("ai too long: " + ai.length);
            }
        } else {
            throw new IllegalArgumentException("unknown params type: " + params.getClass());
        }
        this.instantiateIfNecessary(null);
        this.reseedAlgorithm(this.getEntropyInput(pr), ai);
    }

    public final byte[] engineGenerateSeed(int numBytes) {
        byte[] b = new byte[numBytes];
        SeedGenerator.generateSeed(b);
        return b;
    }

    public final synchronized void engineSetSeed(byte[] input) {
        if (debug != null) {
            debug.println(this, "setSeed");
        }
        if (input.length < this.minLength) {
            input = Arrays.copyOf(input, this.minLength);
        } else if (input.length > this.maxLength) {
            input = Arrays.copyOf(input, this.maxLength);
        }
        if (!this.instantiated) {
            this.instantiateIfNecessary(input);
        } else {
            this.reseedAlgorithm(input, null);
        }
    }

    private byte[] getEntropyInput(boolean isPr) {
        return this.getEntropyInput(this.minLength, this.minLength, this.maxLength, isPr);
    }

    private byte[] getEntropyInput(int minEntropy, int minLength, int maxLength, boolean pr) {
        EntropySource esNow;
        if (debug != null) {
            debug.println(this, "getEntropy(" + minEntropy + "," + minLength + "," + maxLength + "," + pr + ")");
        }
        if ((esNow = this.es) == null) {
            esNow = pr ? SeederHolder.prseeder : SeederHolder.seeder;
        }
        return esNow.getEntropy(minEntropy, minLength, maxLength, pr);
    }

    protected AbstractDrbg() {
    }

    protected AbstractDrbg(SecureRandomParameters params) {
    }

    protected SecureRandomParameters engineGetParameters() {
        return DrbgParameters.instantiation(this.securityStrength, this.predictionResistanceFlag ? DrbgParameters.Capability.PR_AND_RESEED : (this.supportReseeding ? DrbgParameters.Capability.RESEED_ONLY : DrbgParameters.Capability.NONE), this.personalizationString);
    }

    protected final void configure(SecureRandomParameters params) {
        byte[] ps;
        DrbgParameters.Instantiation inst;
        if (debug != null) {
            debug.println(this, "configure " + this + " with " + params);
        }
        if (params == null) {
            params = DrbgParameters.instantiation(-1, DrbgParameters.Capability.RESEED_ONLY, null);
        }
        if (params instanceof MoreDrbgParameters) {
            MoreDrbgParameters m = (MoreDrbgParameters)params;
            this.requestedNonce = m.nonce;
            this.es = m.es;
            this.requestedAlgorithm = m.algorithm;
            this.usedf = m.usedf;
            params = DrbgParameters.instantiation(m.strength, m.capability, m.personalizationString);
        }
        if (params instanceof DrbgParameters.Instantiation) {
            inst = (DrbgParameters.Instantiation)params;
            if (inst.getStrength() > this.highestSupportedSecurityStrength) {
                throw new IllegalArgumentException("strength too big: " + inst.getStrength());
            }
            if (inst.getCapability().supportsPredictionResistance() && !this.supportPredictionResistance) {
                throw new IllegalArgumentException("pr not supported");
            }
            ps = inst.getPersonalizationString();
            if (ps != null && ps.length > this.maxPersonalizationStringLength) {
                throw new IllegalArgumentException("ps too long: " + ps.length);
            }
            if (inst.getCapability().supportsReseeding() && !this.supportReseeding) {
                throw new IllegalArgumentException("reseed not supported");
            }
        } else {
            throw new IllegalArgumentException("unknown params: " + params.getClass());
        }
        this.personalizationString = ps;
        this.predictionResistanceFlag = inst.getCapability().supportsPredictionResistance();
        this.requestedInstantiationSecurityStrength = inst.getStrength();
        this.chooseAlgorithmAndStrength();
        this.instantiated = false;
        if (debug != null) {
            debug.println(this, "configured " + this);
        }
    }

    private synchronized void instantiateIfNecessary(byte[] entropy) {
        if (!this.instantiated) {
            if (entropy == null) {
                entropy = this.getEntropyInput(this.predictionResistanceFlag);
            }
            this.nonce = this.requestedNonce != null ? this.requestedNonce : NonceProvider.next();
            this.initEngine();
            this.instantiateAlgorithm(entropy);
            this.instantiated = true;
        }
    }

    protected static int getStandardStrength(int input) {
        if (input <= 112) {
            return 112;
        }
        if (input <= 128) {
            return 128;
        }
        if (input <= 192) {
            return 192;
        }
        if (input <= 256) {
            return 256;
        }
        throw new IllegalArgumentException("input too big: " + input);
    }

    public String toString() {
        return this.mechName + "," + this.algorithm + "," + this.securityStrength + "," + (this.predictionResistanceFlag ? "pr_and_reseed" : (this.supportReseeding ? "reseed_only" : "none"));
    }

    private static class SeederHolder {
        static final EntropySource prseeder = defaultES;
        static final EntropySource seeder;

        private SeederHolder() {
        }

        static {
            HashDrbg first = new HashDrbg(new MoreDrbgParameters(prseeder, null, "SHA-256", null, false, DrbgParameters.instantiation(256, DrbgParameters.Capability.NONE, SeedGenerator.getSystemEntropy())));
            seeder = (entropy, minLen, maxLen, pr) -> {
                if (pr) {
                    throw new IllegalArgumentException("pr not supported");
                }
                byte[] result = new byte[minLen];
                first.engineNextBytes(result);
                return result;
            };
        }
    }

    private static class NonceProvider {
        private static final byte[] block = new byte[16];

        private NonceProvider() {
        }

        private static synchronized byte[] next() {
            int k = 15;
            while (k >= 0) {
                int n = k--;
                block[n] = (byte)(block[n] + 1);
                if (block[n] == 0) continue;
            }
            return (byte[])block.clone();
        }
    }
}

