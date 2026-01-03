/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.security.PublicKey;

public interface InternalPrivateKey {
    default public PublicKey calculatePublicKey() {
        throw new UnsupportedOperationException();
    }
}

