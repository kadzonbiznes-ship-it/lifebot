/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.HexDumpEncoder;
import sun.security.x509.AlgorithmId;

public class KeyIdentifier {
    private final byte[] octetString;

    public KeyIdentifier(byte[] octetString) {
        this.octetString = (byte[])octetString.clone();
    }

    public KeyIdentifier(DerValue val) throws IOException {
        this.octetString = val.getOctetString();
    }

    public KeyIdentifier(PublicKey pubKey) throws IOException {
        MessageDigest md;
        DerValue algAndKey = new DerValue(pubKey.getEncoded());
        if (algAndKey.tag != 48) {
            throw new IOException("PublicKey value is not a valid X.509 public key");
        }
        AlgorithmId algid = AlgorithmId.parse(algAndKey.data.getDerValue());
        byte[] key = algAndKey.data.getUnalignedBitString().toByteArray();
        try {
            md = MessageDigest.getInstance("SHA1");
        }
        catch (NoSuchAlgorithmException e3) {
            throw new IOException("SHA1 not supported");
        }
        md.update(key);
        this.octetString = md.digest();
    }

    public byte[] getIdentifier() {
        return (byte[])this.octetString.clone();
    }

    public String toString() {
        String s = "KeyIdentifier [\n";
        HexDumpEncoder encoder = new HexDumpEncoder();
        s = s + encoder.encodeBuffer(this.octetString);
        s = s + "]\n";
        return s;
    }

    void encode(DerOutputStream out) {
        out.putOctetString(this.octetString);
    }

    public int hashCode() {
        int retval = 0;
        for (int i = 0; i < this.octetString.length; ++i) {
            retval += this.octetString[i] * i;
        }
        return retval;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof KeyIdentifier)) {
            return false;
        }
        byte[] otherString = ((KeyIdentifier)other).octetString;
        return Arrays.equals(this.octetString, otherString);
    }
}

