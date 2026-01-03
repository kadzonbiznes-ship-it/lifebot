/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import sun.security.jca.JCAUtil;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

abstract class DSA
extends SignatureSpi {
    private static final boolean debug = false;
    private static final int BLINDING_BITS = 7;
    private static final BigInteger BLINDING_CONSTANT = BigInteger.valueOf(128L);
    private DSAParams params;
    private BigInteger presetP;
    private BigInteger presetQ;
    private BigInteger presetG;
    private BigInteger presetY;
    private BigInteger presetX;
    private SecureRandom signingRandom;
    private final MessageDigest md;
    private final boolean p1363Format;

    DSA(MessageDigest md) {
        this(md, false);
    }

    DSA(MessageDigest md, boolean p1363Format) {
        this.md = md;
        this.p1363Format = p1363Format;
    }

    private static void checkKey(DSAParams params, int digestLen, String mdAlgo) throws InvalidKeyException {
        int valueN = params.getQ().bitLength();
        if (valueN > digestLen) {
            throw new InvalidKeyException("The security strength of " + mdAlgo + " digest algorithm is not sufficient for this key size");
        }
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        if (!(privateKey instanceof DSAPrivateKey)) {
            throw new InvalidKeyException("not a DSA private key: " + privateKey);
        }
        DSAPrivateKey priv = (DSAPrivateKey)privateKey;
        DSAParams params = priv.getParams();
        if (params == null) {
            throw new InvalidKeyException("DSA private key lacks parameters");
        }
        if (!"NullDigest20".equals(this.md.getAlgorithm())) {
            DSA.checkKey(params, this.md.getDigestLength() * 8, this.md.getAlgorithm());
        }
        this.signingRandom = null;
        this.params = params;
        this.presetX = priv.getX();
        this.presetY = null;
        this.presetP = params.getP();
        this.presetQ = params.getQ();
        this.presetG = params.getG();
        this.md.reset();
    }

    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        if (!(publicKey instanceof DSAPublicKey)) {
            throw new InvalidKeyException("not a DSA public key: " + publicKey);
        }
        DSAPublicKey pub = (DSAPublicKey)publicKey;
        DSAParams params = pub.getParams();
        if (params == null) {
            throw new InvalidKeyException("DSA public key lacks parameters");
        }
        this.params = params;
        this.presetY = pub.getY();
        this.presetX = null;
        this.presetP = params.getP();
        this.presetQ = params.getQ();
        this.presetG = params.getG();
        this.md.reset();
    }

    @Override
    protected void engineUpdate(byte b) {
        this.md.update(b);
    }

    @Override
    protected void engineUpdate(byte[] data, int off, int len) {
        this.md.update(data, off, len);
    }

    @Override
    protected void engineUpdate(ByteBuffer b) {
        this.md.update(b);
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
        BigInteger k = this.generateK(this.presetQ);
        BigInteger r = this.generateR(this.presetP, this.presetQ, this.presetG, k);
        BigInteger s = this.generateS(this.presetX, this.presetQ, r, k);
        if (this.p1363Format) {
            int j;
            int i;
            byte[] rBytes = r.toByteArray();
            byte[] sBytes = s.toByteArray();
            int size = this.presetQ.bitLength() / 8;
            byte[] outseq = new byte[size * 2];
            int rLength = rBytes.length;
            int sLength = sBytes.length;
            for (i = rLength; i > 0 && rBytes[rLength - i] == 0; --i) {
            }
            for (j = sLength; j > 0 && sBytes[sLength - j] == 0; --j) {
            }
            System.arraycopy(rBytes, rLength - i, outseq, size - i, i);
            System.arraycopy(sBytes, sLength - j, outseq, size * 2 - j, j);
            return outseq;
        }
        DerOutputStream outseq = new DerOutputStream(100);
        outseq.putInteger(r);
        outseq.putInteger(s);
        DerValue result = new DerValue(48, outseq.toByteArray());
        return result.toByteArray();
    }

    @Override
    protected boolean engineVerify(byte[] signature) throws SignatureException {
        return this.engineVerify(signature, 0, signature.length);
    }

    @Override
    protected boolean engineVerify(byte[] signature, int offset, int length) throws SignatureException {
        BigInteger s;
        BigInteger r;
        if (this.p1363Format) {
            if ((length & 1) == 1) {
                throw new SignatureException("invalid signature format");
            }
            int mid = length / 2;
            r = new BigInteger(Arrays.copyOfRange(signature, 0, mid));
            s = new BigInteger(Arrays.copyOfRange(signature, mid, length));
        } else {
            try {
                DerInputStream in = new DerInputStream(signature, offset, length, false);
                DerValue[] values = in.getSequence(2);
                if (values.length != 2 || in.available() != 0) {
                    throw new IOException("Invalid encoding for signature");
                }
                r = values[0].getBigInteger();
                s = values[1].getBigInteger();
            }
            catch (IOException e) {
                throw new SignatureException("Invalid encoding for signature", e);
            }
        }
        if (r.signum() < 0) {
            r = new BigInteger(1, r.toByteArray());
        }
        if (s.signum() < 0) {
            s = new BigInteger(1, s.toByteArray());
        }
        if (r.compareTo(this.presetQ) == -1 && s.compareTo(this.presetQ) == -1 && r.signum() > 0 && s.signum() > 0) {
            BigInteger w = this.generateW(this.presetP, this.presetQ, this.presetG, s);
            BigInteger v = this.generateV(this.presetY, this.presetP, this.presetQ, this.presetG, w, r);
            return v.equals(r);
        }
        throw new SignatureException("invalid signature: out of range values");
    }

    @Override
    @Deprecated
    protected void engineSetParameter(String key, Object param) {
        throw new InvalidParameterException("No parameter accepted");
    }

    @Override
    protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException("No parameter accepted");
        }
    }

    @Override
    @Deprecated
    protected Object engineGetParameter(String key) {
        return null;
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        return null;
    }

    private BigInteger generateR(BigInteger p, BigInteger q, BigInteger g, BigInteger k) {
        SecureRandom random = this.getSigningRandom();
        BigInteger blindingValue = new BigInteger(7, random);
        blindingValue = blindingValue.add(BLINDING_CONSTANT);
        k = k.add(q.multiply(blindingValue));
        BigInteger temp = g.modPow(k, p);
        return temp.mod(q);
    }

    private BigInteger generateS(BigInteger x, BigInteger q, BigInteger r, BigInteger k) throws SignatureException {
        byte[] s2;
        try {
            s2 = this.md.digest();
        }
        catch (RuntimeException re) {
            throw new SignatureException(re.getMessage());
        }
        int nBytes = q.bitLength() / 8;
        if (nBytes < s2.length) {
            s2 = Arrays.copyOfRange(s2, 0, nBytes);
        }
        BigInteger z = new BigInteger(1, s2);
        BigInteger k1 = k.modInverse(q);
        return x.multiply(r).add(z).multiply(k1).mod(q);
    }

    private BigInteger generateW(BigInteger p, BigInteger q, BigInteger g, BigInteger s) {
        return s.modInverse(q);
    }

    private BigInteger generateV(BigInteger y, BigInteger p, BigInteger q, BigInteger g, BigInteger w, BigInteger r) throws SignatureException {
        byte[] s2;
        try {
            s2 = this.md.digest();
        }
        catch (RuntimeException re) {
            throw new SignatureException(re.getMessage());
        }
        int nBytes = q.bitLength() / 8;
        if (nBytes < s2.length) {
            s2 = Arrays.copyOfRange(s2, 0, nBytes);
        }
        BigInteger z = new BigInteger(1, s2);
        BigInteger u1 = z.multiply(w).mod(q);
        BigInteger u2 = r.multiply(w).mod(q);
        BigInteger t1 = g.modPow(u1, p);
        BigInteger t2 = y.modPow(u2, p);
        BigInteger t3 = t1.multiply(t2);
        BigInteger t5 = t3.mod(p);
        return t5.mod(q);
    }

    protected BigInteger generateK(BigInteger q) {
        SecureRandom random = this.getSigningRandom();
        byte[] kValue = new byte[(q.bitLength() + 7) / 8 + 8];
        random.nextBytes(kValue);
        return new BigInteger(1, kValue).mod(q.subtract(BigInteger.ONE)).add(BigInteger.ONE);
    }

    protected SecureRandom getSigningRandom() {
        if (this.signingRandom == null) {
            this.signingRandom = this.appRandom != null ? this.appRandom : JCAUtil.getSecureRandom();
        }
        return this.signingRandom;
    }

    public String toString() {
        String printable = "DSA Signature";
        if (this.presetP != null && this.presetQ != null && this.presetG != null) {
            printable = printable + "\n\tp: " + Debug.toHexString(this.presetP);
            printable = printable + "\n\tq: " + Debug.toHexString(this.presetQ);
            printable = printable + "\n\tg: " + Debug.toHexString(this.presetG);
        } else {
            printable = printable + "\n\t P, Q or G not initialized.";
        }
        if (this.presetY != null) {
            printable = printable + "\n\ty: " + Debug.toHexString(this.presetY);
        }
        if (this.presetY == null && this.presetX == null) {
            printable = printable + "\n\tUNINITIALIZED";
        }
        return printable;
    }

    public static final class RawDSAinP1363Format
    extends Raw {
        public RawDSAinP1363Format() {
            super(true);
        }
    }

    public static final class RawDSA
    extends Raw {
        public RawDSA() {
            super(false);
        }
    }

    static class Raw
    extends DSA {
        private Raw(boolean p1363Format) {
            super(new NullDigest20(), p1363Format);
        }

        public static final class NullDigest20
        extends MessageDigest {
            private final byte[] digestBuffer = new byte[20];
            private int ofs = 0;

            protected NullDigest20() {
                super("NullDigest20");
            }

            @Override
            protected void engineUpdate(byte input) {
                if (this.ofs == this.digestBuffer.length) {
                    this.ofs = Integer.MAX_VALUE;
                } else {
                    this.digestBuffer[this.ofs++] = input;
                }
            }

            @Override
            protected void engineUpdate(byte[] input, int offset, int len) {
                if (len > this.digestBuffer.length - this.ofs) {
                    this.ofs = Integer.MAX_VALUE;
                } else {
                    System.arraycopy(input, offset, this.digestBuffer, this.ofs, len);
                    this.ofs += len;
                }
            }

            @Override
            protected void engineUpdate(ByteBuffer input) {
                int inputLen = input.remaining();
                if (inputLen > this.digestBuffer.length - this.ofs) {
                    this.ofs = Integer.MAX_VALUE;
                } else {
                    input.get(this.digestBuffer, this.ofs, inputLen);
                    this.ofs += inputLen;
                }
            }

            @Override
            protected byte[] engineDigest() throws RuntimeException {
                if (this.ofs != this.digestBuffer.length) {
                    throw new RuntimeException("Data for RawDSA must be exactly 20 bytes long");
                }
                this.reset();
                return this.digestBuffer;
            }

            @Override
            protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
                if (this.ofs != this.digestBuffer.length) {
                    throw new DigestException("Data for RawDSA must be exactly 20 bytes long");
                }
                if (len < this.digestBuffer.length) {
                    throw new DigestException("Output buffer too small; must be at least 20 bytes");
                }
                System.arraycopy(this.digestBuffer, 0, buf, offset, this.digestBuffer.length);
                this.reset();
                return this.digestBuffer.length;
            }

            @Override
            protected void engineReset() {
                this.ofs = 0;
            }

            @Override
            protected int engineGetDigestLength() {
                return this.digestBuffer.length;
            }
        }
    }

    public static final class SHA1withDSAinP1363Format
    extends DSA {
        public SHA1withDSAinP1363Format() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA-1"), true);
        }
    }

    public static final class SHA1withDSA
    extends DSA {
        public SHA1withDSA() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA-1"));
        }
    }

    public static final class SHA512withDSAinP1363Format
    extends DSA {
        public SHA512withDSAinP1363Format() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA-512"), true);
        }
    }

    public static final class SHA512withDSA
    extends DSA {
        public SHA512withDSA() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA-512"));
        }
    }

    public static final class SHA384withDSAinP1363Format
    extends DSA {
        public SHA384withDSAinP1363Format() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA-384"), true);
        }
    }

    public static final class SHA384withDSA
    extends DSA {
        public SHA384withDSA() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA-384"));
        }
    }

    public static final class SHA256withDSAinP1363Format
    extends DSA {
        public SHA256withDSAinP1363Format() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA-256"), true);
        }
    }

    public static final class SHA256withDSA
    extends DSA {
        public SHA256withDSA() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA-256"));
        }
    }

    public static final class SHA224withDSAinP1363Format
    extends DSA {
        public SHA224withDSAinP1363Format() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA-224"), true);
        }
    }

    public static final class SHA224withDSA
    extends DSA {
        public SHA224withDSA() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA-224"));
        }
    }

    public static final class SHA3_512withDSAinP1363Format
    extends DSA {
        public SHA3_512withDSAinP1363Format() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA3-512"), true);
        }
    }

    public static final class SHA3_512withDSA
    extends DSA {
        public SHA3_512withDSA() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA3-512"));
        }
    }

    public static final class SHA3_384withDSAinP1363Format
    extends DSA {
        public SHA3_384withDSAinP1363Format() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA3-384"), true);
        }
    }

    public static final class SHA3_384withDSA
    extends DSA {
        public SHA3_384withDSA() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA3-384"));
        }
    }

    public static final class SHA3_256withDSAinP1363Format
    extends DSA {
        public SHA3_256withDSAinP1363Format() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA3-256"), true);
        }
    }

    public static final class SHA3_256withDSA
    extends DSA {
        public SHA3_256withDSA() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA3-256"));
        }
    }

    public static final class SHA3_224withDSAinP1363Format
    extends DSA {
        public SHA3_224withDSAinP1363Format() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA3-224"), true);
        }
    }

    public static final class SHA3_224withDSA
    extends DSA {
        public SHA3_224withDSA() throws NoSuchAlgorithmException {
            super(MessageDigest.getInstance("SHA3-224"));
        }
    }
}

