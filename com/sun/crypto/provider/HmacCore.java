/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.MacSpi;
import javax.crypto.SecretKey;

abstract class HmacCore
extends MacSpi
implements Cloneable {
    private MessageDigest md;
    private byte[] k_ipad;
    private byte[] k_opad;
    private boolean first;
    private final int blockLen;

    HmacCore(String digestAlgo, int bl) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(digestAlgo);
        if (!(md instanceof Cloneable)) {
            Provider sun = Security.getProvider("SUN");
            if (sun != null) {
                md = MessageDigest.getInstance(digestAlgo, sun);
            } else {
                Provider[] provs;
                String noCloneProv = md.getProvider().getName();
                md = null;
                for (Provider p : provs = Security.getProviders()) {
                    try {
                        MessageDigest md2;
                        if (p.getName().equals(noCloneProv) || !((md2 = MessageDigest.getInstance(digestAlgo, p)) instanceof Cloneable)) continue;
                        md = md2;
                        break;
                    }
                    catch (NoSuchAlgorithmException nsae) {
                        // empty catch block
                    }
                }
                if (md == null) {
                    throw new NoSuchAlgorithmException("No Cloneable digest found for " + digestAlgo);
                }
            }
        }
        this.md = md;
        this.blockLen = bl;
        this.k_ipad = new byte[this.blockLen];
        this.k_opad = new byte[this.blockLen];
        this.first = true;
    }

    @Override
    protected int engineGetMacLength() {
        return this.md.getDigestLength();
    }

    @Override
    protected void engineInit(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException("HMAC does not use parameters");
        }
        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException("Secret key expected");
        }
        byte[] secret = key.getEncoded();
        if (secret == null) {
            throw new InvalidKeyException("Missing key data");
        }
        if (secret.length > this.blockLen) {
            byte[] tmp = this.md.digest(secret);
            Arrays.fill(secret, (byte)0);
            secret = tmp;
        }
        for (int i = 0; i < this.blockLen; ++i) {
            byte si = i < secret.length ? secret[i] : (byte)0;
            this.k_ipad[i] = (byte)(si ^ 0x36);
            this.k_opad[i] = (byte)(si ^ 0x5C);
        }
        Arrays.fill(secret, (byte)0);
        secret = null;
        this.engineReset();
    }

    @Override
    protected void engineUpdate(byte input) {
        if (this.first) {
            this.md.update(this.k_ipad);
            this.first = false;
        }
        this.md.update(input);
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        if (this.first) {
            this.md.update(this.k_ipad);
            this.first = false;
        }
        this.md.update(input, offset, len);
    }

    @Override
    protected void engineUpdate(ByteBuffer input) {
        if (this.first) {
            this.md.update(this.k_ipad);
            this.first = false;
        }
        this.md.update(input);
    }

    @Override
    protected byte[] engineDoFinal() {
        if (this.first) {
            this.md.update(this.k_ipad);
        } else {
            this.first = true;
        }
        try {
            byte[] tmp = this.md.digest();
            this.md.update(this.k_opad);
            this.md.update(tmp);
            this.md.digest(tmp, 0, tmp.length);
            return tmp;
        }
        catch (DigestException e) {
            throw new ProviderException(e);
        }
    }

    @Override
    protected void engineReset() {
        if (!this.first) {
            this.md.reset();
            this.first = true;
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        HmacCore copy = (HmacCore)super.clone();
        copy.md = (MessageDigest)this.md.clone();
        copy.k_ipad = (byte[])this.k_ipad.clone();
        copy.k_opad = (byte[])this.k_opad.clone();
        return copy;
    }

    public static final class HmacSHA3_512
    extends HmacCore {
        public HmacSHA3_512() throws NoSuchAlgorithmException {
            super("SHA3-512", 72);
        }
    }

    public static final class HmacSHA3_384
    extends HmacCore {
        public HmacSHA3_384() throws NoSuchAlgorithmException {
            super("SHA3-384", 104);
        }
    }

    public static final class HmacSHA3_256
    extends HmacCore {
        public HmacSHA3_256() throws NoSuchAlgorithmException {
            super("SHA3-256", 136);
        }
    }

    public static final class HmacSHA3_224
    extends HmacCore {
        public HmacSHA3_224() throws NoSuchAlgorithmException {
            super("SHA3-224", 144);
        }
    }

    public static final class HmacSHA512_256
    extends HmacCore {
        public HmacSHA512_256() throws NoSuchAlgorithmException {
            super("SHA-512/256", 128);
        }
    }

    public static final class HmacSHA512_224
    extends HmacCore {
        public HmacSHA512_224() throws NoSuchAlgorithmException {
            super("SHA-512/224", 128);
        }
    }

    public static final class HmacSHA512
    extends HmacCore {
        public HmacSHA512() throws NoSuchAlgorithmException {
            super("SHA-512", 128);
        }
    }

    public static final class HmacSHA384
    extends HmacCore {
        public HmacSHA384() throws NoSuchAlgorithmException {
            super("SHA-384", 128);
        }
    }

    public static final class HmacSHA256
    extends HmacCore {
        public HmacSHA256() throws NoSuchAlgorithmException {
            super("SHA-256", 64);
        }
    }

    public static final class HmacSHA224
    extends HmacCore {
        public HmacSHA224() throws NoSuchAlgorithmException {
            super("SHA-224", 64);
        }
    }
}

