/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;

public interface CertPathChecker {
    public void init(boolean var1) throws CertPathValidatorException;

    public boolean isForwardCheckingSupported();

    public void check(Certificate var1) throws CertPathValidatorException;
}

