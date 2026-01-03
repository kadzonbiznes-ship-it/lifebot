/*
 * Decompiled with CFR 0.152.
 */
package com.sun.security.sasl.gsskerb;

import com.sun.security.sasl.gsskerb.FactoryImpl;
import java.security.AccessController;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.ProviderException;
import sun.security.util.SecurityConstants;

public final class JdkSASL
extends Provider {
    private static final long serialVersionUID = 8622590901641830849L;
    private static final String info = "JDK SASL provider(implements client and server mechanisms for GSSAPI)";

    public JdkSASL() {
        super("JdkSASL", SecurityConstants.PROVIDER_VER, info);
        final JdkSASL p = this;
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                JdkSASL.this.putService(new ProviderService(p, "SaslClientFactory", "GSSAPI", "com.sun.security.sasl.gsskerb.FactoryImpl"));
                JdkSASL.this.putService(new ProviderService(p, "SaslServerFactory", "GSSAPI", "com.sun.security.sasl.gsskerb.FactoryImpl"));
                return null;
            }
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
                if (algo.equals("GSSAPI")) {
                    return new FactoryImpl();
                }
            }
            catch (Exception ex) {
                throw new NoSuchAlgorithmException("Error constructing " + type + " for " + algo + " using JdkSASL", ex);
            }
            throw new ProviderException("No impl for " + algo + " " + type);
        }
    }
}

