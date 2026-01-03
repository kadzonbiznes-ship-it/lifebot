/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto.spec;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Locale;
import javax.crypto.SecretKey;
import jdk.internal.access.SharedSecrets;

public class SecretKeySpec
implements KeySpec,
SecretKey {
    private static final long serialVersionUID = 6577238317307289933L;
    private byte[] key;
    private final String algorithm;

    public SecretKeySpec(byte[] key, String algorithm) {
        String errMsg = SecretKeySpec.doSanityCheck(key, algorithm);
        if (errMsg != null) {
            throw new IllegalArgumentException(errMsg);
        }
        this.key = (byte[])key.clone();
        this.algorithm = algorithm;
    }

    public SecretKeySpec(byte[] key, int offset, int len, String algorithm) {
        if (key == null || algorithm == null) {
            throw new IllegalArgumentException("Missing argument");
        }
        if (key.length == 0) {
            throw new IllegalArgumentException("Empty key");
        }
        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException("offset is negative");
        }
        if (len < 0) {
            throw new ArrayIndexOutOfBoundsException("len is negative");
        }
        if (key.length - offset < len) {
            throw new IllegalArgumentException("Invalid offset/length combination");
        }
        this.key = new byte[len];
        System.arraycopy(key, offset, this.key, 0, len);
        this.algorithm = algorithm;
    }

    @Override
    public String getAlgorithm() {
        return this.algorithm;
    }

    @Override
    public String getFormat() {
        return "RAW";
    }

    @Override
    public byte[] getEncoded() {
        return (byte[])this.key.clone();
    }

    public int hashCode() {
        int retval = 0;
        for (int i = 1; i < this.key.length; ++i) {
            retval += this.key[i] * i;
        }
        if (this.algorithm.equalsIgnoreCase("TripleDES")) {
            return retval ^ "desede".hashCode();
        }
        return retval ^ this.algorithm.toLowerCase(Locale.ENGLISH).hashCode();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SecretKey)) {
            return false;
        }
        String thatAlg = ((SecretKey)obj).getAlgorithm();
        if (!(thatAlg.equalsIgnoreCase(this.algorithm) || thatAlg.equalsIgnoreCase("DESede") && this.algorithm.equalsIgnoreCase("TripleDES") || thatAlg.equalsIgnoreCase("TripleDES") && this.algorithm.equalsIgnoreCase("DESede"))) {
            return false;
        }
        byte[] thatKey = ((SecretKey)obj).getEncoded();
        try {
            boolean bl = MessageDigest.isEqual(this.key, thatKey);
            return bl;
        }
        finally {
            if (thatKey != null) {
                Arrays.fill(thatKey, (byte)0);
            }
        }
    }

    void clear() {
        Arrays.fill(this.key, (byte)0);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        String errMsg = SecretKeySpec.doSanityCheck(this.key, this.algorithm);
        if (errMsg != null) {
            throw new InvalidObjectException(errMsg);
        }
        byte[] temp = this.key;
        this.key = (byte[])temp.clone();
        Arrays.fill(temp, (byte)0);
    }

    private static String doSanityCheck(byte[] key, String algorithm) {
        String errMsg = null;
        if (key == null || algorithm == null) {
            errMsg = "Missing argument";
        } else if (key.length == 0) {
            errMsg = "Empty key";
        }
        return errMsg;
    }

    static {
        SharedSecrets.setJavaxCryptoSpecAccess(SecretKeySpec::clear);
    }
}

