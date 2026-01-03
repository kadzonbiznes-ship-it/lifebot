/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import sun.security.util.BitArray;
import sun.security.util.DerEncoder;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.GeneralNames;
import sun.security.x509.RDN;

public class DistributionPoint
implements DerEncoder {
    public static final int KEY_COMPROMISE = 1;
    public static final int CA_COMPROMISE = 2;
    public static final int AFFILIATION_CHANGED = 3;
    public static final int SUPERSEDED = 4;
    public static final int CESSATION_OF_OPERATION = 5;
    public static final int CERTIFICATE_HOLD = 6;
    public static final int PRIVILEGE_WITHDRAWN = 7;
    public static final int AA_COMPROMISE = 8;
    private static final String[] REASON_STRINGS = new String[]{null, "key compromise", "CA compromise", "affiliation changed", "superseded", "cessation of operation", "certificate hold", "privilege withdrawn", "AA compromise"};
    private static final byte TAG_DIST_PT = 0;
    private static final byte TAG_REASONS = 1;
    private static final byte TAG_ISSUER = 2;
    private static final byte TAG_FULL_NAME = 0;
    private static final byte TAG_REL_NAME = 1;
    private GeneralNames fullName;
    private RDN relativeName;
    private boolean[] reasonFlags;
    private GeneralNames crlIssuer;
    private volatile int hashCode;

    public DistributionPoint(GeneralNames fullName, boolean[] reasonFlags, GeneralNames crlIssuer) {
        if (fullName == null && crlIssuer == null) {
            throw new IllegalArgumentException("fullName and crlIssuer may not both be null");
        }
        this.fullName = fullName;
        this.reasonFlags = reasonFlags;
        this.crlIssuer = crlIssuer;
    }

    public DistributionPoint(RDN relativeName, boolean[] reasonFlags, GeneralNames crlIssuer) {
        if (relativeName == null && crlIssuer == null) {
            throw new IllegalArgumentException("relativeName and crlIssuer may not both be null");
        }
        this.relativeName = relativeName;
        this.reasonFlags = reasonFlags;
        this.crlIssuer = crlIssuer;
    }

    public DistributionPoint(DerValue val) throws IOException {
        if (val.tag != 48) {
            throw new IOException("Invalid encoding of DistributionPoint.");
        }
        while (val.data != null && val.data.available() != 0) {
            DerValue opt = val.data.getDerValue();
            if (opt.isContextSpecific((byte)0) && opt.isConstructed()) {
                if (this.fullName != null || this.relativeName != null) {
                    throw new IOException("Duplicate DistributionPointName in DistributionPoint.");
                }
                DerValue distPnt = opt.data.getDerValue();
                if (distPnt.isContextSpecific((byte)0) && distPnt.isConstructed()) {
                    distPnt.resetTag((byte)48);
                    this.fullName = new GeneralNames(distPnt);
                    continue;
                }
                if (distPnt.isContextSpecific((byte)1) && distPnt.isConstructed()) {
                    distPnt.resetTag((byte)49);
                    this.relativeName = new RDN(distPnt);
                    continue;
                }
                throw new IOException("Invalid DistributionPointName in DistributionPoint");
            }
            if (opt.isContextSpecific((byte)1) && !opt.isConstructed()) {
                if (this.reasonFlags != null) {
                    throw new IOException("Duplicate Reasons in DistributionPoint.");
                }
                opt.resetTag((byte)3);
                this.reasonFlags = opt.getUnalignedBitString().toBooleanArray();
                continue;
            }
            if (opt.isContextSpecific((byte)2) && opt.isConstructed()) {
                if (this.crlIssuer != null) {
                    throw new IOException("Duplicate CRLIssuer in DistributionPoint.");
                }
                opt.resetTag((byte)48);
                this.crlIssuer = new GeneralNames(opt);
                continue;
            }
            throw new IOException("Invalid encoding of DistributionPoint.");
        }
        if (this.crlIssuer == null && this.fullName == null && this.relativeName == null) {
            throw new IOException("One of fullName, relativeName,  and crlIssuer has to be set");
        }
    }

    public GeneralNames getFullName() {
        return this.fullName;
    }

    public RDN getRelativeName() {
        return this.relativeName;
    }

    public boolean[] getReasonFlags() {
        return this.reasonFlags;
    }

    public GeneralNames getCRLIssuer() {
        return this.crlIssuer;
    }

    @Override
    public void encode(DerOutputStream out) {
        DerOutputStream tagged = new DerOutputStream();
        if (this.fullName != null || this.relativeName != null) {
            DerOutputStream distributionPoint = new DerOutputStream();
            if (this.fullName != null) {
                derOut = new DerOutputStream();
                this.fullName.encode(derOut);
                distributionPoint.writeImplicit(DerValue.createTag((byte)-128, true, (byte)0), derOut);
            } else if (this.relativeName != null) {
                derOut = new DerOutputStream();
                this.relativeName.encode(derOut);
                distributionPoint.writeImplicit(DerValue.createTag((byte)-128, true, (byte)1), derOut);
            }
            tagged.write(DerValue.createTag((byte)-128, true, (byte)0), distributionPoint);
        }
        if (this.reasonFlags != null) {
            DerOutputStream reasons = new DerOutputStream();
            BitArray rf = new BitArray(this.reasonFlags);
            reasons.putTruncatedUnalignedBitString(rf);
            tagged.writeImplicit(DerValue.createTag((byte)-128, false, (byte)1), reasons);
        }
        if (this.crlIssuer != null) {
            DerOutputStream issuer = new DerOutputStream();
            this.crlIssuer.encode(issuer);
            tagged.writeImplicit(DerValue.createTag((byte)-128, true, (byte)2), issuer);
        }
        out.write((byte)48, tagged);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DistributionPoint)) {
            return false;
        }
        DistributionPoint other = (DistributionPoint)obj;
        return Objects.equals(this.fullName, other.fullName) && Objects.equals(this.relativeName, other.relativeName) && Objects.equals(this.crlIssuer, other.crlIssuer) && Arrays.equals(this.reasonFlags, other.reasonFlags);
    }

    public int hashCode() {
        int hash = this.hashCode;
        if (hash == 0) {
            hash = 1;
            if (this.fullName != null) {
                hash += this.fullName.hashCode();
            }
            if (this.relativeName != null) {
                hash += this.relativeName.hashCode();
            }
            if (this.crlIssuer != null) {
                hash += this.crlIssuer.hashCode();
            }
            if (this.reasonFlags != null) {
                for (int i = 0; i < this.reasonFlags.length; ++i) {
                    if (!this.reasonFlags[i]) continue;
                    hash += i;
                }
            }
            this.hashCode = hash;
        }
        return hash;
    }

    private static String reasonToString(int reason) {
        if (reason > 0 && reason < REASON_STRINGS.length) {
            return REASON_STRINGS[reason];
        }
        return "Unknown reason " + reason;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DistributionPoint:\n     ");
        if (this.fullName != null) {
            sb.append(this.fullName);
        }
        if (this.relativeName != null) {
            sb.append(this.relativeName);
        }
        sb.append('\n');
        if (this.reasonFlags != null) {
            sb.append("   ReasonFlags:\n");
            for (int i = 0; i < this.reasonFlags.length; ++i) {
                if (!this.reasonFlags[i]) continue;
                sb.append("    ").append(DistributionPoint.reasonToString(i)).append('\n');
            }
        }
        if (this.crlIssuer != null) {
            sb.append("   CRLIssuer:").append(this.crlIssuer).append('\n');
        }
        return sb.toString();
    }
}

