/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.PKIXExtensions;

public class OIDMap {
    private static final String ROOT = "x509.info.extensions";
    private static final String AUTH_KEY_IDENTIFIER = "x509.info.extensions.AuthorityKeyIdentifier";
    private static final String SUB_KEY_IDENTIFIER = "x509.info.extensions.SubjectKeyIdentifier";
    private static final String KEY_USAGE = "x509.info.extensions.KeyUsage";
    private static final String PRIVATE_KEY_USAGE = "x509.info.extensions.PrivateKeyUsage";
    private static final String POLICY_MAPPINGS = "x509.info.extensions.PolicyMappings";
    private static final String SUB_ALT_NAME = "x509.info.extensions.SubjectAlternativeName";
    private static final String ISSUER_ALT_NAME = "x509.info.extensions.IssuerAlternativeName";
    private static final String BASIC_CONSTRAINTS = "x509.info.extensions.BasicConstraints";
    private static final String NAME_CONSTRAINTS = "x509.info.extensions.NameConstraints";
    private static final String POLICY_CONSTRAINTS = "x509.info.extensions.PolicyConstraints";
    private static final String CRL_NUMBER = "x509.info.extensions.CRLNumber";
    private static final String CRL_REASON = "x509.info.extensions.CRLReasonCode";
    private static final String NETSCAPE_CERT = "x509.info.extensions.NetscapeCertType";
    private static final String CERT_POLICIES = "x509.info.extensions.CertificatePolicies";
    private static final String EXT_KEY_USAGE = "x509.info.extensions.ExtendedKeyUsage";
    private static final String INHIBIT_ANY_POLICY = "x509.info.extensions.InhibitAnyPolicy";
    private static final String CRL_DIST_POINTS = "x509.info.extensions.CRLDistributionPoints";
    private static final String CERT_ISSUER = "x509.info.extensions.CertificateIssuer";
    private static final String SUBJECT_INFO_ACCESS = "x509.info.extensions.SubjectInfoAccess";
    private static final String AUTH_INFO_ACCESS = "x509.info.extensions.AuthorityInfoAccess";
    private static final String ISSUING_DIST_POINT = "x509.info.extensions.IssuingDistributionPoint";
    private static final String DELTA_CRL_INDICATOR = "x509.info.extensions.DeltaCRLIndicator";
    private static final String FRESHEST_CRL = "x509.info.extensions.FreshestCRL";
    private static final String OCSPNOCHECK = "x509.info.extensions.OCSPNoCheck";
    private static final Map<ObjectIdentifier, OIDInfo> oidMap = new HashMap<ObjectIdentifier, OIDInfo>();
    private static final Map<String, OIDInfo> nameMap = new HashMap<String, OIDInfo>();

    private OIDMap() {
    }

    private static void addInternal(String name, ObjectIdentifier oid, String className) {
        OIDInfo info = new OIDInfo(name, oid, className);
        oidMap.put(oid, info);
        nameMap.put(name, info);
    }

    public static void addAttribute(String name, String oid, Class<?> clazz) throws CertificateException {
        ObjectIdentifier objId;
        try {
            objId = ObjectIdentifier.of(oid);
        }
        catch (IOException ioe) {
            throw new CertificateException("Invalid Object identifier: " + oid);
        }
        OIDInfo info = new OIDInfo(name, objId, clazz);
        if (oidMap.put(objId, info) != null) {
            throw new CertificateException("Object identifier already exists: " + oid);
        }
        if (nameMap.put(name, info) != null) {
            throw new CertificateException("Name already exists: " + name);
        }
    }

    public static String getName(ObjectIdentifier oid) {
        OIDInfo info = oidMap.get(oid);
        return info == null ? null : info.name;
    }

    public static ObjectIdentifier getOID(String name) {
        OIDInfo info = nameMap.get(name);
        return info == null ? null : info.oid;
    }

    public static Class<?> getClass(String name) throws CertificateException {
        OIDInfo info = nameMap.get(name);
        return info == null ? null : info.getClazz();
    }

    public static Class<?> getClass(ObjectIdentifier oid) throws CertificateException {
        OIDInfo info = oidMap.get(oid);
        return info == null ? null : info.getClazz();
    }

    static {
        OIDMap.addInternal(SUB_KEY_IDENTIFIER, PKIXExtensions.SubjectKey_Id, "sun.security.x509.SubjectKeyIdentifierExtension");
        OIDMap.addInternal(KEY_USAGE, PKIXExtensions.KeyUsage_Id, "sun.security.x509.KeyUsageExtension");
        OIDMap.addInternal(PRIVATE_KEY_USAGE, PKIXExtensions.PrivateKeyUsage_Id, "sun.security.x509.PrivateKeyUsageExtension");
        OIDMap.addInternal(SUB_ALT_NAME, PKIXExtensions.SubjectAlternativeName_Id, "sun.security.x509.SubjectAlternativeNameExtension");
        OIDMap.addInternal(ISSUER_ALT_NAME, PKIXExtensions.IssuerAlternativeName_Id, "sun.security.x509.IssuerAlternativeNameExtension");
        OIDMap.addInternal(BASIC_CONSTRAINTS, PKIXExtensions.BasicConstraints_Id, "sun.security.x509.BasicConstraintsExtension");
        OIDMap.addInternal(CRL_NUMBER, PKIXExtensions.CRLNumber_Id, "sun.security.x509.CRLNumberExtension");
        OIDMap.addInternal(CRL_REASON, PKIXExtensions.ReasonCode_Id, "sun.security.x509.CRLReasonCodeExtension");
        OIDMap.addInternal(NAME_CONSTRAINTS, PKIXExtensions.NameConstraints_Id, "sun.security.x509.NameConstraintsExtension");
        OIDMap.addInternal(POLICY_MAPPINGS, PKIXExtensions.PolicyMappings_Id, "sun.security.x509.PolicyMappingsExtension");
        OIDMap.addInternal(AUTH_KEY_IDENTIFIER, PKIXExtensions.AuthorityKey_Id, "sun.security.x509.AuthorityKeyIdentifierExtension");
        OIDMap.addInternal(POLICY_CONSTRAINTS, PKIXExtensions.PolicyConstraints_Id, "sun.security.x509.PolicyConstraintsExtension");
        OIDMap.addInternal(NETSCAPE_CERT, ObjectIdentifier.of(KnownOIDs.NETSCAPE_CertType), "sun.security.x509.NetscapeCertTypeExtension");
        OIDMap.addInternal(CERT_POLICIES, PKIXExtensions.CertificatePolicies_Id, "sun.security.x509.CertificatePoliciesExtension");
        OIDMap.addInternal(EXT_KEY_USAGE, PKIXExtensions.ExtendedKeyUsage_Id, "sun.security.x509.ExtendedKeyUsageExtension");
        OIDMap.addInternal(INHIBIT_ANY_POLICY, PKIXExtensions.InhibitAnyPolicy_Id, "sun.security.x509.InhibitAnyPolicyExtension");
        OIDMap.addInternal(CRL_DIST_POINTS, PKIXExtensions.CRLDistributionPoints_Id, "sun.security.x509.CRLDistributionPointsExtension");
        OIDMap.addInternal(CERT_ISSUER, PKIXExtensions.CertificateIssuer_Id, "sun.security.x509.CertificateIssuerExtension");
        OIDMap.addInternal(SUBJECT_INFO_ACCESS, PKIXExtensions.SubjectInfoAccess_Id, "sun.security.x509.SubjectInfoAccessExtension");
        OIDMap.addInternal(AUTH_INFO_ACCESS, PKIXExtensions.AuthInfoAccess_Id, "sun.security.x509.AuthorityInfoAccessExtension");
        OIDMap.addInternal(ISSUING_DIST_POINT, PKIXExtensions.IssuingDistributionPoint_Id, "sun.security.x509.IssuingDistributionPointExtension");
        OIDMap.addInternal(DELTA_CRL_INDICATOR, PKIXExtensions.DeltaCRLIndicator_Id, "sun.security.x509.DeltaCRLIndicatorExtension");
        OIDMap.addInternal(FRESHEST_CRL, PKIXExtensions.FreshestCRL_Id, "sun.security.x509.FreshestCRLExtension");
        OIDMap.addInternal(OCSPNOCHECK, PKIXExtensions.OCSPNoCheck_Id, "sun.security.x509.OCSPNoCheckExtension");
    }

    private static class OIDInfo {
        final ObjectIdentifier oid;
        final String name;
        final String className;
        private volatile Class<?> clazz;

        OIDInfo(String name, ObjectIdentifier oid, String className) {
            this.name = name;
            this.oid = oid;
            this.className = className;
        }

        OIDInfo(String name, ObjectIdentifier oid, Class<?> clazz) {
            this.name = name;
            this.oid = oid;
            this.className = clazz.getName();
            this.clazz = clazz;
        }

        Class<?> getClazz() throws CertificateException {
            try {
                Class<?> c = this.clazz;
                if (c == null) {
                    this.clazz = c = Class.forName(this.className);
                }
                return c;
            }
            catch (ClassNotFoundException e) {
                throw new CertificateException("Could not load class: " + e, e);
            }
        }
    }
}

