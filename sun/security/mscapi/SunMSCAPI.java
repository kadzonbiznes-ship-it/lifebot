/*
 * Decompiled with CFR 0.152.
 */
package sun.security.mscapi;

import java.security.AccessController;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.ProviderException;
import java.util.HashMap;
import java.util.List;
import sun.security.mscapi.CKeyPairGenerator;
import sun.security.mscapi.CKeyStore;
import sun.security.mscapi.CRSACipher;
import sun.security.mscapi.CSignature;
import sun.security.mscapi.PRNG;
import sun.security.util.SecurityConstants;
import sun.security.util.SecurityProviderConstants;

public final class SunMSCAPI
extends Provider {
    private static final long serialVersionUID = 8622598936488630849L;
    private static final String INFO = "Sun's Microsoft Crypto API provider";

    public SunMSCAPI() {
        super("SunMSCAPI", SecurityConstants.PROVIDER_VER, INFO);
        final SunMSCAPI p = this;
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                HashMap<String, String> srattrs = new HashMap<String, String>(1);
                srattrs.put("ThreadSafe", "true");
                SunMSCAPI.this.putService(new ProviderService(p, "SecureRandom", "Windows-PRNG", "sun.security.mscapi.PRNG", null, srattrs));
                SunMSCAPI.this.putService(new ProviderService(p, "KeyStore", "Windows-MY", "sun.security.mscapi.CKeyStore$MY"));
                SunMSCAPI.this.putService(new ProviderService(p, "KeyStore", "Windows-MY-CURRENTUSER", "sun.security.mscapi.CKeyStore$MY"));
                SunMSCAPI.this.putService(new ProviderService(p, "KeyStore", "Windows-ROOT", "sun.security.mscapi.CKeyStore$ROOT"));
                SunMSCAPI.this.putService(new ProviderService(p, "KeyStore", "Windows-ROOT-CURRENTUSER", "sun.security.mscapi.CKeyStore$ROOT"));
                SunMSCAPI.this.putService(new ProviderService(p, "KeyStore", "Windows-MY-LOCALMACHINE", "sun.security.mscapi.CKeyStore$MYLocalMachine"));
                SunMSCAPI.this.putService(new ProviderService(p, "KeyStore", "Windows-ROOT-LOCALMACHINE", "sun.security.mscapi.CKeyStore$ROOTLocalMachine"));
                HashMap<String, String> attrs = new HashMap<String, String>(1);
                attrs.put("SupportedKeyClasses", "sun.security.mscapi.CKey");
                SunMSCAPI.this.putService(new ProviderService(p, "Signature", "NONEwithRSA", "sun.security.mscapi.CSignature$NONEwithRSA", null, attrs));
                SunMSCAPI.this.putService(new ProviderService(p, "Signature", "SHA1withRSA", "sun.security.mscapi.CSignature$SHA1withRSA", null, attrs));
                SunMSCAPI.this.putService(new ProviderServiceA(p, "Signature", "SHA256withRSA", "sun.security.mscapi.CSignature$SHA256withRSA", attrs));
                SunMSCAPI.this.putService(new ProviderServiceA(p, "Signature", "SHA384withRSA", "sun.security.mscapi.CSignature$SHA384withRSA", attrs));
                SunMSCAPI.this.putService(new ProviderServiceA(p, "Signature", "SHA512withRSA", "sun.security.mscapi.CSignature$SHA512withRSA", attrs));
                SunMSCAPI.this.putService(new ProviderServiceA(p, "Signature", "RSASSA-PSS", "sun.security.mscapi.CSignature$PSS", attrs));
                SunMSCAPI.this.putService(new ProviderService(p, "Signature", "MD5withRSA", "sun.security.mscapi.CSignature$MD5withRSA", null, attrs));
                SunMSCAPI.this.putService(new ProviderService(p, "Signature", "MD2withRSA", "sun.security.mscapi.CSignature$MD2withRSA", null, attrs));
                SunMSCAPI.this.putService(new ProviderServiceA(p, "Signature", "SHA1withECDSA", "sun.security.mscapi.CSignature$SHA1withECDSA", attrs));
                SunMSCAPI.this.putService(new ProviderServiceA(p, "Signature", "SHA224withECDSA", "sun.security.mscapi.CSignature$SHA224withECDSA", attrs));
                SunMSCAPI.this.putService(new ProviderServiceA(p, "Signature", "SHA256withECDSA", "sun.security.mscapi.CSignature$SHA256withECDSA", attrs));
                SunMSCAPI.this.putService(new ProviderServiceA(p, "Signature", "SHA384withECDSA", "sun.security.mscapi.CSignature$SHA384withECDSA", attrs));
                SunMSCAPI.this.putService(new ProviderServiceA(p, "Signature", "SHA512withECDSA", "sun.security.mscapi.CSignature$SHA512withECDSA", attrs));
                attrs.clear();
                attrs.put("KeySize", "16384");
                SunMSCAPI.this.putService(new ProviderService(p, "KeyPairGenerator", "RSA", "sun.security.mscapi.CKeyPairGenerator$RSA", null, attrs));
                attrs.clear();
                attrs.put("SupportedModes", "ECB");
                attrs.put("SupportedPaddings", "PKCS1PADDING");
                attrs.put("SupportedKeyClasses", "sun.security.mscapi.CKey");
                SunMSCAPI.this.putService(new ProviderService(p, "Cipher", "RSA", "sun.security.mscapi.CRSACipher", null, attrs));
                SunMSCAPI.this.putService(new ProviderService(p, "Cipher", "RSA/ECB/PKCS1Padding", "sun.security.mscapi.CRSACipher", null, attrs));
                return null;
            }
        });
    }

    static {
        Void void_ = AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                System.loadLibrary("sunmscapi");
                return null;
            }
        });
    }

    private static class ProviderService
    extends Provider.Service {
        ProviderService(Provider p, String type, String algo, String cn) {
            super(p, type, algo, cn, null, null);
        }

        ProviderService(Provider p, String type, String algo, String cn, List<String> aliases, HashMap<String, String> attrs) {
            super(p, type, algo, cn, aliases, attrs);
        }

        @Override
        public Object newInstance(Object ctrParamObj) throws NoSuchAlgorithmException {
            String type = this.getType();
            if (ctrParamObj != null) {
                throw new InvalidParameterException("constructorParameter not used with " + type + " engines");
            }
            String algo = this.getAlgorithm();
            try {
                if (type.equals("SecureRandom")) {
                    if (algo.equals("Windows-PRNG")) {
                        return new PRNG();
                    }
                } else if (type.equals("KeyStore")) {
                    if (algo.equals("Windows-MY") || algo.equals("Windows-MY-CURRENTUSER")) {
                        return new CKeyStore.MY();
                    }
                    if (algo.equals("Windows-ROOT") || algo.equals("Windows-ROOT-CURRENTUSER")) {
                        return new CKeyStore.ROOT();
                    }
                    if (algo.equals("Windows-MY-LOCALMACHINE")) {
                        return new CKeyStore.MYLocalMachine();
                    }
                    if (algo.equals("Windows-ROOT-LOCALMACHINE")) {
                        return new CKeyStore.ROOTLocalMachine();
                    }
                } else if (type.equals("Signature")) {
                    if (algo.equals("NONEwithRSA")) {
                        return new CSignature.NONEwithRSA();
                    }
                    if (algo.equals("SHA1withRSA")) {
                        return new CSignature.SHA1withRSA();
                    }
                    if (algo.equals("SHA256withRSA")) {
                        return new CSignature.SHA256withRSA();
                    }
                    if (algo.equals("SHA384withRSA")) {
                        return new CSignature.SHA384withRSA();
                    }
                    if (algo.equals("SHA512withRSA")) {
                        return new CSignature.SHA512withRSA();
                    }
                    if (algo.equals("MD5withRSA")) {
                        return new CSignature.MD5withRSA();
                    }
                    if (algo.equals("MD2withRSA")) {
                        return new CSignature.MD2withRSA();
                    }
                    if (algo.equals("RSASSA-PSS")) {
                        return new CSignature.PSS();
                    }
                    if (algo.equals("SHA1withECDSA")) {
                        return new CSignature.SHA1withECDSA();
                    }
                    if (algo.equals("SHA224withECDSA")) {
                        return new CSignature.SHA224withECDSA();
                    }
                    if (algo.equals("SHA256withECDSA")) {
                        return new CSignature.SHA256withECDSA();
                    }
                    if (algo.equals("SHA384withECDSA")) {
                        return new CSignature.SHA384withECDSA();
                    }
                    if (algo.equals("SHA512withECDSA")) {
                        return new CSignature.SHA512withECDSA();
                    }
                } else if (type.equals("KeyPairGenerator")) {
                    if (algo.equals("RSA")) {
                        return new CKeyPairGenerator.RSA();
                    }
                } else if (type.equals("Cipher") && (algo.equals("RSA") || algo.equals("RSA/ECB/PKCS1Padding"))) {
                    return new CRSACipher();
                }
            }
            catch (Exception ex) {
                throw new NoSuchAlgorithmException("Error constructing " + type + " for " + algo + " using SunMSCAPI", ex);
            }
            throw new ProviderException("No impl for " + algo + " " + type);
        }
    }

    private static class ProviderServiceA
    extends ProviderService {
        ProviderServiceA(Provider p, String type, String algo, String cn, HashMap<String, String> attrs) {
            super(p, type, algo, cn, SecurityProviderConstants.getAliases(algo), attrs);
        }
    }
}

