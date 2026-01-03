/*
 * Decompiled with CFR 0.152.
 */
package javax.security.auth.login;

import javax.security.auth.login.LoginException;

public class FailedLoginException
extends LoginException {
    private static final long serialVersionUID = 802556922354616286L;

    public FailedLoginException() {
    }

    public FailedLoginException(String msg) {
        super(msg);
    }
}

