/*
 * Decompiled with CFR 0.152.
 */
package sun.security.rsa;

import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.BadPaddingException;
import sun.security.rsa.RSACore;
import sun.security.rsa.RSAKeyFactory;
import sun.security.rsa.RSAPadding;
import sun.security.rsa.RSAUtil;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

abstract class RSASignature
extends SignatureSpi {
    private static final int baseLength = 8;
    private final ObjectIdentifier digestOID;
    private final int encodedLength;
    private final MessageDigest md;
    private boolean digestReset;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private RSAPadding padding;

    RSASignature(String algorithm, ObjectIdentifier digestOID, int oidLength) {
        this.digestOID = digestOID;
        try {
            this.md = MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException e) {
            throw new ProviderException(e);
        }
        this.digestReset = true;
        this.encodedLength = 8 + oidLength + this.md.getDigestLength();
    }

    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        RSAPublicKey rsaKey = (RSAPublicKey)RSAKeyFactory.toRSAKey(publicKey);
        this.privateKey = null;
        this.publicKey = rsaKey;
        this.initCommon(rsaKey, null);
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        this.engineInitSign(privateKey, null);
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
        RSAPrivateKey rsaKey;
        this.privateKey = rsaKey = (RSAPrivateKey)RSAKeyFactory.toRSAKey(privateKey);
        this.publicKey = null;
        this.initCommon(rsaKey, random);
    }

    private void initCommon(RSAKey rsaKey, SecureRandom random) throws InvalidKeyException {
        try {
            RSAUtil.checkParamsAgainstType(RSAUtil.KeyType.RSA, rsaKey.getParams());
        }
        catch (ProviderException e) {
            throw new InvalidKeyException("Invalid key for RSA signatures", e);
        }
        this.resetDigest();
        int keySize = RSACore.getByteLength(rsaKey);
        try {
            this.padding = RSAPadding.getInstance(1, keySize, random);
        }
        catch (InvalidAlgorithmParameterException iape) {
            throw new InvalidKeyException(iape.getMessage());
        }
        int maxDataSize = this.padding.getMaxDataSize();
        if (this.encodedLength > maxDataSize) {
            throw new InvalidKeyException("Key is too short for this signature algorithm");
        }
    }

    private void resetDigest() {
        if (!this.digestReset) {
            this.md.reset();
            this.digestReset = true;
        }
    }

    private byte[] getDigestValue() {
        this.digestReset = true;
        return this.md.digest();
    }

    @Override
    protected void engineUpdate(byte b) throws SignatureException {
        this.md.update(b);
        this.digestReset = false;
    }

    @Override
    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
        this.md.update(b, off, len);
        this.digestReset = false;
    }

    @Override
    protected void engineUpdate(ByteBuffer b) {
        this.md.update(b);
        this.digestReset = false;
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
        if (this.privateKey == null) {
            throw new SignatureException("Missing private key");
        }
        byte[] digest = this.getDigestValue();
        try {
            byte[] encoded = RSAUtil.encodeSignature(this.digestOID, digest);
            byte[] padded = this.padding.pad(encoded);
            if (padded != null) {
                return RSACore.rsa(padded, this.privateKey, true);
            }
        }
        catch (GeneralSecurityException e) {
            throw new SignatureException("Could not sign data", e);
        }
        throw new SignatureException("Could not sign data");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
        if (this.publicKey == null) {
            throw new SignatureException("Missing public key");
        }
        try {
            if (sigBytes.length != RSACore.getByteLength(this.publicKey)) {
                throw new SignatureException("Bad signature length: got " + sigBytes.length + " but was expecting " + RSACore.getByteLength(this.publicKey));
            }
            byte[] decrypted = RSACore.rsa(sigBytes, this.publicKey);
            byte[] digest = this.getDigestValue();
            byte[] encoded = RSAUtil.encodeSignature(this.digestOID, digest);
            byte[] padded = this.padding.pad(encoded);
            if (MessageDigest.isEqual(padded, decrypted)) {
                boolean bl = true;
                return bl;
            }
            encoded = RSAUtil.encodeSignatureWithoutNULL(this.digestOID, digest);
            padded = this.padding.pad(encoded);
            boolean bl = MessageDigest.isEqual(padded, decrypted);
            return bl;
        }
        catch (BadPaddingException e) {
            boolean bl = false;
            return bl;
        }
        finally {
            this.resetDigest();
        }
    }

    @Override
    @Deprecated
    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
        throw new UnsupportedOperationException("setParameter() not supported");
    }

    @Override
    protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException("No parameters accepted");
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

    public static final class SHA3_512withRSA
    extends RSASignature {
        public SHA3_512withRSA() {
            super("SHA3-512", AlgorithmId.SHA3_512_oid, 11);
        }
    }

    public static final class SHA3_384withRSA
    extends RSASignature {
        public SHA3_384withRSA() {
            super("SHA3-384", AlgorithmId.SHA3_384_oid, 11);
        }
    }

    public static final class SHA3_256withRSA
    extends RSASignature {
        public SHA3_256withRSA() {
            super("SHA3-256", AlgorithmId.SHA3_256_oid, 11);
        }
    }

    public static final class SHA3_224withRSA
    extends RSASignature {
        public SHA3_224withRSA() {
            super("SHA3-224", AlgorithmId.SHA3_224_oid, 11);
        }
    }

    public static final class SHA512_256withRSA
    extends RSASignature {
        public SHA512_256withRSA() {
            super("SHA-512/256", AlgorithmId.SHA512_256_oid, 11);
        }
    }

    public static final class SHA512_224withRSA
    extends RSASignature {
        public SHA512_224withRSA() {
            super("SHA-512/224", AlgorithmId.SHA512_224_oid, 11);
        }
    }

    public static final class SHA512withRSA
    extends RSASignature {
        public SHA512withRSA() {
            super("SHA-512", AlgorithmId.SHA512_oid, 11);
        }
    }

    public static final class SHA384withRSA
    extends RSASignature {
        public SHA384withRSA() {
            super("SHA-384", AlgorithmId.SHA384_oid, 11);
        }
    }

    public static final class SHA256withRSA
    extends RSASignature {
        public SHA256withRSA() {
            super("SHA-256", AlgorithmId.SHA256_oid, 11);
        }
    }

    public static final class SHA224withRSA
    extends RSASignature {
        public SHA224withRSA() {
            super("SHA-224", AlgorithmId.SHA224_oid, 11);
        }
    }

    public static final class SHA1withRSA
    extends RSASignature {
        public SHA1withRSA() {
            super("SHA-1", AlgorithmId.SHA_oid, 7);
        }
    }

    public static final class MD5withRSA
    extends RSASignature {
        public MD5withRSA() {
            super("MD5", AlgorithmId.MD5_oid, 10);
        }
    }

    public static final class MD2withRSA
    extends RSASignature {
        public MD2withRSA() {
            super("MD2", AlgorithmId.MD2_oid, 10);
        }
    }
}

