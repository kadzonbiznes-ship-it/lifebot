/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.GeneralName;

public final class AccessDescription {
    private int myhash = -1;
    private final ObjectIdentifier accessMethod;
    private final GeneralName accessLocation;
    public static final ObjectIdentifier Ad_OCSP_Id = ObjectIdentifier.of(KnownOIDs.OCSP);
    public static final ObjectIdentifier Ad_CAISSUERS_Id = ObjectIdentifier.of(KnownOIDs.caIssuers);
    public static final ObjectIdentifier Ad_TIMESTAMPING_Id = ObjectIdentifier.of(KnownOIDs.AD_TimeStamping);
    public static final ObjectIdentifier Ad_CAREPOSITORY_Id = ObjectIdentifier.of(KnownOIDs.caRepository);

    public AccessDescription(ObjectIdentifier accessMethod, GeneralName accessLocation) {
        this.accessMethod = accessMethod;
        this.accessLocation = accessLocation;
    }

    public AccessDescription(DerValue derValue) throws IOException {
        DerInputStream derIn = derValue.getData();
        this.accessMethod = derIn.getOID();
        this.accessLocation = new GeneralName(derIn.getDerValue());
    }

    public ObjectIdentifier getAccessMethod() {
        return this.accessMethod;
    }

    public GeneralName getAccessLocation() {
        return this.accessLocation;
    }

    public void encode(DerOutputStream out) {
        DerOutputStream tmp = new DerOutputStream();
        tmp.putOID(this.accessMethod);
        this.accessLocation.encode(tmp);
        out.write((byte)48, tmp);
    }

    public int hashCode() {
        if (this.myhash == -1) {
            this.myhash = this.accessMethod.hashCode() + this.accessLocation.hashCode();
        }
        return this.myhash;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof AccessDescription)) {
            return false;
        }
        AccessDescription that = (AccessDescription)obj;
        if (this == that) {
            return true;
        }
        return this.accessMethod.equals(that.getAccessMethod()) && this.accessLocation.equals(that.getAccessLocation());
    }

    public String toString() {
        String method = this.accessMethod.equals(Ad_CAISSUERS_Id) ? "caIssuers" : (this.accessMethod.equals(Ad_CAREPOSITORY_Id) ? "caRepository" : (this.accessMethod.equals(Ad_TIMESTAMPING_Id) ? "timeStamping" : (this.accessMethod.equals(Ad_OCSP_Id) ? "ocsp" : this.accessMethod.toString())));
        return "\n   accessMethod: " + method + "\n   accessLocation: " + this.accessLocation.toString() + "\n";
    }
}

