/*
 * Decompiled with CFR 0.152.
 */
package sun.security.validator;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.EnumSet;
import sun.security.util.Debug;
import sun.security.validator.CamerfirmaTLSPolicy;
import sun.security.validator.EntrustTLSPolicy;
import sun.security.validator.SymantecTLSPolicy;
import sun.security.validator.ValidatorException;

enum CADistrustPolicy {
    SYMANTEC_TLS{

        @Override
        void checkDistrust(String variant, X509Certificate[] chain) throws ValidatorException {
            if (!variant.equals("tls server")) {
                return;
            }
            SymantecTLSPolicy.checkDistrust(chain);
        }
    }
    ,
    ENTRUST_TLS{

        @Override
        void checkDistrust(String variant, X509Certificate[] chain) throws ValidatorException {
            if (!variant.equals("tls server")) {
                return;
            }
            EntrustTLSPolicy.checkDistrust(chain);
        }
    }
    ,
    CAMERFIRMA_TLS{

        @Override
        void checkDistrust(String variant, X509Certificate[] chain) throws ValidatorException {
            if (!variant.equals("tls server")) {
                return;
            }
            CamerfirmaTLSPolicy.checkDistrust(chain);
        }
    };

    static final EnumSet<CADistrustPolicy> POLICIES;

    abstract void checkDistrust(String var1, X509Certificate[] var2) throws ValidatorException;

    private static EnumSet<CADistrustPolicy> parseProperty() {
        String[] policies;
        String property = AccessController.doPrivileged(new PrivilegedAction<String>(){

            @Override
            public String run() {
                return Security.getProperty("jdk.security.caDistrustPolicies");
            }
        });
        EnumSet<CADistrustPolicy> set = EnumSet.noneOf(CADistrustPolicy.class);
        if (property == null || property.isEmpty()) {
            return set;
        }
        for (String policy : policies = property.split(",")) {
            policy = policy.trim();
            try {
                CADistrustPolicy caPolicy = Enum.valueOf(CADistrustPolicy.class, policy);
                set.add(caPolicy);
            }
            catch (IllegalArgumentException iae) {
                Debug debug = Debug.getInstance("certpath");
                if (debug == null) continue;
                debug.println("Unknown value for the jdk.security.caDistrustPolicies property: " + policy);
            }
        }
        return set;
    }

    static {
        POLICIES = CADistrustPolicy.parseProperty();
    }
}

