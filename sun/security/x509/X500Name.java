/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.security.auth.x500.X500Principal;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AVA;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.RDN;

public class X500Name
implements GeneralNameInterface,
Principal {
    private String dn;
    private String rfc1779Dn;
    private String rfc2253Dn;
    private String canonicalDn;
    private RDN[] names;
    private X500Principal x500Principal;
    private byte[] encoded;
    private volatile List<RDN> rdnList;
    private volatile List<AVA> allAvaList;
    public static final ObjectIdentifier commonName_oid = ObjectIdentifier.of(KnownOIDs.CommonName);
    public static final ObjectIdentifier SURNAME_OID = ObjectIdentifier.of(KnownOIDs.Surname);
    public static final ObjectIdentifier SERIALNUMBER_OID = ObjectIdentifier.of(KnownOIDs.SerialNumber);
    public static final ObjectIdentifier countryName_oid = ObjectIdentifier.of(KnownOIDs.CountryName);
    public static final ObjectIdentifier localityName_oid = ObjectIdentifier.of(KnownOIDs.LocalityName);
    public static final ObjectIdentifier stateName_oid = ObjectIdentifier.of(KnownOIDs.StateName);
    public static final ObjectIdentifier streetAddress_oid = ObjectIdentifier.of(KnownOIDs.StreetAddress);
    public static final ObjectIdentifier orgName_oid = ObjectIdentifier.of(KnownOIDs.OrgName);
    public static final ObjectIdentifier orgUnitName_oid = ObjectIdentifier.of(KnownOIDs.OrgUnitName);
    public static final ObjectIdentifier title_oid = ObjectIdentifier.of(KnownOIDs.Title);
    public static final ObjectIdentifier GIVENNAME_OID = ObjectIdentifier.of(KnownOIDs.GivenName);
    public static final ObjectIdentifier INITIALS_OID = ObjectIdentifier.of(KnownOIDs.Initials);
    public static final ObjectIdentifier GENERATIONQUALIFIER_OID = ObjectIdentifier.of(KnownOIDs.GenerationQualifier);
    public static final ObjectIdentifier DNQUALIFIER_OID = ObjectIdentifier.of(KnownOIDs.DNQualifier);
    public static final ObjectIdentifier ipAddress_oid = ObjectIdentifier.of(KnownOIDs.SkipIPAddress);
    public static final ObjectIdentifier DOMAIN_COMPONENT_OID = ObjectIdentifier.of(KnownOIDs.UCL_DomainComponent);
    public static final ObjectIdentifier userid_oid = ObjectIdentifier.of(KnownOIDs.UCL_UserID);
    private static final Constructor<X500Principal> principalConstructor;
    private static final Field principalField;

    public X500Name(String dname) throws IOException {
        this(dname, Collections.emptyMap());
    }

    public X500Name(String dname, Map<String, String> keywordMap) throws IOException {
        this.parseDN(dname, keywordMap);
    }

    public X500Name(String dname, String format) throws IOException {
        if (dname == null) {
            throw new NullPointerException("Name must not be null");
        }
        if (format.equalsIgnoreCase("RFC2253")) {
            this.parseRFC2253DN(dname);
        } else if (format.equalsIgnoreCase("DEFAULT")) {
            this.parseDN(dname, Collections.emptyMap());
        } else {
            throw new IOException("Unsupported format " + format);
        }
    }

    public X500Name(String commonName, String organizationUnit, String organizationName, String country) throws IOException {
        this.names = new RDN[4];
        this.names[3] = new RDN(1);
        this.names[3].assertion[0] = new AVA(commonName_oid, new DerValue(commonName));
        this.names[2] = new RDN(1);
        this.names[2].assertion[0] = new AVA(orgUnitName_oid, new DerValue(organizationUnit));
        this.names[1] = new RDN(1);
        this.names[1].assertion[0] = new AVA(orgName_oid, new DerValue(organizationName));
        this.names[0] = new RDN(1);
        this.names[0].assertion[0] = new AVA(countryName_oid, new DerValue(country));
    }

    public X500Name(String commonName, String organizationUnit, String organizationName, String localityName, String stateName, String country) throws IOException {
        RDN name;
        ArrayList<RDN> list = new ArrayList<RDN>(6);
        if (country != null) {
            name = new RDN(1);
            name.assertion[0] = new AVA(countryName_oid, new DerValue(country));
            list.add(name);
        }
        if (stateName != null) {
            name = new RDN(1);
            name.assertion[0] = new AVA(stateName_oid, new DerValue(stateName));
            list.add(name);
        }
        if (localityName != null) {
            name = new RDN(1);
            name.assertion[0] = new AVA(localityName_oid, new DerValue(localityName));
            list.add(name);
        }
        if (organizationName != null) {
            name = new RDN(1);
            name.assertion[0] = new AVA(orgName_oid, new DerValue(organizationName));
            list.add(name);
        }
        if (organizationUnit != null) {
            name = new RDN(1);
            name.assertion[0] = new AVA(orgUnitName_oid, new DerValue(organizationUnit));
            list.add(name);
        }
        if (commonName != null) {
            name = new RDN(1);
            name.assertion[0] = new AVA(commonName_oid, new DerValue(commonName));
            list.add(name);
        }
        this.names = list.toArray(new RDN[0]);
    }

    public X500Name(RDN[] rdnArray) throws IOException {
        if (rdnArray == null) {
            this.names = new RDN[0];
        } else {
            this.names = (RDN[])rdnArray.clone();
            for (int i = 0; i < this.names.length; ++i) {
                if (this.names[i] != null) continue;
                throw new IOException("Cannot create an X500Name");
            }
        }
    }

    public X500Name(DerValue value) throws IOException {
        this(value.toDerInputStream());
    }

    public X500Name(DerInputStream in) throws IOException {
        this.parseDER(in);
    }

    public X500Name(byte[] name) throws IOException {
        DerInputStream in = new DerInputStream(name);
        this.parseDER(in);
    }

    public List<RDN> rdns() {
        List<RDN> list = this.rdnList;
        if (list == null) {
            this.rdnList = list = Collections.unmodifiableList(Arrays.asList(this.names));
        }
        return list;
    }

    public int size() {
        return this.names.length;
    }

    public List<AVA> allAvas() {
        List<AVA> list = this.allAvaList;
        if (list == null) {
            list = new ArrayList<AVA>();
            for (int i = 0; i < this.names.length; ++i) {
                list.addAll(this.names[i].avas());
            }
            this.allAvaList = list = Collections.unmodifiableList(list);
        }
        return list;
    }

    public int avaSize() {
        return this.allAvas().size();
    }

    public boolean isEmpty() {
        int n = this.names.length;
        for (int i = 0; i < n; ++i) {
            if (this.names[i].assertion.length == 0) continue;
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.getRFC2253CanonicalName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof X500Name)) {
            return false;
        }
        X500Name other = (X500Name)obj;
        if (this.canonicalDn != null && other.canonicalDn != null) {
            return this.canonicalDn.equals(other.canonicalDn);
        }
        int n = this.names.length;
        if (n != other.names.length) {
            return false;
        }
        for (int i = 0; i < n; ++i) {
            RDN r1 = this.names[i];
            RDN r2 = other.names[i];
            if (r1.assertion.length == r2.assertion.length) continue;
            return false;
        }
        String thisCanonical = this.getRFC2253CanonicalName();
        String otherCanonical = other.getRFC2253CanonicalName();
        return thisCanonical.equals(otherCanonical);
    }

    private String getString(DerValue attribute) throws IOException {
        if (attribute == null) {
            return null;
        }
        String value = attribute.getAsString();
        if (value == null) {
            throw new IOException("not a DER string encoding, " + attribute.tag);
        }
        return value;
    }

    @Override
    public int getType() {
        return 4;
    }

    public String getCountry() throws IOException {
        DerValue attr = this.findAttribute(countryName_oid);
        return this.getString(attr);
    }

    public String getOrganization() throws IOException {
        DerValue attr = this.findAttribute(orgName_oid);
        return this.getString(attr);
    }

    public String getOrganizationalUnit() throws IOException {
        DerValue attr = this.findAttribute(orgUnitName_oid);
        return this.getString(attr);
    }

    public String getCommonName() throws IOException {
        DerValue attr = this.findAttribute(commonName_oid);
        return this.getString(attr);
    }

    public String getLocality() throws IOException {
        DerValue attr = this.findAttribute(localityName_oid);
        return this.getString(attr);
    }

    public String getState() throws IOException {
        DerValue attr = this.findAttribute(stateName_oid);
        return this.getString(attr);
    }

    public String getDomain() throws IOException {
        DerValue attr = this.findAttribute(DOMAIN_COMPONENT_OID);
        return this.getString(attr);
    }

    public String getDNQualifier() throws IOException {
        DerValue attr = this.findAttribute(DNQUALIFIER_OID);
        return this.getString(attr);
    }

    public String getSurname() throws IOException {
        DerValue attr = this.findAttribute(SURNAME_OID);
        return this.getString(attr);
    }

    public String getGivenName() throws IOException {
        DerValue attr = this.findAttribute(GIVENNAME_OID);
        return this.getString(attr);
    }

    public String getInitials() throws IOException {
        DerValue attr = this.findAttribute(INITIALS_OID);
        return this.getString(attr);
    }

    public String getGeneration() throws IOException {
        DerValue attr = this.findAttribute(GENERATIONQUALIFIER_OID);
        return this.getString(attr);
    }

    public String getIP() throws IOException {
        DerValue attr = this.findAttribute(ipAddress_oid);
        return this.getString(attr);
    }

    @Override
    public String toString() {
        if (this.dn == null) {
            this.generateDN();
        }
        return this.dn;
    }

    public String getRFC1779Name() {
        return this.getRFC1779Name(Collections.emptyMap());
    }

    public String getRFC1779Name(Map<String, String> oidMap) throws IllegalArgumentException {
        if (oidMap.isEmpty()) {
            if (this.rfc1779Dn == null) {
                this.rfc1779Dn = this.generateRFC1779DN(oidMap);
            }
            return this.rfc1779Dn;
        }
        return this.generateRFC1779DN(oidMap);
    }

    public String getRFC2253Name() {
        return this.getRFC2253Name(Collections.emptyMap());
    }

    public String getRFC2253Name(Map<String, String> oidMap) {
        if (oidMap.isEmpty()) {
            if (this.rfc2253Dn == null) {
                this.rfc2253Dn = this.generateRFC2253DN(oidMap);
            }
            return this.rfc2253Dn;
        }
        return this.generateRFC2253DN(oidMap);
    }

    private String generateRFC2253DN(Map<String, String> oidMap) {
        if (this.names.length == 0) {
            return "";
        }
        StringJoiner sj = new StringJoiner(",");
        for (int i = this.names.length - 1; i >= 0; --i) {
            sj.add(this.names[i].toRFC2253String(oidMap));
        }
        return sj.toString();
    }

    public String getRFC2253CanonicalName() {
        if (this.canonicalDn != null) {
            return this.canonicalDn;
        }
        if (this.names.length == 0) {
            this.canonicalDn = "";
            return this.canonicalDn;
        }
        StringJoiner sj = new StringJoiner(",");
        for (int i = this.names.length - 1; i >= 0; --i) {
            sj.add(this.names[i].toRFC2253String(true));
        }
        this.canonicalDn = sj.toString();
        return this.canonicalDn;
    }

    @Override
    public String getName() {
        return this.toString();
    }

    private DerValue findAttribute(ObjectIdentifier attribute) {
        if (this.names != null) {
            for (int i = 0; i < this.names.length; ++i) {
                DerValue value = this.names[i].findAttribute(attribute);
                if (value == null) continue;
                return value;
            }
        }
        return null;
    }

    public DerValue findMostSpecificAttribute(ObjectIdentifier attribute) {
        if (this.names != null) {
            for (int i = this.names.length - 1; i >= 0; --i) {
                DerValue value = this.names[i].findAttribute(attribute);
                if (value == null) continue;
                return value;
            }
        }
        return null;
    }

    private void parseDER(DerInputStream in) throws IOException {
        DerValue[] nameseq;
        byte[] derBytes = in.toByteArray();
        try {
            nameseq = in.getSequence(5);
        }
        catch (IOException ioe) {
            if (derBytes == null) {
                nameseq = null;
            }
            DerValue derVal = new DerValue(48, derBytes);
            derBytes = derVal.toByteArray();
            nameseq = new DerInputStream(derBytes).getSequence(5);
        }
        if (nameseq == null) {
            this.names = new RDN[0];
        } else {
            this.names = new RDN[nameseq.length];
            for (int i = 0; i < nameseq.length; ++i) {
                this.names[i] = new RDN(nameseq[i]);
            }
        }
    }

    @Deprecated
    public void emit(DerOutputStream out) throws IOException {
        this.encode(out);
    }

    @Override
    public void encode(DerOutputStream out) {
        DerOutputStream tmp = new DerOutputStream();
        for (int i = 0; i < this.names.length; ++i) {
            this.names[i].encode(tmp);
        }
        out.write((byte)48, tmp);
    }

    public byte[] getEncodedInternal() throws IOException {
        if (this.encoded == null) {
            DerOutputStream out = new DerOutputStream();
            DerOutputStream tmp = new DerOutputStream();
            for (int i = 0; i < this.names.length; ++i) {
                this.names[i].encode(tmp);
            }
            out.write((byte)48, tmp);
            this.encoded = out.toByteArray();
        }
        return this.encoded;
    }

    public byte[] getEncoded() throws IOException {
        return (byte[])this.getEncodedInternal().clone();
    }

    private void parseDN(String input, Map<String, String> keywordMap) throws IOException {
        RDN rdn;
        String rdnString;
        if (input == null || input.isEmpty()) {
            this.names = new RDN[0];
            return;
        }
        ArrayList<RDN> dnVector = new ArrayList<RDN>();
        int dnOffset = 0;
        int quoteCount = 0;
        String dnString = input;
        int searchOffset = 0;
        int nextComma = dnString.indexOf(44);
        int nextSemiColon = dnString.indexOf(59);
        while (nextComma >= 0 || nextSemiColon >= 0) {
            int rdnEnd = nextSemiColon < 0 ? nextComma : (nextComma < 0 ? nextSemiColon : Math.min(nextComma, nextSemiColon));
            if (rdnEnd >= 0 && (quoteCount += X500Name.countQuotes(dnString, searchOffset, rdnEnd)) != 1 && !X500Name.escaped(rdnEnd, searchOffset, dnString)) {
                rdnString = dnString.substring(dnOffset, rdnEnd);
                rdn = new RDN(rdnString, keywordMap);
                dnVector.add(rdn);
                dnOffset = rdnEnd + 1;
                quoteCount = 0;
            }
            searchOffset = rdnEnd + 1;
            nextComma = dnString.indexOf(44, searchOffset);
            nextSemiColon = dnString.indexOf(59, searchOffset);
        }
        rdnString = dnString.substring(dnOffset);
        rdn = new RDN(rdnString, keywordMap);
        dnVector.add(rdn);
        Collections.reverse(dnVector);
        this.names = dnVector.toArray(new RDN[0]);
    }

    private void parseRFC2253DN(String dnString) throws IOException {
        RDN rdn;
        String rdnString;
        if (dnString.isEmpty()) {
            this.names = new RDN[0];
            return;
        }
        ArrayList<RDN> dnVector = new ArrayList<RDN>();
        int dnOffset = 0;
        int searchOffset = 0;
        int rdnEnd = dnString.indexOf(44);
        while (rdnEnd >= 0) {
            if (rdnEnd > 0 && !X500Name.escaped(rdnEnd, searchOffset, dnString)) {
                rdnString = dnString.substring(dnOffset, rdnEnd);
                rdn = new RDN(rdnString, "RFC2253");
                dnVector.add(rdn);
                dnOffset = rdnEnd + 1;
            }
            searchOffset = rdnEnd + 1;
            rdnEnd = dnString.indexOf(44, searchOffset);
        }
        rdnString = dnString.substring(dnOffset);
        rdn = new RDN(rdnString, "RFC2253");
        dnVector.add(rdn);
        Collections.reverse(dnVector);
        this.names = dnVector.toArray(new RDN[0]);
    }

    static int countQuotes(String string, int from, int to) {
        int count = 0;
        for (int i = from; i < to; ++i) {
            if ((string.charAt(i) != '\"' || i != from) && (string.charAt(i) != '\"' || string.charAt(i - 1) == '\\')) continue;
            ++count;
        }
        return count;
    }

    private static boolean escaped(int rdnEnd, int searchOffset, String dnString) {
        if (rdnEnd == 1 && dnString.charAt(0) == '\\') {
            return true;
        }
        if (rdnEnd > 1 && dnString.charAt(rdnEnd - 1) == '\\' && dnString.charAt(rdnEnd - 2) != '\\') {
            return true;
        }
        if (rdnEnd > 1 && dnString.charAt(rdnEnd - 1) == '\\' && dnString.charAt(rdnEnd - 2) == '\\') {
            int count = 0;
            --rdnEnd;
            while (rdnEnd >= searchOffset) {
                if (dnString.charAt(rdnEnd) == '\\') {
                    ++count;
                }
                --rdnEnd;
            }
            return count % 2 != 0;
        }
        return false;
    }

    private void generateDN() {
        if (this.names.length == 1) {
            this.dn = this.names[0].toString();
            return;
        }
        StringJoiner sj = new StringJoiner(", ");
        for (int i = this.names.length - 1; i >= 0; --i) {
            sj.add(this.names[i].toString());
        }
        this.dn = sj.toString();
    }

    private String generateRFC1779DN(Map<String, String> oidMap) {
        if (this.names.length == 1) {
            return this.names[0].toRFC1779String(oidMap);
        }
        StringJoiner sj = new StringJoiner(", ");
        for (int i = this.names.length - 1; i >= 0; --i) {
            sj.add(this.names[i].toRFC1779String(oidMap));
        }
        return sj.toString();
    }

    @Override
    public int constrains(GeneralNameInterface inputName) throws UnsupportedOperationException {
        X500Name inputX500;
        int constraintType = inputName == null ? -1 : (inputName.getType() != 4 ? -1 : ((inputX500 = (X500Name)inputName).equals(this) ? 0 : (inputX500.names.length == 0 ? 2 : (this.names.length == 0 ? 1 : (inputX500.isWithinSubtree(this) ? 1 : (this.isWithinSubtree(inputX500) ? 2 : 3))))));
        return constraintType;
    }

    private boolean isWithinSubtree(X500Name other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other.names.length == 0) {
            return true;
        }
        if (this.names.length == 0) {
            return false;
        }
        if (this.names.length < other.names.length) {
            return false;
        }
        for (int i = 0; i < other.names.length; ++i) {
            if (this.names[i].equals(other.names[i])) continue;
            return false;
        }
        return true;
    }

    @Override
    public int subtreeDepth() throws UnsupportedOperationException {
        return this.names.length;
    }

    public X500Name commonAncestor(X500Name other) {
        X500Name commonAncestor;
        int i;
        if (other == null) {
            return null;
        }
        int otherLen = other.names.length;
        int thisLen = this.names.length;
        if (thisLen == 0 || otherLen == 0) {
            return null;
        }
        int minLen = Math.min(thisLen, otherLen);
        for (i = 0; i < minLen; ++i) {
            if (this.names[i].equals(other.names[i])) continue;
            if (i != 0) break;
            return null;
        }
        RDN[] ancestor = new RDN[i];
        System.arraycopy(this.names, 0, ancestor, 0, i);
        try {
            commonAncestor = new X500Name(ancestor);
        }
        catch (IOException ioe) {
            return null;
        }
        return commonAncestor;
    }

    public X500Principal asX500Principal() {
        if (this.x500Principal == null) {
            try {
                Object[] args = new Object[]{this};
                this.x500Principal = principalConstructor.newInstance(args);
            }
            catch (Exception e) {
                throw new RuntimeException("Unexpected exception", e);
            }
        }
        return this.x500Principal;
    }

    public static X500Name asX500Name(X500Principal p) {
        try {
            X500Name name = (X500Name)principalField.get(p);
            name.x500Principal = p;
            return name;
        }
        catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    static {
        PrivilegedExceptionAction<Object[]> pa = () -> {
            Class<X500Principal> pClass = X500Principal.class;
            Class[] args = new Class[]{X500Name.class};
            Constructor cons = pClass.getDeclaredConstructor(args);
            cons.setAccessible(true);
            Field field = pClass.getDeclaredField("thisX500Name");
            field.setAccessible(true);
            return new Object[]{cons, field};
        };
        try {
            Constructor constr;
            Object[] result = AccessController.doPrivileged(pa);
            principalConstructor = constr = (Constructor)result[0];
            principalField = (Field)result[1];
        }
        catch (Exception e) {
            throw new InternalError("Could not obtain X500Principal access", e);
        }
    }
}

