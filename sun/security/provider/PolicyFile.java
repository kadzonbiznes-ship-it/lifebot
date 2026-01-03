/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.Security;
import java.security.SecurityPermission;
import java.security.UnresolvedPermission;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;
import jdk.internal.access.JavaSecurityAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.util.StaticProperty;
import sun.net.www.ParseUtil;
import sun.nio.fs.DefaultFileSystemProvider;
import sun.security.provider.PolicyParser;
import sun.security.util.Debug;
import sun.security.util.FilePermCompat;
import sun.security.util.LocalizedMessage;
import sun.security.util.PolicyUtil;
import sun.security.util.PropertyExpander;
import sun.security.util.ResourcesMgr;
import sun.security.util.SecurityConstants;

public class PolicyFile
extends Policy {
    private static final Debug debug = Debug.getInstance("policy");
    private static final String SELF = "${{self}}";
    private static final String X500PRINCIPAL = "javax.security.auth.x500.X500Principal";
    private static final String POLICY = "java.security.policy";
    private static final String POLICY_URL = "policy.url.";
    private static final int DEFAULT_CACHE_SIZE = 1;
    private volatile PolicyInfo policyInfo;
    private boolean expandProperties = true;
    private boolean allowSystemProperties = true;
    private boolean notUtf8 = false;
    private URL url;
    private static final Class<?>[] PARAMS0 = new Class[0];
    private static final Class<?>[] PARAMS1 = new Class[]{String.class};
    private static final Class<?>[] PARAMS2 = new Class[]{String.class, String.class};
    private static Set<URL> badPolicyURLs = Collections.newSetFromMap(new ConcurrentHashMap());
    private static final FileSystem builtInFS = DefaultFileSystemProvider.theFileSystem();

    public PolicyFile() {
        this.init(null);
    }

    public PolicyFile(URL url) {
        this.url = url;
        this.init(url);
    }

    private void init(URL url) {
        int numCaches;
        String numCacheStr = AccessController.doPrivileged(new PrivilegedAction<String>(){

            @Override
            public String run() {
                PolicyFile.this.expandProperties = "true".equalsIgnoreCase(Security.getProperty("policy.expandProperties"));
                PolicyFile.this.allowSystemProperties = "true".equalsIgnoreCase(Security.getProperty("policy.allowSystemProperty"));
                PolicyFile.this.notUtf8 = "false".equalsIgnoreCase(System.getProperty("sun.security.policy.utf8"));
                return System.getProperty("sun.security.policy.numcaches");
            }
        });
        if (numCacheStr != null) {
            try {
                numCaches = Integer.parseInt(numCacheStr);
            }
            catch (NumberFormatException e) {
                numCaches = 1;
            }
        } else {
            numCaches = 1;
        }
        PolicyInfo newInfo = new PolicyInfo(numCaches);
        this.initPolicyFile(newInfo, url);
        this.policyInfo = newInfo;
    }

    private void initPolicyFile(final PolicyInfo newInfo, final URL url) {
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Void run() {
                PolicyFile.this.initDefaultPolicy(newInfo);
                return null;
            }
        });
        if (url != null) {
            if (debug != null) {
                debug.println("reading " + url);
            }
            AccessController.doPrivileged(new PrivilegedAction<Object>(this){
                final /* synthetic */ PolicyFile this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public Void run() {
                    if (!this.this$0.init(url, newInfo)) {
                        this.this$0.initStaticPolicy(newInfo);
                    }
                    return null;
                }
            });
        } else {
            boolean loaded_one = this.initPolicyFile(POLICY, POLICY_URL, newInfo);
            if (!loaded_one) {
                this.initStaticPolicy(newInfo);
            }
        }
    }

    private boolean initPolicyFile(final String propname, final String urlname, final PolicyInfo newInfo) {
        boolean loadedPolicy = AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

            @Override
            public Boolean run() {
                String policy_uri;
                String extra_policy;
                boolean loaded_policy = false;
                if (PolicyFile.this.allowSystemProperties && (extra_policy = System.getProperty(propname)) != null) {
                    boolean overrideAll;
                    block13: {
                        overrideAll = false;
                        if (extra_policy.startsWith("=")) {
                            overrideAll = true;
                            extra_policy = extra_policy.substring(1);
                        }
                        try {
                            extra_policy = PropertyExpander.expand(extra_policy);
                            File policyFile = new File(extra_policy);
                            URL policyURL = policyFile.exists() ? ParseUtil.fileToEncodedURL(new File(policyFile.getCanonicalPath())) : PolicyFile.newURL(extra_policy);
                            if (debug != null) {
                                debug.println("reading " + policyURL);
                            }
                            if (PolicyFile.this.init(policyURL, newInfo)) {
                                loaded_policy = true;
                            }
                        }
                        catch (Exception e) {
                            if (debug == null) break block13;
                            debug.println("caught exception: " + e);
                        }
                    }
                    if (overrideAll) {
                        if (debug != null) {
                            debug.println("overriding other policies!");
                        }
                        return loaded_policy;
                    }
                }
                int n = 1;
                while ((policy_uri = Security.getProperty(urlname + n)) != null) {
                    block14: {
                        try {
                            URL policy_url = null;
                            String expanded_uri = PropertyExpander.expand(policy_uri).replace(File.separatorChar, '/');
                            policy_url = policy_uri.startsWith("file:${java.home}/") || policy_uri.startsWith("file:${user.home}/") ? new File(expanded_uri.substring(5)).toURI().toURL() : new URI(expanded_uri).toURL();
                            if (debug != null) {
                                debug.println("reading " + policy_url);
                            }
                            if (PolicyFile.this.init(policy_url, newInfo)) {
                                loaded_policy = true;
                            }
                        }
                        catch (Exception e) {
                            if (debug == null) break block14;
                            debug.println("Debug info only. Error reading policy " + e);
                            e.printStackTrace();
                        }
                    }
                    ++n;
                }
                return loaded_policy;
            }
        });
        return loadedPolicy;
    }

    private void initDefaultPolicy(PolicyInfo newInfo) {
        Path defaultPolicy = builtInFS.getPath(StaticProperty.javaHome(), "lib", "security", "default.policy");
        if (debug != null) {
            debug.println("reading " + defaultPolicy);
        }
        try (BufferedReader br = Files.newBufferedReader(defaultPolicy);){
            PolicyParser pp = new PolicyParser(this.expandProperties);
            pp.read(br);
            Enumeration<PolicyParser.GrantEntry> enum_ = pp.grantElements();
            while (enum_.hasMoreElements()) {
                PolicyParser.GrantEntry ge = enum_.nextElement();
                this.addGrantEntry(ge, null, newInfo);
            }
        }
        catch (Exception e) {
            throw new InternalError("Failed to load default.policy", e);
        }
    }

    private boolean init(URL policy, PolicyInfo newInfo) {
        block17: {
            boolean bl;
            block16: {
                if (badPolicyURLs.contains(policy)) {
                    if (debug != null) {
                        debug.println("skipping bad policy file: " + policy);
                    }
                    return false;
                }
                InputStreamReader isr = this.getInputStreamReader(PolicyUtil.getInputStream(policy));
                try {
                    KeyStore keyStore;
                    PolicyParser pp;
                    block15: {
                        pp = new PolicyParser(this.expandProperties);
                        pp.read(isr);
                        keyStore = null;
                        try {
                            keyStore = PolicyUtil.getKeyStore(policy, pp.getKeyStoreUrl(), pp.getKeyStoreType(), pp.getKeyStoreProvider(), pp.getStorePassURL(), debug);
                        }
                        catch (Exception e) {
                            if (debug == null) break block15;
                            debug.println("Debug info only. Ignoring exception.");
                            e.printStackTrace();
                        }
                    }
                    Enumeration<PolicyParser.GrantEntry> enum_ = pp.grantElements();
                    while (enum_.hasMoreElements()) {
                        PolicyParser.GrantEntry ge = enum_.nextElement();
                        this.addGrantEntry(ge, keyStore, newInfo);
                    }
                    bl = true;
                    if (isr == null) break block16;
                }
                catch (Throwable pp) {
                    try {
                        if (isr != null) {
                            try {
                                isr.close();
                            }
                            catch (Throwable throwable) {
                                pp.addSuppressed(throwable);
                            }
                        }
                        throw pp;
                    }
                    catch (PolicyParser.ParsingException pe) {
                        badPolicyURLs.add(policy);
                        Object[] source = new Object[]{policy, pe.getNonlocalizedMessage()};
                        System.err.println(LocalizedMessage.getNonlocalized("java.security.policy.error.parsing.policy.message", source));
                        if (debug != null) {
                            pe.printStackTrace();
                        }
                        break block17;
                    }
                    catch (Exception e) {
                        if (debug == null) break block17;
                        debug.println("error parsing " + policy);
                        debug.println(e.toString());
                        e.printStackTrace();
                    }
                }
                isr.close();
            }
            return bl;
        }
        return false;
    }

    private InputStreamReader getInputStreamReader(InputStream is) {
        return this.notUtf8 ? new InputStreamReader(is) : new InputStreamReader(is, StandardCharsets.UTF_8);
    }

    private void initStaticPolicy(final PolicyInfo newInfo) {
        if (debug != null) {
            debug.println("Initializing with static permissions");
        }
        AccessController.doPrivileged(new PrivilegedAction<Object>(this){

            @Override
            public Void run() {
                PolicyEntry pe = new PolicyEntry(new CodeSource(null, (Certificate[])null));
                pe.add(SecurityConstants.LOCAL_LISTEN_PERMISSION);
                pe.add(new PropertyPermission("java.version", "read"));
                pe.add(new PropertyPermission("java.vendor", "read"));
                pe.add(new PropertyPermission("java.vendor.url", "read"));
                pe.add(new PropertyPermission("java.class.version", "read"));
                pe.add(new PropertyPermission("os.name", "read"));
                pe.add(new PropertyPermission("os.version", "read"));
                pe.add(new PropertyPermission("os.arch", "read"));
                pe.add(new PropertyPermission("file.separator", "read"));
                pe.add(new PropertyPermission("path.separator", "read"));
                pe.add(new PropertyPermission("line.separator", "read"));
                pe.add(new PropertyPermission("java.specification.version", "read"));
                pe.add(new PropertyPermission("java.specification.maintenance.version", "read"));
                pe.add(new PropertyPermission("java.specification.vendor", "read"));
                pe.add(new PropertyPermission("java.specification.name", "read"));
                pe.add(new PropertyPermission("java.vm.specification.version", "read"));
                pe.add(new PropertyPermission("java.vm.specification.vendor", "read"));
                pe.add(new PropertyPermission("java.vm.specification.name", "read"));
                pe.add(new PropertyPermission("java.vm.version", "read"));
                pe.add(new PropertyPermission("java.vm.vendor", "read"));
                pe.add(new PropertyPermission("java.vm.name", "read"));
                newInfo.policyEntries.add(pe);
                return null;
            }
        });
    }

    private CodeSource getCodeSource(PolicyParser.GrantEntry ge, KeyStore keyStore, PolicyInfo newInfo) throws MalformedURLException {
        Certificate[] certs = null;
        if (ge.signedBy != null && (certs = this.getCertificates(keyStore, ge.signedBy, newInfo)) == null) {
            if (debug != null) {
                debug.println("  -- No certs for alias '" + ge.signedBy + "' - ignoring entry");
            }
            return null;
        }
        URL location = ge.codeBase != null ? PolicyFile.newURL(ge.codeBase) : null;
        return this.canonicalizeCodebase(new CodeSource(location, certs), false);
    }

    private void addGrantEntry(PolicyParser.GrantEntry ge, KeyStore keyStore, PolicyInfo newInfo) {
        if (debug != null) {
            debug.println("Adding policy entry: ");
            debug.println("  signedBy " + ge.signedBy);
            debug.println("  codeBase " + ge.codeBase);
            if (ge.principals != null) {
                for (PolicyParser.PrincipalEntry pppe : ge.principals) {
                    debug.println("  " + pppe.toString());
                }
            }
        }
        try {
            CodeSource codesource = this.getCodeSource(ge, keyStore, newInfo);
            if (codesource == null) {
                return;
            }
            if (!this.replacePrincipals(ge.principals, keyStore)) {
                return;
            }
            PolicyEntry entry = new PolicyEntry(codesource, ge.principals);
            Enumeration<PolicyParser.PermissionEntry> enum_ = ge.permissionElements();
            while (enum_.hasMoreElements()) {
                Object[] source;
                Object certs;
                PolicyParser.PermissionEntry pe = enum_.nextElement();
                try {
                    Permission perm;
                    this.expandPermissionName(pe, keyStore);
                    if (pe.permission.equals("javax.security.auth.PrivateCredentialPermission") && pe.name.endsWith(" self")) {
                        pe.name = pe.name.substring(0, pe.name.indexOf("self")) + SELF;
                    }
                    if (pe.name != null && pe.name.contains(SELF)) {
                        certs = pe.signedBy != null ? this.getCertificates(keyStore, pe.signedBy, newInfo) : null;
                        perm = new SelfPermission(pe.permission, pe.name, pe.action, (Certificate[])certs);
                    } else {
                        perm = PolicyFile.getInstance(pe.permission, pe.name, pe.action);
                    }
                    entry.add(perm);
                    if (debug == null) continue;
                    debug.println("  " + perm);
                }
                catch (ClassNotFoundException cnfe) {
                    certs = pe.signedBy != null ? this.getCertificates(keyStore, pe.signedBy, newInfo) : null;
                    if (certs == null && pe.signedBy != null) continue;
                    UnresolvedPermission perm = new UnresolvedPermission(pe.permission, pe.name, pe.action, (Certificate[])certs);
                    entry.add(perm);
                    if (debug == null) continue;
                    debug.println("  " + perm);
                }
                catch (InvocationTargetException ite) {
                    source = new Object[]{pe.permission, ite.getCause().toString()};
                    System.err.println(LocalizedMessage.getNonlocalized("java.security.policy.error.adding.Permission.perm.message", source));
                }
                catch (Exception e) {
                    source = new Object[]{pe.permission, e.toString()};
                    System.err.println(LocalizedMessage.getNonlocalized("java.security.policy.error.adding.Permission.perm.message", source));
                }
            }
            newInfo.policyEntries.add(entry);
        }
        catch (Exception e) {
            Object[] source = new Object[]{e.toString()};
            System.err.println(LocalizedMessage.getNonlocalized("java.security.policy.error.adding.Entry.message", source));
        }
        if (debug != null) {
            debug.println();
        }
    }

    private static final Permission getInstance(String type, String name, String actions) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<?> pc = Class.forName(type, false, null);
        Permission answer = PolicyFile.getKnownPermission(pc, name, actions);
        if (answer != null) {
            return answer;
        }
        if (!Permission.class.isAssignableFrom(pc)) {
            throw new ClassCastException(type + " is not a Permission");
        }
        if (name == null && actions == null) {
            try {
                Constructor<?> c = pc.getConstructor(PARAMS0);
                return (Permission)c.newInstance(new Object[0]);
            }
            catch (NoSuchMethodException ne) {
                try {
                    Constructor<?> c = pc.getConstructor(PARAMS1);
                    return (Permission)c.newInstance(name);
                }
                catch (NoSuchMethodException ne1) {
                    Constructor<?> c = pc.getConstructor(PARAMS2);
                    return (Permission)c.newInstance(name, actions);
                }
            }
        }
        if (name != null && actions == null) {
            try {
                Constructor<?> c = pc.getConstructor(PARAMS1);
                return (Permission)c.newInstance(name);
            }
            catch (NoSuchMethodException ne) {
                Constructor<?> c = pc.getConstructor(PARAMS2);
                return (Permission)c.newInstance(name, actions);
            }
        }
        Constructor<?> c = pc.getConstructor(PARAMS2);
        return (Permission)c.newInstance(name, actions);
    }

    private static Permission getKnownPermission(Class<?> claz, String name, String actions) {
        if (claz.equals(FilePermission.class)) {
            return new FilePermission(name, actions);
        }
        if (claz.equals(SocketPermission.class)) {
            return new SocketPermission(name, actions);
        }
        if (claz.equals(RuntimePermission.class)) {
            return new RuntimePermission(name, actions);
        }
        if (claz.equals(PropertyPermission.class)) {
            return new PropertyPermission(name, actions);
        }
        if (claz.equals(NetPermission.class)) {
            return new NetPermission(name, actions);
        }
        if (claz.equals(AllPermission.class)) {
            return SecurityConstants.ALL_PERMISSION;
        }
        if (claz.equals(SecurityPermission.class)) {
            return new SecurityPermission(name, actions);
        }
        return null;
    }

    private static Principal getKnownPrincipal(Class<?> claz, String name) {
        if (claz.equals(X500Principal.class)) {
            return new X500Principal(name);
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Certificate[] getCertificates(KeyStore keyStore, String aliases, PolicyInfo newInfo) {
        ArrayList<Certificate> vcerts = null;
        StringTokenizer st = new StringTokenizer(aliases, ",");
        int n = 0;
        while (st.hasMoreTokens()) {
            String alias = st.nextToken().trim();
            ++n;
            Certificate cert = null;
            Map<Object, Object> map = newInfo.aliasMapping;
            synchronized (map) {
                cert = (Certificate)newInfo.aliasMapping.get(alias);
                if (cert == null && keyStore != null) {
                    try {
                        cert = keyStore.getCertificate(alias);
                    }
                    catch (KeyStoreException keyStoreException) {
                        // empty catch block
                    }
                    if (cert != null) {
                        newInfo.aliasMapping.put(alias, cert);
                        newInfo.aliasMapping.put(cert, alias);
                    }
                }
            }
            if (cert == null) continue;
            if (vcerts == null) {
                vcerts = new ArrayList<Certificate>();
            }
            vcerts.add(cert);
        }
        if (vcerts != null && n == vcerts.size()) {
            Certificate[] certs = new Certificate[vcerts.size()];
            vcerts.toArray(certs);
            return certs;
        }
        return null;
    }

    @Override
    public void refresh() {
        this.init(this.url);
    }

    @Override
    public boolean implies(ProtectionDomain pd, Permission p) {
        JavaSecurityAccess.ProtectionDomainCache pdMap = this.policyInfo.getPdMapping();
        PermissionCollection pc = pdMap.get(pd);
        if (pc != null) {
            return pc.implies(p);
        }
        pc = this.getPermissions(pd);
        if (pc == null) {
            return false;
        }
        pdMap.put(pd, pc);
        return pc.implies(p);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        Permissions perms = new Permissions();
        if (domain == null) {
            return perms;
        }
        this.getPermissions(perms, domain);
        PermissionCollection pc = domain.getPermissions();
        if (pc != null) {
            PermissionCollection permissionCollection = pc;
            synchronized (permissionCollection) {
                Enumeration<Permission> e = pc.elements();
                while (e.hasMoreElements()) {
                    perms.add(FilePermCompat.newPermPlusAltPath(e.nextElement()));
                }
            }
        }
        return perms;
    }

    @Override
    public PermissionCollection getPermissions(CodeSource codesource) {
        return this.getPermissions(new Permissions(), codesource);
    }

    private PermissionCollection getPermissions(Permissions perms, ProtectionDomain pd) {
        CodeSource cs;
        if (debug != null) {
            debug.println("getPermissions:\n\t" + this.printPD(pd));
        }
        if ((cs = pd.getCodeSource()) == null) {
            return perms;
        }
        CodeSource canonCodeSource = AccessController.doPrivileged(new PrivilegedAction<CodeSource>(){

            @Override
            public CodeSource run() {
                return PolicyFile.this.canonicalizeCodebase(cs, true);
            }
        });
        return this.getPermissions(perms, canonCodeSource, pd.getPrincipals());
    }

    private PermissionCollection getPermissions(Permissions perms, final CodeSource cs) {
        if (cs == null) {
            return perms;
        }
        CodeSource canonCodeSource = AccessController.doPrivileged(new PrivilegedAction<CodeSource>(this){
            final /* synthetic */ PolicyFile this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public CodeSource run() {
                return this.this$0.canonicalizeCodebase(cs, true);
            }
        });
        return this.getPermissions(perms, canonCodeSource, null);
    }

    private Permissions getPermissions(Permissions perms, CodeSource cs, Principal[] principals) {
        for (PolicyEntry entry : this.policyInfo.policyEntries) {
            this.addPermissions(perms, cs, principals, entry);
        }
        return perms;
    }

    private void addPermissions(Permissions perms, final CodeSource cs, Principal[] principals, final PolicyEntry entry) {
        Boolean imp;
        if (debug != null) {
            debug.println("evaluate codesources:\n\tPolicy CodeSource: " + entry.getCodeSource() + "\n\tActive CodeSource: " + cs);
        }
        if (!(imp = AccessController.doPrivileged(new PrivilegedAction<Boolean>(this){

            @Override
            public Boolean run() {
                return entry.getCodeSource().implies(cs);
            }
        })).booleanValue()) {
            if (debug != null) {
                debug.println("evaluation (codesource) failed");
            }
            return;
        }
        List<PolicyParser.PrincipalEntry> entryPs = entry.getPrincipals();
        if (debug != null) {
            ArrayList<PolicyParser.PrincipalEntry> accPs = new ArrayList<PolicyParser.PrincipalEntry>();
            if (principals != null) {
                for (int i = 0; i < principals.length; ++i) {
                    accPs.add(new PolicyParser.PrincipalEntry(principals[i].getClass().getName(), principals[i].getName()));
                }
            }
            debug.println("evaluate principals:\n\tPolicy Principals: " + entryPs + "\n\tActive Principals: " + accPs);
        }
        if (entryPs == null || entryPs.isEmpty()) {
            this.addPerms(perms, principals, entry);
            if (debug != null) {
                debug.println("evaluation (codesource/principals) passed");
            }
            return;
        }
        if (principals == null || principals.length == 0) {
            if (debug != null) {
                debug.println("evaluation (principals) failed");
            }
            return;
        }
        for (PolicyParser.PrincipalEntry pppe : entryPs) {
            if (pppe.isWildcardClass()) continue;
            if (pppe.isWildcardName()) {
                if (PolicyFile.wildcardPrincipalNameImplies(pppe.principalClass, principals)) continue;
                if (debug != null) {
                    debug.println("evaluation (principal name wildcard) failed");
                }
                return;
            }
            HashSet<Principal> pSet = new HashSet<Principal>(Arrays.asList(principals));
            Subject subject = new Subject(true, pSet, Collections.EMPTY_SET, Collections.EMPTY_SET);
            try {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Class<?> pClass = Class.forName(pppe.principalClass, false, cl);
                Principal p = PolicyFile.getKnownPrincipal(pClass, pppe.principalName);
                if (p == null) {
                    if (!Principal.class.isAssignableFrom(pClass)) {
                        throw new ClassCastException(pppe.principalClass + " is not a Principal");
                    }
                    Constructor<?> c = pClass.getConstructor(PARAMS1);
                    p = (Principal)c.newInstance(pppe.principalName);
                }
                if (debug != null) {
                    debug.println("found Principal " + p.getClass().getName());
                }
                if (p.implies(subject)) continue;
                if (debug != null) {
                    debug.println("evaluation (principal implies) failed");
                }
                return;
            }
            catch (Exception e) {
                if (debug != null) {
                    e.printStackTrace();
                }
                if (pppe.implies(subject)) continue;
                if (debug != null) {
                    debug.println("evaluation (default principal implies) failed");
                }
                return;
            }
        }
        if (debug != null) {
            debug.println("evaluation (codesource/principals) passed");
        }
        this.addPerms(perms, principals, entry);
    }

    private static boolean wildcardPrincipalNameImplies(String principalClass, Principal[] principals) {
        for (Principal p : principals) {
            if (!principalClass.equals(p.getClass().getName())) continue;
            return true;
        }
        return false;
    }

    private void addPerms(Permissions perms, Principal[] accPs, PolicyEntry entry) {
        for (int i = 0; i < entry.permissions.size(); ++i) {
            Permission p = entry.permissions.get(i);
            if (debug != null) {
                debug.println("  granting " + p);
            }
            if (p instanceof SelfPermission) {
                this.expandSelf((SelfPermission)p, entry.getPrincipals(), accPs, perms);
                continue;
            }
            perms.add(FilePermCompat.newPermPlusAltPath(p));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void expandSelf(SelfPermission sp, List<PolicyParser.PrincipalEntry> entryPs, Principal[] pdp, Permissions perms) {
        String[][] principalInfo;
        int v;
        if (entryPs == null || entryPs.isEmpty()) {
            if (debug == null) return;
            debug.println("Ignoring permission " + sp.getSelfType() + " with target name (" + sp.getSelfName() + ").  No Principal(s) specified in the grant clause.  SELF-based target names are only valid in the context of a Principal-based grant entry.");
            return;
        }
        int startIndex = 0;
        StringBuilder sb = new StringBuilder();
        while ((v = sp.getSelfName().indexOf(SELF, startIndex)) != -1) {
            sb.append(sp.getSelfName().substring(startIndex, v));
            Iterator<PolicyParser.PrincipalEntry> pli = entryPs.iterator();
            while (pli.hasNext()) {
                PolicyParser.PrincipalEntry pppe = pli.next();
                principalInfo = this.getPrincipalInfo(pppe, pdp);
                for (int i = 0; i < principalInfo.length; ++i) {
                    if (i != 0) {
                        sb.append(", ");
                    }
                    sb.append(principalInfo[i][0] + " \"" + principalInfo[i][1] + "\"");
                }
                if (!pli.hasNext()) continue;
                sb.append(", ");
            }
            startIndex = v + SELF.length();
        }
        sb.append(sp.getSelfName().substring(startIndex));
        if (debug != null) {
            debug.println("  expanded:\n\t" + sp.getSelfName() + "\n  into:\n\t" + sb.toString());
        }
        try {
            perms.add(FilePermCompat.newPermPlusAltPath(PolicyFile.getInstance(sp.getSelfType(), sb.toString(), sp.getSelfActions())));
            return;
        }
        catch (ClassNotFoundException cnfe) {
            Class<?> pc = null;
            principalInfo = perms;
            synchronized (perms) {
                Enumeration<Permission> e = perms.elements();
                while (e.hasMoreElements()) {
                    Permission pElement = e.nextElement();
                    if (!pElement.getClass().getName().equals(sp.getSelfType())) continue;
                    pc = pElement.getClass();
                    break;
                }
                // ** MonitorExit[principalInfo] (shouldn't be in output)
                if (pc == null) {
                    perms.add(new UnresolvedPermission(sp.getSelfType(), sb.toString(), sp.getSelfActions(), sp.getCerts()));
                    return;
                }
                try {
                    Constructor<Object> c;
                    if (sp.getSelfActions() == null) {
                        try {
                            c = pc.getConstructor(PARAMS1);
                            perms.add((Permission)c.newInstance(sb.toString()));
                            return;
                        }
                        catch (NoSuchMethodException ne) {
                            c = pc.getConstructor(PARAMS2);
                            perms.add((Permission)c.newInstance(sb.toString(), sp.getSelfActions()));
                        }
                        return;
                    }
                    c = pc.getConstructor(PARAMS2);
                    perms.add((Permission)c.newInstance(sb.toString(), sp.getSelfActions()));
                    return;
                }
                catch (Exception nme) {
                    if (debug == null) return;
                    debug.println("self entry expansion  instantiation failed: " + nme.toString());
                }
                return;
            }
        }
        catch (Exception e) {
            if (debug == null) return;
            debug.println(e.toString());
        }
    }

    private String[][] getPrincipalInfo(PolicyParser.PrincipalEntry pe, Principal[] pdp) {
        if (!pe.isWildcardClass() && !pe.isWildcardName()) {
            String[][] info = new String[1][2];
            info[0][0] = pe.principalClass;
            info[0][1] = pe.principalName;
            return info;
        }
        if (!pe.isWildcardClass() && pe.isWildcardName()) {
            ArrayList<Principal> plist = new ArrayList<Principal>();
            for (int i = 0; i < pdp.length; ++i) {
                if (!pe.principalClass.equals(pdp[i].getClass().getName())) continue;
                plist.add(pdp[i]);
            }
            String[][] info = new String[plist.size()][2];
            int i = 0;
            for (Principal p : plist) {
                info[i][0] = p.getClass().getName();
                info[i][1] = p.getName();
                ++i;
            }
            return info;
        }
        String[][] info = new String[pdp.length][2];
        for (int i = 0; i < pdp.length; ++i) {
            info[i][0] = pdp[i].getClass().getName();
            info[i][1] = pdp[i].getName();
        }
        return info;
    }

    protected Certificate[] getSignerCertificates(CodeSource cs) {
        int i;
        Certificate[] certs = null;
        certs = cs.getCertificates();
        if (certs == null) {
            return null;
        }
        for (i = 0; i < certs.length; ++i) {
            if (certs[i] instanceof X509Certificate) continue;
            return cs.getCertificates();
        }
        int count = 0;
        for (i = 0; i < certs.length; ++i) {
            ++count;
            while (i + 1 < certs.length && ((X509Certificate)certs[i]).getIssuerX500Principal().equals(((X509Certificate)certs[i + 1]).getSubjectX500Principal())) {
                ++i;
            }
        }
        if (count == certs.length) {
            return certs;
        }
        ArrayList<Certificate> userCertList = new ArrayList<Certificate>();
        for (i = 0; i < certs.length; ++i) {
            userCertList.add(certs[i]);
            while (i + 1 < certs.length && ((X509Certificate)certs[i]).getIssuerX500Principal().equals(((X509Certificate)certs[i + 1]).getSubjectX500Principal())) {
                ++i;
            }
        }
        Certificate[] userCerts = new Certificate[userCertList.size()];
        userCertList.toArray(userCerts);
        return userCerts;
    }

    private CodeSource canonicalizeCodebase(CodeSource cs, boolean extractSignerCerts) {
        CodeSource canonCs;
        block12: {
            String path = null;
            canonCs = cs;
            URL u = cs.getLocation();
            if (u != null) {
                String spec;
                int separator;
                if (u.getProtocol().equals("jar") && (separator = (spec = u.getFile()).indexOf("!/")) != -1) {
                    try {
                        u = PolicyFile.newURL(spec.substring(0, separator));
                    }
                    catch (MalformedURLException malformedURLException) {
                        // empty catch block
                    }
                }
                if (u.getProtocol().equals("file")) {
                    boolean isLocalFile = false;
                    String host = u.getHost();
                    boolean bl = isLocalFile = host == null || host.isEmpty() || host.equals("~") || host.equalsIgnoreCase("localhost");
                    if (isLocalFile) {
                        path = u.getFile().replace('/', File.separatorChar);
                        path = ParseUtil.decode(path);
                    }
                }
            }
            if (path != null) {
                try {
                    URL csUrl = null;
                    path = PolicyFile.canonPath(path);
                    csUrl = ParseUtil.fileToEncodedURL(new File(path));
                    if (extractSignerCerts) {
                        canonCs = new CodeSource(csUrl, this.getSignerCertificates(cs));
                        break block12;
                    }
                    canonCs = new CodeSource(csUrl, cs.getCertificates());
                }
                catch (IOException ioe) {
                    if (extractSignerCerts) {
                        canonCs = new CodeSource(cs.getLocation(), this.getSignerCertificates(cs));
                    }
                    break block12;
                }
            }
            if (extractSignerCerts) {
                canonCs = new CodeSource(cs.getLocation(), this.getSignerCertificates(cs));
            }
        }
        return canonCs;
    }

    private static String canonPath(String path) throws IOException {
        if (path.endsWith("*")) {
            path = path.substring(0, path.length() - 1) + "-";
            path = new File(path).getCanonicalPath();
            return path.substring(0, path.length() - 1) + "*";
        }
        return new File(path).getCanonicalPath();
    }

    private String printPD(ProtectionDomain pd) {
        Principal[] principals = pd.getPrincipals();
        String pals = "<no principals>";
        if (principals != null && principals.length > 0) {
            StringBuilder palBuf = new StringBuilder("(principals ");
            for (int i = 0; i < principals.length; ++i) {
                palBuf.append(principals[i].getClass().getName() + " \"" + principals[i].getName() + "\"");
                if (i < principals.length - 1) {
                    palBuf.append(", ");
                    continue;
                }
                palBuf.append(")");
            }
            pals = palBuf.toString();
        }
        return "PD CodeSource: " + pd.getCodeSource() + "\n\tPD ClassLoader: " + pd.getClassLoader() + "\n\tPD Principals: " + pals;
    }

    private boolean replacePrincipals(List<PolicyParser.PrincipalEntry> principals, KeyStore keystore) {
        if (principals == null || principals.isEmpty() || keystore == null) {
            return true;
        }
        for (PolicyParser.PrincipalEntry pppe : principals) {
            if (!pppe.isReplaceName()) continue;
            String name = this.getDN(pppe.principalName, keystore);
            if (name == null) {
                return false;
            }
            if (debug != null) {
                debug.println("  Replacing \"" + pppe.principalName + "\" with " + X500PRINCIPAL + "/\"" + name + "\"");
            }
            pppe.principalClass = X500PRINCIPAL;
            pppe.principalName = name;
        }
        return true;
    }

    private void expandPermissionName(PolicyParser.PermissionEntry pe, KeyStore keystore) throws Exception {
        int e;
        int b;
        if (pe.name == null || pe.name.indexOf("${{", 0) == -1) {
            return;
        }
        int startIndex = 0;
        StringBuilder sb = new StringBuilder();
        while ((b = pe.name.indexOf("${{", startIndex)) != -1 && (e = pe.name.indexOf("}}", b)) >= 1) {
            String value;
            sb.append(pe.name.substring(startIndex, b));
            String prefix = value = pe.name.substring(b + 3, e);
            int colonIndex = value.indexOf(58);
            if (colonIndex != -1) {
                prefix = value.substring(0, colonIndex);
            }
            if (prefix.equalsIgnoreCase("self")) {
                sb.append(pe.name.substring(b, e + 2));
                startIndex = e + 2;
                continue;
            }
            if (prefix.equalsIgnoreCase("alias")) {
                if (colonIndex == -1) {
                    Object[] source = new Object[]{pe.name};
                    throw new Exception(LocalizedMessage.getNonlocalized("alias.name.not.provided.pe.name.", source));
                }
                String suffix = value.substring(colonIndex + 1);
                if ((suffix = this.getDN(suffix, keystore)) == null) {
                    Object[] source = new Object[]{value.substring(colonIndex + 1)};
                    throw new Exception(LocalizedMessage.getNonlocalized("unable.to.perform.substitution.on.alias.suffix", source));
                }
                sb.append("javax.security.auth.x500.X500Principal \"" + suffix + "\"");
                startIndex = e + 2;
                continue;
            }
            Object[] source = new Object[]{prefix};
            throw new Exception(LocalizedMessage.getNonlocalized("substitution.value.prefix.unsupported", source));
        }
        sb.append(pe.name.substring(startIndex));
        if (debug != null) {
            debug.println("  Permission name expanded from:\n\t" + pe.name + "\nto\n\t" + sb.toString());
        }
        pe.name = sb.toString();
    }

    private String getDN(String alias, KeyStore keystore) {
        Certificate cert = null;
        try {
            cert = keystore.getCertificate(alias);
        }
        catch (Exception e) {
            if (debug != null) {
                debug.println("  Error retrieving certificate for '" + alias + "': " + e.toString());
            }
            return null;
        }
        if (!(cert instanceof X509Certificate)) {
            if (debug != null) {
                debug.println("  -- No certificate for '" + alias + "' - ignoring entry");
            }
            return null;
        }
        X509Certificate x509Cert = (X509Certificate)cert;
        X500Principal p = new X500Principal(x509Cert.getSubjectX500Principal().toString());
        return p.getName();
    }

    private static URL newURL(String spec) throws MalformedURLException {
        return new URL(spec);
    }

    private static class PolicyInfo {
        private static final boolean verbose = false;
        final List<PolicyEntry> policyEntries = new ArrayList<PolicyEntry>();
        final Map<Object, Object> aliasMapping = Collections.synchronizedMap(new HashMap(11));
        private final JavaSecurityAccess.ProtectionDomainCache[] pdMapping;
        private Random random;

        PolicyInfo(int numCaches) {
            this.pdMapping = new JavaSecurityAccess.ProtectionDomainCache[numCaches];
            JavaSecurityAccess jspda = SharedSecrets.getJavaSecurityAccess();
            for (int i = 0; i < numCaches; ++i) {
                this.pdMapping[i] = jspda.getProtectionDomainCache();
            }
            if (numCaches > 1) {
                this.random = new Random();
            }
        }

        JavaSecurityAccess.ProtectionDomainCache getPdMapping() {
            if (this.pdMapping.length == 1) {
                return this.pdMapping[0];
            }
            int i = Math.abs(this.random.nextInt() % this.pdMapping.length);
            return this.pdMapping[i];
        }
    }

    private static class PolicyEntry {
        private final CodeSource codesource;
        final List<Permission> permissions;
        private final List<PolicyParser.PrincipalEntry> principals;

        PolicyEntry(CodeSource cs, List<PolicyParser.PrincipalEntry> principals) {
            this.codesource = cs;
            this.permissions = new ArrayList<Permission>();
            this.principals = principals;
        }

        PolicyEntry(CodeSource cs) {
            this(cs, null);
        }

        List<PolicyParser.PrincipalEntry> getPrincipals() {
            return this.principals;
        }

        void add(Permission p) {
            this.permissions.add(p);
        }

        CodeSource getCodeSource() {
            return this.codesource;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(ResourcesMgr.getString("LPARAM"));
            sb.append(this.getCodeSource());
            sb.append("\n");
            for (int j = 0; j < this.permissions.size(); ++j) {
                Permission p = this.permissions.get(j);
                sb.append(ResourcesMgr.getString("SPACE"));
                sb.append(ResourcesMgr.getString("SPACE"));
                sb.append(p);
                sb.append(ResourcesMgr.getString("NEWLINE"));
            }
            sb.append(ResourcesMgr.getString("RPARAM"));
            sb.append(ResourcesMgr.getString("NEWLINE"));
            return sb.toString();
        }
    }

    private static class SelfPermission
    extends Permission {
        private static final long serialVersionUID = -8315562579967246806L;
        private String type;
        private String name;
        private String actions;
        private Certificate[] certs;

        public SelfPermission(String type, String name, String actions, Certificate[] certs) {
            super(type);
            if (type == null) {
                throw new NullPointerException(LocalizedMessage.getNonlocalized("type.can.t.be.null", new Object[0]));
            }
            this.type = type;
            this.name = name;
            this.actions = actions;
            if (certs != null) {
                int i;
                for (i = 0; i < certs.length; ++i) {
                    if (certs[i] instanceof X509Certificate) continue;
                    this.certs = (Certificate[])certs.clone();
                    break;
                }
                if (this.certs == null) {
                    int count = 0;
                    for (i = 0; i < certs.length; ++i) {
                        ++count;
                        while (i + 1 < certs.length && ((X509Certificate)certs[i]).getIssuerX500Principal().equals(((X509Certificate)certs[i + 1]).getSubjectX500Principal())) {
                            ++i;
                        }
                    }
                    if (count == certs.length) {
                        this.certs = (Certificate[])certs.clone();
                    }
                    if (this.certs == null) {
                        ArrayList<Certificate> signerCerts = new ArrayList<Certificate>();
                        for (i = 0; i < certs.length; ++i) {
                            signerCerts.add(certs[i]);
                            while (i + 1 < certs.length && ((X509Certificate)certs[i]).getIssuerX500Principal().equals(((X509Certificate)certs[i + 1]).getSubjectX500Principal())) {
                                ++i;
                            }
                        }
                        this.certs = new Certificate[signerCerts.size()];
                        signerCerts.toArray(this.certs);
                    }
                }
            }
        }

        @Override
        public boolean implies(Permission p) {
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            int j;
            boolean match;
            int i;
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof SelfPermission)) {
                return false;
            }
            SelfPermission that = (SelfPermission)obj;
            if (!(this.type.equals(that.type) && this.name.equals(that.name) && this.actions.equals(that.actions))) {
                return false;
            }
            if (this.certs == null && that.certs == null) {
                return true;
            }
            if (this.certs == null || that.certs == null) {
                return false;
            }
            if (this.certs.length != that.certs.length) {
                return false;
            }
            for (i = 0; i < this.certs.length; ++i) {
                match = false;
                for (j = 0; j < that.certs.length; ++j) {
                    if (!this.certs[i].equals(that.certs[j])) continue;
                    match = true;
                    break;
                }
                if (match) continue;
                return false;
            }
            for (i = 0; i < that.certs.length; ++i) {
                match = false;
                for (j = 0; j < this.certs.length; ++j) {
                    if (!that.certs[i].equals(this.certs[j])) continue;
                    match = true;
                    break;
                }
                if (match) continue;
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = this.type.hashCode();
            if (this.name != null) {
                hash ^= this.name.hashCode();
            }
            if (this.actions != null) {
                hash ^= this.actions.hashCode();
            }
            return hash;
        }

        @Override
        public String getActions() {
            return "";
        }

        public String getSelfType() {
            return this.type;
        }

        public String getSelfName() {
            return this.name;
        }

        public String getSelfActions() {
            return this.actions;
        }

        public Certificate[] getCerts() {
            return this.certs == null ? null : (Certificate[])this.certs.clone();
        }

        @Override
        public String toString() {
            return "(SelfPermission " + this.type + " " + this.name + " " + this.actions + ")";
        }

        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            if (this.certs != null) {
                this.certs = (Certificate[])this.certs.clone();
            }
        }
    }
}

