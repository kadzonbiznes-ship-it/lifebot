/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto;

import java.net.URL;
import java.security.AccessController;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.crypto.CryptoAllPermission;
import javax.crypto.CryptoPermission;
import javax.crypto.CryptoPermissions;
import javax.crypto.ExemptionMechanism;
import javax.crypto.JceSecurity;
import javax.crypto.ProviderVerifier;

final class JceSecurityManager {
    private static final CryptoPermissions defaultPolicy;
    private static final CryptoPermissions exemptPolicy;
    private static final CryptoAllPermission allPerm;
    private static final Vector<Class<?>> TrustedCallersCache;
    private static final ConcurrentMap<URL, CryptoPermissions> exemptCache;
    private static final CryptoPermissions CACHE_NULL_MARK;
    static final JceSecurityManager INSTANCE;
    static final StackWalker WALKER;

    private JceSecurityManager() {
    }

    CryptoPermission getCryptoPermission(String theAlg) {
        String alg = theAlg.toUpperCase(Locale.ENGLISH);
        CryptoPermission defaultPerm = this.getDefaultPermission(alg);
        if (defaultPerm == CryptoAllPermission.INSTANCE) {
            return defaultPerm;
        }
        return WALKER.walk(s -> s.map(StackWalker.StackFrame::getDeclaringClass).filter(c -> !c.getPackageName().equals("javax.crypto")).map(cls -> {
            URL callerCodeBase = JceSecurity.getCodeBase(cls);
            return callerCodeBase != null ? this.getCryptoPermissionFromURL(callerCodeBase, alg, defaultPerm) : defaultPerm;
        }).findFirst().get());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    CryptoPermission getCryptoPermissionFromURL(URL callerCodeBase, String alg, CryptoPermission defaultPerm) {
        CryptoPermissions appPerms = (CryptoPermissions)exemptCache.get(callerCodeBase);
        if (appPerms == null) {
            Class<?> clazz = this.getClass();
            synchronized (clazz) {
                appPerms = (CryptoPermissions)exemptCache.get(callerCodeBase);
                if (appPerms == null) {
                    appPerms = JceSecurityManager.getAppPermissions(callerCodeBase);
                    exemptCache.putIfAbsent(callerCodeBase, appPerms == null ? CACHE_NULL_MARK : appPerms);
                }
            }
        }
        if (appPerms == null || appPerms == CACHE_NULL_MARK) {
            return defaultPerm;
        }
        if (appPerms.implies(allPerm)) {
            return allPerm;
        }
        PermissionCollection appPc = appPerms.getPermissionCollection(alg);
        if (appPc == null) {
            return defaultPerm;
        }
        Enumeration<Permission> enum_ = appPc.elements();
        while (enum_.hasMoreElements()) {
            CryptoPermission cp = (CryptoPermission)enum_.nextElement();
            if (cp.getExemptionMechanism() != null) continue;
            return cp;
        }
        PermissionCollection exemptPc = exemptPolicy.getPermissionCollection(alg);
        if (exemptPc == null) {
            return defaultPerm;
        }
        enum_ = exemptPc.elements();
        while (enum_.hasMoreElements()) {
            CryptoPermission cp = (CryptoPermission)enum_.nextElement();
            try {
                CryptoPermission newCp;
                ExemptionMechanism.getInstance(cp.getExemptionMechanism());
                if (cp.getAlgorithm().equals("*") && appPerms.implies(newCp = cp.getCheckParam() ? new CryptoPermission(alg, cp.getMaxKeySize(), cp.getAlgorithmParameterSpec(), cp.getExemptionMechanism()) : new CryptoPermission(alg, cp.getMaxKeySize(), cp.getExemptionMechanism()))) {
                    return newCp;
                }
                if (!appPerms.implies(cp)) continue;
                return cp;
            }
            catch (Exception exception) {
            }
        }
        return defaultPerm;
    }

    private static CryptoPermissions getAppPermissions(URL callerCodeBase) {
        try {
            return JceSecurity.verifyExemptJar(callerCodeBase);
        }
        catch (Exception e) {
            return null;
        }
    }

    private CryptoPermission getDefaultPermission(String alg) {
        Enumeration<Permission> enum_ = defaultPolicy.getPermissionCollection(alg).elements();
        return (CryptoPermission)enum_.nextElement();
    }

    boolean isCallerTrusted(Class<?> caller, Provider provider) {
        if (caller != null) {
            boolean sameOrigin;
            URL callerCodeBase = JceSecurity.getCodeBase(caller);
            if (callerCodeBase == null) {
                return true;
            }
            if (TrustedCallersCache.contains(caller)) {
                return true;
            }
            Class<?> pCls = provider.getClass();
            Module pMod = pCls.getModule();
            boolean bl = sameOrigin = pMod.isNamed() ? caller.getModule().equals(pMod) : callerCodeBase.equals(JceSecurity.getCodeBase(pCls));
            if (sameOrigin) {
                if (ProviderVerifier.isTrustedCryptoProvider(provider)) {
                    TrustedCallersCache.addElement(caller);
                    return true;
                }
            } else {
                provider = null;
            }
            try {
                JceSecurity.verifyProvider(callerCodeBase, provider);
            }
            catch (Exception e2) {
                return false;
            }
            TrustedCallersCache.addElement(caller);
            return true;
        }
        return false;
    }

    static {
        StackWalker dummyWalker;
        JceSecurityManager dummySecurityManager;
        TrustedCallersCache = new Vector(2);
        exemptCache = new ConcurrentHashMap<URL, CryptoPermissions>();
        CACHE_NULL_MARK = new CryptoPermissions();
        defaultPolicy = JceSecurity.getDefaultPolicy();
        exemptPolicy = JceSecurity.getExemptPolicy();
        allPerm = CryptoAllPermission.INSTANCE;
        PrivilegedAction<JceSecurityManager> paSM = JceSecurityManager::new;
        INSTANCE = dummySecurityManager = AccessController.doPrivileged(paSM);
        PrivilegedAction<StackWalker> paWalker = () -> StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        WALKER = dummyWalker = AccessController.doPrivileged(paWalker);
    }
}

