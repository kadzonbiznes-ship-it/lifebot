/*
 * Decompiled with CFR 0.152.
 */
package sun.security.validator;

import java.io.IOException;
import java.security.AlgorithmConstraints;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.provider.certpath.AlgorithmChecker;
import sun.security.provider.certpath.UntrustedChecker;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.validator.Validator;
import sun.security.validator.ValidatorException;
import sun.security.x509.KeyIdentifier;
import sun.security.x509.NetscapeCertTypeExtension;
import sun.security.x509.X509CertImpl;

public final class SimpleValidator
extends Validator {
    static final String OID_BASIC_CONSTRAINTS = KnownOIDs.BasicConstraints.value();
    static final String OID_NETSCAPE_CERT_TYPE = KnownOIDs.NETSCAPE_CertType.value();
    static final String OID_KEY_USAGE = KnownOIDs.KeyUsage.value();
    static final String OID_EXTENDED_KEY_USAGE = KnownOIDs.extendedKeyUsage.value();
    static final String OID_EKU_ANY_USAGE = KnownOIDs.anyExtendedKeyUsage.value();
    static final ObjectIdentifier OBJID_NETSCAPE_CERT_TYPE = NetscapeCertTypeExtension.NetscapeCertType_Id;
    private static final String NSCT_SSL_CA = "ssl_ca";
    private static final String NSCT_CODE_SIGNING_CA = "object_signing_ca";
    private final Map<X500Principal, List<X509Certificate>> trustedX500Principals;
    private final Collection<X509Certificate> trustedCerts;

    SimpleValidator(String variant, Collection<X509Certificate> trustedCerts) {
        super("Simple", variant);
        this.trustedCerts = trustedCerts;
        this.trustedX500Principals = new HashMap<X500Principal, List<X509Certificate>>();
        for (X509Certificate cert : trustedCerts) {
            X500Principal principal = cert.getSubjectX500Principal();
            List<X509Certificate> list = this.trustedX500Principals.get(principal);
            if (list == null) {
                list = new ArrayList<X509Certificate>(2);
                this.trustedX500Principals.put(principal, list);
            }
            list.add(cert);
        }
    }

    @Override
    public Collection<X509Certificate> getTrustedCertificates() {
        return this.trustedCerts;
    }

    @Override
    X509Certificate[] engineValidate(X509Certificate[] chain, Collection<X509Certificate> otherCerts, List<byte[]> responseList, AlgorithmConstraints constraints, Object parameter) throws CertificateException {
        if (chain == null || chain.length == 0) {
            throw new CertificateException("null or zero-length certificate chain");
        }
        chain = this.buildTrustedChain(chain);
        Date date = this.validationDate;
        if (date == null) {
            date = new Date();
        }
        UntrustedChecker untrustedChecker = new UntrustedChecker();
        X509Certificate anchorCert = chain[chain.length - 1];
        try {
            untrustedChecker.check(anchorCert);
        }
        catch (CertPathValidatorException cpve) {
            throw new ValidatorException("Untrusted certificate: " + anchorCert.getSubjectX500Principal(), ValidatorException.T_UNTRUSTED_CERT, anchorCert, cpve);
        }
        TrustAnchor anchor = new TrustAnchor(anchorCert, null);
        AlgorithmChecker defaultAlgChecker = new AlgorithmChecker(anchor, this.variant);
        AlgorithmChecker appAlgChecker = null;
        if (constraints != null) {
            appAlgChecker = new AlgorithmChecker(anchor, constraints, null, this.variant);
        }
        int maxPathLength = chain.length - 1;
        for (int i = chain.length - 2; i >= 0; --i) {
            X509Certificate issuerCert = chain[i + 1];
            X509Certificate cert = chain[i];
            try {
                untrustedChecker.check(cert, Collections.emptySet());
            }
            catch (CertPathValidatorException cpve) {
                throw new ValidatorException("Untrusted certificate: " + cert.getSubjectX500Principal(), ValidatorException.T_UNTRUSTED_CERT, cert, cpve);
            }
            try {
                defaultAlgChecker.check(cert, Collections.emptySet());
                if (appAlgChecker != null) {
                    appAlgChecker.check(cert, Collections.emptySet());
                }
            }
            catch (CertPathValidatorException cpve) {
                throw new ValidatorException(ValidatorException.T_ALGORITHM_DISABLED, cert, cpve);
            }
            if (!this.variant.equals("code signing") && !this.variant.equals("jce signing")) {
                cert.checkValidity(date);
            }
            if (!cert.getIssuerX500Principal().equals(issuerCert.getSubjectX500Principal())) {
                throw new ValidatorException(ValidatorException.T_NAME_CHAINING, cert);
            }
            try {
                cert.verify(issuerCert.getPublicKey());
            }
            catch (GeneralSecurityException e) {
                throw new ValidatorException(ValidatorException.T_SIGNATURE_ERROR, cert, e);
            }
            if (i == 0) continue;
            maxPathLength = this.checkExtensions(cert, maxPathLength);
        }
        return chain;
    }

    private int checkExtensions(X509Certificate cert, int maxPathLen) throws CertificateException {
        Set<String> critSet = cert.getCriticalExtensionOIDs();
        if (critSet == null) {
            critSet = Collections.emptySet();
        }
        int pathLenConstraint = this.checkBasicConstraints(cert, critSet, maxPathLen);
        this.checkKeyUsage(cert, critSet);
        this.checkNetscapeCertType(cert, critSet);
        if (!critSet.isEmpty()) {
            throw new ValidatorException("Certificate contains unknown critical extensions: " + critSet, ValidatorException.T_CA_EXTENSIONS, cert);
        }
        return pathLenConstraint;
    }

    private void checkNetscapeCertType(X509Certificate cert, Set<String> critSet) throws CertificateException {
        if (!this.variant.equals("generic")) {
            if (this.variant.equals("tls client") || this.variant.equals("tls server")) {
                if (!SimpleValidator.getNetscapeCertTypeBit(cert, NSCT_SSL_CA)) {
                    throw new ValidatorException("Invalid Netscape CertType extension for SSL CA certificate", ValidatorException.T_CA_EXTENSIONS, cert);
                }
                critSet.remove(OID_NETSCAPE_CERT_TYPE);
            } else if (this.variant.equals("code signing") || this.variant.equals("jce signing")) {
                if (!SimpleValidator.getNetscapeCertTypeBit(cert, NSCT_CODE_SIGNING_CA)) {
                    throw new ValidatorException("Invalid Netscape CertType extension for code signing CA certificate", ValidatorException.T_CA_EXTENSIONS, cert);
                }
                critSet.remove(OID_NETSCAPE_CERT_TYPE);
            } else {
                throw new CertificateException("Unknown variant " + this.variant);
            }
        }
    }

    static boolean getNetscapeCertTypeBit(X509Certificate cert, String type) {
        try {
            NetscapeCertTypeExtension ext;
            if (cert instanceof X509CertImpl) {
                X509CertImpl certImpl = (X509CertImpl)cert;
                ext = (NetscapeCertTypeExtension)certImpl.getExtension(OBJID_NETSCAPE_CERT_TYPE);
                if (ext == null) {
                    return true;
                }
            } else {
                byte[] extVal = cert.getExtensionValue(OID_NETSCAPE_CERT_TYPE);
                if (extVal == null) {
                    return true;
                }
                DerInputStream in = new DerInputStream(extVal);
                byte[] encoded = in.getOctetString();
                encoded = new DerValue(encoded).getUnalignedBitString().toByteArray();
                ext = new NetscapeCertTypeExtension(encoded);
            }
            return ext.get(type);
        }
        catch (IOException e) {
            return false;
        }
    }

    private int checkBasicConstraints(X509Certificate cert, Set<String> critSet, int maxPathLen) throws CertificateException {
        critSet.remove(OID_BASIC_CONSTRAINTS);
        int constraints = cert.getBasicConstraints();
        if (constraints < 0) {
            throw new ValidatorException("End user tried to act as a CA", ValidatorException.T_CA_EXTENSIONS, cert);
        }
        if (!X509CertImpl.isSelfIssued(cert)) {
            if (maxPathLen <= 0) {
                throw new ValidatorException("Violated path length constraints", ValidatorException.T_CA_EXTENSIONS, cert);
            }
            --maxPathLen;
        }
        if (maxPathLen > constraints) {
            maxPathLen = constraints;
        }
        return maxPathLen;
    }

    private void checkKeyUsage(X509Certificate cert, Set<String> critSet) throws CertificateException {
        critSet.remove(OID_KEY_USAGE);
        critSet.remove(OID_EXTENDED_KEY_USAGE);
        boolean[] keyUsageInfo = cert.getKeyUsage();
        if (!(keyUsageInfo == null || keyUsageInfo.length >= 6 && keyUsageInfo[5])) {
            throw new ValidatorException("Wrong key usage: expected keyCertSign", ValidatorException.T_CA_EXTENSIONS, cert);
        }
    }

    private X509Certificate[] buildTrustedChain(X509Certificate[] chain) throws CertificateException {
        ArrayList<X509Certificate> c = new ArrayList<X509Certificate>(chain.length);
        for (int i = 0; i < chain.length; ++i) {
            X509Certificate cert = chain[i];
            X509Certificate trustedCert = this.getTrustedCertificate(cert);
            if (trustedCert != null) {
                c.add(trustedCert);
                return c.toArray(CHAIN0);
            }
            c.add(cert);
        }
        X509Certificate cert = chain[chain.length - 1];
        X500Principal subject = cert.getSubjectX500Principal();
        X500Principal issuer = cert.getIssuerX500Principal();
        List<X509Certificate> list = this.trustedX500Principals.get(issuer);
        if (list != null) {
            X509Certificate matchedCert = list.get(0);
            X509CertImpl certImpl = X509CertImpl.toImpl(cert);
            KeyIdentifier akid = certImpl.getAuthKeyId();
            if (akid != null) {
                for (X509Certificate sup : list) {
                    X509CertImpl supCert = X509CertImpl.toImpl(sup);
                    if (!akid.equals(supCert.getSubjectKeyId())) continue;
                    matchedCert = sup;
                    break;
                }
            }
            c.add(matchedCert);
            return c.toArray(CHAIN0);
        }
        throw new ValidatorException(ValidatorException.T_NO_TRUST_ANCHOR);
    }

    private X509Certificate getTrustedCertificate(X509Certificate cert) {
        X500Principal certSubjectName = cert.getSubjectX500Principal();
        List<X509Certificate> list = this.trustedX500Principals.get(certSubjectName);
        if (list == null) {
            return null;
        }
        X500Principal certIssuerName = cert.getIssuerX500Principal();
        PublicKey certPublicKey = cert.getPublicKey();
        for (X509Certificate mycert : list) {
            if (mycert.equals(cert)) {
                return cert;
            }
            if (!mycert.getIssuerX500Principal().equals(certIssuerName) || !mycert.getPublicKey().equals(certPublicKey)) continue;
            return mycert;
        }
        return null;
    }
}

