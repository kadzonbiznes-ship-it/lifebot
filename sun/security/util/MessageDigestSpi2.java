/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.security.InvalidKeyException;
import javax.crypto.SecretKey;

public interface MessageDigestSpi2 {
    public void engineUpdate(SecretKey var1) throws InvalidKeyException;
}

