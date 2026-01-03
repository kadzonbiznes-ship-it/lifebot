/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.X500Name;

class AVAKeyword {
    private static final Map<ObjectIdentifier, AVAKeyword> oidMap = new HashMap<ObjectIdentifier, AVAKeyword>();
    private static final Map<String, AVAKeyword> keywordMap = new HashMap<String, AVAKeyword>();
    private final String keyword;
    private final ObjectIdentifier oid;
    private final boolean rfc1779Compliant;
    private final boolean rfc2253Compliant;

    private AVAKeyword(String keyword, ObjectIdentifier oid, boolean rfc1779Compliant, boolean rfc2253Compliant) {
        this.keyword = keyword;
        this.oid = oid;
        this.rfc1779Compliant = rfc1779Compliant;
        this.rfc2253Compliant = rfc2253Compliant;
        oidMap.put(oid, this);
        keywordMap.put(keyword, this);
    }

    private boolean isCompliant(int standard) {
        switch (standard) {
            case 2: {
                return this.rfc1779Compliant;
            }
            case 3: {
                return this.rfc2253Compliant;
            }
            case 1: {
                return true;
            }
        }
        throw new IllegalArgumentException("Invalid standard " + standard);
    }

    static ObjectIdentifier getOID(String keyword, int standard, Map<String, String> extraKeywordMap) throws IOException {
        char ch;
        String oidString;
        keyword = keyword.toUpperCase(Locale.ENGLISH);
        if (standard == 3) {
            if (keyword.startsWith(" ") || keyword.endsWith(" ")) {
                throw new IOException("Invalid leading or trailing space in keyword \"" + keyword + "\"");
            }
        } else {
            keyword = keyword.trim();
        }
        if ((oidString = extraKeywordMap.get(keyword)) == null) {
            AVAKeyword ak = keywordMap.get(keyword);
            if (ak != null && ak.isCompliant(standard)) {
                return ak.oid;
            }
        } else {
            return ObjectIdentifier.of(oidString);
        }
        if (standard == 1 && keyword.startsWith("OID.")) {
            keyword = keyword.substring(4);
        }
        boolean number = false;
        if (!keyword.isEmpty() && (ch = keyword.charAt(0)) >= '0' && ch <= '9') {
            number = true;
        }
        if (!number) {
            throw new IOException("Invalid keyword \"" + keyword + "\"");
        }
        return ObjectIdentifier.of(keyword);
    }

    static String getKeyword(ObjectIdentifier oid, int standard) {
        return AVAKeyword.getKeyword(oid, standard, Collections.emptyMap());
    }

    static String getKeyword(ObjectIdentifier oid, int standard, Map<String, String> extraOidMap) {
        String oidString = oid.toString();
        String keywordString = extraOidMap.get(oidString);
        if (keywordString == null) {
            AVAKeyword ak = oidMap.get(oid);
            if (ak != null && ak.isCompliant(standard)) {
                return ak.keyword;
            }
        } else {
            if (keywordString.isEmpty()) {
                throw new IllegalArgumentException("keyword cannot be empty");
            }
            char c = (keywordString = keywordString.trim()).charAt(0);
            if (c < 'A' || c > 'z' || c > 'Z' && c < 'a') {
                throw new IllegalArgumentException("keyword does not start with letter");
            }
            for (int i = 1; i < keywordString.length(); ++i) {
                c = keywordString.charAt(i);
                if (c >= 'A' && c <= 'z' && (c <= 'Z' || c >= 'a') || c >= '0' && c <= '9' || c == '_') continue;
                throw new IllegalArgumentException("keyword character is not a letter, digit, or underscore");
            }
            return keywordString;
        }
        if (standard == 3) {
            return oidString;
        }
        return "OID." + oidString;
    }

    static boolean hasKeyword(ObjectIdentifier oid, int standard) {
        AVAKeyword ak = oidMap.get(oid);
        if (ak == null) {
            return false;
        }
        return ak.isCompliant(standard);
    }

    static {
        new AVAKeyword("CN", X500Name.commonName_oid, true, true);
        new AVAKeyword("C", X500Name.countryName_oid, true, true);
        new AVAKeyword("L", X500Name.localityName_oid, true, true);
        new AVAKeyword("S", X500Name.stateName_oid, false, false);
        new AVAKeyword("ST", X500Name.stateName_oid, true, true);
        new AVAKeyword("O", X500Name.orgName_oid, true, true);
        new AVAKeyword("OU", X500Name.orgUnitName_oid, true, true);
        new AVAKeyword("T", X500Name.title_oid, false, false);
        new AVAKeyword("IP", X500Name.ipAddress_oid, false, false);
        new AVAKeyword("STREET", X500Name.streetAddress_oid, true, true);
        new AVAKeyword("DC", X500Name.DOMAIN_COMPONENT_OID, false, true);
        new AVAKeyword("DNQUALIFIER", X500Name.DNQUALIFIER_OID, false, false);
        new AVAKeyword("DNQ", X500Name.DNQUALIFIER_OID, false, false);
        new AVAKeyword("SURNAME", X500Name.SURNAME_OID, false, false);
        new AVAKeyword("GIVENNAME", X500Name.GIVENNAME_OID, false, false);
        new AVAKeyword("INITIALS", X500Name.INITIALS_OID, false, false);
        new AVAKeyword("GENERATION", X500Name.GENERATIONQUALIFIER_OID, false, false);
        new AVAKeyword("EMAIL", PKCS9Attribute.EMAIL_ADDRESS_OID, false, false);
        new AVAKeyword("EMAILADDRESS", PKCS9Attribute.EMAIL_ADDRESS_OID, false, false);
        new AVAKeyword("UID", X500Name.userid_oid, false, true);
        new AVAKeyword("SERIALNUMBER", X500Name.SERIALNUMBER_OID, false, false);
    }
}

