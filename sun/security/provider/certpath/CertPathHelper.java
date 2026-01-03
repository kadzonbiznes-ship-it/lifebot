/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath;

import java.security.cert.TrustAnchor;
import java.security.cert.X509CRLSelector;
import java.util.Date;

public abstract class CertPathHelper {
    protected static CertPathHelper instance;

    protected CertPathHelper() {
    }

    protected abstract void implSetDateAndTime(X509CRLSelector var1, Date var2, long var3);

    protected abstract boolean implIsJdkCA(TrustAnchor var1);

    public static void setDateAndTime(X509CRLSelector sel, Date date, long skew) {
        instance.implSetDateAndTime(sel, date, skew);
    }

    public static boolean isJdkCA(TrustAnchor anchor) {
        return anchor != null && instance.implIsJdkCA(anchor);
    }
}

