/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.KeyStoreBuilderParameters;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.X509ExtendedKeyManager;
import sun.security.ssl.SunX509KeyManagerImpl;
import sun.security.ssl.X509KeyManagerImpl;

abstract class KeyManagerFactoryImpl
extends KeyManagerFactorySpi {
    X509ExtendedKeyManager keyManager;
    boolean isInitialized;

    KeyManagerFactoryImpl() {
    }

    @Override
    protected KeyManager[] engineGetKeyManagers() {
        if (!this.isInitialized) {
            throw new IllegalStateException("KeyManagerFactoryImpl is not initialized");
        }
        return new KeyManager[]{this.keyManager};
    }

    public static final class X509
    extends KeyManagerFactoryImpl {
        @Override
        protected void engineInit(KeyStore ks, char[] password) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
            if (ks == null) {
                this.keyManager = new X509KeyManagerImpl(Collections.emptyList());
            } else {
                try {
                    KeyStore.Builder builder = KeyStore.Builder.newInstance(ks, (KeyStore.ProtectionParameter)new KeyStore.PasswordProtection(password));
                    this.keyManager = new X509KeyManagerImpl(builder);
                }
                catch (RuntimeException e) {
                    throw new KeyStoreException("initialization failed", e);
                }
            }
            this.isInitialized = true;
        }

        @Override
        protected void engineInit(ManagerFactoryParameters params) throws InvalidAlgorithmParameterException {
            if (!(params instanceof KeyStoreBuilderParameters)) {
                throw new InvalidAlgorithmParameterException("Parameters must be instance of KeyStoreBuilderParameters");
            }
            List<KeyStore.Builder> builders = ((KeyStoreBuilderParameters)params).getParameters();
            this.keyManager = new X509KeyManagerImpl(builders);
            this.isInitialized = true;
        }
    }

    public static final class SunX509
    extends KeyManagerFactoryImpl {
        @Override
        protected void engineInit(KeyStore ks, char[] password) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
            this.keyManager = new SunX509KeyManagerImpl(ks, password);
            this.isInitialized = true;
        }

        @Override
        protected void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
            throw new InvalidAlgorithmParameterException("SunX509KeyManager does not use ManagerFactoryParameters");
        }
    }
}

