/*
 * Decompiled with CFR 0.152.
 */
package sun.security.jgss;

import java.security.AccessController;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.ProviderException;
import sun.security.jgss.krb5.Krb5MechFactory;
import sun.security.jgss.spnego.SpNegoMechFactory;
import sun.security.util.SecurityConstants;

public final class SunProvider
extends Provider {
    private static final long serialVersionUID = -238911724858694198L;
    private static final String INFO = "Sun (Kerberos v5, SPNEGO)";

    public SunProvider() {
        super("SunJGSS", SecurityConstants.PROVIDER_VER, INFO);
        SunProvider p = this;
        AccessController.doPrivileged(() -> {
            this.putService(new ProviderService(p, "GssApiMechanism", "1.2.840.113554.1.2.2", "sun.security.jgss.krb5.Krb5MechFactory"));
            this.putService(new ProviderService(p, "GssApiMechanism", "1.3.6.1.5.5.2", "sun.security.jgss.spnego.SpNegoMechFactory"));
            return null;
        });
    }

    private static final class ProviderService
    extends Provider.Service {
        ProviderService(Provider p, String type, String algo, String cn) {
            super(p, type, algo, cn, null, null);
        }

        @Override
        public Object newInstance(Object ctrParamObj) throws NoSuchAlgorithmException {
            String type = this.getType();
            if (ctrParamObj != null) {
                throw new InvalidParameterException("constructorParameter not used with " + type + " engines");
            }
            String algo = this.getAlgorithm();
            try {
                if (type.equals("GssApiMechanism")) {
                    if (algo.equals("1.2.840.113554.1.2.2")) {
                        return new Krb5MechFactory();
                    }
                    if (algo.equals("1.3.6.1.5.5.2")) {
                        return new SpNegoMechFactory();
                    }
                }
            }
            catch (Exception ex) {
                throw new NoSuchAlgorithmException("Error constructing " + type + " for " + algo + " using SunJGSS", ex);
            }
            throw new ProviderException("No impl for " + algo + " " + type);
        }
    }
}

