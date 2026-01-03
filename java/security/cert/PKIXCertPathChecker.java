/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.security.cert.CertPathChecker;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public abstract class PKIXCertPathChecker
implements CertPathChecker,
Cloneable {
    protected PKIXCertPathChecker() {
    }

    @Override
    public abstract void init(boolean var1) throws CertPathValidatorException;

    @Override
    public abstract boolean isForwardCheckingSupported();

    public abstract Set<String> getSupportedExtensions();

    public abstract void check(Certificate var1, Collection<String> var2) throws CertPathValidatorException;

    @Override
    public void check(Certificate cert) throws CertPathValidatorException {
        this.check(cert, Collections.emptySet());
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString(), e);
        }
    }
}

