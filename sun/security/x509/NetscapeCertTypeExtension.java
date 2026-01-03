/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import sun.security.util.BitArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.Extension;
import sun.security.x509.KeyUsageExtension;

public class NetscapeCertTypeExtension
extends Extension {
    public static final String NAME = "NetscapeCertType";
    public static final String SSL_CLIENT = "ssl_client";
    public static final String SSL_SERVER = "ssl_server";
    public static final String S_MIME = "s_mime";
    public static final String OBJECT_SIGNING = "object_signing";
    public static final String SSL_CA = "ssl_ca";
    public static final String S_MIME_CA = "s_mime_ca";
    public static final String OBJECT_SIGNING_CA = "object_signing_ca";
    public static ObjectIdentifier NetscapeCertType_Id = ObjectIdentifier.of(KnownOIDs.NETSCAPE_CertType);
    private boolean[] bitString;
    private static final MapEntry[] mMapData = new MapEntry[]{new MapEntry("ssl_client", 0), new MapEntry("ssl_server", 1), new MapEntry("s_mime", 2), new MapEntry("object_signing", 3), new MapEntry("ssl_ca", 5), new MapEntry("s_mime_ca", 6), new MapEntry("object_signing_ca", 7)};

    private static int getPosition(String name) throws IOException {
        for (int i = 0; i < mMapData.length; ++i) {
            if (!name.equalsIgnoreCase(NetscapeCertTypeExtension.mMapData[i].mName)) continue;
            return NetscapeCertTypeExtension.mMapData[i].mPosition;
        }
        throw new IOException("Attribute name [" + name + "] not recognized by NetscapeCertType.");
    }

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

    public NetscapeCertTypeExtension(byte[] bitString) {
        this.bitString = new BitArray(bitString.length * 8, bitString).toBooleanArray();
        this.extensionId = NetscapeCertType_Id;
        this.critical = true;
        this.encodeThis();
    }

    public NetscapeCertTypeExtension(boolean[] bitString) {
        this.bitString = bitString;
        this.extensionId = NetscapeCertType_Id;
        this.critical = true;
        this.encodeThis();
    }

    public NetscapeCertTypeExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = NetscapeCertType_Id;
        this.critical = critical;
        this.extensionValue = (byte[])value;
        DerValue val = new DerValue(this.extensionValue);
        this.bitString = val.getUnalignedBitString().toBooleanArray();
    }

    public NetscapeCertTypeExtension() {
        this.extensionId = NetscapeCertType_Id;
        this.critical = true;
        this.bitString = new boolean[0];
    }

    public void set(String name, Boolean val) throws IOException {
        this.set(NetscapeCertTypeExtension.getPosition(name), (boolean)val);
        this.encodeThis();
    }

    public boolean get(String name) throws IOException {
        return this.isSet(NetscapeCertTypeExtension.getPosition(name));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("NetscapeCertType [\n");
        if (this.isSet(0)) {
            sb.append("   SSL client\n");
        }
        if (this.isSet(1)) {
            sb.append("   SSL server\n");
        }
        if (this.isSet(2)) {
            sb.append("   S/MIME\n");
        }
        if (this.isSet(3)) {
            sb.append("   Object Signing\n");
        }
        if (this.isSet(5)) {
            sb.append("   SSL CA\n");
        }
        if (this.isSet(6)) {
            sb.append("   S/MIME CA\n");
        }
        if (this.isSet(7)) {
            sb.append("   Object Signing CA");
        }
        sb.append("]\n");
        return sb.toString();
    }

    @Override
    public void encode(DerOutputStream out) {
        if (this.extensionValue == null) {
            this.extensionId = NetscapeCertType_Id;
            this.critical = true;
            this.encodeThis();
        }
        super.encode(out);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public boolean[] getKeyUsageMappedBits() {
        KeyUsageExtension keyUsage = new KeyUsageExtension();
        try {
            if (this.isSet(NetscapeCertTypeExtension.getPosition(SSL_CLIENT)) || this.isSet(NetscapeCertTypeExtension.getPosition(S_MIME)) || this.isSet(NetscapeCertTypeExtension.getPosition(OBJECT_SIGNING))) {
                keyUsage.set("digital_signature", true);
            }
            if (this.isSet(NetscapeCertTypeExtension.getPosition(SSL_SERVER))) {
                keyUsage.set("key_encipherment", true);
            }
            if (this.isSet(NetscapeCertTypeExtension.getPosition(SSL_CA)) || this.isSet(NetscapeCertTypeExtension.getPosition(S_MIME_CA)) || this.isSet(NetscapeCertTypeExtension.getPosition(OBJECT_SIGNING_CA))) {
                keyUsage.set("key_certsign", true);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return keyUsage.getBits();
    }

    private static class MapEntry {
        String mName;
        int mPosition;

        MapEntry(String name, int position) {
            this.mName = name;
            this.mPosition = position;
        }
    }
}

