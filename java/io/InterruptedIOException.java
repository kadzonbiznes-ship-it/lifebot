/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.IOException;

public class InterruptedIOException
extends IOException {
    private static final long serialVersionUID = 4020568460727500567L;
    public int bytesTransferred = 0;

    public InterruptedIOException() {
    }

    public InterruptedIOException(String s) {
        super(s);
    }
}

