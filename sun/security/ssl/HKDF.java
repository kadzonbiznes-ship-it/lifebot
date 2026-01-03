/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

public final class HKDF {
    private final Mac hmacObj;
    private final int hmacLen;

    public HKDF(String hashAlg) throws NoSuchAlgorithmException {
        Objects.requireNonNull(hashAlg, "Must provide underlying HKDF Digest algorithm.");
        String hmacAlg = "Hmac" + hashAlg.replace("-", "");
        this.hmacObj = Mac.getInstance(hmacAlg);
        this.hmacLen = this.hmacObj.getMacLength();
    }

    public SecretKey extract(SecretKey salt, SecretKey inputKey, String keyAlg) throws InvalidKeyException {
        if (salt == null) {
            salt = new SecretKeySpec(new byte[this.hmacLen], "HKDF-Salt");
        }
        this.hmacObj.init(salt);
        return new SecretKeySpec(this.hmacObj.doFinal(inputKey.getEncoded()), keyAlg);
    }

    public SecretKey extract(byte[] salt, SecretKey inputKey, String keyAlg) throws InvalidKeyException {
        if (salt == null) {
            salt = new byte[this.hmacLen];
        }
        return this.extract(new SecretKeySpec(salt, "HKDF-Salt"), inputKey, keyAlg);
    }

    public SecretKey expand(SecretKey pseudoRandKey, byte[] info, int outLen, String keyAlg) throws InvalidKeyException {
        Objects.requireNonNull(pseudoRandKey, "A null PRK is not allowed.");
        if (outLen > 255 * this.hmacLen) {
            throw new IllegalArgumentException("Requested output length exceeds maximum length allowed for HKDF expansion");
        }
        this.hmacObj.init(pseudoRandKey);
        if (info == null) {
            info = new byte[]{};
        }
        int rounds = (outLen + this.hmacLen - 1) / this.hmacLen;
        byte[] kdfOutput = new byte[rounds * this.hmacLen];
        int offset = 0;
        int tLength = 0;
        for (int i = 0; i < rounds; ++i) {
            try {
                this.hmacObj.update(kdfOutput, Math.max(0, offset - this.hmacLen), tLength);
                this.hmacObj.update(info);
                this.hmacObj.update((byte)(i + 1));
                this.hmacObj.doFinal(kdfOutput, offset);
                tLength = this.hmacLen;
                offset += this.hmacLen;
                continue;
            }
            catch (ShortBufferException sbe) {
                throw new RuntimeException(sbe);
            }
        }
        return new SecretKeySpec(kdfOutput, 0, outLen, keyAlg);
    }
}

