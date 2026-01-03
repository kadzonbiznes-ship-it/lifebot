/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.security.cert.Certificate;

public interface CertSelector
extends Cloneable {
    public boolean match(Certificate var1);

    public Object clone();
}

