/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.io.ByteArrayInputStream;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Iterator;
import java.util.List;

public abstract class CertPath
implements Serializable {
    private static final long serialVersionUID = 6068470306649138683L;
    private final transient String type;

    protected CertPath(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public abstract Iterator<String> getEncodings();

    public boolean equals(Object other) {
        CertPath that;
        if (this == other) {
            return true;
        }
        return other instanceof CertPath && (that = (CertPath)other).getType().equals(this.type) && this.getCertificates().equals(that.getCertificates());
    }

    public int hashCode() {
        int hashCode = this.type.hashCode();
        hashCode = 31 * hashCode + this.getCertificates().hashCode();
        return hashCode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<? extends Certificate> stringIterator = this.getCertificates().iterator();
        sb.append("\n" + this.type + " Cert Path: length = " + this.getCertificates().size() + ".\n");
        sb.append("[\n");
        int i = 1;
        while (stringIterator.hasNext()) {
            sb.append("=========================================================Certificate " + i + " start.\n");
            Certificate stringCert = stringIterator.next();
            sb.append(stringCert.toString());
            sb.append("\n=========================================================Certificate " + i + " end.\n\n\n");
            ++i;
        }
        sb.append("\n]");
        return sb.toString();
    }

    public abstract byte[] getEncoded() throws CertificateEncodingException;

    public abstract byte[] getEncoded(String var1) throws CertificateEncodingException;

    public abstract List<? extends Certificate> getCertificates();

    protected Object writeReplace() throws ObjectStreamException {
        try {
            return new CertPathRep(this.type, this.getEncoded());
        }
        catch (CertificateException ce) {
            NotSerializableException nse = new NotSerializableException("java.security.cert.CertPath: " + this.type);
            nse.initCause(ce);
            throw nse;
        }
    }

    protected static class CertPathRep
    implements Serializable {
        private static final long serialVersionUID = 3015633072427920915L;
        private final String type;
        private final byte[] data;

        protected CertPathRep(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }

        protected Object readResolve() throws ObjectStreamException {
            try {
                CertificateFactory cf = CertificateFactory.getInstance(this.type);
                return cf.generateCertPath(new ByteArrayInputStream(this.data));
            }
            catch (CertificateException ce) {
                NotSerializableException nse = new NotSerializableException("java.security.cert.CertPath: " + this.type);
                nse.initCause(ce);
                throw nse;
            }
        }
    }
}

