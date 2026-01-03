/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.event;

import jdk.internal.event.Event;

public final class X509CertificateEvent
extends Event {
    private static final X509CertificateEvent EVENT = new X509CertificateEvent();
    public String algorithm;
    public String serialNumber;
    public String subject;
    public String issuer;
    public String keyType;
    public int keyLength;
    public long certificateId;
    public long validFrom;
    public long validUntil;

    public static boolean isTurnedOn() {
        return EVENT.isEnabled();
    }
}

