/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertPathParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509Certificate;
import java.util.Collection;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.TrustStoreManager;
import sun.security.ssl.X509TrustManagerImpl;
import sun.security.validator.TrustStoreUtil;

abstract class TrustManagerFactoryImpl
extends TrustManagerFactorySpi {
    private X509TrustManager trustManager = null;
    private boolean isInitialized = false;

    TrustManagerFactoryImpl() {
    }

    /*
     * Unable to fully structure code
     */
    @Override
    protected void engineInit(KeyStore ks) throws KeyStoreException {
        if (ks == null) {
            try {
                this.trustManager = this.getInstance(TrustStoreManager.getTrustedCerts());
            }
            catch (SecurityException se) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("trustmanager")) ** GOTO lbl22
                SSLLogger.fine("SunX509: skip default keystore", new Object[]{se});
            }
            catch (Error err) {
                if (SSLLogger.isOn && SSLLogger.isOn("trustmanager")) {
                    SSLLogger.fine("SunX509: skip default keystore", new Object[]{err});
                }
                throw err;
            }
            catch (RuntimeException re) {
                if (SSLLogger.isOn && SSLLogger.isOn("trustmanager")) {
                    SSLLogger.fine("SunX509: skip default keystore", new Object[]{re});
                }
                throw re;
            }
            catch (Exception e) {
                if (SSLLogger.isOn && SSLLogger.isOn("trustmanager")) {
                    SSLLogger.fine("SunX509: skip default keystore", new Object[]{e});
                }
                throw new KeyStoreException("problem accessing trust store", e);
            }
        } else {
            this.trustManager = this.getInstance(TrustStoreUtil.getTrustedCerts(ks));
        }
lbl22:
        // 4 sources

        this.isInitialized = true;
    }

    abstract X509TrustManager getInstance(Collection<X509Certificate> var1);

    abstract X509TrustManager getInstance(ManagerFactoryParameters var1) throws InvalidAlgorithmParameterException;

    @Override
    protected void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
        this.trustManager = this.getInstance(spec);
        this.isInitialized = true;
    }

    @Override
    protected TrustManager[] engineGetTrustManagers() {
        if (!this.isInitialized) {
            throw new IllegalStateException("TrustManagerFactoryImpl is not initialized");
        }
        return new TrustManager[]{this.trustManager};
    }

    public static final class PKIXFactory
    extends TrustManagerFactoryImpl {
        @Override
        X509TrustManager getInstance(Collection<X509Certificate> trustedCerts) {
            return new X509TrustManagerImpl("PKIX", trustedCerts);
        }

        @Override
        X509TrustManager getInstance(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
            if (!(spec instanceof CertPathTrustManagerParameters)) {
                throw new InvalidAlgorithmParameterException("Parameters must be CertPathTrustManagerParameters");
            }
            CertPathParameters params = ((CertPathTrustManagerParameters)spec).getParameters();
            if (!(params instanceof PKIXBuilderParameters)) {
                throw new InvalidAlgorithmParameterException("Encapsulated parameters must be PKIXBuilderParameters");
            }
            PKIXBuilderParameters pkixParams = (PKIXBuilderParameters)params;
            return new X509TrustManagerImpl("PKIX", pkixParams);
        }
    }

    public static final class SimpleFactory
    extends TrustManagerFactoryImpl {
        @Override
        X509TrustManager getInstance(Collection<X509Certificate> trustedCerts) {
            return new X509TrustManagerImpl("Simple", trustedCerts);
        }

        @Override
        X509TrustManager getInstance(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
            throw new InvalidAlgorithmParameterException("SunX509 TrustManagerFactory does not use ManagerFactoryParameters");
        }
    }
}

