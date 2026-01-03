/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.crypto.JarVerifier
 */
package javax.crypto;

import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.cert.Certificate;
import java.util.Optional;
import java.util.jar.JarException;
import javax.crypto.CryptoPermissions;
import javax.crypto.JarVerifier;

final class ProviderVerifier {
    private final URL url;
    private final Provider provider;
    private final boolean savePerms;
    private JarVerifier jarVerifier;
    private static String javaSpecVersion;
    private static String javaHome;
    private static String javaVendor;
    private static String javaVersion;

    private static String getProperty(String property) {
        String value = System.getProperty(property);
        return value != null ? value : "";
    }

    ProviderVerifier(URL url, boolean savePerms) {
        this(url, null, savePerms);
    }

    ProviderVerifier(URL url, Provider provider, boolean savePerms) {
        this.url = url;
        this.provider = provider;
        this.savePerms = savePerms;
    }

    void verify() throws IOException {
        if (this.isTrustedModule()) {
            return;
        }
        if (this.url.getProtocol().equalsIgnoreCase("jrt")) {
            throw new JarException("Non-Oracle JCE providers may not be linked into the image,they must be provided as signed JAR files.");
        }
        this.jarVerifier = new JarVerifier(this.url, this.savePerms);
        this.jarVerifier.verify();
    }

    private boolean isTrustedModule() {
        ModuleReference mref;
        Optional<URI> location;
        String mn;
        if (this.provider == null) {
            return false;
        }
        Class<?> providerClass = this.provider.getClass();
        Module providerModule = providerClass.getModule();
        String providerName = this.provider.getName();
        String providerVersion = this.provider.getVersionStr();
        if (!providerModule.isNamed()) {
            return false;
        }
        if (!providerVersion.equals(javaSpecVersion)) {
            return false;
        }
        PrivilegedAction<Configuration> pa = () -> ModuleLayer.boot().configuration();
        Configuration cf = AccessController.doPrivileged(pa);
        ResolvedModule resovledModule = cf.findModule(mn = providerModule.getName()).orElse(null);
        if (resovledModule != null && (location = (mref = resovledModule.reference()).location()).isPresent()) {
            Path modulesDir;
            Path providerDir;
            URI uri = location.get();
            if (uri.getScheme().equalsIgnoreCase("jrt")) {
                return this.isCryptoProviderModule(providerName, mn);
            }
            if (uri.getScheme().equalsIgnoreCase("file") && (providerDir = Paths.get(uri)).startsWith(modulesDir = Paths.get(javaHome, "modules"))) {
                return this.isCryptoProviderModule(providerName, mn);
            }
        }
        return false;
    }

    private boolean isCryptoProviderModule(String providerName, String moduleName) {
        return providerName.equals("SunJCE") && moduleName.equals("java.base") || providerName.equals("SunEC") && moduleName.equals("jdk.crypto.ec") || providerName.equals("SunMSCAPI") && moduleName.equals("jdk.crypto.mscapi") || providerName.startsWith("SunPKCS11-") && moduleName.equals("jdk.crypto.cryptoki");
    }

    static void verifyPolicySigned(Certificate[] certs) throws Exception {
    }

    static boolean isTrustedCryptoProvider(Provider provider) {
        if (provider == null) {
            return false;
        }
        URL url = (URL)AccessController.doPrivileged(new /* Unavailable Anonymous Inner Class!! */);
        ProviderVerifier verifier = new ProviderVerifier(url, provider, false);
        return verifier.isTrustedModule();
    }

    CryptoPermissions getPermissions() {
        return this.jarVerifier.getPermissions();
    }

    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                javaSpecVersion = ProviderVerifier.getProperty("java.specification.version");
                javaHome = ProviderVerifier.getProperty("java.home");
                javaVendor = ProviderVerifier.getProperty("java.vendor");
                javaVersion = ProviderVerifier.getProperty("java.version");
                return null;
            }
        });
    }
}

