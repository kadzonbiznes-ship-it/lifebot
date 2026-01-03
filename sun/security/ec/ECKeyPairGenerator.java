/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.util.Optional;
import sun.security.ec.ECOperations;
import sun.security.ec.ECPrivateKeyImpl;
import sun.security.jca.JCAUtil;
import sun.security.util.ECUtil;
import sun.security.util.SecurityProviderConstants;
import sun.security.util.math.IntegerFieldModuloP;

public final class ECKeyPairGenerator
extends KeyPairGeneratorSpi {
    private static final int KEY_SIZE_MIN = 112;
    private static final int KEY_SIZE_MAX = 571;
    private SecureRandom random;
    private int keySize;
    private AlgorithmParameterSpec params = null;

    public ECKeyPairGenerator() {
        this.initialize(SecurityProviderConstants.DEF_EC_KEY_SIZE, null);
    }

    @Override
    public void initialize(int keySize, SecureRandom random) {
        this.checkKeySize(keySize);
        this.params = ECUtil.getECParameterSpec(null, keySize);
        if (this.params == null) {
            throw new InvalidParameterException("No EC parameters available for key size " + keySize + " bits");
        }
        this.random = random;
    }

    @Override
    public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        ECParameterSpec ecSpec = null;
        if (params instanceof ECParameterSpec) {
            ECParameterSpec ecParams = (ECParameterSpec)params;
            ecSpec = ECUtil.getECParameterSpec(null, ecParams);
            if (ecSpec == null) {
                throw new InvalidAlgorithmParameterException("Curve not supported: " + String.valueOf(params));
            }
        } else if (params instanceof ECGenParameterSpec) {
            String name = ((ECGenParameterSpec)params).getName();
            ecSpec = ECUtil.getECParameterSpec(null, name);
            if (ecSpec == null) {
                throw new InvalidAlgorithmParameterException("Unknown curve name: " + name);
            }
        } else {
            throw new InvalidAlgorithmParameterException("ECParameterSpec or ECGenParameterSpec required for EC");
        }
        ECKeyPairGenerator.ensureCurveIsSupported(ecSpec);
        this.params = ecSpec;
        this.keySize = ecSpec.getCurve().getField().getFieldSize();
        this.random = random;
    }

    private static void ensureCurveIsSupported(ECParameterSpec ecSpec) throws InvalidAlgorithmParameterException {
        AlgorithmParameters ecParams = ECUtil.getECParameters(null);
        try {
            ecParams.init(ecSpec);
        }
        catch (InvalidParameterSpecException ex) {
            throw new InvalidAlgorithmParameterException("Curve not supported: " + ecSpec.toString());
        }
        if (ECOperations.forParameters(ecSpec).isEmpty()) {
            throw new InvalidAlgorithmParameterException("Curve not supported: " + ecSpec.toString());
        }
    }

    @Override
    public KeyPair generateKeyPair() {
        if (this.random == null) {
            this.random = JCAUtil.getSecureRandom();
        }
        try {
            Optional<KeyPair> kp = this.generateKeyPairImpl(this.random);
            if (kp.isPresent()) {
                return kp.get();
            }
        }
        catch (Exception ex) {
            throw new ProviderException(ex);
        }
        throw new ProviderException("Curve not supported:  " + this.params.toString());
    }

    private byte[] generatePrivateScalar(SecureRandom random, ECOperations ecOps, int seedSize) {
        int numAttempts = 128;
        byte[] seedArr = new byte[seedSize];
        for (int i = 0; i < numAttempts; ++i) {
            random.nextBytes(seedArr);
            try {
                return ecOps.seedToScalar(seedArr);
            }
            catch (ECOperations.IntermediateValueException intermediateValueException) {
                continue;
            }
        }
        throw new ProviderException("Unable to produce private key after " + numAttempts + " attempts");
    }

    private Optional<KeyPair> generateKeyPairImpl(SecureRandom random) throws InvalidKeyException {
        ECParameterSpec ecParams = (ECParameterSpec)this.params;
        Optional<ECOperations> opsOpt = ECOperations.forParameters(ecParams);
        if (opsOpt.isEmpty()) {
            return Optional.empty();
        }
        ECOperations ops = opsOpt.get();
        IntegerFieldModuloP field = ops.getField();
        int numBits = ecParams.getOrder().bitLength();
        int seedBits = numBits + 64;
        int seedSize = (seedBits + 7) / 8;
        byte[] privArr = this.generatePrivateScalar(random, ops, seedSize);
        ECPrivateKeyImpl privateKey = new ECPrivateKeyImpl(privArr, ecParams);
        Arrays.fill(privArr, (byte)0);
        PublicKey publicKey = privateKey.calculatePublicKey();
        return Optional.of(new KeyPair(publicKey, privateKey));
    }

    private void checkKeySize(int keySize) throws InvalidParameterException {
        if (keySize < 112) {
            throw new InvalidParameterException("Key size must be at least 112 bits");
        }
        if (keySize > 571) {
            throw new InvalidParameterException("Key size must be at most 571 bits");
        }
        this.keySize = keySize;
    }
}

