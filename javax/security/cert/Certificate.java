/*
 * Decompiled with CFR 0.152.
 */
package javax.security.cert;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import javax.security.cert.CertificateEncodingException;
import javax.security.cert.CertificateException;

@Deprecated(since="9", forRemoval=true)
public abstract class Certificate {
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Certificate)) {
            return false;
        }
        try {
            byte[] thisCert = this.getEncoded();
            byte[] otherCert = ((Certificate)other).getEncoded();
            if (thisCert.length != otherCert.length) {
                return false;
            }
            for (int i = 0; i < thisCert.length; ++i) {
                if (thisCert[i] == otherCert[i]) continue;
                return false;
            }
            return true;
        }
        catch (CertificateException e) {
            return false;
        }
    }

    public int hashCode() {
        int retval = 0;
        try {
            byte[] certData = this.getEncoded();
            for (int i = 1; i < certData.length; ++i) {
                retval += certData[i] * i;
            }
            return retval;
        }
        catch (CertificateException e) {
            return retval;
        }
    }

    public abstract byte[] getEncoded() throws CertificateEncodingException;

    public abstract void verify(PublicKey var1) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException;

    public abstract void verify(PublicKey var1, String var2) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException;

    public abstract String toString();

    public abstract PublicKey getPublicKey();
}

