/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.event;

import jdk.internal.event.Event;

public final class X509ValidationEvent
extends Event {
    public long certificateId;
    public int certificatePosition;
    public long validationCounter;
}

