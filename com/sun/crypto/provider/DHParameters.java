/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.DHParameterSpec;
import sun.security.util.Debug;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public final class DHParameters
extends AlgorithmParametersSpi {
    private BigInteger p = BigInteger.ZERO;
    private BigInteger g = BigInteger.ZERO;
    private int l = 0;

    @Override
    protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
        if (!(paramSpec instanceof DHParameterSpec)) {
            throw new InvalidParameterSpecException("Inappropriate parameter specification");
        }
        this.p = ((DHParameterSpec)paramSpec).getP();
        this.g = ((DHParameterSpec)paramSpec).getG();
        this.l = ((DHParameterSpec)paramSpec).getL();
    }

    @Override
    protected void engineInit(byte[] params) throws IOException {
        try {
            DerValue encodedParams = new DerValue(params);
            if (encodedParams.tag != 48) {
                throw new IOException("DH params parsing error");
            }
            encodedParams.data.reset();
            this.p = encodedParams.data.getBigInteger();
            this.g = encodedParams.data.getBigInteger();
            if (encodedParams.data.available() != 0) {
                this.l = encodedParams.data.getInteger();
            }
            if (encodedParams.data.available() != 0) {
                throw new IOException("DH parameter parsing error: Extra data");
            }
        }
        catch (NumberFormatException e) {
            throw new IOException("Private-value length too big");
        }
    }

    @Override
    protected void engineInit(byte[] params, String decodingMethod) throws IOException {
        this.engineInit(params);
    }

    @Override
    protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec) throws InvalidParameterSpecException {
        if (paramSpec.isAssignableFrom(DHParameterSpec.class)) {
            return (T)((AlgorithmParameterSpec)paramSpec.cast(new DHParameterSpec(this.p, this.g, this.l)));
        }
        throw new InvalidParameterSpecException("Inappropriate parameter Specification");
    }

    @Override
    protected byte[] engineGetEncoded() throws IOException {
        DerOutputStream out = new DerOutputStream();
        DerOutputStream bytes = new DerOutputStream();
        bytes.putInteger(this.p);
        bytes.putInteger(this.g);
        if (this.l > 0) {
            bytes.putInteger(this.l);
        }
        out.write((byte)48, bytes);
        return out.toByteArray();
    }

    @Override
    protected byte[] engineGetEncoded(String encodingMethod) throws IOException {
        return this.engineGetEncoded();
    }

    @Override
    protected String engineToString() {
        String LINE_SEP = System.lineSeparator();
        StringBuilder sb = new StringBuilder("SunJCE Diffie-Hellman Parameters:" + LINE_SEP + "p:" + LINE_SEP + Debug.toHexString(this.p) + LINE_SEP + "g:" + LINE_SEP + Debug.toHexString(this.g));
        if (this.l != 0) {
            sb.append(LINE_SEP + "l:" + LINE_SEP + "    " + this.l);
        }
        return sb.toString();
    }
}

