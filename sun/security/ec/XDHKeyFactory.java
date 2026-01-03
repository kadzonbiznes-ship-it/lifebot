/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.interfaces.XECKey;
import java.security.interfaces.XECPrivateKey;
import java.security.interfaces.XECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.NamedParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.XECPrivateKeySpec;
import java.security.spec.XECPublicKeySpec;
import java.util.Arrays;
import java.util.function.Function;
import sun.security.ec.XDHPrivateKeyImpl;
import sun.security.ec.XDHPublicKeyImpl;
import sun.security.ec.XECParameters;

public class XDHKeyFactory
extends KeyFactorySpi {
    private XECParameters lockedParams = null;

    XDHKeyFactory() {
    }

    protected XDHKeyFactory(AlgorithmParameterSpec paramSpec) {
        this.lockedParams = XECParameters.get(ProviderException::new, paramSpec);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("Key must not be null");
        }
        if (key instanceof XECKey) {
            XECKey xecKey = (XECKey)((Object)key);
            XECParameters params = XECParameters.get(InvalidKeyException::new, xecKey.getParams());
            this.checkLockedParams(InvalidKeyException::new, params);
            if (xecKey instanceof XECPublicKey) {
                XECPublicKey publicKey = (XECPublicKey)xecKey;
                return new XDHPublicKeyImpl(params, publicKey.getU());
            }
            if (xecKey instanceof XECPrivateKey) {
                XECPrivateKey privateKey = (XECPrivateKey)xecKey;
                byte[] scalar = privateKey.getScalar().orElseThrow(() -> new InvalidKeyException("No private key data"));
                return new XDHPrivateKeyImpl(params, scalar);
            }
            throw new InvalidKeyException("Unsupported XECKey subclass");
        }
        if (key instanceof PublicKey && key.getFormat().equals("X.509")) {
            XDHPublicKeyImpl result = new XDHPublicKeyImpl(key.getEncoded());
            this.checkLockedParams(InvalidKeyException::new, result.getParams());
            return result;
        }
        if (key instanceof PrivateKey && key.getFormat().equals("PKCS#8")) {
            byte[] encoded = key.getEncoded();
            try {
                XDHPrivateKeyImpl result = new XDHPrivateKeyImpl(encoded);
                this.checkLockedParams(InvalidKeyException::new, result.getParams());
                XDHPrivateKeyImpl xDHPrivateKeyImpl = result;
                return xDHPrivateKeyImpl;
            }
            finally {
                Arrays.fill(encoded, (byte)0);
            }
        }
        throw new InvalidKeyException("Unsupported key type or format");
    }

    private <T extends Throwable> void checkLockedParams(Function<String, T> exception, AlgorithmParameterSpec spec) throws T {
        XECParameters params = XECParameters.get(exception, spec);
        this.checkLockedParams(exception, params);
    }

    private <T extends Throwable> void checkLockedParams(Function<String, T> exception, XECParameters params) throws T {
        if (this.lockedParams != null && this.lockedParams != params) {
            throw (Throwable)exception.apply("Parameters must be " + this.lockedParams.getName());
        }
    }

    @Override
    protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
        try {
            return this.generatePublicImpl(keySpec);
        }
        catch (InvalidKeyException ex) {
            throw new InvalidKeySpecException(ex);
        }
    }

    @Override
    protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
        try {
            return this.generatePrivateImpl(keySpec);
        }
        catch (InvalidKeyException ex) {
            throw new InvalidKeySpecException(ex);
        }
    }

    private PublicKey generatePublicImpl(KeySpec keySpec) throws InvalidKeyException, InvalidKeySpecException {
        if (keySpec instanceof X509EncodedKeySpec) {
            X509EncodedKeySpec x509Spec = (X509EncodedKeySpec)keySpec;
            XDHPublicKeyImpl result = new XDHPublicKeyImpl(x509Spec.getEncoded());
            this.checkLockedParams(InvalidKeySpecException::new, result.getParams());
            return result;
        }
        if (keySpec instanceof XECPublicKeySpec) {
            XECPublicKeySpec publicKeySpec = (XECPublicKeySpec)keySpec;
            XECParameters params = XECParameters.get(InvalidKeySpecException::new, publicKeySpec.getParams());
            this.checkLockedParams(InvalidKeySpecException::new, params);
            return new XDHPublicKeyImpl(params, publicKeySpec.getU());
        }
        throw new InvalidKeySpecException("Only X509EncodedKeySpec and XECPublicKeySpec are supported");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private PrivateKey generatePrivateImpl(KeySpec keySpec) throws InvalidKeyException, InvalidKeySpecException {
        if (keySpec instanceof PKCS8EncodedKeySpec) {
            PKCS8EncodedKeySpec pkcsSpec = (PKCS8EncodedKeySpec)keySpec;
            byte[] encoded = pkcsSpec.getEncoded();
            try {
                XDHPrivateKeyImpl result = new XDHPrivateKeyImpl(encoded);
                this.checkLockedParams(InvalidKeySpecException::new, result.getParams());
                XDHPrivateKeyImpl xDHPrivateKeyImpl = result;
                return xDHPrivateKeyImpl;
            }
            finally {
                Arrays.fill(encoded, (byte)0);
            }
        }
        if (keySpec instanceof XECPrivateKeySpec) {
            XECPrivateKeySpec privateKeySpec = (XECPrivateKeySpec)keySpec;
            XECParameters params = XECParameters.get(InvalidKeySpecException::new, privateKeySpec.getParams());
            this.checkLockedParams(InvalidKeySpecException::new, params);
            byte[] scalar = privateKeySpec.getScalar();
            try {
                XDHPrivateKeyImpl xDHPrivateKeyImpl = new XDHPrivateKeyImpl(params, scalar);
                return xDHPrivateKeyImpl;
            }
            finally {
                Arrays.fill(scalar, (byte)0);
            }
        }
        throw new InvalidKeySpecException("Only PKCS8EncodedKeySpec and XECPrivateKeySpec supported");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {
        if (key instanceof XECPublicKey) {
            this.checkLockedParams(InvalidKeySpecException::new, ((XECPublicKey)key).getParams());
            if (keySpec.isAssignableFrom(X509EncodedKeySpec.class)) {
                if (!key.getFormat().equals("X.509")) {
                    throw new InvalidKeySpecException("Format is not X.509");
                }
                return (T)((KeySpec)keySpec.cast(new X509EncodedKeySpec(key.getEncoded())));
            }
            if (keySpec.isAssignableFrom(XECPublicKeySpec.class)) {
                XECPublicKey xecKey = (XECPublicKey)key;
                return (T)((KeySpec)keySpec.cast(new XECPublicKeySpec(xecKey.getParams(), xecKey.getU())));
            }
            throw new InvalidKeySpecException("KeySpec must be X509EncodedKeySpec or XECPublicKeySpec");
        }
        if (key instanceof XECPrivateKey) {
            this.checkLockedParams(InvalidKeySpecException::new, ((XECPrivateKey)key).getParams());
            if (keySpec.isAssignableFrom(PKCS8EncodedKeySpec.class)) {
                if (!key.getFormat().equals("PKCS#8")) {
                    throw new InvalidKeySpecException("Format is not PKCS#8");
                }
                byte[] encoded = key.getEncoded();
                try {
                    KeySpec keySpec2 = (KeySpec)keySpec.cast(new PKCS8EncodedKeySpec(encoded));
                    return (T)keySpec2;
                }
                finally {
                    Arrays.fill(encoded, (byte)0);
                }
            }
            if (keySpec.isAssignableFrom(XECPrivateKeySpec.class)) {
                XECPrivateKey xecKey = (XECPrivateKey)key;
                byte[] scalar = xecKey.getScalar().orElseThrow(() -> new InvalidKeySpecException("No private key value"));
                try {
                    KeySpec keySpec3 = (KeySpec)keySpec.cast(new XECPrivateKeySpec(xecKey.getParams(), scalar));
                    return (T)keySpec3;
                }
                finally {
                    Arrays.fill(scalar, (byte)0);
                }
            }
            throw new InvalidKeySpecException("KeySpec must be PKCS8EncodedKeySpec or XECPrivateKeySpec");
        }
        throw new InvalidKeySpecException("Unsupported key type");
    }

    static class X448
    extends XDHKeyFactory {
        public X448() {
            super(NamedParameterSpec.X448);
        }
    }

    static class X25519
    extends XDHKeyFactory {
        public X25519() {
            super(NamedParameterSpec.X25519);
        }
    }
}

