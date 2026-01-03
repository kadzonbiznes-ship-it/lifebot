/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import sun.security.provider.AbstractDrbg;
import sun.security.util.HexDumpEncoder;

public abstract class AbstractHashDrbg
extends AbstractDrbg {
    protected int outLen;
    protected int seedLen;

    private static int alg2strength(String algorithm) {
        switch (algorithm.toUpperCase(Locale.ROOT)) {
            case "SHA-224": 
            case "SHA-512/224": {
                return 192;
            }
            case "SHA-256": 
            case "SHA-512/256": 
            case "SHA-384": 
            case "SHA-512": {
                return 256;
            }
        }
        throw new IllegalArgumentException(algorithm + " not supported in Hash_DBRG");
    }

    @Override
    protected void chooseAlgorithmAndStrength() {
        if (this.requestedAlgorithm != null) {
            this.algorithm = this.requestedAlgorithm.toUpperCase(Locale.ROOT);
            int supportedStrength = AbstractHashDrbg.alg2strength(this.algorithm);
            if (this.requestedInstantiationSecurityStrength >= 0) {
                int tryStrength = AbstractHashDrbg.getStandardStrength(this.requestedInstantiationSecurityStrength);
                if (tryStrength > supportedStrength) {
                    throw new IllegalArgumentException(this.algorithm + " does not support strength " + this.requestedInstantiationSecurityStrength);
                }
                this.securityStrength = tryStrength;
            } else {
                this.securityStrength = Math.min(128, supportedStrength);
            }
        } else {
            int tryStrength = this.requestedInstantiationSecurityStrength < 0 ? 128 : this.requestedInstantiationSecurityStrength;
            tryStrength = AbstractHashDrbg.getStandardStrength(tryStrength);
            this.algorithm = "SHA-256";
            this.securityStrength = tryStrength;
        }
        switch (this.algorithm.toUpperCase(Locale.ROOT)) {
            case "SHA-224": 
            case "SHA-512/224": {
                this.seedLen = 55;
                this.outLen = 28;
                break;
            }
            case "SHA-256": 
            case "SHA-512/256": {
                this.seedLen = 55;
                this.outLen = 32;
                break;
            }
            case "SHA-384": {
                this.seedLen = 111;
                this.outLen = 48;
                break;
            }
            case "SHA-512": {
                this.seedLen = 111;
                this.outLen = 64;
                break;
            }
            default: {
                throw new IllegalArgumentException(this.algorithm + " not supported in Hash_DBRG");
            }
        }
        this.minLength = this.securityStrength / 8;
    }

    @Override
    public void instantiateAlgorithm(byte[] entropy) {
        if (debug != null) {
            debug.println(this, "instantiate");
        }
        ArrayList<byte[]> inputs = new ArrayList<byte[]>(3);
        inputs.add(entropy);
        inputs.add(this.nonce);
        if (this.personalizationString != null) {
            inputs.add(this.personalizationString);
        }
        this.hashReseedInternal(inputs);
    }

    @Override
    protected void reseedAlgorithm(byte[] ei, byte[] additionalInput) {
        if (debug != null) {
            debug.println(this, "reseedAlgorithm\n" + new HexDumpEncoder().encodeBuffer(ei) + "\n" + (additionalInput == null ? "" : new HexDumpEncoder().encodeBuffer(additionalInput)));
        }
        ArrayList<byte[]> inputs = new ArrayList<byte[]>(2);
        inputs.add(ei);
        if (additionalInput != null) {
            inputs.add(additionalInput);
        }
        this.hashReseedInternal(inputs);
    }

    protected abstract void hashReseedInternal(List<byte[]> var1);
}

