/*
 * Decompiled with CFR 0.152.
 */
package sun.security.validator;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import sun.security.util.KnownOIDs;
import sun.security.validator.CADistrustPolicy;
import sun.security.validator.SimpleValidator;
import sun.security.validator.ValidatorException;

class EndEntityChecker {
    private static final String OID_EXTENDED_KEY_USAGE = SimpleValidator.OID_EXTENDED_KEY_USAGE;
    private static final String OID_EKU_TLS_SERVER = KnownOIDs.serverAuth.value();
    private static final String OID_EKU_TLS_CLIENT = KnownOIDs.clientAuth.value();
    private static final String OID_EKU_CODE_SIGNING = KnownOIDs.codeSigning.value();
    private static final String OID_EKU_TIME_STAMPING = KnownOIDs.KP_TimeStamping.value();
    private static final String OID_EKU_ANY_USAGE = KnownOIDs.anyExtendedKeyUsage.value();
    private static final String OID_EKU_NS_SGC = KnownOIDs.NETSCAPE_ExportApproved.value();
    private static final String OID_EKU_MS_SGC = KnownOIDs.MICROSOFT_ExportApproved.value();
    private static final String OID_SUBJECT_ALT_NAME = KnownOIDs.SubjectAlternativeName.value();
    private static final String NSCT_SSL_CLIENT = "ssl_client";
    private static final String NSCT_SSL_SERVER = "ssl_server";
    private static final String NSCT_CODE_SIGNING = "object_signing";
    private static final int KU_SIGNATURE = 0;
    private static final int KU_NON_REPUDIATION = 1;
    private static final int KU_KEY_ENCIPHERMENT = 2;
    private static final int KU_KEY_AGREEMENT = 4;
    private static final Collection<String> KU_SERVER_SIGNATURE = Arrays.asList("DHE_DSS", "DHE_RSA", "ECDHE_ECDSA", "ECDHE_RSA", "RSA_EXPORT", "UNKNOWN");
    private static final Collection<String> KU_SERVER_ENCRYPTION = List.of("RSA");
    private static final Collection<String> KU_SERVER_KEY_AGREEMENT = Arrays.asList("DH_DSS", "DH_RSA", "ECDH_ECDSA", "ECDH_RSA");
    private final String variant;
    private final String type;

    private EndEntityChecker(String type, String variant) {
        this.type = type;
        this.variant = variant;
    }

    static EndEntityChecker getInstance(String type, String variant) {
        return new EndEntityChecker(type, variant);
    }

    void check(X509Certificate[] chain, Object parameter, boolean checkUnresolvedCritExts) throws CertificateException {
        if (this.variant.equals("generic")) {
            return;
        }
        Set<String> exts = this.getCriticalExtensions(chain[0]);
        if (this.variant.equals("tls server")) {
            this.checkTLSServer(chain[0], (String)parameter, exts);
        } else if (this.variant.equals("tls client")) {
            this.checkTLSClient(chain[0], exts);
        } else if (this.variant.equals("code signing")) {
            this.checkCodeSigning(chain[0], exts);
        } else if (this.variant.equals("jce signing")) {
            this.checkCodeSigning(chain[0], exts);
        } else if (this.variant.equals("tsa server")) {
            this.checkTSAServer(chain[0], exts);
        } else {
            throw new CertificateException("Unknown variant: " + this.variant);
        }
        if (checkUnresolvedCritExts) {
            this.checkRemainingExtensions(exts);
        }
        for (CADistrustPolicy policy : CADistrustPolicy.POLICIES) {
            policy.checkDistrust(this.variant, chain);
        }
    }

    private Set<String> getCriticalExtensions(X509Certificate cert) {
        Set<String> exts = cert.getCriticalExtensionOIDs();
        if (exts == null) {
            exts = Collections.emptySet();
        }
        return exts;
    }

    private void checkRemainingExtensions(Set<String> exts) throws CertificateException {
        exts.remove(SimpleValidator.OID_BASIC_CONSTRAINTS);
        exts.remove(OID_SUBJECT_ALT_NAME);
        if (!exts.isEmpty()) {
            throw new CertificateException("Certificate contains unsupported critical extensions: " + exts);
        }
    }

    private boolean checkEKU(X509Certificate cert, Set<String> exts, String expectedEKU) throws CertificateException {
        List<String> eku = cert.getExtendedKeyUsage();
        if (eku == null) {
            return true;
        }
        return eku.contains(expectedEKU) || eku.contains(OID_EKU_ANY_USAGE);
    }

    private boolean checkKeyUsage(X509Certificate cert, int bit) {
        boolean[] keyUsage = cert.getKeyUsage();
        if (keyUsage == null) {
            return true;
        }
        return keyUsage.length > bit && keyUsage[bit];
    }

    private void checkTLSClient(X509Certificate cert, Set<String> exts) throws CertificateException {
        if (!this.checkKeyUsage(cert, 0)) {
            throw new ValidatorException("KeyUsage does not allow digital signatures", ValidatorException.T_EE_EXTENSIONS, cert);
        }
        if (!this.checkEKU(cert, exts, OID_EKU_TLS_CLIENT)) {
            throw new ValidatorException("Extended key usage does not permit use for TLS client authentication", ValidatorException.T_EE_EXTENSIONS, cert);
        }
        if (!SimpleValidator.getNetscapeCertTypeBit(cert, NSCT_SSL_CLIENT)) {
            throw new ValidatorException("Netscape cert type does not permit use for SSL client", ValidatorException.T_EE_EXTENSIONS, cert);
        }
        exts.remove(SimpleValidator.OID_KEY_USAGE);
        exts.remove(SimpleValidator.OID_EXTENDED_KEY_USAGE);
        exts.remove(SimpleValidator.OID_NETSCAPE_CERT_TYPE);
    }

    private void checkTLSServer(X509Certificate cert, String parameter, Set<String> exts) throws CertificateException {
        if (KU_SERVER_ENCRYPTION.contains(parameter)) {
            if (!this.checkKeyUsage(cert, 2)) {
                throw new ValidatorException("KeyUsage does not allow key encipherment", ValidatorException.T_EE_EXTENSIONS, cert);
            }
        } else if (KU_SERVER_SIGNATURE.contains(parameter)) {
            if (!this.checkKeyUsage(cert, 0)) {
                throw new ValidatorException("KeyUsage does not allow digital signatures", ValidatorException.T_EE_EXTENSIONS, cert);
            }
        } else if (KU_SERVER_KEY_AGREEMENT.contains(parameter)) {
            if (!this.checkKeyUsage(cert, 4)) {
                throw new ValidatorException("KeyUsage does not allow key agreement", ValidatorException.T_EE_EXTENSIONS, cert);
            }
        } else {
            throw new CertificateException("Unknown authType: " + parameter);
        }
        if (!(this.checkEKU(cert, exts, OID_EKU_TLS_SERVER) || this.checkEKU(cert, exts, OID_EKU_MS_SGC) || this.checkEKU(cert, exts, OID_EKU_NS_SGC))) {
            throw new ValidatorException("Extended key usage does not permit use for TLS server authentication", ValidatorException.T_EE_EXTENSIONS, cert);
        }
        if (!SimpleValidator.getNetscapeCertTypeBit(cert, NSCT_SSL_SERVER)) {
            throw new ValidatorException("Netscape cert type does not permit use for SSL server", ValidatorException.T_EE_EXTENSIONS, cert);
        }
        exts.remove(SimpleValidator.OID_KEY_USAGE);
        exts.remove(SimpleValidator.OID_EXTENDED_KEY_USAGE);
        exts.remove(SimpleValidator.OID_NETSCAPE_CERT_TYPE);
    }

    private void checkCodeSigning(X509Certificate cert, Set<String> exts) throws CertificateException {
        if (!this.checkKeyUsage(cert, 0)) {
            throw new ValidatorException("KeyUsage does not allow digital signatures", ValidatorException.T_EE_EXTENSIONS, cert);
        }
        if (!this.checkEKU(cert, exts, OID_EKU_CODE_SIGNING)) {
            throw new ValidatorException("Extended key usage does not permit use for code signing", ValidatorException.T_EE_EXTENSIONS, cert);
        }
        if (!this.variant.equals("jce signing")) {
            if (!SimpleValidator.getNetscapeCertTypeBit(cert, NSCT_CODE_SIGNING)) {
                throw new ValidatorException("Netscape cert type does not permit use for code signing", ValidatorException.T_EE_EXTENSIONS, cert);
            }
            exts.remove(SimpleValidator.OID_NETSCAPE_CERT_TYPE);
        }
        exts.remove(SimpleValidator.OID_KEY_USAGE);
        exts.remove(SimpleValidator.OID_EXTENDED_KEY_USAGE);
    }

    private void checkTSAServer(X509Certificate cert, Set<String> exts) throws CertificateException {
        if (!this.checkKeyUsage(cert, 0) && !this.checkKeyUsage(cert, 1)) {
            throw new ValidatorException("KeyUsage does not allow digital signatures or non repudiation", ValidatorException.T_EE_EXTENSIONS, cert);
        }
        if (cert.getExtendedKeyUsage() == null) {
            throw new ValidatorException("Certificate does not contain an extended key usage extension required for a TSA server", ValidatorException.T_EE_EXTENSIONS, cert);
        }
        if (!this.checkEKU(cert, exts, OID_EKU_TIME_STAMPING)) {
            throw new ValidatorException("Extended key usage does not permit use for TSA server", ValidatorException.T_EE_EXTENSIONS, cert);
        }
        exts.remove(SimpleValidator.OID_KEY_USAGE);
        exts.remove(SimpleValidator.OID_EXTENDED_KEY_USAGE);
    }
}

