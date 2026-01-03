/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;

public class CertificateAlgorithmId
implements DerEncoder {
    private AlgorithmId algId;
    public static final String NAME = "algorithmID";

    public CertificateAlgorithmId(AlgorithmId algId) {
        this.algId = algId;
    }

    public CertificateAlgorithmId(DerInputStream in) throws IOException {
        DerValue val = in.getDerValue();
        this.algId = AlgorithmId.parse(val);
    }

    public CertificateAlgorithmId(InputStream in) throws IOException {
        DerValue val = new DerValue(in);
        this.algId = AlgorithmId.parse(val);
    }

    public String toString() {
        if (this.algId == null) {
            return "";
        }
        return this.algId.toString() + ", OID = " + this.algId.getOID().toString() + "\n";
    }

    @Override
    public void encode(DerOutputStream out) {
        this.algId.encode(out);
    }

    public AlgorithmId getAlgId() throws IOException {
        return this.algId;
    }
}

