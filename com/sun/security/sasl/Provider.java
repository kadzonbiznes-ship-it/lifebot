/*
 * Decompiled with CFR 0.152.
 */
package com.sun.security.sasl;

import com.sun.security.sasl.ClientFactoryImpl;
import com.sun.security.sasl.ServerFactoryImpl;
import com.sun.security.sasl.ntlm.FactoryImpl;
import java.security.AccessController;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.ProviderException;
import sun.security.util.SecurityConstants;

public final class Provider
extends java.security.Provider {
    private static final long serialVersionUID = 8622598936488630849L;
    private static final String info = "Sun SASL provider(implements client mechanisms for: DIGEST-MD5, EXTERNAL, PLAIN, CRAM-MD5, NTLM; server mechanisms for: DIGEST-MD5, CRAM-MD5, NTLM)";

    public Provider() {
        super("SunSASL", SecurityConstants.PROVIDER_VER, info);
        final Provider p = this;
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                Provider.this.putService(new ProviderService(p, "SaslClientFactory", "DIGEST-MD5", "com.sun.security.sasl.digest.FactoryImpl"));
                Provider.this.putService(new ProviderService(p, "SaslClientFactory", "NTLM", "com.sun.security.sasl.ntlm.FactoryImpl"));
                Provider.this.putService(new ProviderService(p, "SaslClientFactory", "EXTERNAL", "com.sun.security.sasl.ClientFactoryImpl"));
                Provider.this.putService(new ProviderService(p, "SaslClientFactory", "PLAIN", "com.sun.security.sasl.ClientFactoryImpl"));
                Provider.this.putService(new ProviderService(p, "SaslClientFactory", "CRAM-MD5", "com.sun.security.sasl.ClientFactoryImpl"));
                Provider.this.putService(new ProviderService(p, "SaslServerFactory", "CRAM-MD5", "com.sun.security.sasl.ServerFactoryImpl"));
                Provider.this.putService(new ProviderService(p, "SaslServerFactory", "DIGEST-MD5", "com.sun.security.sasl.digest.FactoryImpl"));
                Provider.this.putService(new ProviderService(p, "SaslServerFactory", "NTLM", "com.sun.security.sasl.ntlm.FactoryImpl"));
                return null;
            }
        });
    }

    private static final class ProviderService
    extends Provider.Service {
        ProviderService(java.security.Provider p, String type, String algo, String cn) {
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
                if (algo.equals("DIGEST-MD5")) {
                    return new com.sun.security.sasl.digest.FactoryImpl();
                }
                if (algo.equals("NTLM")) {
                    return new FactoryImpl();
                }
                if (type.equals("SaslClientFactory")) {
                    if (algo.equals("EXTERNAL") || algo.equals("PLAIN") || algo.equals("CRAM-MD5")) {
                        return new ClientFactoryImpl();
                    }
                } else if (type.equals("SaslServerFactory") && algo.equals("CRAM-MD5")) {
                    return new ServerFactoryImpl();
                }
            }
            catch (Exception ex) {
                throw new NoSuchAlgorithmException("Error constructing " + type + " for " + algo + " using SunSASL", ex);
            }
            throw new ProviderException("No impl for " + algo + " " + type);
        }
    }
}

