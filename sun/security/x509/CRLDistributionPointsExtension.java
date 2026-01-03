/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.DistributionPoint;
import sun.security.x509.Extension;
import sun.security.x509.PKIXExtensions;

public class CRLDistributionPointsExtension
extends Extension {
    public static final String NAME = "CRLDistributionPoints";
    private List<DistributionPoint> distributionPoints;
    private final String extensionName;

    public CRLDistributionPointsExtension(List<DistributionPoint> distributionPoints) {
        this(false, distributionPoints);
    }

    public CRLDistributionPointsExtension(boolean isCritical, List<DistributionPoint> distributionPoints) {
        this(PKIXExtensions.CRLDistributionPoints_Id, isCritical, distributionPoints, NAME);
    }

    protected CRLDistributionPointsExtension(ObjectIdentifier extensionId, boolean isCritical, List<DistributionPoint> distributionPoints, String extensionName) {
        if (distributionPoints == null || distributionPoints.isEmpty()) {
            throw new IllegalArgumentException("distribution points cannot be null or empty");
        }
        this.extensionId = extensionId;
        this.critical = isCritical;
        this.distributionPoints = distributionPoints;
        this.encodeThis();
        this.extensionName = extensionName;
    }

    public CRLDistributionPointsExtension(Boolean critical, Object value) throws IOException {
        this(PKIXExtensions.CRLDistributionPoints_Id, critical, value, NAME);
    }

    protected CRLDistributionPointsExtension(ObjectIdentifier extensionId, Boolean critical, Object value, String extensionName) throws IOException {
        this.extensionId = extensionId;
        this.critical = critical;
        if (!(value instanceof byte[])) {
            throw new IOException("Illegal argument type");
        }
        this.extensionValue = (byte[])value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag != 48) {
            throw new IOException("Invalid encoding for " + extensionName + " extension.");
        }
        this.distributionPoints = new ArrayList<DistributionPoint>();
        while (val.data.available() != 0) {
            DerValue seq = val.data.getDerValue();
            DistributionPoint point = new DistributionPoint(seq);
            this.distributionPoints.add(point);
        }
        this.extensionName = extensionName;
    }

    @Override
    public String getName() {
        return this.extensionName;
    }

    @Override
    public void encode(DerOutputStream out) {
        this.encode(out, PKIXExtensions.CRLDistributionPoints_Id, false);
    }

    protected void encode(DerOutputStream out, ObjectIdentifier extensionId, boolean isCritical) {
        if (this.extensionValue == null) {
            this.extensionId = extensionId;
            this.critical = isCritical;
            this.encodeThis();
        }
        super.encode(out);
    }

    public List<DistributionPoint> getDistributionPoints() {
        return this.distributionPoints;
    }

    private void encodeThis() {
        if (this.distributionPoints.isEmpty()) {
            this.extensionValue = null;
        } else {
            DerOutputStream pnts = new DerOutputStream();
            for (DistributionPoint point : this.distributionPoints) {
                point.encode(pnts);
            }
            DerOutputStream seq = new DerOutputStream();
            seq.write((byte)48, pnts);
            this.extensionValue = seq.toByteArray();
        }
    }

    @Override
    public String toString() {
        return super.toString() + this.extensionName + " [\n  " + this.distributionPoints + "]\n";
    }
}

