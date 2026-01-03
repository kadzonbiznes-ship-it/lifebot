/*
 * Decompiled with CFR 0.152.
 */
package sun.security.rsa;

import java.io.IOException;
import java.security.AlgorithmParametersSpi;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;

public final class PSSParameters
extends AlgorithmParametersSpi {
    private PSSParameterSpec spec;

    @Override
    protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
        if (!(paramSpec instanceof PSSParameterSpec)) {
            throw new InvalidParameterSpecException("Inappropriate parameter specification");
        }
        PSSParameterSpec spec = (PSSParameterSpec)paramSpec;
        String mgfName = spec.getMGFAlgorithm();
        if (!spec.getMGFAlgorithm().equalsIgnoreCase("MGF1")) {
            throw new InvalidParameterSpecException("Unsupported mgf " + mgfName + "; MGF1 only");
        }
        AlgorithmParameterSpec mgfSpec = spec.getMGFParameters();
        if (!(mgfSpec instanceof MGF1ParameterSpec)) {
            throw new InvalidParameterSpecException("Inappropriate mgf parameters; non-null MGF1ParameterSpec only");
        }
        this.spec = spec;
    }

    @Override
    protected void engineInit(byte[] encoded) throws IOException {
        DerValue[] datum;
        String mdName = "SHA-1";
        MGF1ParameterSpec mgfSpec = MGF1ParameterSpec.SHA1;
        int saltLength = 20;
        int trailerField = 1;
        DerInputStream der = new DerInputStream(encoded);
        for (DerValue d : datum = der.getSequence(4)) {
            if (d.isContextSpecific((byte)0)) {
                mdName = AlgorithmId.parse(d.data.getDerValue()).getName();
                continue;
            }
            if (d.isContextSpecific((byte)1)) {
                String mgfDigestName;
                AlgorithmId val = AlgorithmId.parse(d.data.getDerValue());
                if (!val.getOID().equals(AlgorithmId.MGF1_oid)) {
                    throw new IOException("Only MGF1 mgf is supported");
                }
                byte[] encodedParams = val.getEncodedParams();
                if (encodedParams == null) {
                    throw new IOException("Missing MGF1 parameters");
                }
                AlgorithmId params = AlgorithmId.parse(new DerValue(encodedParams));
                switch (mgfDigestName = params.getName()) {
                    case "SHA-1": {
                        mgfSpec = MGF1ParameterSpec.SHA1;
                        break;
                    }
                    case "SHA-224": {
                        mgfSpec = MGF1ParameterSpec.SHA224;
                        break;
                    }
                    case "SHA-256": {
                        mgfSpec = MGF1ParameterSpec.SHA256;
                        break;
                    }
                    case "SHA-384": {
                        mgfSpec = MGF1ParameterSpec.SHA384;
                        break;
                    }
                    case "SHA-512": {
                        mgfSpec = MGF1ParameterSpec.SHA512;
                        break;
                    }
                    case "SHA-512/224": {
                        mgfSpec = MGF1ParameterSpec.SHA512_224;
                        break;
                    }
                    case "SHA-512/256": {
                        mgfSpec = MGF1ParameterSpec.SHA512_256;
                        break;
                    }
                    case "SHA3-224": {
                        mgfSpec = MGF1ParameterSpec.SHA3_224;
                        break;
                    }
                    case "SHA3-256": {
                        mgfSpec = MGF1ParameterSpec.SHA3_256;
                        break;
                    }
                    case "SHA3-384": {
                        mgfSpec = MGF1ParameterSpec.SHA3_384;
                        break;
                    }
                    case "SHA3-512": {
                        mgfSpec = MGF1ParameterSpec.SHA3_512;
                        break;
                    }
                    default: {
                        throw new IOException("Unrecognized message digest algorithm " + mgfDigestName);
                    }
                }
                continue;
            }
            if (d.isContextSpecific((byte)2)) {
                saltLength = d.data.getDerValue().getInteger();
                if (saltLength >= 0) continue;
                throw new IOException("Negative value for saltLength");
            }
            if (d.isContextSpecific((byte)3)) {
                trailerField = d.data.getDerValue().getInteger();
                if (trailerField == 1) continue;
                throw new IOException("Unsupported trailerField value " + trailerField);
            }
            throw new IOException("Invalid encoded PSSParameters");
        }
        this.spec = new PSSParameterSpec(mdName, "MGF1", mgfSpec, saltLength, trailerField);
    }

    @Override
    protected void engineInit(byte[] encoded, String decodingMethod) throws IOException {
        if (decodingMethod != null && !decodingMethod.equalsIgnoreCase("ASN.1")) {
            throw new IllegalArgumentException("Only support ASN.1 format");
        }
        this.engineInit(encoded);
    }

    @Override
    protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec) throws InvalidParameterSpecException {
        if (paramSpec.isAssignableFrom(PSSParameterSpec.class)) {
            return (T)((AlgorithmParameterSpec)paramSpec.cast(this.spec));
        }
        throw new InvalidParameterSpecException("Inappropriate parameter specification");
    }

    @Override
    protected byte[] engineGetEncoded() throws IOException {
        return PSSParameters.getEncoded(this.spec);
    }

    @Override
    protected byte[] engineGetEncoded(String encMethod) throws IOException {
        if (encMethod != null && !encMethod.equalsIgnoreCase("ASN.1")) {
            throw new IllegalArgumentException("Only support ASN.1 format");
        }
        return this.engineGetEncoded();
    }

    @Override
    protected String engineToString() {
        return this.spec.toString();
    }

    public static byte[] getEncoded(PSSParameterSpec spec) throws IOException {
        AlgorithmId mgfDigestId;
        DerOutputStream tmp2;
        AlgorithmId mdAlgId;
        AlgorithmParameterSpec mgfSpec = spec.getMGFParameters();
        if (!(mgfSpec instanceof MGF1ParameterSpec)) {
            throw new IOException("Cannot encode " + mgfSpec);
        }
        MGF1ParameterSpec mgf1Spec = (MGF1ParameterSpec)mgfSpec;
        DerOutputStream tmp = new DerOutputStream();
        try {
            mdAlgId = AlgorithmId.get(spec.getDigestAlgorithm());
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new IOException("AlgorithmId " + spec.getDigestAlgorithm() + " impl not found");
        }
        if (!mdAlgId.getOID().equals(AlgorithmId.SHA_oid)) {
            tmp2 = new DerOutputStream();
            mdAlgId.encode(tmp2);
            tmp.write(DerValue.createTag((byte)-128, true, (byte)0), tmp2);
        }
        try {
            mgfDigestId = AlgorithmId.get(mgf1Spec.getDigestAlgorithm());
        }
        catch (NoSuchAlgorithmException nase) {
            throw new IOException("AlgorithmId " + mgf1Spec.getDigestAlgorithm() + " impl not found");
        }
        if (!mgfDigestId.getOID().equals(AlgorithmId.SHA_oid)) {
            tmp2 = new DerOutputStream();
            tmp2.putOID(AlgorithmId.MGF1_oid);
            mgfDigestId.encode(tmp2);
            DerOutputStream tmp3 = new DerOutputStream();
            tmp3.write((byte)48, tmp2);
            tmp.write(DerValue.createTag((byte)-128, true, (byte)1), tmp3);
        }
        if (spec.getSaltLength() != 20) {
            tmp2 = new DerOutputStream();
            tmp2.putInteger(spec.getSaltLength());
            tmp.write(DerValue.createTag((byte)-128, true, (byte)2), tmp2);
        }
        if (spec.getTrailerField() != 1) {
            tmp2 = new DerOutputStream();
            tmp2.putInteger(spec.getTrailerField());
            tmp.write(DerValue.createTag((byte)-128, true, (byte)3), tmp2);
        }
        DerOutputStream out = new DerOutputStream();
        out.write((byte)48, tmp);
        return out.toByteArray();
    }
}

