/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivilegedAction;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import sun.security.action.GetPropertyAction;
import sun.security.action.OpenFileInputStreamAction;
import sun.security.ssl.SSLLogger;
import sun.security.util.FilePaths;
import sun.security.validator.TrustStoreUtil;

final class TrustStoreManager {
    private static final TrustAnchorManager tam = new TrustAnchorManager();

    private TrustStoreManager() {
    }

    public static Set<X509Certificate> getTrustedCerts() throws Exception {
        return tam.getTrustedCerts(TrustStoreDescriptor.createInstance());
    }

    public static KeyStore getTrustedKeyStore() throws Exception {
        return tam.getKeyStore(TrustStoreDescriptor.createInstance());
    }

    private static final class TrustAnchorManager {
        private TrustStoreDescriptor descriptor = null;
        private WeakReference<KeyStore> ksRef;
        private WeakReference<Set<X509Certificate>> csRef;
        private final ReentrantLock tamLock = new ReentrantLock();

        private TrustAnchorManager() {
            this.ksRef = new WeakReference<Object>(null);
            this.csRef = new WeakReference<Object>(null);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        KeyStore getKeyStore(TrustStoreDescriptor descriptor) throws Exception {
            TrustStoreDescriptor temporaryDesc = this.descriptor;
            KeyStore ks = (KeyStore)this.ksRef.get();
            if (ks != null && descriptor.equals(temporaryDesc)) {
                return ks;
            }
            this.tamLock.lock();
            try {
                ks = (KeyStore)this.ksRef.get();
                if (ks != null && descriptor.equals(temporaryDesc)) {
                    KeyStore keyStore = ks;
                    return keyStore;
                }
                if (SSLLogger.isOn && SSLLogger.isOn("trustmanager")) {
                    SSLLogger.fine("Reload the trust store", new Object[0]);
                }
                ks = TrustAnchorManager.loadKeyStore(descriptor);
                this.descriptor = descriptor;
                this.ksRef = new WeakReference<KeyStore>(ks);
            }
            finally {
                this.tamLock.unlock();
            }
            return ks;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        Set<X509Certificate> getTrustedCerts(TrustStoreDescriptor descriptor) throws Exception {
            KeyStore ks = null;
            TrustStoreDescriptor temporaryDesc = this.descriptor;
            Set<X509Certificate> certs = (Set<X509Certificate>)this.csRef.get();
            if (certs != null && descriptor.equals(temporaryDesc)) {
                return certs;
            }
            this.tamLock.lock();
            try {
                temporaryDesc = this.descriptor;
                certs = (Set)this.csRef.get();
                if (certs != null) {
                    if (descriptor.equals(temporaryDesc)) {
                        Set<X509Certificate> set = certs;
                        return set;
                    }
                    this.descriptor = descriptor;
                } else if (descriptor.equals(temporaryDesc)) {
                    ks = (KeyStore)this.ksRef.get();
                } else {
                    this.descriptor = descriptor;
                }
                if (ks == null) {
                    if (SSLLogger.isOn && SSLLogger.isOn("trustmanager")) {
                        SSLLogger.fine("Reload the trust store", new Object[0]);
                    }
                    ks = TrustAnchorManager.loadKeyStore(descriptor);
                    this.ksRef = new WeakReference<KeyStore>(ks);
                }
                if (SSLLogger.isOn && SSLLogger.isOn("trustmanager")) {
                    SSLLogger.fine("Reload trust certs", new Object[0]);
                }
                certs = TrustAnchorManager.loadTrustedCerts(ks);
                if (SSLLogger.isOn && SSLLogger.isOn("trustmanager")) {
                    SSLLogger.fine("Reloaded " + certs.size() + " trust certs", new Object[0]);
                }
                this.csRef = new WeakReference<Set<X509Certificate>>(certs);
            }
            finally {
                this.tamLock.unlock();
            }
            return certs;
        }

        private static KeyStore loadKeyStore(TrustStoreDescriptor descriptor) throws Exception {
            KeyStore ks;
            block13: {
                if (!"NONE".equals(descriptor.storeName) && descriptor.storeFile == null) {
                    if (SSLLogger.isOn && SSLLogger.isOn("trustmanager")) {
                        SSLLogger.fine("No available key store", new Object[0]);
                    }
                    return null;
                }
                ks = descriptor.storeProvider.isEmpty() ? KeyStore.getInstance(descriptor.storeType) : KeyStore.getInstance(descriptor.storeType, descriptor.storeProvider);
                char[] password = null;
                if (!descriptor.storePassword.isEmpty()) {
                    password = descriptor.storePassword.toCharArray();
                }
                if (!"NONE".equals(descriptor.storeName)) {
                    try (FileInputStream fis = AccessController.doPrivileged(new OpenFileInputStreamAction(descriptor.storeFile));){
                        ks.load(fis, password);
                        break block13;
                    }
                    catch (FileNotFoundException fnfe) {
                        if (SSLLogger.isOn && SSLLogger.isOn("trustmanager")) {
                            SSLLogger.fine("Not available key store: " + descriptor.storeName, new Object[0]);
                        }
                        return null;
                    }
                }
                ks.load(null, password);
            }
            return ks;
        }

        private static Set<X509Certificate> loadTrustedCerts(KeyStore ks) {
            if (ks == null) {
                return Collections.emptySet();
            }
            return TrustStoreUtil.getTrustedCerts(ks);
        }
    }

    private static final class TrustStoreDescriptor {
        private static final String fileSep = File.separator;
        private static final String defaultStorePath = GetPropertyAction.privilegedGetProperty("java.home") + fileSep + "lib" + fileSep + "security";
        private static final String defaultStore = FilePaths.cacerts();
        private static final String jsseDefaultStore = defaultStorePath + fileSep + "jssecacerts";
        private final String storeName;
        private final String storeType;
        private final String storeProvider;
        private final String storePassword;
        private final File storeFile;
        private final long lastModified;

        private TrustStoreDescriptor(String storeName, String storeType, String storeProvider, String storePassword, File storeFile, long lastModified) {
            this.storeName = storeName;
            this.storeType = storeType;
            this.storeProvider = storeProvider;
            this.storePassword = storePassword;
            this.storeFile = storeFile;
            this.lastModified = lastModified;
            if (SSLLogger.isOn && SSLLogger.isOn("trustmanager")) {
                SSLLogger.fine("trustStore is: " + storeName + "\ntrustStore type is: " + storeType + "\ntrustStore provider is: " + storeProvider + "\nthe last modified time is: " + new Date(lastModified), new Object[0]);
            }
        }

        static TrustStoreDescriptor createInstance() {
            return AccessController.doPrivileged(new PrivilegedAction<TrustStoreDescriptor>(){

                @Override
                public TrustStoreDescriptor run() {
                    String storePropName = System.getProperty("javax.net.ssl.trustStore", jsseDefaultStore);
                    String storePropType = System.getProperty("javax.net.ssl.trustStoreType", KeyStore.getDefaultType());
                    String storePropProvider = System.getProperty("javax.net.ssl.trustStoreProvider", "");
                    String storePropPassword = System.getProperty("javax.net.ssl.trustStorePassword", "");
                    String temporaryName = "";
                    File temporaryFile = null;
                    long temporaryTime = 0L;
                    if (!"NONE".equals(storePropName)) {
                        String[] fileNames;
                        for (String fileName : fileNames = new String[]{storePropName, defaultStore}) {
                            File f = new File(fileName);
                            if (f.isFile() && f.canRead()) {
                                temporaryName = fileName;
                                temporaryFile = f;
                                temporaryTime = f.lastModified();
                                break;
                            }
                            if (!SSLLogger.isOn || !SSLLogger.isOn("trustmanager")) continue;
                            SSLLogger.fine("Inaccessible trust store: " + fileName, new Object[0]);
                        }
                    } else {
                        temporaryName = storePropName;
                    }
                    return new TrustStoreDescriptor(temporaryName, storePropType, storePropProvider, storePropPassword, temporaryFile, temporaryTime);
                }
            });
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof TrustStoreDescriptor) {
                TrustStoreDescriptor that = (TrustStoreDescriptor)obj;
                return this.lastModified == that.lastModified && Objects.equals(this.storeName, that.storeName) && Objects.equals(this.storeType, that.storeType) && Objects.equals(this.storeProvider, that.storeProvider);
            }
            return false;
        }

        public int hashCode() {
            int result = 17;
            if (this.storeName != null && !this.storeName.isEmpty()) {
                result = 31 * result + this.storeName.hashCode();
            }
            if (this.storeType != null && !this.storeType.isEmpty()) {
                result = 31 * result + this.storeType.hashCode();
            }
            if (this.storeProvider != null && !this.storeProvider.isEmpty()) {
                result = 31 * result + this.storeProvider.hashCode();
            }
            if (this.storeFile != null) {
                result = 31 * result + this.storeFile.hashCode();
            }
            if (this.lastModified != 0L) {
                result = (int)((long)(31 * result) + this.lastModified);
            }
            return result;
        }
    }
}

