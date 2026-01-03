/*
 * Decompiled with CFR 0.152.
 */
package java.security.interfaces;

import java.security.PublicKey;
import java.security.interfaces.ECKey;
import java.security.spec.ECPoint;

public interface ECPublicKey
extends PublicKey,
ECKey {
    @Deprecated
    public static final long serialVersionUID = -3314988629879632826L;

    public ECPoint getW();
}

