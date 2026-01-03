/*
 * Decompiled with CFR 0.152.
 */
package javax.net.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;

public abstract class TrustManagerFactorySpi {
    protected abstract void engineInit(KeyStore var1) throws KeyStoreException;

    protected abstract void engineInit(ManagerFactoryParameters var1) throws InvalidAlgorithmParameterException;

    protected abstract TrustManager[] engineGetTrustManagers();
}

