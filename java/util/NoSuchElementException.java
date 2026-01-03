/*
 * Decompiled with CFR 0.152.
 */
package java.util;

public class NoSuchElementException
extends RuntimeException {
    private static final long serialVersionUID = 6769829250639411880L;

    public NoSuchElementException() {
    }

    public NoSuchElementException(String s, Throwable cause) {
        super(s, cause);
    }

    public NoSuchElementException(Throwable cause) {
        super(cause);
    }

    public NoSuchElementException(String s) {
        super(s);
    }
}

