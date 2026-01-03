/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public abstract class KeyStoreSpi {
    public abstract Key engineGetKey(String var1, char[] var2) throws NoSuchAlgorithmException, UnrecoverableKeyException;

    public abstract Certificate[] engineGetCertificateChain(String var1);

    public abstract Certificate engineGetCertificate(String var1);

    public abstract Date engineGetCreationDate(String var1);

    public abstract void engineSetKeyEntry(String var1, Key var2, char[] var3, Certificate[] var4) throws KeyStoreException;

    public abstract void engineSetKeyEntry(String var1, byte[] var2, Certificate[] var3) throws KeyStoreException;

    public abstract void engineSetCertificateEntry(String var1, Certificate var2) throws KeyStoreException;

    public abstract void engineDeleteEntry(String var1) throws KeyStoreException;

    public abstract Enumeration<String> engineAliases();

    public abstract boolean engineContainsAlias(String var1);

    public abstract int engineSize();

    public abstract boolean engineIsKeyEntry(String var1);

    public abstract boolean engineIsCertificateEntry(String var1);

    public abstract String engineGetCertificateAlias(Certificate var1);

    public abstract void engineStore(OutputStream var1, char[] var2) throws IOException, NoSuchAlgorithmException, CertificateException;

    public void engineStore(KeyStore.LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        throw new UnsupportedOperationException();
    }

    public abstract void engineLoad(InputStream var1, char[] var2) throws IOException, NoSuchAlgorithmException, CertificateException;

    public void engineLoad(KeyStore.LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        this.engineLoad(null, param);
    }

    void engineLoad(InputStream stream, KeyStore.LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        char[] password;
        if (param == null) {
            this.engineLoad(null, (char[])null);
            return;
        }
        KeyStore.ProtectionParameter protection = param.getProtectionParameter();
        if (protection instanceof KeyStore.PasswordProtection) {
            password = ((KeyStore.PasswordProtection)protection).getPassword();
        } else if (protection instanceof KeyStore.CallbackHandlerProtection) {
            CallbackHandler handler = ((KeyStore.CallbackHandlerProtection)protection).getCallbackHandler();
            PasswordCallback callback = new PasswordCallback("Password: ", false);
            try {
                handler.handle(new Callback[]{callback});
            }
            catch (UnsupportedCallbackException e) {
                throw new NoSuchAlgorithmException("Could not obtain password", e);
            }
            password = callback.getPassword();
            callback.clearPassword();
            if (password == null) {
                throw new NoSuchAlgorithmException("No password provided");
            }
        } else {
            throw new NoSuchAlgorithmException("ProtectionParameter must be PasswordProtection or CallbackHandlerProtection");
        }
        this.engineLoad(stream, password);
    }

    public Set<KeyStore.Entry.Attribute> engineGetAttributes(String alias) {
        return Collections.emptySet();
    }

    public KeyStore.Entry engineGetEntry(String alias, KeyStore.ProtectionParameter protParam) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        if (!this.engineContainsAlias(alias)) {
            return null;
        }
        if (protParam == null) {
            if (this.engineIsCertificateEntry(alias)) {
                return new KeyStore.TrustedCertificateEntry(this.engineGetCertificate(alias));
            }
            throw new UnrecoverableKeyException("requested entry requires a password");
        }
        if (protParam instanceof KeyStore.PasswordProtection) {
            if (this.engineIsCertificateEntry(alias)) {
                throw new UnsupportedOperationException("trusted certificate entries are not password-protected");
            }
            if (this.engineIsKeyEntry(alias)) {
                KeyStore.PasswordProtection pp = (KeyStore.PasswordProtection)protParam;
                if (pp.getProtectionAlgorithm() != null) {
                    throw new KeyStoreException("unsupported password protection algorithm");
                }
                char[] password = pp.getPassword();
                Key key = this.engineGetKey(alias, password);
                if (key instanceof PrivateKey) {
                    Certificate[] chain = this.engineGetCertificateChain(alias);
                    return new KeyStore.PrivateKeyEntry((PrivateKey)key, chain);
                }
                if (key instanceof SecretKey) {
                    return new KeyStore.SecretKeyEntry((SecretKey)key);
                }
            }
        }
        throw new UnsupportedOperationException();
    }

    public void engineSetEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException {
        if (protParam != null && !(protParam instanceof KeyStore.PasswordProtection)) {
            throw new KeyStoreException("unsupported protection parameter");
        }
        KeyStore.PasswordProtection pProtect = null;
        if (protParam != null && (pProtect = (KeyStore.PasswordProtection)protParam).getProtectionAlgorithm() != null) {
            throw new KeyStoreException("unsupported password protection algorithm");
        }
        if (entry instanceof KeyStore.TrustedCertificateEntry) {
            if (protParam != null && pProtect.getPassword() != null) {
                throw new KeyStoreException("trusted certificate entries are not password-protected");
            }
            KeyStore.TrustedCertificateEntry tce = (KeyStore.TrustedCertificateEntry)entry;
            this.engineSetCertificateEntry(alias, tce.getTrustedCertificate());
            return;
        }
        if (entry instanceof KeyStore.PrivateKeyEntry) {
            if (pProtect == null || pProtect.getPassword() == null) {
                throw new KeyStoreException("non-null password required to create PrivateKeyEntry");
            }
            this.engineSetKeyEntry(alias, ((KeyStore.PrivateKeyEntry)entry).getPrivateKey(), pProtect.getPassword(), ((KeyStore.PrivateKeyEntry)entry).getCertificateChain());
            return;
        }
        if (entry instanceof KeyStore.SecretKeyEntry) {
            if (pProtect == null || pProtect.getPassword() == null) {
                throw new KeyStoreException("non-null password required to create SecretKeyEntry");
            }
            this.engineSetKeyEntry(alias, ((KeyStore.SecretKeyEntry)entry).getSecretKey(), pProtect.getPassword(), null);
            return;
        }
        throw new KeyStoreException("unsupported entry type: " + entry.getClass().getName());
    }

    public boolean engineEntryInstanceOf(String alias, Class<? extends KeyStore.Entry> entryClass) {
        if (entryClass == KeyStore.TrustedCertificateEntry.class) {
            return this.engineIsCertificateEntry(alias);
        }
        if (entryClass == KeyStore.PrivateKeyEntry.class) {
            return this.engineIsKeyEntry(alias) && this.engineGetCertificate(alias) != null;
        }
        if (entryClass == KeyStore.SecretKeyEntry.class) {
            return this.engineIsKeyEntry(alias) && this.engineGetCertificate(alias) == null;
        }
        return false;
    }

    public boolean engineProbe(InputStream stream) throws IOException {
        if (stream == null) {
            throw new NullPointerException("input stream must not be null");
        }
        return false;
    }
}

