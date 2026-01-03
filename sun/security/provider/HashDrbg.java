/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider;

import java.math.BigInteger;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandomParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import sun.security.provider.AbstractHashDrbg;

public class HashDrbg
extends AbstractHashDrbg {
    private static final byte[] ZERO = new byte[1];
    private static final byte[] ONE = new byte[]{1};
    private MessageDigest digest;
    private byte[] v;
    private byte[] c;

    public HashDrbg(SecureRandomParameters params) {
        this.mechName = "Hash_DRBG";
        this.configure(params);
    }

    @Override
    protected void initEngine() {
        try {
            this.digest = MessageDigest.getInstance(this.algorithm, "SUN");
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            try {
                this.digest = MessageDigest.getInstance(this.algorithm);
            }
            catch (NoSuchAlgorithmException exc) {
                throw new InternalError("internal error: " + this.algorithm + " not available.", exc);
            }
        }
    }

    private byte[] hashDf(int requested, List<byte[]> inputs) {
        return HashDrbg.hashDf(this.digest, this.outLen, requested, inputs);
    }

    public static byte[] hashDf(MessageDigest digest, int outLen, int requested, List<byte[]> inputs) {
        int len = (requested + outLen - 1) / outLen;
        byte[] temp = new byte[len * outLen];
        int counter = 1;
        for (int i = 0; i < len; ++i) {
            digest.update((byte)counter);
            digest.update((byte)(requested >> 21));
            digest.update((byte)(requested >> 13));
            digest.update((byte)(requested >> 5));
            digest.update((byte)(requested << 3));
            for (byte[] input : inputs) {
                digest.update(input);
            }
            try {
                digest.digest(temp, i * outLen, outLen);
            }
            catch (DigestException e) {
                throw new AssertionError("will not happen", e);
            }
            ++counter;
        }
        return temp.length == requested ? temp : Arrays.copyOf(temp, requested);
    }

    @Override
    protected final synchronized void hashReseedInternal(List<byte[]> inputs) {
        if (this.v != null) {
            inputs.add(0, ONE);
            inputs.add(1, this.v);
        }
        byte[] seed = this.hashDf(this.seedLen, inputs);
        this.v = seed;
        inputs = new ArrayList<byte[]>(2);
        inputs.add(ZERO);
        inputs.add(this.v);
        this.c = this.hashDf(this.seedLen, inputs);
        this.reseedCounter = 1;
    }

    private void status() {
        if (debug != null) {
            debug.println(this, "V = " + HexFormat.of().formatHex(this.v));
            debug.println(this, "C = " + HexFormat.of().formatHex(this.c));
            debug.println(this, "reseed counter = " + this.reseedCounter);
        }
    }

    private static void addBytes(byte[] out, int len, byte[] ... data) {
        block0: for (byte[] d : data) {
            int dlen = d.length;
            int carry = 0;
            for (int i = 0; i < len; ++i) {
                int sum = (out[len - i - 1] & 0xFF) + carry;
                if (i < dlen) {
                    sum += d[dlen - i - 1] & 0xFF;
                }
                out[len - i - 1] = (byte)sum;
                carry = sum >> 8;
                if (i >= dlen - 1 && carry == 0) continue block0;
            }
        }
    }

    @Override
    public final synchronized void generateAlgorithm(byte[] result, byte[] additionalInput) {
        if (debug != null) {
            debug.println(this, "generateAlgorithm");
        }
        if (additionalInput != null) {
            this.digest.update((byte)2);
            this.digest.update(this.v);
            this.digest.update(additionalInput);
            HashDrbg.addBytes(this.v, this.seedLen, new byte[][]{this.digest.digest()});
        }
        this.hashGen(result, this.v);
        this.digest.update((byte)3);
        this.digest.update(this.v);
        byte[] h = this.digest.digest();
        byte[] rcBytes = this.reseedCounter < 256 ? new byte[]{(byte)this.reseedCounter} : BigInteger.valueOf(this.reseedCounter).toByteArray();
        HashDrbg.addBytes(this.v, this.seedLen, h, this.c, rcBytes);
        ++this.reseedCounter;
    }

    private void hashGen(byte[] output, byte[] v) {
        byte[] data = v;
        int pos = 0;
        int len = output.length;
        while (len > 0) {
            this.digest.update(data);
            if (len < this.outLen) {
                byte[] out = this.digest.digest();
                System.arraycopy(out, 0, output, pos, len);
                Arrays.fill(out, (byte)0);
            } else {
                try {
                    this.digest.digest(output, pos, this.outLen);
                }
                catch (DigestException e) {
                    throw new AssertionError("will not happen", e);
                }
            }
            if ((len -= this.outLen) <= 0) break;
            if (data == v) {
                data = Arrays.copyOf(v, v.length);
            }
            HashDrbg.addBytes(data, this.seedLen, new byte[][]{ONE});
            pos += this.outLen;
        }
    }
}

