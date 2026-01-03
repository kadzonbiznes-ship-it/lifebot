/*
 * Decompiled with CFR 0.152.
 */
package java.security;

public class InvalidParameterException
extends IllegalArgumentException {
    private static final long serialVersionUID = -857968536935667808L;

    public InvalidParameterException() {
    }

    public InvalidParameterException(String msg) {
        super(msg);
    }

    public InvalidParameterException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public InvalidParameterException(Throwable cause) {
        super(cause);
    }
}

