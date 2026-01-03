/*
 * Decompiled with CFR 0.152.
 */
package javax.net.ssl;

import java.util.EventListener;
import javax.net.ssl.HandshakeCompletedEvent;

public interface HandshakeCompletedListener
extends EventListener {
    public void handshakeCompleted(HandshakeCompletedEvent var1);
}

