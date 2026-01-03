/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.Extension;
import sun.security.x509.PKIXExtensions;

public class ExtendedKeyUsageExtension
extends Extension {
    public static final String NAME = "ExtendedKeyUsage";
    private Vector<ObjectIdentifier> keyUsages;

    private void encodeThis() {
        if (this.keyUsages == null || this.keyUsages.isEmpty()) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream os = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        for (int i = 0; i < this.keyUsages.size(); ++i) {
            tmp.putOID(this.keyUsages.elementAt(i));
        }
        os.write((byte)48, tmp);
        this.extensionValue = os.toByteArray();
    }

    public ExtendedKeyUsageExtension(Vector<ObjectIdentifier> keyUsages) {
        this(Boolean.FALSE, keyUsages);
    }

    public ExtendedKeyUsageExtension(Boolean critical, Vector<ObjectIdentifier> keyUsages) {
        if (keyUsages == null || keyUsages.isEmpty()) {
            throw new IllegalArgumentException("key usages cannot be null or empty");
        }
        this.keyUsages = keyUsages;
        this.extensionId = PKIXExtensions.ExtendedKeyUsage_Id;
        this.critical = critical;
        this.encodeThis();
    }

    public ExtendedKeyUsageExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = PKIXExtensions.ExtendedKeyUsage_Id;
        this.critical = critical;
        this.extensionValue = (byte[])value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag != 48) {
            throw new IOException("Invalid encoding for ExtendedKeyUsageExtension.");
        }
        this.keyUsages = new Vector();
        while (val.data.available() != 0) {
            DerValue seq = val.data.getDerValue();
            ObjectIdentifier usage = seq.getOID();
            this.keyUsages.addElement(usage);
        }
    }

    @Override
    public String toString() {
        if (this.keyUsages == null) {
            return "";
        }
        String usage = "  ";
        boolean first = true;
        for (ObjectIdentifier oid : this.keyUsages) {
            String res;
            KnownOIDs os;
            if (!first) {
                usage = usage + "\n  ";
            }
            usage = (os = KnownOIDs.findMatch(res = oid.toString())) != null ? usage + os.stdName() : usage + res;
            first = false;
        }
        return super.toString() + "ExtendedKeyUsages [\n" + usage + "\n]\n";
    }

    @Override
    public void encode(DerOutputStream out) {
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.ExtendedKeyUsage_Id;
            this.critical = false;
            this.encodeThis();
        }
        super.encode(out);
    }

    public Vector<ObjectIdentifier> getUsages() {
        return this.keyUsages;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public List<String> getExtendedKeyUsage() {
        ArrayList<String> al = new ArrayList<String>(this.keyUsages.size());
        for (ObjectIdentifier oid : this.keyUsages) {
            al.add(oid.toString());
        }
        return al;
    }
}

