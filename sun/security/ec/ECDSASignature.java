/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.util.Optional;
import sun.security.ec.ECDSAOperations;
import sun.security.ec.ECKeyFactory;
import sun.security.ec.ECOperations;
import sun.security.ec.ECPrivateKeyImpl;
import sun.security.jca.JCAUtil;
import sun.security.util.ECUtil;

abstract class ECDSASignature
extends SignatureSpi {
    private final MessageDigest messageDigest;
    private SecureRandom random;
    private boolean needsReset;
    private ECPrivateKey privateKey;
    private ECPublicKey publicKey;
    private final boolean p1363Format;

    ECDSASignature() {
        this(false);
    }

    ECDSASignature(boolean p1363Format) {
        this.messageDigest = null;
        this.p1363Format = p1363Format;
    }

    ECDSASignature(String digestName) {
        this(digestName, false);
    }

    ECDSASignature(String digestName, boolean p1363Format) {
        try {
            this.messageDigest = MessageDigest.getInstance(digestName);
        }
        catch (NoSuchAlgorithmException e) {
            throw new ProviderException(e);
        }
        this.needsReset = false;
        this.p1363Format = p1363Format;
    }

    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        ECPublicKey key;
        this.publicKey = key = (ECPublicKey)ECKeyFactory.toECKey(publicKey);
        this.privateKey = null;
        this.resetDigest();
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        this.engineInitSign(privateKey, null);
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
        ECPrivateKey key = (ECPrivateKey)ECKeyFactory.toECKey(privateKey);
        ECUtil.checkPrivateKey(key);
        this.privateKey = key;
        this.publicKey = null;
        this.random = random;
        this.resetDigest();
    }

    protected void resetDigest() {
        if (this.needsReset) {
            if (this.messageDigest != null) {
                this.messageDigest.reset();
            }
            this.needsReset = false;
        }
    }

    protected byte[] getDigestValue() throws SignatureException {
        this.needsReset = false;
        return this.messageDigest.digest();
    }

    @Override
    protected void engineUpdate(byte b) throws SignatureException {
        this.messageDigest.update(b);
        this.needsReset = true;
    }

    @Override
    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
        this.messageDigest.update(b, off, len);
        this.needsReset = true;
    }

    @Override
    protected void engineUpdate(ByteBuffer byteBuffer) {
        int len = byteBuffer.remaining();
        if (len <= 0) {
            return;
        }
        this.messageDigest.update(byteBuffer);
        this.needsReset = true;
    }

    private byte[] signDigestImpl(ECDSAOperations ops, int seedBits, byte[] digest, ECPrivateKey priv, SecureRandom random) throws SignatureException {
        byte[] seedBytes = new byte[(seedBits + 7) / 8];
        byte[] s = priv instanceof ECPrivateKeyImpl ? ((ECPrivateKeyImpl)priv).getArrayS() : ECUtil.sArray(priv.getS(), priv.getParams());
        int numAttempts = 128;
        for (int i = 0; i < numAttempts; ++i) {
            random.nextBytes(seedBytes);
            ECDSAOperations.Seed seed = new ECDSAOperations.Seed(seedBytes);
            try {
                return ops.signDigest(s, digest, seed);
            }
            catch (ECOperations.IntermediateValueException intermediateValueException) {
                continue;
            }
        }
        throw new SignatureException("Unable to produce signature after " + numAttempts + " attempts");
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
        if (this.random == null) {
            this.random = JCAUtil.getSecureRandom();
        }
        byte[] digest = this.getDigestValue();
        ECParameterSpec params = this.privateKey.getParams();
        int seedBits = params.getOrder().bitLength() + 64;
        Optional<ECDSAOperations> opsOpt = ECDSAOperations.forParameters(params);
        if (opsOpt.isEmpty()) {
            throw new SignatureException("Curve not supported: " + String.valueOf(params));
        }
        byte[] sig = this.signDigestImpl(opsOpt.get(), seedBits, digest, this.privateKey, this.random);
        if (this.p1363Format) {
            return sig;
        }
        return ECUtil.encodeSignature(sig);
    }

    @Override
    protected boolean engineVerify(byte[] signature) throws SignatureException {
        ECPoint w = this.publicKey.getW();
        ECParameterSpec params = this.publicKey.getParams();
        try {
            ECUtil.validatePublicKey(w, params);
        }
        catch (InvalidKeyException e) {
            return false;
        }
        ECDSAOperations ops = ECDSAOperations.forParameters(params).orElseThrow(() -> new SignatureException("Curve not supported: " + String.valueOf(params)));
        if (params.getCofactor() != 1 && !ops.getEcOperations().checkOrder(w)) {
            return false;
        }
        byte[] sig = this.p1363Format ? signature : ECUtil.decodeSignature(signature);
        return ops.verifySignedDigest(this.getDigestValue(), sig, w);
    }

    @Override
    @Deprecated
    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
        throw new UnsupportedOperationException("setParameter() not supported");
    }

    @Override
    protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        Key key;
        if (params == null) {
            return;
        }
        if (!(params instanceof ECParameterSpec)) {
            throw new InvalidAlgorithmParameterException("Parameters must be of type ECParameterSpec");
        }
        ECParameterSpec ecparams = (ECParameterSpec)params;
        Key key2 = key = this.privateKey == null ? this.publicKey : this.privateKey;
        if (key != null && !ECUtil.equals(ecparams, key.getParams())) {
            throw new InvalidAlgorithmParameterException("Signature params does not match key params");
        }
    }

    @Override
    @Deprecated
    protected Object engineGetParameter(String param) throws InvalidParameterException {
        throw new UnsupportedOperationException("getParameter() not supported");
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        return null;
    }

    public static final class SHA3_512inP1363Format
    extends ECDSASignature {
        public SHA3_512inP1363Format() {
            super("SHA3-512", true);
        }
    }

    public static final class SHA3_512
    extends ECDSASignature {
        public SHA3_512() {
            super("SHA3-512");
        }
    }

    public static final class SHA3_384inP1363Format
    extends ECDSASignature {
        public SHA3_384inP1363Format() {
            super("SHA3-384", true);
        }
    }

    public static final class SHA3_384
    extends ECDSASignature {
        public SHA3_384() {
            super("SHA3-384");
        }
    }

    public static final class SHA3_256inP1363Format
    extends ECDSASignature {
        public SHA3_256inP1363Format() {
            super("SHA3-256", true);
        }
    }

    public static final class SHA3_256
    extends ECDSASignature {
        public SHA3_256() {
            super("SHA3-256");
        }
    }

    public static final class SHA3_224inP1363Format
    extends ECDSASignature {
        public SHA3_224inP1363Format() {
            super("SHA3-224", true);
        }
    }

    public static final class SHA3_224
    extends ECDSASignature {
        public SHA3_224() {
            super("SHA3-224");
        }
    }

    public static final class SHA512inP1363Format
    extends ECDSASignature {
        public SHA512inP1363Format() {
            super("SHA-512", true);
        }
    }

    public static final class SHA512
    extends ECDSASignature {
        public SHA512() {
            super("SHA-512");
        }
    }

    public static final class SHA384inP1363Format
    extends ECDSASignature {
        public SHA384inP1363Format() {
            super("SHA-384", true);
        }
    }

    public static final class SHA384
    extends ECDSASignature {
        public SHA384() {
            super("SHA-384");
        }
    }

    public static final class SHA256inP1363Format
    extends ECDSASignature {
        public SHA256inP1363Format() {
            super("SHA-256", true);
        }
    }

    public static final class SHA256
    extends ECDSASignature {
        public SHA256() {
            super("SHA-256");
        }
    }

    public static final class SHA224inP1363Format
    extends ECDSASignature {
        public SHA224inP1363Format() {
            super("SHA-224", true);
        }
    }

    public static final class SHA224
    extends ECDSASignature {
        public SHA224() {
            super("SHA-224");
        }
    }

    public static final class SHA1inP1363Format
    extends ECDSASignature {
        public SHA1inP1363Format() {
            super("SHA1", true);
        }
    }

    public static final class SHA1
    extends ECDSASignature {
        public SHA1() {
            super("SHA1");
        }
    }

    public static final class RawinP1363Format
    extends RawECDSA {
        public RawinP1363Format() {
            super(true);
        }
    }

    public static final class Raw
    extends RawECDSA {
        public Raw() {
            super(false);
        }
    }

    static class RawECDSA
    extends ECDSASignature {
        private static final int RAW_ECDSA_MAX = 64;
        private final byte[] precomputedDigest = new byte[64];
        private int offset = 0;

        RawECDSA(boolean p1363Format) {
            super(p1363Format);
        }

        @Override
        protected void engineUpdate(byte b) throws SignatureException {
            if (this.offset >= this.precomputedDigest.length) {
                this.offset = 65;
                return;
            }
            this.precomputedDigest[this.offset++] = b;
        }

        @Override
        protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
            if (this.offset >= this.precomputedDigest.length) {
                this.offset = 65;
                return;
            }
            System.arraycopy(b, off, this.precomputedDigest, this.offset, len);
            this.offset += len;
        }

        @Override
        protected void engineUpdate(ByteBuffer byteBuffer) {
            int len = byteBuffer.remaining();
            if (len <= 0) {
                return;
            }
            if (len >= this.precomputedDigest.length - this.offset) {
                this.offset = 65;
                return;
            }
            byteBuffer.get(this.precomputedDigest, this.offset, len);
            this.offset += len;
        }

        @Override
        protected void resetDigest() {
            this.offset = 0;
        }

        @Override
        protected byte[] getDigestValue() throws SignatureException {
            if (this.offset > 64) {
                throw new SignatureException("Message digest is too long");
            }
            byte[] result = new byte[this.offset];
            System.arraycopy(this.precomputedDigest, 0, result, 0, this.offset);
            this.offset = 0;
            return result;
        }
    }
}

