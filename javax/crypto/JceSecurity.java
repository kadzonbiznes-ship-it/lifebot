/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.Provider;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.crypto.CryptoAllPermission;
import javax.crypto.CryptoPermissions;
import javax.crypto.ProviderVerifier;
import sun.security.jca.GetInstance;
import sun.security.util.Debug;

final class JceSecurity {
    private static final Debug debug = Debug.getInstance("jca");
    private static CryptoPermissions defaultPolicy = null;
    private static CryptoPermissions exemptPolicy = null;
    private static final Map<WeakIdentityWrapper, Object> verificationResults = new ConcurrentHashMap<WeakIdentityWrapper, Object>();
    private static final Map<Provider, Object> verifyingProviders = new IdentityHashMap<Provider, Object>();
    private static final ReferenceQueue<Object> queue = new ReferenceQueue();
    private static final boolean isRestricted;
    private static final Object PROVIDER_VERIFIED;
    private static final URL NULL_URL;
    private static final Map<Class<?>, URL> codeBaseCacheRef;

    private JceSecurity() {
    }

    static GetInstance.Instance getInstance(String type, Class<?> clazz, String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Provider.Service s = GetInstance.getService(type, algorithm, provider);
        Exception ve = JceSecurity.getVerificationResult(s.getProvider());
        if (ve != null) {
            String msg = "JCE cannot authenticate the provider " + provider;
            throw (NoSuchProviderException)new NoSuchProviderException(msg).initCause(ve);
        }
        return GetInstance.getInstance(s, clazz);
    }

    static GetInstance.Instance getInstance(String type, Class<?> clazz, String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Provider.Service s = GetInstance.getService(type, algorithm, provider);
        Exception ve = JceSecurity.getVerificationResult(provider);
        if (ve != null) {
            String msg = "JCE cannot authenticate the provider " + provider.getName();
            throw new SecurityException(msg, ve);
        }
        return GetInstance.getInstance(s, clazz);
    }

    static GetInstance.Instance getInstance(String type, Class<?> clazz, String algorithm) throws NoSuchAlgorithmException {
        List<Provider.Service> services = GetInstance.getServices(type, algorithm);
        NoSuchAlgorithmException failure = null;
        for (Provider.Service s : services) {
            if (!JceSecurity.canUseProvider(s.getProvider())) continue;
            try {
                GetInstance.Instance instance = GetInstance.getInstance(s, clazz);
                return instance;
            }
            catch (NoSuchAlgorithmException e) {
                failure = e;
            }
        }
        throw new NoSuchAlgorithmException("Algorithm " + algorithm + " not available", failure);
    }

    static CryptoPermissions verifyExemptJar(URL codeBase) throws Exception {
        ProviderVerifier pv = new ProviderVerifier(codeBase, true);
        pv.verify();
        return pv.getPermissions();
    }

    static void verifyProvider(URL codeBase, Provider p) throws Exception {
        ProviderVerifier pv = new ProviderVerifier(codeBase, p, false);
        pv.verify();
    }

    static Exception getVerificationResult(final Provider p) {
        JceSecurity.expungeStaleWrappers();
        WeakIdentityWrapper pKey = new WeakIdentityWrapper(p, queue);
        try {
            Object o = verificationResults.computeIfAbsent(pKey, new Function<WeakIdentityWrapper, Object>(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public Object apply(WeakIdentityWrapper key) {
                    Object result;
                    if (verifyingProviders.get(p) != null) {
                        throw new IllegalStateException();
                    }
                    try {
                        verifyingProviders.put(p, Boolean.FALSE);
                        URL providerURL = JceSecurity.getCodeBase(p.getClass());
                        JceSecurity.verifyProvider(providerURL, p);
                        result = PROVIDER_VERIFIED;
                    }
                    catch (Exception e) {
                        result = e;
                    }
                    finally {
                        verifyingProviders.remove(p);
                    }
                    if (debug != null) {
                        debug.println("Provider " + p.getName() + " verification result: " + result);
                    }
                    return result;
                }
            });
            return o == PROVIDER_VERIFIED ? null : (Exception)o;
        }
        catch (IllegalStateException ise) {
            return new NoSuchProviderException("Recursion during verification");
        }
    }

    static void expungeStaleWrappers() {
        WeakIdentityWrapper key;
        while ((key = (WeakIdentityWrapper)queue.poll()) != null) {
            verificationResults.remove(key);
        }
    }

    static boolean canUseProvider(Provider p) {
        Exception e = JceSecurity.getVerificationResult(p);
        if (debug != null && e != null) {
            debug.println("Provider verification result: " + e);
        }
        return e == null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static URL getCodeBase(final Class<?> clazz) {
        Map<Class<?>, URL> map = codeBaseCacheRef;
        synchronized (map) {
            URL url = codeBaseCacheRef.get(clazz);
            if (url == null) {
                url = AccessController.doPrivileged(new PrivilegedAction<URL>(){

                    @Override
                    public URL run() {
                        CodeSource cs;
                        ProtectionDomain pd = clazz.getProtectionDomain();
                        if (pd != null && (cs = pd.getCodeSource()) != null) {
                            return cs.getLocation();
                        }
                        return NULL_URL;
                    }
                });
                codeBaseCacheRef.put(clazz, url);
            }
            return url == NULL_URL ? null : url;
        }
    }

    /*
     * Exception decompiling
     */
    private static void setupJurisdictionPolicies() throws Exception {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 2 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    static CryptoPermissions getDefaultPolicy() {
        return defaultPolicy;
    }

    static CryptoPermissions getExemptPolicy() {
        return exemptPolicy;
    }

    static boolean isRestricted() {
        return isRestricted;
    }

    static {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>(){

                @Override
                public Void run() throws Exception {
                    JceSecurity.setupJurisdictionPolicies();
                    return null;
                }
            });
            isRestricted = !defaultPolicy.implies(CryptoAllPermission.INSTANCE);
        }
        catch (Exception e) {
            throw new SecurityException("Can not initialize cryptographic mechanism", e);
        }
        PROVIDER_VERIFIED = Boolean.TRUE;
        try {
            URL e = NULL_URL = new URL("http://null.oracle.com/");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        codeBaseCacheRef = new WeakHashMap();
    }

    private static final class WeakIdentityWrapper
    extends WeakReference<Object> {
        final int hash;

        WeakIdentityWrapper(Provider obj, ReferenceQueue<Object> queue) {
            super(obj, queue);
            this.hash = System.identityHashCode(obj);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof WeakIdentityWrapper)) return false;
            WeakIdentityWrapper w = (WeakIdentityWrapper)o;
            if (this.get() != w.get()) return false;
            return true;
        }

        public int hashCode() {
            return this.hash;
        }
    }
}

