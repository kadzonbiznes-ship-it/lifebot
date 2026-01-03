/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.interfaces.XECPrivateKey;
import java.security.interfaces.XECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.util.function.Function;
import javax.crypto.KeyAgreementSpi;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;
import sun.security.ec.XECOperations;
import sun.security.ec.XECParameters;

public class XDHKeyAgreement
extends KeyAgreementSpi {
    private byte[] privateKey;
    private byte[] secret;
    private XECOperations ops;
    private XECParameters lockedParams = null;

    XDHKeyAgreement() {
    }

    XDHKeyAgreement(AlgorithmParameterSpec paramSpec) {
        this.lockedParams = XECParameters.get(ProviderException::new, paramSpec);
    }

    @Override
    protected void engineInit(Key key, SecureRandom random) throws InvalidKeyException {
        this.initImpl(key);
    }

    @Override
    protected void engineInit(Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        XECParameters xecParams;
        this.initImpl(key);
        if (params != null && !(xecParams = XECParameters.get(InvalidAlgorithmParameterException::new, params)).oidEquals(this.ops.getParameters())) {
            throw new InvalidKeyException("Incorrect private key parameters");
        }
    }

    private <T extends Throwable> void checkLockedParams(Function<String, T> exception, XECParameters params) throws T {
        if (this.lockedParams != null && this.lockedParams != params) {
            throw (Throwable)exception.apply("Parameters must be " + this.lockedParams.getName());
        }
    }

    private void initImpl(Key key) throws InvalidKeyException {
        if (!(key instanceof XECPrivateKey)) {
            throw new InvalidKeyException("Unsupported key type");
        }
        XECPrivateKey privateKey = (XECPrivateKey)key;
        XECParameters xecParams = XECParameters.get(InvalidKeyException::new, privateKey.getParams());
        this.checkLockedParams(InvalidKeyException::new, xecParams);
        this.ops = new XECOperations(xecParams);
        this.privateKey = privateKey.getScalar().orElseThrow(() -> new InvalidKeyException("No private key value"));
        this.secret = null;
    }

    @Override
    protected Key engineDoPhase(Key key, boolean lastPhase) throws InvalidKeyException, IllegalStateException {
        if (this.privateKey == null) {
            throw new IllegalStateException("Not initialized");
        }
        if (this.secret != null) {
            throw new IllegalStateException("Phase already executed");
        }
        if (!lastPhase) {
            throw new IllegalStateException("Only two party agreement supported, lastPhase must be true");
        }
        if (!(key instanceof XECPublicKey)) {
            throw new InvalidKeyException("Unsupported key type");
        }
        XECPublicKey publicKey = (XECPublicKey)key;
        XECParameters xecParams = XECParameters.get(InvalidKeyException::new, publicKey.getParams());
        if (!this.ops.getParameters().oidEquals(xecParams)) {
            throw new InvalidKeyException("Public key parameters are not compatible with private key.");
        }
        byte[] computedSecret = this.ops.encodedPointMultiply(this.privateKey, publicKey.getU());
        if (this.allZero(computedSecret)) {
            throw new InvalidKeyException("Point has small order");
        }
        this.secret = computedSecret;
        return null;
    }

    private boolean allZero(byte[] arr) {
        byte orValue = 0;
        for (int i = 0; i < arr.length; ++i) {
            orValue = (byte)(orValue | arr[i]);
        }
        return orValue == 0;
    }

    @Override
    protected byte[] engineGenerateSecret() throws IllegalStateException {
        if (this.secret == null) {
            throw new IllegalStateException("Not initialized correctly");
        }
        byte[] result = this.secret;
        this.secret = null;
        return result;
    }

    @Override
    protected int engineGenerateSecret(byte[] sharedSecret, int offset) throws IllegalStateException, ShortBufferException {
        if (this.secret == null) {
            throw new IllegalStateException("Not initialized correctly");
        }
        int secretLen = this.secret.length;
        if (secretLen > sharedSecret.length - offset) {
            throw new ShortBufferException("Need " + secretLen + " bytes, only " + (sharedSecret.length - offset) + " available");
        }
        System.arraycopy(this.secret, 0, sharedSecret, offset, secretLen);
        this.secret = null;
        return secretLen;
    }

    @Override
    protected SecretKey engineGenerateSecret(String algorithm) throws IllegalStateException, NoSuchAlgorithmException, InvalidKeyException {
        if (algorithm == null) {
            throw new NoSuchAlgorithmException("Algorithm must not be null");
        }
        if (!algorithm.equals("TlsPremasterSecret")) {
            throw new NoSuchAlgorithmException("Only supported for algorithm TlsPremasterSecret");
        }
        return new SecretKeySpec(this.engineGenerateSecret(), algorithm);
    }

    static class X448
    extends XDHKeyAgreement {
        public X448() {
            super(NamedParameterSpec.X448);
        }
    }

    static class X25519
    extends XDHKeyAgreement {
        public X25519() {
            super(NamedParameterSpec.X25519);
        }
    }
}

