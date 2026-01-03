/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.event;

import jdk.internal.event.Event;

public final class SecurityProviderServiceEvent
extends Event {
    private static final SecurityProviderServiceEvent EVENT = new SecurityProviderServiceEvent();
    public String type;
    public String algorithm;
    public String provider;

    public static boolean isTurnedOn() {
        return EVENT.isEnabled();
    }
}

