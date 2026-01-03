/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Random;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.SerialNumber;

public class CertificateSerialNumber
implements DerEncoder {
    public static final String NAME = "serialNumber";
    private SerialNumber serial;

    public CertificateSerialNumber(BigInteger num) {
        this.serial = new SerialNumber(num);
    }

    public CertificateSerialNumber(int num) {
        this.serial = new SerialNumber(num);
    }

    public CertificateSerialNumber(DerInputStream in) throws IOException {
        this.serial = new SerialNumber(in);
    }

    public CertificateSerialNumber(InputStream in) throws IOException {
        this.serial = new SerialNumber(in);
    }

    public CertificateSerialNumber(DerValue val) throws IOException {
        this.serial = new SerialNumber(val);
    }

    public String toString() {
        if (this.serial == null) {
            return "";
        }
        return this.serial.toString();
    }

    @Override
    public void encode(DerOutputStream out) {
        this.serial.encode(out);
    }

    public SerialNumber getSerial() {
        return this.serial;
    }

    public static CertificateSerialNumber newRandom64bit(Random rand) {
        BigInteger b;
        while ((b = new BigInteger(64, rand)).signum() == 0) {
        }
        return new CertificateSerialNumber(b);
    }
}

