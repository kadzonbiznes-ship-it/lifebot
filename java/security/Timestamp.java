/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.List;

public final class Timestamp
implements Serializable {
    private static final long serialVersionUID = -5502683707821851294L;
    private Date timestamp;
    private final CertPath signerCertPath;
    private transient int myhash = -1;

    public Timestamp(Date timestamp, CertPath signerCertPath) {
        if (Timestamp.isNull(timestamp, signerCertPath)) {
            throw new NullPointerException();
        }
        this.timestamp = new Date(timestamp.getTime());
        this.signerCertPath = signerCertPath;
    }

    public Date getTimestamp() {
        return new Date(this.timestamp.getTime());
    }

    public CertPath getSignerCertPath() {
        return this.signerCertPath;
    }

    public int hashCode() {
        if (this.myhash == -1) {
            this.myhash = this.timestamp.hashCode() + this.signerCertPath.hashCode();
        }
        return this.myhash;
    }

    public boolean equals(Object obj) {
        Timestamp other;
        if (this == obj) {
            return true;
        }
        return obj instanceof Timestamp && this.timestamp.equals((other = (Timestamp)obj).getTimestamp()) && this.signerCertPath.equals(other.getSignerCertPath());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append("timestamp: " + this.timestamp);
        List<? extends Certificate> certs = this.signerCertPath.getCertificates();
        if (!certs.isEmpty()) {
            sb.append("TSA: " + certs.get(0));
        } else {
            sb.append("TSA: <empty>");
        }
        sb.append(")");
        return sb.toString();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        if (Timestamp.isNull(this.timestamp, this.signerCertPath)) {
            throw new InvalidObjectException("Invalid null field(s)");
        }
        this.myhash = -1;
        this.timestamp = new Date(this.timestamp.getTime());
    }

    private static boolean isNull(Date d, CertPath c) {
        return d == null || c == null;
    }
}

