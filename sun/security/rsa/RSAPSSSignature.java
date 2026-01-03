/*
 * Decompiled with CFR 0.152.
 */
package sun.security.rsa;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.DigestException;
import java.security.GeneralSecurityException;
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
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Arrays;
import java.util.Hashtable;
import javax.crypto.BadPaddingException;
import sun.security.jca.JCAUtil;
import sun.security.rsa.MGF1;
import sun.security.rsa.PSSParameters;
import sun.security.rsa.RSACore;
import sun.security.rsa.RSAKeyFactory;
import sun.security.rsa.RSAPrivateCrtKeyImpl;
import sun.security.util.KnownOIDs;

public class RSAPSSSignature
extends SignatureSpi {
    private static final boolean DEBUG = false;
    private static final byte[] EIGHT_BYTES_OF_ZEROS = new byte[8];
    private static final Hashtable<KnownOIDs, Integer> DIGEST_LENGTHS = new Hashtable();
    private MessageDigest md = null;
    private boolean digestReset = true;
    private RSAPrivateKey privKey = null;
    private RSAPublicKey pubKey = null;
    private PSSParameterSpec sigParams = null;
    private SecureRandom random;

    private boolean isDigestEqual(String stdAlg, String givenAlg) {
        if (stdAlg == null || givenAlg == null) {
            return false;
        }
        if (givenAlg.contains("-")) {
            return stdAlg.equalsIgnoreCase(givenAlg);
        }
        if (stdAlg.equals("SHA-1")) {
            return givenAlg.equalsIgnoreCase("SHA") || givenAlg.equalsIgnoreCase("SHA1");
        }
        StringBuilder sb = new StringBuilder(givenAlg);
        if (givenAlg.regionMatches(true, 0, "SHA", 0, 3)) {
            givenAlg = sb.insert(3, "-").toString();
            return stdAlg.equalsIgnoreCase(givenAlg);
        }
        throw new ProviderException("Unsupported digest algorithm " + givenAlg);
    }

    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        if (!(publicKey instanceof RSAPublicKey)) {
            throw new InvalidKeyException("key must be RSAPublicKey");
        }
        RSAPublicKey rsaPubKey = (RSAPublicKey)publicKey;
        this.isPublicKeyValid(rsaPubKey);
        this.pubKey = rsaPubKey;
        this.privKey = null;
        this.resetDigest();
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        this.engineInitSign(privateKey, null);
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
        if (!(privateKey instanceof RSAPrivateKey)) {
            throw new InvalidKeyException("key must be RSAPrivateKey");
        }
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)privateKey;
        this.isPrivateKeyValid(rsaPrivateKey);
        this.privKey = rsaPrivateKey;
        this.pubKey = null;
        this.random = random == null ? JCAUtil.getSecureRandom() : random;
        this.resetDigest();
    }

    private static boolean isCompatible(AlgorithmParameterSpec keyParams, PSSParameterSpec sigParams) {
        if (keyParams == null) {
            return true;
        }
        if (!(keyParams instanceof PSSParameterSpec)) {
            return false;
        }
        PSSParameterSpec pssKeyParams = (PSSParameterSpec)keyParams;
        if (sigParams == null) {
            return true;
        }
        if (pssKeyParams.getSaltLength() > sigParams.getSaltLength()) {
            return false;
        }
        PSSParameterSpec keyParams2 = new PSSParameterSpec(pssKeyParams.getDigestAlgorithm(), pssKeyParams.getMGFAlgorithm(), pssKeyParams.getMGFParameters(), sigParams.getSaltLength(), pssKeyParams.getTrailerField());
        PSSParameters ap = new PSSParameters();
        try {
            ap.engineInit(keyParams2);
            byte[] encoded = ap.engineGetEncoded();
            ap.engineInit(sigParams);
            byte[] encoded2 = ap.engineGetEncoded();
            return Arrays.equals(encoded, encoded2);
        }
        catch (Exception e) {
            return false;
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void isPrivateKeyValid(RSAPrivateKey prKey) throws InvalidKeyException {
        try {
            if (prKey instanceof RSAPrivateCrtKey) {
                RSAPrivateCrtKey crtKey = (RSAPrivateCrtKey)prKey;
                if (!RSAPrivateCrtKeyImpl.checkComponents(crtKey)) throw new InvalidKeyException("Some of the CRT-specific components are not available");
                RSAKeyFactory.checkRSAProviderKeyLengths(crtKey.getModulus().bitLength(), crtKey.getPublicExponent());
            } else {
                RSAKeyFactory.checkRSAProviderKeyLengths(prKey.getModulus().bitLength(), null);
            }
        }
        catch (InvalidKeyException ikEx) {
            throw ikEx;
        }
        catch (Exception e) {
            throw new InvalidKeyException("Can not access private key components", e);
        }
        this.isValid(prKey);
    }

    private void isPublicKeyValid(RSAPublicKey pKey) throws InvalidKeyException {
        try {
            RSAKeyFactory.checkRSAProviderKeyLengths(pKey.getModulus().bitLength(), pKey.getPublicExponent());
        }
        catch (InvalidKeyException ikEx) {
            throw ikEx;
        }
        catch (Exception e) {
            throw new InvalidKeyException("Can not access public key components", e);
        }
        this.isValid(pKey);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void isValid(RSAKey rsaKey) throws InvalidKeyException {
        if (!RSAPSSSignature.isCompatible(rsaKey.getParams(), this.sigParams)) {
            throw new InvalidKeyException("Key contains incompatible PSS parameter values");
        }
        if (this.sigParams == null) return;
        String digestAlgo = this.sigParams.getDigestAlgorithm();
        KnownOIDs ko = KnownOIDs.findMatch(digestAlgo);
        if (ko == null) throw new ProviderException("Unrecognized digest algo: " + digestAlgo);
        Integer hLen = DIGEST_LENGTHS.get((Object)ko);
        if (hLen == null) throw new ProviderException("Unsupported digest algo: " + digestAlgo);
        RSAPSSSignature.checkKeyLength(rsaKey, hLen, this.sigParams.getSaltLength());
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private PSSParameterSpec validateSigParams(AlgorithmParameterSpec p) throws InvalidAlgorithmParameterException {
        Key key;
        if (p == null) {
            throw new InvalidAlgorithmParameterException("Parameters cannot be null");
        }
        if (!(p instanceof PSSParameterSpec)) {
            throw new InvalidAlgorithmParameterException("parameters must be type PSSParameterSpec");
        }
        PSSParameterSpec params = (PSSParameterSpec)p;
        if (params == this.sigParams) {
            return params;
        }
        Key key2 = key = this.privKey == null ? this.pubKey : this.privKey;
        if (key != null && !RSAPSSSignature.isCompatible(key.getParams(), params)) {
            throw new InvalidAlgorithmParameterException("Signature parameters does not match key parameters");
        }
        if (!params.getMGFAlgorithm().equalsIgnoreCase("MGF1")) {
            throw new InvalidAlgorithmParameterException("Only supports MGF1");
        }
        if (params.getTrailerField() != 1) {
            throw new InvalidAlgorithmParameterException("Only supports TrailerFieldBC(1)");
        }
        if (key == null) return params;
        String digestAlgo = params.getDigestAlgorithm();
        KnownOIDs ko = KnownOIDs.findMatch(digestAlgo);
        if (ko == null) throw new InvalidAlgorithmParameterException("Unrecognized digest algo: " + digestAlgo);
        Integer hLen = DIGEST_LENGTHS.get((Object)ko);
        if (hLen == null) throw new InvalidAlgorithmParameterException("Unsupported digest algo: " + digestAlgo);
        try {
            RSAPSSSignature.checkKeyLength((RSAKey)((Object)key), hLen, params.getSaltLength());
            return params;
        }
        catch (InvalidKeyException e) {
            throw new InvalidAlgorithmParameterException(e);
        }
    }

    private void ensureInit() throws SignatureException {
        Key key;
        Key key2 = key = this.privKey == null ? this.pubKey : this.privKey;
        if (key == null) {
            throw new SignatureException("Missing key");
        }
        if (this.sigParams == null) {
            throw new SignatureException("Parameters required for RSASSA-PSS signatures");
        }
    }

    private static void checkKeyLength(RSAKey key, int digestLen, int saltLen) throws InvalidKeyException {
        int minLength;
        int keyLength;
        if (key != null && (keyLength = RSAPSSSignature.getKeyLengthInBits(key) + 7 >> 3) < (minLength = Math.addExact(Math.addExact(digestLen, saltLen), 2))) {
            throw new InvalidKeyException("Key is too short, need min " + minLength + " bytes");
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
        this.ensureInit();
        this.md.update(b);
        this.digestReset = false;
    }

    @Override
    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
        this.ensureInit();
        this.md.update(b, off, len);
        this.digestReset = false;
    }

    @Override
    protected void engineUpdate(ByteBuffer b) {
        try {
            this.ensureInit();
        }
        catch (SignatureException se) {
            throw new RuntimeException(se.getMessage());
        }
        this.md.update(b);
        this.digestReset = false;
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
        this.ensureInit();
        byte[] mHash = this.getDigestValue();
        try {
            byte[] encoded = this.encodeSignature(mHash);
            return RSACore.rsa(encoded, this.privKey, true);
        }
        catch (GeneralSecurityException e) {
            throw new SignatureException("Could not sign data", e);
        }
        catch (IOException e) {
            throw new SignatureException("Could not encode data", e);
        }
    }

    @Override
    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
        this.ensureInit();
        try {
            if (sigBytes.length != RSACore.getByteLength(this.pubKey)) {
                throw new SignatureException("Signature length not correct: got " + sigBytes.length + " but was expecting " + RSACore.getByteLength(this.pubKey));
            }
            byte[] mHash = this.getDigestValue();
            byte[] decrypted = RSACore.rsa(sigBytes, this.pubKey);
            boolean bl = this.decodeSignature(mHash, decrypted);
            return bl;
        }
        catch (BadPaddingException e) {
            boolean bl = false;
            return bl;
        }
        catch (IOException e) {
            throw new SignatureException("Signature encoding error", e);
        }
        finally {
            this.resetDigest();
        }
    }

    private static int getKeyLengthInBits(RSAKey k) {
        if (k != null) {
            return k.getModulus().bitLength();
        }
        return -1;
    }

    private byte[] encodeSignature(byte[] mHash) throws IOException, DigestException {
        AlgorithmParameterSpec mgfParams = this.sigParams.getMGFParameters();
        String mgfDigestAlgo = mgfParams != null ? ((MGF1ParameterSpec)mgfParams).getDigestAlgorithm() : this.md.getAlgorithm();
        try {
            int emBits = RSAPSSSignature.getKeyLengthInBits(this.privKey) - 1;
            int emLen = emBits + 7 >> 3;
            int hLen = this.md.getDigestLength();
            int dbLen = emLen - hLen - 1;
            int sLen = this.sigParams.getSaltLength();
            byte[] em = new byte[emLen];
            em[dbLen - sLen - 1] = 1;
            em[em.length - 1] = -68;
            if (!this.digestReset) {
                throw new ProviderException("Digest should be reset");
            }
            this.md.update(EIGHT_BYTES_OF_ZEROS);
            this.digestReset = false;
            this.md.update(mHash);
            if (sLen != 0) {
                byte[] salt = new byte[sLen];
                this.random.nextBytes(salt);
                this.md.update(salt);
                System.arraycopy(salt, 0, em, dbLen - sLen, sLen);
            }
            this.md.digest(em, dbLen, hLen);
            this.digestReset = true;
            MGF1 mgf1 = new MGF1(mgfDigestAlgo);
            mgf1.generateAndXor(em, dbLen, hLen, dbLen, em, 0);
            int numZeroBits = (emLen << 3) - emBits;
            if (numZeroBits != 0) {
                byte MASK = (byte)(255 >>> numZeroBits);
                em[0] = (byte)(em[0] & MASK);
            }
            return em;
        }
        catch (NoSuchAlgorithmException e) {
            throw new IOException(e.toString());
        }
    }

    private boolean decodeSignature(byte[] mHash, byte[] em) throws IOException {
        int i;
        byte MASK;
        int hLen = mHash.length;
        int sLen = this.sigParams.getSaltLength();
        int emBits = RSAPSSSignature.getKeyLengthInBits(this.pubKey) - 1;
        int emLen = emBits + 7 >> 3;
        int emOfs = em.length - emLen;
        if (emOfs == 1 && em[0] != 0) {
            return false;
        }
        if (emLen < hLen + sLen + 2) {
            return false;
        }
        if (em[emOfs + emLen - 1] != -68) {
            return false;
        }
        int numZeroBits = (emLen << 3) - emBits;
        if (numZeroBits != 0 && (em[emOfs] & (MASK = (byte)(255 << 8 - numZeroBits))) != 0) {
            return false;
        }
        AlgorithmParameterSpec mgfParams = this.sigParams.getMGFParameters();
        String mgfDigestAlgo = mgfParams != null ? ((MGF1ParameterSpec)mgfParams).getDigestAlgorithm() : this.md.getAlgorithm();
        int dbLen = emLen - hLen - 1;
        try {
            MGF1 mgf1 = new MGF1(mgfDigestAlgo);
            mgf1.generateAndXor(em, emOfs + dbLen, hLen, dbLen, em, emOfs);
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new IOException(nsae.toString());
        }
        if (numZeroBits != 0) {
            byte MASK2 = (byte)(255 >>> numZeroBits);
            em[emOfs] = (byte)(em[emOfs] & MASK2);
        }
        for (i = emOfs; i < emOfs + (dbLen - sLen - 1); ++i) {
            if (em[i] == 0) continue;
            return false;
        }
        if (em[i] != 1) {
            return false;
        }
        this.md.update(EIGHT_BYTES_OF_ZEROS);
        this.digestReset = false;
        this.md.update(mHash);
        if (sLen > 0) {
            this.md.update(em, emOfs + (dbLen - sLen), sLen);
        }
        byte[] digest2 = this.md.digest();
        this.digestReset = true;
        byte[] digestInEM = Arrays.copyOfRange(em, emOfs + dbLen, emOfs + emLen - 1);
        return MessageDigest.isEqual(digest2, digestInEM);
    }

    @Override
    @Deprecated
    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
        throw new UnsupportedOperationException("setParameter() not supported");
    }

    @Override
    protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        this.sigParams = this.validateSigParams(params);
        if (!this.digestReset) {
            throw new ProviderException("Cannot set parameters during operations");
        }
        String newHashAlg = this.sigParams.getDigestAlgorithm();
        if (this.md == null || !this.md.getAlgorithm().equalsIgnoreCase(newHashAlg)) {
            try {
                this.md = MessageDigest.getInstance(newHashAlg);
            }
            catch (NoSuchAlgorithmException nsae) {
                throw new InvalidAlgorithmParameterException("Unsupported digest algorithm " + newHashAlg, nsae);
            }
        }
    }

    @Override
    @Deprecated
    protected Object engineGetParameter(String param) throws InvalidParameterException {
        throw new UnsupportedOperationException("getParameter() not supported");
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        AlgorithmParameters ap = null;
        if (this.sigParams != null) {
            try {
                ap = AlgorithmParameters.getInstance("RSASSA-PSS");
                ap.init(this.sigParams);
            }
            catch (GeneralSecurityException gse) {
                throw new ProviderException(gse.getMessage());
            }
        }
        return ap;
    }

    static {
        DIGEST_LENGTHS.put(KnownOIDs.SHA_1, 20);
        DIGEST_LENGTHS.put(KnownOIDs.SHA_224, 28);
        DIGEST_LENGTHS.put(KnownOIDs.SHA_256, 32);
        DIGEST_LENGTHS.put(KnownOIDs.SHA_384, 48);
        DIGEST_LENGTHS.put(KnownOIDs.SHA_512, 64);
        DIGEST_LENGTHS.put(KnownOIDs.SHA_512$224, 28);
        DIGEST_LENGTHS.put(KnownOIDs.SHA_512$256, 32);
        DIGEST_LENGTHS.put(KnownOIDs.SHA3_224, 28);
        DIGEST_LENGTHS.put(KnownOIDs.SHA3_256, 32);
        DIGEST_LENGTHS.put(KnownOIDs.SHA3_384, 48);
        DIGEST_LENGTHS.put(KnownOIDs.SHA3_512, 64);
    }
}

