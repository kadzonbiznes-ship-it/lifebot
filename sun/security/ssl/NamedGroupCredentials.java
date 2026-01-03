/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.security.PublicKey;
import sun.security.ssl.NamedGroup;
import sun.security.ssl.SSLCredentials;

interface NamedGroupCredentials
extends SSLCredentials {
    public PublicKey getPublicKey();

    public NamedGroup getNamedGroup();
}

