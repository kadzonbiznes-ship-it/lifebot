/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath;

import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.KnownOIDs;
import sun.security.x509.AuthorityKeyIdentifierExtension;
import sun.security.x509.SerialNumber;

class AdaptableX509CertSelector
extends X509CertSelector {
    private static final Debug debug = Debug.getInstance("certpath");
    private Date startDate;
    private Date endDate;
    private byte[] ski;
    private BigInteger serial;

    AdaptableX509CertSelector() {
    }

    void setValidityPeriod(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public void setSubjectKeyIdentifier(byte[] subjectKeyID) {
        throw new IllegalArgumentException();
    }

    @Override
    public void setSerialNumber(BigInteger serial) {
        throw new IllegalArgumentException();
    }

    void setSkiAndSerialNumber(AuthorityKeyIdentifierExtension ext) throws IOException {
        this.ski = null;
        this.serial = null;
        if (ext != null) {
            this.ski = ext.getEncodedKeyIdentifier();
            SerialNumber asn = ext.getSerialNumber();
            if (asn != null) {
                this.serial = asn.getNumber();
            }
        }
    }

    @Override
    public boolean match(Certificate cert) {
        X509Certificate xcert = (X509Certificate)cert;
        if (!this.matchSubjectKeyID(xcert)) {
            return false;
        }
        int version = xcert.getVersion();
        if (this.serial != null && version > 2 && !this.serial.equals(xcert.getSerialNumber())) {
            return false;
        }
        if (version < 3) {
            if (this.startDate != null) {
                try {
                    xcert.checkValidity(this.startDate);
                }
                catch (CertificateException ce) {
                    return false;
                }
            }
            if (this.endDate != null) {
                try {
                    xcert.checkValidity(this.endDate);
                }
                catch (CertificateException ce) {
                    return false;
                }
            }
        }
        return super.match(cert);
    }

    private boolean matchSubjectKeyID(X509Certificate xcert) {
        if (this.ski == null) {
            return true;
        }
        try {
            byte[] extVal = xcert.getExtensionValue(KnownOIDs.SubjectKeyID.value());
            if (extVal == null) {
                if (debug != null && Debug.isVerbose()) {
                    debug.println("AdaptableX509CertSelector.match: no subject key ID extension. Subject: " + xcert.getSubjectX500Principal());
                }
                return true;
            }
            DerInputStream in = new DerInputStream(extVal);
            byte[] certSubjectKeyID = in.getOctetString();
            if (certSubjectKeyID == null || !Arrays.equals(this.ski, certSubjectKeyID)) {
                if (debug != null && Debug.isVerbose()) {
                    debug.println("AdaptableX509CertSelector.match: subject key IDs don't match. Expected: " + Arrays.toString(this.ski) + " Cert's: " + Arrays.toString(certSubjectKeyID));
                }
                return false;
            }
        }
        catch (IOException ex) {
            if (debug != null && Debug.isVerbose()) {
                debug.println("AdaptableX509CertSelector.match: exception in subject key ID check");
            }
            return false;
        }
        return true;
    }

    @Override
    public Object clone() {
        AdaptableX509CertSelector copy = (AdaptableX509CertSelector)super.clone();
        if (this.startDate != null) {
            copy.startDate = (Date)this.startDate.clone();
        }
        if (this.endDate != null) {
            copy.endDate = (Date)this.endDate.clone();
        }
        if (this.ski != null) {
            copy.ski = (byte[])this.ski.clone();
        }
        return copy;
    }
}

