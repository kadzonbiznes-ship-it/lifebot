/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLHandshakeException;
import sun.security.ssl.HKDF;
import sun.security.ssl.Record;
import sun.security.ssl.SSLKeyDerivation;

final class SSLBasicKeyDerivation
implements SSLKeyDerivation {
    private final String hashAlg;
    private final SecretKey secret;
    private final byte[] hkdfInfo;

    SSLBasicKeyDerivation(SecretKey secret, String hashAlg, byte[] label, byte[] context, int length) {
        this.hashAlg = hashAlg.replace("-", "");
        this.secret = secret;
        this.hkdfInfo = SSLBasicKeyDerivation.createHkdfInfo(label, context, length);
    }

    @Override
    public SecretKey deriveKey(String algorithm, AlgorithmParameterSpec keySpec) throws IOException {
        try {
            HKDF hkdf = new HKDF(this.hashAlg);
            return hkdf.expand(this.secret, this.hkdfInfo, ((SecretSizeSpec)keySpec).length, algorithm);
        }
        catch (GeneralSecurityException gse) {
            throw new SSLHandshakeException("Could not generate secret", gse);
        }
    }

    private static byte[] createHkdfInfo(byte[] label, byte[] context, int length) {
        byte[] info = new byte[4 + label.length + context.length];
        ByteBuffer m = ByteBuffer.wrap(info);
        try {
            Record.putInt16(m, length);
            Record.putBytes8(m, label);
            Record.putBytes8(m, context);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return info;
    }

    static class SecretSizeSpec
    implements AlgorithmParameterSpec {
        final int length;

        SecretSizeSpec(int length) {
            this.length = length;
        }
    }
}

