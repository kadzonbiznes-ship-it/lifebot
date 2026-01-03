/*
 * Decompiled with CFR 0.152.
 */
package javax.net.ssl;

import java.util.Arrays;
import java.util.HexFormat;

public abstract class SNIServerName {
    private final int type;
    private final byte[] encoded;

    protected SNIServerName(int type, byte[] encoded) {
        if (type < 0) {
            throw new IllegalArgumentException("Server name type cannot be less than zero");
        }
        if (type > 255) {
            throw new IllegalArgumentException("Server name type cannot be greater than 255");
        }
        this.type = type;
        if (encoded == null) {
            throw new NullPointerException("Server name encoded value cannot be null");
        }
        this.encoded = (byte[])encoded.clone();
    }

    public final int getType() {
        return this.type;
    }

    public final byte[] getEncoded() {
        return (byte[])this.encoded.clone();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (this.getClass() != other.getClass()) {
            return false;
        }
        SNIServerName that = (SNIServerName)other;
        return this.type == that.type && Arrays.equals(this.encoded, that.encoded);
    }

    public int hashCode() {
        int result = 17;
        result = 31 * result + this.type;
        result = 31 * result + Arrays.hashCode(this.encoded);
        return result;
    }

    public String toString() {
        if (this.type == 0) {
            return "type=host_name (0), value=" + SNIServerName.toHexString(this.encoded);
        }
        return "type=(" + this.type + "), value=" + SNIServerName.toHexString(this.encoded);
    }

    private static String toHexString(byte[] bytes) {
        if (bytes.length == 0) {
            return "(empty)";
        }
        return HexFormat.ofDelimiter(":").withUpperCase().formatHex(bytes);
    }
}

