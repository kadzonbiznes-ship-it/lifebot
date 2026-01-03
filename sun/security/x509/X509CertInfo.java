/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.util.Collection;
import java.util.Map;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.HexDumpEncoder;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.Extension;
import sun.security.x509.GeneralNames;
import sun.security.x509.OIDMap;
import sun.security.x509.SubjectAlternativeNameExtension;
import sun.security.x509.UniqueIdentity;
import sun.security.x509.X500Name;

public class X509CertInfo {
    public static final String NAME = "info";
    public static final String DN_NAME = "dname";
    public static final String VERSION = "version";
    public static final String SERIAL_NUMBER = "serialNumber";
    public static final String ALGORITHM_ID = "algorithmID";
    public static final String ISSUER = "issuer";
    public static final String SUBJECT = "subject";
    public static final String VALIDITY = "validity";
    public static final String KEY = "key";
    public static final String ISSUER_ID = "issuerID";
    public static final String SUBJECT_ID = "subjectID";
    public static final String EXTENSIONS = "extensions";
    protected CertificateVersion version = new CertificateVersion();
    protected CertificateSerialNumber serialNum = null;
    protected CertificateAlgorithmId algId = null;
    protected X500Name issuer = null;
    protected X500Name subject = null;
    protected CertificateValidity interval = null;
    protected CertificateX509Key pubKey = null;
    protected UniqueIdentity issuerUniqueId = null;
    protected UniqueIdentity subjectUniqueId = null;
    protected CertificateExtensions extensions = null;
    private byte[] rawCertInfo = null;

    public X509CertInfo() {
    }

    public X509CertInfo(byte[] cert) throws CertificateParsingException {
        try {
            DerValue in = new DerValue(cert);
            this.parse(in);
        }
        catch (IOException e) {
            throw new CertificateParsingException(e);
        }
    }

    public X509CertInfo(DerValue derVal) throws CertificateParsingException {
        try {
            this.parse(derVal);
        }
        catch (IOException e) {
            throw new CertificateParsingException(e);
        }
    }

    public void encode(DerOutputStream out) throws CertificateException {
        if (this.rawCertInfo == null) {
            this.emit(out);
            this.rawCertInfo = out.toByteArray();
        } else {
            out.writeBytes((byte[])this.rawCertInfo.clone());
        }
    }

    public byte[] getEncodedInfo() throws CertificateEncodingException {
        try {
            if (this.rawCertInfo == null) {
                DerOutputStream tmp = new DerOutputStream();
                this.emit(tmp);
                this.rawCertInfo = tmp.toByteArray();
            }
            return (byte[])this.rawCertInfo.clone();
        }
        catch (CertificateException e) {
            throw new CertificateEncodingException(e.toString());
        }
    }

    public boolean equals(Object other) {
        if (other instanceof X509CertInfo) {
            return this.equals((X509CertInfo)other);
        }
        return false;
    }

    public boolean equals(X509CertInfo other) {
        if (this == other) {
            return true;
        }
        if (this.rawCertInfo == null || other.rawCertInfo == null) {
            return false;
        }
        if (this.rawCertInfo.length != other.rawCertInfo.length) {
            return false;
        }
        for (int i = 0; i < this.rawCertInfo.length; ++i) {
            if (this.rawCertInfo[i] == other.rawCertInfo[i]) continue;
            return false;
        }
        return true;
    }

    public int hashCode() {
        int retval = 0;
        for (int i = 1; i < this.rawCertInfo.length; ++i) {
            retval += this.rawCertInfo[i] * i;
        }
        return retval;
    }

    public String toString() {
        if (this.subject == null || this.pubKey == null || this.interval == null || this.issuer == null || this.algId == null || this.serialNum == null) {
            throw new NullPointerException("X.509 cert is incomplete");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[\n").append("  ").append(this.version).append('\n').append("  Subject: ").append(this.subject).append('\n').append("  Signature Algorithm: ").append(this.algId).append('\n').append("  Key:  ").append(this.pubKey).append('\n').append("  ").append(this.interval).append('\n').append("  Issuer: ").append(this.issuer).append('\n').append("  ").append(this.serialNum).append('\n');
        if (this.issuerUniqueId != null) {
            sb.append("  Issuer Id:\n").append(this.issuerUniqueId).append('\n');
        }
        if (this.subjectUniqueId != null) {
            sb.append("  Subject Id:\n").append(this.subjectUniqueId).append('\n');
        }
        if (this.extensions != null) {
            Collection<Extension> allExts = this.extensions.getAllExtensions();
            Extension[] exts = allExts.toArray(new Extension[0]);
            sb.append("\nCertificate Extensions: ").append(exts.length);
            for (int i = 0; i < exts.length; ++i) {
                sb.append("\n[").append(i + 1).append("]: ");
                Extension ext = exts[i];
                try {
                    if (OIDMap.getClass(ext.getExtensionId()) == null) {
                        sb.append(ext);
                        byte[] extValue = ext.getExtensionValue();
                        if (extValue == null) continue;
                        DerOutputStream out = new DerOutputStream();
                        out.putOctetString(extValue);
                        extValue = out.toByteArray();
                        HexDumpEncoder enc = new HexDumpEncoder();
                        sb.append("Extension unknown: ").append("DER encoded OCTET string =\n").append(enc.encodeBuffer(extValue)).append('\n');
                        continue;
                    }
                    sb.append(ext);
                    continue;
                }
                catch (Exception e) {
                    sb.append(", Error parsing this extension");
                }
            }
            Map<String, Extension> invalid = this.extensions.getUnparseableExtensions();
            if (!invalid.isEmpty()) {
                sb.append("\nUnparseable certificate extensions: ").append(invalid.size());
                int i = 1;
                for (Extension ext : invalid.values()) {
                    sb.append("\n[").append(i++).append("]: ").append(ext);
                }
            }
        }
        sb.append("\n]");
        return sb.toString();
    }

    public CertificateExtensions getExtensions() {
        return this.extensions;
    }

    public UniqueIdentity getIssuerUniqueId() {
        return this.issuerUniqueId;
    }

    public UniqueIdentity getSubjectUniqueId() {
        return this.subjectUniqueId;
    }

    public X500Name getIssuer() {
        return this.issuer;
    }

    public X500Name getSubject() {
        return this.subject;
    }

    private Object getX500Name(String name, boolean getIssuer) throws IOException {
        if (name.equalsIgnoreCase(DN_NAME)) {
            return getIssuer ? this.issuer : this.subject;
        }
        if (name.equalsIgnoreCase("x500principal")) {
            return getIssuer ? this.issuer.asX500Principal() : this.subject.asX500Principal();
        }
        throw new IOException("Attribute name not recognized.");
    }

    private void parse(DerValue val) throws CertificateParsingException, IOException {
        if (val.tag != 48) {
            throw new CertificateParsingException("signed fields invalid");
        }
        this.rawCertInfo = val.toByteArray();
        DerInputStream in = val.data;
        DerValue tmp = in.getDerValue();
        if (tmp.isContextSpecific((byte)0)) {
            this.version = new CertificateVersion(tmp);
            tmp = in.getDerValue();
        }
        this.serialNum = new CertificateSerialNumber(tmp);
        this.algId = new CertificateAlgorithmId(in);
        this.issuer = new X500Name(in);
        if (this.issuer.isEmpty()) {
            throw new CertificateParsingException("Empty issuer DN not allowed in X509Certificates");
        }
        this.interval = new CertificateValidity(in);
        this.subject = new X500Name(in);
        if (this.version.compare(0) == 0 && this.subject.isEmpty()) {
            throw new CertificateParsingException("Empty subject DN not allowed in v1 certificate");
        }
        this.pubKey = new CertificateX509Key(in);
        if (in.available() != 0) {
            if (this.version.compare(0) == 0) {
                throw new CertificateParsingException("no more data allowed for version 1 certificate");
            }
        } else {
            return;
        }
        tmp = in.getDerValue();
        if (tmp.isContextSpecific((byte)1)) {
            this.issuerUniqueId = new UniqueIdentity(tmp);
            if (in.available() == 0) {
                return;
            }
            tmp = in.getDerValue();
        }
        if (tmp.isContextSpecific((byte)2)) {
            this.subjectUniqueId = new UniqueIdentity(tmp);
            if (in.available() == 0) {
                return;
            }
            tmp = in.getDerValue();
        }
        if (this.version.compare(2) != 0) {
            throw new CertificateParsingException("Extensions not allowed in v2 certificate");
        }
        if (tmp.isConstructed() && tmp.isContextSpecific((byte)3)) {
            this.extensions = new CertificateExtensions(tmp.data);
        }
        this.verifyCert(this.subject, this.extensions);
    }

    private void verifyCert(X500Name subject, CertificateExtensions extensions) throws CertificateParsingException {
        if (subject.isEmpty()) {
            if (extensions == null) {
                throw new CertificateParsingException("X.509 Certificate is incomplete: subject field is empty, and certificate has no extensions");
            }
            SubjectAlternativeNameExtension subjectAltNameExt = (SubjectAlternativeNameExtension)extensions.getExtension("SubjectAlternativeName");
            if (subjectAltNameExt == null) {
                throw new CertificateParsingException("X.509 Certificate is incomplete: subject field is empty, and SubjectAlternativeName extension is absent");
            }
            GeneralNames names = subjectAltNameExt.getNames();
            if (names == null || names.isEmpty()) {
                throw new CertificateParsingException("X.509 Certificate is incomplete: subject field is empty, and SubjectAlternativeName extension is empty");
            }
            if (!subjectAltNameExt.isCritical()) {
                throw new CertificateParsingException("X.509 Certificate is incomplete: SubjectAlternativeName extension MUST be marked critical when subject field is empty");
            }
        }
    }

    private void emit(DerOutputStream out) throws CertificateException {
        DerOutputStream tmp = new DerOutputStream();
        this.version.encode(tmp);
        this.serialNum.encode(tmp);
        this.algId.encode(tmp);
        if (this.version.compare(0) == 0 && this.issuer.toString() == null) {
            throw new CertificateParsingException("Null issuer DN not allowed in v1 certificate");
        }
        this.issuer.encode(tmp);
        this.interval.encode(tmp);
        if (this.version.compare(0) == 0 && this.subject.toString() == null) {
            throw new CertificateParsingException("Null subject DN not allowed in v1 certificate");
        }
        this.subject.encode(tmp);
        this.pubKey.encode(tmp);
        if (this.issuerUniqueId != null) {
            this.issuerUniqueId.encode(tmp, DerValue.createTag((byte)-128, false, (byte)1));
        }
        if (this.subjectUniqueId != null) {
            this.subjectUniqueId.encode(tmp, DerValue.createTag((byte)-128, false, (byte)2));
        }
        if (this.extensions != null) {
            this.extensions.encode(tmp);
        }
        out.write((byte)48, tmp);
    }

    public void setVersion(CertificateVersion val) {
        this.rawCertInfo = null;
        this.version = val;
    }

    public CertificateVersion getVersion() {
        return this.version;
    }

    public void setSerialNumber(CertificateSerialNumber val) {
        this.rawCertInfo = null;
        this.serialNum = val;
    }

    public CertificateSerialNumber getSerialNumber() {
        return this.serialNum;
    }

    public void setAlgorithmId(CertificateAlgorithmId val) {
        this.rawCertInfo = null;
        this.algId = val;
    }

    public CertificateAlgorithmId getAlgorithmId() {
        return this.algId;
    }

    public void setIssuer(X500Name val) {
        this.rawCertInfo = null;
        this.issuer = val;
    }

    public void setValidity(CertificateValidity val) {
        this.rawCertInfo = null;
        this.interval = val;
    }

    public CertificateValidity getValidity() {
        return this.interval;
    }

    public void setSubject(X500Name val) throws CertificateException {
        this.rawCertInfo = null;
        this.subject = val;
    }

    public void setKey(CertificateX509Key val) {
        this.rawCertInfo = null;
        this.pubKey = val;
    }

    public CertificateX509Key getKey() {
        return this.pubKey;
    }

    public void setIssuerUniqueId(UniqueIdentity val) throws CertificateException {
        this.rawCertInfo = null;
        if (this.version.compare(1) < 0) {
            throw new CertificateException("Invalid version");
        }
        this.issuerUniqueId = val;
    }

    public void setSubjectUniqueId(UniqueIdentity val) throws CertificateException {
        this.rawCertInfo = null;
        if (this.version.compare(1) < 0) {
            throw new CertificateException("Invalid version");
        }
        this.subjectUniqueId = val;
    }

    public void setExtensions(CertificateExtensions val) throws CertificateException {
        this.rawCertInfo = null;
        if (this.version.compare(2) < 0) {
            throw new CertificateException("Invalid version");
        }
        this.extensions = val;
    }
}

