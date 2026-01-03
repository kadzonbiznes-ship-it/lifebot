/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import sun.security.util.Debug;

public class KeyStore {
    private static final Debug kdebug = Debug.getInstance("keystore");
    private static final Debug pdebug = Debug.getInstance("provider", "Provider");
    private static final boolean skipDebug = Debug.isOn("engine=") && !Debug.isOn("keystore");
    private static final String KEYSTORE_TYPE = "keystore.type";
    private final String type;
    private final Provider provider;
    private final KeyStoreSpi keyStoreSpi;
    private boolean initialized;

    protected KeyStore(KeyStoreSpi keyStoreSpi, Provider provider, String type) {
        this.keyStoreSpi = keyStoreSpi;
        this.provider = provider;
        this.type = type;
        if (!skipDebug && pdebug != null) {
            pdebug.println("KeyStore." + type.toUpperCase() + " type from: " + this.getProviderName());
        }
    }

    private String getProviderName() {
        return this.provider == null ? "(no provider)" : this.provider.getName();
    }

    public static KeyStore getInstance(String type) throws KeyStoreException {
        Objects.requireNonNull(type, "null type name");
        try {
            Object[] objs = Security.getImpl(type, "KeyStore", (String)null);
            return new KeyStore((KeyStoreSpi)objs[0], (Provider)objs[1], type);
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new KeyStoreException(type + " not found", e);
        }
    }

    public static KeyStore getInstance(String type, String provider) throws KeyStoreException, NoSuchProviderException {
        Objects.requireNonNull(type, "null type name");
        if (provider == null || provider.isEmpty()) {
            throw new IllegalArgumentException("missing provider");
        }
        try {
            Object[] objs = Security.getImpl(type, "KeyStore", provider);
            return new KeyStore((KeyStoreSpi)objs[0], (Provider)objs[1], type);
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new KeyStoreException(type + " not found", nsae);
        }
    }

    public static KeyStore getInstance(String type, Provider provider) throws KeyStoreException {
        Objects.requireNonNull(type, "null type name");
        if (provider == null) {
            throw new IllegalArgumentException("missing provider");
        }
        try {
            Object[] objs = Security.getImpl(type, "KeyStore", provider);
            return new KeyStore((KeyStoreSpi)objs[0], (Provider)objs[1], type);
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new KeyStoreException(type + " not found", nsae);
        }
    }

    public static final String getDefaultType() {
        String kstype = AccessController.doPrivileged(() -> Security.getProperty(KEYSTORE_TYPE));
        if (kstype == null) {
            kstype = "jks";
        }
        return kstype;
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public final String getType() {
        return this.type;
    }

    public final Set<Entry.Attribute> getAttributes(String alias) throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return this.keyStoreSpi.engineGetAttributes(Objects.requireNonNull(alias));
    }

    public final Key getKey(String alias, char[] password) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return this.keyStoreSpi.engineGetKey(alias, password);
    }

    public final Certificate[] getCertificateChain(String alias) throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return this.keyStoreSpi.engineGetCertificateChain(alias);
    }

    public final Certificate getCertificate(String alias) throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return this.keyStoreSpi.engineGetCertificate(alias);
    }

    public final Date getCreationDate(String alias) throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return this.keyStoreSpi.engineGetCreationDate(alias);
    }

    public final void setKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        if (key instanceof PrivateKey && (chain == null || chain.length == 0)) {
            throw new IllegalArgumentException("Private key must be accompanied by certificate chain");
        }
        this.keyStoreSpi.engineSetKeyEntry(alias, key, password, chain);
    }

    public final void setKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        this.keyStoreSpi.engineSetKeyEntry(alias, key, chain);
    }

    public final void setCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        this.keyStoreSpi.engineSetCertificateEntry(alias, cert);
    }

    public final void deleteEntry(String alias) throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        this.keyStoreSpi.engineDeleteEntry(alias);
    }

    public final Enumeration<String> aliases() throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return this.keyStoreSpi.engineAliases();
    }

    public final boolean containsAlias(String alias) throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return this.keyStoreSpi.engineContainsAlias(alias);
    }

    public final int size() throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return this.keyStoreSpi.engineSize();
    }

    public final boolean isKeyEntry(String alias) throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return this.keyStoreSpi.engineIsKeyEntry(alias);
    }

    public final boolean isCertificateEntry(String alias) throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return this.keyStoreSpi.engineIsCertificateEntry(alias);
    }

    public final String getCertificateAlias(Certificate cert) throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return this.keyStoreSpi.engineGetCertificateAlias(cert);
    }

    public final void store(OutputStream stream, char[] password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        this.keyStoreSpi.engineStore(stream, password);
    }

    public final void store(LoadStoreParameter param) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        this.keyStoreSpi.engineStore(param);
    }

    public final void load(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        this.keyStoreSpi.engineLoad(stream, password);
        this.initialized = true;
    }

    public final void load(LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        this.keyStoreSpi.engineLoad(param);
        this.initialized = true;
    }

    public final Entry getEntry(String alias, ProtectionParameter protParam) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException {
        if (alias == null) {
            throw new NullPointerException("invalid null input");
        }
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return this.keyStoreSpi.engineGetEntry(alias, protParam);
    }

    public final void setEntry(String alias, Entry entry, ProtectionParameter protParam) throws KeyStoreException {
        if (alias == null || entry == null) {
            throw new NullPointerException("invalid null input");
        }
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        this.keyStoreSpi.engineSetEntry(alias, entry, protParam);
    }

    public final boolean entryInstanceOf(String alias, Class<? extends Entry> entryClass) throws KeyStoreException {
        if (alias == null || entryClass == null) {
            throw new NullPointerException("invalid null input");
        }
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return this.keyStoreSpi.engineEntryInstanceOf(alias, entryClass);
    }

    public static final KeyStore getInstance(File file, char[] password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        return KeyStore.getInstance(file, password, null, true);
    }

    public static final KeyStore getInstance(File file, LoadStoreParameter param) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        return KeyStore.getInstance(file, null, param, false);
    }

    private static final KeyStore getInstance(File file, char[] password, LoadStoreParameter param, boolean hasPassword) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        if (file == null) {
            throw new NullPointerException();
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("File does not exist or it does not refer to a normal file: " + file);
        }
        KeyStore keystore = null;
        try (DataInputStream dataStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));){
            dataStream.mark(Integer.MAX_VALUE);
            block8: for (Provider p : Security.getProviders()) {
                for (Provider.Service s : p.getServices()) {
                    block18: {
                        if (!s.getType().equals("KeyStore")) continue;
                        try {
                            KeyStoreSpi impl = (KeyStoreSpi)s.newInstance(null);
                            if (impl.engineProbe(dataStream)) {
                                if (kdebug != null) {
                                    kdebug.println(s.getAlgorithm() + " keystore detected: " + file);
                                }
                                keystore = new KeyStore(impl, p, s.getAlgorithm());
                                continue block8;
                            }
                        }
                        catch (NoSuchAlgorithmException e) {
                            if (kdebug != null) {
                                kdebug.println("not found - " + e);
                            }
                        }
                        catch (IOException e) {
                            if (kdebug == null) break block18;
                            kdebug.println("I/O error in " + file + " - " + e);
                        }
                    }
                    dataStream.reset();
                }
            }
            if (keystore != null) {
                dataStream.reset();
                if (hasPassword) {
                    keystore.load(dataStream, password);
                } else {
                    keystore.keyStoreSpi.engineLoad((InputStream)dataStream, param);
                    keystore.initialized = true;
                }
                KeyStore keyStore = keystore;
                return keyStore;
            }
        }
        throw new KeyStoreException("Unrecognized keystore format. Please load it with a specified type");
    }

    public static interface LoadStoreParameter {
        public ProtectionParameter getProtectionParameter();
    }

    public static interface ProtectionParameter {
    }

    public static interface Entry {
        default public Set<Attribute> getAttributes() {
            return Collections.emptySet();
        }

        public static interface Attribute {
            public String getName();

            public String getValue();
        }
    }

    static class SimpleLoadStoreParameter
    implements LoadStoreParameter {
        private final ProtectionParameter protection;

        SimpleLoadStoreParameter(ProtectionParameter protection) {
            this.protection = protection;
        }

        @Override
        public ProtectionParameter getProtectionParameter() {
            return this.protection;
        }
    }

    public static abstract class Builder {
        static final int MAX_CALLBACK_TRIES = 3;

        protected Builder() {
        }

        public abstract KeyStore getKeyStore() throws KeyStoreException;

        public abstract ProtectionParameter getProtectionParameter(String var1) throws KeyStoreException;

        public static Builder newInstance(final KeyStore keyStore, final ProtectionParameter protectionParameter) {
            if (keyStore == null || protectionParameter == null) {
                throw new NullPointerException();
            }
            if (!keyStore.initialized) {
                throw new IllegalArgumentException("KeyStore not initialized");
            }
            return new Builder(){
                private volatile boolean getCalled;

                @Override
                public KeyStore getKeyStore() {
                    this.getCalled = true;
                    return keyStore;
                }

                @Override
                public ProtectionParameter getProtectionParameter(String alias) {
                    if (alias == null) {
                        throw new NullPointerException();
                    }
                    if (!this.getCalled) {
                        throw new IllegalStateException("getKeyStore() must be called first");
                    }
                    return protectionParameter;
                }
            };
        }

        public static Builder newInstance(String type, Provider provider, File file, ProtectionParameter protection) {
            if (type == null || file == null || protection == null) {
                throw new NullPointerException();
            }
            if (!(protection instanceof PasswordProtection) && !(protection instanceof CallbackHandlerProtection)) {
                throw new IllegalArgumentException("Protection must be PasswordProtection or CallbackHandlerProtection");
            }
            if (!file.isFile()) {
                throw new IllegalArgumentException("File does not exist or it does not refer to a normal file: " + file);
            }
            AccessControlContext acc = AccessController.getContext();
            return new FileBuilder(type, provider, file, protection, acc);
        }

        public static Builder newInstance(File file, ProtectionParameter protection) {
            return Builder.newInstance("", null, file, protection);
        }

        public static Builder newInstance(final String type, final Provider provider, final ProtectionParameter protection) {
            if (type == null || protection == null) {
                throw new NullPointerException();
            }
            final AccessControlContext context = AccessController.getContext();
            return new Builder(){
                private volatile boolean getCalled;
                private IOException oldException;
                private final PrivilegedExceptionAction<KeyStore> action = new PrivilegedExceptionAction<KeyStore>(){

                    @Override
                    public KeyStore run() throws Exception {
                        KeyStore ks = provider == null ? KeyStore.getInstance(type) : KeyStore.getInstance(type, provider);
                        SimpleLoadStoreParameter param = new SimpleLoadStoreParameter(protection);
                        if (!(protection instanceof CallbackHandlerProtection)) {
                            ks.load(param);
                        } else {
                            int tries = 0;
                            while (true) {
                                ++tries;
                                try {
                                    ks.load(param);
                                }
                                catch (IOException e) {
                                    if (e.getCause() instanceof UnrecoverableKeyException) {
                                        if (tries < 3) continue;
                                        oldException = e;
                                    }
                                    throw e;
                                }
                                break;
                            }
                        }
                        getCalled = true;
                        return ks;
                    }
                };

                @Override
                public synchronized KeyStore getKeyStore() throws KeyStoreException {
                    if (this.oldException != null) {
                        throw new KeyStoreException("Previous KeyStore instantiation failed", this.oldException);
                    }
                    try {
                        return AccessController.doPrivileged(this.action, context);
                    }
                    catch (PrivilegedActionException e) {
                        Throwable cause = e.getCause();
                        throw new KeyStoreException("KeyStore instantiation failed", cause);
                    }
                }

                @Override
                public ProtectionParameter getProtectionParameter(String alias) {
                    if (alias == null) {
                        throw new NullPointerException();
                    }
                    if (!this.getCalled) {
                        throw new IllegalStateException("getKeyStore() must be called first");
                    }
                    return protection;
                }
            };
        }

        private static final class FileBuilder
        extends Builder {
            private final String type;
            private final Provider provider;
            private final File file;
            private final ProtectionParameter protection;
            private ProtectionParameter keyProtection;
            private final AccessControlContext context;
            private KeyStore keyStore;
            private Throwable oldException;

            FileBuilder(String type, Provider provider, File file, ProtectionParameter protection, AccessControlContext context) {
                this.type = type;
                this.provider = provider;
                this.file = file;
                this.protection = protection;
                this.context = context;
            }

            @Override
            public synchronized KeyStore getKeyStore() throws KeyStoreException {
                if (this.keyStore != null) {
                    return this.keyStore;
                }
                if (this.oldException != null) {
                    throw new KeyStoreException("Previous KeyStore instantiation failed", this.oldException);
                }
                PrivilegedExceptionAction<KeyStore> action = new PrivilegedExceptionAction<KeyStore>(){

                    @Override
                    public KeyStore run() throws Exception {
                        if (!(protection instanceof CallbackHandlerProtection)) {
                            return this.run0();
                        }
                        int tries = 0;
                        while (true) {
                            ++tries;
                            try {
                                return this.run0();
                            }
                            catch (IOException e) {
                                if (tries < 3 && e.getCause() instanceof UnrecoverableKeyException) continue;
                                throw e;
                            }
                            break;
                        }
                    }

                    public KeyStore run0() throws Exception {
                        KeyStore ks;
                        char[] password;
                        if (protection instanceof PasswordProtection) {
                            password = ((PasswordProtection)protection).getPassword();
                            keyProtection = protection;
                        } else {
                            CallbackHandler handler = ((CallbackHandlerProtection)protection).getCallbackHandler();
                            PasswordCallback callback = new PasswordCallback("Password for keystore " + file.getName(), false);
                            handler.handle(new Callback[]{callback});
                            password = callback.getPassword();
                            if (password == null) {
                                throw new KeyStoreException("No password provided");
                            }
                            callback.clearPassword();
                            keyProtection = new PasswordProtection(password);
                        }
                        if (type.isEmpty()) {
                            ks = KeyStore.getInstance(file, password);
                        } else {
                            ks = provider == null ? KeyStore.getInstance(type) : KeyStore.getInstance(type, provider);
                            try (FileInputStream in = new FileInputStream(file);){
                                ks.load(in, password);
                            }
                        }
                        return ks;
                    }
                };
                try {
                    this.keyStore = AccessController.doPrivileged(action, this.context);
                    return this.keyStore;
                }
                catch (PrivilegedActionException e) {
                    this.oldException = e.getCause();
                    throw new KeyStoreException("KeyStore instantiation failed", this.oldException);
                }
            }

            @Override
            public synchronized ProtectionParameter getProtectionParameter(String alias) {
                if (alias == null) {
                    throw new NullPointerException();
                }
                if (this.keyStore == null) {
                    throw new IllegalStateException("getKeyStore() must be called first");
                }
                return this.keyProtection;
            }
        }
    }

    public static final class TrustedCertificateEntry
    implements Entry {
        private final Certificate cert;
        private final Set<Entry.Attribute> attributes;

        public TrustedCertificateEntry(Certificate trustedCert) {
            if (trustedCert == null) {
                throw new NullPointerException("invalid null input");
            }
            this.cert = trustedCert;
            this.attributes = Collections.emptySet();
        }

        public TrustedCertificateEntry(Certificate trustedCert, Set<Entry.Attribute> attributes) {
            if (trustedCert == null || attributes == null) {
                throw new NullPointerException("invalid null input");
            }
            this.cert = trustedCert;
            this.attributes = Collections.unmodifiableSet(new HashSet<Entry.Attribute>(attributes));
        }

        public Certificate getTrustedCertificate() {
            return this.cert;
        }

        @Override
        public Set<Entry.Attribute> getAttributes() {
            return this.attributes;
        }

        public String toString() {
            return "Trusted certificate entry:\r\n" + this.cert.toString();
        }
    }

    public static final class SecretKeyEntry
    implements Entry {
        private final SecretKey sKey;
        private final Set<Entry.Attribute> attributes;

        public SecretKeyEntry(SecretKey secretKey) {
            if (secretKey == null) {
                throw new NullPointerException("invalid null input");
            }
            this.sKey = secretKey;
            this.attributes = Collections.emptySet();
        }

        public SecretKeyEntry(SecretKey secretKey, Set<Entry.Attribute> attributes) {
            if (secretKey == null || attributes == null) {
                throw new NullPointerException("invalid null input");
            }
            this.sKey = secretKey;
            this.attributes = Collections.unmodifiableSet(new HashSet<Entry.Attribute>(attributes));
        }

        public SecretKey getSecretKey() {
            return this.sKey;
        }

        @Override
        public Set<Entry.Attribute> getAttributes() {
            return this.attributes;
        }

        public String toString() {
            return "Secret key entry with algorithm " + this.sKey.getAlgorithm();
        }
    }

    public static final class PrivateKeyEntry
    implements Entry {
        private final PrivateKey privKey;
        private final Certificate[] chain;
        private final Set<Entry.Attribute> attributes;

        public PrivateKeyEntry(PrivateKey privateKey, Certificate[] chain) {
            this(privateKey, chain, Collections.emptySet());
        }

        public PrivateKeyEntry(PrivateKey privateKey, Certificate[] chain, Set<Entry.Attribute> attributes) {
            if (privateKey == null || chain == null || attributes == null) {
                throw new NullPointerException("invalid null input");
            }
            if (chain.length == 0) {
                throw new IllegalArgumentException("invalid zero-length input chain");
            }
            Certificate[] clonedChain = (Certificate[])chain.clone();
            String certType = clonedChain[0].getType();
            for (int i = 1; i < clonedChain.length; ++i) {
                if (certType.equals(clonedChain[i].getType())) continue;
                throw new IllegalArgumentException("chain does not contain certificates of the same type");
            }
            if (!privateKey.getAlgorithm().equals(clonedChain[0].getPublicKey().getAlgorithm())) {
                throw new IllegalArgumentException("private key algorithm does not match algorithm of public key in end entity certificate (at index 0)");
            }
            this.privKey = privateKey;
            if (clonedChain[0] instanceof X509Certificate && !(clonedChain instanceof X509Certificate[])) {
                this.chain = new X509Certificate[clonedChain.length];
                System.arraycopy(clonedChain, 0, this.chain, 0, clonedChain.length);
            } else {
                this.chain = clonedChain;
            }
            this.attributes = Collections.unmodifiableSet(new HashSet<Entry.Attribute>(attributes));
        }

        public PrivateKey getPrivateKey() {
            return this.privKey;
        }

        public Certificate[] getCertificateChain() {
            return (Certificate[])this.chain.clone();
        }

        public Certificate getCertificate() {
            return this.chain[0];
        }

        @Override
        public Set<Entry.Attribute> getAttributes() {
            return this.attributes;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Private key entry and certificate chain with " + this.chain.length + " elements:\r\n");
            for (Certificate cert : this.chain) {
                sb.append(cert);
                sb.append("\r\n");
            }
            return sb.toString();
        }
    }

    public static class CallbackHandlerProtection
    implements ProtectionParameter {
        private final CallbackHandler handler;

        public CallbackHandlerProtection(CallbackHandler handler) {
            if (handler == null) {
                throw new NullPointerException("handler must not be null");
            }
            this.handler = handler;
        }

        public CallbackHandler getCallbackHandler() {
            return this.handler;
        }
    }

    public static class PasswordProtection
    implements ProtectionParameter,
    Destroyable {
        private final char[] password;
        private final String protectionAlgorithm;
        private final AlgorithmParameterSpec protectionParameters;
        private volatile boolean destroyed;

        public PasswordProtection(char[] password) {
            this.password = password == null ? null : (char[])password.clone();
            this.protectionAlgorithm = null;
            this.protectionParameters = null;
        }

        public PasswordProtection(char[] password, String protectionAlgorithm, AlgorithmParameterSpec protectionParameters) {
            if (protectionAlgorithm == null) {
                throw new NullPointerException("invalid null input");
            }
            this.password = password == null ? null : (char[])password.clone();
            this.protectionAlgorithm = protectionAlgorithm;
            this.protectionParameters = protectionParameters;
        }

        public String getProtectionAlgorithm() {
            return this.protectionAlgorithm;
        }

        public AlgorithmParameterSpec getProtectionParameters() {
            return this.protectionParameters;
        }

        public synchronized char[] getPassword() {
            if (this.destroyed) {
                throw new IllegalStateException("password has been cleared");
            }
            return this.password;
        }

        @Override
        public synchronized void destroy() throws DestroyFailedException {
            this.destroyed = true;
            if (this.password != null) {
                Arrays.fill(this.password, ' ');
            }
        }

        @Override
        public synchronized boolean isDestroyed() {
            return this.destroyed;
        }
    }
}

