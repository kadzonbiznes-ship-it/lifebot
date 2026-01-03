/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;
import sun.security.ec.XDHPrivateKeyImpl;
import sun.security.ec.XDHPublicKeyImpl;
import sun.security.ec.XECOperations;
import sun.security.ec.XECParameters;
import sun.security.jca.JCAUtil;

public class XDHKeyPairGenerator
extends KeyPairGeneratorSpi {
    private static final NamedParameterSpec DEFAULT_PARAM_SPEC = NamedParameterSpec.X25519;
    private SecureRandom random = null;
    private XECOperations ops = null;
    private XECParameters lockedParams = null;

    XDHKeyPairGenerator() {
        this.tryInitialize(DEFAULT_PARAM_SPEC);
    }

    private XDHKeyPairGenerator(NamedParameterSpec paramSpec) {
        this.tryInitialize(paramSpec);
        this.lockedParams = this.ops.getParameters();
    }

    private void tryInitialize(NamedParameterSpec paramSpec) {
        try {
            this.initialize(paramSpec, null);
        }
        catch (InvalidAlgorithmParameterException ex) {
            String name = paramSpec.getName();
            throw new ProviderException(name + " not supported");
        }
    }

    @Override
    public void initialize(int keySize, SecureRandom random) {
        XECParameters params = XECParameters.getBySize(InvalidParameterException::new, keySize);
        this.initializeImpl(params, random);
    }

    @Override
    public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        XECParameters xecParams = XECParameters.get(InvalidAlgorithmParameterException::new, params);
        this.initializeImpl(xecParams, random);
    }

    private void initializeImpl(XECParameters params, SecureRandom random) {
        if (this.lockedParams != null && this.lockedParams != params) {
            throw new InvalidParameterException("Parameters must be " + this.lockedParams.getName());
        }
        this.ops = new XECOperations(params);
        this.random = random == null ? JCAUtil.getSecureRandom() : random;
    }

    @Override
    public KeyPair generateKeyPair() {
        byte[] privateKey = this.ops.generatePrivate(this.random);
        byte[] cloned = (byte[])privateKey.clone();
        BigInteger publicKey = this.ops.computePublic(cloned);
        Arrays.fill(cloned, (byte)0);
        try {
            KeyPair keyPair = new KeyPair(new XDHPublicKeyImpl(this.ops.getParameters(), publicKey), new XDHPrivateKeyImpl(this.ops.getParameters(), privateKey));
            return keyPair;
        }
        catch (InvalidKeyException ex) {
            throw new ProviderException(ex);
        }
        finally {
            Arrays.fill(privateKey, (byte)0);
        }
    }

    static class X448
    extends XDHKeyPairGenerator {
        public X448() {
            super(NamedParameterSpec.X448);
        }
    }

    static class X25519
    extends XDHKeyPairGenerator {
        public X25519() {
            super(NamedParameterSpec.X25519);
        }
    }
}

