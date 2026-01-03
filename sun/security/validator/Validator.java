/*
 * Decompiled with CFR 0.152.
 */
package sun.security.validator;

import java.security.AlgorithmConstraints;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import sun.security.validator.EndEntityChecker;
import sun.security.validator.PKIXValidator;
import sun.security.validator.SimpleValidator;
import sun.security.validator.TrustStoreUtil;

public abstract sealed class Validator
permits PKIXValidator, SimpleValidator {
    static final X509Certificate[] CHAIN0 = new X509Certificate[0];
    public static final String TYPE_SIMPLE = "Simple";
    public static final String TYPE_PKIX = "PKIX";
    public static final String VAR_GENERIC = "generic";
    public static final String VAR_CODE_SIGNING = "code signing";
    public static final String VAR_JCE_SIGNING = "jce signing";
    public static final String VAR_TLS_CLIENT = "tls client";
    public static final String VAR_TLS_SERVER = "tls server";
    public static final String VAR_TSA_SERVER = "tsa server";
    private final String type;
    final EndEntityChecker endEntityChecker;
    final String variant;
    @Deprecated
    volatile Date validationDate;

    Validator(String type, String variant) {
        this.type = type;
        this.variant = variant;
        this.endEntityChecker = EndEntityChecker.getInstance(type, variant);
    }

    public static Validator getInstance(String type, String variant, KeyStore ks) {
        return Validator.getInstance(type, variant, TrustStoreUtil.getTrustedCerts(ks));
    }

    public static Validator getInstance(String type, String variant, Collection<X509Certificate> trustedCerts) {
        if (type.equals(TYPE_SIMPLE)) {
            return new SimpleValidator(variant, trustedCerts);
        }
        if (type.equals(TYPE_PKIX)) {
            return new PKIXValidator(variant, trustedCerts);
        }
        throw new IllegalArgumentException("Unknown validator type: " + type);
    }

    public static Validator getInstance(String type, String variant, PKIXBuilderParameters params) {
        if (!type.equals(TYPE_PKIX)) {
            throw new IllegalArgumentException("getInstance(PKIXBuilderParameters) can only be used with PKIX validator");
        }
        return new PKIXValidator(variant, params);
    }

    public final X509Certificate[] validate(X509Certificate[] chain) throws CertificateException {
        return this.validate(chain, null, null);
    }

    public final X509Certificate[] validate(X509Certificate[] chain, Collection<X509Certificate> otherCerts) throws CertificateException {
        return this.validate(chain, otherCerts, null);
    }

    public final X509Certificate[] validate(X509Certificate[] chain, Collection<X509Certificate> otherCerts, Object parameter) throws CertificateException {
        return this.validate(chain, otherCerts, Collections.emptyList(), null, parameter);
    }

    public final X509Certificate[] validate(X509Certificate[] chain, Collection<X509Certificate> otherCerts, List<byte[]> responseList, AlgorithmConstraints constraints, Object parameter) throws CertificateException {
        if ((chain = this.engineValidate(chain, otherCerts, responseList, constraints, parameter)).length > 1) {
            boolean checkUnresolvedCritExts = this.type != TYPE_PKIX;
            this.endEntityChecker.check(chain, parameter, checkUnresolvedCritExts);
        }
        return chain;
    }

    abstract X509Certificate[] engineValidate(X509Certificate[] var1, Collection<X509Certificate> var2, List<byte[]> var3, AlgorithmConstraints var4, Object var5) throws CertificateException;

    public abstract Collection<X509Certificate> getTrustedCertificates();

    @Deprecated
    public void setValidationDate(Date validationDate) {
        this.validationDate = validationDate;
    }
}

