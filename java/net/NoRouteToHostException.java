/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.net.SocketException;

public class NoRouteToHostException
extends SocketException {
    private static final long serialVersionUID = -1897550894873493790L;

    public NoRouteToHostException(String msg) {
        super(msg);
    }

    public NoRouteToHostException() {
    }
}

