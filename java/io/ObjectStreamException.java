/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.IOException;

public abstract class ObjectStreamException
extends IOException {
    private static final long serialVersionUID = 7260898174833392607L;

    protected ObjectStreamException(String message) {
        super(message);
    }

    protected ObjectStreamException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ObjectStreamException() {
    }

    protected ObjectStreamException(Throwable cause) {
        super(cause);
    }
}

