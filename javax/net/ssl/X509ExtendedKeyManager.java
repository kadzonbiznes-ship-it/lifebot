/*
 * Decompiled with CFR 0.152.
 */
package javax.net.ssl;

import java.security.Principal;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509KeyManager;

public abstract class X509ExtendedKeyManager
implements X509KeyManager {
    protected X509ExtendedKeyManager() {
    }

    public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
        return null;
    }

    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        return null;
    }
}

