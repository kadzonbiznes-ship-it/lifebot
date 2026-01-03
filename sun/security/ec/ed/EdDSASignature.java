/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec.ed;

import java.io.ByteArrayOutputStream;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.EdECPrivateKey;
import java.security.interfaces.EdECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.EdDSAParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.util.function.Function;
import sun.security.ec.ed.EdDSAOperations;
import sun.security.ec.ed.EdDSAParameters;
import sun.security.ec.ed.EdDSAPublicKeyImpl;
import sun.security.ec.point.AffinePoint;

public class EdDSASignature
extends SignatureSpi {
    private byte[] privateKey;
    private AffinePoint publicKeyPoint;
    private byte[] publicKeyBytes;
    private EdDSAOperations ops;
    private EdDSAParameters lockedParams = null;
    private MessageAccumulator message = null;
    private EdDSAParameterSpec sigParams = new EdDSAParameterSpec(false);

    public EdDSASignature() {
    }

    EdDSASignature(NamedParameterSpec paramSpec) {
        this.lockedParams = EdDSAParameters.get(ProviderException::new, paramSpec);
    }

    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        if (!(publicKey instanceof EdECPublicKey)) {
            throw new InvalidKeyException("Unsupported key type");
        }
        EdECPublicKey edKey = (EdECPublicKey)publicKey;
        EdDSAParameters params = EdDSAParameters.get(InvalidKeyException::new, edKey.getParams());
        this.initImpl(params);
        this.privateKey = null;
        this.publicKeyPoint = this.ops.decodeAffinePoint(InvalidKeyException::new, edKey.getPoint());
        EdDSAPublicKeyImpl pubKeyImpl = new EdDSAPublicKeyImpl(params, edKey.getPoint());
        this.publicKeyBytes = pubKeyImpl.getEncodedPoint();
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        this.engineInitSign(privateKey, null);
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
        if (!(privateKey instanceof EdECPrivateKey)) {
            throw new InvalidKeyException("Unsupported key type");
        }
        EdECPrivateKey edKey = (EdECPrivateKey)privateKey;
        this.initImpl(edKey.getParams());
        this.privateKey = edKey.getBytes().orElseThrow(() -> new InvalidKeyException("No private key value"));
        this.publicKeyPoint = null;
        this.publicKeyBytes = null;
    }

    private <T extends Throwable> void checkLockedParams(Function<String, T> exception, EdDSAParameters params) throws T {
        if (this.lockedParams != null && this.lockedParams != params) {
            throw (Throwable)exception.apply("Parameters must be " + this.lockedParams.getName());
        }
    }

    private void ensureMessageInit() throws SignatureException {
        if (this.message == null) {
            this.initMessage();
        }
    }

    private void initMessage() throws SignatureException {
        if (this.ops == null) {
            throw new SignatureException("not initialized");
        }
        EdDSAParameters params = this.ops.getParameters();
        this.message = this.sigParams.isPrehash() ? new DigestAccumulator(params.createDigester(64)) : new MemoryAccumulator();
    }

    @Override
    protected void engineUpdate(byte b) throws SignatureException {
        this.ensureMessageInit();
        this.message.add(b);
    }

    @Override
    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
        this.ensureMessageInit();
        this.message.add(b, off, len);
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
        if (this.privateKey == null) {
            throw new SignatureException("Missing private key");
        }
        this.ensureMessageInit();
        byte[] result = this.ops.sign(this.sigParams, this.privateKey, this.message.getMessage());
        this.message = null;
        return result;
    }

    @Override
    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
        if (this.publicKeyBytes == null) {
            throw new SignatureException("Missing publicKey");
        }
        this.ensureMessageInit();
        boolean result = this.ops.verify(this.sigParams, this.publicKeyPoint, this.publicKeyBytes, this.message.getMessage(), sigBytes);
        this.message = null;
        return result;
    }

    private void initImpl(EdDSAParameters params) throws InvalidKeyException {
        this.checkLockedParams(InvalidKeyException::new, params);
        try {
            this.ops = new EdDSAOperations(params);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new ProviderException(ex);
        }
        this.message = null;
    }

    private void initImpl(NamedParameterSpec paramSpec) throws InvalidKeyException {
        EdDSAParameters params = EdDSAParameters.get(InvalidKeyException::new, paramSpec);
        this.initImpl(params);
    }

    @Override
    @Deprecated
    protected Object engineGetParameter(String param) throws InvalidParameterException {
        throw new UnsupportedOperationException("getParameter() not supported");
    }

    @Override
    @Deprecated
    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
        throw new UnsupportedOperationException("setParameter() not supported");
    }

    @Override
    protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        if (params == null) {
            return;
        }
        if (params instanceof EdDSAParameterSpec) {
            if (this.message != null) {
                throw new InvalidParameterException("Cannot change signature parameters during operation");
            }
        } else {
            throw new InvalidAlgorithmParameterException("Only EdDSAParameterSpec supported");
        }
        EdDSAParameterSpec edDsaParams = (EdDSAParameterSpec)params;
        EdDSASignature.checkContextLength(edDsaParams);
        this.sigParams = edDsaParams;
    }

    private static void checkContextLength(EdDSAParameterSpec edDsaParams) throws InvalidAlgorithmParameterException {
        byte[] context;
        if (edDsaParams.getContext().isPresent() && (context = edDsaParams.getContext().get()).length > 255) {
            throw new InvalidAlgorithmParameterException("Context is longer than 255 bytes");
        }
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        return null;
    }

    private static interface MessageAccumulator {
        public void add(byte var1);

        public void add(byte[] var1, int var2, int var3);

        public byte[] getMessage();
    }

    private static class DigestAccumulator
    implements MessageAccumulator {
        private final EdDSAParameters.Digester digester;

        DigestAccumulator(EdDSAParameters.Digester digester) {
            this.digester = digester;
        }

        @Override
        public void add(byte b) {
            this.digester.update(b);
        }

        @Override
        public void add(byte[] data, int off, int len) {
            this.digester.update(data, off, len);
        }

        @Override
        public byte[] getMessage() {
            return this.digester.digest();
        }
    }

    private static class MemoryAccumulator
    implements MessageAccumulator {
        ByteArrayOutputStream message = new ByteArrayOutputStream();

        private MemoryAccumulator() {
        }

        @Override
        public void add(byte b) {
            this.message.write(b);
        }

        @Override
        public void add(byte[] data, int off, int len) {
            this.message.write(data, off, len);
        }

        @Override
        public byte[] getMessage() {
            return this.message.toByteArray();
        }
    }

    public static class Ed448
    extends EdDSASignature {
        public Ed448() {
            super(NamedParameterSpec.ED448);
        }
    }

    public static class Ed25519
    extends EdDSASignature {
        public Ed25519() {
            super(NamedParameterSpec.ED25519);
        }
    }
}

