/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.security.cert.TrustAnchor;
import java.security.cert.X509CRLSelector;
import java.util.Date;
import sun.security.provider.certpath.CertPathHelper;

class CertPathHelperImpl
extends CertPathHelper {
    private CertPathHelperImpl() {
    }

    static synchronized void initialize() {
        if (CertPathHelper.instance == null) {
            CertPathHelper.instance = new CertPathHelperImpl();
        }
    }

    @Override
    protected void implSetDateAndTime(X509CRLSelector sel, Date date, long skew) {
        sel.setDateAndTime(date, skew);
    }

    @Override
    protected boolean implIsJdkCA(TrustAnchor anchor) {
        return anchor.isJdkCA();
    }
}

