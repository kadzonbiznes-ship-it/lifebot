/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.security.GeneralSecurityException;

public class KeyException
extends GeneralSecurityException {
    private static final long serialVersionUID = -7483676942812432108L;

    public KeyException() {
    }

    public KeyException(String msg) {
        super(msg);
    }

    public KeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyException(Throwable cause) {
        super(cause);
    }
}

