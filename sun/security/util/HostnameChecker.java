/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.IOException;
import java.net.IDN;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.Normalizer;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SNIHostName;
import javax.security.auth.x500.X500Principal;
import sun.net.util.IPAddressUtil;
import sun.security.ssl.SSLLogger;
import sun.security.util.DerValue;
import sun.security.util.RegisteredDomain;
import sun.security.x509.X500Name;

public class HostnameChecker {
    public static final byte TYPE_TLS = 1;
    private static final HostnameChecker INSTANCE_TLS = new HostnameChecker(1);
    public static final byte TYPE_LDAP = 2;
    private static final HostnameChecker INSTANCE_LDAP = new HostnameChecker(2);
    private static final int ALTNAME_DNS = 2;
    private static final int ALTNAME_IP = 7;
    private final byte checkType;

    private HostnameChecker(byte checkType) {
        this.checkType = checkType;
    }

    public static HostnameChecker getInstance(byte checkType) {
        if (checkType == 1) {
            return INSTANCE_TLS;
        }
        if (checkType == 2) {
            return INSTANCE_LDAP;
        }
        throw new IllegalArgumentException("Unknown check type: " + checkType);
    }

    public void match(String expectedName, X509Certificate cert, boolean chainsToPublicCA) throws CertificateException {
        if (expectedName == null) {
            throw new CertificateException("Hostname or IP address is undefined.");
        }
        if (HostnameChecker.isIpAddress(expectedName)) {
            HostnameChecker.matchIP(expectedName, cert);
        } else {
            this.matchDNS(expectedName, cert, chainsToPublicCA);
        }
    }

    public void match(String expectedName, X509Certificate cert) throws CertificateException {
        this.match(expectedName, cert, false);
    }

    private static boolean isIpAddress(String name) {
        return IPAddressUtil.isIPv4LiteralAddress(name) || IPAddressUtil.isIPv6LiteralAddress(name);
    }

    private static void matchIP(String expectedIP, X509Certificate cert) throws CertificateException {
        Collection<List<?>> subjAltNames = cert.getSubjectAlternativeNames();
        if (subjAltNames == null) {
            throw new CertificateException("No subject alternative names present");
        }
        for (List<?> next : subjAltNames) {
            if ((Integer)next.get(0) != 7) continue;
            String ipAddress = (String)next.get(1);
            if (expectedIP.equalsIgnoreCase(ipAddress)) {
                return;
            }
            try {
                if (!InetAddress.getByName(expectedIP).equals(InetAddress.getByName(ipAddress))) continue;
                return;
            }
            catch (SecurityException | UnknownHostException exception) {
            }
        }
        throw new CertificateException("No subject alternative names matching IP address " + expectedIP + " found");
    }

    private void matchDNS(String expectedName, X509Certificate cert, boolean chainsToPublicCA) throws CertificateException {
        X500Name subjectName;
        DerValue derValue;
        try {
            SNIHostName sNIHostName = new SNIHostName(expectedName);
        }
        catch (IllegalArgumentException iae) {
            throw new CertificateException("Illegal given domain name: " + expectedName, iae);
        }
        Collection<List<?>> subjAltNames = cert.getSubjectAlternativeNames();
        if (subjAltNames != null) {
            boolean foundDNS = false;
            for (List<?> next : subjAltNames) {
                if ((Integer)next.get(0) != 2) continue;
                foundDNS = true;
                String dnsName = (String)next.get(1);
                if (!this.isMatched(expectedName, dnsName, chainsToPublicCA)) continue;
                return;
            }
            if (foundDNS) {
                throw new CertificateException("No subject alternative DNS name matching " + expectedName + " found.");
            }
        }
        if ((derValue = (subjectName = HostnameChecker.getSubjectX500Name(cert)).findMostSpecificAttribute(X500Name.commonName_oid)) != null) {
            try {
                String cname = derValue.getAsString();
                if (!Normalizer.isNormalized(cname, Normalizer.Form.NFKC)) {
                    throw new CertificateException("Not a formal name " + cname);
                }
                if (this.isMatched(expectedName, cname, chainsToPublicCA)) {
                    return;
                }
            }
            catch (IOException cname) {
                // empty catch block
            }
        }
        String msg = "No name matching " + expectedName + " found";
        throw new CertificateException(msg);
    }

    public static X500Name getSubjectX500Name(X509Certificate cert) throws CertificateParsingException {
        try {
            Principal subjectDN = cert.getSubjectDN();
            if (subjectDN instanceof X500Name) {
                return (X500Name)subjectDN;
            }
            X500Principal subjectX500 = cert.getSubjectX500Principal();
            return new X500Name(subjectX500.getEncoded());
        }
        catch (IOException e) {
            throw new CertificateParsingException(e);
        }
    }

    private boolean isMatched(String name, String template, boolean chainsToPublicCA) {
        try {
            name = IDN.toUnicode(IDN.toASCII(name));
            template = IDN.toUnicode(IDN.toASCII(template));
        }
        catch (RuntimeException re) {
            if (SSLLogger.isOn) {
                SSLLogger.fine("Failed to normalize to Unicode: " + re, new Object[0]);
            }
            return false;
        }
        if (HostnameChecker.hasIllegalWildcard(template, chainsToPublicCA)) {
            return false;
        }
        try {
            new SNIHostName(template.replace('*', 'z'));
        }
        catch (IllegalArgumentException iae) {
            return false;
        }
        if (this.checkType == 1 || this.checkType == 2) {
            return HostnameChecker.matchLeftmostWildcard(name, template);
        }
        return false;
    }

    private static boolean hasIllegalWildcard(String template, boolean chainsToPublicCA) {
        if (template.equals("*") || template.equals("*.")) {
            if (SSLLogger.isOn) {
                SSLLogger.fine("Certificate domain name has illegal single wildcard character: " + template, new Object[0]);
            }
            return true;
        }
        int lastWildcardIndex = template.lastIndexOf("*");
        if (lastWildcardIndex == -1) {
            return false;
        }
        String afterWildcard = template.substring(lastWildcardIndex);
        int firstDotIndex = afterWildcard.indexOf(".");
        if (firstDotIndex == -1) {
            if (SSLLogger.isOn) {
                SSLLogger.fine("Certificate domain name has illegal wildcard, no dot after wildcard character: " + template, new Object[0]);
            }
            return true;
        }
        if (!chainsToPublicCA) {
            return false;
        }
        String wildcardedDomain = afterWildcard.substring(firstDotIndex + 1);
        String templateDomainSuffix = RegisteredDomain.from("z." + wildcardedDomain).filter(d -> d.type() == RegisteredDomain.Type.ICANN).map(RegisteredDomain::publicSuffix).orElse(null);
        if (templateDomainSuffix == null) {
            return false;
        }
        if (wildcardedDomain.equalsIgnoreCase(templateDomainSuffix)) {
            if (SSLLogger.isOn) {
                SSLLogger.fine("Certificate domain name has illegal wildcard for top-level public suffix: " + template, new Object[0]);
            }
            return true;
        }
        return false;
    }

    private static boolean matchLeftmostWildcard(String name, String template) {
        name = name.toLowerCase(Locale.ENGLISH);
        template = template.toLowerCase(Locale.ENGLISH);
        int templateIdx = template.indexOf(".");
        int nameIdx = name.indexOf(".");
        if (templateIdx == -1) {
            templateIdx = template.length();
        }
        if (nameIdx == -1) {
            nameIdx = name.length();
        }
        if (HostnameChecker.matchWildCards(name.substring(0, nameIdx), template.substring(0, templateIdx))) {
            return template.substring(templateIdx).equals(name.substring(nameIdx));
        }
        return false;
    }

    private static boolean matchWildCards(String name, String template) {
        int wildcardIdx = template.indexOf("*");
        if (wildcardIdx == -1) {
            return name.equals(template);
        }
        boolean isBeginning = true;
        String afterWildcard = template;
        while (wildcardIdx != -1) {
            String beforeWildcard = afterWildcard.substring(0, wildcardIdx);
            afterWildcard = afterWildcard.substring(wildcardIdx + 1);
            int beforeStartIdx = name.indexOf(beforeWildcard);
            if (beforeStartIdx == -1 || isBeginning && beforeStartIdx != 0) {
                return false;
            }
            isBeginning = false;
            name = name.substring(beforeStartIdx + beforeWildcard.length());
            wildcardIdx = afterWildcard.indexOf("*");
        }
        return name.endsWith(afterWildcard);
    }
}

