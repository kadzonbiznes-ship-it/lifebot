/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.event;

import jdk.internal.event.Event;

public final class TLSHandshakeEvent
extends Event {
    public String peerHost;
    public int peerPort;
    public String protocolVersion;
    public String cipherSuite;
    public long certificateId;
}

