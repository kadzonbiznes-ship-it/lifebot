/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.security.AccessController;
import java.security.CodeSource;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PolicySpi;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.Provider;
import java.security.Security;
import java.security.SecurityPermission;
import java.util.Enumeration;
import java.util.Objects;
import java.util.WeakHashMap;
import sun.security.jca.GetInstance;
import sun.security.provider.PolicyFile;
import sun.security.util.Debug;
import sun.security.util.SecurityConstants;

@Deprecated(since="17", forRemoval=true)
public abstract class Policy {
    public static final PermissionCollection UNSUPPORTED_EMPTY_COLLECTION = new UnsupportedEmptyCollection();
    private static volatile PolicyInfo policyInfo = new PolicyInfo(null, false);
    private static final Debug debug = Debug.getInstance("policy");
    private static final String DEFAULT_POLICY = "sun.security.provider.PolicyFile";
    private WeakHashMap<ProtectionDomain.Key, PermissionCollection> pdMapping;

    static boolean isSet() {
        PolicyInfo pi = policyInfo;
        return pi.policy != null && pi.initialized;
    }

    private static void checkPermission(String type) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission("createPolicy." + type));
        }
    }

    public static Policy getPolicy() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_POLICY_PERMISSION);
        }
        return Policy.getPolicyNoCheck();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static Policy getPolicyNoCheck() {
        PolicyInfo pi = policyInfo;
        if (pi.initialized && pi.policy != null) return pi.policy;
        Class<Policy> clazz = Policy.class;
        synchronized (Policy.class) {
            pi = policyInfo;
            if (pi.policy != null) return pi.policy;
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return Policy.loadPolicyProvider();
        }
    }

    private static Policy loadPolicyProvider() {
        final String policyProvider = AccessController.doPrivileged(new PrivilegedAction<String>(){

            @Override
            public String run() {
                return Security.getProperty("policy.provider");
            }
        });
        if (policyProvider == null || policyProvider.isEmpty() || policyProvider.equals(DEFAULT_POLICY)) {
            PolicyFile polFile = new PolicyFile();
            policyInfo = new PolicyInfo(polFile, true);
            return polFile;
        }
        PolicyFile polFile = new PolicyFile();
        policyInfo = new PolicyInfo(polFile, false);
        Policy pol = AccessController.doPrivileged(new PrivilegedAction<Policy>(){

            @Override
            public Policy run() {
                try {
                    ClassLoader scl = ClassLoader.getSystemClassLoader();
                    Object o = Class.forName(policyProvider, true, scl).newInstance();
                    return (Policy)o;
                }
                catch (Exception e) {
                    if (debug != null) {
                        debug.println("policy provider " + policyProvider + " not available");
                        e.printStackTrace();
                    }
                    return null;
                }
            }
        });
        if (pol == null) {
            if (debug != null) {
                debug.println("using sun.security.provider.PolicyFile");
            }
            pol = polFile;
        }
        policyInfo = new PolicyInfo(pol, true);
        return pol;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setPolicy(Policy p) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission("setPolicy"));
        }
        if (p != null) {
            Policy.initPolicy(p);
        }
        Class<Policy> clazz = Policy.class;
        synchronized (Policy.class) {
            policyInfo = new PolicyInfo(p, p != null);
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void initPolicy(final Policy p) {
        ProtectionDomain policyDomain = AccessController.doPrivileged(new PrivilegedAction<ProtectionDomain>(){

            @Override
            public ProtectionDomain run() {
                return p.getClass().getProtectionDomain();
            }
        });
        PermissionCollection policyPerms = null;
        Policy policy = p;
        synchronized (policy) {
            if (p.pdMapping == null) {
                p.pdMapping = new WeakHashMap();
            }
        }
        if (policyDomain.getCodeSource() != null) {
            Policy pol = Policy.policyInfo.policy;
            if (pol != null) {
                policyPerms = pol.getPermissions(policyDomain);
            }
            if (policyPerms == null) {
                policyPerms = new Permissions();
                policyPerms.add(SecurityConstants.ALL_PERMISSION);
            }
            WeakHashMap<ProtectionDomain.Key, PermissionCollection> weakHashMap = p.pdMapping;
            synchronized (weakHashMap) {
                p.pdMapping.put(policyDomain.key, policyPerms);
            }
        }
    }

    public static Policy getInstance(String type, Parameters params) throws NoSuchAlgorithmException {
        Objects.requireNonNull(type, "null type name");
        Policy.checkPermission(type);
        try {
            GetInstance.Instance instance = GetInstance.getInstance("Policy", PolicySpi.class, type, params);
            return new PolicyDelegate((PolicySpi)instance.impl, instance.provider, type, params);
        }
        catch (NoSuchAlgorithmException nsae) {
            return Policy.handleException(nsae);
        }
    }

    public static Policy getInstance(String type, Parameters params, String provider) throws NoSuchProviderException, NoSuchAlgorithmException {
        Objects.requireNonNull(type, "null type name");
        if (provider == null || provider.isEmpty()) {
            throw new IllegalArgumentException("missing provider");
        }
        Policy.checkPermission(type);
        try {
            GetInstance.Instance instance = GetInstance.getInstance("Policy", PolicySpi.class, type, (Object)params, provider);
            return new PolicyDelegate((PolicySpi)instance.impl, instance.provider, type, params);
        }
        catch (NoSuchAlgorithmException nsae) {
            return Policy.handleException(nsae);
        }
    }

    public static Policy getInstance(String type, Parameters params, Provider provider) throws NoSuchAlgorithmException {
        Objects.requireNonNull(type, "null type name");
        if (provider == null) {
            throw new IllegalArgumentException("missing provider");
        }
        Policy.checkPermission(type);
        try {
            GetInstance.Instance instance = GetInstance.getInstance("Policy", PolicySpi.class, type, (Object)params, provider);
            return new PolicyDelegate((PolicySpi)instance.impl, instance.provider, type, params);
        }
        catch (NoSuchAlgorithmException nsae) {
            return Policy.handleException(nsae);
        }
    }

    private static Policy handleException(NoSuchAlgorithmException nsae) throws NoSuchAlgorithmException {
        Throwable cause = nsae.getCause();
        if (cause instanceof IllegalArgumentException) {
            throw (IllegalArgumentException)cause;
        }
        throw nsae;
    }

    public Provider getProvider() {
        return null;
    }

    public String getType() {
        return null;
    }

    public Parameters getParameters() {
        return null;
    }

    public PermissionCollection getPermissions(CodeSource codesource) {
        return UNSUPPORTED_EMPTY_COLLECTION;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        PermissionCollection pc = null;
        if (domain == null) {
            return new Permissions();
        }
        if (this.pdMapping == null) {
            Policy.initPolicy(this);
        }
        WeakHashMap<ProtectionDomain.Key, PermissionCollection> weakHashMap = this.pdMapping;
        synchronized (weakHashMap) {
            pc = this.pdMapping.get(domain.key);
        }
        if (pc != null) {
            Permissions perms = new Permissions();
            PermissionCollection permissionCollection = pc;
            synchronized (permissionCollection) {
                Enumeration<Permission> e = pc.elements();
                while (e.hasMoreElements()) {
                    perms.add(e.nextElement());
                }
            }
            return perms;
        }
        pc = this.getPermissions(domain.getCodeSource());
        if (pc == null || pc == UNSUPPORTED_EMPTY_COLLECTION) {
            pc = new Permissions();
        }
        this.addStaticPerms(pc, domain.getPermissions());
        return pc;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void addStaticPerms(PermissionCollection perms, PermissionCollection statics) {
        if (statics != null) {
            PermissionCollection permissionCollection = statics;
            synchronized (permissionCollection) {
                Enumeration<Permission> e = statics.elements();
                while (e.hasMoreElements()) {
                    perms.add(e.nextElement());
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean implies(ProtectionDomain domain, Permission permission) {
        PermissionCollection pc;
        if (this.pdMapping == null) {
            Policy.initPolicy(this);
        }
        WeakHashMap<ProtectionDomain.Key, PermissionCollection> weakHashMap = this.pdMapping;
        synchronized (weakHashMap) {
            pc = this.pdMapping.get(domain.key);
        }
        if (pc != null) {
            return pc.implies(permission);
        }
        pc = this.getPermissions(domain);
        if (pc == null) {
            return false;
        }
        weakHashMap = this.pdMapping;
        synchronized (weakHashMap) {
            this.pdMapping.put(domain.key, pc);
        }
        return pc.implies(permission);
    }

    public void refresh() {
    }

    private static class PolicyInfo {
        final Policy policy;
        final boolean initialized;

        PolicyInfo(Policy policy, boolean initialized) {
            this.policy = policy;
            this.initialized = initialized;
        }
    }

    private static class PolicyDelegate
    extends Policy {
        private PolicySpi spi;
        private Provider p;
        private String type;
        private Parameters params;

        private PolicyDelegate(PolicySpi spi, Provider p, String type, Parameters params) {
            this.spi = spi;
            this.p = p;
            this.type = type;
            this.params = params;
        }

        @Override
        public String getType() {
            return this.type;
        }

        @Override
        public Parameters getParameters() {
            return this.params;
        }

        @Override
        public Provider getProvider() {
            return this.p;
        }

        @Override
        public PermissionCollection getPermissions(CodeSource codesource) {
            return this.spi.engineGetPermissions(codesource);
        }

        @Override
        public PermissionCollection getPermissions(ProtectionDomain domain) {
            return this.spi.engineGetPermissions(domain);
        }

        @Override
        public boolean implies(ProtectionDomain domain, Permission perm) {
            return this.spi.engineImplies(domain, perm);
        }

        @Override
        public void refresh() {
            this.spi.engineRefresh();
        }
    }

    @Deprecated(since="17", forRemoval=true)
    public static interface Parameters {
    }

    private static class UnsupportedEmptyCollection
    extends PermissionCollection {
        private static final long serialVersionUID = -8492269157353014774L;
        private Permissions perms = new Permissions();

        public UnsupportedEmptyCollection() {
            this.perms.setReadOnly();
        }

        @Override
        public void add(Permission permission) {
            this.perms.add(permission);
        }

        @Override
        public boolean implies(Permission permission) {
            return this.perms.implies(permission);
        }

        @Override
        public Enumeration<Permission> elements() {
            return this.perms.elements();
        }
    }
}

