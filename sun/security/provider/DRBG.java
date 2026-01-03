/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.AccessController;
import java.security.DrbgParameters;
import java.security.SecureRandomParameters;
import java.security.SecureRandomSpi;
import java.security.Security;
import java.util.Locale;
import sun.security.provider.AbstractDrbg;
import sun.security.provider.CtrDrbg;
import sun.security.provider.EntropySource;
import sun.security.provider.HashDrbg;
import sun.security.provider.HmacDrbg;
import sun.security.provider.MoreDrbgParameters;

public final class DRBG
extends SecureRandomSpi {
    private static final String PROP_NAME = "securerandom.drbg.config";
    private static final long serialVersionUID = 9L;
    private transient AbstractDrbg impl;
    private final MoreDrbgParameters mdp;

    public DRBG(SecureRandomParameters params) {
        String mech = null;
        Boolean usedf = null;
        String algorithm = null;
        int strength = -1;
        DrbgParameters.Capability cap = null;
        byte[] ps = null;
        EntropySource es = null;
        byte[] nonce = null;
        String config = AccessController.doPrivileged(() -> Security.getProperty(PROP_NAME));
        if (config != null && !config.isEmpty()) {
            block22: for (String part : config.split(",")) {
                part = part.trim();
                switch (part.toLowerCase(Locale.ROOT)) {
                    case "": {
                        throw new IllegalArgumentException("aspect in securerandom.drbg.config cannot be empty");
                    }
                    case "pr_and_reseed": {
                        DRBG.checkTwice(cap != null, "capability");
                        cap = DrbgParameters.Capability.PR_AND_RESEED;
                        continue block22;
                    }
                    case "reseed_only": {
                        DRBG.checkTwice(cap != null, "capability");
                        cap = DrbgParameters.Capability.RESEED_ONLY;
                        continue block22;
                    }
                    case "none": {
                        DRBG.checkTwice(cap != null, "capability");
                        cap = DrbgParameters.Capability.NONE;
                        continue block22;
                    }
                    case "hash_drbg": 
                    case "hmac_drbg": 
                    case "ctr_drbg": {
                        DRBG.checkTwice(mech != null, "mechanism name");
                        mech = part;
                        continue block22;
                    }
                    case "no_df": {
                        DRBG.checkTwice(usedf != null, "usedf flag");
                        usedf = false;
                        continue block22;
                    }
                    case "use_df": {
                        DRBG.checkTwice(usedf != null, "usedf flag");
                        usedf = true;
                        continue block22;
                    }
                    default: {
                        try {
                            int tmp = Integer.parseInt(part);
                            if (tmp < 0) {
                                throw new IllegalArgumentException("strength in securerandom.drbg.config cannot be negative: " + part);
                            }
                            DRBG.checkTwice(strength >= 0, "strength");
                            strength = tmp;
                            continue block22;
                        }
                        catch (NumberFormatException e) {
                            DRBG.checkTwice(algorithm != null, "algorithm name");
                            algorithm = part;
                        }
                    }
                }
            }
        }
        if (params != null) {
            if (params instanceof MoreDrbgParameters) {
                MoreDrbgParameters m = (MoreDrbgParameters)params;
                params = DrbgParameters.instantiation(m.strength, m.capability, m.personalizationString);
                es = m.es;
                nonce = m.nonce;
                if (m.mech != null) {
                    mech = m.mech;
                }
                if (m.algorithm != null) {
                    algorithm = m.algorithm;
                }
                usedf = m.usedf;
            }
            if (params instanceof DrbgParameters.Instantiation) {
                DrbgParameters.Instantiation dp = (DrbgParameters.Instantiation)params;
                ps = dp.getPersonalizationString();
                int tmp = dp.getStrength();
                if (tmp != -1) {
                    strength = tmp;
                }
                cap = dp.getCapability();
            } else {
                throw new IllegalArgumentException("Unsupported params: " + params.getClass());
            }
        }
        if (cap == null) {
            cap = DrbgParameters.Capability.NONE;
        }
        if (mech == null) {
            mech = "Hash_DRBG";
        }
        if (usedf == null) {
            usedf = true;
        }
        this.mdp = new MoreDrbgParameters(es, mech, algorithm, nonce, usedf, DrbgParameters.instantiation(strength, cap, ps));
        this.createImpl();
    }

    private void createImpl() {
        switch (this.mdp.mech.toLowerCase(Locale.ROOT)) {
            case "hash_drbg": {
                this.impl = new HashDrbg(this.mdp);
                break;
            }
            case "hmac_drbg": {
                this.impl = new HmacDrbg(this.mdp);
                break;
            }
            case "ctr_drbg": {
                this.impl = new CtrDrbg(this.mdp);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported mech: " + this.mdp.mech);
            }
        }
    }

    @Override
    protected void engineSetSeed(byte[] seed) {
        this.impl.engineSetSeed(seed);
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {
        this.impl.engineNextBytes(bytes);
    }

    @Override
    protected byte[] engineGenerateSeed(int numBytes) {
        return this.impl.engineGenerateSeed(numBytes);
    }

    @Override
    protected void engineNextBytes(byte[] bytes, SecureRandomParameters params) {
        this.impl.engineNextBytes(bytes, params);
    }

    @Override
    protected void engineReseed(SecureRandomParameters params) {
        this.impl.engineReseed(params);
    }

    @Override
    protected SecureRandomParameters engineGetParameters() {
        return this.impl.engineGetParameters();
    }

    @Override
    public String toString() {
        return this.impl.toString();
    }

    private static void checkTwice(boolean flag, String name) {
        if (flag) {
            throw new IllegalArgumentException(name + " cannot be provided more than once in " + PROP_NAME);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (this.mdp == null || this.mdp.mech == null) {
            throw new IllegalArgumentException("Input data is corrupted");
        }
        this.createImpl();
    }
}

