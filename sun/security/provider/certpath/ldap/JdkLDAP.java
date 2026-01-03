/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider.certpath.ldap;

import java.security.AccessController;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.ProviderException;
import java.security.cert.CertStoreParameters;
import java.util.HashMap;
import java.util.List;
import sun.security.provider.certpath.ldap.LDAPCertStore;
import sun.security.util.SecurityConstants;

public final class JdkLDAP
extends Provider {
    private static final long serialVersionUID = -2279741232933606418L;

    public JdkLDAP() {
        super("JdkLDAP", SecurityConstants.PROVIDER_VER, "JdkLDAP Provider (implements LDAP CertStore)");
        JdkLDAP p = this;
        PrivilegedAction<Void> pa = () -> {
            HashMap<String, String> attrs = new HashMap<String, String>(2);
            attrs.put("LDAPSchema", "RFC2587");
            attrs.put("ImplementedIn", "Software");
            this.putService(new ProviderService((Provider)p, "CertStore", "LDAP", "sun.security.provider.certpath.ldap.LDAPCertStore", null, attrs));
            return null;
        };
        AccessController.doPrivileged(pa);
    }

    private static final class ProviderService
    extends Provider.Service {
        ProviderService(Provider p, String type, String algo, String cn, List<String> aliases, HashMap<String, String> attrs) {
            super(p, type, algo, cn, aliases, attrs);
        }

        @Override
        public Object newInstance(Object ctrParamObj) throws NoSuchAlgorithmException {
            String type = this.getType();
            String algo = this.getAlgorithm();
            if (type.equals("CertStore") && algo.equals("LDAP")) {
                if (ctrParamObj != null && !(ctrParamObj instanceof CertStoreParameters)) {
                    throw new InvalidParameterException("constructorParameter must be instanceof CertStoreParameters");
                }
                try {
                    return new LDAPCertStore((CertStoreParameters)ctrParamObj);
                }
                catch (Exception ex) {
                    throw new NoSuchAlgorithmException("Error constructing " + type + " for " + algo + " using JdkLDAP", ex);
                }
            }
            throw new ProviderException("No impl for " + algo + " " + type);
        }
    }
}

