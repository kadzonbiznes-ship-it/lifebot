/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Set;
import sun.security.util.Debug;

public class KeyStoreDelegator
extends KeyStoreSpi {
    private static final String KEYSTORE_TYPE_COMPAT = "keystore.type.compat";
    private static final Debug debug = Debug.getInstance("keystore");
    private final String primaryType;
    private final String secondaryType;
    private final Class<? extends KeyStoreSpi> primaryKeyStore;
    private final Class<? extends KeyStoreSpi> secondaryKeyStore;
    private String type;
    private KeyStoreSpi keystore;
    private final boolean compatModeEnabled;

    public KeyStoreDelegator(String primaryType, Class<? extends KeyStoreSpi> primaryKeyStore, String secondaryType, Class<? extends KeyStoreSpi> secondaryKeyStore) {
        String prop = AccessController.doPrivileged(() -> Security.getProperty(KEYSTORE_TYPE_COMPAT));
        this.compatModeEnabled = "true".equalsIgnoreCase(prop);
        if (this.compatModeEnabled) {
            this.primaryType = primaryType;
            this.secondaryType = secondaryType;
            this.primaryKeyStore = primaryKeyStore;
            this.secondaryKeyStore = secondaryKeyStore;
        } else {
            this.primaryType = primaryType;
            this.secondaryType = null;
            this.primaryKeyStore = primaryKeyStore;
            this.secondaryKeyStore = null;
            if (debug != null) {
                debug.println("WARNING: compatibility mode disabled for " + primaryType + " and " + secondaryType + " keystore types");
            }
        }
    }

    @Override
    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        return this.keystore.engineGetKey(alias, password);
    }

    @Override
    public Certificate[] engineGetCertificateChain(String alias) {
        return this.keystore.engineGetCertificateChain(alias);
    }

    @Override
    public Certificate engineGetCertificate(String alias) {
        return this.keystore.engineGetCertificate(alias);
    }

    @Override
    public Date engineGetCreationDate(String alias) {
        return this.keystore.engineGetCreationDate(alias);
    }

    @Override
    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        this.keystore.engineSetKeyEntry(alias, key, password, chain);
    }

    @Override
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        this.keystore.engineSetKeyEntry(alias, key, chain);
    }

    @Override
    public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        this.keystore.engineSetCertificateEntry(alias, cert);
    }

    @Override
    public void engineDeleteEntry(String alias) throws KeyStoreException {
        this.keystore.engineDeleteEntry(alias);
    }

    @Override
    public Set<KeyStore.Entry.Attribute> engineGetAttributes(String alias) {
        return this.keystore.engineGetAttributes(alias);
    }

    @Override
    public Enumeration<String> engineAliases() {
        return this.keystore.engineAliases();
    }

    @Override
    public boolean engineContainsAlias(String alias) {
        return this.keystore.engineContainsAlias(alias);
    }

    @Override
    public int engineSize() {
        return this.keystore.engineSize();
    }

    @Override
    public boolean engineIsKeyEntry(String alias) {
        return this.keystore.engineIsKeyEntry(alias);
    }

    @Override
    public boolean engineIsCertificateEntry(String alias) {
        return this.keystore.engineIsCertificateEntry(alias);
    }

    @Override
    public String engineGetCertificateAlias(Certificate cert) {
        return this.keystore.engineGetCertificateAlias(cert);
    }

    @Override
    public KeyStore.Entry engineGetEntry(String alias, KeyStore.ProtectionParameter protParam) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        return this.keystore.engineGetEntry(alias, protParam);
    }

    @Override
    public void engineSetEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException {
        this.keystore.engineSetEntry(alias, entry, protParam);
    }

    @Override
    public boolean engineEntryInstanceOf(String alias, Class<? extends KeyStore.Entry> entryClass) {
        return this.keystore.engineEntryInstanceOf(alias, entryClass);
    }

    @Override
    public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        if (debug != null) {
            debug.println("Storing keystore in " + this.type + " format");
        }
        this.keystore.engineStore(stream, password);
    }

    @Override
    public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        if (stream == null) {
            try {
                KeyStoreSpi tmp;
                this.keystore = tmp = this.primaryKeyStore.newInstance();
            }
            catch (IllegalAccessException | InstantiationException tmp) {
                // empty catch block
            }
            this.type = this.primaryType;
            if (debug != null) {
                debug.println("Creating a new keystore in " + this.type + " format");
            }
            this.keystore.engineLoad(stream, password);
        } else {
            block18: {
                BufferedInputStream bufferedStream = new BufferedInputStream(stream);
                ((InputStream)bufferedStream).mark(Integer.MAX_VALUE);
                try {
                    KeyStoreSpi tmp = this.primaryKeyStore.newInstance();
                    tmp.engineLoad((InputStream)bufferedStream, password);
                    this.keystore = tmp;
                    this.type = this.primaryType;
                }
                catch (Exception e) {
                    if (e instanceof IOException && e.getCause() instanceof UnrecoverableKeyException) {
                        throw (IOException)e;
                    }
                    try {
                        if (!this.compatModeEnabled) {
                            throw e;
                        }
                        KeyStoreSpi tmp = this.secondaryKeyStore.newInstance();
                        ((InputStream)bufferedStream).reset();
                        tmp.engineLoad((InputStream)bufferedStream, password);
                        this.keystore = tmp;
                        this.type = this.secondaryType;
                        if (debug != null) {
                            debug.println("WARNING: switching from " + this.primaryType + " to " + this.secondaryType + " keystore file format has altered the keystore security level");
                        }
                    }
                    catch (IllegalAccessException | InstantiationException tmp) {
                    }
                    catch (IOException | NoSuchAlgorithmException | CertificateException e3) {
                        if (e3 instanceof IOException && e3.getCause() instanceof UnrecoverableKeyException) {
                            throw (IOException)e3;
                        }
                        if (e instanceof IOException) {
                            throw (IOException)e;
                        }
                        if (e instanceof CertificateException) {
                            throw (CertificateException)e;
                        }
                        if (e instanceof NoSuchAlgorithmException) {
                            throw (NoSuchAlgorithmException)e;
                        }
                        if (!(e instanceof RuntimeException)) break block18;
                        throw (RuntimeException)e;
                    }
                }
            }
            if (debug != null) {
                debug.println("Loaded a keystore in " + this.type + " format");
            }
        }
    }

    @Override
    public boolean engineProbe(InputStream stream) throws IOException {
        boolean result = false;
        try {
            KeyStoreSpi tmp;
            this.keystore = tmp = this.primaryKeyStore.newInstance();
            this.type = this.primaryType;
            result = this.keystore.engineProbe(stream);
        }
        catch (Exception e) {
            throw new IOException(e);
        }
        finally {
            if (!result) {
                this.type = null;
                this.keystore = null;
            }
        }
        return result;
    }
}

