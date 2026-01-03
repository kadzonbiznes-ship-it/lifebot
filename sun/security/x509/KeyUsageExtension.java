/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import sun.security.util.BitArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.Extension;
import sun.security.x509.PKIXExtensions;

public class KeyUsageExtension
extends Extension {
    public static final String NAME = "KeyUsage";
    public static final String DIGITAL_SIGNATURE = "digital_signature";
    public static final String NON_REPUDIATION = "non_repudiation";
    public static final String KEY_ENCIPHERMENT = "key_encipherment";
    public static final String DATA_ENCIPHERMENT = "data_encipherment";
    public static final String KEY_AGREEMENT = "key_agreement";
    public static final String KEY_CERTSIGN = "key_certsign";
    public static final String CRL_SIGN = "crl_sign";
    public static final String ENCIPHER_ONLY = "encipher_only";
    public static final String DECIPHER_ONLY = "decipher_only";
    private boolean[] bitString;

    private void encodeThis() {
        DerOutputStream os = new DerOutputStream();
        os.putTruncatedUnalignedBitString(new BitArray(this.bitString));
        this.extensionValue = os.toByteArray();
    }

    private boolean isSet(int position) {
        return position < this.bitString.length && this.bitString[position];
    }

    private void set(int position, boolean val) {
        if (position >= this.bitString.length) {
            boolean[] tmp = new boolean[position + 1];
            System.arraycopy(this.bitString, 0, tmp, 0, this.bitString.length);
            this.bitString = tmp;
        }
        this.bitString[position] = val;
    }

    public KeyUsageExtension(byte[] bitString) {
        this.bitString = new BitArray(bitString.length * 8, bitString).toBooleanArray();
        this.extensionId = PKIXExtensions.KeyUsage_Id;
        this.critical = true;
        this.encodeThis();
    }

    public KeyUsageExtension(boolean[] bitString) {
        this.bitString = bitString;
        this.extensionId = PKIXExtensions.KeyUsage_Id;
        this.critical = true;
        this.encodeThis();
    }

    public KeyUsageExtension(BitArray bitString) {
        this.bitString = bitString.toBooleanArray();
        this.extensionId = PKIXExtensions.KeyUsage_Id;
        this.critical = true;
        this.encodeThis();
    }

    public KeyUsageExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = PKIXExtensions.KeyUsage_Id;
        this.critical = critical;
        byte[] extValue = (byte[])value;
        this.extensionValue = extValue[0] == 4 ? new DerValue(extValue).getOctetString() : extValue;
        DerValue val = new DerValue(this.extensionValue);
        this.bitString = val.getUnalignedBitString().toBooleanArray();
    }

    public KeyUsageExtension() {
        this.extensionId = PKIXExtensions.KeyUsage_Id;
        this.critical = true;
        this.bitString = new boolean[0];
    }

    public void set(String name, boolean val) throws IOException {
        if (name.equalsIgnoreCase(DIGITAL_SIGNATURE)) {
            this.set(0, val);
        } else if (name.equalsIgnoreCase(NON_REPUDIATION)) {
            this.set(1, val);
        } else if (name.equalsIgnoreCase(KEY_ENCIPHERMENT)) {
            this.set(2, val);
        } else if (name.equalsIgnoreCase(DATA_ENCIPHERMENT)) {
            this.set(3, val);
        } else if (name.equalsIgnoreCase(KEY_AGREEMENT)) {
            this.set(4, val);
        } else if (name.equalsIgnoreCase(KEY_CERTSIGN)) {
            this.set(5, val);
        } else if (name.equalsIgnoreCase(CRL_SIGN)) {
            this.set(6, val);
        } else if (name.equalsIgnoreCase(ENCIPHER_ONLY)) {
            this.set(7, val);
        } else if (name.equalsIgnoreCase(DECIPHER_ONLY)) {
            this.set(8, val);
        } else {
            throw new IOException("Attribute name not recognized by KeyUsage.");
        }
        this.encodeThis();
    }

    public boolean get(String name) throws IOException {
        if (name.equalsIgnoreCase(DIGITAL_SIGNATURE)) {
            return this.isSet(0);
        }
        if (name.equalsIgnoreCase(NON_REPUDIATION)) {
            return this.isSet(1);
        }
        if (name.equalsIgnoreCase(KEY_ENCIPHERMENT)) {
            return this.isSet(2);
        }
        if (name.equalsIgnoreCase(DATA_ENCIPHERMENT)) {
            return this.isSet(3);
        }
        if (name.equalsIgnoreCase(KEY_AGREEMENT)) {
            return this.isSet(4);
        }
        if (name.equalsIgnoreCase(KEY_CERTSIGN)) {
            return this.isSet(5);
        }
        if (name.equalsIgnoreCase(CRL_SIGN)) {
            return this.isSet(6);
        }
        if (name.equalsIgnoreCase(ENCIPHER_ONLY)) {
            return this.isSet(7);
        }
        if (name.equalsIgnoreCase(DECIPHER_ONLY)) {
            return this.isSet(8);
        }
        throw new IOException("Attribute name not recognized by KeyUsage.");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("KeyUsage [\n");
        if (this.isSet(0)) {
            sb.append("  DigitalSignature\n");
        }
        if (this.isSet(1)) {
            sb.append("  Non_repudiation\n");
        }
        if (this.isSet(2)) {
            sb.append("  Key_Encipherment\n");
        }
        if (this.isSet(3)) {
            sb.append("  Data_Encipherment\n");
        }
        if (this.isSet(4)) {
            sb.append("  Key_Agreement\n");
        }
        if (this.isSet(5)) {
            sb.append("  Key_CertSign\n");
        }
        if (this.isSet(6)) {
            sb.append("  Crl_Sign\n");
        }
        if (this.isSet(7)) {
            sb.append("  Encipher_Only\n");
        }
        if (this.isSet(8)) {
            sb.append("  Decipher_Only\n");
        }
        sb.append("]\n");
        return sb.toString();
    }

    @Override
    public void encode(DerOutputStream out) {
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.KeyUsage_Id;
            this.critical = true;
            this.encodeThis();
        }
        super.encode(out);
    }

    public boolean[] getBits() {
        return (boolean[])this.bitString.clone();
    }

    @Override
    public String getName() {
        return NAME;
    }
}

