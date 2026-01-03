/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.io.IOException;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.CertSelector;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.Extension;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.util.HexDumpEncoder;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificatePoliciesExtension;
import sun.security.x509.CertificatePolicyId;
import sun.security.x509.CertificatePolicySet;
import sun.security.x509.DNSName;
import sun.security.x509.EDIPartyName;
import sun.security.x509.ExtendedKeyUsageExtension;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.GeneralNames;
import sun.security.x509.GeneralSubtree;
import sun.security.x509.GeneralSubtrees;
import sun.security.x509.IPAddressName;
import sun.security.x509.NameConstraintsExtension;
import sun.security.x509.OIDName;
import sun.security.x509.OtherName;
import sun.security.x509.PolicyInformation;
import sun.security.x509.PrivateKeyUsageExtension;
import sun.security.x509.RFC822Name;
import sun.security.x509.SubjectAlternativeNameExtension;
import sun.security.x509.URIName;
import sun.security.x509.X400Address;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509Key;

public class X509CertSelector
implements CertSelector {
    private static final Debug debug = Debug.getInstance("certpath");
    private static final ObjectIdentifier ANY_EXTENDED_KEY_USAGE = ObjectIdentifier.of(KnownOIDs.anyExtendedKeyUsage);
    private BigInteger serialNumber;
    private X500Principal issuer;
    private X500Principal subject;
    private byte[] subjectKeyID;
    private byte[] authorityKeyID;
    private Date certificateValid;
    private Date privateKeyValid;
    private ObjectIdentifier subjectPublicKeyAlgID;
    private PublicKey subjectPublicKey;
    private byte[] subjectPublicKeyBytes;
    private boolean[] keyUsage;
    private Set<String> keyPurposeSet;
    private Set<ObjectIdentifier> keyPurposeOIDSet;
    private Set<List<?>> subjectAlternativeNames;
    private Set<GeneralNameInterface> subjectAlternativeGeneralNames;
    private CertificatePolicySet policy;
    private Set<String> policySet;
    private Set<List<?>> pathToNames;
    private Set<GeneralNameInterface> pathToGeneralNames;
    private NameConstraintsExtension nc;
    private byte[] ncBytes;
    private int basicConstraints = -1;
    private X509Certificate x509Cert;
    private boolean matchAllSubjectAltNames = true;
    private static final Boolean FALSE = Boolean.FALSE;
    static final int NAME_ANY = 0;
    static final int NAME_RFC822 = 1;
    static final int NAME_DNS = 2;
    static final int NAME_X400 = 3;
    static final int NAME_DIRECTORY = 4;
    static final int NAME_EDI = 5;
    static final int NAME_URI = 6;
    static final int NAME_IP = 7;
    static final int NAME_OID = 8;

    public void setCertificate(X509Certificate cert) {
        this.x509Cert = cert;
    }

    public void setSerialNumber(BigInteger serial) {
        this.serialNumber = serial;
    }

    public void setIssuer(X500Principal issuer) {
        this.issuer = issuer;
    }

    @Deprecated(since="16")
    public void setIssuer(String issuerDN) throws IOException {
        this.issuer = issuerDN == null ? null : new X500Name(issuerDN).asX500Principal();
    }

    public void setIssuer(byte[] issuerDN) throws IOException {
        try {
            this.issuer = issuerDN == null ? null : new X500Principal(issuerDN);
        }
        catch (IllegalArgumentException e) {
            throw new IOException("Invalid name", e);
        }
    }

    public void setSubject(X500Principal subject) {
        this.subject = subject;
    }

    @Deprecated(since="16")
    public void setSubject(String subjectDN) throws IOException {
        this.subject = subjectDN == null ? null : new X500Name(subjectDN).asX500Principal();
    }

    public void setSubject(byte[] subjectDN) throws IOException {
        try {
            this.subject = subjectDN == null ? null : new X500Principal(subjectDN);
        }
        catch (IllegalArgumentException e) {
            throw new IOException("Invalid name", e);
        }
    }

    public void setSubjectKeyIdentifier(byte[] subjectKeyID) {
        this.subjectKeyID = (byte[])(subjectKeyID == null ? null : (byte[])subjectKeyID.clone());
    }

    public void setAuthorityKeyIdentifier(byte[] authorityKeyID) {
        this.authorityKeyID = (byte[])(authorityKeyID == null ? null : (byte[])authorityKeyID.clone());
    }

    public void setCertificateValid(Date certValid) {
        this.certificateValid = certValid == null ? null : (Date)certValid.clone();
    }

    public void setPrivateKeyValid(Date privateKeyValid) {
        this.privateKeyValid = privateKeyValid == null ? null : (Date)privateKeyValid.clone();
    }

    public void setSubjectPublicKeyAlgID(String oid) throws IOException {
        this.subjectPublicKeyAlgID = oid == null ? null : ObjectIdentifier.of(oid);
    }

    public void setSubjectPublicKey(PublicKey key) {
        if (key == null) {
            this.subjectPublicKey = null;
            this.subjectPublicKeyBytes = null;
        } else {
            this.subjectPublicKey = key;
            this.subjectPublicKeyBytes = key.getEncoded();
        }
    }

    public void setSubjectPublicKey(byte[] key) throws IOException {
        if (key == null) {
            this.subjectPublicKey = null;
            this.subjectPublicKeyBytes = null;
        } else {
            this.subjectPublicKeyBytes = (byte[])key.clone();
            this.subjectPublicKey = X509Key.parse(new DerValue(this.subjectPublicKeyBytes));
        }
    }

    public void setKeyUsage(boolean[] keyUsage) {
        this.keyUsage = (boolean[])(keyUsage == null ? null : (boolean[])keyUsage.clone());
    }

    public void setExtendedKeyUsage(Set<String> keyPurposeSet) throws IOException {
        if (keyPurposeSet == null || keyPurposeSet.isEmpty()) {
            this.keyPurposeSet = null;
            this.keyPurposeOIDSet = null;
        } else {
            this.keyPurposeSet = Collections.unmodifiableSet(new HashSet<String>(keyPurposeSet));
            this.keyPurposeOIDSet = new HashSet<ObjectIdentifier>();
            for (String s : this.keyPurposeSet) {
                this.keyPurposeOIDSet.add(ObjectIdentifier.of(s));
            }
        }
    }

    public void setMatchAllSubjectAltNames(boolean matchAllNames) {
        this.matchAllSubjectAltNames = matchAllNames;
    }

    public void setSubjectAlternativeNames(Collection<List<?>> names) throws IOException {
        if (names == null) {
            this.subjectAlternativeNames = null;
            this.subjectAlternativeGeneralNames = null;
        } else {
            if (names.isEmpty()) {
                this.subjectAlternativeNames = null;
                this.subjectAlternativeGeneralNames = null;
                return;
            }
            Set<List<?>> tempNames = X509CertSelector.cloneAndCheckNames(names);
            this.subjectAlternativeGeneralNames = X509CertSelector.parseNames(tempNames);
            this.subjectAlternativeNames = tempNames;
        }
    }

    public void addSubjectAlternativeName(int type, String name) throws IOException {
        this.addSubjectAlternativeNameInternal(type, name);
    }

    public void addSubjectAlternativeName(int type, byte[] name) throws IOException {
        this.addSubjectAlternativeNameInternal(type, name.clone());
    }

    private void addSubjectAlternativeNameInternal(int type, Object name) throws IOException {
        GeneralNameInterface tempName = X509CertSelector.makeGeneralNameInterface(type, name);
        if (this.subjectAlternativeNames == null) {
            this.subjectAlternativeNames = new HashSet();
        }
        if (this.subjectAlternativeGeneralNames == null) {
            this.subjectAlternativeGeneralNames = new HashSet<GeneralNameInterface>();
        }
        ArrayList<Object> list = new ArrayList<Object>(2);
        list.add(type);
        list.add(name);
        this.subjectAlternativeNames.add(list);
        this.subjectAlternativeGeneralNames.add(tempName);
    }

    private static Set<GeneralNameInterface> parseNames(Collection<List<?>> names) throws IOException {
        HashSet<GeneralNameInterface> genNames = new HashSet<GeneralNameInterface>();
        for (List<?> nameList : names) {
            if (nameList.size() != 2) {
                throw new IOException("name list size not 2");
            }
            Object o = nameList.get(0);
            if (!(o instanceof Integer)) {
                throw new IOException("expected an Integer");
            }
            Integer nameType = (Integer)o;
            o = nameList.get(1);
            genNames.add(X509CertSelector.makeGeneralNameInterface(nameType, o));
        }
        return genNames;
    }

    static boolean equalNames(Collection<?> object1, Collection<?> object2) {
        if (object1 == null || object2 == null) {
            return object1 == object2;
        }
        return object1.equals(object2);
    }

    static GeneralNameInterface makeGeneralNameInterface(int type, Object name) throws IOException {
        GeneralNameInterface result;
        if (debug != null) {
            debug.println("X509CertSelector.makeGeneralNameInterface(" + type + ")...");
        }
        if (name instanceof String) {
            String nameAsString = (String)name;
            if (debug != null) {
                debug.println("X509CertSelector.makeGeneralNameInterface() name is String: " + nameAsString);
            }
            result = switch (type) {
                case 1 -> new RFC822Name(nameAsString);
                case 2 -> new DNSName(nameAsString);
                case 4 -> new X500Name(nameAsString);
                case 6 -> new URIName(nameAsString);
                case 7 -> new IPAddressName(nameAsString);
                case 8 -> new OIDName(nameAsString);
                default -> throw new IOException("unable to parse String names of type " + type);
            };
            if (debug != null) {
                debug.println("X509CertSelector.makeGeneralNameInterface() result: " + result.toString());
            }
        } else if (name instanceof byte[]) {
            DerValue val = new DerValue((byte[])name);
            if (debug != null) {
                debug.println("X509CertSelector.makeGeneralNameInterface() is byte[]");
            }
            result = switch (type) {
                case 0 -> new OtherName(val);
                case 1 -> new RFC822Name(val);
                case 2 -> new DNSName(val);
                case 3 -> new X400Address(val);
                case 4 -> new X500Name(val);
                case 5 -> new EDIPartyName(val);
                case 6 -> new URIName(val);
                case 7 -> new IPAddressName(val);
                case 8 -> new OIDName(val);
                default -> throw new IOException("unable to parse byte array names of type " + type);
            };
            if (debug != null) {
                debug.println("X509CertSelector.makeGeneralNameInterface() result: " + result.toString());
            }
        } else {
            if (debug != null) {
                debug.println("X509CertSelector.makeGeneralName() input name not String or byte array");
            }
            throw new IOException("name not String or byte array");
        }
        return result;
    }

    public void setNameConstraints(byte[] bytes) throws IOException {
        if (bytes == null) {
            this.ncBytes = null;
            this.nc = null;
        } else {
            this.ncBytes = (byte[])bytes.clone();
            this.nc = new NameConstraintsExtension(FALSE, bytes);
        }
    }

    public void setBasicConstraints(int minMaxPathLen) {
        if (minMaxPathLen < -2) {
            throw new IllegalArgumentException("basic constraints less than -2");
        }
        this.basicConstraints = minMaxPathLen;
    }

    public void setPolicy(Set<String> certPolicySet) throws IOException {
        if (certPolicySet == null) {
            this.policySet = null;
            this.policy = null;
        } else {
            Set<String> tempSet = Collections.unmodifiableSet(new HashSet<String>(certPolicySet));
            Iterator<String> i = tempSet.iterator();
            Vector<CertificatePolicyId> polIdVector = new Vector<CertificatePolicyId>();
            while (i.hasNext()) {
                String o = i.next();
                if (!(o instanceof String)) {
                    throw new IOException("non String in certPolicySet");
                }
                polIdVector.add(new CertificatePolicyId(ObjectIdentifier.of(o)));
            }
            this.policySet = tempSet;
            this.policy = new CertificatePolicySet(polIdVector);
        }
    }

    public void setPathToNames(Collection<List<?>> names) throws IOException {
        if (names == null || names.isEmpty()) {
            this.pathToNames = null;
            this.pathToGeneralNames = null;
        } else {
            Set<List<?>> tempNames = X509CertSelector.cloneAndCheckNames(names);
            this.pathToGeneralNames = X509CertSelector.parseNames(tempNames);
            this.pathToNames = tempNames;
        }
    }

    public void addPathToName(int type, String name) throws IOException {
        this.addPathToNameInternal(type, name);
    }

    public void addPathToName(int type, byte[] name) throws IOException {
        this.addPathToNameInternal(type, name.clone());
    }

    private void addPathToNameInternal(int type, Object name) throws IOException {
        GeneralNameInterface tempName = X509CertSelector.makeGeneralNameInterface(type, name);
        if (this.pathToGeneralNames == null) {
            this.pathToNames = new HashSet();
            this.pathToGeneralNames = new HashSet<GeneralNameInterface>();
        }
        ArrayList<Object> list = new ArrayList<Object>(2);
        list.add(type);
        list.add(name);
        this.pathToNames.add(list);
        this.pathToGeneralNames.add(tempName);
    }

    public X509Certificate getCertificate() {
        return this.x509Cert;
    }

    public BigInteger getSerialNumber() {
        return this.serialNumber;
    }

    public X500Principal getIssuer() {
        return this.issuer;
    }

    @Deprecated(since="16")
    public String getIssuerAsString() {
        return this.issuer == null ? null : this.issuer.getName();
    }

    public byte[] getIssuerAsBytes() throws IOException {
        return this.issuer == null ? null : this.issuer.getEncoded();
    }

    public X500Principal getSubject() {
        return this.subject;
    }

    @Deprecated(since="16")
    public String getSubjectAsString() {
        return this.subject == null ? null : this.subject.getName();
    }

    public byte[] getSubjectAsBytes() throws IOException {
        return this.subject == null ? null : this.subject.getEncoded();
    }

    public byte[] getSubjectKeyIdentifier() {
        if (this.subjectKeyID == null) {
            return null;
        }
        return (byte[])this.subjectKeyID.clone();
    }

    public byte[] getAuthorityKeyIdentifier() {
        if (this.authorityKeyID == null) {
            return null;
        }
        return (byte[])this.authorityKeyID.clone();
    }

    public Date getCertificateValid() {
        if (this.certificateValid == null) {
            return null;
        }
        return (Date)this.certificateValid.clone();
    }

    public Date getPrivateKeyValid() {
        if (this.privateKeyValid == null) {
            return null;
        }
        return (Date)this.privateKeyValid.clone();
    }

    public String getSubjectPublicKeyAlgID() {
        if (this.subjectPublicKeyAlgID == null) {
            return null;
        }
        return this.subjectPublicKeyAlgID.toString();
    }

    public PublicKey getSubjectPublicKey() {
        return this.subjectPublicKey;
    }

    public boolean[] getKeyUsage() {
        if (this.keyUsage == null) {
            return null;
        }
        return (boolean[])this.keyUsage.clone();
    }

    public Set<String> getExtendedKeyUsage() {
        return this.keyPurposeSet;
    }

    public boolean getMatchAllSubjectAltNames() {
        return this.matchAllSubjectAltNames;
    }

    public Collection<List<?>> getSubjectAlternativeNames() {
        if (this.subjectAlternativeNames == null) {
            return null;
        }
        return X509CertSelector.cloneNames(this.subjectAlternativeNames);
    }

    private static Set<List<?>> cloneNames(Collection<List<?>> names) {
        try {
            return X509CertSelector.cloneAndCheckNames(names);
        }
        catch (IOException e) {
            throw new RuntimeException("cloneNames encountered IOException: " + e.getMessage());
        }
    }

    private static Set<List<?>> cloneAndCheckNames(Collection<List<?>> names) throws IOException {
        HashSet namesCopy = new HashSet();
        for (List<?> list : names) {
            namesCopy.add(new ArrayList(list));
        }
        for (List<Object> list : namesCopy) {
            List<Object> nameList = list;
            if (nameList.size() != 2) {
                throw new IOException("name list size not 2");
            }
            Object o = nameList.get(0);
            if (!(o instanceof Integer)) {
                throw new IOException("expected an Integer");
            }
            Integer nameType = (Integer)o;
            if (nameType < 0 || nameType > 8) {
                throw new IOException("name type not 0-8");
            }
            Object nameObject = nameList.get(1);
            if (!(nameObject instanceof byte[]) && !(nameObject instanceof String)) {
                if (debug != null) {
                    debug.println("X509CertSelector.cloneAndCheckNames() name not byte array");
                }
                throw new IOException("name not byte array or String");
            }
            if (!(nameObject instanceof byte[])) continue;
            nameList.set(1, ((byte[])nameObject).clone());
        }
        return namesCopy;
    }

    public byte[] getNameConstraints() {
        if (this.ncBytes == null) {
            return null;
        }
        return (byte[])this.ncBytes.clone();
    }

    public int getBasicConstraints() {
        return this.basicConstraints;
    }

    public Set<String> getPolicy() {
        return this.policySet;
    }

    public Collection<List<?>> getPathToNames() {
        if (this.pathToNames == null) {
            return null;
        }
        return X509CertSelector.cloneNames(this.pathToNames);
    }

    public String toString() {
        HexDumpEncoder enc;
        StringBuilder sb = new StringBuilder();
        sb.append("X509CertSelector: [\n");
        if (this.x509Cert != null) {
            sb.append("  Certificate: " + this.x509Cert + "\n");
        }
        if (this.serialNumber != null) {
            sb.append("  Serial Number: " + this.serialNumber + "\n");
        }
        if (this.issuer != null) {
            sb.append("  Issuer: " + this.getIssuerAsString() + "\n");
        }
        if (this.subject != null) {
            sb.append("  Subject: " + this.getSubjectAsString() + "\n");
        }
        sb.append("  matchAllSubjectAltNames flag: " + this.matchAllSubjectAltNames + "\n");
        if (this.subjectAlternativeNames != null) {
            sb.append("  SubjectAlternativeNames:\n");
            for (List<?> list : this.subjectAlternativeNames) {
                sb.append("    type " + list.get(0) + ", name " + list.get(1) + "\n");
            }
        }
        if (this.subjectKeyID != null) {
            enc = new HexDumpEncoder();
            sb.append("  Subject Key Identifier: " + enc.encodeBuffer(this.subjectKeyID) + "\n");
        }
        if (this.authorityKeyID != null) {
            enc = new HexDumpEncoder();
            sb.append("  Authority Key Identifier: " + enc.encodeBuffer(this.authorityKeyID) + "\n");
        }
        if (this.certificateValid != null) {
            sb.append("  Certificate Valid: " + this.certificateValid + "\n");
        }
        if (this.privateKeyValid != null) {
            sb.append("  Private Key Valid: " + this.privateKeyValid + "\n");
        }
        if (this.subjectPublicKeyAlgID != null) {
            sb.append("  Subject Public Key AlgID: " + this.subjectPublicKeyAlgID + "\n");
        }
        if (this.subjectPublicKey != null) {
            sb.append("  Subject Public Key: " + this.subjectPublicKey + "\n");
        }
        if (this.keyUsage != null) {
            sb.append("  Key Usage: " + X509CertSelector.keyUsageToString(this.keyUsage) + "\n");
        }
        if (this.keyPurposeSet != null) {
            sb.append("  Extended Key Usage: " + this.keyPurposeSet + "\n");
        }
        if (this.policy != null) {
            sb.append("  Policy: " + this.policy + "\n");
        }
        if (this.pathToGeneralNames != null) {
            sb.append("  Path to names:\n");
            for (GeneralNameInterface pathToGeneralName : this.pathToGeneralNames) {
                sb.append("    " + pathToGeneralName + "\n");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String keyUsageToString(boolean[] k) {
        String s = "KeyUsage [\n";
        try {
            if (k[0]) {
                s = s + "  DigitalSignature\n";
            }
            if (k[1]) {
                s = s + "  Non_repudiation\n";
            }
            if (k[2]) {
                s = s + "  Key_Encipherment\n";
            }
            if (k[3]) {
                s = s + "  Data_Encipherment\n";
            }
            if (k[4]) {
                s = s + "  Key_Agreement\n";
            }
            if (k[5]) {
                s = s + "  Key_CertSign\n";
            }
            if (k[6]) {
                s = s + "  Crl_Sign\n";
            }
            if (k[7]) {
                s = s + "  Encipher_Only\n";
            }
            if (k[8]) {
                s = s + "  Decipher_Only\n";
            }
        }
        catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
            // empty catch block
        }
        s = s + "]\n";
        return s;
    }

    private static Extension getExtensionObject(X509Certificate cert, KnownOIDs extId) throws IOException {
        if (cert instanceof X509CertImpl) {
            X509CertImpl impl = (X509CertImpl)cert;
            return switch (extId) {
                case KnownOIDs.PrivateKeyUsage -> impl.getPrivateKeyUsageExtension();
                case KnownOIDs.SubjectAlternativeName -> impl.getSubjectAlternativeNameExtension();
                case KnownOIDs.NameConstraints -> impl.getNameConstraintsExtension();
                case KnownOIDs.CertificatePolicies -> impl.getCertificatePoliciesExtension();
                case KnownOIDs.extendedKeyUsage -> impl.getExtendedKeyUsageExtension();
                default -> null;
            };
        }
        byte[] rawExtVal = cert.getExtensionValue(extId.value());
        if (rawExtVal == null) {
            return null;
        }
        DerInputStream in = new DerInputStream(rawExtVal);
        byte[] encoded = in.getOctetString();
        switch (extId) {
            case PrivateKeyUsage: {
                try {
                    return new PrivateKeyUsageExtension(FALSE, encoded);
                }
                catch (CertificateException ex) {
                    throw new IOException(ex.getMessage());
                }
            }
            case SubjectAlternativeName: {
                return new SubjectAlternativeNameExtension(FALSE, encoded);
            }
            case NameConstraints: {
                return new NameConstraintsExtension(FALSE, encoded);
            }
            case CertificatePolicies: {
                return new CertificatePoliciesExtension(FALSE, encoded);
            }
            case extendedKeyUsage: {
                return new ExtendedKeyUsageExtension(FALSE, encoded);
            }
        }
        return null;
    }

    @Override
    public boolean match(Certificate cert) {
        boolean result;
        byte[] certKey;
        if (!(cert instanceof X509Certificate)) {
            return false;
        }
        X509Certificate xcert = (X509Certificate)cert;
        if (debug != null) {
            debug.println("X509CertSelector.match(Serial number: " + Debug.toString((BigInteger)xcert.getSerialNumber()) + "\n  Issuer: " + xcert.getIssuerX500Principal() + "\n  Subject: " + xcert.getSubjectX500Principal() + ")");
        }
        if (this.x509Cert != null && !this.x509Cert.equals(xcert)) {
            if (debug != null) {
                debug.println("X509CertSelector.match: certs don't match");
            }
            return false;
        }
        if (this.serialNumber != null && !this.serialNumber.equals(xcert.getSerialNumber())) {
            if (debug != null) {
                debug.println("X509CertSelector.match: serial numbers don't match");
            }
            return false;
        }
        if (this.issuer != null && !this.issuer.equals(xcert.getIssuerX500Principal())) {
            if (debug != null) {
                debug.println("X509CertSelector.match: issuer DNs don't match");
            }
            return false;
        }
        if (this.subject != null && !this.subject.equals(xcert.getSubjectX500Principal())) {
            if (debug != null) {
                debug.println("X509CertSelector.match: subject DNs don't match");
            }
            return false;
        }
        if (this.certificateValid != null) {
            try {
                xcert.checkValidity(this.certificateValid);
            }
            catch (CertificateException e) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: certificate not within validity period");
                }
                return false;
            }
        }
        if (this.subjectPublicKeyBytes != null && !Arrays.equals(this.subjectPublicKeyBytes, certKey = xcert.getPublicKey().getEncoded())) {
            if (debug != null) {
                debug.println("X509CertSelector.match: subject public keys don't match");
            }
            return false;
        }
        boolean bl = result = this.matchBasicConstraints(xcert) && this.matchKeyUsage(xcert) && this.matchExtendedKeyUsage(xcert) && this.matchSubjectKeyID(xcert) && this.matchAuthorityKeyID(xcert) && this.matchPrivateKeyValid(xcert) && this.matchSubjectPublicKeyAlgID(xcert) && this.matchPolicy(xcert) && this.matchSubjectAlternativeNames(xcert) && this.matchPathToNames(xcert) && this.matchNameConstraints(xcert);
        if (result && debug != null) {
            debug.println("X509CertSelector.match returning: true");
        }
        return result;
    }

    private boolean matchSubjectKeyID(X509Certificate xcert) {
        if (this.subjectKeyID == null) {
            return true;
        }
        try {
            byte[] extVal = xcert.getExtensionValue("2.5.29.14");
            if (extVal == null) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: no subject key ID extension");
                }
                return false;
            }
            DerInputStream in = new DerInputStream(extVal);
            byte[] certSubjectKeyID = in.getOctetString();
            if (certSubjectKeyID == null || !Arrays.equals(this.subjectKeyID, certSubjectKeyID)) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: subject key IDs don't match\nX509CertSelector.match: subjectKeyID: " + Arrays.toString(this.subjectKeyID) + "\nX509CertSelector.match: certSubjectKeyID: " + Arrays.toString(certSubjectKeyID));
                }
                return false;
            }
        }
        catch (IOException ex) {
            if (debug != null) {
                debug.println("X509CertSelector.match: exception in subject key ID check");
            }
            return false;
        }
        return true;
    }

    private boolean matchAuthorityKeyID(X509Certificate xcert) {
        if (this.authorityKeyID == null) {
            return true;
        }
        try {
            byte[] extVal = xcert.getExtensionValue("2.5.29.35");
            if (extVal == null) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: no authority key ID extension");
                }
                return false;
            }
            DerInputStream in = new DerInputStream(extVal);
            byte[] certAuthKeyID = in.getOctetString();
            if (certAuthKeyID == null || !Arrays.equals(this.authorityKeyID, certAuthKeyID)) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: authority key IDs don't match");
                }
                return false;
            }
        }
        catch (IOException ex) {
            if (debug != null) {
                debug.println("X509CertSelector.match: exception in authority key ID check");
            }
            return false;
        }
        return true;
    }

    private boolean matchPrivateKeyValid(X509Certificate xcert) {
        if (this.privateKeyValid == null) {
            return true;
        }
        PrivateKeyUsageExtension ext = null;
        try {
            ext = (PrivateKeyUsageExtension)X509CertSelector.getExtensionObject(xcert, KnownOIDs.PrivateKeyUsage);
            if (ext != null) {
                ext.valid(this.privateKeyValid);
            }
        }
        catch (CertificateExpiredException e1) {
            if (debug != null) {
                String time = "n/a";
                Date notAfter = ext.getNotAfter();
                time = notAfter.toString();
                debug.println("X509CertSelector.match: private key usage not within validity date; ext.NOT_After: " + time + "; X509CertSelector: " + this.toString());
                e1.printStackTrace();
            }
            return false;
        }
        catch (CertificateNotYetValidException e2) {
            if (debug != null) {
                String time = "n/a";
                Date notBefore = ext.getNotBefore();
                time = notBefore.toString();
                debug.println("X509CertSelector.match: private key usage not within validity date; ext.NOT_BEFORE: " + time + "; X509CertSelector: " + this);
                e2.printStackTrace();
            }
            return false;
        }
        catch (IOException e4) {
            if (debug != null) {
                debug.println("X509CertSelector.match: IOException in private key usage check; X509CertSelector: " + this);
                e4.printStackTrace();
            }
            return false;
        }
        return true;
    }

    private boolean matchSubjectPublicKeyAlgID(X509Certificate xcert) {
        if (this.subjectPublicKeyAlgID == null) {
            return true;
        }
        try {
            byte[] encodedKey = xcert.getPublicKey().getEncoded();
            DerValue val = new DerValue(encodedKey);
            if (val.tag != 48) {
                throw new IOException("invalid key format");
            }
            AlgorithmId algID = AlgorithmId.parse(val.data.getDerValue());
            if (debug != null) {
                debug.println("X509CertSelector.match: subjectPublicKeyAlgID = " + this.subjectPublicKeyAlgID + ", xcert subjectPublicKeyAlgID = " + algID.getOID());
            }
            if (!this.subjectPublicKeyAlgID.equals(algID.getOID())) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: subject public key alg IDs don't match");
                }
                return false;
            }
        }
        catch (IOException e5) {
            if (debug != null) {
                debug.println("X509CertSelector.match: IOException in subject public key algorithm OID check");
            }
            return false;
        }
        return true;
    }

    private boolean matchKeyUsage(X509Certificate xcert) {
        if (this.keyUsage == null) {
            return true;
        }
        boolean[] certKeyUsage = xcert.getKeyUsage();
        if (certKeyUsage != null) {
            for (int keyBit = 0; keyBit < this.keyUsage.length; ++keyBit) {
                if (!this.keyUsage[keyBit] || keyBit < certKeyUsage.length && certKeyUsage[keyBit]) continue;
                if (debug != null) {
                    debug.println("X509CertSelector.match: key usage bits don't match");
                }
                return false;
            }
        }
        return true;
    }

    private boolean matchExtendedKeyUsage(X509Certificate xcert) {
        if (this.keyPurposeSet == null || this.keyPurposeSet.isEmpty()) {
            return true;
        }
        try {
            Vector<ObjectIdentifier> certKeyPurposeVector;
            ExtendedKeyUsageExtension ext = (ExtendedKeyUsageExtension)X509CertSelector.getExtensionObject(xcert, KnownOIDs.extendedKeyUsage);
            if (ext != null && !(certKeyPurposeVector = ext.getUsages()).contains(ANY_EXTENDED_KEY_USAGE) && !certKeyPurposeVector.containsAll(this.keyPurposeOIDSet)) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: cert failed extendedKeyUsage criterion");
                }
                return false;
            }
        }
        catch (IOException ex) {
            if (debug != null) {
                debug.println("X509CertSelector.match: IOException in extended key usage check");
            }
            return false;
        }
        return true;
    }

    private boolean matchSubjectAlternativeNames(X509Certificate xcert) {
        if (this.subjectAlternativeNames == null || this.subjectAlternativeNames.isEmpty()) {
            return true;
        }
        try {
            SubjectAlternativeNameExtension sanExt = (SubjectAlternativeNameExtension)X509CertSelector.getExtensionObject(xcert, KnownOIDs.SubjectAlternativeName);
            if (sanExt == null) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: no subject alternative name extension");
                }
                return false;
            }
            GeneralNames certNames = sanExt.getNames();
            Iterator<GeneralNameInterface> i = this.subjectAlternativeGeneralNames.iterator();
            while (i.hasNext()) {
                GeneralNameInterface matchName = i.next();
                boolean found = false;
                Iterator<GeneralName> t = certNames.iterator();
                while (t.hasNext() && !found) {
                    GeneralNameInterface certName = t.next().getName();
                    found = certName.equals(matchName);
                }
                if (!(found || !this.matchAllSubjectAltNames && i.hasNext())) {
                    if (debug != null) {
                        debug.println("X509CertSelector.match: subject alternative name " + matchName + " not found");
                    }
                    return false;
                }
                if (!found || this.matchAllSubjectAltNames) continue;
                break;
            }
        }
        catch (IOException ex) {
            if (debug != null) {
                debug.println("X509CertSelector.match: IOException in subject alternative name check");
            }
            return false;
        }
        return true;
    }

    private boolean matchNameConstraints(X509Certificate xcert) {
        if (this.nc == null) {
            return true;
        }
        try {
            if (!this.nc.verify(xcert)) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: name constraints not satisfied");
                }
                return false;
            }
        }
        catch (IOException e) {
            if (debug != null) {
                debug.println("X509CertSelector.match: IOException in name constraints check");
            }
            return false;
        }
        return true;
    }

    private boolean matchPolicy(X509Certificate xcert) {
        if (this.policy == null) {
            return true;
        }
        try {
            CertificatePoliciesExtension ext = (CertificatePoliciesExtension)X509CertSelector.getExtensionObject(xcert, KnownOIDs.CertificatePolicies);
            if (ext == null) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: no certificate policy extension");
                }
                return false;
            }
            List<PolicyInformation> policies = ext.getCertPolicies();
            ArrayList<CertificatePolicyId> policyIDs = new ArrayList<CertificatePolicyId>(policies.size());
            for (PolicyInformation info : policies) {
                policyIDs.add(info.getPolicyIdentifier());
            }
            if (this.policy != null) {
                boolean foundOne = false;
                if (this.policy.getCertPolicyIds().isEmpty()) {
                    if (policyIDs.isEmpty()) {
                        if (debug != null) {
                            debug.println("X509CertSelector.match: cert failed policyAny criterion");
                        }
                        return false;
                    }
                } else {
                    for (CertificatePolicyId id : this.policy.getCertPolicyIds()) {
                        if (!policyIDs.contains(id)) continue;
                        foundOne = true;
                        break;
                    }
                    if (!foundOne) {
                        if (debug != null) {
                            debug.println("X509CertSelector.match: cert failed policyAny criterion");
                        }
                        return false;
                    }
                }
            }
        }
        catch (IOException ex) {
            if (debug != null) {
                debug.println("X509CertSelector.match: IOException in certificate policy ID check");
            }
            return false;
        }
        return true;
    }

    private boolean matchPathToNames(X509Certificate xcert) {
        if (this.pathToGeneralNames == null) {
            return true;
        }
        try {
            NameConstraintsExtension ext = (NameConstraintsExtension)X509CertSelector.getExtensionObject(xcert, KnownOIDs.NameConstraints);
            if (ext == null) {
                return true;
            }
            if (debug != null && Debug.isOn("certpath")) {
                debug.println("X509CertSelector.match pathToNames:\n");
                for (GeneralNameInterface pathToGeneralName : this.pathToGeneralNames) {
                    debug.println("    " + pathToGeneralName + "\n");
                }
            }
            GeneralSubtrees permitted = ext.getPermittedSubtrees();
            GeneralSubtrees excluded = ext.getExcludedSubtrees();
            if (excluded != null && !this.matchExcluded(excluded)) {
                return false;
            }
            if (permitted != null && !this.matchPermitted(permitted)) {
                return false;
            }
        }
        catch (IOException ex) {
            if (debug != null) {
                debug.println("X509CertSelector.match: IOException in name constraints check");
            }
            return false;
        }
        return true;
    }

    private boolean matchExcluded(GeneralSubtrees excluded) {
        Iterator<GeneralSubtree> t = excluded.iterator();
        while (t.hasNext()) {
            GeneralSubtree tree = t.next();
            GeneralNameInterface excludedName = tree.getName().getName();
            for (GeneralNameInterface pathToName : this.pathToGeneralNames) {
                if (excludedName.getType() != pathToName.getType()) continue;
                switch (pathToName.constrains(excludedName)) {
                    case 0: 
                    case 2: {
                        if (debug != null) {
                            debug.println("X509CertSelector.match: name constraints inhibit path to specified name");
                            debug.println("X509CertSelector.match: excluded name: " + pathToName);
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean matchPermitted(GeneralSubtrees permitted) {
        for (GeneralNameInterface pathToName : this.pathToGeneralNames) {
            Iterator<GeneralSubtree> t = permitted.iterator();
            boolean permittedNameFound = false;
            boolean nameTypeFound = false;
            String names = "";
            while (t.hasNext() && !permittedNameFound) {
                GeneralSubtree tree = t.next();
                GeneralNameInterface permittedName = tree.getName().getName();
                if (permittedName.getType() != pathToName.getType()) continue;
                nameTypeFound = true;
                names = names + "  " + permittedName;
                switch (pathToName.constrains(permittedName)) {
                    case 0: 
                    case 2: {
                        permittedNameFound = true;
                        break;
                    }
                }
            }
            if (permittedNameFound || !nameTypeFound) continue;
            if (debug != null) {
                debug.println("X509CertSelector.match: name constraints inhibit path to specified name; permitted names of type " + pathToName.getType() + ": " + names);
            }
            return false;
        }
        return true;
    }

    private boolean matchBasicConstraints(X509Certificate xcert) {
        if (this.basicConstraints == -1) {
            return true;
        }
        int maxPathLen = xcert.getBasicConstraints();
        if (this.basicConstraints == -2) {
            if (maxPathLen != -1) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: not an EE cert");
                }
                return false;
            }
        } else if (maxPathLen < this.basicConstraints) {
            if (debug != null) {
                debug.println("X509CertSelector.match: cert's maxPathLen is less than the min maxPathLen set by basicConstraints. (" + maxPathLen + " < " + this.basicConstraints + ")");
            }
            return false;
        }
        return true;
    }

    private static <T> Set<T> cloneSet(Set<T> set) {
        if (set instanceof HashSet) {
            Object clone = ((HashSet)set).clone();
            return (Set)clone;
        }
        return new HashSet<T>(set);
    }

    @Override
    public Object clone() {
        try {
            X509CertSelector copy = (X509CertSelector)super.clone();
            if (this.subjectAlternativeNames != null) {
                copy.subjectAlternativeNames = X509CertSelector.cloneSet(this.subjectAlternativeNames);
                copy.subjectAlternativeGeneralNames = X509CertSelector.cloneSet(this.subjectAlternativeGeneralNames);
            }
            if (this.pathToGeneralNames != null) {
                copy.pathToNames = X509CertSelector.cloneSet(this.pathToNames);
                copy.pathToGeneralNames = X509CertSelector.cloneSet(this.pathToGeneralNames);
            }
            return copy;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString(), e);
        }
    }
}

