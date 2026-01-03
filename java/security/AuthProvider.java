/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.security.Provider;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

public abstract class AuthProvider
extends Provider {
    private static final long serialVersionUID = 4197859053084546461L;

    @Deprecated(since="9")
    protected AuthProvider(String name, double version, String info) {
        super(name, Double.toString(version), info);
    }

    protected AuthProvider(String name, String versionStr, String info) {
        super(name, versionStr, info);
    }

    public abstract void login(Subject var1, CallbackHandler var2) throws LoginException;

    public abstract void logout() throws LoginException;

    public abstract void setCallbackHandler(CallbackHandler var1);
}

