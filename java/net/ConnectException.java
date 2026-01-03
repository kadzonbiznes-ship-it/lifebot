/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.net.SocketException;

public class ConnectException
extends SocketException {
    private static final long serialVersionUID = 3831404271622369215L;

    public ConnectException(String msg) {
        super(msg);
    }

    public ConnectException() {
    }
}

