/*
 * Decompiled with CFR 0.152.
 */
package java.security.cert;

import java.io.IOException;
import sun.security.util.DerValue;
import sun.security.util.HexDumpEncoder;

public class PolicyQualifierInfo {
    private final byte[] mEncoded;
    private final String mId;
    private final byte[] mData;
    private String pqiString;

    public PolicyQualifierInfo(byte[] encoded) throws IOException {
        this.mEncoded = (byte[])encoded.clone();
        DerValue val = new DerValue(this.mEncoded);
        if (val.tag != 48) {
            throw new IOException("Invalid encoding for PolicyQualifierInfo");
        }
        this.mId = val.data.getDerValue().getOID().toString();
        byte[] tmp = val.data.toByteArray();
        if (tmp == null) {
            this.mData = null;
        } else {
            this.mData = new byte[tmp.length];
            System.arraycopy(tmp, 0, this.mData, 0, tmp.length);
        }
    }

    public final String getPolicyQualifierId() {
        return this.mId;
    }

    public final byte[] getEncoded() {
        return (byte[])this.mEncoded.clone();
    }

    public final byte[] getPolicyQualifier() {
        return this.mData == null ? null : (byte[])this.mData.clone();
    }

    public String toString() {
        if (this.pqiString != null) {
            return this.pqiString;
        }
        HexDumpEncoder enc = new HexDumpEncoder();
        this.pqiString = "PolicyQualifierInfo: [\n  qualifierID: " + this.mId + "\n  qualifier: " + (this.mData == null ? "null" : enc.encodeBuffer(this.mData)) + "\n]";
        return this.pqiString;
    }
}

