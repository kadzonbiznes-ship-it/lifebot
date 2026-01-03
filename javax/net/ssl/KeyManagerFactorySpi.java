/*
 * Decompiled with CFR 0.152.
 */
package javax.net.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.ManagerFactoryParameters;

public abstract class KeyManagerFactorySpi {
    protected abstract void engineInit(KeyStore var1, char[] var2) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException;

    protected abstract void engineInit(ManagerFactoryParameters var1) throws InvalidAlgorithmParameterException;

    protected abstract KeyManager[] engineGetKeyManagers();
}

