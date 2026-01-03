/*
 * Decompiled with CFR 0.152.
 */
package java.security.spec;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.util.Objects;

public class PSSParameterSpec
implements AlgorithmParameterSpec {
    private final String mdName;
    private final String mgfName;
    private final AlgorithmParameterSpec mgfSpec;
    private final int saltLen;
    private final int trailerField;
    public static final int TRAILER_FIELD_BC = 1;
    @Deprecated(since="19")
    public static final PSSParameterSpec DEFAULT = new PSSParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, 20, 1);

    public PSSParameterSpec(String mdName, String mgfName, AlgorithmParameterSpec mgfSpec, int saltLen, int trailerField) {
        Objects.requireNonNull(mdName, "digest algorithm is null");
        Objects.requireNonNull(mgfName, "mask generation function algorithm is null");
        if (saltLen < 0) {
            throw new IllegalArgumentException("negative saltLen value: " + saltLen);
        }
        if (trailerField < 0) {
            throw new IllegalArgumentException("negative trailerField: " + trailerField);
        }
        this.mdName = mdName;
        this.mgfName = mgfName;
        this.mgfSpec = mgfSpec;
        this.saltLen = saltLen;
        this.trailerField = trailerField;
    }

    @Deprecated(since="19")
    public PSSParameterSpec(int saltLen) {
        this("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, saltLen, 1);
    }

    public String getDigestAlgorithm() {
        return this.mdName;
    }

    public String getMGFAlgorithm() {
        return this.mgfName;
    }

    public AlgorithmParameterSpec getMGFParameters() {
        return this.mgfSpec;
    }

    public int getSaltLength() {
        return this.saltLen;
    }

    public int getTrailerField() {
        return this.trailerField;
    }

    public String toString() {
        return "PSSParameterSpec[hashAlgorithm=" + this.mdName + ", maskGenAlgorithm=" + this.mgfSpec + ", saltLength=" + this.saltLen + ", trailerField=" + this.trailerField + ']';
    }
}

