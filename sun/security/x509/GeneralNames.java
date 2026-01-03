/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.GeneralName;

public class GeneralNames {
    private final List<GeneralName> names = new ArrayList<GeneralName>();

    public GeneralNames(DerValue derVal) throws IOException {
        this();
        if (derVal.tag != 48) {
            throw new IOException("Invalid encoding for GeneralNames.");
        }
        if (derVal.data.available() == 0) {
            throw new IOException("No data available in passed DER encoded value.");
        }
        while (derVal.data.available() != 0) {
            DerValue encName = derVal.data.getDerValue();
            GeneralName name = new GeneralName(encName);
            this.add(name);
        }
    }

    public GeneralNames() {
    }

    public GeneralNames add(GeneralName name) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.names.add(name);
        return this;
    }

    public GeneralName get(int index) {
        return this.names.get(index);
    }

    public boolean isEmpty() {
        return this.names.isEmpty();
    }

    public int size() {
        return this.names.size();
    }

    public Iterator<GeneralName> iterator() {
        return this.names.iterator();
    }

    public List<GeneralName> names() {
        return this.names;
    }

    public void encode(DerOutputStream out) {
        if (this.isEmpty()) {
            return;
        }
        DerOutputStream temp = new DerOutputStream();
        for (GeneralName gn : this.names) {
            gn.encode(temp);
        }
        out.write((byte)48, temp);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GeneralNames)) {
            return false;
        }
        GeneralNames other = (GeneralNames)obj;
        return this.names.equals(other.names);
    }

    public int hashCode() {
        return this.names.hashCode();
    }

    public String toString() {
        return this.names.toString();
    }
}

