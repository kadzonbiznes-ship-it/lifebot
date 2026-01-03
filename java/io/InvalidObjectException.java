/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.ObjectStreamException;

public class InvalidObjectException
extends ObjectStreamException {
    private static final long serialVersionUID = 3233174318281839583L;

    public InvalidObjectException(String reason) {
        super(reason);
    }

    public InvalidObjectException(String reason, Throwable cause) {
        super(reason, cause);
    }
}

