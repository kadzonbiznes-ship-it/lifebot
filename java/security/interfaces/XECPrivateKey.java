/*
 * Decompiled with CFR 0.152.
 */
package java.security.interfaces;

import java.security.PrivateKey;
import java.security.interfaces.XECKey;
import java.util.Optional;

public interface XECPrivateKey
extends XECKey,
PrivateKey {
    public Optional<byte[]> getScalar();
}

