/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.security.AccessController;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.ProviderException;
import java.util.HashMap;
import java.util.List;
import sun.security.ec.ECDHKeyAgreement;
import sun.security.ec.ECDSASignature;
import sun.security.ec.ECKeyFactory;
import sun.security.ec.ECKeyPairGenerator;
import sun.security.ec.XDHKeyAgreement;
import sun.security.ec.XDHKeyFactory;
import sun.security.ec.XDHKeyPairGenerator;
import sun.security.ec.ed.EdDSAKeyFactory;
import sun.security.ec.ed.EdDSAKeyPairGenerator;
import sun.security.ec.ed.EdDSASignature;
import sun.security.util.CurveDB;
import sun.security.util.ECParameters;
import sun.security.util.NamedCurve;
import sun.security.util.SecurityConstants;
import sun.security.util.SecurityProviderConstants;

public final class SunEC
extends Provider {
    private static final long serialVersionUID = -2279741672933606418L;

    public SunEC() {
        super("SunEC", SecurityConstants.PROVIDER_VER, "Sun Elliptic Curve provider");
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                SunEC.this.putEntries();
                return null;
            }
        });
    }

    void putEntries() {
        HashMap<String, String> ATTRS = new HashMap<String, String>(3);
        ATTRS.put("ImplementedIn", "Software");
        String ecKeyClasses = "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey";
        ATTRS.put("SupportedKeyClasses", ecKeyClasses);
        ATTRS.put("KeySize", "256");
        this.putService(new ProviderServiceA(this, "KeyFactory", "EC", "sun.security.ec.ECKeyFactory", ATTRS));
        boolean firstCurve = true;
        StringBuilder names = new StringBuilder();
        for (NamedCurve namedCurve : List.of(CurveDB.P_256, CurveDB.P_384, CurveDB.P_521)) {
            String[] commonNames;
            if (!firstCurve) {
                names.append("|");
            } else {
                firstCurve = false;
            }
            names.append("[");
            for (String commonName : commonNames = namedCurve.getNameAndAliases()) {
                names.append(commonName);
                names.append(",");
            }
            names.append(namedCurve.getObjectId());
            names.append("]");
        }
        HashMap<String, String> apAttrs = new HashMap<String, String>(ATTRS);
        apAttrs.put("SupportedCurves", names.toString());
        this.putService(new ProviderServiceA(this, "AlgorithmParameters", "EC", "sun.security.util.ECParameters", apAttrs));
        this.putXDHEntries();
        this.putEdDSAEntries();
        this.putService(new ProviderService((Provider)this, "Signature", "NONEwithECDSA", "sun.security.ec.ECDSASignature$Raw", null, ATTRS));
        this.putService(new ProviderServiceA(this, "Signature", "SHA1withECDSA", "sun.security.ec.ECDSASignature$SHA1", ATTRS));
        this.putService(new ProviderServiceA(this, "Signature", "SHA224withECDSA", "sun.security.ec.ECDSASignature$SHA224", ATTRS));
        this.putService(new ProviderServiceA(this, "Signature", "SHA256withECDSA", "sun.security.ec.ECDSASignature$SHA256", ATTRS));
        this.putService(new ProviderServiceA(this, "Signature", "SHA384withECDSA", "sun.security.ec.ECDSASignature$SHA384", ATTRS));
        this.putService(new ProviderServiceA(this, "Signature", "SHA512withECDSA", "sun.security.ec.ECDSASignature$SHA512", ATTRS));
        this.putService(new ProviderServiceA(this, "Signature", "SHA3-224withECDSA", "sun.security.ec.ECDSASignature$SHA3_224", ATTRS));
        this.putService(new ProviderServiceA(this, "Signature", "SHA3-256withECDSA", "sun.security.ec.ECDSASignature$SHA3_256", ATTRS));
        this.putService(new ProviderServiceA(this, "Signature", "SHA3-384withECDSA", "sun.security.ec.ECDSASignature$SHA3_384", ATTRS));
        this.putService(new ProviderServiceA(this, "Signature", "SHA3-512withECDSA", "sun.security.ec.ECDSASignature$SHA3_512", ATTRS));
        this.putService(new ProviderService(this, "Signature", "NONEwithECDSAinP1363Format", "sun.security.ec.ECDSASignature$RawinP1363Format"));
        this.putService(new ProviderService(this, "Signature", "SHA1withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA1inP1363Format"));
        this.putService(new ProviderService(this, "Signature", "SHA224withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA224inP1363Format"));
        this.putService(new ProviderService(this, "Signature", "SHA256withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA256inP1363Format"));
        this.putService(new ProviderService(this, "Signature", "SHA384withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA384inP1363Format"));
        this.putService(new ProviderService(this, "Signature", "SHA512withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA512inP1363Format"));
        this.putService(new ProviderService(this, "Signature", "SHA3-224withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA3_224inP1363Format"));
        this.putService(new ProviderService(this, "Signature", "SHA3-256withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA3_256inP1363Format"));
        this.putService(new ProviderService(this, "Signature", "SHA3-384withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA3_384inP1363Format"));
        this.putService(new ProviderService(this, "Signature", "SHA3-512withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA3_512inP1363Format"));
        this.putService(new ProviderServiceA(this, "KeyPairGenerator", "EC", "sun.security.ec.ECKeyPairGenerator", ATTRS));
        this.putService(new ProviderService((Provider)this, "KeyAgreement", "ECDH", "sun.security.ec.ECDHKeyAgreement", null, ATTRS));
    }

    private void putXDHEntries() {
        HashMap<String, String> ATTRS = new HashMap<String, String>(1);
        ATTRS.put("ImplementedIn", "Software");
        this.putService(new ProviderService((Provider)this, "KeyFactory", "XDH", "sun.security.ec.XDHKeyFactory", null, ATTRS));
        this.putService(new ProviderServiceA(this, "KeyFactory", "X25519", "sun.security.ec.XDHKeyFactory.X25519", ATTRS));
        this.putService(new ProviderServiceA(this, "KeyFactory", "X448", "sun.security.ec.XDHKeyFactory.X448", ATTRS));
        this.putService(new ProviderService((Provider)this, "KeyPairGenerator", "XDH", "sun.security.ec.XDHKeyPairGenerator", null, ATTRS));
        this.putService(new ProviderServiceA(this, "KeyPairGenerator", "X25519", "sun.security.ec.XDHKeyPairGenerator.X25519", ATTRS));
        this.putService(new ProviderServiceA(this, "KeyPairGenerator", "X448", "sun.security.ec.XDHKeyPairGenerator.X448", ATTRS));
        this.putService(new ProviderService((Provider)this, "KeyAgreement", "XDH", "sun.security.ec.XDHKeyAgreement", null, ATTRS));
        this.putService(new ProviderServiceA(this, "KeyAgreement", "X25519", "sun.security.ec.XDHKeyAgreement.X25519", ATTRS));
        this.putService(new ProviderServiceA(this, "KeyAgreement", "X448", "sun.security.ec.XDHKeyAgreement.X448", ATTRS));
    }

    private void putEdDSAEntries() {
        HashMap<String, String> ATTRS = new HashMap<String, String>(1);
        ATTRS.put("ImplementedIn", "Software");
        this.putService(new ProviderService((Provider)this, "KeyFactory", "EdDSA", "sun.security.ec.ed.EdDSAKeyFactory", null, ATTRS));
        this.putService(new ProviderServiceA(this, "KeyFactory", "Ed25519", "sun.security.ec.ed.EdDSAKeyFactory.Ed25519", ATTRS));
        this.putService(new ProviderServiceA(this, "KeyFactory", "Ed448", "sun.security.ec.ed.EdDSAKeyFactory.Ed448", ATTRS));
        this.putService(new ProviderService((Provider)this, "KeyPairGenerator", "EdDSA", "sun.security.ec.ed.EdDSAKeyPairGenerator", null, ATTRS));
        this.putService(new ProviderServiceA(this, "KeyPairGenerator", "Ed25519", "sun.security.ec.ed.EdDSAKeyPairGenerator.Ed25519", ATTRS));
        this.putService(new ProviderServiceA(this, "KeyPairGenerator", "Ed448", "sun.security.ec.ed.EdDSAKeyPairGenerator.Ed448", ATTRS));
        this.putService(new ProviderService((Provider)this, "Signature", "EdDSA", "sun.security.ec.ed.EdDSASignature", null, ATTRS));
        this.putService(new ProviderServiceA(this, "Signature", "Ed25519", "sun.security.ec.ed.EdDSASignature.Ed25519", ATTRS));
        this.putService(new ProviderServiceA(this, "Signature", "Ed448", "sun.security.ec.ed.EdDSASignature.Ed448", ATTRS));
    }

    private static class ProviderServiceA
    extends ProviderService {
        ProviderServiceA(Provider p, String type, String algo, String cn, HashMap<String, String> attrs) {
            super(p, type, algo, cn, SecurityProviderConstants.getAliases(algo), attrs);
        }
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
                if (type.equals("Signature")) {
                    if (algo.equalsIgnoreCase("EdDSA")) {
                        return new EdDSASignature();
                    }
                    if (algo.equalsIgnoreCase("Ed25519")) {
                        return new EdDSASignature.Ed25519();
                    }
                    if (algo.equalsIgnoreCase("Ed448")) {
                        return new EdDSASignature.Ed448();
                    }
                    boolean inP1363 = algo.endsWith("inP1363Format");
                    if (inP1363) {
                        algo = algo.substring(0, algo.length() - 13);
                    }
                    if (algo.equals("SHA1withECDSA")) {
                        return inP1363 ? new ECDSASignature.SHA1inP1363Format() : new ECDSASignature.SHA1();
                    }
                    if (algo.equals("SHA224withECDSA")) {
                        return inP1363 ? new ECDSASignature.SHA224inP1363Format() : new ECDSASignature.SHA224();
                    }
                    if (algo.equals("SHA256withECDSA")) {
                        return inP1363 ? new ECDSASignature.SHA256inP1363Format() : new ECDSASignature.SHA256();
                    }
                    if (algo.equals("SHA384withECDSA")) {
                        return inP1363 ? new ECDSASignature.SHA384inP1363Format() : new ECDSASignature.SHA384();
                    }
                    if (algo.equals("SHA512withECDSA")) {
                        return inP1363 ? new ECDSASignature.SHA512inP1363Format() : new ECDSASignature.SHA512();
                    }
                    if (algo.equals("NONEwithECDSA")) {
                        return inP1363 ? new ECDSASignature.RawinP1363Format() : new ECDSASignature.Raw();
                    }
                    if (algo.equals("SHA3-224withECDSA")) {
                        return inP1363 ? new ECDSASignature.SHA3_224inP1363Format() : new ECDSASignature.SHA3_224();
                    }
                    if (algo.equals("SHA3-256withECDSA")) {
                        return inP1363 ? new ECDSASignature.SHA3_256inP1363Format() : new ECDSASignature.SHA3_256();
                    }
                    if (algo.equals("SHA3-384withECDSA")) {
                        return inP1363 ? new ECDSASignature.SHA3_384inP1363Format() : new ECDSASignature.SHA3_384();
                    }
                    if (algo.equals("SHA3-512withECDSA")) {
                        return inP1363 ? new ECDSASignature.SHA3_512inP1363Format() : new ECDSASignature.SHA3_512();
                    }
                } else if (type.equals("KeyFactory")) {
                    if (algo.equals("EC")) {
                        return new ECKeyFactory();
                    }
                    if (algo.equals("XDH")) {
                        return new XDHKeyFactory();
                    }
                    if (algo.equals("X25519")) {
                        return new XDHKeyFactory.X25519();
                    }
                    if (algo.equals("X448")) {
                        return new XDHKeyFactory.X448();
                    }
                    if (algo.equalsIgnoreCase("EdDSA")) {
                        return new EdDSAKeyFactory();
                    }
                    if (algo.equalsIgnoreCase("Ed25519")) {
                        return new EdDSAKeyFactory.Ed25519();
                    }
                    if (algo.equalsIgnoreCase("Ed448")) {
                        return new EdDSAKeyFactory.Ed448();
                    }
                } else if (type.equals("AlgorithmParameters")) {
                    if (algo.equals("EC")) {
                        return new ECParameters();
                    }
                } else if (type.equals("KeyPairGenerator")) {
                    if (algo.equals("EC")) {
                        return new ECKeyPairGenerator();
                    }
                    if (algo.equals("XDH")) {
                        return new XDHKeyPairGenerator();
                    }
                    if (algo.equals("X25519")) {
                        return new XDHKeyPairGenerator.X25519();
                    }
                    if (algo.equals("X448")) {
                        return new XDHKeyPairGenerator.X448();
                    }
                    if (algo.equalsIgnoreCase("EdDSA")) {
                        return new EdDSAKeyPairGenerator();
                    }
                    if (algo.equalsIgnoreCase("Ed25519")) {
                        return new EdDSAKeyPairGenerator.Ed25519();
                    }
                    if (algo.equalsIgnoreCase("Ed448")) {
                        return new EdDSAKeyPairGenerator.Ed448();
                    }
                } else if (type.equals("KeyAgreement")) {
                    if (algo.equals("ECDH")) {
                        return new ECDHKeyAgreement();
                    }
                    if (algo.equals("XDH")) {
                        return new XDHKeyAgreement();
                    }
                    if (algo.equals("X25519")) {
                        return new XDHKeyAgreement.X25519();
                    }
                    if (algo.equals("X448")) {
                        return new XDHKeyAgreement.X448();
                    }
                }
            }
            catch (Exception ex) {
                throw new NoSuchAlgorithmException("Error constructing " + type + " for " + algo + " using SunEC", ex);
            }
            throw new ProviderException("No impl for " + algo + " " + type);
        }
    }
}

