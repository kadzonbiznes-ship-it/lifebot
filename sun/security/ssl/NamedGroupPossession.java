/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.security.PrivateKey;
import java.security.PublicKey;
import sun.security.ssl.NamedGroup;
import sun.security.ssl.SSLPossession;

interface NamedGroupPossession
extends SSLPossession {
    public NamedGroup getNamedGroup();

    public PublicKey getPublicKey();

    public PrivateKey getPrivateKey();
}

