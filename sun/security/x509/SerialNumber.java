/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class SerialNumber {
    private BigInteger serialNum;

    private void construct(DerValue derVal) throws IOException {
        this.serialNum = derVal.getBigInteger();
        if (derVal.data.available() != 0) {
            throw new IOException("Excess SerialNumber data");
        }
    }

    public SerialNumber(BigInteger num) {
        this.serialNum = num;
    }

    public SerialNumber(int num) {
        this.serialNum = BigInteger.valueOf(num);
    }

    public SerialNumber(DerInputStream in) throws IOException {
        DerValue derVal = in.getDerValue();
        this.construct(derVal);
    }

    public SerialNumber(DerValue val) throws IOException {
        this.construct(val);
    }

    public SerialNumber(InputStream in) throws IOException {
        DerValue derVal = new DerValue(in);
        this.construct(derVal);
    }

    public String toString() {
        return "SerialNumber: " + Debug.toString((BigInteger)this.serialNum);
    }

    public void encode(DerOutputStream out) {
        out.putInteger(this.serialNum);
    }

    public BigInteger getNumber() {
        return this.serialNum;
    }
}

