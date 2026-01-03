/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.math.BigInteger;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import sun.security.util.DerOutputStream;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;

public final class NamedCurve
extends ECParameterSpec {
    private final String[] nameAndAliases;
    private final String oid;
    private final byte[] encoded;

    NamedCurve(KnownOIDs ko, EllipticCurve curve, ECPoint g, BigInteger n, int h) {
        super(curve, g, n, h);
        String[] aliases = ko.aliases();
        this.nameAndAliases = new String[aliases.length + 1];
        this.nameAndAliases[0] = ko.stdName();
        System.arraycopy(aliases, 0, this.nameAndAliases, 1, aliases.length);
        this.oid = ko.value();
        DerOutputStream out = new DerOutputStream();
        out.putOID(ObjectIdentifier.of(ko));
        this.encoded = out.toByteArray();
    }

    public String[] getNameAndAliases() {
        return this.nameAndAliases;
    }

    public byte[] getEncoded() {
        return (byte[])this.encoded.clone();
    }

    public String getObjectId() {
        return this.oid;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.nameAndAliases[0]);
        if (this.nameAndAliases.length > 1) {
            sb.append(" [");
            int j = 1;
            while (j < this.nameAndAliases.length - 1) {
                sb.append(this.nameAndAliases[j++]);
                sb.append(',');
            }
            sb.append(this.nameAndAliases[j] + "]");
        }
        sb.append(" (" + this.oid + ")");
        return sb.toString();
    }
}

