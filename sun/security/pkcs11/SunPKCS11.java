/*
 * Decompiled with CFR 0.152.
 */
package sun.security.pkcs11;

import com.sun.crypto.provider.ChaCha20Poly1305Parameters;
import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.security.AccessController;
import java.security.AuthProvider;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.Provider;
import java.security.ProviderException;
import java.security.Security;
import java.security.SecurityPermission;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import jdk.internal.misc.InnocuousThread;
import sun.security.pkcs11.Config;
import sun.security.pkcs11.P11AEADCipher;
import sun.security.pkcs11.P11Cipher;
import sun.security.pkcs11.P11Digest;
import sun.security.pkcs11.P11ECDHKeyAgreement;
import sun.security.pkcs11.P11Key;
import sun.security.pkcs11.P11KeyAgreement;
import sun.security.pkcs11.P11KeyGenerator;
import sun.security.pkcs11.P11KeyPairGenerator;
import sun.security.pkcs11.P11KeyWrapCipher;
import sun.security.pkcs11.P11Mac;
import sun.security.pkcs11.P11PBECipher;
import sun.security.pkcs11.P11PSSSignature;
import sun.security.pkcs11.P11RSACipher;
import sun.security.pkcs11.P11SecretKeyFactory;
import sun.security.pkcs11.P11Signature;
import sun.security.pkcs11.P11TlsKeyMaterialGenerator;
import sun.security.pkcs11.P11TlsMasterSecretGenerator;
import sun.security.pkcs11.P11TlsPrfGenerator;
import sun.security.pkcs11.P11TlsRsaPremasterSecretGenerator;
import sun.security.pkcs11.P11Util;
import sun.security.pkcs11.Secmod;
import sun.security.pkcs11.Session;
import sun.security.pkcs11.Token;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_INFO;
import sun.security.pkcs11.wrapper.CK_MECHANISM_INFO;
import sun.security.pkcs11.wrapper.CK_SLOT_INFO;
import sun.security.pkcs11.wrapper.CK_VERSION;
import sun.security.pkcs11.wrapper.Functions;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Exception;
import sun.security.util.Debug;
import sun.security.util.ECParameters;
import sun.security.util.GCMParameters;
import sun.security.util.ResourcesMgr;
import sun.security.util.SecurityConstants;
import sun.security.util.SecurityProviderConstants;

public final class SunPKCS11
extends AuthProvider {
    private static final long serialVersionUID = -1354835039035306505L;
    static final Debug debug = Debug.getInstance("sunpkcs11");
    final PKCS11 p11;
    final Config config;
    final long slotID;
    private CallbackHandler pHandler;
    private final Object LOCK_HANDLER = new Object();
    final boolean removable;
    final Secmod.Module nssModule;
    final boolean nssUseSecmodTrust;
    private volatile Token token;
    private TokenPoller poller;
    static NativeResourceCleaner cleaner;
    private static final Map<Integer, List<Descriptor>> descriptors;
    private static final String MD = "MessageDigest";
    private static final String SIG = "Signature";
    private static final String KPG = "KeyPairGenerator";
    private static final String KG = "KeyGenerator";
    private static final String AGP = "AlgorithmParameters";
    private static final String KF = "KeyFactory";
    private static final String SKF = "SecretKeyFactory";
    private static final String CIP = "Cipher";
    private static final String MAC = "Mac";
    private static final String KA = "KeyAgreement";
    private static final String KS = "KeyStore";
    private static final String SR = "SecureRandom";

    Token getToken() {
        return this.token;
    }

    public SunPKCS11() {
        super("SunPKCS11", SecurityConstants.PROVIDER_VER, "Unconfigured and unusable PKCS11 provider");
        this.p11 = null;
        this.config = null;
        this.slotID = 0L;
        this.pHandler = null;
        this.removable = false;
        this.nssModule = null;
        this.nssUseSecmodTrust = false;
        this.token = null;
        this.poller = null;
    }

    @Override
    public Provider configure(String configArg) throws InvalidParameterException {
        final String newConfigName = SunPKCS11.checkNull(configArg);
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Provider>(this){

                @Override
                public SunPKCS11 run() throws Exception {
                    return new SunPKCS11(new Config(newConfigName));
                }
            });
        }
        catch (PrivilegedActionException pae) {
            throw new InvalidParameterException("Error configuring SunPKCS11 provider", pae.getException());
        }
    }

    @Override
    public boolean isConfigured() {
        return this.config != null;
    }

    private static <T> T checkNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }

    SunPKCS11(Config c) {
        super("SunPKCS11-" + c.getName(), SecurityConstants.PROVIDER_VER, c.getDescription());
        this.config = c;
        if (debug != null) {
            debug.println("SunPKCS11 loading " + this.config.getFileName());
        }
        String library = this.config.getLibrary();
        String functionList = this.config.getFunctionList();
        long slotID = this.config.getSlotID();
        int slotListIndex = this.config.getSlotListIndex();
        boolean useSecmod = this.config.getNssUseSecmod();
        boolean nssUseSecmodTrust = this.config.getNssUseSecmodTrust();
        Secmod.Module nssModule = null;
        if (useSecmod) {
            String moduleName;
            Secmod secmod = Secmod.getInstance();
            Secmod.DbMode nssDbMode = this.config.getNssDbMode();
            try {
                String nssLibraryDirectory = this.config.getNssLibraryDirectory();
                String nssSecmodDirectory = this.config.getNssSecmodDirectory();
                boolean nssOptimizeSpace = this.config.getNssOptimizeSpace();
                if (secmod.isInitialized()) {
                    String s;
                    if (nssSecmodDirectory != null && (s = secmod.getConfigDir()) != null && !s.equals(nssSecmodDirectory)) {
                        throw new ProviderException("Secmod directory " + nssSecmodDirectory + " invalid, NSS already initialized with " + s);
                    }
                    if (nssLibraryDirectory != null && (s = secmod.getLibDir()) != null && !s.equals(nssLibraryDirectory)) {
                        throw new ProviderException("NSS library directory " + nssLibraryDirectory + " invalid, NSS already initialized with " + s);
                    }
                } else {
                    if (nssDbMode != Secmod.DbMode.NO_DB) {
                        if (nssSecmodDirectory == null) {
                            throw new ProviderException("Secmod not initialized and nssSecmodDirectory not specified");
                        }
                    } else if (nssSecmodDirectory != null) {
                        throw new ProviderException("nssSecmodDirectory must not be specified in noDb mode");
                    }
                    secmod.initialize(nssDbMode, nssSecmodDirectory, nssLibraryDirectory, nssOptimizeSpace);
                }
            }
            catch (IOException e) {
                throw new ProviderException("Could not initialize NSS", e);
            }
            List<Secmod.Module> modules = secmod.getModules();
            if (this.config.getShowInfo()) {
                System.out.println("NSS modules: " + String.valueOf(modules));
            }
            if ((moduleName = this.config.getNssModule()) == null) {
                nssModule = secmod.getModule(Secmod.ModuleType.FIPS);
                if (nssModule != null) {
                    moduleName = "fips";
                } else {
                    String string = moduleName = nssDbMode == Secmod.DbMode.NO_DB ? "crypto" : "keystore";
                }
            }
            if (moduleName.equals("fips")) {
                nssModule = secmod.getModule(Secmod.ModuleType.FIPS);
                nssUseSecmodTrust = true;
                functionList = "FC_GetFunctionList";
            } else if (moduleName.equals("keystore")) {
                nssModule = secmod.getModule(Secmod.ModuleType.KEYSTORE);
                nssUseSecmodTrust = true;
            } else if (moduleName.equals("crypto")) {
                nssModule = secmod.getModule(Secmod.ModuleType.CRYPTO);
            } else if (moduleName.equals("trustanchors")) {
                nssModule = secmod.getModule(Secmod.ModuleType.TRUSTANCHOR);
                nssUseSecmodTrust = true;
            } else if (moduleName.startsWith("external-")) {
                int moduleIndex;
                try {
                    moduleIndex = Integer.parseInt(moduleName.substring("external-".length()));
                }
                catch (NumberFormatException e) {
                    moduleIndex = -1;
                }
                if (moduleIndex < 1) {
                    throw new ProviderException("Invalid external module: " + moduleName);
                }
                int k = 0;
                for (Secmod.Module module : modules) {
                    if (module.getType() != Secmod.ModuleType.EXTERNAL || ++k != moduleIndex) continue;
                    nssModule = module;
                    break;
                }
                if (nssModule == null) {
                    throw new ProviderException("Invalid module " + moduleName + ": only " + k + " external NSS modules available");
                }
            } else {
                throw new ProviderException("Unknown NSS module: " + moduleName);
            }
            if (nssModule == null) {
                throw new ProviderException("NSS module not available: " + moduleName);
            }
            if (nssModule.hasInitializedProvider()) {
                throw new ProviderException("Secmod module already configured");
            }
            library = nssModule.libraryName;
            slotListIndex = nssModule.slot;
        }
        this.nssUseSecmodTrust = nssUseSecmodTrust;
        this.nssModule = nssModule;
        File libraryFile = new File(library);
        if (!libraryFile.getName().equals(library) && !new File(library).isFile()) {
            String msg = "Library " + library + " does not exist";
            if (this.config.getHandleStartupErrors() == 1) {
                throw new ProviderException(msg);
            }
            throw new UnsupportedOperationException(msg);
        }
        try {
            PKCS11 tmpPKCS11;
            if (debug != null) {
                debug.println("Initializing PKCS#11 library " + library);
            }
            CK_C_INITIALIZE_ARGS initArgs = new CK_C_INITIALIZE_ARGS();
            String nssArgs = this.config.getNssArgs();
            if (nssArgs != null) {
                initArgs.pReserved = nssArgs;
            }
            initArgs.flags = 2L;
            try {
                tmpPKCS11 = PKCS11.getInstance(library, functionList, initArgs, this.config.getOmitInitialize());
            }
            catch (PKCS11Exception e) {
                if (debug != null) {
                    debug.println("Multi-threaded initialization failed: " + String.valueOf(e));
                }
                if (!this.config.getAllowSingleThreadedModules()) {
                    throw e;
                }
                if (nssArgs == null) {
                    initArgs = null;
                } else {
                    initArgs.flags = 0L;
                }
                tmpPKCS11 = PKCS11.getInstance(library, functionList, initArgs, this.config.getOmitInitialize());
            }
            this.p11 = tmpPKCS11;
            if (this.p11.getVersion().major < 2) {
                throw new ProviderException("Only PKCS#11 v2.0 and later supported, library version is v" + String.valueOf(this.p11.getVersion()));
            }
            boolean showInfo = this.config.getShowInfo();
            if (showInfo) {
                CK_INFO p11Info = this.p11.C_GetInfo();
                System.out.println("Information for provider " + this.getName());
                System.out.println("Library info:");
                System.out.println(p11Info);
            }
            if (slotID < 0L || showInfo) {
                long[] slots = this.p11.C_GetSlotList(false);
                if (showInfo) {
                    System.out.println("All slots: " + SunPKCS11.toString(slots));
                    System.out.println("Slots with tokens: " + SunPKCS11.toString(this.p11.C_GetSlotList(true)));
                }
                if (slotID < 0L) {
                    if (slotListIndex < 0 || slotListIndex >= slots.length) {
                        throw new ProviderException("slotListIndex is " + slotListIndex + " but token only has " + slots.length + " slots");
                    }
                    slotID = slots[slotListIndex];
                }
            }
            this.slotID = slotID;
            CK_SLOT_INFO slotInfo = this.p11.C_GetSlotInfo(slotID);
            this.removable = (slotInfo.flags & 2L) != 0L;
            this.initToken(slotInfo);
            if (nssModule != null) {
                nssModule.setProvider(this);
            }
        }
        catch (Exception e) {
            if (this.config.getHandleStartupErrors() == 2) {
                throw new UnsupportedOperationException("Initialization failed", e);
            }
            throw new ProviderException("Initialization failed", e);
        }
    }

    private static String toString(long[] longs) {
        if (longs.length == 0) {
            return "(none)";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(longs[0]);
        for (int i = 1; i < longs.length; ++i) {
            sb.append(", ");
            sb.append(longs[i]);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    private static int[] m(long m1) {
        return new int[]{(int)m1};
    }

    private static int[] m(long m1, long m2) {
        return new int[]{(int)m1, (int)m2};
    }

    private static int[] m(long m1, long m2, long m3) {
        return new int[]{(int)m1, (int)m2, (int)m3};
    }

    private static int[] m(long m1, long m2, long m3, long m4) {
        return new int[]{(int)m1, (int)m2, (int)m3, (int)m4};
    }

    private static void d(String type, String algorithm, String className, int[] m) {
        SunPKCS11.register(new Descriptor(type, algorithm, className, null, m));
    }

    private static void d(String type, String algorithm, String className, List<String> aliases, int[] m) {
        SunPKCS11.register(new Descriptor(type, algorithm, className, aliases, m));
    }

    private static void d(String type, String algorithm, String className, int[] m, int[] requiredMechs) {
        SunPKCS11.register(new Descriptor(type, algorithm, className, null, m, requiredMechs));
    }

    private static void dA(String type, String algorithm, String className, int[] m) {
        SunPKCS11.register(new Descriptor(type, algorithm, className, SecurityProviderConstants.getAliases(algorithm), m));
    }

    private static void dA(String type, String algorithm, String className, int[] m, int[] requiredMechs) {
        SunPKCS11.register(new Descriptor(type, algorithm, className, SecurityProviderConstants.getAliases(algorithm), m, requiredMechs));
    }

    private static void register(Descriptor d) {
        for (int i = 0; i < d.mechanisms.length; ++i) {
            int m = d.mechanisms[i];
            Integer key = m;
            List list = descriptors.computeIfAbsent(key, (? super K k) -> new ArrayList());
            list.add(d);
        }
    }

    private void createPoller() {
        if (this.poller != null) {
            return;
        }
        this.poller = new TokenPoller(this);
        Thread t = InnocuousThread.newSystemThread("Poller-" + this.getName(), this.poller, 1);
        assert (t.getContextClassLoader() == null);
        t.setDaemon(true);
        t.start();
    }

    private void destroyPoller() {
        if (this.poller != null) {
            this.poller.disable();
            this.poller = null;
        }
    }

    private boolean hasValidToken() {
        Token token = this.token;
        return token != null && token.isValid();
    }

    private void createCleaner() {
        cleaner = new NativeResourceCleaner();
        Thread t = InnocuousThread.newSystemThread("Cleanup-SunPKCS11", cleaner, 1);
        assert (t.getContextClassLoader() == null);
        t.setDaemon(true);
        t.start();
    }

    synchronized void uninitToken(Token token) {
        if (this.token != token) {
            return;
        }
        this.destroyPoller();
        this.token = null;
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                SunPKCS11.this.clear();
                return null;
            }
        });
        if (this.removable && !this.config.getDestroyTokenAfterLogout()) {
            this.createPoller();
        }
    }

    private void initToken(CK_SLOT_INFO slotInfo) throws PKCS11Exception {
        if (slotInfo == null) {
            slotInfo = this.p11.C_GetSlotInfo(this.slotID);
        }
        if (this.removable && (slotInfo.flags & 1L) == 0L) {
            this.createPoller();
            return;
        }
        this.destroyPoller();
        boolean showInfo = this.config.getShowInfo();
        if (showInfo) {
            System.out.println("Slot info for slot " + this.slotID + ":");
            System.out.println(slotInfo);
        }
        final Token token = new Token(this);
        if (showInfo) {
            System.out.println("Token info for token in slot " + this.slotID + ":");
            System.out.println(token.tokenInfo);
        }
        Set<Object> brokenMechanisms = Set.of();
        if (P11Util.isNSS(token)) {
            CK_VERSION nssVersion = slotInfo.hardwareVersion;
            if (nssVersion.major < 3 || nssVersion.major == 3 && nssVersion.minor < 65) {
                brokenMechanisms = Set.of(Long.valueOf(13L), Long.valueOf(14L), Long.valueOf(71L), Long.valueOf(67L), Long.valueOf(68L), Long.valueOf(69L), Long.valueOf(103L), Long.valueOf(99L), Long.valueOf(100L), Long.valueOf(101L));
            }
        }
        long[] supportedMechanisms = this.p11.C_GetMechanismList(this.slotID);
        Set supportedMechSet = Arrays.stream(supportedMechanisms).boxed().collect(Collectors.toCollection(HashSet::new));
        final HashMap<Descriptor, Integer> supportedAlgs = new HashMap<Descriptor, Integer>();
        for (long longMech : supportedMechanisms) {
            CK_MECHANISM_INFO mechInfo = token.getMechanismInfo(longMech);
            if (showInfo) {
                System.out.println("Mechanism " + Functions.getMechanismName(longMech) + ":");
                System.out.println(mechInfo == null ? "  info n/a" : mechInfo);
            }
            if (!this.config.isEnabled(longMech)) {
                if (!showInfo) continue;
                System.out.println("DISABLED in configuration");
                continue;
            }
            if (longMech >>> 32 != 0L) {
                if (!showInfo) continue;
                System.out.println("DISABLED due to unknown mech value");
                continue;
            }
            int mech = (int)longMech;
            Integer integerMech = mech;
            List<Descriptor> ds = descriptors.get(integerMech);
            if (ds == null) continue;
            boolean allowLegacy = this.config.getAllowLegacy();
            block1: for (Descriptor d : ds) {
                Integer oldMech = (Integer)supportedAlgs.get(d);
                if (oldMech == null) {
                    if (d.requiredMechs != null) {
                        for (int reqMech : d.requiredMechs) {
                            long longReqMech = (long)reqMech & 0xFFFFFFFFL;
                            if (this.config.isEnabled(longReqMech) && supportedMechSet.contains(longReqMech) && !brokenMechanisms.contains(longReqMech)) continue;
                            if (!showInfo) continue block1;
                            System.out.println("DISABLED " + d.type + " " + d.algorithm + " due to no support for req'd mech " + Functions.getMechanismName(longReqMech));
                            continue block1;
                        }
                    }
                    if (!allowLegacy && mechInfo != null && (d.type == CIP && (mechInfo.flags & 0x100L) == 0L || d.type == SIG && (mechInfo.flags & 0x800L) == 0L)) {
                        if (!showInfo) continue;
                        System.out.println("DISABLED " + d.type + " " + d.algorithm + " due to partial support");
                        continue;
                    }
                    supportedAlgs.put(d, integerMech);
                    continue;
                }
                int intOldMech = oldMech;
                for (int j = 0; j < d.mechanisms.length; ++j) {
                    int nextMech = d.mechanisms[j];
                    if (mech == nextMech) {
                        supportedAlgs.put(d, integerMech);
                        continue block1;
                    }
                    if (intOldMech == nextMech) continue block1;
                }
            }
        }
        Object dummy = AccessController.doPrivileged(new PrivilegedAction<Object>(){
            final /* synthetic */ SunPKCS11 this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public Object run() {
                for (Map.Entry entry : supportedAlgs.entrySet()) {
                    Descriptor d = (Descriptor)entry.getKey();
                    int mechanism = (Integer)entry.getValue();
                    P11Service s = d.service(token, mechanism);
                    this.this$0.putService(s);
                }
                if ((token.tokenInfo.flags & 1L) != 0L && this.this$0.config.isEnabled(2147483424L) && !token.sessionManager.lowMaxSessions()) {
                    this.this$0.putService(new P11Service(token, SunPKCS11.SR, "PKCS11", "sun.security.pkcs11.P11SecureRandom", null, 2147483424L));
                }
                if (this.this$0.config.isEnabled(2147483425L)) {
                    this.this$0.putService(new P11Service(token, SunPKCS11.KS, "PKCS11", "sun.security.pkcs11.P11KeyStore", List.of("PKCS11-" + this.this$0.config.getName()), 2147483425L));
                }
                return null;
            }
        });
        this.token = token;
        if (cleaner == null) {
            this.createCleaner();
        }
    }

    @Override
    public void login(Subject subject, CallbackHandler handler) throws LoginException {
        if (!this.isConfigured()) {
            throw new IllegalStateException("Configuration is required");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (debug != null) {
                debug.println("checking login permission");
            }
            sm.checkPermission(new SecurityPermission("authProvider." + this.getName()));
        }
        if (!this.hasValidToken()) {
            throw new LoginException("No token present");
        }
        if ((this.token.tokenInfo.flags & 4L) == 0L) {
            if (debug != null) {
                debug.println("login operation not required for token - ignoring login request");
            }
            return;
        }
        try {
            if (this.token.isLoggedInNow(null)) {
                if (debug != null) {
                    debug.println("user already logged in");
                }
                return;
            }
        }
        catch (PKCS11Exception pKCS11Exception) {
            // empty catch block
        }
        char[] pin = null;
        if ((this.token.tokenInfo.flags & 0x100L) == 0L) {
            CallbackHandler myHandler = this.getCallbackHandler(handler);
            if (myHandler == null) {
                throw new LoginException("no password provided, and no callback handler available for retrieving password");
            }
            MessageFormat form = new MessageFormat(ResourcesMgr.getString("PKCS11.Token.providerName.Password."));
            Object[] source = new Object[]{this.getName()};
            PasswordCallback pcall = new PasswordCallback(form.format(source), false);
            Callback[] callbacks = new Callback[]{pcall};
            try {
                myHandler.handle(callbacks);
            }
            catch (Exception e) {
                LoginException le = new LoginException("Unable to perform password callback");
                le.initCause(e);
                throw le;
            }
            pin = pcall.getPassword();
            pcall.clearPassword();
            if (pin == null && debug != null) {
                debug.println("caller passed NULL pin");
            }
        }
        Session session = null;
        try {
            session = this.token.getOpSession();
            this.p11.C_Login(session.id(), 1L, pin);
            if (debug != null) {
                debug.println("login succeeded");
            }
        }
        catch (PKCS11Exception pe) {
            if (pe.match(PKCS11Exception.RV.CKR_USER_ALREADY_LOGGED_IN)) {
                if (debug != null) {
                    debug.println("user already logged in");
                }
                return;
            }
            if (pe.match(PKCS11Exception.RV.CKR_PIN_INCORRECT)) {
                FailedLoginException fle = new FailedLoginException();
                fle.initCause(pe);
                throw fle;
            }
            LoginException le = new LoginException();
            le.initCause(pe);
            throw le;
        }
        finally {
            this.token.releaseSession(session);
            if (pin != null) {
                Arrays.fill(pin, ' ');
            }
        }
    }

    @Override
    public void logout() throws LoginException {
        if (!this.isConfigured()) {
            throw new IllegalStateException("Configuration is required");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission("authProvider." + this.getName()));
        }
        if (!this.hasValidToken()) {
            return;
        }
        if ((this.token.tokenInfo.flags & 4L) == 0L) {
            if (debug != null) {
                debug.println("logout operation not required for token - ignoring logout request");
            }
            return;
        }
        try {
            if (!this.token.isLoggedInNow(null)) {
                if (debug != null) {
                    debug.println("user not logged in");
                }
                if (this.config.getDestroyTokenAfterLogout()) {
                    this.token.destroy();
                }
                return;
            }
        }
        catch (PKCS11Exception pKCS11Exception) {
            // empty catch block
        }
        Session session = null;
        try {
            session = this.token.getOpSession();
            this.p11.C_Logout(session.id());
            if (debug != null) {
                debug.println("logout succeeded");
            }
        }
        catch (PKCS11Exception pe) {
            if (pe.match(PKCS11Exception.RV.CKR_USER_NOT_LOGGED_IN)) {
                if (debug != null) {
                    debug.println("user not logged in");
                }
                return;
            }
            LoginException le = new LoginException();
            le.initCause(pe);
            throw le;
        }
        finally {
            this.token.releaseSession(session);
            if (this.config.getDestroyTokenAfterLogout()) {
                this.token.destroy();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setCallbackHandler(CallbackHandler handler) {
        if (!this.isConfigured()) {
            throw new IllegalStateException("Configuration is required");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission("authProvider." + this.getName()));
        }
        Object object = this.LOCK_HANDLER;
        synchronized (object) {
            this.pHandler = handler;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private CallbackHandler getCallbackHandler(CallbackHandler handler) {
        if (handler != null) {
            return handler;
        }
        if (debug != null) {
            debug.println("getting provider callback handler");
        }
        Object object = this.LOCK_HANDLER;
        synchronized (object) {
            if (this.pHandler != null) {
                return this.pHandler;
            }
            try {
                CallbackHandler myHandler;
                if (debug != null) {
                    debug.println("getting default callback handler");
                }
                this.pHandler = myHandler = AccessController.doPrivileged(new PrivilegedExceptionAction<CallbackHandler>(this){

                    @Override
                    public CallbackHandler run() throws Exception {
                        String defaultHandler = Security.getProperty("auth.login.defaultCallbackHandler");
                        if (defaultHandler == null || defaultHandler.length() == 0) {
                            if (debug != null) {
                                debug.println("no default handler set");
                            }
                            return null;
                        }
                        Class<?> c = Class.forName(defaultHandler, true, Thread.currentThread().getContextClassLoader());
                        if (!CallbackHandler.class.isAssignableFrom(c)) {
                            if (debug != null) {
                                debug.println("default handler " + defaultHandler + " is not a CallbackHandler");
                            }
                            return null;
                        }
                        Object result = c.newInstance();
                        return (CallbackHandler)result;
                    }
                });
                return myHandler;
            }
            catch (PrivilegedActionException pae) {
                if (debug != null) {
                    debug.println("Unable to load default callback handler");
                    pae.printStackTrace();
                }
            }
        }
        return null;
    }

    private Object writeReplace() throws ObjectStreamException {
        return new SunPKCS11Rep(this);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("SunPKCS11 not directly deserializable");
    }

    static {
        descriptors = new HashMap<Integer, List<Descriptor>>();
        String P11Digest2 = "sun.security.pkcs11.P11Digest";
        String P11Mac2 = "sun.security.pkcs11.P11Mac";
        String P11KeyPairGenerator2 = "sun.security.pkcs11.P11KeyPairGenerator";
        String P11KeyGenerator2 = "sun.security.pkcs11.P11KeyGenerator";
        String P11RSAKeyFactory = "sun.security.pkcs11.P11RSAKeyFactory";
        String P11DSAKeyFactory = "sun.security.pkcs11.P11DSAKeyFactory";
        String P11DHKeyFactory = "sun.security.pkcs11.P11DHKeyFactory";
        String P11ECKeyFactory = "sun.security.pkcs11.P11ECKeyFactory";
        String P11KeyAgreement2 = "sun.security.pkcs11.P11KeyAgreement";
        String P11SecretKeyFactory2 = "sun.security.pkcs11.P11SecretKeyFactory";
        String P11Cipher2 = "sun.security.pkcs11.P11Cipher";
        String P11KeyWrapCipher2 = "sun.security.pkcs11.P11KeyWrapCipher";
        String P11RSACipher2 = "sun.security.pkcs11.P11RSACipher";
        String P11AEADCipher2 = "sun.security.pkcs11.P11AEADCipher";
        String P11PBECipher2 = "sun.security.pkcs11.P11PBECipher";
        String P11Signature2 = "sun.security.pkcs11.P11Signature";
        String P11PSSSignature2 = "sun.security.pkcs11.P11PSSSignature";
        SunPKCS11.d(MD, "MD2", P11Digest2, SunPKCS11.m(512L));
        SunPKCS11.d(MD, "MD5", P11Digest2, SunPKCS11.m(528L));
        SunPKCS11.dA(MD, "SHA-1", P11Digest2, SunPKCS11.m(544L));
        SunPKCS11.dA(MD, "SHA-224", P11Digest2, SunPKCS11.m(597L));
        SunPKCS11.dA(MD, "SHA-256", P11Digest2, SunPKCS11.m(592L));
        SunPKCS11.dA(MD, "SHA-384", P11Digest2, SunPKCS11.m(608L));
        SunPKCS11.dA(MD, "SHA-512", P11Digest2, SunPKCS11.m(624L));
        SunPKCS11.dA(MD, "SHA-512/224", P11Digest2, SunPKCS11.m(72L));
        SunPKCS11.dA(MD, "SHA-512/256", P11Digest2, SunPKCS11.m(76L));
        SunPKCS11.dA(MD, "SHA3-224", P11Digest2, SunPKCS11.m(693L));
        SunPKCS11.dA(MD, "SHA3-256", P11Digest2, SunPKCS11.m(688L));
        SunPKCS11.dA(MD, "SHA3-384", P11Digest2, SunPKCS11.m(704L));
        SunPKCS11.dA(MD, "SHA3-512", P11Digest2, SunPKCS11.m(720L));
        SunPKCS11.d(MAC, "HmacMD5", P11Mac2, SunPKCS11.m(529L));
        SunPKCS11.dA(MAC, "HmacSHA1", P11Mac2, SunPKCS11.m(545L));
        SunPKCS11.dA(MAC, "HmacSHA224", P11Mac2, SunPKCS11.m(598L));
        SunPKCS11.dA(MAC, "HmacSHA256", P11Mac2, SunPKCS11.m(593L));
        SunPKCS11.dA(MAC, "HmacSHA384", P11Mac2, SunPKCS11.m(609L));
        SunPKCS11.dA(MAC, "HmacSHA512", P11Mac2, SunPKCS11.m(625L));
        SunPKCS11.dA(MAC, "HmacSHA512/224", P11Mac2, SunPKCS11.m(73L));
        SunPKCS11.dA(MAC, "HmacSHA512/256", P11Mac2, SunPKCS11.m(77L));
        SunPKCS11.dA(MAC, "HmacSHA3-224", P11Mac2, SunPKCS11.m(694L));
        SunPKCS11.dA(MAC, "HmacSHA3-256", P11Mac2, SunPKCS11.m(689L));
        SunPKCS11.dA(MAC, "HmacSHA3-384", P11Mac2, SunPKCS11.m(705L));
        SunPKCS11.dA(MAC, "HmacSHA3-512", P11Mac2, SunPKCS11.m(721L));
        SunPKCS11.d(MAC, "SslMacMD5", P11Mac2, SunPKCS11.m(896L));
        SunPKCS11.d(MAC, "SslMacSHA1", P11Mac2, SunPKCS11.m(897L));
        SunPKCS11.d(MAC, "HmacPBESHA1", P11Mac2, SunPKCS11.m(545L), SunPKCS11.m(960L));
        SunPKCS11.d(MAC, "HmacPBESHA224", P11Mac2, SunPKCS11.m(598L), SunPKCS11.m(3461563245L));
        SunPKCS11.d(MAC, "HmacPBESHA256", P11Mac2, SunPKCS11.m(593L), SunPKCS11.m(3461563246L));
        SunPKCS11.d(MAC, "HmacPBESHA384", P11Mac2, SunPKCS11.m(609L), SunPKCS11.m(3461563247L));
        SunPKCS11.d(MAC, "HmacPBESHA512", P11Mac2, SunPKCS11.m(625L), SunPKCS11.m(3461563248L));
        SunPKCS11.d(KPG, "RSA", P11KeyPairGenerator2, SecurityProviderConstants.getAliases("PKCS1"), SunPKCS11.m(0L));
        List<String> dhAlias = List.of("DiffieHellman");
        SunPKCS11.dA(KPG, "DSA", P11KeyPairGenerator2, SunPKCS11.m(16L));
        SunPKCS11.d(KPG, "DH", P11KeyPairGenerator2, dhAlias, SunPKCS11.m(32L));
        SunPKCS11.d(KPG, "EC", P11KeyPairGenerator2, SunPKCS11.m(4160L));
        SunPKCS11.dA(KG, "ARCFOUR", P11KeyGenerator2, SunPKCS11.m(272L));
        SunPKCS11.d(KG, "DES", P11KeyGenerator2, SunPKCS11.m(288L));
        SunPKCS11.d(KG, "DESede", P11KeyGenerator2, SunPKCS11.m(305L, 304L));
        SunPKCS11.d(KG, "AES", P11KeyGenerator2, SunPKCS11.m(4224L));
        SunPKCS11.d(KG, "Blowfish", P11KeyGenerator2, SunPKCS11.m(4240L));
        SunPKCS11.d(KG, "ChaCha20", P11KeyGenerator2, SunPKCS11.m(4645L));
        SunPKCS11.d(KG, "HmacMD5", P11KeyGenerator2, SunPKCS11.m(848L));
        SunPKCS11.dA(KG, "HmacSHA1", P11KeyGenerator2, SunPKCS11.m(16387L, 848L));
        SunPKCS11.dA(KG, "HmacSHA224", P11KeyGenerator2, SunPKCS11.m(16388L, 848L));
        SunPKCS11.dA(KG, "HmacSHA256", P11KeyGenerator2, SunPKCS11.m(16389L, 848L));
        SunPKCS11.dA(KG, "HmacSHA384", P11KeyGenerator2, SunPKCS11.m(16390L, 848L));
        SunPKCS11.dA(KG, "HmacSHA512", P11KeyGenerator2, SunPKCS11.m(16391L, 848L));
        SunPKCS11.dA(KG, "HmacSHA512/224", P11KeyGenerator2, SunPKCS11.m(16392L, 848L));
        SunPKCS11.dA(KG, "HmacSHA512/256", P11KeyGenerator2, SunPKCS11.m(16393L, 848L));
        SunPKCS11.dA(KG, "HmacSHA3-224", P11KeyGenerator2, SunPKCS11.m(696L, 848L));
        SunPKCS11.dA(KG, "HmacSHA3-256", P11KeyGenerator2, SunPKCS11.m(691L, 848L));
        SunPKCS11.dA(KG, "HmacSHA3-384", P11KeyGenerator2, SunPKCS11.m(707L, 848L));
        SunPKCS11.dA(KG, "HmacSHA3-512", P11KeyGenerator2, SunPKCS11.m(723L, 848L));
        SunPKCS11.d(KF, "RSA", P11RSAKeyFactory, SecurityProviderConstants.getAliases("PKCS1"), SunPKCS11.m(0L, 1L, 3L));
        SunPKCS11.dA(KF, "DSA", P11DSAKeyFactory, SunPKCS11.m(16L, 17L, 18L));
        SunPKCS11.d(KF, "DH", P11DHKeyFactory, dhAlias, SunPKCS11.m(32L, 33L));
        SunPKCS11.d(KF, "EC", P11ECKeyFactory, SunPKCS11.m(4160L, 4176L, 4161L, 4162L));
        SunPKCS11.dA(AGP, "EC", "sun.security.util.ECParameters", SunPKCS11.m(4160L, 4176L, 4161L, 4162L));
        SunPKCS11.d(AGP, "GCM", "sun.security.util.GCMParameters", SunPKCS11.m(4231L));
        SunPKCS11.dA(AGP, "ChaCha20-Poly1305", "com.sun.crypto.provider.ChaCha20Poly1305Parameters", SunPKCS11.m(16417L));
        SunPKCS11.d(KA, "DH", P11KeyAgreement2, dhAlias, SunPKCS11.m(33L));
        SunPKCS11.d(KA, "ECDH", "sun.security.pkcs11.P11ECDHKeyAgreement", SunPKCS11.m(4176L));
        SunPKCS11.dA(SKF, "ARCFOUR", P11SecretKeyFactory2, SunPKCS11.m(273L));
        SunPKCS11.d(SKF, "DES", P11SecretKeyFactory2, SunPKCS11.m(290L));
        SunPKCS11.d(SKF, "DESede", P11SecretKeyFactory2, SunPKCS11.m(307L));
        SunPKCS11.dA(SKF, "AES", P11SecretKeyFactory2, SunPKCS11.m(4226L));
        SunPKCS11.d(SKF, "Blowfish", P11SecretKeyFactory2, SunPKCS11.m(4241L));
        SunPKCS11.d(SKF, "ChaCha20", P11SecretKeyFactory2, SunPKCS11.m(16417L));
        SunPKCS11.d(SKF, "PBEWithHmacSHA1AndAES_128", P11SecretKeyFactory2, SunPKCS11.m(944L), SunPKCS11.m(545L));
        SunPKCS11.d(SKF, "PBEWithHmacSHA224AndAES_128", P11SecretKeyFactory2, SunPKCS11.m(944L), SunPKCS11.m(598L));
        SunPKCS11.d(SKF, "PBEWithHmacSHA256AndAES_128", P11SecretKeyFactory2, SunPKCS11.m(944L), SunPKCS11.m(593L));
        SunPKCS11.d(SKF, "PBEWithHmacSHA384AndAES_128", P11SecretKeyFactory2, SunPKCS11.m(944L), SunPKCS11.m(609L));
        SunPKCS11.d(SKF, "PBEWithHmacSHA512AndAES_128", P11SecretKeyFactory2, SunPKCS11.m(944L), SunPKCS11.m(625L));
        SunPKCS11.d(SKF, "PBEWithHmacSHA1AndAES_256", P11SecretKeyFactory2, SunPKCS11.m(944L), SunPKCS11.m(545L));
        SunPKCS11.d(SKF, "PBEWithHmacSHA224AndAES_256", P11SecretKeyFactory2, SunPKCS11.m(944L), SunPKCS11.m(598L));
        SunPKCS11.d(SKF, "PBEWithHmacSHA256AndAES_256", P11SecretKeyFactory2, SunPKCS11.m(944L), SunPKCS11.m(593L));
        SunPKCS11.d(SKF, "PBEWithHmacSHA384AndAES_256", P11SecretKeyFactory2, SunPKCS11.m(944L), SunPKCS11.m(609L));
        SunPKCS11.d(SKF, "PBEWithHmacSHA512AndAES_256", P11SecretKeyFactory2, SunPKCS11.m(944L), SunPKCS11.m(625L));
        SunPKCS11.d(SKF, "HmacPBESHA1", P11SecretKeyFactory2, SunPKCS11.m(960L));
        SunPKCS11.d(SKF, "HmacPBESHA224", P11SecretKeyFactory2, SunPKCS11.m(3461563245L));
        SunPKCS11.d(SKF, "HmacPBESHA256", P11SecretKeyFactory2, SunPKCS11.m(3461563246L));
        SunPKCS11.d(SKF, "HmacPBESHA384", P11SecretKeyFactory2, SunPKCS11.m(3461563247L));
        SunPKCS11.d(SKF, "HmacPBESHA512", P11SecretKeyFactory2, SunPKCS11.m(3461563248L));
        SunPKCS11.dA(SKF, "PBKDF2WithHmacSHA1", P11SecretKeyFactory2, SunPKCS11.m(944L), SunPKCS11.m(545L));
        SunPKCS11.d(SKF, "PBKDF2WithHmacSHA224", P11SecretKeyFactory2, SunPKCS11.m(944L), SunPKCS11.m(598L));
        SunPKCS11.d(SKF, "PBKDF2WithHmacSHA256", P11SecretKeyFactory2, SunPKCS11.m(944L), SunPKCS11.m(593L));
        SunPKCS11.d(SKF, "PBKDF2WithHmacSHA384", P11SecretKeyFactory2, SunPKCS11.m(944L), SunPKCS11.m(609L));
        SunPKCS11.d(SKF, "PBKDF2WithHmacSHA512", P11SecretKeyFactory2, SunPKCS11.m(944L), SunPKCS11.m(625L));
        SunPKCS11.dA(CIP, "ARCFOUR", P11Cipher2, SunPKCS11.m(273L));
        SunPKCS11.d(CIP, "DES/CBC/NoPadding", P11Cipher2, SunPKCS11.m(290L));
        SunPKCS11.d(CIP, "DES/CBC/PKCS5Padding", P11Cipher2, SunPKCS11.m(293L, 290L));
        SunPKCS11.d(CIP, "DES/ECB/NoPadding", P11Cipher2, SunPKCS11.m(289L));
        SunPKCS11.d(CIP, "DES/ECB/PKCS5Padding", P11Cipher2, List.of("DES"), SunPKCS11.m(289L));
        SunPKCS11.d(CIP, "DESede/CBC/NoPadding", P11Cipher2, SunPKCS11.m(307L));
        SunPKCS11.d(CIP, "DESede/CBC/PKCS5Padding", P11Cipher2, SunPKCS11.m(310L, 307L));
        SunPKCS11.d(CIP, "DESede/ECB/NoPadding", P11Cipher2, SunPKCS11.m(306L));
        SunPKCS11.d(CIP, "DESede/ECB/PKCS5Padding", P11Cipher2, List.of("DESede"), SunPKCS11.m(306L));
        SunPKCS11.d(CIP, "AES/CBC/NoPadding", P11Cipher2, SunPKCS11.m(4226L));
        SunPKCS11.dA(CIP, "AES_128/CBC/NoPadding", P11Cipher2, SunPKCS11.m(4226L));
        SunPKCS11.dA(CIP, "AES_192/CBC/NoPadding", P11Cipher2, SunPKCS11.m(4226L));
        SunPKCS11.dA(CIP, "AES_256/CBC/NoPadding", P11Cipher2, SunPKCS11.m(4226L));
        SunPKCS11.d(CIP, "AES/CBC/PKCS5Padding", P11Cipher2, SunPKCS11.m(4229L, 4226L));
        SunPKCS11.d(CIP, "AES/ECB/NoPadding", P11Cipher2, SunPKCS11.m(4225L));
        SunPKCS11.dA(CIP, "AES_128/ECB/NoPadding", P11Cipher2, SunPKCS11.m(4225L));
        SunPKCS11.dA(CIP, "AES_192/ECB/NoPadding", P11Cipher2, SunPKCS11.m(4225L));
        SunPKCS11.dA(CIP, "AES_256/ECB/NoPadding", P11Cipher2, SunPKCS11.m(4225L));
        SunPKCS11.d(CIP, "AES/ECB/PKCS5Padding", P11Cipher2, List.of("AES"), SunPKCS11.m(4225L));
        SunPKCS11.d(CIP, "AES/CTR/NoPadding", P11Cipher2, SunPKCS11.m(4230L));
        SunPKCS11.dA(CIP, "AES/KW/NoPadding", P11KeyWrapCipher2, SunPKCS11.m(8457L));
        SunPKCS11.dA(CIP, "AES_128/KW/NoPadding", P11KeyWrapCipher2, SunPKCS11.m(8457L));
        SunPKCS11.dA(CIP, "AES_192/KW/NoPadding", P11KeyWrapCipher2, SunPKCS11.m(8457L));
        SunPKCS11.dA(CIP, "AES_256/KW/NoPadding", P11KeyWrapCipher2, SunPKCS11.m(8457L));
        SunPKCS11.d(CIP, "AES/KW/PKCS5Padding", P11KeyWrapCipher2, SunPKCS11.m(8458L));
        SunPKCS11.d(CIP, "AES_128/KW/PKCS5Padding", P11KeyWrapCipher2, SunPKCS11.m(8458L));
        SunPKCS11.d(CIP, "AES_192/KW/PKCS5Padding", P11KeyWrapCipher2, SunPKCS11.m(8458L));
        SunPKCS11.d(CIP, "AES_256/KW/PKCS5Padding", P11KeyWrapCipher2, SunPKCS11.m(8458L));
        SunPKCS11.dA(CIP, "AES/KWP/NoPadding", P11KeyWrapCipher2, SunPKCS11.m(8459L));
        SunPKCS11.dA(CIP, "AES_128/KWP/NoPadding", P11KeyWrapCipher2, SunPKCS11.m(8459L));
        SunPKCS11.dA(CIP, "AES_192/KWP/NoPadding", P11KeyWrapCipher2, SunPKCS11.m(8459L));
        SunPKCS11.dA(CIP, "AES_256/KWP/NoPadding", P11KeyWrapCipher2, SunPKCS11.m(8459L));
        SunPKCS11.d(CIP, "AES/GCM/NoPadding", P11AEADCipher2, SunPKCS11.m(4231L));
        SunPKCS11.dA(CIP, "AES_128/GCM/NoPadding", P11AEADCipher2, SunPKCS11.m(4231L));
        SunPKCS11.dA(CIP, "AES_192/GCM/NoPadding", P11AEADCipher2, SunPKCS11.m(4231L));
        SunPKCS11.dA(CIP, "AES_256/GCM/NoPadding", P11AEADCipher2, SunPKCS11.m(4231L));
        SunPKCS11.d(CIP, "Blowfish/CBC/NoPadding", P11Cipher2, SunPKCS11.m(4241L));
        SunPKCS11.d(CIP, "Blowfish/CBC/PKCS5Padding", P11Cipher2, SunPKCS11.m(4241L));
        SunPKCS11.dA(CIP, "ChaCha20-Poly1305", P11AEADCipher2, SunPKCS11.m(16417L));
        SunPKCS11.d(CIP, "RSA/ECB/PKCS1Padding", P11RSACipher2, List.of("RSA"), SunPKCS11.m(1L));
        SunPKCS11.d(CIP, "RSA/ECB/NoPadding", P11RSACipher2, SunPKCS11.m(3L));
        SunPKCS11.d(CIP, "PBEWithHmacSHA1AndAES_128", P11PBECipher2, SunPKCS11.m(4229L, 4226L), SunPKCS11.m(944L, 545L));
        SunPKCS11.d(CIP, "PBEWithHmacSHA224AndAES_128", P11PBECipher2, SunPKCS11.m(4229L, 4226L), SunPKCS11.m(944L, 598L));
        SunPKCS11.d(CIP, "PBEWithHmacSHA256AndAES_128", P11PBECipher2, SunPKCS11.m(4229L, 4226L), SunPKCS11.m(944L, 593L));
        SunPKCS11.d(CIP, "PBEWithHmacSHA384AndAES_128", P11PBECipher2, SunPKCS11.m(4229L, 4226L), SunPKCS11.m(944L, 609L));
        SunPKCS11.d(CIP, "PBEWithHmacSHA512AndAES_128", P11PBECipher2, SunPKCS11.m(4229L, 4226L), SunPKCS11.m(944L, 625L));
        SunPKCS11.d(CIP, "PBEWithHmacSHA1AndAES_256", P11PBECipher2, SunPKCS11.m(4229L, 4226L), SunPKCS11.m(944L, 545L));
        SunPKCS11.d(CIP, "PBEWithHmacSHA224AndAES_256", P11PBECipher2, SunPKCS11.m(4229L, 4226L), SunPKCS11.m(944L, 598L));
        SunPKCS11.d(CIP, "PBEWithHmacSHA256AndAES_256", P11PBECipher2, SunPKCS11.m(4229L, 4226L), SunPKCS11.m(944L, 593L));
        SunPKCS11.d(CIP, "PBEWithHmacSHA384AndAES_256", P11PBECipher2, SunPKCS11.m(4229L, 4226L), SunPKCS11.m(944L, 609L));
        SunPKCS11.d(CIP, "PBEWithHmacSHA512AndAES_256", P11PBECipher2, SunPKCS11.m(4229L, 4226L), SunPKCS11.m(944L, 625L));
        SunPKCS11.d(SIG, "RawDSA", P11Signature2, List.of("NONEwithDSA"), SunPKCS11.m(17L));
        SunPKCS11.dA(SIG, "SHA1withDSA", P11Signature2, SunPKCS11.m(18L, 17L));
        SunPKCS11.dA(SIG, "SHA224withDSA", P11Signature2, SunPKCS11.m(19L));
        SunPKCS11.dA(SIG, "SHA256withDSA", P11Signature2, SunPKCS11.m(20L));
        SunPKCS11.dA(SIG, "SHA384withDSA", P11Signature2, SunPKCS11.m(21L));
        SunPKCS11.dA(SIG, "SHA512withDSA", P11Signature2, SunPKCS11.m(22L));
        SunPKCS11.dA(SIG, "SHA3-224withDSA", P11Signature2, SunPKCS11.m(24L));
        SunPKCS11.dA(SIG, "SHA3-256withDSA", P11Signature2, SunPKCS11.m(25L));
        SunPKCS11.dA(SIG, "SHA3-384withDSA", P11Signature2, SunPKCS11.m(26L));
        SunPKCS11.dA(SIG, "SHA3-512withDSA", P11Signature2, SunPKCS11.m(27L));
        SunPKCS11.d(SIG, "RawDSAinP1363Format", P11Signature2, List.of("NONEwithDSAinP1363Format"), SunPKCS11.m(17L));
        SunPKCS11.d(SIG, "DSAinP1363Format", P11Signature2, List.of("SHA1withDSAinP1363Format"), SunPKCS11.m(18L, 17L));
        SunPKCS11.d(SIG, "SHA224withDSAinP1363Format", P11Signature2, SunPKCS11.m(19L));
        SunPKCS11.d(SIG, "SHA256withDSAinP1363Format", P11Signature2, SunPKCS11.m(20L));
        SunPKCS11.d(SIG, "SHA384withDSAinP1363Format", P11Signature2, SunPKCS11.m(21L));
        SunPKCS11.d(SIG, "SHA512withDSAinP1363Format", P11Signature2, SunPKCS11.m(22L));
        SunPKCS11.d(SIG, "SHA3-224withDSAinP1363Format", P11Signature2, SunPKCS11.m(24L));
        SunPKCS11.d(SIG, "SHA3-256withDSAinP1363Format", P11Signature2, SunPKCS11.m(25L));
        SunPKCS11.d(SIG, "SHA3-384withDSAinP1363Format", P11Signature2, SunPKCS11.m(26L));
        SunPKCS11.d(SIG, "SHA3-512withDSAinP1363Format", P11Signature2, SunPKCS11.m(27L));
        SunPKCS11.d(SIG, "NONEwithECDSA", P11Signature2, SunPKCS11.m(4161L));
        SunPKCS11.dA(SIG, "SHA1withECDSA", P11Signature2, SunPKCS11.m(4162L, 4161L));
        SunPKCS11.dA(SIG, "SHA224withECDSA", P11Signature2, SunPKCS11.m(4163L, 4161L));
        SunPKCS11.dA(SIG, "SHA256withECDSA", P11Signature2, SunPKCS11.m(4164L, 4161L));
        SunPKCS11.dA(SIG, "SHA384withECDSA", P11Signature2, SunPKCS11.m(4165L, 4161L));
        SunPKCS11.dA(SIG, "SHA512withECDSA", P11Signature2, SunPKCS11.m(4166L, 4161L));
        SunPKCS11.dA(SIG, "SHA3-224withECDSA", P11Signature2, SunPKCS11.m(4167L, 4161L));
        SunPKCS11.dA(SIG, "SHA3-256withECDSA", P11Signature2, SunPKCS11.m(4168L, 4161L));
        SunPKCS11.dA(SIG, "SHA3-384withECDSA", P11Signature2, SunPKCS11.m(4169L, 4161L));
        SunPKCS11.dA(SIG, "SHA3-512withECDSA", P11Signature2, SunPKCS11.m(4170L, 4161L));
        SunPKCS11.d(SIG, "NONEwithECDSAinP1363Format", P11Signature2, SunPKCS11.m(4161L));
        SunPKCS11.d(SIG, "SHA1withECDSAinP1363Format", P11Signature2, SunPKCS11.m(4162L, 4161L));
        SunPKCS11.d(SIG, "SHA224withECDSAinP1363Format", P11Signature2, SunPKCS11.m(4163L, 4161L));
        SunPKCS11.d(SIG, "SHA256withECDSAinP1363Format", P11Signature2, SunPKCS11.m(4164L, 4161L));
        SunPKCS11.d(SIG, "SHA384withECDSAinP1363Format", P11Signature2, SunPKCS11.m(4165L, 4161L));
        SunPKCS11.d(SIG, "SHA512withECDSAinP1363Format", P11Signature2, SunPKCS11.m(4166L, 4161L));
        SunPKCS11.d(SIG, "SHA3-224withECDSAinP1363Format", P11Signature2, SunPKCS11.m(4167L, 4161L));
        SunPKCS11.d(SIG, "SHA3-256withECDSAinP1363Format", P11Signature2, SunPKCS11.m(4168L, 4161L));
        SunPKCS11.d(SIG, "SHA3-384withECDSAinP1363Format", P11Signature2, SunPKCS11.m(4169L, 4161L));
        SunPKCS11.d(SIG, "SHA3-512withECDSAinP1363Format", P11Signature2, SunPKCS11.m(4170L, 4161L));
        SunPKCS11.dA(SIG, "MD2withRSA", P11Signature2, SunPKCS11.m(4L, 1L, 3L));
        SunPKCS11.dA(SIG, "MD5withRSA", P11Signature2, SunPKCS11.m(5L, 1L, 3L));
        SunPKCS11.dA(SIG, "SHA1withRSA", P11Signature2, SunPKCS11.m(6L, 1L, 3L));
        SunPKCS11.dA(SIG, "SHA224withRSA", P11Signature2, SunPKCS11.m(70L, 1L, 3L));
        SunPKCS11.dA(SIG, "SHA256withRSA", P11Signature2, SunPKCS11.m(64L, 1L, 3L));
        SunPKCS11.dA(SIG, "SHA384withRSA", P11Signature2, SunPKCS11.m(65L, 1L, 3L));
        SunPKCS11.dA(SIG, "SHA512withRSA", P11Signature2, SunPKCS11.m(66L, 1L, 3L));
        SunPKCS11.dA(SIG, "SHA3-224withRSA", P11Signature2, SunPKCS11.m(102L, 1L, 3L));
        SunPKCS11.dA(SIG, "SHA3-256withRSA", P11Signature2, SunPKCS11.m(96L, 1L, 3L));
        SunPKCS11.dA(SIG, "SHA3-384withRSA", P11Signature2, SunPKCS11.m(97L, 1L, 3L));
        SunPKCS11.dA(SIG, "SHA3-512withRSA", P11Signature2, SunPKCS11.m(98L, 1L, 3L));
        SunPKCS11.dA(SIG, "RSASSA-PSS", P11PSSSignature2, SunPKCS11.m(13L));
        SunPKCS11.d(SIG, "SHA1withRSASSA-PSS", P11PSSSignature2, SunPKCS11.m(14L));
        SunPKCS11.d(SIG, "SHA224withRSASSA-PSS", P11PSSSignature2, SunPKCS11.m(71L));
        SunPKCS11.d(SIG, "SHA256withRSASSA-PSS", P11PSSSignature2, SunPKCS11.m(67L));
        SunPKCS11.d(SIG, "SHA384withRSASSA-PSS", P11PSSSignature2, SunPKCS11.m(68L));
        SunPKCS11.d(SIG, "SHA512withRSASSA-PSS", P11PSSSignature2, SunPKCS11.m(69L));
        SunPKCS11.d(SIG, "SHA3-224withRSASSA-PSS", P11PSSSignature2, SunPKCS11.m(103L));
        SunPKCS11.d(SIG, "SHA3-256withRSASSA-PSS", P11PSSSignature2, SunPKCS11.m(99L));
        SunPKCS11.d(SIG, "SHA3-384withRSASSA-PSS", P11PSSSignature2, SunPKCS11.m(100L));
        SunPKCS11.d(SIG, "SHA3-512withRSASSA-PSS", P11PSSSignature2, SunPKCS11.m(101L));
        SunPKCS11.d(KG, "SunTlsRsaPremasterSecret", "sun.security.pkcs11.P11TlsRsaPremasterSecretGenerator", List.of("SunTls12RsaPremasterSecret"), SunPKCS11.m(880L, 884L));
        SunPKCS11.d(KG, "SunTlsMasterSecret", "sun.security.pkcs11.P11TlsMasterSecretGenerator", SunPKCS11.m(881L, 885L, 883L, 887L));
        SunPKCS11.d(KG, "SunTls12MasterSecret", "sun.security.pkcs11.P11TlsMasterSecretGenerator", SunPKCS11.m(992L, 994L));
        SunPKCS11.d(KG, "SunTlsKeyMaterial", "sun.security.pkcs11.P11TlsKeyMaterialGenerator", SunPKCS11.m(882L, 886L));
        SunPKCS11.d(KG, "SunTls12KeyMaterial", "sun.security.pkcs11.P11TlsKeyMaterialGenerator", SunPKCS11.m(993L));
        SunPKCS11.d(KG, "SunTlsPrf", "sun.security.pkcs11.P11TlsPrfGenerator", SunPKCS11.m(888L, 2147484531L));
        SunPKCS11.d(KG, "SunTls12Prf", "sun.security.pkcs11.P11TlsPrfGenerator", SunPKCS11.m(996L));
    }

    private static class TokenPoller
    implements Runnable {
        private final SunPKCS11 provider;
        private volatile boolean enabled;

        private TokenPoller(SunPKCS11 provider) {
            this.provider = provider;
            this.enabled = true;
        }

        @Override
        public void run() {
            int interval = this.provider.config.getInsertionCheckInterval();
            while (this.enabled) {
                try {
                    Thread.sleep(interval);
                }
                catch (InterruptedException e) {
                    break;
                }
                if (!this.enabled) break;
                try {
                    this.provider.initToken(null);
                }
                catch (PKCS11Exception pKCS11Exception) {}
            }
        }

        void disable() {
            this.enabled = false;
        }
    }

    private static final class Descriptor {
        final String type;
        final String algorithm;
        final String className;
        final List<String> aliases;
        final int[] mechanisms;
        final int[] requiredMechs;

        private Descriptor(String type, String algorithm, String className, List<String> aliases, int[] mechanisms) {
            this(type, algorithm, className, aliases, mechanisms, null);
        }

        private Descriptor(String type, String algorithm, String className, List<String> aliases, int[] mechanisms, int[] requiredMechs) {
            this.type = type;
            this.algorithm = algorithm;
            this.className = className;
            this.aliases = aliases;
            this.mechanisms = mechanisms;
            this.requiredMechs = requiredMechs;
        }

        private P11Service service(Token token, int mechanism) {
            return new P11Service(token, this.type, this.algorithm, this.className, this.aliases, mechanism);
        }

        public String toString() {
            return this.type + "." + this.algorithm;
        }
    }

    private class NativeResourceCleaner
    implements Runnable {
        private long sleepMillis;
        private int count;
        boolean keyRefFound;
        boolean sessRefFound;

        private NativeResourceCleaner() {
            this.sleepMillis = SunPKCS11.this.config.getResourceCleanerShortInterval();
            this.count = 0;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(this.sleepMillis);
                }
                catch (InterruptedException ie) {
                    break;
                }
                this.keyRefFound = P11Key.drainRefQueue();
                this.sessRefFound = Session.drainRefQueue();
                if (!this.keyRefFound && !this.sessRefFound) {
                    ++this.count;
                    if (this.count <= 100) continue;
                    this.sleepMillis = SunPKCS11.this.config.getResourceCleanerLongInterval();
                    continue;
                }
                this.count = 0;
                this.sleepMillis = SunPKCS11.this.config.getResourceCleanerShortInterval();
            }
        }
    }

    private static class SunPKCS11Rep
    implements Serializable {
        static final long serialVersionUID = -2896606995897745419L;
        private final String providerName;
        private final String configName;

        SunPKCS11Rep(SunPKCS11 provider) throws NotSerializableException {
            this.providerName = provider.getName();
            this.configName = provider.config.getFileName();
            if (Security.getProvider(this.providerName) != provider) {
                throw new NotSerializableException("Only SunPKCS11 providers installed in java.security.Security can be serialized");
            }
        }

        private Object readResolve() throws ObjectStreamException {
            SunPKCS11 p = (SunPKCS11)Security.getProvider(this.providerName);
            if (p == null || !p.config.getFileName().equals(this.configName)) {
                throw new NotSerializableException("Could not find " + this.providerName + " in installed providers");
            }
            return p;
        }
    }

    private static final class P11Service
    extends Provider.Service {
        private final Token token;
        private final long mechanism;

        P11Service(Token token, String type, String algorithm, String className, List<String> al, long mechanism) {
            super(token.provider, type, algorithm, className, al, type.equals(SunPKCS11.SR) ? Map.of("ThreadSafe", "true") : null);
            this.token = token;
            this.mechanism = mechanism & 0xFFFFFFFFL;
        }

        @Override
        public Object newInstance(Object param) throws NoSuchAlgorithmException {
            if (!this.token.isValid()) {
                throw new NoSuchAlgorithmException("Token has been removed");
            }
            try {
                return this.newInstance0(param);
            }
            catch (PKCS11Exception e) {
                throw new NoSuchAlgorithmException(e);
            }
        }

        public Object newInstance0(Object param) throws PKCS11Exception, NoSuchAlgorithmException {
            String algorithm = this.getAlgorithm();
            String type = this.getType();
            if (type == SunPKCS11.MD) {
                return new P11Digest(this.token, algorithm, this.mechanism);
            }
            if (type == SunPKCS11.CIP) {
                if (algorithm.startsWith("RSA")) {
                    return new P11RSACipher(this.token, algorithm, this.mechanism);
                }
                if (algorithm.endsWith("GCM/NoPadding") || algorithm.startsWith("ChaCha20-Poly1305")) {
                    return new P11AEADCipher(this.token, algorithm, this.mechanism);
                }
                if (algorithm.contains("/KW/") || algorithm.contains("/KWP/")) {
                    return new P11KeyWrapCipher(this.token, algorithm, this.mechanism);
                }
                if (algorithm.startsWith("PBE")) {
                    return new P11PBECipher(this.token, algorithm, this.mechanism);
                }
                return new P11Cipher(this.token, algorithm, this.mechanism);
            }
            if (type == SunPKCS11.SIG) {
                if (algorithm.contains("RSASSA-PSS")) {
                    return new P11PSSSignature(this.token, algorithm, this.mechanism);
                }
                return new P11Signature(this.token, algorithm, this.mechanism);
            }
            if (type == SunPKCS11.MAC) {
                return new P11Mac(this.token, algorithm, this.mechanism);
            }
            if (type == SunPKCS11.KPG) {
                return new P11KeyPairGenerator(this.token, algorithm, this.mechanism);
            }
            if (type == SunPKCS11.KA) {
                if (algorithm.equals("ECDH")) {
                    return new P11ECDHKeyAgreement(this.token, algorithm, this.mechanism);
                }
                return new P11KeyAgreement(this.token, algorithm, this.mechanism);
            }
            if (type == SunPKCS11.KF) {
                return this.token.getKeyFactory(algorithm);
            }
            if (type == SunPKCS11.SKF) {
                return new P11SecretKeyFactory(this.token, algorithm);
            }
            if (type == SunPKCS11.KG) {
                if (algorithm == "SunTlsRsaPremasterSecret") {
                    return new P11TlsRsaPremasterSecretGenerator(this.token, algorithm, this.mechanism);
                }
                if (algorithm == "SunTlsMasterSecret" || algorithm == "SunTls12MasterSecret") {
                    return new P11TlsMasterSecretGenerator(this.token, algorithm, this.mechanism);
                }
                if (algorithm == "SunTlsKeyMaterial" || algorithm == "SunTls12KeyMaterial") {
                    return new P11TlsKeyMaterialGenerator(this.token, algorithm, this.mechanism);
                }
                if (algorithm == "SunTlsPrf" || algorithm == "SunTls12Prf") {
                    return new P11TlsPrfGenerator(this.token, algorithm, this.mechanism);
                }
                return new P11KeyGenerator(this.token, algorithm, this.mechanism);
            }
            if (type == SunPKCS11.SR) {
                return this.token.getRandom();
            }
            if (type == SunPKCS11.KS) {
                return this.token.getKeyStore();
            }
            if (type == SunPKCS11.AGP) {
                if (algorithm == "EC") {
                    return new ECParameters();
                }
                if (algorithm == "GCM") {
                    return new GCMParameters();
                }
                if (algorithm == "ChaCha20-Poly1305") {
                    return new ChaCha20Poly1305Parameters();
                }
                throw new NoSuchAlgorithmException("Unsupported algorithm: " + algorithm);
            }
            throw new NoSuchAlgorithmException("Unknown type: " + type);
        }

        @Override
        public boolean supportsParameter(Object param) {
            if (param == null || !this.token.isValid()) {
                return false;
            }
            if (!(param instanceof Key)) {
                throw new InvalidParameterException("Parameter must be a Key");
            }
            Key key = (Key)param;
            String algorithm = this.getAlgorithm();
            String type = this.getType();
            String keyAlgorithm = key.getAlgorithm();
            if (type == SunPKCS11.CIP && algorithm.startsWith("RSA") || type == SunPKCS11.SIG && algorithm.contains("RSA")) {
                if (!keyAlgorithm.equals("RSA")) {
                    return false;
                }
                return this.isLocalKey(key) || key instanceof RSAPrivateKey || key instanceof RSAPublicKey;
            }
            if (type == SunPKCS11.KA && algorithm.equals("ECDH") || type == SunPKCS11.SIG && algorithm.contains("ECDSA")) {
                if (!keyAlgorithm.equals("EC")) {
                    return false;
                }
                return this.isLocalKey(key) || key instanceof ECPrivateKey || key instanceof ECPublicKey;
            }
            if (type == SunPKCS11.SIG && algorithm.contains("DSA") && !algorithm.contains("ECDSA")) {
                if (!keyAlgorithm.equals("DSA")) {
                    return false;
                }
                return this.isLocalKey(key) || key instanceof DSAPrivateKey || key instanceof DSAPublicKey;
            }
            if (type == SunPKCS11.CIP || type == SunPKCS11.MAC) {
                return this.isLocalKey(key) || "RAW".equals(key.getFormat());
            }
            if (type == SunPKCS11.KA) {
                if (!keyAlgorithm.equals("DH")) {
                    return false;
                }
                return this.isLocalKey(key) || key instanceof DHPrivateKey || key instanceof DHPublicKey;
            }
            throw new AssertionError((Object)("SunPKCS11 error: " + type + ", " + algorithm));
        }

        private boolean isLocalKey(Key key) {
            return key instanceof P11Key && ((P11Key)key).token == this.token;
        }

        @Override
        public String toString() {
            return super.toString() + " (" + Functions.getMechanismName(this.mechanism) + ")";
        }
    }
}

