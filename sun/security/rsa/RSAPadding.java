/*
 * Decompiled with CFR 0.152.
 */
package sun.security.rsa;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import sun.security.jca.JCAUtil;
import sun.security.rsa.MGF1;
import sun.security.rsa.RSACore;

public final class RSAPadding {
    public static final int PAD_BLOCKTYPE_1 = 1;
    public static final int PAD_BLOCKTYPE_2 = 2;
    public static final int PAD_NONE = 3;
    public static final int PAD_OAEP_MGF1 = 4;
    private final int type;
    private final int paddedSize;
    private SecureRandom random;
    private final int maxDataSize;
    private MessageDigest md;
    private MGF1 mgf;
    private byte[] lHash;
    private static final Map<String, byte[]> emptyHashes = Collections.synchronizedMap(new HashMap());

    public static RSAPadding getInstance(int type, int paddedSize) throws InvalidKeyException, InvalidAlgorithmParameterException {
        return new RSAPadding(type, paddedSize, null, null);
    }

    public static RSAPadding getInstance(int type, int paddedSize, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        return new RSAPadding(type, paddedSize, random, null);
    }

    public static RSAPadding getInstance(int type, int paddedSize, SecureRandom random, OAEPParameterSpec spec) throws InvalidKeyException, InvalidAlgorithmParameterException {
        return new RSAPadding(type, paddedSize, random, spec);
    }

    private RSAPadding(int type, int paddedSize, SecureRandom random, OAEPParameterSpec spec) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.type = type;
        this.paddedSize = paddedSize;
        this.random = random;
        if (paddedSize < 64) {
            throw new InvalidKeyException("Padded size must be at least 64");
        }
        switch (type) {
            case 1: 
            case 2: {
                this.maxDataSize = paddedSize - 11;
                break;
            }
            case 3: {
                this.maxDataSize = paddedSize;
                break;
            }
            case 4: {
                String mdName;
                String mgfMdName = mdName = "SHA-1";
                byte[] digestInput = null;
                try {
                    if (spec != null) {
                        mdName = spec.getDigestAlgorithm();
                        String mgfName = spec.getMGFAlgorithm();
                        if (!mgfName.equalsIgnoreCase("MGF1")) {
                            throw new InvalidAlgorithmParameterException("Unsupported MGF algo: " + mgfName);
                        }
                        mgfMdName = ((MGF1ParameterSpec)spec.getMGFParameters()).getDigestAlgorithm();
                        PSource pSrc = spec.getPSource();
                        String pSrcAlgo = pSrc.getAlgorithm();
                        if (!pSrcAlgo.equalsIgnoreCase("PSpecified")) {
                            throw new InvalidAlgorithmParameterException("Unsupported pSource algo: " + pSrcAlgo);
                        }
                        digestInput = ((PSource.PSpecified)pSrc).getValue();
                    }
                    this.md = MessageDigest.getInstance(mdName);
                    this.mgf = new MGF1(mgfMdName);
                }
                catch (NoSuchAlgorithmException e) {
                    throw new InvalidKeyException("Digest not available", e);
                }
                this.lHash = RSAPadding.getInitialHash(this.md, digestInput);
                int digestLen = this.lHash.length;
                this.maxDataSize = paddedSize - 2 - 2 * digestLen;
                if (this.maxDataSize > 0) break;
                throw new InvalidKeyException("Key is too short for encryption using OAEPPadding with " + mdName + " and " + this.mgf.getName());
            }
            default: {
                throw new InvalidKeyException("Invalid padding: " + type);
            }
        }
    }

    private static byte[] getInitialHash(MessageDigest md, byte[] digestInput) {
        byte[] result;
        if (digestInput == null || digestInput.length == 0) {
            String digestName = md.getAlgorithm();
            result = emptyHashes.get(digestName);
            if (result == null) {
                result = md.digest();
                emptyHashes.put(digestName, result);
            }
        } else {
            result = md.digest(digestInput);
        }
        return result;
    }

    public int getMaxDataSize() {
        return this.maxDataSize;
    }

    public byte[] pad(byte[] data) {
        return this.pad(data, 0, data.length);
    }

    public byte[] pad(byte[] data, int ofs, int len) {
        if (len > this.maxDataSize) {
            return null;
        }
        switch (this.type) {
            case 3: {
                return RSACore.convert(data, ofs, len);
            }
            case 1: 
            case 2: {
                return this.padV15(data, ofs, len);
            }
            case 4: {
                return this.padOAEP(data, ofs, len);
            }
        }
        throw new AssertionError();
    }

    public byte[] unpad(byte[] padded) {
        if (padded.length == this.paddedSize) {
            return switch (this.type) {
                case 3 -> padded;
                case 1, 2 -> this.unpadV15(padded);
                case 4 -> this.unpadOAEP(padded);
                default -> throw new AssertionError();
            };
        }
        return null;
    }

    private byte[] padV15(byte[] data, int ofs, int len) {
        byte[] padded = new byte[this.paddedSize];
        System.arraycopy(data, ofs, padded, this.paddedSize - len, len);
        int psSize = this.paddedSize - 3 - len;
        int k = 0;
        padded[k++] = 0;
        padded[k++] = (byte)this.type;
        if (this.type == 1) {
            while (psSize-- > 0) {
                padded[k++] = -1;
            }
        } else {
            if (this.random == null) {
                this.random = JCAUtil.getSecureRandom();
            }
            while (psSize > 0) {
                byte[] r = new byte[psSize + 4];
                this.random.nextBytes(r);
                for (int i = 0; i < r.length && psSize > 0; ++i) {
                    if (r[i] == 0) continue;
                    padded[k++] = r[i];
                    --psSize;
                }
            }
        }
        return padded;
    }

    private byte[] unpadV15(byte[] padded) {
        int paddedLength = padded.length;
        if (paddedLength < 2) {
            return null;
        }
        byte p0 = padded[0];
        byte p1 = padded[1];
        int bp = (-(p0 & 0xFF) | (p1 - this.type | this.type - p1)) >>> 31;
        int padLen = 0;
        int k = 2;
        while (k < paddedLength) {
            int b = padded[k++] & 0xFF;
            padLen += k * (1 - (-(b | padLen) >>> 31));
            if (k == paddedLength) {
                bp |= 1 - (-padLen >>> 31);
            }
            bp |= 1 - (-(this.type - 1 & 0xFF | padLen | 1 - (b - 255 >>> 31)) >>> 31);
        }
        int n = paddedLength - padLen;
        bp |= this.maxDataSize - n >>> 31;
        byte[] padding = new byte[padLen + 2];
        for (int i = 0; i < padLen; ++i) {
            padding[i] = padded[i];
        }
        byte[] data = new byte[n];
        for (int i = 0; i < n; ++i) {
            data[i] = padded[padLen + i];
        }
        if ((bp | padding[bp]) != 0) {
            return null;
        }
        return data;
    }

    public byte[] unpadForTls(byte[] padded, int clientVersion, int serverVersion) {
        int paddedLength = padded.length;
        int bp = (padded[0] | padded[1] - 2) & 0xFFF;
        int k = 2;
        while (k < paddedLength - 49) {
            int b = padded[k++] & 0xFF;
            bp |= 1 - (-b >>> 31);
        }
        bp |= padded[k++] & 0xFF;
        int encodedVersion = (padded[k] & 0xFF) << 8 | padded[k + 1] & 0xFF;
        int bv1 = clientVersion - encodedVersion;
        bv1 |= -bv1;
        int bv3 = serverVersion - encodedVersion;
        bv3 |= -bv3;
        int bv2 = 769 - clientVersion;
        bp |= (bv1 & (bv2 | bv3)) >>> 28;
        byte[] data = Arrays.copyOfRange(padded, paddedLength - 48, paddedLength);
        if (this.random == null) {
            this.random = JCAUtil.getSecureRandom();
        }
        byte[] fake = new byte[48];
        this.random.nextBytes(fake);
        bp = -bp >> 24;
        for (int i = 0; i < 48; ++i) {
            data[i] = (byte)(~bp & data[i] | bp & fake[i]);
        }
        return data;
    }

    private byte[] padOAEP(byte[] M, int ofs, int len) {
        if (this.random == null) {
            this.random = JCAUtil.getSecureRandom();
        }
        int hLen = this.lHash.length;
        byte[] seed = new byte[hLen];
        this.random.nextBytes(seed);
        byte[] EM = new byte[this.paddedSize];
        int seedStart = 1;
        int seedLen = hLen;
        System.arraycopy(seed, 0, EM, seedStart, seedLen);
        int dbStart = hLen + 1;
        int dbLen = EM.length - dbStart;
        int mStart = this.paddedSize - len;
        System.arraycopy(this.lHash, 0, EM, dbStart, hLen);
        EM[mStart - 1] = 1;
        System.arraycopy(M, ofs, EM, mStart, len);
        this.mgf.generateAndXor(EM, seedStart, seedLen, dbLen, EM, dbStart);
        this.mgf.generateAndXor(EM, dbStart, dbLen, seedLen, EM, seedStart);
        return EM;
    }

    private byte[] unpadOAEP(byte[] padded) {
        byte[] EM = padded;
        boolean bp = false;
        int hLen = this.lHash.length;
        if (EM[0] != 0) {
            bp = true;
        }
        int seedStart = 1;
        int seedLen = hLen;
        int dbStart = hLen + 1;
        int dbLen = EM.length - dbStart;
        this.mgf.generateAndXor(EM, dbStart, dbLen, seedLen, EM, seedStart);
        this.mgf.generateAndXor(EM, seedStart, seedLen, dbLen, EM, dbStart);
        for (int i = 0; i < hLen; ++i) {
            if (this.lHash[i] == EM[dbStart + i]) continue;
            bp = true;
        }
        int padStart = dbStart + hLen;
        int onePos = -1;
        for (int i = padStart; i < EM.length; ++i) {
            byte value = EM[i];
            if (onePos != -1 || value == 0) continue;
            if (value == 1) {
                onePos = i;
                continue;
            }
            bp = true;
        }
        if (onePos == -1) {
            bp = true;
            onePos = EM.length - 1;
        }
        int mStart = onePos + 1;
        byte[] tmp = new byte[mStart - padStart];
        System.arraycopy(EM, padStart, tmp, 0, tmp.length);
        byte[] m = new byte[EM.length - mStart];
        System.arraycopy(EM, mStart, m, 0, m.length);
        return bp ? null : m;
    }
}

